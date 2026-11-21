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
package org.openhab.binding.freeathome.internal.handler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.freeathome.internal.FreeAtHomeBindingConstants.BRIDGE_TYPE_UID;
import static org.openhab.binding.freeathome.internal.FreeAtHomeBindingConstants.DEVICE_TYPE_UID;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.freeathome.internal.datamodel.FreeAtHomeDeviceDescription;
import org.openhab.binding.freeathome.internal.type.FreeAtHomeChannelTypeProvider;
import org.openhab.binding.freeathome.internal.util.FreeAtHomeHttpCommunicationException;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;

import com.google.gson.JsonParser;

/**
 * Tests that a state event reaches every channel built on its datapoint, for new and for existing things.
 *
 * @author Martin Littkovsky - Initial contribution
 */
@NonNullByDefault
public class FreeAtHomeDeviceHandlerStateEventTest {

    private static final ThingUID BRIDGE_UID = new ThingUID(BRIDGE_TYPE_UID, "bridge");
    private static final ThingUID DEVICE_UID = new ThingUID(DEVICE_TYPE_UID, "rtc");
    private static final String DEVICE_ID = "ABB700000001";
    private static final String STATUS_INDICATION_EVENT = DEVICE_ID + "/ch0000/odp0009";
    private static final ChannelUID STATUS_INDICATION_CHANNEL = new ChannelUID(DEVICE_UID, "ch0000",
            "status-indication");
    private static final ChannelUID ECO_MODE_CHANNEL = new ChannelUID(DEVICE_UID, "ch0000", "eco-mode-on-off-request");
    private static final String MOVEMENT_EVENT = DEVICE_ID + "/ch0000/odp0001";
    private static final ChannelUID MOVE_CHANNEL = new ChannelUID(DEVICE_UID, "ch0000", "move-up-down");
    private static final ChannelUID ADJUST_CHANNEL = new ChannelUID(DEVICE_UID, "ch0000", "adjust-up-down");

    private static final String ROOM_TEMPERATURE_CONTROLLER = """
            "functionID": "23", "displayName": "RTC",
            "inputs": { "idp0011": { "pairingID": 58 } },
            "outputs": { "odp0009": { "pairingID": 54 } }
            """;
    private static final String SHUTTER_ACTUATOR = """
            "functionID": "9", "displayName": "Shutter",
            "inputs": { "idp0000": { "pairingID": 32 }, "idp0001": { "pairingID": 33 } },
            "outputs": { "odp0001": { "pairingID": 288 } }
            """;

    private @NonNullByDefault({}) Thing thing;
    private @NonNullByDefault({}) Bridge bridge;
    private @NonNullByDefault({}) FreeAtHomeBridgeHandler bridgeHandler;
    private @NonNullByDefault({}) ThingHandlerCallback callback;
    private @NonNullByDefault({}) LocaleProvider localeProvider;
    private @NonNullByDefault({}) FreeAtHomeChannelTypeProvider channelTypeProvider;
    private @NonNullByDefault({}) TranslationProvider i18nProvider;
    private @NonNullByDefault({}) FreeAtHomeDeviceHandler handler;

    @BeforeEach
    public void setUp() {
        thing = mock(Thing.class);
        when(thing.getUID()).thenReturn(DEVICE_UID);
        when(thing.getThingTypeUID()).thenReturn(DEVICE_TYPE_UID);
        when(thing.getBridgeUID()).thenReturn(BRIDGE_UID);
        when(thing.getConfiguration()).thenReturn(new Configuration(Map.of("deviceId", DEVICE_ID)));
        when(thing.getStatus()).thenReturn(ThingStatus.UNKNOWN);
        when(thing.getLabel()).thenReturn("rtc");

        bridgeHandler = mock(FreeAtHomeBridgeHandler.class);

        bridge = mock(Bridge.class);
        when(bridge.getUID()).thenReturn(BRIDGE_UID);
        when(bridge.getStatus()).thenReturn(ThingStatus.ONLINE);
        when(bridge.getHandler()).thenReturn(bridgeHandler);

        callback = mock(ThingHandlerCallback.class);
        when(callback.getBridge(BRIDGE_UID)).thenReturn(bridge);

        localeProvider = mock(LocaleProvider.class);
        when(localeProvider.getLocale()).thenReturn(Locale.GERMAN);

        channelTypeProvider = mock(FreeAtHomeChannelTypeProvider.class);
        i18nProvider = mock(TranslationProvider.class);

        handler = new FreeAtHomeDeviceHandler(thing, channelTypeProvider, i18nProvider, localeProvider);
        handler.setCallback(callback);
    }

    private void initialize(String channel, List<Channel> existingChannels)
            throws FreeAtHomeHttpCommunicationException {
        FreeAtHomeDeviceDescription description = new FreeAtHomeDeviceDescription(JsonParser.parseString("""
                { "%s": { "interface": "TP", "displayName": "device", "channels": { "ch0000": { %s } } } }
                """.formatted(DEVICE_ID, channel)).getAsJsonObject(), DEVICE_ID);
        when(bridgeHandler.getFreeatHomeDeviceDescription(DEVICE_ID)).thenReturn(description);
        when(thing.getChannels()).thenReturn(existingChannels);

        handler.initializeDevice(handler.currentInitializationGeneration());
    }

    @Test
    public void statusEventUpdatesStatusIndicationAndEcoMode() throws FreeAtHomeHttpCommunicationException {
        initialize(ROOM_TEMPERATURE_CONTROLLER, List.of());

        handler.onDeviceStateChanged(STATUS_INDICATION_EVENT, "68");

        verify(callback).stateUpdated(STATUS_INDICATION_CHANNEL, new DecimalType(68));
        verify(callback).stateUpdated(ECO_MODE_CHANNEL, OnOffType.ON);
    }

    @Test
    public void statusEventWithoutEcoBitSwitchesEcoModeOff() throws FreeAtHomeHttpCommunicationException {
        initialize(ROOM_TEMPERATURE_CONTROLLER, List.of());

        handler.onDeviceStateChanged(STATUS_INDICATION_EVENT, "65");

        verify(callback).stateUpdated(STATUS_INDICATION_CHANNEL, new DecimalType(65));
        verify(callback).stateUpdated(ECO_MODE_CHANNEL, OnOffType.OFF);
    }

    @Test
    public void existingThingRoutesStatusEventToEcoModeToo() throws FreeAtHomeHttpCommunicationException {
        initialize(ROOM_TEMPERATURE_CONTROLLER, List.of(ChannelBuilder.create(STATUS_INDICATION_CHANNEL).build(),
                ChannelBuilder.create(ECO_MODE_CHANNEL).build()));

        handler.onDeviceStateChanged(STATUS_INDICATION_EVENT, "68");

        verify(callback).stateUpdated(STATUS_INDICATION_CHANNEL, new DecimalType(68));
        verify(callback).stateUpdated(ECO_MODE_CHANNEL, OnOffType.ON);
    }

    @Test
    public void movementEventUpdatesBothShutterChannels() throws FreeAtHomeHttpCommunicationException {
        initialize(SHUTTER_ACTUATOR, List.of());

        handler.onDeviceStateChanged(MOVEMENT_EVENT, "2");

        verify(callback).stateUpdated(MOVE_CHANNEL, UpDownType.UP);
        verify(callback).stateUpdated(ADJUST_CHANNEL, UpDownType.UP);
    }
}
