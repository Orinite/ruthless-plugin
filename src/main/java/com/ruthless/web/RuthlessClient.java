package com.ruthless.web;

import com.google.gson.Gson;
import com.ruthless.RuthlessPlugin;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.RuneLiteProperties;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RuthlessClient {
    private OkHttpClient okHttpClient;
    private Gson gson;

    @Inject
    private Client client;

    private RuthlessPlugin plugin;

    private String userAgent;

    @Inject
    public RuthlessClient(Gson gson, RuthlessPlugin plugin, Client client, OkHttpClient okHttpClient)
    {
        this.gson = gson.newBuilder().create();
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
                .header("User-Agent", "Runelite")
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
}
