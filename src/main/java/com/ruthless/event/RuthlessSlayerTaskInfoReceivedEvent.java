package com.ruthless.event;

import com.ruthless.web.response.RuthlessSlayerTaskInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class RuthlessSlayerTaskInfoReceivedEvent {

    @Getter
    private RuthlessSlayerTaskInfo ruthlessSlayerTask;
}
