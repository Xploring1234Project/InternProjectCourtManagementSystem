package com.justiceserve.hearingservice.service;

import com.justiceserve.hearingservice.dto.HearingRequest;
import com.justiceserve.hearingservice.dto.HearingResponse;
import com.justiceserve.hearingservice.dto.ProceedingRequest;
import com.justiceserve.hearingservice.dto.ProceedingResponse;
import com.justiceserve.hearingservice.entity.Hearing;

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