
package com.opmgmt.controller;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.opmgmt.services.ScheduledEventsService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class OpmgmtController {
	
	@Autowired
	ScheduledEventsService scheduledEventsService;

	@RequestMapping("/")
	private String searchEvents(HttpServletRequest request, Model model)
	{
		
		return "createEvent";
	}
	
	@RequestMapping("/submitEvent")
	private String createEvent(@RequestParam Map<String, String> allParams, HttpServletRequest request, Model model)
	{
		
		try {
			//System.out.println(allParams);
			scheduledEventsService.createEvent(request);
			model.addAttribute("message", "Event scheduled successfully");
		} catch (Exception e) {
			model.addAttribute("message", "Unable to schedule the eventdue to "+e.getMessage());
			e.printStackTrace();
		}
		return "messagePage";				
	}
}
