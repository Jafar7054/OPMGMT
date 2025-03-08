package com.opmgmt.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.opmgmt.entity.ScheduledEvents;

public interface ScheduledEventsRepository extends JpaRepository<ScheduledEvents, Long>{

}
