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

@Entity
@Table(name = "users")
@Getter
@Setter
public class Users {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	private boolean admin = false;

	private String firstName;

	private String lastName;

	private String mobileNo;

	private String email;

	private String City;

	private String State;

	private String password;

	private String registrationDate;

	private String updatedDate;

	private Date dob;

	@Lob
	@Column(columnDefinition = "MEDIUMBLOB", name = "photo")
	@JsonIgnore
	private byte[] photo;

}
