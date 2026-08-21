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
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.THING_TYPE_ECHO;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlStateDescriptionProvider;
import org.openhab.binding.amazonechocontrol.internal.util.NonNullListTypeAdapterFactory;
import org.openhab.binding.amazonechocontrol.internal.util.SerializeNullTypeAdapterFactory;
import org.openhab.core.library.types.OnOffType;
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
    private static final String PAYLOAD = "{\"dopplerId\":{\"deviceSerialNumber\":\"G000AA0000000000\","
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

    private EchoHandler createHandler() {
        when(thing.getUID()).thenReturn(thingUID);
        EchoHandler handler = new EchoHandler(thing, gson, mock(AmazonEchoControlStateDescriptionProvider.class));
        handler.setCallback(callback);
        return handler;
    }
}
