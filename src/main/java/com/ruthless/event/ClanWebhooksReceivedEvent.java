package com.ruthless.event;

import com.ruthless.web.response.webhooks.ClanWebhooks;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
public class ClanWebhooksReceivedEvent {
    private ClanWebhooks clanWebhooks;
}
