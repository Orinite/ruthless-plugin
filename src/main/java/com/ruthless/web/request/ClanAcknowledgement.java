package com.ruthless.web.request;

import lombok.Builder;

import java.util.Map;

@Builder
public class ClanAcknowledgement {
    String username;
    Map<String, Object> metadata;
}
