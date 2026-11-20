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
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.http.HttpFields;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openhab.binding.amazonechocontrol.internal.ConnectionException;

import com.google.gson.Gson;

/**
 * The {@link ConnectionAuthAgentTest} checks that the login and token requests keep the AmazonWebView agent of the app
 * version they declare
 *
 * @author Martin Littkovsky - Initial contribution
 */
@NonNullByDefault
@Timeout(10)
public class ConnectionAuthAgentTest {
    private static final String AUTH_AGENT_PREFIX = "AmazonWebView/";
    private static final String TOKEN_PATH = "/auth/token";
    private static final String EXCHANGE_TOKEN_PATH = "/ap/exchangetoken";
    private static final String REGISTER_PATH = "/auth/register";
    private static final String CAPABILITIES_PATH = "/v1/devices/@self/capabilities";
    private static final String RESPONSE_BODY = "{\"access_token\":\"token\",\"expires_in\":3600,"
            + "\"marketPlaceDomainName\":\"amazon.de\",\"retailDomain\":\"amazon.de\","
            + "\"retailUrl\":\"https://www.amazon.de\",\"websiteApiUrl\":\"https://alexa.amazon.de\","
            + "\"response\":{\"success\":{\"tokens\":{\"bearer\":{\"access_token\":\"token\","
            + "\"refresh_token\":\"refresh\"}}}}}";

    private final Map<String, Request> requestsByPath = new ConcurrentHashMap<>();
    private @NonNullByDefault({}) Connection connection;

    @BeforeEach
    @SuppressWarnings("null")
    public void setUp() {
        HttpClient httpClient = mock(HttpClient.class);
        when(httpClient.newRequest(any(URI.class)))
                .thenAnswer(invocation -> requestsByPath.computeIfAbsent(((URI) invocation.getArgument(0)).getPath(),
                        path -> answeringRequest(invocation.getArgument(0))));
        connection = new Connection(null, new Gson(), httpClient);
        connection.getLoginData().setRefreshToken("refresh");
    }

    @AfterEach
    public void tearDown() {
        connection.logout(false);
    }

    @Test
    @SuppressWarnings("null")
    public void testRenewingTheTokensSendsTheAuthUserAgent() throws ConnectionException {
        connection.renewTokens();

        assertThat(requestsByPath.keySet(), hasItems(TOKEN_PATH, EXCHANGE_TOKEN_PATH));
        verify(requestsByPath.get(TOKEN_PATH)).agent(startsWith(AUTH_AGENT_PREFIX));
        verify(requestsByPath.get(EXCHANGE_TOKEN_PATH)).agent(startsWith(AUTH_AGENT_PREFIX));
    }

    @Test
    @SuppressWarnings("null")
    public void testRegisteringAsAppSendsTheAuthUserAgent() {
        assertThat(connection.registerConnectionAsApp(null, "token"), is(true));

        verify(requestsByPath.get(REGISTER_PATH)).agent(startsWith(AUTH_AGENT_PREFIX));
        verify(requestsByPath.get(CAPABILITIES_PATH)).agent(startsWith(AUTH_AGENT_PREFIX));
    }

    @SuppressWarnings("null")
    private Request answeringRequest(URI uri) {
        Request request = mock(Request.class, RETURNS_SELF);
        when(request.getURI()).thenReturn(uri);
        doAnswer(invocation -> {
            BufferingResponseListener listener = invocation.getArgument(0);
            Response response = okJsonResponse(request);
            listener.onHeaders(response);
            listener.onContent(response, ByteBuffer.wrap(RESPONSE_BODY.getBytes(StandardCharsets.UTF_8)));
            listener.onComplete(resultOf(response));
            return null;
        }).when(request).send(any(Response.CompleteListener.class));
        return request;
    }

    @SuppressWarnings("null")
    private Response okJsonResponse(Request request) {
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
