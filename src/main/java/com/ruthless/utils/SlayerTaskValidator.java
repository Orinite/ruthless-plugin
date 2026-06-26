package com.ruthless.utils;

import com.ruthless.api.Validator;
import com.ruthless.web.response.RuthlessSlayerTaskInfo;

import javax.inject.Singleton;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

@Singleton
public class SlayerTaskValidator implements Validator<RuthlessSlayerTaskInfo> {
    @Override
    public boolean valid(RuthlessSlayerTaskInfo ruthlessSlayerTaskInfo) {
        if (Objects.isNull(ruthlessSlayerTaskInfo.getCurrentTask())) {
            return false;
        }
        Duration difference = Duration.between(Instant.now(), Instant.ofEpochSecond(ruthlessSlayerTaskInfo.getCurrentTask().getExpiresAt()));
        return !difference.isNegative();
    }
}
