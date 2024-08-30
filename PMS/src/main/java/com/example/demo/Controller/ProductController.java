package com.example.demo.Controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.Product;
import com.example.demo.model.Users;
import com.example.demo.service.ProductService;
import com.example.demo.service.UsersService;

import jakarta.xml.ws.Response;

@RestController("/rest")
public class ProductController {

	@Autowired
	ProductService productService;

	@Autowired
	UsersService usersService;

	@PostMapping("/registerUser")
	public ResponseEntity<Map<String, String>> registerUser(@RequestBody Users users) {
		boolean message = usersService.svaeUsers(users);
		Map<String, String> response = new HashMap<>();
		String res = "";
		if (message) {
			response.put("message", "User saved successfully");
			return ResponseEntity.status(HttpStatus.CREATED).body(response);
		} else {
			response.put("message", "User already exists or could not be saved");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}

	}
	
	@PostMapping("/registerProduct")
	public ResponseEntity<Map<String, String>> registerProduct(@RequestBody Product product) {
	    Map<String, String> response = new HashMap<>();
	    try {
	        productService.saveProduct(product);
	        response.put("message", "Product saved successfully");
	        return ResponseEntity.status(HttpStatus.CREATED).body(response);
	    } catch (Exception e) {
	        e.printStackTrace();
	        response.put("message", "Failed to save product");
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	    }
	}

}