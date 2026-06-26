package com.ruthless.web;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.ruthless.RuthlessConfig;
import com.ruthless.RuthlessPlugin;
import com.ruthless.event.ClanBroadcastEvent;
import com.ruthless.event.ItemOfTheDayReceivedEvent;
import com.ruthless.event.RuthlessSlayerTaskInfoReceivedEvent;
import com.ruthless.utils.Constants;
import com.ruthless.web.interceptor.RuthlessApiInterceptor;
import com.ruthless.web.request.RuthlessMemberBossTimeRequest;
import com.ruthless.web.response.ClanBroadcast;
import com.ruthless.web.response.ItemOfTheDay;
import com.ruthless.web.response.RuthlessSlayerTaskInfo;
import lombok.NonNull;import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
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
        HttpUrl url = buildUrl(pathSegments);
        return new Request.Builder()
                .header("User-Agent", userAgent)
                .header("x-api-key", config.memberAPIKey())
                .post(RequestBody.create(MediaType.get("application/json"), gson.toJson(body)))
                .url(url)
                .build();
    }

    private HttpUrl buildUrl(String[] pathSegments)
    {
        HttpUrl.Builder urlBuilder = new HttpUrl.Builder()
                .scheme("https")
                .host("ruthless-osrs.com")
                .addPathSegment("api")
                .addPathSegment("v2");

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

    public void getItemOfTheDay() {
        Request request = createRequest("iotd", "current");

        this.okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                log.error("Error fetching Item of the Day", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String body = response.body().string();
                    ItemOfTheDay iotdResponse = gson.fromJson(body, ItemOfTheDay.class);
                    postEvent(new ItemOfTheDayReceivedEvent(iotdResponse));
                }
                response.close();
            }
        });
    }

    public void getCurrentSlayerTask(@NonNull String username) {
        Request request = createRequest("member", username, "current_task");

        this.okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                log.error("Error fetching Slayer task for member {}", username, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String body = response.body().string();
                    RuthlessSlayerTaskInfo slayerTaskResponse = gson.fromJson(body, RuthlessSlayerTaskInfo.class);
                    postEvent(new RuthlessSlayerTaskInfoReceivedEvent(slayerTaskResponse));
                }
                response.close();
            }
        });
    }

    private void postEvent(Object event) {
        clientThread.invokeLater(() -> eventBus.post(event));
    }

    public void getClanBroadcast() {
        Request request = createRequest("clans", Constants.RUTHLESS_DISCORD_GUILD_ID, "broadcast");

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
                    postEvent(new ClanBroadcastEvent(clanBroadcast));
                }
                response.close();
            }
        });
    }

    public void submitBossTimeRequest(RuthlessMemberBossTimeRequest ruthlessMemberBossTimeRequest) {
        Request request = createPostRequest(ruthlessMemberBossTimeRequest, "clans", "submit_time");

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
}
