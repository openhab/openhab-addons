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
package org.openhab.binding.modbus.hager.internal.handler;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.modbus.hager.internal.HagerFlowRole;
import org.openhab.binding.modbus.hager.internal.modbus.RegisterPoller.PollStatus;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;

/**
 * The {@link HagerFlowHandlerTest} verifies how the handlers map the poll status and rejected configurations to the
 * Thing status.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("null") // Mockito and parts of the openHAB core API are not designed with null annotations in mind
public class HagerFlowHandlerTest {

    @Test
    public void testDeviceStatusFollowsPollStatus() {
        Thing thing = deviceThing();
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        HagerFlowDeviceHandler handler = new HagerFlowDeviceHandler(thing, HagerFlowRole.EVSE);
        handler.setCallback(callback);

        handler.onStatus(PollStatus.PENDING);
        verify(callback, never()).statusUpdated(eq(thing), any());

        handler.onStatus(PollStatus.OK);
        verify(callback).statusUpdated(eq(thing), argThat(
                info -> info.getStatus() == ThingStatus.ONLINE && info.getStatusDetail() == ThingStatusDetail.NONE));

        handler.onStatus(PollStatus.FAILED);
        verify(callback).statusUpdated(eq(thing), argThat(info -> info.getStatus() == ThingStatus.OFFLINE
                && info.getStatusDetail() == ThingStatusDetail.COMMUNICATION_ERROR));
    }

    @Test
    public void testEmsStatusFollowsPollStatus() {
        Bridge bridge = mock(Bridge.class);
        when(bridge.getUID()).thenReturn(new ThingUID("modbus", "hager-flow-ems", "ems"));
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        HagerFlowEmsHandler handler = new HagerFlowEmsHandler(bridge);
        handler.setCallback(callback);

        handler.onStatus(PollStatus.OK);
        verify(callback).statusUpdated(eq((Thing) bridge), argThat(info -> info.getStatus() == ThingStatus.ONLINE));

        handler.onStatus(PollStatus.FAILED);
        verify(callback).statusUpdated(eq((Thing) bridge), argThat(info -> info.getStatus() == ThingStatus.OFFLINE
                && info.getStatusDetail() == ThingStatusDetail.COMMUNICATION_ERROR));
    }

    @Test
    public void testSlaveIdOutsideTheRoleRangeIsRejected() {
        Map<String, Object> config = new HashMap<>();
        config.put("slaveId", 9);
        config.put("refresh", 5000);

        verifyConfigurationError(deviceThing(), config);
    }

    @Test
    public void testTooShortRefreshIntervalIsRejected() {
        Map<String, Object> config = new HashMap<>();
        config.put("slaveId", 1);
        config.put("refresh", 999);

        verifyConfigurationError(deviceThing(), config);
    }

    private void verifyConfigurationError(Thing thing, Map<String, Object> config) {
        when(thing.getConfiguration()).thenReturn(new Configuration(config));
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        HagerFlowDeviceHandler handler = new HagerFlowDeviceHandler(thing, HagerFlowRole.EVSE);
        handler.setCallback(callback);

        handler.initialize();

        verify(callback).statusUpdated(eq(thing), argThat(info -> info.getStatus() == ThingStatus.OFFLINE
                && info.getStatusDetail() == ThingStatusDetail.CONFIGURATION_ERROR && info.getDescription() != null));
    }

    private Thing deviceThing() {
        Thing thing = mock(Thing.class);
        when(thing.getUID()).thenReturn(new ThingUID("modbus", "hager-flow-evse", "evse1"));
        return thing;
    }
}
