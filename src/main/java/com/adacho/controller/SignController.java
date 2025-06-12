package com.adacho.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.adacho.dto.KakaoUserDto;
import com.adacho.dto.SignInRequestDto;
import com.adacho.dto.SignInResultDto;
import com.adacho.dto.SignUpRequestDto;
import com.adacho.dto.SignUpResultDto;
import com.adacho.service.KakaoService;
import com.adacho.service.SignService;

@RestController
@RequestMapping("/api/sign-api")
public class SignController {
	private final SignService signService;
	private final KakaoService kakaoService;
	
	public SignController(SignService signService, KakaoService kakaoService) {
		this.signService = signService;
		this.kakaoService = kakaoService;
	}

	private Logger logger = LoggerFactory.getLogger(SignController.class);

	@PostMapping("/sign-in")
	public ResponseEntity<?> signIn(@RequestBody SignInRequestDto signInRequestDto) {
	    logger.info("[signIn] 로그인을 시도하고 있습니다. id : {}, pw : ****", signInRequestDto.getId());

	    try {
	        SignInResultDto signInResultDto = signService.signIn(signInRequestDto.getId(), signInRequestDto.getPassword());

	        if (signInResultDto.getCode() == 0) {
	            logger.info("[signIn] 정상적으로 로그인되었습니다. id : {}, token : {}, name : {}",
	                    signInRequestDto.getId(), signInResultDto.getToken(), signInResultDto.getName());
	        } else if (signInResultDto.getCode() == -1) {
	            logger.info("[signIn] 로그인 실패");
	        }

	        return ResponseEntity.ok(signInResultDto); // 정상 로그인 응답

	    } catch (Exception e) {
	        logger.error("[signIn] 로그인 도중 예외 발생: {}", e.getMessage());
	        e.printStackTrace();

	        // 에러 메시지와 함께 HTTP 500 응답
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body("로그인 중 서버 오류가 발생했습니다.");
	    }
	}

	@PostMapping("/sign-up")
	public ResponseEntity<?> signUp(@RequestBody SignUpRequestDto signUpRequestDto) {
		signUpRequestDto.setRole("USER");
		logger.info("[signUp] 회원가입을 수행합니다. id : {}, password : ****, name : {}, role : {}", signUpRequestDto.getId(), signUpRequestDto.getName(), signUpRequestDto.getRole());
		
		try {
			SignUpResultDto signUpResultDto = signService.signUp(signUpRequestDto.getId(), signUpRequestDto.getPassword(), signUpRequestDto.getName(), signUpRequestDto.getRole());

			logger.info("[signUp] 회원가입을 완료했습니다. id : {}", signUpRequestDto.getId());
			return ResponseEntity.ok(signUpResultDto);
			
		}catch (Exception e) {
	        logger.error("[signIn] 로그인 도중 예외 발생: {}", e.getMessage());
	        e.printStackTrace();

	        // 에러 메시지와 함께 HTTP 500 응답
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body("회원가입 중 서버 오류가 발생했습니다.");
		}
	}

	@GetMapping("/exception")
	public void exceptionTest() throws RuntimeException {
		throw new RuntimeException("접근이 금지되었습니다.");
	}

	@PostMapping("/kakao-sign-in")
	public ResponseEntity<?> kakaoSignIn(@RequestParam String code) {
	    try {
	        logger.info("[kakao-signIn] 카카오 로그인 시도");

	        String accessToken = kakaoService.getAccessToken(code);
	        KakaoUserDto kakaoUserDto = kakaoService.getUserInfo(accessToken);

	        SignInResultDto signInResultDto = kakaoService.isThisUser(kakaoUserDto);

	        if (signInResultDto.getCode() == 0) {
	            logger.info("[kakao-signIn] 정상적으로 로그인되었습니다.");
	        } else if (signInResultDto.getCode() == -1) {
	            logger.info("[kakao-signIn] 로그인 실패");
	        }

	        return ResponseEntity.ok(signInResultDto); // 정상 응답

	    } catch (Exception e) {
	        logger.error("[kakao-signIn] 카카오 로그인 중 예외 발생: {}", e.getMessage());
	        e.printStackTrace();

	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body("카카오 로그인 처리 중 서버 오류가 발생했습니다.");
	    }
	}

	
	@ExceptionHandler(value=RuntimeException.class)
	public Map<String, String> ExceptionHandler(RuntimeException e) {
		HttpStatus httpStatus = HttpStatus.BAD_REQUEST;

		logger.error("ExceptionHandler 호출, {}, {}", e.getCause(), e.getMessage());

		Map<String, String> map = new HashMap<>();
		map.put("error type", httpStatus.getReasonPhrase());
		map.put("code", "400");
		map.put("message", "에러 발생");

		return map;
	}
	
}
