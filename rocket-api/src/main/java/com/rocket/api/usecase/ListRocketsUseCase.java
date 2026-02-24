package com.rocket.api.usecase;

import com.rocket.api.infrastructure.output.persistence.RocketStateRepository;
import com.rocket.api.domain.RocketState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListRocketsUseCase {

    private final RocketStateRepository stateRepository;

    public List<RocketState> execute(String sortBy, String sortOrder) {
        return stateRepository.findAll(
                sortBy != null ? sortBy : "type",
                sortOrder != null ? sortOrder : "asc"
        );
    }
}