package com.ruthless.utils;

import com.ruthless.api.Validator;
import com.ruthless.web.response.ClanBroadcast;

import javax.inject.Singleton;
import java.time.Duration;
import java.time.Instant;

@Singleton
public class ClanBroadcastValidator implements Validator<ClanBroadcast> {

    // 5 days threshold for sending clan broadcast of expiration date.
    public static final long DAYS_THRESHOLD = 30;

    public boolean valid(ClanBroadcast clanBroadcast) {
        Instant now = Instant.now();
        Instant expiresAt = Instant.ofEpochSecond(clanBroadcast.getExpiresAt());

        Duration difference = Duration.between(now, expiresAt);

        if( expiresAt.isBefore(now) ) {
            return false;
        }
        long daysDifference = difference.toDays();
        return daysDifference <= DAYS_THRESHOLD;

    }
}