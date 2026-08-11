package com.library.servicesImpl;

import com.library.bean.*;
import com.library.dao.IGenericDao;
import com.library.services.AuthService;
import com.library.services.EmailService;
import com.library.util.FileStorageUtil;
import com.library.util.JwtUtil;
import com.library.util.PasswordUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
@SuppressWarnings({ "rawtypes", "unchecked" })
public class AuthServiceImpl implements AuthService {

	@Autowired
	IGenericDao iGenericDao;

	@Autowired
	PasswordUtil passwordUtil;

	@Autowired
	JwtUtil jwtUtil;

	@Autowired
	FileStorageUtil fileStorageUtil;
	
	@Autowired
	EmailService emailService;

	private String normalize(String v) {
		if (v == null || v.trim().isEmpty()) return null;
		return v.trim();
	}

	// ============ ADMIN REGISTER (one-time setup) ============
	@Override
	@Transactional
	public ApiResponse adminRegister(AdminDetails adminDetails, MultipartFile photo) {
		try {
			String phone = normalize(adminDetails.getPhone());
			String password = normalize(adminDetails.getPassword());

			if (phone == null || password == null) {
				return new ApiResponse(false, "Phone and password required");
			}

			List<Map> existing = iGenericDao.executeDDLSQL(JavaConstant.CHECK_ADMIN_BY_PHONE, new Object[] { phone });
			if (existing != null && !existing.isEmpty()) {
				return new ApiResponse(false, "Admin already exists");
			}

			String name = normalize(adminDetails.getName());
			String imagePath = fileStorageUtil.store(photo);
			String hashedPassword = passwordUtil.hash(password);

			iGenericDao.executeDMLSQL(JavaConstant.INSERT_ADMIN,
					new Object[] { name != null ? name : "Admin", phone, hashedPassword, imagePath });

			return new ApiResponse(true, "Admin created successfully");
		} catch (Exception e) {
			e.printStackTrace();
			return new ApiResponse(false, "Server error: " + e.getMessage());
		}
	}

	// ============ ADMIN LOGIN ============
	@Override
	public Map<String, Object> adminLogin(String phone, String password) {
		Map<String, Object> result = new LinkedHashMap<>();
		try {
			if (phone == null || password == null) {
				result.put("httpStatus", 400);
				result.put("message", "Phone and password are required");
				return result;
			}

			List<Map> admins = iGenericDao.executeDDLSQL(JavaConstant.CHECK_ADMIN_BY_PHONE, new Object[] { phone });
			if (admins == null || admins.isEmpty()) {
				result.put("httpStatus", 404);
				result.put("message", "Admin not found");
				return result;
			}

			Map admin = admins.get(0);
			String storedPassword = (String) admin.get("password");
			if (!passwordUtil.matches(password, storedPassword)) {
				result.put("httpStatus", 401);
				result.put("message", "Invalid password");
				return result;
			}

			String adminId = String.valueOf(admin.get("admin_id"));
			String token = jwtUtil.generateToken(adminId);

			Map<String, Object> data = new LinkedHashMap<>();
			data.put("id", admin.get("admin_id"));
			data.put("name", admin.get("name"));
			data.put("phone", admin.get("phone"));
			data.put("image", admin.get("image"));

			result.put("httpStatus", 200);
			result.put("message", "Login successful");
			result.put("token", token);
			result.put("data", data);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			result.put("httpStatus", 500);
			result.put("message", "Server error: " + e.getMessage());
			return result;
		}
	}

	// ============ ADMIN EDIT PROFILE ============
	@Override
	@Transactional
	public ApiResponse adminEditProfile(String phone, String name, String newPhone, String password, MultipartFile photo) {
		try {
			phone = normalize(phone);
			if (phone == null) {
				return new ApiResponse(false, "Phone number is required");
			}

			List<Map> existing = iGenericDao.executeDDLSQL(JavaConstant.CHECK_ADMIN_BY_PHONE, new Object[] { phone });
			if (existing == null || existing.isEmpty()) {
				return new ApiResponse(false, "Admin not found");
			}

			List<String> setClauses = new ArrayList<>();
			List<Object> values = new ArrayList<>();

			if (normalize(name) != null) {
				setClauses.add("name = ?" + (values.size() + 1));
				values.add(normalize(name));
			}
			if (normalize(newPhone) != null) {
				setClauses.add("phone = ?" + (values.size() + 1));
				values.add(normalize(newPhone));
			}
			if (normalize(password) != null) {
				setClauses.add("password = ?" + (values.size() + 1));
				values.add(passwordUtil.hash(normalize(password)));
			}
			String imagePath = fileStorageUtil.store(photo);
			if (imagePath != null) {
				setClauses.add("image = ?" + (values.size() + 1));
				values.add(imagePath);
			}
			setClauses.add("updated_at = CURRENT_TIMESTAMP");

			values.add(phone);
			int whereIndex = values.size();

			String query = "update admin_user set " + String.join(", ", setClauses) + " where phone = ?" + whereIndex;
			iGenericDao.executeDMLSQL(query, values.toArray());

			return new ApiResponse(true, "Profile updated successfully");
		} catch (Exception e) {
			e.printStackTrace();
			return new ApiResponse(false, "Server error: " + e.getMessage());
		}
	}

	// ============ USER REGISTER (full) ============
	@Override
	@Transactional
	public ApiResponse register(UserDetails u) {
		try {
			if (normalize(u.getEmail()) != null) {
				List<Map> existing = iGenericDao.executeDDLSQL(JavaConstant.GET_USER_BY_EMAIL, new Object[] { u.getEmail() });
				if (existing != null && !existing.isEmpty()) {
					return new ApiResponse(false, "User already exists");
				}
			}

			String hashedPassword = passwordUtil.hash(normalize(u.getPassword()));

			iGenericDao.executeDMLSQL(JavaConstant.INSERT_USER_FULL, new Object[] {
					normalize(u.getFullName()), normalize(u.getFatherName()), normalize(u.getPreparationFor()),
					normalize(u.getDob()), normalize(u.getBloodGroup()), normalize(u.getEmail()),
					normalize(u.getPersonalNumber()), normalize(u.getEmergencyNumber()),
					normalize(u.getPresentAddress()), normalize(u.getPermanentAddress()), hashedPassword
			});

			return new ApiResponse(true, "User registered successfully");
		} catch (Exception e) {
			e.printStackTrace();
			return new ApiResponse(false, "Server error: " + e.getMessage());
		}
	}
	
	// ============ FORGOT PASSWORD ============

	@Override
	@Transactional
	public ApiResponse forgotPassword(ForgotPasswordRequest request) {

	    try {

	        String email = normalize(request.getEmail());

	        if (email == null) {
	            return new ApiResponse(false, "Email is required");
	        }

	        email = email.toLowerCase();

	        List<Map> users = iGenericDao.executeDDLSQL(
	                JavaConstant.GET_USER_BY_EMAIL,
	                new Object[]{email}
	        );

	        /*
	         * Security:
	         */
	        if (users == null || users.isEmpty()) {

	            return new ApiResponse(
	                    true,
	                    "If this email is registered, an OTP has been sent"
	            );
	        }

	        Map user = users.get(0);

	        Integer userId = ((Number) user.get("user_id")).intValue();

	        String userName = user.get("full_name") != null
	                ? String.valueOf(user.get("full_name"))
	                : "User";

	        // Remove all previous OTPs
	        iGenericDao.executeDMLSQL(
	                JavaConstant.DELETE_OLD_PASSWORD_OTPS,
	                new Object[]{email}
	        );

	        // Generate new OTP
	        String otp = generateOtp();

	        // Hash OTP before storing
	        String otpHash = passwordUtil.hash(otp);

	        // OTP valid for 5 minutes
	        java.sql.Timestamp expiresAt =
	                new java.sql.Timestamp(
	                        System.currentTimeMillis() + (5 * 60 * 1000)
	                );

	        iGenericDao.executeDMLSQL(
	                JavaConstant.INSERT_PASSWORD_RESET_OTP,
	                new Object[]{
	                        userId,
	                        email,
	                        otpHash,
	                        expiresAt
	                }
	        );

	        // Send OTP email
	        emailService.sendOtpEmail(
	                email,
	                userName,
	                otp
	        );

	        return new ApiResponse(
	                true,
	                "If this email is registered, an OTP has been sent"
	        );

	    } catch (Exception e) {

	        e.printStackTrace();

	        return new ApiResponse(
	                false,
	                "Unable to process password reset request"
	        );
	    }
	}

	@Override
	@Transactional
	public ApiResponse verifyOtp(VerifyOtpRequest request) {

	    try {

	        String email = normalize(request.getEmail());
	        String otp = normalize(request.getOtp());

	        if (email == null || otp == null) {
	            return new ApiResponse(
	                    false,
	                    "Email and OTP are required"
	            );
	        }

	        email = email.toLowerCase();

	        if (!otp.matches("\\d{6}")) {
	            return new ApiResponse(
	                    false,
	                    "Invalid OTP format"
	            );
	        }

	        List<Map> otpRecords =
	                iGenericDao.executeDDLSQL(
	                        JavaConstant.GET_LATEST_PASSWORD_RESET_OTP,
	                        new Object[]{email}
	                );

	        if (otpRecords == null || otpRecords.isEmpty()) {

	            return new ApiResponse(
	                    false,
	                    "OTP not found or expired"
	            );
	        }

	        Map otpRecord = otpRecords.get(0);

	        Integer otpId =
	                ((Number) otpRecord.get("id")).intValue();

	        int attempts =
	                otpRecord.get("attempts") == null
	                        ? 0
	                        : ((Number) otpRecord.get("attempts")).intValue();

	        Boolean used =
	                otpRecord.get("used") != null
	                        && (Boolean) otpRecord.get("used");

	        Boolean verified =
	                otpRecord.get("verified") != null
	                        && (Boolean) otpRecord.get("verified");

	        if (used) {

	            return new ApiResponse(
	                    false,
	                    "OTP has already been used"
	            );
	        }

	        if (verified) {

	            return new ApiResponse(
	                    true,
	                    "OTP already verified"
	            );
	        }

	        // Maximum 5 wrong attempts
	        if (attempts >= 5) {

	            return new ApiResponse(
	                    false,
	                    "Too many incorrect attempts. Please request a new OTP"
	            );
	        }

	        String storedOtpHash =
	                String.valueOf(otpRecord.get("otp_hash"));

	        boolean matches =
	                passwordUtil.matches(
	                        otp,
	                        storedOtpHash
	                );

	        if (!matches) {

	            iGenericDao.executeDMLSQL(
	                    JavaConstant.UPDATE_OTP_ATTEMPTS,
	                    new Object[]{otpId}
	            );

	            return new ApiResponse(
	                    false,
	                    "Invalid OTP"
	            );
	        }

	        iGenericDao.executeDMLSQL(
	                JavaConstant.UPDATE_OTP_VERIFIED,
	                new Object[]{otpId}
	        );

	        return new ApiResponse(
	                true,
	                "OTP verified successfully"
	        );

	    } catch (Exception e) {

	        e.printStackTrace();

	        return new ApiResponse(
	                false,
	                "Unable to verify OTP"
	        );
	    }
	}
	
	// ============ RESET PASSWORD ============

	@Override
	@Transactional
	public ApiResponse resetPassword(ResetPasswordRequest request) {

	    try {

	        String email = normalize(request.getEmail());
	        String newPassword = normalize(request.getNewPassword());

	        if (email == null || newPassword == null) {

	            return new ApiResponse(
	                    false,
	                    "Email and new password are required"
	            );
	        }

	        email = email.toLowerCase();

	        // Basic password validation
	        if (newPassword.length() < 6) {

	            return new ApiResponse(
	                    false,
	                    "Password must be at least 6 characters"
	            );
	        }

	        /*
	         * Only OTP verified within last 5 minutes
	         * can reset password.
	         */
	        List<Map> otpRecords =
	                iGenericDao.executeDDLSQL(
	                        JavaConstant.GET_VERIFIED_OTP,
	                        new Object[]{email}
	                );

	        if (otpRecords == null || otpRecords.isEmpty()) {

	            return new ApiResponse(
	                    false,
	                    "OTP verification required or OTP expired"
	            );
	        }

	        Map otpRecord = otpRecords.get(0);

	        Integer otpId =
	                ((Number) otpRecord.get("id")).intValue();

	        // Find user
	        List<Map> users =
	                iGenericDao.executeDDLSQL(
	                        JavaConstant.GET_USER_BY_EMAIL,
	                        new Object[]{email}
	                );

	        if (users == null || users.isEmpty()) {

	            return new ApiResponse(
	                    false,
	                    "Unable to reset password"
	            );
	        }

	        // Hash new password
	        String hashedPassword =
	                passwordUtil.hash(newPassword);

	        Integer userId =
	                ((Number) users.get(0).get("user_id")).intValue();

	        /*
	         * Password update using user ID
	         * instead of trusting phone number.
	         */
	        String updatePasswordSql =
	                "update app_user " +
	                "set password = ?1, updated_at = CURRENT_TIMESTAMP " +
	                "where user_id = ?2";

	        iGenericDao.executeDMLSQL(
	                updatePasswordSql,
	                new Object[]{
	                        hashedPassword,
	                        userId
	                }
	        );

	        // OTP can never be used again
	        iGenericDao.executeDMLSQL(
	                JavaConstant.MARK_OTP_USED,
	                new Object[]{otpId}
	        );

	        return new ApiResponse(
	                true,
	                "Password reset successfully"
	        );

	    } catch (Exception e) {

	        e.printStackTrace();

	        return new ApiResponse(
	                false,
	                "Unable to reset password"
	        );
	    }
	}

	// ============ UNPAID USER ============
	@Override
	public Map<String, Object> unpaidUser(String phoneNumber) {
		Map<String, Object> result = new LinkedHashMap<>();
		try {
			List<Map> users = iGenericDao.executeDDLSQL(JavaConstant.GET_USER_BY_PHONE, new Object[] { phoneNumber });
			if (users == null || users.isEmpty()) {
				result.put("httpStatus", 404);
				result.put("msg", "User not found");
				return result;
			}
			Map user = users.get(0);
			Object userId = user.get("user_id");

			List<Map> paidPayment = iGenericDao.executeDDLSQL(JavaConstant.GET_PAYMENT_PAID_BY_USER_ID,
					new Object[] { String.valueOf(userId) });

			if (paidPayment != null && !paidPayment.isEmpty()) {
				result.put("httpStatus", 400);
				result.put("msg", "Payment already completed");
				result.put("paymentStatus", "paid");
				return result;
			}

			user.remove("password");
			result.put("httpStatus", 200);
			result.put("msg", "Unpaid user found");
			result.put("paymentStatus", "not-paid");
			result.put("user", user);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			result.put("httpStatus", 500);
			result.put("msg", "Server error: " + e.getMessage());
			return result;
		}
	}

	// ============ LOGIN ============
	@Override
	public Map<String, Object> login(String phoneNumber, String password) {
		Map<String, Object> result = new LinkedHashMap<>();
		try {
			List<Map> users = iGenericDao.executeDDLSQL(JavaConstant.GET_USER_BY_PHONE, new Object[] { phoneNumber });
			if (users == null || users.isEmpty()) {
				result.put("httpStatus", 400);
				result.put("msg", "Invalid credentials");
				return result;
			}

			Map user = users.get(0);
			if (!passwordUtil.matches(password, (String) user.get("password"))) {
				result.put("httpStatus", 400);
				result.put("msg", "Invalid credentials");
				return result;
			}

			Object userId = user.get("user_id");
			List<Map> payments = iGenericDao.executeDDLSQL(JavaConstant.GET_PAYMENT_PAID_BY_USER_ID,
					new Object[] { String.valueOf(userId) });

			Map userCopy = new LinkedHashMap<>(user);
			userCopy.remove("password");

			if (payments == null || payments.isEmpty()) {
				result.put("httpStatus", 403);
				result.put("msg", "Payment not completed. Please pay or wait for approval.");
				result.put("paymentStatus", "not-paid");
				result.put("user", userCopy);
				return result;
			}

			result.put("httpStatus", 200);
			result.put("msg", "Login successful");
			result.put("user", userCopy);
			result.put("paymentStatus", payments.get(0).get("status"));
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			result.put("httpStatus", 500);
			result.put("msg", "Server error: " + e.getMessage());
			return result;
		}
	}

	// ============ REGISTER BASIC ============
	@Override
	@Transactional
	public ApiResponse registerBasic(UserDetails u) {
		try {
			String fullName = normalize(u.getFullName());
			String phoneNumber = normalize(u.getPhoneNumber());

			if (fullName == null || phoneNumber == null) {
				return new ApiResponse(false, "Name and Phone are required");
			}

			List<Map> existing = iGenericDao.executeDDLSQL(JavaConstant.GET_USER_BY_PHONE, new Object[] { phoneNumber });
			if (existing != null && !existing.isEmpty()) {
				return new ApiResponse(false, "Phone already registered");
			}

			iGenericDao.executeDMLSQL(JavaConstant.INSERT_USER_BASIC, new Object[] { fullName, phoneNumber });

			return new ApiResponse(true, "Basic registration completed");
		} catch (Exception e) {
			e.printStackTrace();
			return new ApiResponse(false, "Server error: " + e.getMessage());
		}
	}

	// ============ COMPLETE PROFILE ============
	@Override
	@Transactional
	public ApiResponse completeProfile(String phoneNumber, Map<String, Object> fields, MultipartFile photo) {
		try {
			phoneNumber = normalize(phoneNumber);
			if (phoneNumber == null) {
				return new ApiResponse(false, "Phone number needed");
			}

			List<Map> existing = iGenericDao.executeDDLSQL(JavaConstant.GET_USER_BY_PHONE, new Object[] { phoneNumber });
			if (existing == null || existing.isEmpty()) {
				return new ApiResponse(false, "User not found");
			}

			List<String> setClauses = new ArrayList<>();
			List<Object> values = new ArrayList<>();

			Map<String, String> allowedColumns = allowedProfileColumns();

			for (Map.Entry<String, Object> e : fields.entrySet()) {
				String column = allowedColumns.get(e.getKey());
				if (column == null || e.getValue() == null) continue;
				String val = String.valueOf(e.getValue());
				if (column.equals("password")) {
					val = passwordUtil.hash(val);
				}
				if (column.equals("dob")) {
					setClauses.add("dob = CAST(?" + (values.size() + 1) + " AS date)");
				} else {
					setClauses.add(column + " = ?" + (values.size() + 1));
				}
				values.add(val);
			}

			String imagePath = fileStorageUtil.store(photo);
			if (imagePath != null) {
				setClauses.add("photo = ?" + (values.size() + 1));
				values.add(imagePath);
			}

			if (setClauses.isEmpty()) {
				return new ApiResponse(false, "No fields provided to update");
			}

			setClauses.add("updated_at = CURRENT_TIMESTAMP");
			values.add(phoneNumber);
			int whereIndex = values.size();

			String query = "update app_user set " + String.join(", ", setClauses) + " where phone_number = ?" + whereIndex;
			iGenericDao.executeDMLSQL(query, values.toArray());

			return new ApiResponse(true, "Profile updated successfully");
		} catch (Exception e) {
			e.printStackTrace();
			return new ApiResponse(false, "Server error: " + e.getMessage());
		}
	}

	// ============ EDIT PROFILE (by userId) ============
	@Override
	@Transactional
	public ApiResponse editProfile(Integer userId, Map<String, Object> fields, MultipartFile photo) {
		try {
			if (userId == null) {
				return new ApiResponse(false, "User ID required");
			}

			List<Map> existing = iGenericDao.executeDDLSQL(JavaConstant.GET_USER_BY_USER_ID, new Object[] { userId });
			if (existing == null || existing.isEmpty()) {
				return new ApiResponse(false, "User not found");
			}

			List<String> setClauses = new ArrayList<>();
			List<Object> values = new ArrayList<>();

			Map<String, String> allowedColumns = allowedProfileColumns();
//			allowedColumns.put("gender", "gender");
//			allowedColumns.put("aadh", "aadhar_number");

			for (Map.Entry<String, Object> e : fields.entrySet()) {
				String column = allowedColumns.get(e.getKey());
				if (column == null || e.getValue() == null) continue;
				String val = String.valueOf(e.getValue());
				if (column.equals("password")) {
					val = passwordUtil.hash(val);
				}
				if (column.equals("dob")) {
					setClauses.add("dob = CAST(?" + (values.size() + 1) + " AS date)");
				} else {
					setClauses.add(column + " = ?" + (values.size() + 1));
				}
				values.add(val);
			}

			String imagePath = fileStorageUtil.store(photo);
			if (imagePath != null) {
				setClauses.add("photo = ?" + (values.size() + 1));
				values.add(imagePath);
			}

			if (setClauses.isEmpty()) {
				return new ApiResponse(false, "No fields provided to update");
			}

			setClauses.add("updated_at = CURRENT_TIMESTAMP");
			values.add(userId);
			int whereIndex = values.size();

			String query = "update app_user set " + String.join(", ", setClauses) + " where user_id = ?" + whereIndex;
			iGenericDao.executeDMLSQL(query, values.toArray());

			return new ApiResponse(true, "Profile updated successfully");
		} catch (Exception e) {
			e.printStackTrace();
			return new ApiResponse(false, "Server error: " + e.getMessage());
		}
	}
	
	@Override
	public Map<String, Object> getAllUsers(String fullName, String phone, String userId) {
		Map<String, Object> result = new LinkedHashMap<>();
		try {
			StringBuilder where = new StringBuilder(" where 1=1 ");
			List<Object> params = new ArrayList<>();

			if (userId != null && !userId.isBlank()) {
				where.append(" and user_id = ?").append(params.size() + 1);
				params.add(Integer.parseInt(userId));
			}
			if (fullName != null && !fullName.isBlank()) {
				where.append(" and full_name ilike ?").append(params.size() + 1);
				params.add("%" + fullName + "%");
			}
			if (phone != null && !phone.isBlank()) {
				where.append(" and phone_number ilike ?").append(params.size() + 1);
				params.add("%" + phone + "%");
			}

			// password column intentionally excluded from the select list
			String query =
				"select user_id, full_name, phone_number, photo, father_name, preparation_for, " +
				"dob, blood_group, email, personal_number, emergency_number, present_address, " +
				"permanent_address, gender, aadhar_number, created_at, updated_at " +
				"from app_user" + where + " order by created_at desc";

			List<Map> data = iGenericDao.executeDDLSQL(query, params.toArray());

			result.put("success", true);
			result.put("total", data.size());
			result.put("data", data);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			result.put("success", false);
			result.put("message", "Fetch all users error");
			result.put("data", Collections.emptyList());
			return result;
		}
	}

	private Map<String, String> allowedProfileColumns() {
		Map<String, String> m = new HashMap<>();
		m.put("fullName", "full_name");
		m.put("fatherName", "father_name");
		m.put("preparationFor", "preparation_for");
		m.put("dob", "dob");
		m.put("bloodGroup", "blood_group");
		m.put("email", "email");
		m.put("personalNumber", "personal_number");
		m.put("personalNumber", "personal_number");
		m.put("emergencyNumber", "emergency_number");
		m.put("presentAddress", "present_address");
		m.put("permanentAddress", "permanent_address");
		m.put("password", "password");
	    m.put("gender", "gender");
	    m.put("aadh", "aadhar_number");
		return m;
	}
	
	private String generateOtp() {
	    return String.valueOf(
	            100000 + new Random().nextInt(900000)
	    );
	}
}
