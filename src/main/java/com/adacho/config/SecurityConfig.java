package com.adacho.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.adacho.security.CustomAccessDeniedHandler;
import com.adacho.security.CustomAuthenticationEntryPoint;
import com.adacho.security.JwtAuthenticationFilter;
import com.adacho.security.JwtTokenProvider;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled=true)
public class SecurityConfig {
	private final JwtTokenProvider jwtTokenProvider;
	
	public SecurityConfig(JwtTokenProvider jwtTokenProvider) {
		this.jwtTokenProvider = jwtTokenProvider;
	}
	
	@Bean // 프로그램을 실행하면 bean 메서드가 호출되고 이 메서드의 반환객체 SecurityFilterChain객체가 스프링 컨테이너에 들어간다.
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.httpBasic(HttpBasicConfigurer::disable) // UI를 사용하는 것을 기본값으로 가진 시큐리티 설정을 비활성
				.cors(cors -> cors.disable())
				.csrf(CsrfConfigurer::disable) // CSRF 보안설정 비활성
				.sessionManagement(configurer -> configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // session을 사용하지 않는다는 의미
				// JWT 토큰인증 방식의 사용으로 세션은 사용하지 않음
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers("/api/sign-api/sign-in", "/api/sign-api/sign-up", "/api/sign-api/exception", "/get/name", "/api/sign-api/kakao-sign-in").permitAll() // 3개의 주소에 대해서는 security 적용x
						.requestMatchers("**exception**").permitAll()
						.anyRequest().hasAnyRole("ADMIN") 
						// 나머지 주소에 대한 요청은 ADMIN 권한을 가진 사용자에게 허용
				)
				.exceptionHandling(authenticationManager -> authenticationManager
						.authenticationEntryPoint(new CustomAuthenticationEntryPoint()) // 인증 과정에서 발생하는 예외
						.accessDeniedHandler(new CustomAccessDeniedHandler())) // 권한을 확인하는 과정에서 발생한 예외
				.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
						UsernamePasswordAuthenticationFilter.class);
					// 현재 필터에서 인증이 정상처리되면 UsernamePasswordAuthenticationFilter 는 자동으로 통과
		return http.build();
	}
	
}
