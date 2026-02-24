package com.rocket.api.usecase;

import com.rocket.api.domain.RocketEvent;
import com.rocket.api.domain.RocketSnapshot;
import com.rocket.api.domain.RocketState;
import com.rocket.api.domain.UtcDateTime;
import com.rocket.api.infrastructure.output.persistence.RocketEventRepository;
import com.rocket.api.infrastructure.output.persistence.RocketSnapshotRepository;
import com.rocket.api.infrastructure.output.persistence.RocketStateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReceiveMessageUseCase {


    private final RocketEventRepository eventRepository;
    private final RocketStateRepository stateRepository;
    private final RocketSnapshotRepository snapshotRepository;
    private final Clock clock;

    @Transactional
    public void execute(RocketEvent event) {

        boolean saved = eventRepository.save(event);

        if (!saved) {
            log.debug("Duplicate event ignored: channel={}, messageNumber={}", event.channel(), event.messageNumber());
            return;
        }

        log.info("Saved event: channel={}, messageNumber={}, type={}", event.channel(), event.messageNumber(), event.messageType());
        recomputeState(event.channel());
    }

    private void recomputeState(UUID channel) {
        UtcDateTime now = UtcDateTime.now(clock);

        RocketState state = snapshotRepository.findLatestByChannel(channel)
                .map(RocketSnapshot::toState)
                .orElse(RocketState.empty(channel, now));

        List<RocketEvent> events = eventRepository.findByChannelAfterMessageNumber(channel, state.lastProcessedMsgNumber());

        log.debug("Replaying {} events for channel={} from messageNumber={}", events.size(), channel, state.lastProcessedMsgNumber());

        state = state.applyAll(events, now);

        stateRepository.save(state);

        if (state.shouldSnapshot()) {
            snapshotRepository.save(RocketSnapshot.fromState(state, clock));
            log.debug("Saved snapshot for channel={} at messageNumber={}", channel, state.lastProcessedMsgNumber());
        }

        log.info("Updated state for channel={}: type={}, speed={}, status={}, lastMsg={}, stale={}",
                channel, state.rocketType(), state.speed(), state.status(), state.lastProcessedMsgNumber(), state.stale());
    }
}