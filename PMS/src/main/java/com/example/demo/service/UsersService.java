package com.example.demo.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.model.Users;
import com.example.demo.repositary.UserRepositary;

@Service
public class UsersService {
	@Autowired
	UserRepositary userRepositary;
	

	public Boolean svaeUsers(Users users) {
		 Users existingUsers = userRepositary.findByEmail(users.getEmail());
	        MessageDigest md = null;
	        if (existingUsers!=null) {

	            return false;
	        }
	        if(users.getPassword()!=null)
	        {
	        	try {
					md = MessageDigest.getInstance("SHA-256");
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				}
				byte[] hashedPassword = md.digest(users.getPassword().getBytes(StandardCharsets.UTF_8));
				StringBuilder sb = new StringBuilder();
				for (byte b : hashedPassword) {
					sb.append(String.format("%02x", b));
				}
				users.setAdmin(false);
				users.setPassword(sb.toString());
	        }
	        userRepositary.save(users);
	        return true;
		
		
	}


	public Users findByEmail(String email) {
		Users existingEmployee = userRepositary.findByEmail(email);
		return existingEmployee;
	}


	public List<Users> getAll() {
		// TODO Auto-generated method stub
		return userRepositary.findAll();
	}

}
