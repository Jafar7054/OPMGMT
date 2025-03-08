package com.opmgmt.services;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.opmgmt.entity.EventParameters;
import com.opmgmt.entity.ScheduledEvents;
import com.opmgmt.entity.SftpInfo;
import com.opmgmt.repository.EventParametersRepository;
import com.opmgmt.repository.ScheduledEventsRepository;
import com.opmgmt.repository.SftpRepository;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class ScheduledEventsService {
	
	@Autowired
	ScheduledEventsRepository scheduledEventsRepository;
	
	@Autowired
	EventParametersRepository eventParametersRepository;
	
	@Autowired
	SftpRepository sftpRepository;
	
	@Autowired
	QuartzEventSchedulerService quartzEventSchedulerService;
	
	EventParameters eventParameters;
	
	SftpInfo sftpInfo;
	
	ScheduledEvents scheduledEvents;
	
	public void createEvent(HttpServletRequest request) throws Exception
	{
		
		EventParameters ep=new EventParameters();
		String enableEncryption=request.getParameter("enableEncryption");
		if("true".equalsIgnoreCase(enableEncryption))
		{
			ep.setEnableEncription(true);
			ep.setEncryptionType(request.getParameter("encryptionType"));
		}
		ep.setFileNameFormat(request.getParameter("fileNameFormat"));
		//ep.setFileType(Long.parseLong(request.getParameter("fileType")));
		eventParameters=ep;
		eventParametersRepository.save(eventParameters);
		

		SftpInfo info=new SftpInfo();
		info.setSftpHost(request.getParameter("ftpServerIp"));
		info.setSftpPort(Integer.parseInt(request.getParameter("ftpServerPort")));
		info.setSftpUsername(request.getParameter("username"));
		info.setSftpPassword(request.getParameter("password"));
		info.setLocalDir(request.getParameter("directoryPath"));
		info.setRemoteDir(request.getParameter("remotePath"));
		sftpInfo=info;
		sftpRepository.save(info);
		
		ScheduledEvents se=new ScheduledEvents();
		se.setSftpInfo(info);
		se.setEventParameter(ep);
		se.setEventName(request.getParameter("eventName"));
		se.setEventType(request.getParameter("eventType"));
		se.setTriggerType(request.getParameter("triggerType"));
		switch(se.getTriggerType())
		{
		case "interval":
		{
			se.setTriggerTime(request.getParameter("triggerTime"));
			se.setTriggerInterval(request.getParameter("triggerInterval"));
		}
		break;
		case "timeOfDay":
		{
			se.setTriggerTime(request.getParameter("triggerTime"));
		}
		break;
		case "dayOfWeek":
		{
			se.setDay(request.getParameter("day"));
		}
		break;
		case "dayOfWeekInterval":
		{
			System.out.println("in dayOfWeekInterval");
			se.setDay(request.getParameter("hiddenDay"));
			LocalTime time = LocalTime.parse(request.getParameter("hiddenTriggerTime"), DateTimeFormatter.ofPattern("HH:mm"));
            System.out.println("Received time: " + time);
			se.setTriggerTime(request.getParameter("hiddenTriggerTime"));
			se.setTriggerInterval(request.getParameter("hiddenInterval"));
			System.out.println("success");
		}
		break;
		case "dayOfMonth":
		{
			se.setMonthDate(request.getParameter("monthDate"));
		}
		break;
		case "oneTime":
		{
			se.setDate(request.getParameter("date"));
		}
		break;
		default :
		{
			System.out.println("No trigger selected");
		}
		}
		scheduledEvents=se;
		
		 scheduledEventsRepository.save(scheduledEvents);
		 System.out.println("going to QuartzEventSchedulerService.scheduleEvent()...");
         quartzEventSchedulerService.scheduleEvent(scheduledEvents);
		
	}

}
