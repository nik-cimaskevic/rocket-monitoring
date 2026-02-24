package com.rocket.api.infrastructure.input.http;

import com.rocket.api.usecase.ReceiveMessageUseCase;
import com.rocket.api.domain.MessageType;
import com.rocket.api.domain.RocketEvent;
import com.rocket.api.domain.UtcDateTime;
import com.rocket.api.openapi.api.MessagesApi;
import com.rocket.api.openapi.model.RocketMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MessagesController implements MessagesApi {

    private final ReceiveMessageUseCase receiveMessageUseCase;

    @Override
    public ResponseEntity<Void> postMessage(RocketMessage rocketMessage) {
        RocketEvent event = mapToEvent(rocketMessage);
        receiveMessageUseCase.execute(event);
        return ResponseEntity.ok().build();
    }

    private RocketEvent mapToEvent(RocketMessage message) {
        var metadata = message.getMetadata();

        return new RocketEvent(
                metadata.getChannel(),
                metadata.getMessageNumber(),
                UtcDateTime.of(metadata.getMessageTime().toOffsetDateTime()),
                MessageType.valueOf(metadata.getMessageType().getValue()),
                message.getMessage()
        );
    }
}