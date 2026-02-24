package com.rocket.api.usecase;

import com.rocket.api.domain.RocketState;
import com.rocket.api.infrastructure.output.persistence.RocketStateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetRocketUseCase {

    private final RocketStateRepository stateRepository;

    public Optional<RocketState> execute(UUID channel) {
        return stateRepository.findByChannel(channel);
    }
}