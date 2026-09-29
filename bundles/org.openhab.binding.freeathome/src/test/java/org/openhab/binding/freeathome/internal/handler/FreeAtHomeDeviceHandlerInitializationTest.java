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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;

/**
 * Tests that only the current initialization run reaches the outside world, because a run left over from a previous
 * configuration would otherwise keep the old device id registered on the bridge, and a later devicesRemoved event for
 * that id then marks the reconfigured thing GONE.
 *
 * @author Martin Littkovsky - Initial contribution
 */
@NonNullByDefault
public class FreeAtHomeDeviceHandlerInitializationTest {

    private static final ThingUID BRIDGE_UID = new ThingUID(BRIDGE_TYPE_UID, "bridge");
    private static final ThingUID DEVICE_UID = new ThingUID(DEVICE_TYPE_UID, "test");
    private static final String DEVICE_ID = "ABB0001";

    private @NonNullByDefault({}) Thing thing;
    private @NonNullByDefault({}) ThingHandlerCallback callback;
    private @NonNullByDefault({}) FreeAtHomeBridgeHandler bridgeHandler;
    private @NonNullByDefault({}) FreeAtHomeDeviceHandler handler;

    @BeforeEach
    public void setUp() throws FreeAtHomeHttpCommunicationException {
        thing = mock(Thing.class);
        when(thing.getUID()).thenReturn(DEVICE_UID);
        when(thing.getThingTypeUID()).thenReturn(DEVICE_TYPE_UID);
        when(thing.getBridgeUID()).thenReturn(BRIDGE_UID);
        when(thing.getConfiguration()).thenReturn(new Configuration(Map.of("deviceId", DEVICE_ID)));
        when(thing.getChannels()).thenReturn(List.of());
        when(thing.getStatus()).thenReturn(ThingStatus.UNKNOWN);
        when(thing.getLabel()).thenReturn("test");

        FreeAtHomeDeviceDescription descriptionWithoutChannels = new FreeAtHomeDeviceDescription();
        descriptionWithoutChannels.deviceId = DEVICE_ID;

        bridgeHandler = mock(FreeAtHomeBridgeHandler.class);
        when(bridgeHandler.getFreeatHomeDeviceDescription(DEVICE_ID)).thenReturn(descriptionWithoutChannels);

        Bridge bridge = mock(Bridge.class);
        when(bridge.getUID()).thenReturn(BRIDGE_UID);
        when(bridge.getStatus()).thenReturn(ThingStatus.ONLINE);
        when(bridge.getHandler()).thenReturn(bridgeHandler);

        callback = mock(ThingHandlerCallback.class);
        when(callback.getBridge(BRIDGE_UID)).thenReturn(bridge);

        LocaleProvider localeProvider = mock(LocaleProvider.class);
        when(localeProvider.getLocale()).thenReturn(Locale.GERMAN);

        handler = new FreeAtHomeDeviceHandler(thing, mock(FreeAtHomeChannelTypeProvider.class),
                mock(TranslationProvider.class), localeProvider);
        handler.setCallback(callback);
    }

    @Test
    public void staleInitializationRunNeitherRegistersNorPublishesStatus() {
        long staleGeneration = handler.currentInitializationGeneration() - 1;

        handler.initializeDevice(staleGeneration);

        verify(bridgeHandler, never()).registerDeviceStateListener(any(), any());
        verify(callback, never()).statusUpdated(any(), any());
    }

    @Test
    public void bridgeGoingOfflineOutdatesTheRunningInitialization() {
        long generation = handler.currentInitializationGeneration();

        handler.bridgeStatusChanged(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.NONE, null));
        handler.initializeDevice(generation);

        verify(bridgeHandler, never()).registerDeviceStateListener(any(), any());
        verify(callback, never()).statusUpdated(eq(thing),
                argThat(statusInfo -> statusInfo.getStatus() == ThingStatus.ONLINE));
    }

    @Test
    public void currentInitializationRunRegistersOnceAndGoesOnline() {
        handler.initializeDevice(handler.currentInitializationGeneration());

        verify(bridgeHandler, times(1)).registerDeviceStateListener(DEVICE_ID, handler);
        verify(callback).statusUpdated(eq(thing), argThat(statusInfo -> statusInfo.getStatus() == ThingStatus.ONLINE));
    }

    @Test
    public void initializationRunAfterDisposeNeitherRegistersNorPublishesStatus() {
        handler.dispose();

        handler.initializeDevice(handler.currentInitializationGeneration());

        verify(bridgeHandler, never()).registerDeviceStateListener(any(), any());
        verify(callback, never()).statusUpdated(any(), any());
    }
}
