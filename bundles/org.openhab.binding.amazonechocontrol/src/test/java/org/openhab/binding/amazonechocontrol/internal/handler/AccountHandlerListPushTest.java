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

import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.THING_TYPE_ACCOUNT;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlCommandDescriptionProvider;
import org.openhab.binding.amazonechocontrol.internal.connection.Connection;
import org.openhab.binding.amazonechocontrol.internal.connection.LoginData;
import org.openhab.binding.amazonechocontrol.internal.dto.push.PushCommandTO;
import org.openhab.core.storage.Storage;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;

import com.google.gson.Gson;

/**
 * The {@link AccountHandlerListPushTest} checks that a changed shopping or to-do list does not make the
 * {@link AccountHandler} talk to Amazon, the binding has no list channels.
 *
 * @author Martin Littkovsky - Initial contribution
 */
@NonNullByDefault
public class AccountHandlerListPushTest {
    private static final ThingUID ACCOUNT_UID = new ThingUID(THING_TYPE_ACCOUNT, "account1");

    private @NonNullByDefault({}) AccountHandler accountHandler;
    private @NonNullByDefault({}) Connection connection;

    @BeforeEach
    public void setUp() {
        Bridge bridge = mock(Bridge.class);
        when(bridge.getUID()).thenReturn(ACCOUNT_UID);

        @SuppressWarnings("unchecked")
        Storage<String> storage = (Storage<String>) mock(Storage.class);
        accountHandler = new AccountHandler(bridge, storage, new Gson(), mock(HttpClient.class),
                mock(HTTP2Client.class), mock(AmazonEchoControlCommandDescriptionProvider.class));
        accountHandler.setCallback(mock(ThingHandlerCallback.class));

        connection = mock(Connection.class);
        when(connection.isLoggedIn()).thenReturn(true);
        LoginData loginData = mock(LoginData.class);
        when(loginData.serializeLoginData()).thenReturn("");
        when(connection.getLoginData()).thenReturn(loginData);
        accountHandler.setConnection(connection);
        clearInvocations(connection);
    }

    @Test
    public void testListChangeMakesNoRequest() {
        PushCommandTO pushCommand = new PushCommandTO();
        pushCommand.command = "PUSH_LIST_ITEM_CHANGE";
        pushCommand.payload = "{\"listId\":\"L1\",\"listItemId\":\"I1\"}";

        accountHandler.onPushCommandReceived(pushCommand);

        verifyNoInteractions(connection);
    }
}
