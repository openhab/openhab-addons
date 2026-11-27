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
package org.openhab.binding.amazonechocontrol.internal.handler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.THING_TYPE_ACCOUNT;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlCommandDescriptionProvider;
import org.openhab.binding.amazonechocontrol.internal.connection.Connection;
import org.openhab.binding.amazonechocontrol.internal.connection.LoginData;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.storage.Storage;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;

import com.google.gson.Gson;

/**
 * The {@link AccountHandlerUserAgentTest} checks that the user agent configured on the account reaches the connection
 *
 * @author Martin Littkovsky - Initial contribution
 */
@NonNullByDefault
public class AccountHandlerUserAgentTest {
    private static final String CONFIGURED_USER_AGENT = "AmazonWebView/Amazon Alexa/2.2.556530.0/iOS/17.0/iPhone";

    private @NonNullByDefault({}) AccountHandler accountHandler;

    @AfterEach
    public void tearDown() {
        accountHandler.dispose();
    }

    @Test
    @SuppressWarnings({ "null", "unchecked" })
    public void testTheConfiguredUserAgentReachesTheConnection() {
        Bridge bridge = mock(Bridge.class);
        when(bridge.getUID()).thenReturn(new ThingUID(THING_TYPE_ACCOUNT, "account1"));
        when(bridge.getConfiguration()).thenReturn(new Configuration(Map.of("userAgent", CONFIGURED_USER_AGENT)));
        Connection connection = mock(Connection.class);
        when(connection.getLoginData()).thenReturn(mock(LoginData.class));
        accountHandler = new AccountHandler(bridge, mock(Storage.class), new Gson(), mock(HttpClient.class),
                mock(HTTP2Client.class), mock(AmazonEchoControlCommandDescriptionProvider.class));
        accountHandler.setCallback(mock(ThingHandlerCallback.class));
        accountHandler.setConnection(connection);

        accountHandler.initialize();

        verify(connection).setUserAgent(CONFIGURED_USER_AGENT);
    }
}
