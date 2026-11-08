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
package org.openhab.binding.remehaheating.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import com.google.gson.JsonParser;

/**
 * Unit tests for {@link RemehaHeatingHandler}.
 *
 * @author Michael Fraedrich - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
public class RemehaHeatingHandlerTest {

    private @Mock @NonNullByDefault({}) Thing thing;
    private @Mock @NonNullByDefault({}) ThingHandlerCallback callback;
    private @Mock @NonNullByDefault({}) Configuration configuration;
    private @Mock @NonNullByDefault({}) org.eclipse.jetty.client.HttpClient httpClient;
    private @NonNullByDefault({}) RemehaHeatingHandler handler;
    private @NonNullByDefault({}) ThingUID thingUID;
    private @NonNullByDefault({}) ChannelUID channelUID;

    @BeforeEach
    public void setUp() {
        thingUID = new ThingUID(RemehaHeatingBindingConstants.THING_TYPE_BOILER, "test");
        channelUID = new ChannelUID(thingUID, RemehaHeatingBindingConstants.CHANNEL_TARGET_TEMPERATURE);

        lenient().when(thing.getUID()).thenReturn(thingUID);
        lenient().when(thing.getConfiguration()).thenReturn(configuration);

        handler = new RemehaHeatingHandler(thing, httpClient);
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
        lenient().when(configuration.as(RemehaHeatingConfiguration.class)).thenReturn(config);

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
    public void testNullValuesInDashboardAreUndefined() {
        // The Remeha API returns null for unavailable values, e.g. no hot water temperature on some appliances
        String json = """
                { "appliances": [{
                  "waterPressure": null, "errorStatus": "Running", "waterPressureOK": true,
                  "outdoorTemperatureInformation": { "internetOutdoorTemperature": null },
                  "climateZones": [{ "climateZoneId": "zone123", "roomTemperature": 21.5, "setPoint": 21.5 }],
                  "hotWaterZones": [{ "hotWaterZoneId": "zone456", "dhwTemperature": null, "targetSetpoint": 60.0,
                    "dhwZoneMode": "Off", "dhwStatus": "Idle" }]
                }] }
                """;
        assertDoesNotThrow(() -> handler.updateChannelsFromDashboard(JsonParser.parseString(json).getAsJsonObject()));

        verifyState(RemehaHeatingBindingConstants.CHANNEL_WATER_PRESSURE, UnDefType.UNDEF);
        verifyState(RemehaHeatingBindingConstants.CHANNEL_OUTDOOR_TEMPERATURE, UnDefType.UNDEF);
        verifyState(RemehaHeatingBindingConstants.CHANNEL_DHW_TEMPERATURE, UnDefType.UNDEF);
        // values after a null value are still processed
        verifyState(RemehaHeatingBindingConstants.CHANNEL_DHW_TARGET, new QuantityType<>(60.0, SIUnits.CELSIUS));
        verifyState(RemehaHeatingBindingConstants.CHANNEL_DHW_MODE, new StringType("Off"));
        verifyState(RemehaHeatingBindingConstants.CHANNEL_ROOM_TEMPERATURE, new QuantityType<>(21.5, SIUnits.CELSIUS));
        verifyState(RemehaHeatingBindingConstants.CHANNEL_STATUS, new StringType("Running"));
    }

    @Test
    public void testDashboardValuesAreMapped() {
        String json = """
                { "appliances": [{
                  "waterPressure": 1.6, "errorStatus": "Running", "waterPressureOK": true,
                  "outdoorTemperatureInformation": { "internetOutdoorTemperature": 7.5 },
                  "climateZones": [{ "roomTemperature": 21.5, "setPoint": 22.0 }],
                  "hotWaterZones": [{ "dhwTemperature": 58.6, "targetSetpoint": 56.0,
                    "dhwZoneMode": "Scheduling", "dhwStatus": "Idle" }]
                }] }
                """;
        handler.updateChannelsFromDashboard(JsonParser.parseString(json).getAsJsonObject());

        verifyState(RemehaHeatingBindingConstants.CHANNEL_WATER_PRESSURE, new QuantityType<>(1.6, Units.BAR));
        verifyState(RemehaHeatingBindingConstants.CHANNEL_WATER_PRESSURE_OK, OnOffType.ON);
        verifyState(RemehaHeatingBindingConstants.CHANNEL_OUTDOOR_TEMPERATURE,
                new QuantityType<>(7.5, SIUnits.CELSIUS));
        verifyState(RemehaHeatingBindingConstants.CHANNEL_TARGET_TEMPERATURE,
                new QuantityType<>(22.0, SIUnits.CELSIUS));
        verifyState(RemehaHeatingBindingConstants.CHANNEL_DHW_TEMPERATURE, new QuantityType<>(58.6, SIUnits.CELSIUS));
        verifyState(RemehaHeatingBindingConstants.CHANNEL_DHW_STATUS, new StringType("Idle"));
    }

    private void verifyState(String channelId, State expected) {
        verify(callback).stateUpdated(new ChannelUID(thingUID, channelId), expected);
    }

    @Test
    public void testDispose() {
        // Should not throw exception
        assertDoesNotThrow(() -> handler.dispose());
    }
}
