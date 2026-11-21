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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Answers.RETURNS_SELF;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.HttpCookie;
import java.net.URI;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.google.gson.Gson;

/**
 * Verifies that requests read the cookie store that the connection writes to, also after logging out and
 * re-registering as a new device.
 *
 * @author Jens Erat - Initial contribution
 */
@NonNullByDefault
public class ConnectionCookieStoreTest {
    private final HttpClient httpClient = mock(HttpClient.class);
    private final Request request = mock(Request.class, RETURNS_SELF);
    private final Connection connection = new Connection(null, new Gson(), httpClient);

    public ConnectionCookieStoreTest() {
        when(httpClient.newRequest(any(URI.class))).thenReturn(request);
    }

    @AfterEach
    public void logout() {
        connection.logout(false);
    }

    @Test
    public void cookiesAddedAfterLogoutAndReRegisterAreSent() {
        connection.logout(true);
        connection.getLoginData().deserialize(sessionWithCookie("frc", "fresh"));

        connection.getRequestBuilder().get("https://www.amazon.com/ap/signin").send();

        ArgumentCaptor<HttpCookie> cookies = ArgumentCaptor.forClass(HttpCookie.class);
        verify(request, atLeastOnce()).cookie(cookies.capture());
        List<HttpCookie> sentCookies = cookies.getAllValues();
        assertTrue(sentCookies.stream().anyMatch(cookie -> "frc".equals(cookie.getName())),
                "frc cookie was not sent: " + sentCookies);
    }

    private static String sessionWithCookie(String name, String value) {
        StringBuilder builder = new StringBuilder();
        builder.append("8\n");
        builder.append("frc\n");
        builder.append("0123456789ABCDEF0123456789ABCDEF\n");
        builder.append("stored-device-id\n");
        builder.append("refresh-token\n");
        builder.append("amazon.com\n");
        builder.append("https://www.amazon.com\n");
        builder.append("https://alexa.amazon.com\n");
        builder.append("device-name\n");
        builder.append("customer-id\n");
        builder.append("1234567890\n");
        builder.append("1\n");
        appendValue(builder, name);
        appendValue(builder, value);
        builder.append("0\n");
        builder.append("0\n");
        appendValue(builder, "www.amazon.com");
        appendValue(builder, "-1");
        appendValue(builder, "/");
        builder.append("0\n");
        appendValue(builder, "0");
        appendValue(builder, "false");
        appendValue(builder, "false");
        return builder.toString();
    }

    private static void appendValue(StringBuilder builder, String value) {
        builder.append("1\n").append(value).append('\n');
    }
}
