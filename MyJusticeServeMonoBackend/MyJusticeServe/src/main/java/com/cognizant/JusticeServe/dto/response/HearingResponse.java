package com.cognizant.JusticeServe.dto.response;

import com.cognizant.JusticeServe.entity.Hearing;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class HearingResponse {
    private Long hearingId;
    private Long caseId;
    private String caseTitle;
    private Long judgeId;
    private String judgeName;
    private LocalDate date;
    private LocalTime time;
    private Hearing.HearingStatus status;
}