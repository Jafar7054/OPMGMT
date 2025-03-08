package com.opmgmt.jobs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.opmgmt.entity.EventParameters;
import com.opmgmt.entity.ScheduledEvents;
import com.opmgmt.entity.SftpInfo;
import com.opmgmt.repository.ScheduledEventsRepository;
import com.opmgmt.services.SftpService;

@Component
public class EventJob implements Job{
	
	 @Autowired
	    private SftpService sftpService;

	    @Autowired
	    private ScheduledEventsRepository scheduledEventsRepository;

	    @Override
	    public void execute(JobExecutionContext context) throws JobExecutionException {
	        String eventId = context.getJobDetail().getJobDataMap().getString("eventId");

	        ScheduledEvents event = scheduledEventsRepository.findById(Long.parseLong(eventId))
	                .orElse(null);

	        if (event == null) {
	            System.err.println("Event not found with ID: " + eventId);
	            return;
	        }

	        try {
	            SftpInfo sftpInfo = event.getSftpInfo();
	            EventParameters eventParams = event.getEventParameter();

	            if ("get_file".equalsIgnoreCase(event.getEventType())) {
	                sftpService.downloadFiles(eventParams.getFileNameFormat(), sftpInfo, eventParams.getEncryptionType(), eventParams.getSecretKey());
	            } else if ("post_file".equalsIgnoreCase(event.getEventType())) {
	                sftpService.uploadFiles(eventParams.getFileNameFormat(), eventParams.getEncryptionType(), sftpInfo, eventParams.getSecretKey());
	            }

	            if("oneTime".equalsIgnoreCase(event.getEventType())) {
	            	event.setEventStatus("COMPLETED");
	            	scheduledEventsRepository.save(event);
	            }
	            System.out.println("Executed event: " + event.getEventName());
	            

	        } catch (Exception e) {
	            System.err.println("Error executing event: " + e.getMessage());
	        }
	    }

}
