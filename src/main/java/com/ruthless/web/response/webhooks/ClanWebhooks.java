package com.ruthless.web.response.webhooks;

import lombok.Data;

import java.util.List;

@Data
public class ClanWebhooks {
    public static final String LOOTS_WEBHOOK_TYPE_NAME = "loots";
    public static final String DEATHS_WEBHHOOK_TYPE_NAME = "deaths";
    List<ClanWebhook> items;
}
