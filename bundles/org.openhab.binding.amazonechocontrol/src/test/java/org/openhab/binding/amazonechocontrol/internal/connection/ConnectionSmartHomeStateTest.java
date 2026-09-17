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
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Answers.RETURNS_SELF;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.http.HttpFields;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
            listener.onContent(response, ByteBuffer.wrap(responseBody.getBytes(StandardCharsets.UTF_8)));
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
    public void testADeviceWithAnEmptyMergeListIsAskedForItsOwnState() throws ConnectionException {
        connection.getSmartHomeDeviceStatesJson(Set.of(device(List.of())));

        assertThat(sentContent(), containsString(APPLIANCE_ID));
    }

    @Test
    public void testADeviceWithoutAMergeListIsAskedForItsOwnState() throws ConnectionException {
        connection.getSmartHomeDeviceStatesJson(Set.of(device(null)));

        assertThat(sentContent(), containsString(APPLIANCE_ID));
    }

    @Test
    public void testAMergedDeviceIsAskedForTheMergedIdsInstead() throws ConnectionException {
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

    @SuppressWarnings("null")
    private String sentContent() {
        ArgumentCaptor<ContentProvider> content = ArgumentCaptor.forClass(ContentProvider.class);
        verify(request).content(content.capture());
        StringBuilder sent = new StringBuilder();
        content.getValue().forEach(buffer -> sent.append(StandardCharsets.UTF_8.decode(buffer)));
        return sent.toString();
    }

    private static SmartHomeBaseDevice device(@Nullable List<String> mergedIds) {
        JsonSmartHomeDevice device = new JsonSmartHomeDevice();
        device.applianceId = APPLIANCE_ID;
        device.mergedApplianceIds = mergedIds;
        return device;
    }

    @SuppressWarnings("null")
    private Response okJsonResponse() {
        HttpFields headers = new HttpFields();
        headers.add("Content-Type", "application/json");
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
