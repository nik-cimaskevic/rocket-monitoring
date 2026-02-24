package com.rocket.api.common.uuid;

import com.github.f4b6a3.uuid.UuidCreator;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UuidProvider {

    /*
     * Generates a UUIDv7 (time-ordered) identifier.
     * UUIDv7 embeds a Unix timestamp, making IDs naturally sortable by creation time.
     * This improves database index performance compared to random UUIDv4.
     */
    public UUID nextUuidV7() {
        return UuidCreator.getTimeOrderedEpoch();
    }
}