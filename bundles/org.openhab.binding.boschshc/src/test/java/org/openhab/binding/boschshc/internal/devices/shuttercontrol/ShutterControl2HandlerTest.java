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
package org.openhab.binding.boschshc.internal.devices.shuttercontrol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.childprotection.dto.ChildProtectionServiceState;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingTypeUID;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Unit tests for {@link ShutterControl2Handler}
 * 
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
class ShutterControl2HandlerTest extends ShutterControlHandlerTest {

    private @Captor @NonNullByDefault({}) ArgumentCaptor<QuantityType<Power>> powerCaptor;

    private @Captor @NonNullByDefault({}) ArgumentCaptor<QuantityType<Energy>> energyCaptor;

    private @Captor @NonNullByDefault({}) ArgumentCaptor<ChildProtectionServiceState> childProtectionServiceStateCaptor;

    @Override
    protected ShutterControlHandler createFixture() {
        return new ShutterControl2Handler(getThing());
    }

    @Override
    protected ThingTypeUID getThingTypeUID() {
        return BoschSHCBindingConstants.THING_TYPE_SHUTTER_CONTROL_2;
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
    void testUpdateChannelsChildProtectionService() {
        String json = """
                {
                    "@type": "ChildProtectionState",
                    "childLockActive": true
                }
                """;
        JsonElement jsonObject = JsonParser.parseString(json);

        getFixture().processUpdate("ChildProtection", jsonObject);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_CHILD_PROTECTION), OnOffType.ON);
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

    @Test
    void testHandleCommandChildProtection()
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        getFixture().handleCommand(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_CHILD_PROTECTION), OnOffType.ON);
        verify(getBridgeHandler()).putState(eq(getDeviceID()), eq("ChildProtection"),
                childProtectionServiceStateCaptor.capture());
        ChildProtectionServiceState state = childProtectionServiceStateCaptor.getValue();
        assertTrue(state.childLockActive);
    }

    @Test
    void testHandleCommandChildProtectionInvalidCommand()
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        getFixture().handleCommand(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_CHILD_PROTECTION),
                DecimalType.ZERO);
        verify(getBridgeHandler(), times(0)).putState(eq(getDeviceID()), eq("ChildProtection"),
                childProtectionServiceStateCaptor.capture());
    }
}
