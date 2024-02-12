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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.openhab.binding.boschshc.internal.devices.AbstractPowerSwitchHandlerTest;
import org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.childprotection.dto.ChildProtectionServiceState;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingTypeUID;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Unit tests for {@link LightSwitchHandler}.
 * 
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
class LightSwitchHandlerTest extends AbstractPowerSwitchHandlerTest<LightSwitchHandler> {

    private @Captor @NonNullByDefault({}) ArgumentCaptor<ChildProtectionServiceState> childProtectionServiceStateCaptor;

    @Override
    protected LightSwitchHandler createFixture() {
        return new LightSwitchHandler(getThing());
    }

    @Override
    protected ThingTypeUID getThingTypeUID() {
        return BoschSHCBindingConstants.THING_TYPE_LIGHT_SWITCH;
    }

    @Override
    protected String getDeviceID() {
        return "hdm:ZigBee:70ac08fffe5294ea#3";
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
    void testHandleCommandChildProtection()
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        getFixture().handleCommand(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_CHILD_PROTECTION), OnOffType.ON);
        verify(getBridgeHandler()).putState(eq(getDeviceID()), eq("ChildProtection"),
                childProtectionServiceStateCaptor.capture());
        ChildProtectionServiceState state = childProtectionServiceStateCaptor.getValue();
        assertTrue(state.childLockActive);
    }
}
