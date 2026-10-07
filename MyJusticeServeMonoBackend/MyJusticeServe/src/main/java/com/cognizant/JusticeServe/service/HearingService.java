package com.cognizant.JusticeServe.service;

import com.cognizant.JusticeServe.dto.request.HearingRequest;
import com.cognizant.JusticeServe.dto.request.ProceedingRequest;
import com.cognizant.JusticeServe.dto.response.HearingResponse;
import com.cognizant.JusticeServe.dto.response.ProceedingResponse;
import com.cognizant.JusticeServe.entity.Hearing;

import java.util.List;

public interface HearingService {
    HearingResponse scheduleNewHearing(HearingRequest request);
    HearingResponse getHearingById(Long id);
    List<HearingResponse> getAllHearings();
    List<HearingResponse> getHearingsByCaseId(Long caseId);
    List<HearingResponse> getHearingsByJudgeId(Long judgeId);
    HearingResponse updateHearingStatus(Long id, Hearing.HearingStatus status);
    ProceedingResponse recordHearingProceeding(ProceedingRequest request);
    List<ProceedingResponse> fetchProceedingsByHearingId(Long hearingId);
}
