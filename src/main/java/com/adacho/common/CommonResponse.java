package com.adacho.common;

public enum CommonResponse {
	
	SUCCESS(0, "Success"), FAIL(-1, "Fail"); // SUCCESS 에 code, msg가 들어있고 code의 값은 0, msg의 값은 "Success"
	int code;								// FAIL 에 code, msg가 들어있고 code의 값은 -1, msg의 값은 "Fail"
	String msg;
	
	CommonResponse(int code, String msg) {
		this.code = code;
		this.msg = msg;
	}

	public int getCode() { //CommonResponse.SUCESS.getCode = 0
		return code;
	}

	public String getMsg() { //CommonResponse.SUCESS.getMsg = "Success"
		return msg;
	}
	
}
