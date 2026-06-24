package com.ruthless.web.interceptor;

import com.ruthless.event.MemberAPIKeyInvalidEvent;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.eventbus.EventBus;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;

@Slf4j
public class RuthlessApiInterceptor implements Interceptor {

    private EventBus eventBus;

    public RuthlessApiInterceptor(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        Response response = chain.proceed(request);
        if (response.code() == 401) {
            log.error("member API Key not set valid.");
            eventBus.post(new MemberAPIKeyInvalidEvent());
        }
        return response;
    }
}
