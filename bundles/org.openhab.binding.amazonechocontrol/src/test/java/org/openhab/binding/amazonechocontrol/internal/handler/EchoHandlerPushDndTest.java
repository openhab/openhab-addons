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
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.CHANNEL_DO_NOT_DISTURB;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.DEVICE_PROPERTY_SERIAL_NUMBER;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.THING_TYPE_ACCOUNT;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.THING_TYPE_ECHO;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.junit.jupiter.api.Test;
import org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlCommandDescriptionProvider;
import org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlStateDescriptionProvider;
import org.openhab.binding.amazonechocontrol.internal.dto.push.PushCommandTO;
import org.openhab.binding.amazonechocontrol.internal.util.NonNullListTypeAdapterFactory;
import org.openhab.binding.amazonechocontrol.internal.util.SerializeNullTypeAdapterFactory;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.storage.Storage;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The {@link EchoHandlerPushDndTest} contains tests for handling PUSH_DND_STATE_CHANGE messages
 *
 * @author Martin Littkovsky - Initial contribution
 */
@NonNullByDefault
public class EchoHandlerPushDndTest {
    // reconstructed from the log in openhab/openhab-addons#21339, ids replaced
    private static final String ECHO_SERIAL = "G000AA0000000000";
    private static final String PAYLOAD = "{\"dopplerId\":{\"deviceSerialNumber\":\"" + ECHO_SERIAL + "\","
            + "\"deviceType\":\"A3EVMLQTU6WL1W\"},\"enabled\":%s,\"destinationUserId\":\"A1PY8QQU9P0FJP\"}";

    private final Gson gson = new GsonBuilder().registerTypeAdapterFactory(new NonNullListTypeAdapterFactory())
            .registerTypeAdapterFactory(new SerializeNullTypeAdapterFactory()).create();

    private final ThingUID thingUID = new ThingUID(THING_TYPE_ECHO, "test");
    private final Thing thing = mock(Thing.class);
    private final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);

    @Test
    public void dndEnabledUpdatesTheDoNotDisturbChannelAndNothingElse() {
        EchoHandler handler = createHandler();

        handler.handlePushCommand("PUSH_DND_STATE_CHANGE", PAYLOAD.formatted("true"));

        verify(callback).stateUpdated(new ChannelUID(thingUID, CHANNEL_DO_NOT_DISTURB), OnOffType.ON);
        // a fall-through into the equalizer case would zero three unrelated channels
        verifyNoMoreInteractions(callback);
    }

    @Test
    public void dndDisabledUpdatesTheDoNotDisturbChannelAndNothingElse() {
        EchoHandler handler = createHandler();

        handler.handlePushCommand("PUSH_DND_STATE_CHANGE", PAYLOAD.formatted("false"));

        verify(callback).stateUpdated(new ChannelUID(thingUID, CHANNEL_DO_NOT_DISTURB), OnOffType.OFF);
        verifyNoMoreInteractions(callback);
    }

    @Test
    public void aDndPushReceivedByTheAccountReachesTheDeviceChannel() {
        EchoHandler echoHandler = createHandler();
        AccountHandler accountHandler = createAccountHandlerKnowing(echoHandler);
        PushCommandTO pushCommand = new PushCommandTO();
        pushCommand.command = "PUSH_DND_STATE_CHANGE";
        pushCommand.payload = PAYLOAD.formatted("true");

        accountHandler.onPushCommandReceived(pushCommand);

        verify(callback).stateUpdated(new ChannelUID(thingUID, CHANNEL_DO_NOT_DISTURB), OnOffType.ON);
        verifyNoMoreInteractions(callback);
    }

    private AccountHandler createAccountHandlerKnowing(EchoHandler echoHandler) {
        Bridge bridge = mock(Bridge.class);
        when(bridge.getUID()).thenReturn(new ThingUID(THING_TYPE_ACCOUNT, "account"));
        @SuppressWarnings("unchecked")
        Storage<String> storage = (Storage<String>) mock(Storage.class);
        AccountHandler accountHandler = new AccountHandler(bridge, storage, gson, mock(HttpClient.class),
                mock(HTTP2Client.class), mock(AmazonEchoControlCommandDescriptionProvider.class));
        accountHandler.setCallback(mock(ThingHandlerCallback.class));
        when(thing.getConfiguration())
                .thenReturn(new Configuration(Map.of(DEVICE_PROPERTY_SERIAL_NUMBER, ECHO_SERIAL)));
        accountHandler.childHandlerInitialized(echoHandler, thing);
        return accountHandler;
    }

    private EchoHandler createHandler() {
        when(thing.getUID()).thenReturn(thingUID);
        EchoHandler handler = new EchoHandler(thing, gson, mock(AmazonEchoControlStateDescriptionProvider.class));
        handler.setCallback(callback);
        return handler;
    }
}
