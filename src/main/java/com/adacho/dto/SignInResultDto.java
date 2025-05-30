package com.adacho.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignInResultDto {
	
	private String token;
	private String name;
	private boolean success;
	private int code;
	private String msg;
	
	@Builder
	public SignInResultDto(boolean success, int code, String msg, String token) {
		this.success = success;
		this.code = code;
		this.msg = msg;
		this.token = token;
	}
}
