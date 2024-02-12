/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal.devices.lightcontrol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.openhab.binding.boschshc.internal.devices.AbstractBoschSHCDeviceHandlerTest;
import org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingTypeUID;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Unit tests for {@link LightControl2Handler}.
 * 
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
class LightControl2HandlerTest extends AbstractBoschSHCDeviceHandlerTest<LightControl2Handler> {

    private @Captor @NonNullByDefault({}) ArgumentCaptor<QuantityType<Power>> powerCaptor;

    private @Captor @NonNullByDefault({}) ArgumentCaptor<QuantityType<Energy>> energyCaptor;

    @Override
    protected LightControl2Handler createFixture() {
        return new LightControl2Handler(getThing());
    }

    @Override
    protected ThingTypeUID getThingTypeUID() {
        return BoschSHCBindingConstants.THING_TYPE_LIGHT_CONTROL_2;
    }

    @Override
    protected String getDeviceID() {
        return "hdm:ZigBee:70ac08fcfefa5197";
    }

    @Test
    void testUpdateChannelsCommunicationQualityService() {
        String json = """
                {
                    "@type": "communicationQualityState",
                    "quality": "UNKNOWN"
                }
                """;
        JsonElement jsonObject = JsonParser.parseString(json);

        getFixture().processUpdate("CommunicationQuality", jsonObject);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_SIGNAL_STRENGTH),
                new DecimalType(0));

        json = """
                {
                    "@type": "communicationQualityState",
                    "quality": "GOOD"
                }
                """;
        jsonObject = JsonParser.parseString(json);

        getFixture().processUpdate("CommunicationQuality", jsonObject);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_SIGNAL_STRENGTH),
                new DecimalType(4));
    }

    @Test
    void testUpdateChannelPowerMeterServiceState() {
        JsonElement jsonObject = JsonParser.parseString("""
                {
                  "@type": "powerMeterState",
                  "powerConsumption": "23",
                  "energyConsumption": 42
                }\
                """);
        getFixture().processUpdate("PowerMeter", jsonObject);

        verify(getCallback()).stateUpdated(eq(getChannelUID(BoschSHCBindingConstants.CHANNEL_POWER_CONSUMPTION)),
                powerCaptor.capture());
        QuantityType<Power> powerValue = powerCaptor.getValue();
        assertEquals(23, powerValue.intValue());

        verify(getCallback()).stateUpdated(eq(getChannelUID(BoschSHCBindingConstants.CHANNEL_ENERGY_CONSUMPTION)),
                energyCaptor.capture());
        QuantityType<Energy> energyValue = energyCaptor.getValue();
        assertEquals(42, energyValue.intValue());
    }
}
