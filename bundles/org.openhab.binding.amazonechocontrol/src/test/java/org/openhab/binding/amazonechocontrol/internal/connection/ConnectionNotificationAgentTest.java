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
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Answers.RETURNS_SELF;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openhab.binding.amazonechocontrol.internal.ConnectionException;
import org.openhab.binding.amazonechocontrol.internal.dto.DeviceTO;
import org.openhab.binding.amazonechocontrol.internal.dto.NotificationTO;

import com.google.gson.Gson;

/**
 * The {@link ConnectionNotificationAgentTest} checks that every request to Amazon's notifications API goes out with
 * the browser user agent and the expected URL
 *
 * @author Martin Littkovsky - Initial contribution
 */
@NonNullByDefault
@Timeout(10)
public class ConnectionNotificationAgentTest {
    private static final String BROWSER_AGENT_PREFIX = "Mozilla/5.0";
    private static final String NOTIFICATIONS_URL = "https://alexa.amazon.com/api/notifications";
    private static final String RESPONSE_BODY = "{\"id\":\"id\",\"notifications\":[]}";

    private final HttpClient httpClient = mock(HttpClient.class);
    private final Request request = mock(Request.class, RETURNS_SELF);
    private @NonNullByDefault({}) Connection connection;

    @BeforeEach
    public void setUp() {
        when(request.getURI()).thenReturn(URI.create(NOTIFICATIONS_URL));
        doAnswer(invocation -> {
            BufferingResponseListener listener = invocation.getArgument(0);
            Response response = okJsonResponse();
            listener.onHeaders(response);
            listener.onContent(response, ByteBuffer.wrap(RESPONSE_BODY.getBytes(StandardCharsets.UTF_8)));
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
    public void testPollingTheNotificationsSendsTheBrowserUserAgent() throws ConnectionException {
        assertThat(connection.getNotifications(), is(empty()));

        verify(httpClient).newRequest(URI.create(NOTIFICATIONS_URL));
        verify(request).method(HttpMethod.GET);
        verify(request).agent(startsWith(BROWSER_AGENT_PREFIX));
    }

    @Test
    public void testReadingOneNotificationSendsTheBrowserUserAgent() throws ConnectionException {
        assertThat(connection.getNotification("id").id, is("id"));

        verify(httpClient).newRequest(URI.create(NOTIFICATIONS_URL + "/id"));
        verify(request).method(HttpMethod.GET);
        verify(request).agent(startsWith(BROWSER_AGENT_PREFIX));
    }

    @Test
    public void testCreatingAReminderSendsTheBrowserUserAgent() throws ConnectionException {
        DeviceTO device = new DeviceTO();
        device.serialNumber = "SERIAL";
        device.deviceType = "TYPE";

        NotificationTO created = connection.createNotification(device, "Reminder", "test", null);

        assertThat(created, is(notNullValue()));
        verify(httpClient).newRequest(URI.create(NOTIFICATIONS_URL + "/createReminder"));
        verify(request).method(HttpMethod.PUT);
        verify(request).agent(startsWith(BROWSER_AGENT_PREFIX));
    }

    @Test
    public void testDeletingANotificationSendsTheBrowserUserAgent() {
        connection.deleteNotification("id");

        verify(httpClient).newRequest(URI.create(NOTIFICATIONS_URL + "/id"));
        verify(request).method(HttpMethod.DELETE);
        verify(request).agent(startsWith(BROWSER_AGENT_PREFIX));
    }

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

    private Result resultOf(Response response) {
        Result result = mock(Result.class);
        when(result.getResponse()).thenReturn(response);
        return result;
    }
}
