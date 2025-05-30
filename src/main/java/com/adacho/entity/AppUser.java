package com.adacho.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Component
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AppUser implements UserDetails{
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY) // 자동증가 옵션. 
	private Long id;
	
	@Column(nullable=false, unique=true) // uid 값은 null일수없음.
	private String uid;
	
	@JsonProperty(access=Access.WRITE_ONLY) // 사용자가 입력한 비밀번호를 클라이언트가 서버로 보낼때, 클라이언트는  password를 알수없게 해주는 어노테이션
	@Column(nullable=false)
	private String password;
	
	@Column(nullable=false)
	private String name;
	
	@ElementCollection(fetch=FetchType.EAGER) // roles 필드는 지연로딩(필요하지 않으면 로딩을 안하는것)을 못하게 막는 어노테이션
	@Builder.Default // builder 패턴의 기본값으로 roles라는 리스트를 사용하겠다는 의미
	private List<String> roles = new ArrayList<>();

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		// TODO Auto-generated method stub
		return this.roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()); // .stream() -> roles 리스트를 순차적으로 처리할 수 있는 스트림으로 변환합니다. 
	}

	@Override
	public String getUsername() {
		// TODO Auto-generated method stub
		return null;
	}
	

}
