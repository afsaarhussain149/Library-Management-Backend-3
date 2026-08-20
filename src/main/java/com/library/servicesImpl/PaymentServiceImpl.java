package com.library.servicesImpl;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.bean.ChangeSeatRequest;
import com.library.bean.JavaConstant;
import com.library.bean.PaymentDetails;
import com.library.bean.UpdateStatusRequest;
import com.library.bean.VerifyPaymentRequest;
import com.library.dao.IGenericDao;
import com.library.services.PaymentService;
import com.library.util.ShiftTimeUtil;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;

import jakarta.transaction.Transactional;

@Service
@SuppressWarnings({ "rawtypes", "unchecked" })
public class PaymentServiceImpl implements PaymentService {

	@Autowired
	IGenericDao iGenericDao;

	@Value("${razorpay.key-id}")
	private String razorpayKeyId;

	@Value("${razorpay.key-secret}")
	private String razorpayKeySecret;

	private final ObjectMapper objectMapper = new ObjectMapper();

	private String seatsToString(List<Integer> seats) {
		if (seats == null || seats.isEmpty()) return "";
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < seats.size(); i++) {
			sb.append(seats.get(i));
			if (i < seats.size() - 1) sb.append(",");
		}
		return sb.toString();
	}

	private List<Integer> stringToSeats(Object seatsObj) {
		List<Integer> list = new ArrayList<>();
		if (seatsObj == null) return list;
		String seatsStr = String.valueOf(seatsObj).trim();
		if (seatsStr.isEmpty()) return list;
		for (String s : seatsStr.split(",")) {
			if (!s.trim().isEmpty()) list.add(Integer.parseInt(s.trim()));
		}
		return list;
	}

	private String toJson(Object o) {
		try {
			return o == null ? "{}" : objectMapper.writeValueAsString(o);
		} catch (Exception e) {
			return "{}";
		}
	}

	private int getPageLimit() { return 10; }

	/**
	 * Checks whether any of {@code seats} clash (same seat + overlapping shift time) with another
	 * currently active, paid booking. Bookings belonging to {@code excludeUserId} are ignored so a
	 * student's own renewal/edit never conflicts with their own existing booking, and the booking
	 * identified by {@code excludePaymentId} (if any) is also skipped.
	 *
	 * Returns the first conflicting seat number, or null if there is no clash.
	 */
	private Integer findConflictingSeat(List<Integer> seats, String shiftTime, Object excludeUserId,
			Object excludePaymentId) {
		if (seats == null || seats.isEmpty() || shiftTime == null) return null;

		ShiftTimeUtil.Range requestedRange = ShiftTimeUtil.parseShift(shiftTime);
		List<Map> otherActivePayments = excludeUserId != null
				? iGenericDao.executeDDLSQL(JavaConstant.GET_ACTIVE_PAID_PAYMENTS_EXCLUDING_USER,
						new Object[] { String.valueOf(excludeUserId) })
				: iGenericDao.executeDDLSQL(JavaConstant.GET_ALL_PAID_PAYMENTS, new Object[] {});

		for (Integer seatNo : seats) {
			for (Map other : otherActivePayments) {
				if (excludePaymentId != null
						&& String.valueOf(excludePaymentId).equals(String.valueOf(other.get("payment_id")))) {
					continue;
				}
				Boolean isActive = (Boolean) other.get("is_active");
				if (!Boolean.TRUE.equals(isActive)) continue;

				List<Integer> otherSeats = stringToSeats(other.get("seats"));
				if (!otherSeats.contains(seatNo)) continue;

				String otherShiftTime = (String) other.get("shift_time");
				if (otherShiftTime == null) continue;

				ShiftTimeUtil.Range otherRange = ShiftTimeUtil.parseShift(otherShiftTime);
				if (ShiftTimeUtil.isOverlap(requestedRange, otherRange)) {
					return seatNo;
				}
			}
		}
		return null;
	}

	// ============ GET /pending-cash-users/:page ============
	@Override
	public Map<String, Object> pendingCashUsers(int page) {
		Map<String, Object> result = new LinkedHashMap<>();
		try {
			int limit = getPageLimit();
			int offset = (page - 1) * limit;

			List<Map> total = iGenericDao.executeDDLSQL(JavaConstant.COUNT_PENDING_CASH, new Object[] {});
			long totalCount = ((Number) total.get(0).values().iterator().next()).longValue();

			String query =
				"select u.user_id as user_id, u.full_name as full_name, u.phone_number as phone_number, " +
				"u.email as email, p.payment_id as payment_id, p.amount as amount, p.plan_hours as plan_hours, " +
				"p.plan_type as plan_type, p.shift_label as shift_label, p.shift_time as shift_time, " +
				"p.seats as seats, p.status as status, p.created_at as payment_created_at " +
				"from app_user u join payment p on p.user_id = CAST(u.user_id AS text) " +
				"where p.payment_mode = 'cash' and p.status = 'pending' " +
				"order by u.created_at desc limit ?1 offset ?2";

			List<Map> data = iGenericDao.executeDDLSQL(query, new Object[] { limit, offset });

			result.put("success", true);
			result.put("page", page);
			result.put("perPage", limit);
			result.put("total", totalCount);
			result.put("totalPages", (int) Math.ceil((double) totalCount / limit));
			result.put("data", data);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			result.put("success", false);
			result.put("message", "Error fetching data");
			return result;
		}
	}

	// ============ GET /users-with-payments/count ============
	@Override
	public Map<String, Object> usersWithPaymentsCount() {
		Map<String, Object> result = new LinkedHashMap<>();
		try {
			List<Map> total = iGenericDao.executeDDLSQL(JavaConstant.COUNT_ALL_USERS, new Object[] {});
			result.put("success", true);
			result.put("totalCount", total.get(0).values().iterator().next());
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			result.put("success", false);
			result.put("message", "Failed to fetch count");
			return result;
		}
	}

	// ============ POST /cash-request ============
	@Override
	@Transactional
	public Map<String, Object> cashRequest(PaymentDetails p) {
		Map<String, Object> result = new LinkedHashMap<>();
		try {
			if (p.getUserId() == null || p.getAmount() == null || p.getPlanHours() == null
					|| p.getShiftLabel() == null || p.getSeats() == null) {
				result.put("success", false);
				result.put("message", "Missing fields");
				return result;
			}
			
			List<Map> existingPending = iGenericDao.executeDDLSQL(JavaConstant.GET_LATEST_PENDING_CASH_BY_USER,
			        new Object[] { p.getUserId() });
			if (existingPending != null && !existingPending.isEmpty()) {
			    result.put("success", false);
			    result.put("message", "You already have a cash payment request pending admin approval. "
			            + "Please wait until it is approved or rejected before sending another request.");
			    return result;
			}

			// Safety-net: re-validate seat/time overlap on the server even though the
			// frontend already checks availability, so a race between two students (or a
			// stale UI) can never double-book the same seat for an overlapping shift.
			// The requesting student's own existing bookings are excluded so renewing on
			// the same seat/shift never blocks itself.
			Integer conflictingSeat = findConflictingSeat(p.getSeats(), p.getShiftTime(), p.getUserId(), null);
			if (conflictingSeat != null) {
				result.put("success", false);
				result.put("message", "Seat " + conflictingSeat + " is already booked for an overlapping shift");
				return result;
			}

			iGenericDao.executeDMLSQL(JavaConstant.INSERT_CASH_PAYMENT, new Object[] {
					p.getUserId(), p.getAmount(), p.getPlanHours(), p.getPlanType(), p.getPlanAmount(),
					p.getShiftLabel(), p.getShiftTime(), seatsToString(p.getSeats()), toJson(p.getMetadata()),
					p.getEndPlanDate()
			});

			List<Map> inserted = iGenericDao.executeDDLSQL(JavaConstant.GET_LATEST_PENDING_CASH_BY_USER,
					new Object[] { p.getUserId() });

			result.put("success", true);
			result.put("message", "Cash request submitted, wait for admin approval");
			result.put("paymentId", inserted != null && !inserted.isEmpty() ? inserted.get(0).get("payment_id") : null);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			result.put("success", false);
			result.put("message", "Error creating cash request");
			return result;
		}
	}
	
	// ============ PATCH /reject/:id ============
	// Companion to approve: lets admin reject a wrong/unwanted pending cash
	// request. This is important - since a user can only have ONE pending cash
	// request at a time (see cashRequest()), without a way to reject a bad one
	// the student would be stuck forever, unable to submit a corrected request.
	@Override
	@Transactional
	public Map<String, Object> rejectCashPayment(String userId) {
	    Map<String, Object> result = new LinkedHashMap<>();
	    try {
	        List<Map> payments = iGenericDao.executeDDLSQL(JavaConstant.GET_LATEST_PENDING_CASH_BY_USER,
	                new Object[] { userId });

	        if (payments == null || payments.isEmpty()) {
	            result.put("success", false);
	            result.put("message", "No pending cash payment found");
	            return result;
	        }

	        Object paymentId = payments.get(0).get("payment_id");
	        iGenericDao.executeDMLSQL(JavaConstant.UPDATE_PAYMENT_REJECT_CASH, new Object[] { paymentId });

	        List<Map> updated = iGenericDao.executeDDLSQL(JavaConstant.GET_PAYMENT_BY_ID, new Object[] { paymentId });

	        result.put("success", true);
	        result.put("message", "Cash payment rejected");
	        result.put("payment", updated.get(0));
	        return result;
	    } catch (Exception e) {
	        e.printStackTrace();
	        result.put("success", false);
	        result.put("message", "Rejection failed");
	        return result;
	    }
	}

	// ============ PATCH /approve/:id ============
	@Override
	@Transactional
	public Map<String, Object> approveCashPayment(String userId) {
		Map<String, Object> result = new LinkedHashMap<>();
		try {
			List<Map> payments = iGenericDao.executeDDLSQL(JavaConstant.GET_LATEST_PENDING_CASH_BY_USER,
					new Object[] { userId });

			if (payments == null || payments.isEmpty()) {
				result.put("success", false);
				result.put("message", "No pending cash payment found");
				return result;
			}

			Object paymentId = payments.get(0).get("payment_id");
			iGenericDao.executeDMLSQL(JavaConstant.UPDATE_PAYMENT_APPROVE_CASH, new Object[] { paymentId });

			List<Map> updated = iGenericDao.executeDDLSQL(JavaConstant.GET_PAYMENT_BY_ID, new Object[] { paymentId });

			result.put("success", true);
			result.put("message", "Cash payment approved successfully");
			result.put("payment", updated.get(0));
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			result.put("success", false);
			result.put("message", "Approval failed");
			return result;
		}
	}

	// ============ GET /pending-cash ============
	@Override
	public Map<String, Object> pendingCash() {
		Map<String, Object> result = new LinkedHashMap<>();
		List<Map> pending = iGenericDao.executeDDLSQL(JavaConstant.GET_ALL_PENDING_CASH, new Object[] {});
		result.put("success", true);
		result.put("total", pending.size());
		result.put("data", pending);
		return result;
	}

	// ============ GET /users/active-inactive-count ============
	@Override
	public Map<String, Object> activeInactiveCount() {
		Map<String, Object> result = new LinkedHashMap<>();
		try {
			List<Map> rows = iGenericDao.executeDDLSQL(JavaConstant.GET_ACTIVE_INACTIVE_COUNT, new Object[] {});
			long active = 0, inactive = 0;
			for (Map row : rows) {
				Boolean isActive = (Boolean) row.get("is_active");
				long cnt = ((Number) row.get("cnt")).longValue();
				if (Boolean.TRUE.equals(isActive)) active = cnt;
				else inactive = cnt;
			}
			result.put("success", true);
			result.put("activeUsers", active);
			result.put("inactiveUsers", inactive);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			result.put("success", false);
			result.put("message", e.getMessage());
			return result;
		}
	}

	// ============ PUT /update-status ============
	@Override
	@Transactional
	public Map<String, Object> updateStatus(UpdateStatusRequest request) {
		Map<String, Object> result = new LinkedHashMap<>();
		try {
			List<Map> users = iGenericDao.executeDDLSQL(JavaConstant.GET_USER_BY_PHONE,
					new Object[] { request.getPhoneNumber() });
			if (users == null || users.isEmpty()) {
				result.put("success", false);
				result.put("message", "User not found");
				return result;
			}

			Object userId = users.get(0).get("user_id");
			List<Map> payments = iGenericDao.executeDDLSQL(JavaConstant.GET_LATEST_PAYMENT_BY_USER_ID,
					new Object[] { String.valueOf(userId) });

			if (payments == null || payments.isEmpty()) {
				result.put("success", false);
				result.put("message", "Payment not found");
				return result;
			}

			Object paymentId = payments.get(0).get("payment_id");

			if (Boolean.FALSE.equals(request.getIsActive())) {
			    iGenericDao.executeDMLSQL(JavaConstant.UPDATE_PAYMENT_DEACTIVATE_AND_EXPIRE,
			            new Object[] { paymentId });
			} else {
			    iGenericDao.executeDMLSQL(JavaConstant.UPDATE_PAYMENT_ACTIVE_STATUS,
			            new Object[] { request.getIsActive(), paymentId });
			}

			List<Map> updated = iGenericDao.executeDDLSQL(JavaConstant.GET_PAYMENT_BY_ID, new Object[] { paymentId });

			result.put("success", true);
			result.put("message", "Payment status updated successfully");
			result.put("payment", updated.get(0));
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			result.put("success", false);
			result.put("message", e.getMessage());
			return result;
		}
	}

	// ============ GET /users-with-payments/:page (with filters) ============
	@Override
	public Map<String, Object> usersWithPayments(int page, String userId, String fullName, String phone, String status) {
		Map<String, Object> result = new LinkedHashMap<>();
		try {
			int limit = getPageLimit();
			int offset = (page - 1) * limit;

			StringBuilder where = new StringBuilder(" where 1=1 ");
			List<Object> params = new ArrayList<>();

			if (userId != null && !userId.isBlank()) {
				where.append(" and u.user_id = ?").append(params.size() + 1);
				params.add(Integer.parseInt(userId));
			}
			if (fullName != null && !fullName.isBlank()) {
				where.append(" and u.full_name ilike ?").append(params.size() + 1);
				params.add("%" + fullName + "%");
			}
			if (phone != null && !phone.isBlank()) {
				where.append(" and u.personal_number ilike ?").append(params.size() + 1);
				params.add("%" + phone + "%");
			}
			if ("active".equals(status)) {
			    where.append(" and p.is_active = true ");
			} else if ("inactive".equals(status)) {
			    where.append(" and p.is_active = false ");
			} else if ("unpaid".equals(status)) {
				where.append(" and p.payment_id is null ");
			}
			
			String baseFrom =
				"from app_user u left join lateral (select * from payment pp where pp.user_id = CAST(u.user_id AS text) " +
				"order by pp.created_at desc limit 1) p on true ";

			String countQuery = "select count(*) " + baseFrom + where;
			List<Map> countRows = iGenericDao.executeDDLSQL(countQuery, params.toArray());
			long total = ((Number) countRows.get(0).values().iterator().next()).longValue();

//			String dataQuery =
//				"select u.user_id as user_id, u.full_name as full_name, u.phone_number as phone_number, " +
//				"u.email as email, u.personal_number as personal_number, " +
//				"p.payment_id as payment_id, p.status as payment_status, p.is_active as payment_is_active, " +
//				"p.plan_hours as plan_hours, p.plan_type as plan_type, p.shift_label as shift_label, " +
//				"p.seats as seats, p.created_at as payment_created_at " +
//				baseFrom + where + " order by u.created_at desc limit ?" + (params.size() + 1) +
//				" offset ?" + (params.size() + 2);

			String dataQuery =
				    "select u.user_id as user_id, u.full_name as full_name, u.phone_number as phone_number, " +
				    "u.email as email, u.personal_number as personal_number, u.photo as photo, " +
				    "u.father_name as father_name, u.preparation_for as preparation_for, u.dob as dob," +
				    "u.blood_group as blood_group, u.emergency_number as emergency_number, u.present_address as present_address," +
				    "u.permanent_address as permanent_address, u.gender as gender, u.aadhar_number as aadhar_number," +
				    "p.payment_id as payment_id, p.status as payment_status, p.is_active as payment_is_active, " +
				    "p.amount as amount, p.plan_hours as plan_hours, p.plan_type as plan_type, " +
				    "p.shift_label as shift_label, p.shift_time as shift_time, " +
				    "p.seats as seats, p.created_at as payment_created_at " +
				    baseFrom + where + " order by u.created_at desc limit ?" + (params.size() + 1) +
				    " offset ?" + (params.size() + 2);
			
			List<Object> dataParams = new ArrayList<>(params);
			dataParams.add(limit);
			dataParams.add(offset);

			List<Map> data = iGenericDao.executeDDLSQL(dataQuery, dataParams.toArray());

			result.put("success", true);
			result.put("page", page);
			result.put("perPage", limit);
			result.put("total", total);
			result.put("totalPages", (int) Math.ceil((double) total / limit));
			result.put("data", data);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			result.put("success", false);
			result.put("message", "Pagination error");
			return result;
		}
	}
	
	// ============ GET /seat-details (with filters) ============
	@Override
	public Map<String, Object> seatDetails(String studentName, String phone, String seatNo, String month) {
		Map<String, Object> result = new LinkedHashMap<>();
		try {
			StringBuilder where = new StringBuilder(" where p.is_active = true and p.status = 'paid' ");
			List<Object> params = new ArrayList<>();

			if (studentName != null && !studentName.isBlank()) {
				where.append(" and u.full_name ilike ?").append(params.size() + 1);
				params.add("%" + studentName + "%");
			}
			if (phone != null && !phone.isBlank()) {
				where.append(" and u.phone_number ilike ?").append(params.size() + 1);
				params.add("%" + phone + "%");
			}
			if (seatNo != null && !seatNo.isBlank()) {
				// seats is stored as CSV text e.g. "12,13" - wrap both
				// sides in commas so "1" never matches inside "12"/"21".
				where.append(" and (',' || replace(p.seats, ' ', '') || ',') like ?")
					.append(params.size() + 1);
				params.add("%," + seatNo.trim() + ",%");
			}
			if (month != null && !month.isBlank()) {
				// Filter by the CALENDAR MONTH of end_plan_date (1-12),
				// regardless of year - matches "month ke expire date" filter.
				where.append(" and extract(month from p.end_plan_date) = ?")
					.append(params.size() + 1);
				params.add(Integer.parseInt(month));
			}

			String query =
				"select u.user_id as student_id, u.phone_number as phone, u.full_name as student_name, " +
				"p.seats as seat_no, p.plan_hours as plan_hours, " +
				"case lower(p.plan_type) " +
				"  when 'monthly' then 1 when 'quarterly' then 3 " +
				"  when 'half yearly' then 6 when 'annually' then 12 " +
				"  else null end as plan_months, " +
				"p.plan_type as plan_type, p.shift_time as timing_duration, " +
				"p.created_at as payment_date, p.end_plan_date as expire_date " +
				"from payment p join app_user u on u.user_id = CAST(p.user_id AS integer) " +
				where + " order by u.full_name asc";

			List<Map> data = iGenericDao.executeDDLSQL(query, params.toArray());

			result.put("success", true);
			result.put("total", data.size());
			result.put("data", data);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			result.put("success", false);
			result.put("message", e.getMessage());
			return result;
		}
	}

	// ============ POST /create-order ============
	@Override
	@Transactional
	public Map<String, Object> createOrder(PaymentDetails p) {
		Map<String, Object> result = new LinkedHashMap<>();
		try {
			if (p.getAmount() == null || p.getUserId() == null) {
				result.put("httpStatus", 400);
				result.put("message", "Missing required fields: amount and userId");
				return result;
			}

			RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

			Map<String, Object> orderRequest = new HashMap<>();
			orderRequest.put("amount", p.getAmount().intValue());
			orderRequest.put("currency", p.getCurrency() != null ? p.getCurrency() : "INR");
			orderRequest.put("receipt", "rcpt_" + System.currentTimeMillis());
			orderRequest.put("payment_capture", 1);

			Order order = razorpay.orders.create(new org.json.JSONObject(orderRequest));
			String orderId = order.get("id");

			iGenericDao.executeDMLSQL(JavaConstant.INSERT_ONLINE_ORDER, new Object[] {
					p.getUserId(), p.getAmount(), p.getCurrency() != null ? p.getCurrency() : "INR",
					p.getPlanHours(), p.getPlanType(), p.getPlanAmount(), p.getShiftLabel(), p.getShiftTime(),
					seatsToString(p.getSeats()), orderId, toJson(p.getMetadata()), p.getEndPlanDate()
			});

			List<Map> saved = iGenericDao.executeDDLSQL(JavaConstant.GET_PAYMENT_BY_ORDER_ID, new Object[] { orderId });

			result.put("httpStatus", 200);
			result.put("id", orderId);
			result.put("amount", order.get("amount"));
			result.put("currency", order.get("currency"));
			result.put("key", razorpayKeyId);
			result.put("userId", p.getUserId());
			result.put("paymentId", saved != null && !saved.isEmpty() ? saved.get(0).get("payment_id") : null);
			result.put("endPlanDate", p.getEndPlanDate());
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			result.put("httpStatus", 500);
			result.put("message", "Order creation failed: " + e.getMessage());
			return result;
		}
	}

	// ============ GET /users-with-payments-all ============
	@Override
	public Map<String, Object> usersWithPaymentsAll(String userId, String fullName, String phone, String status) {
		Map<String, Object> result = new LinkedHashMap<>();
		try {
			StringBuilder where = new StringBuilder(" where 1=1 ");
			List<Object> params = new ArrayList<>();

			if (userId != null && !userId.isBlank()) {
				where.append(" and u.user_id = ?").append(params.size() + 1);
				params.add(Integer.parseInt(userId));
			}
			if (fullName != null && !fullName.isBlank()) {
				where.append(" and u.full_name ilike ?").append(params.size() + 1);
				params.add("%" + fullName + "%");
			}
			if (phone != null && !phone.isBlank()) {
				where.append(" and u.phone_number ilike ?").append(params.size() + 1);
				params.add("%" + phone + "%");
			}
			if ("active".equals(status)) {
			    where.append(" and p.is_active = true ");
			} else if ("inactive".equals(status)) {
			    where.append(" and p.is_active = false ");
			} else if ("unpaid".equals(status)) {
				where.append(" and p.payment_id is null ");
			}

			String query =
				"select u.user_id as user_id, u.full_name as full_name, u.phone_number as phone_number, " +
				"u.email as email, p.payment_id as payment_id, p.status as payment_status, " +
				"p.is_active as payment_is_active, p.created_at as payment_created_at " +
				"from app_user u left join lateral (select * from payment pp where pp.user_id = CAST(u.user_id AS text) " +
				"order by pp.created_at desc limit 1) p on true " + where + " order by u.created_at desc";

			List<Map> data = iGenericDao.executeDDLSQL(query, params.toArray());

			result.put("success", true);
			result.put("total", data.size());
			result.put("data", data);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			result.put("success", false);
			result.put("message", "Fetch all error");
			return result;
		}
	}

	// ============ GET /users-status-count ============
	@Override
	public Map<String, Object> usersStatusCount() {
		Map<String, Object> result = new LinkedHashMap<>();
		try {
			String query =
				"select " +
				"sum(case when p.is_active = true then 1 else 0 end) as active_count, " +
				"sum(case when p.is_active = false then 1 else 0 end) as inactive_count, " +
				"count(*) as total " +
				"from app_user u left join lateral (select * from payment pp where pp.user_id = CAST(u.user_id AS text) " +
				"order by pp.created_at desc limit 1) p on true";

			List<Map> rows = iGenericDao.executeDDLSQL(query, new Object[] {});
			Map row = rows.get(0);

			result.put("success", true);
			result.put("activeCount", row.get("active_count") != null ? row.get("active_count") : 0);
			result.put("inactiveCount", row.get("inactive_count") != null ? row.get("inactive_count") : 0);
			result.put("total", row.get("total"));
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			result.put("success", false);
			result.put("message", e.getMessage());
			return result;
		}
	}

	// ============ POST /verify ============
	@Override
	@Transactional
	public Map<String, Object> verifyPayment(VerifyPaymentRequest request) {
		Map<String, Object> result = new LinkedHashMap<>();
		try {
			if (request.getRazorpayOrderId() == null || request.getRazorpayPaymentId() == null
					|| request.getRazorpaySignature() == null) {
				result.put("httpStatus", 400);
				result.put("success", false);
				result.put("message", "Missing payment verification fields");
				return result;
			}

			String generatedSignature = hmacSha256(
					request.getRazorpayOrderId() + "|" + request.getRazorpayPaymentId(), razorpayKeySecret);
			boolean isValid = generatedSignature.equals(request.getRazorpaySignature());

			List<Map> paymentRows;
			if (request.getPaymentId() != null) {
				paymentRows = iGenericDao.executeDDLSQL(JavaConstant.GET_PAYMENT_BY_ID, new Object[] { request.getPaymentId() });
			} else {
				paymentRows = iGenericDao.executeDDLSQL(JavaConstant.GET_PAYMENT_BY_ORDER_ID, new Object[] { request.getRazorpayOrderId() });
			}

			if (paymentRows != null && !paymentRows.isEmpty() && isValid) {
				Map payment = paymentRows.get(0);
				Object paymentId = payment.get("payment_id");
				Object userId = payment.get("user_id");
				List<Integer> seats = stringToSeats(payment.get("seats"));
				String shiftTime = (String) payment.get("shift_time");

				// Safety-net: re-validate seat/time overlap right before actually activating
				// the booking, so two students paying for the same seat/shift at nearly the
				// same time can never both end up "paid". Excludes this student's own other
				// bookings (renewal case) and this payment itself.
				Integer conflictingSeat = findConflictingSeat(seats, shiftTime, userId, paymentId);
				if (conflictingSeat != null) {
					iGenericDao.executeDMLSQL(JavaConstant.UPDATE_PAYMENT_VERIFY, new Object[] {
							request.getRazorpayPaymentId(), request.getRazorpaySignature(), "failed", paymentId
					});
					result.put("httpStatus", 409);
					result.put("success", false);
					result.put("message", "Seat " + conflictingSeat
							+ " was just booked for an overlapping shift by someone else. Payment could not be completed; please contact admin for a refund.");
					return result;
				}

				iGenericDao.executeDMLSQL(JavaConstant.UPDATE_PAYMENT_VERIFY, new Object[] {
						request.getRazorpayPaymentId(), request.getRazorpaySignature(), "paid", paymentId
				});
			} else if (paymentRows != null && !paymentRows.isEmpty()) {
				Object paymentId = paymentRows.get(0).get("payment_id");
				iGenericDao.executeDMLSQL(JavaConstant.UPDATE_PAYMENT_VERIFY, new Object[] {
						request.getRazorpayPaymentId(), request.getRazorpaySignature(), "failed", paymentId
				});
			}

			if (isValid) {
				result.put("httpStatus", 200);
				result.put("success", true);
				result.put("message", "Payment verified");
			} else {
				result.put("httpStatus", 400);
				result.put("success", false);
				result.put("message", "Invalid signature");
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			result.put("httpStatus", 500);
			result.put("success", false);
			result.put("message", "Verification error: " + e.getMessage());
			return result;
		}
	}

	private String hmacSha256(String data, String secret) throws Exception {
		Mac mac = Mac.getInstance("HmacSHA256");
		mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
		byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
		StringBuilder sb = new StringBuilder();
		for (byte b : hash) sb.append(String.format("%02x", b));
		return sb.toString();
	}

	// ============ GET /user/:userId (payment history) ============
	@Override
	public Map<String, Object> paymentsByUser(String userId) {
		Map<String, Object> result = new LinkedHashMap<>();
		List<Map> payments = iGenericDao.executeDDLSQL(JavaConstant.GET_PAYMENTS_BY_USER_ID, new Object[] { userId });
		
		// The student's original joining date - renewals anchor their
	 	// new expiry date's DAY-OF-MONTH to this.
	 	Object joiningDate = null;
	 	try {
	 		List<Map> userRows = iGenericDao.executeDDLSQL(JavaConstant.GET_USER_BY_USER_ID, new Object[] { userId });
	 		if (!userRows.isEmpty()) {
	 			joiningDate = userRows.get(0).get("created_at");
	 		}
	 	} catch (Exception ignored) { }

		if (payments == null || payments.isEmpty()) {
			result.put("success", true);
			result.put("count", 0);
			result.put("data", new ArrayList<>());
			result.put("joiningDate", joiningDate);
			return result;
		}

		result.put("success", true);
		result.put("count", payments.size());
		result.put("data", payments);
		result.put("joiningDate", joiningDate);
		return result;
	}

	// ============ GET /seat/check ============
	@Override
	public Map<String, Object> checkSeat(Integer seatNo, String shift, Integer excludeUserId) {
		Map<String, Object> result = new LinkedHashMap<>();
		if (seatNo == null || shift == null) {
			result.put("success", false);
			result.put("msg", "seatNo and shift required");
			return result;
		}

		ShiftTimeUtil.Range requested = ShiftTimeUtil.parseShift(shift);
		List<Map> bookings = iGenericDao.executeDDLSQL(JavaConstant.GET_PAID_BOOKINGS_FOR_SEAT,
				new Object[] { String.valueOf(seatNo) });

		for (Map b : bookings) {
			// when checking on behalf of a specific student (e.g. admin editing their
			// own seat/time), skip that student's own existing booking(s) so they
			// don't clash with themselves
			if (excludeUserId != null && String.valueOf(excludeUserId).equals(String.valueOf(b.get("user_id")))) {
				continue;
			}
			Boolean isActive = (Boolean) b.get("is_active");
			if (!Boolean.TRUE.equals(isActive)) continue;
			List<Integer> seats = stringToSeats(b.get("seats"));
			if (!seats.contains(seatNo)) continue;
			String bookedShiftTime = (String) b.get("shift_time");
			if (bookedShiftTime == null) continue;
			ShiftTimeUtil.Range bookedShift = ShiftTimeUtil.parseShift(bookedShiftTime);
			if (ShiftTimeUtil.isOverlap(requested, bookedShift)) {
				result.put("success", false);
				result.put("msg", "Seat " + seatNo + " already booked for " + bookedShiftTime);
				return result;
			}
		}

		result.put("success", true);
		result.put("available", true);
		return result;
	}

	// ============ GET /all/:page ============
	@Override
	public Map<String, Object> allPayments(int page) {
		Map<String, Object> result = new LinkedHashMap<>();
		try {
			int limit = getPageLimit();
			int offset = (page - 1) * limit;

			List<Map> totalRows = iGenericDao.executeDDLSQL(JavaConstant.COUNT_ALL_PAYMENTS, new Object[] {});
			long total = ((Number) totalRows.get(0).values().iterator().next()).longValue();

			String query = "select * from payment order by created_at desc limit ?1 offset ?2";
			List<Map> payments = iGenericDao.executeDDLSQL(query, new Object[] { limit, offset });

			Map<String, Object> pagination = new LinkedHashMap<>();
			pagination.put("totalRecords", total);
			pagination.put("currentPage", page);
			pagination.put("pageSize", limit);
			pagination.put("totalPages", (int) Math.ceil((double) total / limit));

			result.put("success", true);
			result.put("pagination", pagination);
			result.put("data", payments);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			result.put("success", false);
			result.put("message", "Failed to fetch payments");
			return result;
		}
	}

	// ============ PUT /add-seat-block-flag ============
	@Override
	@Transactional
	public Map<String, Object> addSeatBlockFlag() {
		Map<String, Object> result = new LinkedHashMap<>();
		iGenericDao.executeDMLSQL(JavaConstant.ADD_SEAT_BLOCK_FLAG_TO_OLD_RECORDS, new Object[] {});
		result.put("success", true);
		result.put("message", "Flag added to all old records");
		return result;
	}

	// ============ PUT /set-active ============
	@Override
	@Transactional
	public Map<String, Object> setAllActive() {
		Map<String, Object> result = new LinkedHashMap<>();
		iGenericDao.executeDMLSQL(JavaConstant.SET_ALL_PAYMENTS_ACTIVE, new Object[] {});
		result.put("success", true);
		result.put("message", "All payments set to active");
		return result;
	}

	// ============ GET /seats/status ============
	@Override
	public Map<String, Object> seatsStatus(String shift, String userId) {
		Map<String, Object> result = new LinkedHashMap<>();
		try {
			if (shift == null) {
				result.put("httpStatus", 400);
				result.put("message", "Shift required");
				return result;
			}
			
			Integer myUserId = null;
	 		if (userId != null && !userId.isBlank()) {
	 			try { myUserId = Integer.parseInt(userId); } catch (Exception ignored) { }
	 		}

			ShiftTimeUtil.Range requested = ShiftTimeUtil.parseShift(shift);
			List<Map> payments = iGenericDao.executeDDLSQL(JavaConstant.GET_ALL_PAID_PAYMENTS, new Object[] {});

			Set<Integer> bookedSeats = new HashSet<>();
			Set<Integer> myCurrentSeats = new HashSet<>();
			for (Map p : payments) {
				Boolean isActive = (Boolean) p.get("is_active");
				if (!Boolean.TRUE.equals(isActive)) continue;
				String shiftTime = (String) p.get("shift_time");
				List<Integer> seats = stringToSeats(p.get("seats"));
				if (shiftTime == null || seats.isEmpty()) continue;

				ShiftTimeUtil.Range existing = ShiftTimeUtil.parseShift(shiftTime);
//				if (ShiftTimeUtil.isOverlap(existing, requested)) {
//					bookedSeats.addAll(seats);
//				}
				
				if (!ShiftTimeUtil.isOverlap(existing, requested)) continue;

	 			Object uidObj = p.get("user_id");
	 			boolean isMine = myUserId != null && uidObj != null
	 					&& String.valueOf(uidObj).equals(String.valueOf(myUserId));

	 			if (isMine) {
	 				myCurrentSeats.addAll(seats);
	 			} else {
					bookedSeats.addAll(seats);
				}
			}

			List<Map<String, Object>> seats = new ArrayList<>();
			for (int i = 1; i <= 68; i++) {
				Map<String, Object> seat = new LinkedHashMap<>();
				seat.put("seatNo", i);
				seat.put("status", bookedSeats.contains(i) ? "booked" : "available");
				seats.add(seat);
			}

			result.put("httpStatus", 200);
			result.put("success", true);
			result.put("seats", seats);
			result.put("currentSeats", new ArrayList<>(myCurrentSeats));
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			result.put("httpStatus", 500);
			result.put("success", false);
			result.put("message", e.getMessage());
			return result;
		}
	}
	
	// ============ PUT /change-seat ============
	@Override
	@Transactional
	public Map<String, Object> changeSeat(ChangeSeatRequest request) {
		Map<String, Object> result = new LinkedHashMap<>();
		try {
			if (request.getUserId() == null || request.getNewSeats() == null || request.getNewSeats().isEmpty()) {
				result.put("httpStatus", 400);
				result.put("success", false);
				result.put("message", "userId and newSeats are required");
				return result;
			}

			List<Map> paymentRows = iGenericDao.executeDDLSQL(JavaConstant.GET_ACTIVE_PAID_PAYMENT_BY_USER_ID,
					new Object[] { String.valueOf(request.getUserId()) });

			if (paymentRows == null || paymentRows.isEmpty()) {
				result.put("httpStatus", 404);
				result.put("success", false);
				result.put("message", "No active paid plan found for this user");
				return result;
			}

			Map currentPayment = paymentRows.get(0);
			Object paymentId = currentPayment.get("payment_id");
			String existingShiftTime = (String) currentPayment.get("shift_time");
			String existingShiftLabel = (String) currentPayment.get("shift_label");

			// Admin may optionally send a new shiftLabel/shiftTime along with the seat change.
			// If not sent, we keep the student's current shift/time as-is.
			String targetShiftTime = (request.getShiftTime() != null && !request.getShiftTime().isBlank())
					? request.getShiftTime() : existingShiftTime;
			String targetShiftLabel = (request.getShiftLabel() != null && !request.getShiftLabel().isBlank())
					? request.getShiftLabel() : existingShiftLabel;

			if (targetShiftTime == null) {
				result.put("httpStatus", 400);
				result.put("success", false);
				result.put("message", "Existing plan has no shift time recorded, cannot check seat clash");
				return result;
			}

			// Validate the requested seat(s) against the *target* time (new time if the
			// admin is changing it, otherwise the student's existing time). Only currently
			// active, paid bookings (is_active = true) count as a clash, which implicitly
			// respects plan/month validity - an expired/inactive booking never blocks.
			Integer conflictingSeat = findConflictingSeat(request.getNewSeats(), targetShiftTime,
					request.getUserId(), paymentId);
			if (conflictingSeat != null) {
				result.put("httpStatus", 400);
				result.put("success", false);
				result.put("message", "Seat " + conflictingSeat + " is already booked for an overlapping shift ("
						+ targetShiftTime + ")");
				return result;
			}

			iGenericDao.executeDMLSQL(JavaConstant.UPDATE_PAYMENT_SEATS_AND_SHIFT,
					new Object[] { seatsToString(request.getNewSeats()), targetShiftLabel, targetShiftTime, paymentId });

			List<Map> updated = iGenericDao.executeDDLSQL(JavaConstant.GET_PAYMENT_BY_ID, new Object[] { paymentId });

			result.put("httpStatus", 200);
			result.put("success", true);
			result.put("message", "Seat changed successfully");
			result.put("payment", updated != null && !updated.isEmpty() ? updated.get(0) : null);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			result.put("httpStatus", 500);
			result.put("success", false);
			result.put("message", "Seat change failed: " + e.getMessage());
			return result;
		}
	}
	
	// ============ GET /fee-records (with filters) ============
	// Full fee/payment history - every successfully PAID transaction
	// (online + cash), not just the latest/active one per user. Each
	// payment row is its own record here, unlike seat-details or
	// users-with-payments which only look at the latest payment per user.
	@Override
	public Map<String, Object> feeRecords(String studentName, String phone, String paymentMode, String month,
			String expireMonth, String expireYear, int page) {
		Map<String, Object> result = new LinkedHashMap<>();
		try {
			StringBuilder where = new StringBuilder(" where p.status = 'paid' ");
			List<Object> params = new ArrayList<>();

			if (studentName != null && !studentName.isBlank()) {
				where.append(" and u.full_name ilike ?").append(params.size() + 1);
				params.add("%" + studentName + "%");
			}
			if (phone != null && !phone.isBlank()) {
				where.append(" and u.phone_number ilike ?").append(params.size() + 1);
				params.add("%" + phone + "%");
			}
			if (paymentMode != null && !paymentMode.isBlank()) {
				where.append(" and p.payment_mode = ?").append(params.size() + 1);
				params.add(paymentMode);
			}
			if (month != null && !month.isBlank()) {
				// Filter by the CALENDAR MONTH (1-12) the payment was
				// made, regardless of year.
				where.append(" and extract(month from p.created_at) = ?")
					.append(params.size() + 1);
				params.add(Integer.parseInt(month));
			}
			if (expireMonth != null && !expireMonth.isBlank()) {
				// Filter by the CALENDAR MONTH (1-12) the plan expires.
				where.append(" and extract(month from p.end_plan_date) = ?")
					.append(params.size() + 1);
				params.add(Integer.parseInt(expireMonth));
			}
			if (expireYear != null && !expireYear.isBlank()) {
				where.append(" and extract(year from p.end_plan_date) = ?")
					.append(params.size() + 1);
				params.add(Integer.parseInt(expireYear));
			}

			// ---- total count (for pagination) using the SAME filters ----
			String countQuery =
				"select count(*) from payment p join app_user u on u.user_id = CAST(p.user_id AS integer) " + where;
			List<Map> countRows = iGenericDao.executeDDLSQL(countQuery, params.toArray());
			long total = ((Number) countRows.get(0).values().iterator().next()).longValue();

			int limit = getPageLimit();
			int safePage = Math.max(page, 1);
			int offset = (safePage - 1) * limit;

			List<Object> dataParams = new ArrayList<>(params);
			dataParams.add(limit);
			dataParams.add(offset);

			String query =
				"select u.user_id as student_id, u.full_name as student_name, u.phone_number as phone, " +
				"p.amount as amount, p.payment_mode as payment_mode, p.plan_type as plan_type, " +
				"p.status as status, p.created_at as payment_date, p.end_plan_date as expire_date " +
				"from payment p join app_user u on u.user_id = CAST(p.user_id AS integer) " +
				where + " order by p.created_at desc limit ?" + (dataParams.size() - 1) +
				" offset ?" + dataParams.size();

			List<Map> data = iGenericDao.executeDDLSQL(query, dataParams.toArray());

			result.put("success", true);
			result.put("page", safePage);
			result.put("perPage", limit);
			result.put("total", total);
			result.put("totalPages", (int) Math.ceil((double) total / limit));
			result.put("data", data);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			result.put("success", false);
			result.put("message", e.getMessage());
			return result;
		}
	}
	
	// ============ GET /seats/overview (Admin dashboard) ============
	 	// For every seat 1-68: list of active bookings (student  time slot).
	 	// Empty list = seat is fully free all day.
	 	@Override
	 	public Map<String, Object> seatsOverview() {
	 		Map<String, Object> result = new LinkedHashMap<>();
	 		try {
	 			List<Map> bookings = iGenericDao.executeDDLSQL(
	 					JavaConstant.GET_ACTIVE_BOOKINGS_WITH_STUDENT, new Object[] {});
	 
	 			// seatNo -> list of {studentName, phone, shiftTime, endPlanDate}
	 			Map<Integer, List<Map<String, Object>>> seatBookings = new LinkedHashMap<>();
	 			for (int i = 1; i <= 68; i++) {
	 				seatBookings.put(i, new ArrayList<>());
	 			}
	 
	 			for (Map b : bookings) {
	 				List<Integer> seatNos = stringToSeats(b.get("seats"));
	 				for (Integer seatNo : seatNos) {
	 					if (seatNo < 1 || seatNo > 68) continue;
	 					Map<String, Object> entry = new LinkedHashMap<>();
	 					entry.put("studentName", b.get("student_name"));
	 					entry.put("phone", b.get("phone"));
	 					entry.put("shiftTime", b.get("shift_time"));
	 					entry.put("endPlanDate", b.get("end_plan_date"));
	 					seatBookings.get(seatNo).add(entry);
	 				}
	 			}
	 
	 			List<Map<String, Object>> seats = new ArrayList<>();
	 			int occupiedCount = 0;
	 			for (int i = 1; i <= 68; i++) {
	 				List<Map<String, Object>> bookingsForSeat = seatBookings.get(i);
	 				Map<String, Object> seat = new LinkedHashMap<>();
	 				seat.put("seatNo", i);
	 				seat.put("status", bookingsForSeat.isEmpty() ? "available" : "booked");
	 				seat.put("bookings", bookingsForSeat);
	 				seats.add(seat);
	 				if (!bookingsForSeat.isEmpty()) occupiedCount++;
	 			}
	 
	 			Map<String, Object> summary = new LinkedHashMap<>();
	 			summary.put("totalSeats", 68);
	 			summary.put("bookedSeatsCount", occupiedCount);
	 			summary.put("availableSeatsCount", 68 - occupiedCount);
	 			summary.put("totalActiveBookings", bookings.size());
	 
	 			result.put("httpStatus", 200);
	 			result.put("success", true);
	 			result.put("summary", summary);
	 			result.put("seats", seats);
	 			return result;
	 		} catch (Exception e) {
	 			e.printStackTrace();
	 			result.put("httpStatus", 500);
	 			result.put("success", false);
	 			result.put("message", e.getMessage());
	 			return result;
	 		}
	 	}
	 

}
