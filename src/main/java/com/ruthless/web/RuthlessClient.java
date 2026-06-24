package com.ruthless.web;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.ruthless.RuthlessPlugin;
import com.ruthless.ui.ItemOfTheDayInfoBox;
import com.ruthless.utils.ClanBroadcastValidator;
import com.ruthless.utils.Constants;
import com.ruthless.web.response.ClanBroadcast;
import com.ruthless.web.response.ClanItemWhitelist;
import com.ruthless.web.response.ItemOfTheDay;
import com.ruthless.web.response.RuthlessSlayerTask;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.client.RuneLiteProperties;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import okhttp3.*;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RuthlessClient {
    private OkHttpClient okHttpClient;
    private Gson gson;

    @Inject
    private Client client;

    @Inject
    private ClanBroadcastValidator clanBroadcastValidator;

    private RuthlessPlugin plugin;

    private String userAgent;

    @Inject
    private ChatMessageManager chatMessageManager;

    @Inject
    public RuthlessClient(Gson gson, RuthlessPlugin plugin, Client client, OkHttpClient okHttpClient)
    {
        this.gson = gson.newBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
        this.plugin = plugin;
        this.client = client;
        this.okHttpClient = okHttpClient.newBuilder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

        String runeliteVersion = RuneLiteProperties.getVersion();

        this.userAgent = "RuthlessRunelitePlugin/1.0.0 " + "RuneLite/" + runeliteVersion;
    }

    private Request createRequest(String... pathSegments)
    {
        HttpUrl url = buildUrl(pathSegments);
        return new Request.Builder()
                .header("User-Agent", userAgent)
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
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String body = response.body().string();
                    ItemOfTheDay iotdResponse = gson.fromJson(body, ItemOfTheDay.class);
                    plugin.addIotdInfoBox(new ItemOfTheDayInfoBox(iotdResponse, plugin));
                }
            }
        });
    }

//    public void getCurrentSlayerTask() {
//        Player local = client.getLocalPlayer();
//        Request request = createRequest("member", local.getName(), "current_task");
//
//        this.okHttpClient.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                e.printStackTrace();
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                if (response.isSuccessful()) {
//                    String body = response.body().string();
//                    RuthlessSlayerTask slayerTaskResponse = gson.fromJson(body, RuthlessSlayerTask.class);
//                    plugin.addIotdInfoBox(new SlayerTaskInfoBox(slayerTaskResponse, plugin));
//                }
//            }
//        });
//    }

    public void getClanBroadcast() {
        Request request = createRequest("clans", Constants.RUTHLESS_DISCORD_GUILD_ID, "broadcast");

        this.okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() == 404) {
                    //not found, its fine we wont queue a message.
                    log.debug("No broadcast message found");
                    return;
                }
                else if (response.isSuccessful()) {
                    String body = response.body().string();
                    ClanBroadcast clanBroadcastResponse = gson.fromJson(body, ClanBroadcast.class);
                    if (clanBroadcastValidator.validate(clanBroadcastResponse)) {
                        ChatMessageBuilder cmd = new ChatMessageBuilder();
                        cmd.append("[Ruthless] ").append(clanBroadcastResponse.getMessage());


                        chatMessageManager.queue(QueuedMessage.builder()
                                .type(ChatMessageType.BROADCAST)
                                .runeLiteFormattedMessage(cmd.build()).build());
                    }

                }
            }
        });
    }

    public void getClanItemWhitelist() {
        Request request = createRequest("clans", Constants.RUTHLESS_DISCORD_GUILD_ID, "whitelist");
        this.okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String body = response.body().string();
                    List<ClanItemWhitelist> clanItemWhitelist = gson.fromJson(body, List.class);

                    plugin.setItemWhitelist(clanItemWhitelist);
                }
            }
        });
    }
}
