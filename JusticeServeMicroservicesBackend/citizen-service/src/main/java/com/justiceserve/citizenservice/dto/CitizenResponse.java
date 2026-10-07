package com.justiceserve.citizenservice.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CitizenResponse {
    private Long citizenId;
    private Long userId;
    private String name;
    private LocalDate dob;
    private String gender;
    private String address;
    private String contactInfo;
    private String status;
    private LocalDateTime createdAt;
}