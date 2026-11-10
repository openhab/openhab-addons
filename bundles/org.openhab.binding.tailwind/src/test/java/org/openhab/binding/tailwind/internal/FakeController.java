/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.tailwind.internal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.mockito.Answers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * An {@link HttpClient} that answers like a TailWind controller, for tests.
 *
 * @author Christopher Smith - Initial contribution
 */
@NonNullByDefault
public class FakeController {
    public static final String STATUS = """
            {"result":"OK","product":"iQ3","dev_id":"_30_ae_a4_80_18_80_","proto_ver":"0.1","door_num":2,\
            "night_mode_en":0,"fw_ver":"10.80","led_brightness":100,"router_rssi":-31,"server_monitor":true,\
            "data":{"door1":{"index":0,"status":"open","lockup":0,"disabled":0},\
            "door2":{"index":1,"status":"close","lockup":1,"disabled":0}}}""";
    public static final String OK = "{\"result\":\"OK\"}";
    public static final String TOKEN_FAIL = "{\"result\": \"Fail\",\"info\": \"token check fail\"}";

    /** An answer of the controller. */
    public record Answer(int httpStatus, String body) {
    }

    /** A request the controller received. */
    public record Received(String url, @Nullable String token, JsonObject body) {
        public String name() {
            return body.getAsJsonObject("data").get("name").getAsString();
        }

        public JsonObject value() {
            return body.getAsJsonObject("data").getAsJsonObject("value");
        }
    }

    public final HttpClient httpClient = mock(HttpClient.class);
    public final List<Received> requests = new CopyOnWriteArrayList<>();

    private volatile Function<Received, Answer> behavior = received -> new Answer(200,
            "dev_st".equals(received.name()) ? STATUS : OK);
    private volatile boolean unreachable;

    public FakeController() {
        when(httpClient.newRequest(anyString())).thenAnswer(invocation -> request(invocation.getArgument(0)));
    }

    /** Makes the controller answer every request with the given function. */
    public void answerWith(Function<Received, Answer> behavior) {
        this.behavior = behavior;
    }

    public void answerAllWith(int httpStatus, String body) {
        answerWith(received -> new Answer(httpStatus, body));
    }

    public void setUnreachable(boolean unreachable) {
        this.unreachable = unreachable;
    }

    public List<Received> requestsNamed(String name) {
        return requests.stream().filter(received -> name.equals(received.name())).toList();
    }

    private Request request(String url) throws Exception {
        Request request = mock(Request.class, Answers.RETURNS_SELF);
        String[] token = new String[1];
        String[] content = new String[1];
        doAnswer(invocation -> {
            token[0] = invocation.getArgument(1);
            return request;
        }).when(request).header(anyString(), anyString());
        doAnswer(invocation -> {
            StringBuilder text = new StringBuilder();
            for (ByteBuffer buffer : (ContentProvider) invocation.getArgument(0)) {
                text.append(StandardCharsets.UTF_8.decode(buffer.duplicate()));
            }
            content[0] = text.toString();
            return request;
        }).when(request).content(any());
        doAnswer(invocation -> {
            if (unreachable) {
                throw new TimeoutException("Total timeout 10000 ms elapsed");
            }
            Received received = new Received(url, token[0], JsonParser.parseString(content[0]).getAsJsonObject());
            requests.add(received);
            Answer answer = behavior.apply(received);
            ContentResponse response = mock(ContentResponse.class);
            when(response.getStatus()).thenReturn(answer.httpStatus());
            when(response.getContentAsString()).thenReturn(answer.body());
            return response;
        }).when(request).send();
        return request;
    }
}
