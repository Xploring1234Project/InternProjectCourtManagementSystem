package com.cognizant.JusticeServe.config;

import com.cognizant.JusticeServe.dto.request.ReportRequest;
import com.cognizant.JusticeServe.dto.response.*;
import com.cognizant.JusticeServe.entity.*;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {


    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                // This is the one you need to skip nulls during updates
                .setSkipNullEnabled(true)
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE);

        // Explicitly map the nested User ID to the Response DTO
        // 1. Map Citizen -> CitizenResponse (Handle the Nested User ID)
        modelMapper.typeMap(Citizen.class, CitizenResponse.class).addMappings(mapper -> {
            mapper.map(src -> src.getUser().getUserId(), CitizenResponse::setUserUserId);
        });

        /*// 2. Map CitizenRequest -> Citizen (Prevent ID overwriting during updates)
        modelMapper.typeMap(CitizenRequest.class, Citizen.class).addMappings(mapper -> {
            mapper.skip(Citizen::setCitizenId);
        });*/

        // Mapping for Case -> CaseResponse
        modelMapper.typeMap(Case.class, CaseResponse.class).addMappings(mapper -> {
            mapper.map(src -> src.getCitizen().getCitizenId(), CaseResponse::setCitizenId);
            mapper.map(src -> src.getCitizen().getName(), CaseResponse::setCitizenName);
            // Handle nulls for Lawyer and Judge
            mapper.map(src -> src.getLawyer().getUserId(), CaseResponse::setLawyerId);
            mapper.map(src -> src.getLawyer().getName(), CaseResponse::setLawyerName);
            mapper.map(src -> src.getJudge().getUserId(), CaseResponse::setJudgeId);
            mapper.map(src -> src.getJudge().getName(), CaseResponse::setJudgeName);
        });

        // Hearing -> HearingResponse mapping
        modelMapper.typeMap(Hearing.class, HearingResponse.class).addMappings(mapper -> {
            mapper.map(src -> src.getCaseEntity().getCaseId(), HearingResponse::setCaseId);
            mapper.map(src -> src.getCaseEntity().getTitle(), HearingResponse::setCaseTitle);
            mapper.map(src -> src.getJudge().getUserId(), HearingResponse::setJudgeId);
            mapper.map(src -> src.getJudge().getName(), HearingResponse::setJudgeName);
        });

        // Proceeding -> ProceedingResponse mapping
        modelMapper.typeMap(Proceeding.class, ProceedingResponse.class).addMappings(mapper -> {
            mapper.map(src -> src.getHearing().getHearingId(), ProceedingResponse::setHearingId);
        });

        // Judgment -> JudgmentResponse
        modelMapper.typeMap(Judgment.class, JudgmentResponse.class).addMappings(mapper -> {
            mapper.map(src -> src.getCaseEntity().getCaseId(), JudgmentResponse::setCaseId);
            mapper.map(src -> src.getCaseEntity().getTitle(), JudgmentResponse::setCaseTitle);
            mapper.map(src -> src.getJudge().getUserId(), JudgmentResponse::setJudgeId);
            mapper.map(src -> src.getJudge().getName(), JudgmentResponse::setJudgeName);
        });

        // CourtOrder -> CourtOrderResponse
        modelMapper.typeMap(CourtOrder.class, CourtOrderResponse.class).addMappings(mapper -> {
            mapper.map(src -> src.getCaseEntity().getCaseId(), CourtOrderResponse::setCaseId);
            mapper.map(src -> src.getCaseEntity().getTitle(), CourtOrderResponse::setCaseTitle);
            mapper.map(src -> src.getJudge().getUserId(), CourtOrderResponse::setJudgeId);
            mapper.map(src -> src.getJudge().getName(), CourtOrderResponse::setJudgeName);
        });

        // Audit Entity -> AuditResponse
        modelMapper.typeMap(Audit.class, AuditResponse.class).addMappings(mapper -> {
            mapper.map(src -> src.getOfficer().getUserId(), AuditResponse::setOfficerId);
            mapper.map(src -> src.getOfficer().getName(), AuditResponse::setOfficerName);
        });

        // Custom Mapping for Report -> ReportResponse
        // This ensures the nested User's ID is mapped to the Long generatedBy field
        modelMapper.typeMap(Report.class, ReportResponse.class).addMappings(mapper -> {
            mapper.map(src -> src.getGeneratedBy().getUserId(), ReportResponse::setGeneratedBy);
        });
        // Request -> Entity Mapping
        modelMapper.typeMap(ReportRequest.class, Report.class).addMappings(mapper -> {
            // We skip the generatedBy field because we will set the User object manually
            // or via a provider to ensure the JPA proxy is managed correctly.
            mapper.skip(Report::setGeneratedBy);
        });

        // Custom Mapping: AuditLog Entity -> AuditLogResponse DTO
        modelMapper.typeMap(AuditLog.class, AuditLogResponse.class).addMappings(mapper -> {
            mapper.map(src -> src.getUser().getUserId(), AuditLogResponse::setUserId);
            mapper.map(src -> src.getUser().getName(), AuditLogResponse::setUserName);
        });

        // Custom Mapping: Notification -> NotificationResponse
        modelMapper.typeMap(Notification.class, NotificationResponse.class).addMappings(mapper -> {
            mapper.map(src -> src.getUser().getUserId(), NotificationResponse::setUserId);
        });

        return modelMapper;
    }
}
