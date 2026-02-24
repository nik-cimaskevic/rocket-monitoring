package com.rocket.api.infrastructure.input.http;

import com.rocket.api.usecase.GetRocketUseCase;
import com.rocket.api.usecase.ListRocketsUseCase;
import com.rocket.api.domain.RocketState;
import com.rocket.api.openapi.api.RocketsApi;
import com.rocket.api.openapi.model.Rocket;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class RocketsController implements RocketsApi {

    private final GetRocketUseCase getRocketUseCase;
    private final ListRocketsUseCase listRocketsUseCase;

    public RocketsController(
            GetRocketUseCase getRocketUseCase,
            ListRocketsUseCase listRocketsUseCase
    ) {
        this.getRocketUseCase = getRocketUseCase;
        this.listRocketsUseCase = listRocketsUseCase;
    }

    @Override
    public ResponseEntity<Rocket> getRocket(UUID rocketId) {
        return getRocketUseCase.execute(rocketId)
                .map(this::mapToResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<List<Rocket>> listRockets(String sortBy, String sortOrder) {
        List<RocketState> rockets = listRocketsUseCase.execute(sortBy, sortOrder);
        List<Rocket> response = rockets.stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    private Rocket mapToResponse(RocketState state) {
        Rocket rocket = new Rocket();
        rocket.setId(state.channel());
        rocket.setType(state.rocketType());
        rocket.setSpeed(state.speed());
        rocket.setMission(state.mission());
        rocket.setStatus(Rocket.StatusEnum.fromValue(state.status().name()));
        rocket.setExplodedReason(state.explodedReason());
        rocket.setStale(state.stale());
        return rocket;
    }
}