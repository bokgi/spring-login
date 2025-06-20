package com.adacho.service;

import java.util.Optional;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.adacho.dto.KakaoUserDto;
import com.adacho.dto.SignInResultDto;
import com.adacho.entity.AppUser;
//import com.adacho.entity.KakaoUser;
import com.adacho.repository.AppUserRepository;
//import com.adacho.repository.KakaoUserRepository;
import com.adacho.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoService {
	private final AppUserRepository appUserRepository;
//	private final KakaoUserRepository kakaoUserRepository;
	private final JwtTokenProvider jwtTokenProvider;
	private final PasswordEncoder passwordEncoder;
	
	private final RestTemplate restTemplate = new RestTemplate();
	
	public KakaoUserDto getUserInfo(String accessToken) {
		
	    String kakaoApi = "https://kapi.kakao.com/v2/user/me";
	    
	    HttpHeaders headers = new HttpHeaders();
	    headers.set("Authorization", "Bearer " + accessToken);
	    HttpEntity<?> entity = new HttpEntity<>(headers);

	    ResponseEntity<String> response = restTemplate.exchange(kakaoApi, HttpMethod.GET, entity, String.class);
	    
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response.getBody());

            Long id = jsonNode.get("id").asLong();
            String nickname = jsonNode.path("properties").path("nickname").asText();
            String email = jsonNode.path("kakao_account").path("email").asText();
            
            KakaoUserDto kakaoUserDto = new KakaoUserDto();
            kakaoUserDto.setEmail(email);
            kakaoUserDto.setUserName(nickname);
            kakaoUserDto.setId(id);
  
            return kakaoUserDto;

        } catch (Exception e) {
            throw new RuntimeException("카카오 사용자 정보 파싱 실패", e);
        }
	}
	
	public String getAccessToken(String code) {
		
	    String tokenUrl = "https://kauth.kakao.com/oauth/token";

	    HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

	    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
	    params.add("grant_type", "authorization_code");
	    params.add("client_id", "55d2a867b5b86ca3c3b738518c2e03c5"); // 카카오 앱 REST API 키
	    params.add("redirect_uri", "https://matgpt.p-e.kr/kakao"); // 프론트에서 설정한 redirect_uri
	    params.add("code", code); // 프론트에서 받은 code

	    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

	    ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, request, String.class);

	    try {
	        ObjectMapper objectMapper = new ObjectMapper();
	        JsonNode jsonNode = objectMapper.readTree(response.getBody());
	        return jsonNode.get("access_token").asText();
	    } catch (Exception e) {
	        throw new RuntimeException("액세스 토큰 파싱 실패", e);
	    }
	}
	
	@Transactional
	public SignInResultDto isThisUser(KakaoUserDto kakaoUserDto) {
		// userdto에서 정보를 빼와서 kakaouser 테이블에 email이 같은 카카오계정있다면 -> 로그인처리
		AppUser appUser;
		Optional<AppUser> existingUser = appUserRepository.findByUid(kakaoUserDto.getEmail());
		
		if(existingUser.isPresent()) {
			appUser = existingUser.get();
		} else {
			log.info("no existing user ==> create user");
			appUser = AppUser.builder()
	                .uid(kakaoUserDto.getEmail())
	                .password("pw")
	                .name(kakaoUserDto.getUserName())
	                .role("USER")
	                .build();

	        appUserRepository.save(appUser);
		}
		// 없으면 -> 테이블에 추가(회원가입) -> 로그인처리
		
	    String token = jwtTokenProvider.createToken(appUser.getUid(),appUser.getRole());
	    SignInResultDto signInResultDto = new SignInResultDto();
	    signInResultDto.setToken(token);
	    signInResultDto.setName(appUser.getName());
	    signInResultDto.setSuccess(true);
	    signInResultDto.setCode(0);
	    signInResultDto.setMsg("Success");
	    
	    return signInResultDto;
	}
	
}
