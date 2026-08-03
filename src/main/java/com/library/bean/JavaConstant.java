package com.library.bean;

public class JavaConstant {

	// ===================== ADMIN =====================
	public final static String CHECK_ADMIN_BY_PHONE = "select * from admin_user where phone = ?1";
	public final static String INSERT_ADMIN =
			"insert into admin_user (name, phone, password, image) values (?1, ?2, ?3, ?4)";

	// ===================== APP USER =====================
	public final static String GET_USER_BY_EMAIL = "select * from app_user where email = ?1";
	public final static String GET_USER_BY_PHONE = "select * from app_user where phone_number = ?1";
	public final static String INSERT_USER_FULL =
			"insert into app_user (full_name, father_name, preparation_for, dob, blood_group, email, " +
			"personal_number, emergency_number, present_address, permanent_address, password) " +
			"values (?1, ?2, ?3, CAST(?4 AS date), ?5, ?6, ?7, ?8, ?9, ?10, ?11)";
	public final static String INSERT_USER_BASIC =
			"insert into app_user (full_name, phone_number) values (?1, ?2)";
	public final static String UPDATE_USER_PASSWORD_BY_PHONE =
			"update app_user set password = ?1, updated_at = CURRENT_TIMESTAMP where phone_number = ?2";
	public final static String GET_USER_BY_USER_ID = "select * from app_user where user_id = ?1";

	// ===================== PAYMENT =====================
	public final static String GET_PAYMENT_PAID_BY_USER_ID =
			"select * from payment where user_id = ?1 and status = 'paid' order by created_at desc limit 1";
	public final static String COUNT_PENDING_CASH =
			"select count(*) from payment where payment_mode = 'cash' and status = 'pending'";
	public final static String GET_LATEST_PENDING_CASH_BY_USER =
			"select * from payment where user_id = ?1 and payment_mode = 'cash' and status = 'pending' " +
			"order by created_at desc limit 1";
	public final static String GET_ALL_PENDING_CASH =
			"select * from payment where payment_mode = 'cash' and status = 'pending' order by created_at desc";
	public final static String INSERT_CASH_PAYMENT =
			"insert into payment (user_id, amount, plan_hours, plan_type, plan_amount, shift_label, shift_time, " +
			"seats, metadata, payment_mode, status, is_approved_by_admin, end_plan_date) " +
			"values (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, CAST(?9 AS jsonb), 'cash', 'pending', false, CAST(?10 AS date))";
	public final static String INSERT_ONLINE_ORDER =
			"insert into payment (user_id, amount, currency, plan_hours, plan_type, plan_amount, shift_label, " +
			"shift_time, seats, razorpay_order_id, status, metadata, end_plan_date) " +
			"values (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10, 'created', CAST(?11 AS jsonb), CAST(?12 AS date))";
	public final static String UPDATE_PAYMENT_APPROVE_CASH =
			"update payment set status = 'paid', is_approved_by_admin = true, updated_at = CURRENT_TIMESTAMP " +
			"where payment_id = ?1";
	public final static String GET_ACTIVE_INACTIVE_COUNT =
			"select is_active, count(*) as cnt from payment group by is_active";
	public final static String GET_LATEST_PAYMENT_BY_USER_ID =
			"select * from payment where user_id = ?1 order by created_at desc limit 1";
	public final static String UPDATE_PAYMENT_ACTIVE_STATUS =
			"update payment set is_active = ?1, updated_at = CURRENT_TIMESTAMP where payment_id = ?2";
	public final static String UPDATE_PAYMENT_DEACTIVATE_AND_EXPIRE =
		    "update payment set is_active = false, seats = null, plan_expire_seat_block = true, " +
		    "end_plan_date = CURRENT_DATE, updated_at = CURRENT_TIMESTAMP where payment_id = ?1";
	public final static String GET_PAYMENT_BY_ORDER_ID =
			"select * from payment where razorpay_order_id = ?1";
	public final static String GET_PAYMENT_BY_ID =
			"select * from payment where payment_id = ?1";
	public final static String UPDATE_PAYMENT_VERIFY =
			"update payment set razorpay_payment_id = ?1, razorpay_signature = ?2, status = ?3, " +
			"updated_at = CURRENT_TIMESTAMP where payment_id = ?4";
	public final static String GET_PAYMENTS_BY_USER_ID =
			"select * from payment where user_id = ?1 order by created_at desc";
	public final static String GET_PAID_BOOKINGS_FOR_SEAT =
			"select * from payment where status = 'paid' and seats like '%' || ?1 || '%'";
	public final static String GET_ALL_PAID_PAYMENTS = "select * from payment where status = 'paid'";
	public final static String GET_ACTIVE_PAID_PAYMENT_BY_USER_ID =
	        "select * from payment where user_id = ?1 and status = 'paid' and is_active = true " +
	        "order by created_at desc limit 1";
	public final static String GET_ACTIVE_PAID_PAYMENTS_EXCLUDING =
	        "select * from payment where status = 'paid' and is_active = true and payment_id <> ?1";
	public final static String UPDATE_PAYMENT_SEATS =
	        "update payment set seats = ?1, updated_at = CURRENT_TIMESTAMP where payment_id = ?2";
	public final static String UPDATE_PAYMENT_SEATS_AND_SHIFT =
	        "update payment set seats = ?1, shift_label = ?2, shift_time = ?3, updated_at = CURRENT_TIMESTAMP " +
	        "where payment_id = ?4";
	// Same as GET_ACTIVE_PAID_PAYMENTS_EXCLUDING but also excludes every booking belonging to a given user,
	// used to check seat/time overlap while allowing a user's own renewal/edit to not clash with themselves.
	public final static String GET_ACTIVE_PAID_PAYMENTS_EXCLUDING_USER =
	        "select * from payment where status = 'paid' and is_active = true and user_id <> ?1";
	public final static String COUNT_ALL_PAYMENTS = "select count(*) from payment";
	public final static String ADD_SEAT_BLOCK_FLAG_TO_OLD_RECORDS =
			"update payment set plan_expire_seat_block = false where plan_expire_seat_block is null";
	public final static String SET_ALL_PAYMENTS_ACTIVE = "update payment set is_active = true";
	public final static String COUNT_ALL_USERS = "select count(*) from app_user";
	
	//	public final static String EXPIRE_PLANS_JOB =
	//			"update payment set is_active = false, plan_expire_seat_block = true " +
	//			"where status = 'paid' and is_active = true and " +
	//			"(created_at + (case lower(plan_type) " +
	//			"  when 'quarterly' then interval '90 day' " +
	//			"  when 'half yearly' then interval '180 day' " +
	//			"  when 'annual' then interval '365 day' " +
	//			"  else interval '30 day' end)) < now()";
	
	public final static String EXPIRE_PLANS_JOB =
		    "update payment set is_active = false, plan_expire_seat_block = true " +
		    "where status = 'paid' and is_active = true and " +
		    "(created_at + (case lower(plan_type) " +
		    "  when 'quarterly' then interval '90 day' " +
		    "  when 'half yearly' then interval '180 day' " +
		    "  when 'annually' then interval '365 day' " +        // ✅ matches frontend's 'Annually'
		    "  else interval '30 day' end)) < now()";

	// ===================== SEAT SELECTION =====================
	public final static String CHECK_SEAT_TAKEN_FOR_PLAN =
			"select * from seat_selection where plan_id = ?1 and seat_no = ?2";
	public final static String INSERT_SEAT_SELECTION =
			"insert into seat_selection (user_id, plan_id, seat_no, status) values (?1, ?2, ?3, 'booked')";
	public final static String GET_SEAT_SELECTIONS_BY_USER =
			"select * from seat_selection where user_id = ?1";
	public final static String GET_BOOKED_SEATS_BY_PLAN =
			"select * from seat_selection where plan_id = ?1 and status = 'booked'";

	// ===================== SHIFT SELECTION =====================
	public final static String INSERT_SHIFT_SELECTION =
			"insert into shift_selection (user_id, plan_id, shift_label, shift_time) values (?1, ?2, ?3, ?4)";
	public final static String GET_SHIFT_SELECTIONS_BY_USER =
			"select * from shift_selection where user_id = ?1";

	// ===================== USER SELECTION (plan pick) =====================
	public final static String INSERT_USER_SELECTION =
			"insert into user_selection (user_id, full_name, email, plan_hours, selected_option, price) " +
			"values (?1, ?2, ?3, ?4, ?5, ?6)";
	public final static String GET_USER_SELECTIONS_BY_USER =
			"select * from user_selection where user_id = ?1 order by created_at desc";

	// ===================== PLAN CATALOG =====================
	public final static String GET_ALL_PLAN_CATALOG = "select * from plan_catalog order by hours";
	public final static String COUNT_PLAN_CATALOG = "select count(*) from plan_catalog";
	public final static String INSERT_PLAN_CATALOG = "insert into plan_catalog (hours) values (?1)";
	public final static String INSERT_PLAN_OPTION =
			"insert into plan_option (plan_id, name, price) values (?1, ?2, ?3)";
	public final static String GET_OPTIONS_BY_PLAN_ID = "select * from plan_option where plan_id = ?1";

	// ===================== COMPLAINT =====================
	public final static String GET_ALL_COMPLAINTS = "select * from complaint order by created_at desc";
	public final static String GET_COMPLAINTS_BY_USER_ID =
			"select * from complaint where user_id = ?1 order by created_at desc";
	public final static String INSERT_COMPLAINT =
			"insert into complaint (user_id, message, issue_type) values (?1, ?2, ?3)";
	public final static String DELETE_COMPLAINT_BY_ID = "delete from complaint where complaint_id = ?1";
	public final static String GET_COMPLAINT_BY_ID = "select * from complaint where complaint_id = ?1";

	// ===================== PUBLIC QUERY (contact form) =====================
	public final static String GET_ALL_PUBLIC_QUERY = "select * from public_query order by created_at desc";
	public final static String INSERT_PUBLIC_QUERY =
			"insert into public_query (name, mail, subject, message) values (?1, ?2, ?3, ?4)";
	public final static String DELETE_PUBLIC_QUERY = "delete from public_query where query_id = ?1";
	public final static String GET_PUBLIC_QUERY_BY_ID = "select * from public_query where query_id = ?1";
}
