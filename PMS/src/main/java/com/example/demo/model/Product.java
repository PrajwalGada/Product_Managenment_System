package com.example.demo.model;

import java.sql.Blob;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;


@Table(name = "Product")
@Entity
@Getter
@Setter
public class Product {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	private String productName;
	
	private long qty;
	
	private Date registrationDate;
	
	private String price;
	
	private  String description;
	@Lob
    @Column(columnDefinition = "MEDIUMBLOB", name = "photo")
    @JsonIgnore
    private Blob photo;
	
	

}
