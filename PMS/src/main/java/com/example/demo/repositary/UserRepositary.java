package com.example.demo.repositary;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Users;

@Repository 
public interface UserRepositary extends JpaRepository<Users, Long> {

	Users findByEmail(String email);

}
