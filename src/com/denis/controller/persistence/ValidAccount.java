package com.denis.controller.persistence;

import java.io.Serializable;

public class ValidAccount implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ValidAccount(String address, String password, String type) {
		this.address = address;
		this.password = password;
		this.type = type;
	}

	public ValidAccount(String userName, String deptName, int deptCode, int userLevel, int userSecurityLevel) {
		this.userName = userName;
		this.deptName = deptName;
		this.deptCode = deptCode;
		this.userLevel = userLevel;
		this.userSecurityLevel = userSecurityLevel;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isValidAccount() {
		return this.address != null &&
				this.address.trim().length() > 0 &&
				this.password != null &&
				this.password.trim().length() > 0;
	}

	//user info
	String userName;
	String deptName;
	int deptCode;
	int userLevel;
	int userSecurityLevel;

	private String address;
	public String getAddress() {
		return address;
	}
	private String password;
	public String getPassword() {
		return password;
	}
	private String type;
	public String getType() {
		return type;
	}

	/**
	 * get the user name from email
	 * ex: 123@mail.com => 123
	 * @param address
	 * @return
	 */
	public static String getUserNameFrom(String address) {
	    return address.substring(0, address.indexOf("@"));
    }

	/**
	 * get the formatted email string
	 * @param email
	 * @return
	 */
	public static String getFormattedEmailFrom(String email) {
		if (email.indexOf("<") < 0)
			email = String.format("%s <%s>", getUserNameFrom(email), email);
		return email;
	}

    public boolean isSameEmail(String from) {
		try {
			return address.compareToIgnoreCase(from) == 0;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
    }

    public String getUserName() {
		return this.userName;
	}

    public void setUserName(String userName) {
		this.userName = userName;
    }

	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}

	public void setDeptCode(int deptCode) {
		this.deptCode = deptCode;
	}

	public void setUserLevel(int userLevel) {
		this.userLevel = userLevel;
	}

	public void setUserSecLevel(int userSecurityLevel) {
		this.userSecurityLevel = userSecurityLevel;
	}

	public boolean isValidUserInfo() {
		return this.userName != null &&
				this.userName.trim().length() > 0 &&
				this.deptName != null &&
				this.deptName.trim().length() > 0;
	}
}
