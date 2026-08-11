//package com.library.bean;
//
//public class ResetPasswordRequest {
//	private String phoneNumber;
//	private String newPassword;
//
//	public String getPhoneNumber() { return phoneNumber; }
//	public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
//	public String getNewPassword() { return newPassword; }
//	public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
//}


package com.library.bean;

public class ResetPasswordRequest {

    private String email;
    private String newPassword;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}