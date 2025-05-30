package com.adacho.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignUpResultDto {

	private boolean success; // signup의 결과를 SignUpResultDto 객체의 세개 필드에 넣는다.
	private int code;
	private String msg;
}
