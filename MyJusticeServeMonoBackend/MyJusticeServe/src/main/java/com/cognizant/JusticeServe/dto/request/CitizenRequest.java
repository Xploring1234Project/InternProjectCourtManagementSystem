package com.cognizant.JusticeServe.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CitizenRequest {
    @NotNull
    private Long userId;
    @NotBlank
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
