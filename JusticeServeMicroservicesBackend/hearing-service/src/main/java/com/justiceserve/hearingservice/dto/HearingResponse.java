package com.justiceserve.hearingservice.dto;

import com.justiceserve.hearingservice.entity.Hearing;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class HearingResponse {
    private Long hearingId;
    private Long caseId;
    private Long judgeId;
    private String judgeName;//
    private Long citizenId;  //
    private Long lawyerId;   //
    private String caseTitle;//
    private LocalDate date;
    private LocalTime time;
    private Hearing.HearingStatus status;

}