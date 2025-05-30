package com.adacho.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.adacho.repository.AppUserRepository;

@RestController
@RequestMapping("/get")
public class UserController {
	private final AppUserRepository appUserRepository;
	
	public UserController(AppUserRepository appUserRepository) {
		this.appUserRepository = appUserRepository;
	}
	
	@GetMapping("/name")
	public String getUserName(@RequestParam String userId) {
		return appUserRepository.getByUid(userId).getName();
	}
}
