package com.cognizant.JusticeServe.dto.response;

import com.cognizant.JusticeServe.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
public class CitizenResponse {
    private Long citizenId;
    private Long userUserId;
    private String name;
    private LocalDate dob;
    private String gender;
    private String address;
    private String contactInfo;
    private User.Status status;
}
