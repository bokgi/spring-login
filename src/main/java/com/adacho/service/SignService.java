package com.adacho.service;

import java.util.Collections;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.adacho.common.CommonResponse;
import com.adacho.dto.SignInResultDto;
import com.adacho.dto.SignUpResultDto;
import com.adacho.entity.AppUser;
import com.adacho.repository.AppUserRepository;
import com.adacho.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SignService {
	private final AppUserRepository appUserRepository;
	private final JwtTokenProvider jwtTokenProvider;
	private final PasswordEncoder passwordEncoder;
	
	public SignUpResultDto signUp(String id, String password, String name, String role) {
		AppUser appUser;
		AppUser existingUser = appUserRepository.getByUid(id);
		
	    if (existingUser != null) {
	    	
	    	SignUpResultDto signUpResultDto = new SignUpResultDto();
	    	duplicate(signUpResultDto, id);
	    	
	        return signUpResultDto; // 중복됨
	    }
		
		if (role.equalsIgnoreCase("admin")) {
			appUser = AppUser.builder().uid(id).name(name).password(passwordEncoder.encode(password)) // AppUser 클래스가 builder 이므로 이렇게 객체 생성가능
					.role(role).build();							  // new AppUser() 와 같은 기능을함.
		} else {
			appUser = AppUser.builder().uid(id).name(name).password(passwordEncoder.encode(password))
					.role(role).build();
		}

		AppUser savedUser = appUserRepository.save(appUser); // appUser를 테이블에 저장하고 appUser가 savedUser에 들어감.
		SignUpResultDto signUpResultDto = new SignUpResultDto();

		if (!savedUser.getName().isEmpty()) {
			setSuccessSignUpResult(signUpResultDto);
		} else {
			setFailSignUpResult(signUpResultDto);
		}
		return signUpResultDto;
	}
	
	public SignInResultDto signIn(String id, String password) throws RuntimeException{
		
		AppUser appUser = appUserRepository.getByUid(id); // db에서 id가 입력받은 id와 같은 유저 찾은 후 appUser객체를 만들고 객체의 필드를 그 유저 속성값과 매칭함.
		
		if(appUser == null) { //입력받은 id가 db에 없으면
			System.out.println("**id없음**");
			SignInResultDto signInResultDto = new SignInResultDto();
			setFailSignInResult(signInResultDto);
			return signInResultDto;
		
		}
		if(!passwordEncoder.matches(password, appUser.getPassword())) { // 입력한 패스워드와 db의 패스워드가 다르다면
			System.out.println("**pw다름**");
			throw new RuntimeException();
		}
		//입력한 패스워드와 db의 패스워드가 같으면 (로그인 성공하면)
		SignInResultDto signInResultDto = SignInResultDto.builder() // SignInResultDto 객체 생성하고 signInResultDto에 넣음.
				.token(jwtTokenProvider.createToken(String.valueOf(appUser.getUid()), appUser.getRoles()))
				.build();
		signInResultDto.setName(appUser.getName());
		setSuccessSignInResult(signInResultDto);
		
		return signInResultDto;
	}
	
	private void setSuccessSignUpResult(SignUpResultDto result) {
		result.setSuccess(true);
		result.setCode(CommonResponse.SUCCESS.getCode());
		result.setMsg(CommonResponse.SUCCESS.getMsg());
	}
	
	private void setFailSignUpResult(SignUpResultDto result) {
		result.setSuccess(false);
		result.setCode(CommonResponse.FAIL.getCode());
		result.setMsg(CommonResponse.FAIL.getMsg());
	}
	
	private void setSuccessSignInResult(SignInResultDto result) {
		result.setSuccess(true);
		result.setCode(CommonResponse.SUCCESS.getCode());
		result.setMsg(CommonResponse.SUCCESS.getMsg());
	}
	
	private void setFailSignInResult(SignInResultDto result) {
		result.setSuccess(false);
		result.setCode(CommonResponse.FAIL.getCode());
		result.setMsg(CommonResponse.FAIL.getMsg());
	}
	
	private void duplicate(SignUpResultDto result, String id) {    	
		result.setCode(-2);
		result.setSuccess(false);
		result.setMsg("이미 존재하는 아이디");
	}
}
