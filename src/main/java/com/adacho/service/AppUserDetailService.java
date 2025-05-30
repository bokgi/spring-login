package com.adacho.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.adacho.repository.AppUserRepository;

@Service
public class AppUserDetailService implements UserDetailsService{
	private final AppUserRepository appUserRepository;
	
	public AppUserDetailService(AppUserRepository appUserRepository) {
		this.appUserRepository = appUserRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// TODO Auto-generated method stub
		return appUserRepository.getByUid(username);
	}
}
