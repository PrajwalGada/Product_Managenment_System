package com.example.demo.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.model.Product;
import com.example.demo.repositary.ProductRepoitary;

@Service
public class ProductService {
	@Autowired
	ProductRepoitary productRepoitary;

	public void saveProduct(Product product) {
		 Date date = new Date();
		product.setRegistrationDate(date);
		productRepoitary.save(product);	
	}

}
