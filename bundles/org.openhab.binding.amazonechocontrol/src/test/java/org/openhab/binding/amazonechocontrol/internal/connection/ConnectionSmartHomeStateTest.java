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
package org.openhab.binding.amazonechocontrol.internal.connection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Answers.RETURNS_SELF;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.BufferingResponseListener;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.Request;
import org.eclipse.jetty.client.Response;
import org.eclipse.jetty.client.Result;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.io.Content;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.amazonechocontrol.internal.ConnectionException;
import org.openhab.binding.amazonechocontrol.internal.dto.smarthome.JsonSmartHomeDevice;
import org.openhab.binding.amazonechocontrol.internal.dto.smarthome.SmartHomeBaseDevice;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

/**
 * The {@link ConnectionSmartHomeStateTest} contains tests for the smart home state request and the answer to it
 *
 * @author Martin Littkovsky - Initial contribution
 */
@NonNullByDefault
@Timeout(10)
public class ConnectionSmartHomeStateTest {
    private static final String APPLIANCE_ID = "applianceOfTheDevice";
    private static final String MERGED_ID = "mergedApplianceOfTheDevice";
    private static final URI STATE_URI = URI.create("https://alexa.amazon.com/api/phoenix/state");

    @SuppressWarnings("null")
    private final HttpClient httpClient = mock(HttpClient.class);
    @SuppressWarnings("null")
    private final Request request = mock(Request.class, RETURNS_SELF);
    private @NonNullByDefault({}) Connection connection;
    private String responseBody = "{\"deviceStates\":[]}";

    @BeforeEach
    @SuppressWarnings("null")
    public void setUp() {
        when(request.getURI()).thenReturn(STATE_URI);
        doAnswer(invocation -> {
            BufferingResponseListener listener = invocation.getArgument(0);
            Response response = okJsonResponse();
            listener.onHeaders(response);
            // Since Jetty 12 only the Content.Chunk overload buffers content; the ByteBuffer one merely
            // records that the application wants accumulation to happen.
            listener.onContent(response,
                    Content.Chunk.from(ByteBuffer.wrap(responseBody.getBytes(StandardCharsets.UTF_8)), true), () -> {
                    });
            // Without this an unbuffered body is indistinguishable from a response carrying no device
            // states, which would let those tests pass while nothing reaches the parser.
            assertThat(listener.getContentAsString(), is(responseBody));
            listener.onComplete(resultOf(response));
            return null;
        }).when(request).send(any(Response.CompleteListener.class));
        when(httpClient.newRequest(any(URI.class))).thenReturn(request);
        connection = new Connection(null, new Gson(), httpClient);
    }

    @AfterEach
    public void tearDown() {
        connection.logout(false);
    }

    @Test
    public void testADeviceWithAnEmptyMergeListIsAskedForItsOwnState() throws Exception {
        connection.getSmartHomeDeviceStatesJson(Set.of(device(List.of())));

        assertThat(sentContent(), containsString(APPLIANCE_ID));
    }

    @Test
    public void testADeviceWithoutAMergeListIsAskedForItsOwnState() throws Exception {
        connection.getSmartHomeDeviceStatesJson(Set.of(device(null)));

        assertThat(sentContent(), containsString(APPLIANCE_ID));
    }

    @Test
    public void testAMergedDeviceIsAskedForTheMergedIdsInstead() throws Exception {
        connection.getSmartHomeDeviceStatesJson(Set.of(device(List.of(MERGED_ID))));

        String content = sentContent();
        assertThat(content, containsString(MERGED_ID));
        assertThat(content, not(containsString("\"" + APPLIANCE_ID + "\"")));
    }

    @Test
    public void testAMergedStateIsReturnedUnderTheApplianceIdOfItsDevice() throws ConnectionException {
        responseBody = "{\"deviceStates\":[{\"entity\":{\"entityId\":\"" + MERGED_ID
                + "\"},\"capabilityStates\":[\"{\\\"name\\\":\\\"powerState\\\"}\"]}]}";

        Map<String, JsonArray> states = connection.getSmartHomeDeviceStatesJson(Set.of(device(List.of(MERGED_ID))));

        assertThat(states.keySet(), contains(APPLIANCE_ID));
    }

    @Test
    public void testAnAnswerWithoutDeviceStatesIsEmptyInsteadOfAFailure() throws ConnectionException {
        responseBody = "{\"errors\":[{\"code\":\"ENDPOINT_UNREACHABLE\"}]}";

        assertThat(connection.getSmartHomeDeviceStatesJson(Set.of(device(null))), is(anEmptyMap()));
    }

    private String sentContent() throws IOException {
        ArgumentCaptor<Request.Content> content = ArgumentCaptor.captor();
        verify(request).body(content.capture());
        return Content.Source.asString(content.getValue(), StandardCharsets.UTF_8);
    }

    private static SmartHomeBaseDevice device(@Nullable List<String> mergedIds) {
        JsonSmartHomeDevice device = new JsonSmartHomeDevice();
        device.applianceId = APPLIANCE_ID;
        device.mergedApplianceIds = mergedIds;
        return device;
    }

    @SuppressWarnings("null")
    private Response okJsonResponse() {
        HttpFields headers = HttpFields.build().add("Content-Type", "application/json");
        Response response = mock(Response.class);
        when(response.getRequest()).thenReturn(request);
        when(response.getStatus()).thenReturn(200);
        when(response.getHeaders()).thenReturn(headers);
        when(response.getReason()).thenReturn("OK");
        return response;
    }

    @SuppressWarnings("null")
    private Result resultOf(Response response) {
        Result result = mock(Result.class);
        when(result.getResponse()).thenReturn(response);
        return result;
    }
}
