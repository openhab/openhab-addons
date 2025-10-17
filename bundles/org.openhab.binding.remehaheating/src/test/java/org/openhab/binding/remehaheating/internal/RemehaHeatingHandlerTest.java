/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.remehaheating.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.RefreshType;

/**
 * Unit tests for {@link RemehaHeatingHandler}.
 *
 * @author Michael Fraedrich - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
public class RemehaHeatingHandlerTest {

    @Mock
    private Thing thing;
    @Mock
    private ThingHandlerCallback callback;
    @Mock
    private Configuration configuration;
    private RemehaHeatingHandler handler;
    private ThingUID thingUID;
    private ChannelUID channelUID;

    @BeforeEach
    public void setUp() {
        thingUID = new ThingUID(RemehaHeatingBindingConstants.THING_TYPE_BOILER, "test");
        channelUID = new ChannelUID(thingUID, RemehaHeatingBindingConstants.CHANNEL_TARGET_TEMPERATURE);

        lenient().when(thing.getUID()).thenReturn(thingUID);
        lenient().when(thing.getConfiguration()).thenReturn(configuration);

        handler = new RemehaHeatingHandler(thing);
        handler.setCallback(callback);
    }

    @Test
    public void testConstructor() {
        assertNotNull(handler);
    }

    @Test
    public void testInitializeWithMissingCredentials() {
        RemehaHeatingConfiguration config = new RemehaHeatingConfiguration();
        config.email = "";
        config.password = "";

        when(configuration.as(RemehaHeatingConfiguration.class)).thenReturn(config);

        handler.initialize();

        verify(callback).statusUpdated(eq(thing), argThat(status -> status.getStatus() == ThingStatus.OFFLINE
                && status.getStatusDetail() == ThingStatusDetail.CONFIGURATION_ERROR));
    }

    @Test
    public void testHandleRefreshCommand() {
        ChannelUID channelUID = new ChannelUID(thingUID, RemehaHeatingBindingConstants.CHANNEL_ROOM_TEMPERATURE);

        // Should not throw exception
        assertDoesNotThrow(() -> handler.handleCommand(channelUID, RefreshType.REFRESH));
    }

    @Test
    public void testHandleTargetTemperatureCommand() {
        DecimalType temperature = new DecimalType(21.5);

        // Should not throw exception
        assertDoesNotThrow(() -> handler.handleCommand(channelUID, temperature));
    }

    @Test
    public void testHandleDhwModeCommand() {
        ChannelUID dhwChannelUID = new ChannelUID(thingUID, RemehaHeatingBindingConstants.CHANNEL_DHW_MODE);
        StringType mode = new StringType("schedule");

        // Should not throw exception
        assertDoesNotThrow(() -> handler.handleCommand(dhwChannelUID, mode));
    }

    @Test
    public void testDispose() {
        // Should not throw exception
        assertDoesNotThrow(() -> handler.dispose());
    }
}
