package com.adacho.security;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.adacho.service.AppUserDetailService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class JwtTokenProvider {
	private final AppUserDetailService appUserDetailService;
	
	public JwtTokenProvider(AppUserDetailService appUserDetailService) {
		this.appUserDetailService = appUserDetailService;
	}
	private Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

	@Value("${springboot.jwt.secret}") // application.properties 파일에 있는 springboot.jwt.secret 의 값을 가져와서 secretKey 변수에 넣는다.
	private String secretKey = "randomKey";
	private final long tokenValidMillisecond = 1000L * 60;// 토큰 만료 기간을 1시간으로 설정. 1000L = 1초
	//1000L * 60 * 60;
	// 스프링 빈 생성 후 실행(secretKey 값을 Base64형식으로 인코딩하여 저장) // 객체생성후 init 메서드 실행
	@PostConstruct
	protected void init() {
		secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes(StandardCharsets.UTF_8)); // SECRET KEY를 BASE64로 인코딩
	}

	private SecretKey getSigningKey() {
		byte[] keyBytes = Decoders.BASE64.decode(secretKey); // base64로 인코딩된 secretkey를 디코딩하고 keybytes에 넣음.
		return Keys.hmacShaKeyFor(keyBytes);
	}

	public String createToken(String userUid, String role) { 
		logger.info("[createToken] 토큰 생성 시작");

		Date now = new Date(); // 현재날짜생성
		String token = Jwts.builder().subject(userUid).claim("roles", role).issuedAt(now) // token을 생성하고 문자열로 변환한뒤에 token에 넣음.
				.expiration(new Date(now.getTime() + tokenValidMillisecond)).signWith(getSigningKey()).compact(); // .compact() -> 토큰을 문자열로 변환
		// subject(userUid) : sub 클레임에 userUid 저장
		// claim("roles", roles) : roles 라는 비공개 클레임에 roles 저장
		logger.info("[createToken] 토큰 생성 완료");
		return token;
	}

	// JWT 토큰으로 인증 정보 조회
	public Authentication getAuthentication(String token) { // 토큰을 주고 토큰이 인증이 되어있는지 확인
		logger.info("[getAuthentication] 토큰 인증 정보 조회 시작");
		UserDetails userDetails = appUserDetailService.loadUserByUsername(this.getUsername(token));
		logger.info("[getAuthentication] 토큰 인증 정보 조회 완료, UserDetails UserName : {}", userDetails.getUsername());
		return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
	}

	// JWT 토큰에서 회원 구별 정보 추출
	public String getUsername(String token) {
		logger.info("[getUsername] 토큰 기반 회원 구별 정보 추출");
		String info = Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload()
				.getSubject();
		logger.info("[getUsername] 토큰 기반 회원 구별 정보 추출 완료, info : {}", info);
		return info;
	}

	/**
	 * HTTP Request Header 에 설정된 토큰 값을 가져옴
	 *
	 * @param request Http Request Header
	 * @return String type Token 값
	 */
	public String resolveToken(HttpServletRequest request) {
		logger.info("[resolveToken] HTTP 헤더에서 Token 값 추출");
		return request.getHeader("X-AUTH-TOKEN"); // 요청에 포함된 헤더에서 X-AUTH-TOKEN 이라는 변수를 꺼내서 리턴함. X-AUTH-TOKEN에 token 값이 들어있음.
	}

	// JWT 토큰의 유효성 + 만료일 체크
	public boolean validateToken(String token) {
		logger.info("[validateToken] 토큰 유효 체크 시작");
		try {
			Jws<Claims> claims = Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
			logger.info("[validateToken] 토큰 유효 체크 완료");
			return !claims.getPayload().getExpiration().before(new Date()); // 만료기간이 현재날짜보다 이전이라면 false, 이후라면 true 리턴
		} catch (Exception e) {
			logger.info("[validateToken] 토큰 유효 체크 예외 발생");
			return false;
		}
	}
}
