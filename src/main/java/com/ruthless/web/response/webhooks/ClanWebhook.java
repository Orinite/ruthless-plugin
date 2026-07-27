package com.ruthless.web.response.webhooks;

import lombok.Data;

@Data
public class ClanWebhook {
    private String webhookTypeName;
    private String name;
    private String url;
}
