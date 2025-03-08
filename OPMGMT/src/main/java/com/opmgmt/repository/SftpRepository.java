package com.opmgmt.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.opmgmt.entity.SftpInfo;

public interface SftpRepository extends JpaRepository<SftpInfo,Long>{

}
