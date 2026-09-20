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
package org.openhab.binding.sonos.internal.handler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.sonos.internal.SonosBindingConstants.FIRMWARE;
import static org.openhab.binding.sonos.internal.SonosBindingConstants.ZONEPLAYER_THING_TYPE_UID;

import java.net.URL;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.sonos.internal.SonosStateDescriptionOptionProvider;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.transport.upnp.UpnpIOParticipant;
import org.openhab.core.io.transport.upnp.UpnpIOService;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.binding.builder.ThingStatusInfoBuilder;

/**
 * Tests the switch of a generic zoneplayer thing to the thing type of its model, and the firmware channel.
 *
 * @author Martin Littkovsky - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
public class ZonePlayerHandlerTest {

    private static final String UDN = "RINCON_000000000000001400";

    private @Mock @NonNullByDefault({}) ThingRegistry thingRegistry;
    private @Mock @NonNullByDefault({}) UpnpIOService upnpIOService;
    private @Mock @NonNullByDefault({}) SonosStateDescriptionOptionProvider stateDescriptionProvider;
    private @Mock @NonNullByDefault({}) ThingHandlerCallback callback;

    private final AtomicBoolean registered = new AtomicBoolean(true);
    private @Nullable ZonePlayerHandler handler;

    @AfterEach
    public void disposeHandler() {
        ZonePlayerHandler handler = this.handler;
        if (handler != null) {
            handler.dispose();
        }
    }

    @Test
    public void zonePlayerSwitchesToTheThingTypeOfItsModel() {
        Thing thing = initializeZonePlayerReporting("/descriptor-play5.xml");

        verify(callback).migrateThingType(thing, new ThingTypeUID("sonos", "PLAY5"), thing.getConfiguration());
    }

    @Test
    @SuppressWarnings("null")
    public void zonePlayerOfUnknownModelKeepsItsThingType() {
        initializeZonePlayerReporting("/descriptor-unknown.xml");

        verify(callback, never()).migrateThingType(any(), any(), any());
    }

    @Test
    @SuppressWarnings("null")
    public void zonePlayerRegisteredAfterInitializeSwitchesOnTheNextPoll() {
        registered.set(false);
        when(upnpIOService.isRegistered(any(UpnpIOParticipant.class))).thenAnswer(invocation -> registered.get());
        Thing thing = initializeZonePlayerReporting("/descriptor-play5.xml");
        verify(callback, never()).migrateThingType(any(), any(), any());

        registered.set(true);

        verify(callback, timeout(5000)).migrateThingType(thing, new ThingTypeUID("sonos", "PLAY5"),
                thing.getConfiguration());
    }

    @Test
    public void firmwareVersionThatArrivedBeforeTheLinkStillReachesTheChannel() {
        final String reportedVersion = "18.8";
        ThingUID thingUID = new ThingUID(ZONEPLAYER_THING_TYPE_UID, "test");
        ChannelUID firmwareChannel = new ChannelUID(thingUID, FIRMWARE);
        Thing thing = ThingBuilder.create(ZONEPLAYER_THING_TYPE_UID, thingUID)
                .withChannel(ChannelBuilder.create(firmwareChannel).withAcceptedItemType("String").build()).build();
        thing.setStatusInfo(ThingStatusInfoBuilder.create(ThingStatus.ONLINE).build());
        ZonePlayerHandler handler = new ZonePlayerHandler(thingRegistry, thing, upnpIOService, null,
                stateDescriptionProvider);
        handler.setCallback(callback);
        this.handler = handler;

        handler.onValueReceived("DisplaySoftwareVersion", reportedVersion, "DeviceProperties");

        verify(callback, never()).stateUpdated(eq(firmwareChannel), any());

        when(callback.isChannelLinked(firmwareChannel)).thenReturn(true);
        handler.channelLinked(firmwareChannel);

        verify(callback).stateUpdated(firmwareChannel, new StringType(reportedVersion));
    }

    private Thing initializeZonePlayerReporting(String descriptor) {
        URL descriptorUrl = getClass().getResource(descriptor);
        when(upnpIOService.getDescriptorURL(any(UpnpIOParticipant.class))).thenAnswer(
                invocation -> registered.get() && UDN.equals(invocation.<UpnpIOParticipant> getArgument(0).getUDN())
                        ? descriptorUrl
                        : null);
        Thing thing = ThingBuilder.create(ZONEPLAYER_THING_TYPE_UID, "test")
                .withConfiguration(new Configuration(Map.of("udn", UDN, "refresh", 1))).build();
        ZonePlayerHandler handler = new ZonePlayerHandler(thingRegistry, thing, upnpIOService, null,
                stateDescriptionProvider);
        handler.setCallback(callback);
        this.handler = handler;
        handler.initialize();
        return thing;
    }
}
