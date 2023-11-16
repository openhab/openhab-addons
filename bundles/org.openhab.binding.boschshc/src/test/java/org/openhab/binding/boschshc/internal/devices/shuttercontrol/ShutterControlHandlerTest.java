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
package org.openhab.binding.boschshc.internal.devices.shuttercontrol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.openhab.binding.boschshc.internal.devices.AbstractBoschSHCDeviceHandlerTest;
import org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.shuttercontrol.OperationState;
import org.openhab.binding.boschshc.internal.services.shuttercontrol.dto.ShutterControlServiceState;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingTypeUID;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Unit tests for {@link ShutterControlHandler}.
 *
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
class ShutterControlHandlerTest extends AbstractBoschSHCDeviceHandlerTest<ShutterControlHandler> {

    private @Captor @NonNullByDefault({}) ArgumentCaptor<ShutterControlServiceState> shutterControlServiceStateCaptor;

    @Override
    protected String getDeviceID() {
        return "hdm:ZigBee:abcd6fc012ad25b1";
    }

    @Override
    protected ShutterControlHandler createFixture() {
        return new ShutterControlHandler(getThing());
    }

    @Override
    protected ThingTypeUID getThingTypeUID() {
        return BoschSHCBindingConstants.THING_TYPE_SHUTTER_CONTROL;
    }

    @Test
    void testHandleCommandUpDownType()
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        getFixture().handleCommand(new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_LEVEL),
                UpDownType.UP);
        verify(getBridgeHandler()).putState(eq(getDeviceID()), eq("ShutterControl"),
                shutterControlServiceStateCaptor.capture());
        ShutterControlServiceState state = shutterControlServiceStateCaptor.getValue();
        assertEquals(1d, state.level);

        getFixture().handleCommand(new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_LEVEL),
                UpDownType.DOWN);
        verify(getBridgeHandler(), times(2)).putState(eq(getDeviceID()), eq("ShutterControl"),
                shutterControlServiceStateCaptor.capture());
        state = shutterControlServiceStateCaptor.getValue();
        assertEquals(0d, state.level);
    }

    @Test
    void testHandleCommandStopMoveType()
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        getFixture().handleCommand(new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_LEVEL),
                StopMoveType.STOP);
        verify(getBridgeHandler()).putState(eq(getDeviceID()), eq("ShutterControl"),
                shutterControlServiceStateCaptor.capture());
        ShutterControlServiceState state = shutterControlServiceStateCaptor.getValue();
        assertEquals(OperationState.STOPPED, state.operationState);
    }

    @Test
    void testHandleCommandPercentType()
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        getFixture().handleCommand(new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_LEVEL),
                new PercentType(42));
        verify(getBridgeHandler()).putState(eq(getDeviceID()), eq("ShutterControl"),
                shutterControlServiceStateCaptor.capture());
        ShutterControlServiceState state = shutterControlServiceStateCaptor.getValue();
        assertEquals(0.58d, state.level);
    }

    @Test
    void testUpdateChannelsShutterControlService() {
        JsonElement jsonObject = JsonParser
                .parseString("{\n" + "   \"@type\": \"shutterControlState\",\n" + "   \"level\": 0.58\n" + " }");
        getFixture().processUpdate("ShutterControl", jsonObject);
        verify(getCallback()).stateUpdated(new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_LEVEL),
                new PercentType(42));
    }
}
