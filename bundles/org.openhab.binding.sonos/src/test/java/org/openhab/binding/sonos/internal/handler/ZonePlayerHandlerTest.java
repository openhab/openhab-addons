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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.sonos.internal.SonosBindingConstants.ZONEPLAYER_THING_TYPE_UID;

import java.net.URL;
import java.util.Map;

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
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ThingBuilder;

/**
 * Tests the switch of a generic zoneplayer thing to the thing type of its model.
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
    public void zonePlayerOfUnknownModelKeepsItsThingType() {
        initializeZonePlayerReporting("/descriptor-unknown.xml");

        verify(callback, never()).migrateThingType(any(), any(), any());
    }

    private Thing initializeZonePlayerReporting(String descriptor) {
        URL descriptorUrl = getClass().getResource(descriptor);
        when(upnpIOService.getDescriptorURL(any(UpnpIOParticipant.class))).thenAnswer(
                invocation -> UDN.equals(invocation.<UpnpIOParticipant> getArgument(0).getUDN()) ? descriptorUrl
                        : null);
        Thing thing = ThingBuilder.create(ZONEPLAYER_THING_TYPE_UID, "test")
                .withConfiguration(new Configuration(Map.of("udn", UDN))).build();
        ZonePlayerHandler handler = new ZonePlayerHandler(thingRegistry, thing, upnpIOService, null,
                stateDescriptionProvider);
        handler.setCallback(callback);
        this.handler = handler;
        handler.initialize();
        return thing;
    }
}
