package com.ruthless.web;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.ruthless.RuthlessConfig;
import com.ruthless.RuthlessPlugin;
import com.ruthless.event.ClanBroadcastEvent;
import com.ruthless.event.ClanWhitelistReceivedEvent;
import com.ruthless.utils.Constants;
import com.ruthless.web.interceptor.RuthlessApiInterceptor;
import com.ruthless.web.request.BossKillSubmission;
import com.ruthless.web.request.ClanAcknowledgement;
import com.ruthless.web.request.DonationSubmission;
import com.ruthless.web.request.LootDropSubmission;
import com.ruthless.web.response.ClanBroadcast;
import com.ruthless.web.response.ClanWhitelist;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.client.RuneLiteProperties;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.EventBus;
import okhttp3.*;

import javax.inject.Inject;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RuthlessClient {
    private OkHttpClient okHttpClient;
    private Gson gson;


    private @Inject Client client;
    private @Inject ClientThread clientThread;
    private EventBus eventBus;
    private RuthlessPlugin plugin;
    private String userAgent;
    private RuthlessConfig config;



    @Inject
    public RuthlessClient(Gson gson, RuthlessPlugin plugin, Client client, OkHttpClient okHttpClient, RuthlessConfig config, EventBus eventBus)
    {
        this.gson = gson.newBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
        this.plugin = plugin;
        this.client = client;
        this.config = config;
        this.eventBus = eventBus;
        this.okHttpClient = okHttpClient.newBuilder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(new RuthlessApiInterceptor(eventBus))
            .build();

        String runeliteVersion = RuneLiteProperties.getVersion();

        this.userAgent = "RuthlessRunelitePlugin/1.0.0 " + "RuneLite/" + runeliteVersion;
    }

    private Request createRequest(String... pathSegments)
    {
        HttpUrl url = buildUrl(pathSegments);
        return new Request.Builder()
                .header("User-Agent", userAgent)
                .header("x-api-key", config.memberAPIKey())
                .url(url)
                .build();
    }

    private Request createPostRequest(Object body, String... pathSegments)
    {
        String jsonBody = gson.toJson(body);
        log.debug("Ruthless POST Body: {}", jsonBody);
        HttpUrl url = buildUrl(pathSegments);
        return new Request.Builder()
                .header("User-Agent", userAgent)
                .header("x-api-key", config.memberAPIKey())
                .post(RequestBody.create(MediaType.get("application/json"), jsonBody))
                .url(url)
                .build();
    }

    private HttpUrl buildUrl(String[] pathSegments)
    {
        HttpUrl.Builder urlBuilder = new HttpUrl.Builder()
                .scheme(config.useHttps() ? "https" : "http")
                .host(config.baseApiHostname())
                .port(config.baseApiPort())
                .addPathSegment("api")
                .addPathSegment("v3");

        for (String pathSegment : pathSegments)
        {
            if (pathSegment.startsWith("?"))
            {
                // A query param
                String[] kv = pathSegment.substring(1).split("=");
                urlBuilder.addQueryParameter(kv[0], kv[1]);
            }
            else
            {
                urlBuilder.addPathSegment(pathSegment);
            }
        }


        return urlBuilder.build();
    }

    private void postEvent(Object event) {
        clientThread.invokeLater(() -> eventBus.post(event));
    }

    public void getClanBroadcast() {
        //exit early, dont fetch.
        if (!config.showClanBroadcasts()) return;

        Request request = createRequest("clans", String.valueOf(config.clanId()), "broadcasts", "latest");

        this.okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                log.error("Error fetching Clan broadcast", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() == 404) {
                    //not found, its fine we wont queue a message.
                    log.debug("No broadcast message found");
                    return;
                }
                if (response.isSuccessful()) {
                    String body = response.body().string();
                    ClanBroadcast clanBroadcast = gson.fromJson(body, ClanBroadcast.class);
                    if (config.broadcastLimit() != Constants.BROADCAST_LIMIT_DEFAULT) {
                        long broadcastCounts = clanBroadcast.getAcknowledgements().getItems().stream().filter(ack -> ack.getClanBroadcastId() == clanBroadcast.getId()).count();
                        if (broadcastCounts >= config.broadcastLimit()) {
                            //dont send event.
                            return;
                        }
                    }
                    clientThread.invokeLater(() -> {
                        Player player = client.getLocalPlayer();
                        if (player == null) {
                            return false;
                        }
                        submitBroadcastAcknowledgement(ClanAcknowledgement.builder().username(client.getLocalPlayer().getName()).build(), clanBroadcast.getId());
                        return true;
                    });
                    postEvent(new ClanBroadcastEvent(clanBroadcast));
                }
                response.close();
            }
        });
    }

    public void getClanWhitelist() {
        Request request = createRequest("clans", String.valueOf(config.clanId()), "whitelists");

        this.okHttpClient.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                log.error("Error fetching whitelist for clan", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    log.debug("Whitelist fetched successfully");
                    String body = response.body().string();
                    ClanWhitelist clanWhitelist = gson.fromJson(body, ClanWhitelist.class);
                    postEvent(new ClanWhitelistReceivedEvent(clanWhitelist));
                }
                response.close();
            }
        });
    }

    public void submitBossTimeRequest(BossKillSubmission ruthlessMemberBossTimeRequest) {
        Request request = createPostRequest(ruthlessMemberBossTimeRequest, "api-kill-submissions");

        this.okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                log.error("Error submitting bosstime request", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                log.debug("Sent bosstime request");
                if (response.code() == 201) {
                    log.debug("Boss time recorded successfully.");
                } else {
                    log.debug("Error recording boss time");
                }
                response.close();
            }
        });
    }



    public void submitLoot(LootDropSubmission ruthlessMemberLootRequest) {

        Request request = createPostRequest(ruthlessMemberLootRequest, "api-drop-submissions");

        this.okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                log.error("Error submitting item request", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                log.debug("Sent item request");
                if (response.isSuccessful()) {
                    log.debug("Clan item recorded successfully.");
                } else {
                    log.debug("Error recording item. Response: {}. error: {}", response.code(), response.body().string());
                }
                response.close();
            }
        });
    }

    public void submitDonation(DonationSubmission donationSubmission) {
        Request request = createPostRequest(donationSubmission, "clans", String.valueOf(config.clanId()), "donations");

        this.okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                log.error("Error submitting item request", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                log.debug("Sent item request");
                if (response.isSuccessful()) {
                    log.debug("Clan Donation recorded successfully.");
                } else {
                    log.debug("Error recording Donation. Response: {}. error: {}", response.code(), response.body().string());
                }
                response.close();
            }
        });
    }

    public void submitBroadcastAcknowledgement(ClanAcknowledgement clanAcknowledgement, int broadcastId) {
        Request request = createPostRequest(clanAcknowledgement, "clans", String.valueOf(config.clanId()), "broadcasts", String.valueOf(broadcastId), "acknowledgements");

        this.okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                log.error("Error submitting item request", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                log.debug("Sent acknowledgement request");
                if (response.isSuccessful()) {
                    log.debug("Clan Broadcast Acknowledgement recorded successfully.");
                } else {
                    log.debug("Error recording Clan Broadcast Acknowledgement. Response: {}. error: {}", response.code(), response.body().string());
                }
                response.close();
            }
        });
    }
}
