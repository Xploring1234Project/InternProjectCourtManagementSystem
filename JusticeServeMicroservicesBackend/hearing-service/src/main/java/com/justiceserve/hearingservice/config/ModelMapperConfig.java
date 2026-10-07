package com.justiceserve.hearingservice.config;

import com.justiceserve.hearingservice.dto.ProceedingResponse;
import com.justiceserve.hearingservice.entity.Proceeding;
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
                .setSkipNullEnabled(true)
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE);

        // Explicit Mapping for Proceeding -> ProceedingResponse
        modelMapper.typeMap(Proceeding.class, ProceedingResponse.class).addMappings(mapper -> {
            // Map the hearingId from the Hearing object inside Proceeding
            mapper.map(src -> src.getHearing().getHearingId(), ProceedingResponse::setHearingId);
        });

        return modelMapper;
    }
}
