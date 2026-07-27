package com.ruthless.web.request.discord;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DiscordWebhookBody
{
    private String content;
}