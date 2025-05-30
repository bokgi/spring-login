package com.adacho.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// 클라에서 서버로 요청을 보내면 요청이 필터를 거쳐서 dispatcher servlet으로 간다.
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private final JwtTokenProvider jwtTokenProvider;

	public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
		this.jwtTokenProvider = jwtTokenProvider;
	}

	private Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String path = request.getServletPath();
		logger.info("[doFilterInternal] 요청 경로 : {}", path);
		
		if ("/sign-api/sign-in".equals(path) || "/sign-api/sign-up".equals(path) || "/sign-api/exception".equals(path) || "/get/name".equals(path)) {
			logger.info("[doFilterInternal] Skipping JWT filter for permitAll path: {}", path);
			filterChain.doFilter(request, response); // 필터 건너뛰고 다음 필터로 진행
			return; // 중요: 이후 코드가 실행되지 않도록 return 합니다.
		}

		// TODO Auto-generated method stub
		String token = jwtTokenProvider.resolveToken(request);
		logger.info("[doFilterInternal] token 값 추출 완료. token : {}", token);

		logger.info("[doFilterInternal] token 값 유효성 체크 시작");
		if (token != null && jwtTokenProvider.validateToken(token)) {
			Authentication authentication = jwtTokenProvider.getAuthentication(token);
			SecurityContextHolder.getContext().setAuthentication(authentication);
			logger.info("[doFilterInternal] token 값 유효성 체크 완료");
		}
		// Dispatcher Servlet 실행 전에 필터
		filterChain.doFilter(request, response);
	}
}
