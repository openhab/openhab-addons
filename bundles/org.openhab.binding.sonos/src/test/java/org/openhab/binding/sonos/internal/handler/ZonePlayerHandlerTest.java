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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.sonos.internal.SonosBindingConstants.ONE_THING_TYPE_UID;
import static org.openhab.binding.sonos.internal.SonosBindingConstants.ZONEPLAYER_THING_TYPE_UID;

import java.net.URL;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ThingBuilder;

/**
 * Tests the poll of a zoneplayer: the switch to the thing type of its model and the status after unanswered polls.
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
    private final AtomicInteger pollsBeforeOffline = new AtomicInteger();
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
    public void onlinePlayerStaysOnlineWhileNoTwoPollsInARowGoUnanswered() {
        Thing thing = initializePlayerAnsweringPolls(true, false, true, false, true);

        verify(upnpIOService, timeout(30000).atLeast(6)).invokeAction(any(UpnpIOParticipant.class),
                eq("DeviceProperties"), eq("GetZoneInfo"), isNull());
        assertEquals(ThingStatus.ONLINE, thing.getStatus());
        assertEquals(0, pollsBeforeOffline.get());
    }

    @Test
    public void onlinePlayerGoesOfflineAfterTwoUnansweredPolls() {
        Thing thing = initializePlayerAnsweringPolls(true, false, false);

        verify(upnpIOService, timeout(30000).atLeast(4)).invokeAction(any(UpnpIOParticipant.class),
                eq("DeviceProperties"), eq("GetZoneInfo"), isNull());
        assertEquals(ThingStatus.OFFLINE, thing.getStatus());
        assertEquals(3, pollsBeforeOffline.get());
    }

    @Test
    public void playerThatNeverAnsweredGoesOfflineOnTheFirstPoll() {
        Thing thing = initializePlayerAnsweringPolls(false);

        verify(upnpIOService, timeout(30000).atLeast(2)).invokeAction(any(UpnpIOParticipant.class),
                eq("DeviceProperties"), eq("GetZoneInfo"), isNull());
        assertEquals(ThingStatus.OFFLINE, thing.getStatus());
        assertEquals(1, pollsBeforeOffline.get());
    }

    @SuppressWarnings("null")
    private Thing initializePlayerAnsweringPolls(Boolean... answered) {
        AtomicInteger polls = new AtomicInteger();
        when(upnpIOService.isRegistered(any(UpnpIOParticipant.class))).thenReturn(true);
        when(upnpIOService.invokeAction(any(UpnpIOParticipant.class), eq("DeviceProperties"), eq("GetZoneInfo"),
                isNull()))
                .thenAnswer(invocation -> answered[Math.min(polls.getAndIncrement(), answered.length - 1)]
                        ? Map.of("HardwareVersion", "1.0")
                        : Map.of());
        Thing thing = ThingBuilder.create(ONE_THING_TYPE_UID, "test")
                .withConfiguration(new Configuration(Map.of("udn", UDN, "refresh", 1))).build();
        doAnswer(invocation -> {
            ThingStatusInfo statusInfo = invocation.getArgument(1);
            thing.setStatusInfo(statusInfo);
            if (statusInfo.getStatus() == ThingStatus.OFFLINE) {
                pollsBeforeOffline.compareAndSet(0, polls.get());
            }
            return null;
        }).when(callback).statusUpdated(any(Thing.class), any(ThingStatusInfo.class));
        ZonePlayerHandler handler = new ZonePlayerHandler(thingRegistry, thing, upnpIOService, null,
                stateDescriptionProvider);
        handler.setCallback(callback);
        this.handler = handler;
        handler.initialize();
        return thing;
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
