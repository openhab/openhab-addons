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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.DEVICE_FAMILY_THIRD_PARTY_AVS_MEDIA_DISPLAY;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlStateDescriptionProvider;
import org.openhab.binding.amazonechocontrol.internal.dto.DeviceTO;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.ThingHandlerCallback;

import com.google.gson.Gson;

/**
 * The {@link EchoHandlerDeviceStatusTest} checks how the thing status follows the online flag Amazon reports for a
 * device
 *
 * @author Martin Littkovsky - Initial contribution
 */
@NonNullByDefault
public class EchoHandlerDeviceStatusTest {
    private static final ThingStatusInfo ONLINE = new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);
    private static final ThingStatusInfo OFFLINE = new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
            null);

    private final Thing thing = mock(Thing.class);
    private final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);

    @Test
    public void anOfflineEchoTakesTheThingOffline() {
        EchoHandler handler = createHandler();

        boolean online = handler.setDeviceAndUpdateThingStatus(device("ECHO", false), null);

        assertThat(online, is(false));
        verify(callback).statusUpdated(thing, OFFLINE);
    }

    @Test
    public void anOnlineEchoTakesTheThingOnline() {
        EchoHandler handler = createHandler();

        boolean online = handler.setDeviceAndUpdateThingStatus(device("ECHO", true), null);

        assertThat(online, is(true));
        verify(callback).statusUpdated(thing, ONLINE);
    }

    @Test
    public void aThirdPartyMediaDeviceIsOnlineAlthoughAmazonReportsItOffline() {
        EchoHandler handler = createHandler();

        boolean online = handler
                .setDeviceAndUpdateThingStatus(device(DEVICE_FAMILY_THIRD_PARTY_AVS_MEDIA_DISPLAY, false), null);

        assertThat(online, is(true));
        verify(callback).statusUpdated(thing, ONLINE);
    }

    @Test
    public void anOfflineDeviceWithoutFamilyTakesTheThingOffline() {
        EchoHandler handler = createHandler();

        boolean online = handler.setDeviceAndUpdateThingStatus(device(null, false), null);

        assertThat(online, is(false));
        verify(callback).statusUpdated(thing, OFFLINE);
    }

    private static DeviceTO device(@Nullable String family, boolean online) {
        DeviceTO device = new DeviceTO();
        device.serialNumber = "SERIAL";
        device.deviceFamily = family;
        device.online = online;
        return device;
    }

    private EchoHandler createHandler() {
        EchoHandler handler = new EchoHandler(thing, new Gson(), mock(AmazonEchoControlStateDescriptionProvider.class));
        handler.setCallback(callback);
        return handler;
    }
}
