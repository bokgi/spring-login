package com.adacho.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.adacho.entity.AppUser;

public interface AppUserRepository extends JpaRepository<AppUser, Long>{
	AppUser getByUid(String uid); // uid가 String uid인 유저를 찾고 정보들을 리턴함.
}
