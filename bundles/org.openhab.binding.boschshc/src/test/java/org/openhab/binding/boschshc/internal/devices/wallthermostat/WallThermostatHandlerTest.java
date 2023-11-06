/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal.devices.wallthermostat;

import static org.mockito.Mockito.verify;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.boschshc.internal.devices.AbstractBatteryPoweredDeviceHandlerTest;
import org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingTypeUID;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Unit Tests for {@link WallThermostatHandler}.
 *
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
class WallThermostatHandlerTest extends AbstractBatteryPoweredDeviceHandlerTest<WallThermostatHandler> {

    @Override
    protected WallThermostatHandler createFixture() {
        return new WallThermostatHandler(getThing());
    }

    @Override
    protected String getDeviceID() {
        return "hdm:ZigBee:000d6f0016d1a193";
    }

    @Override
    protected ThingTypeUID getThingTypeUID() {
        return BoschSHCBindingConstants.THING_TYPE_WALL_THERMOSTAT;
    }

    @Test
    void testUpdateChannelsTemperatureLevelService() {
        JsonElement jsonObject = JsonParser.parseString("""
                {
                   "@type": "temperatureLevelState",
                   "temperature": 21.5
                 }\
                """);
        getFixture().processUpdate("TemperatureLevel", jsonObject);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_TEMPERATURE),
                new QuantityType<Temperature>(21.5, SIUnits.CELSIUS));
    }

    @Test
    void testUpdateChannelsHumidityLevelService() {
        JsonElement jsonObject = JsonParser
                .parseString("{\n" + "   \"@type\": \"humidityLevelState\",\n" + "   \"humidity\": 42.5\n" + " }");
        getFixture().processUpdate("HumidityLevel", jsonObject);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_HUMIDITY),
                new QuantityType<Dimensionless>(42.5, Units.PERCENT));
    }
}
