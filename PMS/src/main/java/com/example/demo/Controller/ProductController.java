package com.example.demo.Controller;

import java.sql.Blob;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.model.Product;
import com.example.demo.model.Users;
import com.example.demo.service.ProductService;
import com.example.demo.service.UsersService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.xml.ws.Response;

@RestController
@RequestMapping("/rest")
public class ProductController {

	@Autowired
	ProductService productService;

	@Autowired
	UsersService usersService;
	
	@GetMapping("/home")
	public String home(HttpSession session, HttpServletRequest httpServletRequest) {
	    return "login"; // This will map to login.html in the templates directory
	}

	@PostMapping("/registerUser")
	public ResponseEntity<Map<String, String>> registerUser(@RequestBody Users users,
			@RequestParam("image") MultipartFile file) {
		Map<String, String> response = new HashMap<>();
		try {
			if (file != null && !file.isEmpty()) {
				byte[] bytes = file.getBytes();
				Blob blob = new javax.sql.rowset.serial.SerialBlob(bytes);
				users.setPhoto(blob);
			}
			boolean message = usersService.svaeUsers(users);
			if (message) {
				response.put("message", "User saved successfully");
				return ResponseEntity.status(HttpStatus.CREATED).body(response);
			} else {
				response.put("message", "User already exists or could not be saved");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.put("message", "An error occurred while saving the user");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	@PostMapping("/registerProduct")
	public ResponseEntity<Map<String, String>> registerProduct(@RequestBody Product product,
			@RequestParam("image") MultipartFile file) {
		Map<String, String> response = new HashMap<>();
		try {
			if (file != null) {
				byte[] bytes = file.getBytes();
				Blob blob = new javax.sql.rowset.serial.SerialBlob(bytes);
				product.setPhoto(blob);
			}
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