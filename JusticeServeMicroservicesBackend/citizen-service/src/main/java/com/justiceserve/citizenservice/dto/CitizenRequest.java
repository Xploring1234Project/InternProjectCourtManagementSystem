package com.justiceserve.citizenservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CitizenRequest {
    @NotNull(message = "userId is required")
    private Long userId;

    @NotBlank(message = "Name is required")
    private String name;
    @NotNull
    private LocalDate dob;
    @NotBlank
    private String gender;
    @NotBlank
    private String address;
    @NotBlank
    private String contactInfo;
}
