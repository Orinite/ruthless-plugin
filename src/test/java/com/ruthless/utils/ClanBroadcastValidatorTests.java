package com.ruthless.utils;

import com.ruthless.web.response.ClanBroadcast;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

import static org.junit.Assert.assertEquals;

public class ClanBroadcastValidatorTests {

    @Test
    public void testClanBroadcastValidator_valid() {
        Instant now = Instant.now();
        ClanBroadcastValidator validator = new ClanBroadcastValidator();

        ClanBroadcast clanBroadcast = getClanBroadcastWithExpiration(now.plus(ClanBroadcastValidator.DAYS_THRESHOLD, ChronoUnit.DAYS).getEpochSecond());
        assertEquals(true, validator.validate(clanBroadcast));
    }

    @Test
    public void testClanBroadcastValidator_invalid() {
        Instant now = Instant.now();
        ClanBroadcastValidator validator = new ClanBroadcastValidator();
        ClanBroadcast clanBroadcast = getClanBroadcastWithExpiration(now.plus(ClanBroadcastValidator.DAYS_THRESHOLD+2, ChronoUnit.DAYS).getEpochSecond());
        assertEquals(false, validator.validate(clanBroadcast));
    }

    @Test
    public void testClanBroadcastValidator_invalidbefore() {
        Instant now = Instant.now();
        ClanBroadcastValidator validator = new ClanBroadcastValidator();
        ClanBroadcast clanBroadcast = getClanBroadcastWithExpiration(now.minus(1, ChronoUnit.DAYS).getEpochSecond());
        assertEquals(false, validator.validate(clanBroadcast));
    }

    private ClanBroadcast getClanBroadcastWithExpiration(long expiration) {
        return new ClanBroadcast(1, "1", Instant.now().getEpochSecond(), expiration, "This is a test message");
    }
}
