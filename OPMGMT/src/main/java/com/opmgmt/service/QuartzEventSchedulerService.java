package com.opmgmt.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

import org.quartz.CronScheduleBuilder;
import org.quartz.DateBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

import com.opmgmt.entity.ScheduledEvents;
import com.opmgmt.jobs.EventJob;

@Service
public class QuartzEventSchedulerService {
	
	@Autowired
    private SchedulerFactoryBean schedulerFactoryBean;

    public void scheduleEvent(ScheduledEvents event) throws SchedulerException {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();

        JobDetail jobDetail = JobBuilder.newJob(EventJob.class)
                .withIdentity("event_" + event.getId())
                .usingJobData("eventId", String.valueOf(event.getId()))
                .build();

        System.out.println("going into create trigger...");
        Trigger trigger = createTrigger(event);

        scheduler.scheduleJob(jobDetail, trigger);
        System.out.println("Scheduled event: " + event.getEventName());
    }

    private Trigger createTrigger(ScheduledEvents event) {
        TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger()
                .withIdentity("trigger_" + event.getId())
                .startAt(getStartTime(event));
        System.out.println("going in switch...");
        switch (event.getTriggerType()) {
            case "interval":
            {
            	System.out.println("in interval case");
                return triggerBuilder
                        .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                                .withIntervalInMinutes(Integer.parseInt(event.getTriggerInterval()))
                                .repeatForever())
                        .build();
            }

            case "timeOfDay":
            {
            	System.out.println("in timeOOfday case");
                return triggerBuilder
                        .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(
                                Integer.parseInt(event.getTriggerTime().split(":")[0]),
                                Integer.parseInt(event.getTriggerTime().split(":")[1])))
                        .build();
            }
            case "dayOfWeek":
            {
            	System.out.println("in dayOfweek case");
                return triggerBuilder
                        .withSchedule(CronScheduleBuilder.weeklyOnDayAndHourAndMinute(
                                getDayOfWeek(event.getDay()),
                                Integer.parseInt(event.getTriggerTime().split(":")[0]),
                                Integer.parseInt(event.getTriggerTime().split(":")[1])))
                        .build();
            }
                
            case "dayOfWeekInterval":
            {
            	System.out.println("in switch case dayOfWeekInterval");
                return triggerBuilder
                        .withSchedule(CronScheduleBuilder.cronSchedule(
                                String.format("0 %d %d ? * %s/%d *",
                                        Integer.parseInt(event.getTriggerTime().split(":")[1]), // Minute
                                        Integer.parseInt(event.getTriggerTime().split(":")[0]), // Hour
                                        getDayOfWeek(event.getDay()), // Start Day
                                        Integer.parseInt(event.getTriggerInterval()) // Interval (every N weeks)
                                )))
                        .build();
            }

            case "dayOfMonth":
            {
            	System.out.println("in dayOfMonth case");
                return triggerBuilder
                        .withSchedule(CronScheduleBuilder.monthlyOnDayAndHourAndMinute(
                                Integer.parseInt(event.getMonthDate()),
                                Integer.parseInt(event.getTriggerTime().split(":")[0]),
                                Integer.parseInt(event.getTriggerTime().split(":")[1])))
                        .build();
            }

            case "oneTime":
            {
            	System.out.println("in oneTime case");
                return triggerBuilder.startAt(getOneTimeExecutionTime(event)).build();
            }

            default:
                throw new IllegalArgumentException("Invalid trigger type: " + event.getTriggerType());
        }
    }

    private Date getStartTime(ScheduledEvents event) {
    	 LocalTime triggerTime = LocalTime.parse(event.getTriggerTime()); // Assuming this returns LocalTime
         LocalDateTime startDateTime = LocalDate.now().atTime(triggerTime);
         
         // If the time has already passed today, schedule for tomorrow
         if (startDateTime.isBefore(LocalDateTime.now())) {
             startDateTime = startDateTime.plusDays(6);
         }

         return Date.from(startDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    private Date getOneTimeExecutionTime(ScheduledEvents event) {
        return Date.from(LocalDateTime.parse(event.getDate() + "T" + event.getTriggerTime())
                .atZone(ZoneId.systemDefault()).toInstant());
    }

    private int getDayOfWeek(String day) {
        switch (day.toLowerCase()) {
            case "monday": return DateBuilder.MONDAY;
            case "tuesday": return DateBuilder.TUESDAY;
            case "wednesday": return DateBuilder.WEDNESDAY;
            case "thursday": return DateBuilder.THURSDAY;
            case "friday": return DateBuilder.FRIDAY;
            case "saturday": return DateBuilder.SATURDAY;
            case "sunday": return DateBuilder.SUNDAY;
            default: throw new IllegalArgumentException("Invalid day: " + day);
        }
    }

}
