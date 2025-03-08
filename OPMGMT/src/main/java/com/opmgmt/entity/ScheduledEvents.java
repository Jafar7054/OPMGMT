package com.opmgmt.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Data;

@Entity
@Data
public class ScheduledEvents {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	private String eventName;
	private String eventType;
	private String eventStatus;
	private String triggerType;
	private String triggerTime;
	private String triggerInterval;
	private String day;
	private String date;
	private String monthDate;
	
	@OneToOne
	@JoinColumn(name="eventParameterId")
	private EventParameters eventParameter;
	
	@OneToOne
	@JoinColumn(name="sftpInfoId")
	@JsonIgnore
	private SftpInfo sftpInfo;
	
}
