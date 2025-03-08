package com.opmgmt.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.Data;

@Entity
@Data
public class EventParameters {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	//private Long fileType;				//File type
	private boolean enableEncription;	//Tells whether file is encrypted or not
	private String encryptionType;		//if encrypted then what type of encryption is used
	private String fileNameFormat;		//Unique for every partner to differentiate between partners
	private String secretKey;
	
	@OneToOne(mappedBy="eventParameter")
	@JsonIgnore
    private ScheduledEvents scheduledEvents;
	
	
}
