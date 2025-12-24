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
package org.openhab.binding.boschshc.internal.devices.twinguard;

import static org.mockito.Mockito.verify;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.boschshc.internal.devices.AbstractSmokeDetectorHandlerTest;
import org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingTypeUID;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Unit Tests for {@link TwinguardHandler}.
 *
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
class TwinguardHandlerTest extends AbstractSmokeDetectorHandlerTest<TwinguardHandler> {

    @Override
    protected TwinguardHandler createFixture() {
        return new TwinguardHandler(getThing());
    }

    @Override
    protected String getDeviceID() {
        return "hdm:ZigBee:000d6f0016d1a193";
    }

    @Override
    protected ThingTypeUID getThingTypeUID() {
        return BoschSHCBindingConstants.THING_TYPE_TWINGUARD;
    }

    @Test
    void testUpdateChannelsAirQualityLevelService() {
        JsonElement jsonObject = JsonParser.parseString(
                """
                        {"temperatureRating":"GOOD","humidityRating":"MEDIUM","purity":620,"@type":"airQualityLevelState",
                             "purityRating":"GOOD","temperature":23.77,"description":"LITTLE_DRY","humidity":32.69,"combinedRating":"MEDIUM"}\
                        """);
        getFixture().processUpdate("AirQualityLevel", jsonObject);

        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_TEMPERATURE),
                new QuantityType<>(23.77, SIUnits.CELSIUS));

        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_TEMPERATURE_RATING),
                new StringType("GOOD"));

        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_HUMIDITY),
                new QuantityType<>(32.69, Units.PERCENT));

        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_HUMIDITY_RATING),
                new StringType("MEDIUM"));

        verify(getCallback()).stateUpdated(new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_PURITY),
                new QuantityType<>(620, Units.PARTS_PER_MILLION));

        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_PURITY_RATING),
                new StringType("GOOD"));

        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_AIR_DESCRIPTION),
                new StringType("LITTLE_DRY"));

        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_COMBINED_RATING),
                new StringType("MEDIUM"));
    }
}
