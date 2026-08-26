package com.library.services;

import com.library.bean.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface AuthService {

	ApiResponse adminRegister(AdminDetails adminDetails, MultipartFile photo);

	Map<String, Object> adminLogin(String phone, String password);

	ApiResponse adminEditProfile(String phone, String name, String newPhone, String password, MultipartFile photo);

	ApiResponse register(UserDetails userDetails);
	
	ApiResponse forgotPassword(ForgotPasswordRequest request);

	ApiResponse verifyOtp(VerifyOtpRequest request);

	ApiResponse resetPassword(ResetPasswordRequest request);

	Map<String, Object> unpaidUser(String phoneNumber);

	Map<String, Object> login(String phoneNumber, String password);

	ApiResponse registerBasic(UserDetails userDetails);

	ApiResponse completeProfile(String phoneNumber, Map<String, Object> fields, MultipartFile photo);

	ApiResponse editProfile(Integer userId, Map<String, Object> fields, MultipartFile photo);
	
	ApiResponse removePhoto(Integer userId);
	
	Map<String, Object> getAllUsers(String fullName, String phone, String userId);
}
