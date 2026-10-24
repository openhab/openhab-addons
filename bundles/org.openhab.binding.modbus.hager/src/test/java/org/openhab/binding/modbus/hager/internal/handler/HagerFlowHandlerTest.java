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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.modbus.hager.internal.HagerFlowRole;
import org.openhab.binding.modbus.hager.internal.modbus.RegisterPoller.PollStatus;
import org.openhab.binding.modbus.handler.ModbusEndpointThingHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.transport.modbus.ModbusCommunicationInterface;
import org.openhab.core.io.transport.modbus.PollTask;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerCallback;

/**
 * The {@link HagerFlowHandlerTest} verifies how the handlers map the poll status and rejected configurations to the
 * Thing status and how they start, restart and stop the polls when the bridge comes and goes.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("null") // Mockito and parts of the openHAB core API are not designed with null annotations in mind
public class HagerFlowHandlerTest {

    /** Upper bound for the poll registration which the handlers schedule asynchronously. */
    private static final long TIMEOUT_MS = 5000;

    /** Time a startup gets to settle after it was released by the test. */
    private static final long STARTUP_SETTLE_MS = 300;

    private static final ThingUID EMS_UID = new ThingUID("modbus", "hager-flow-ems", "ems");
    private static final ThingUID EVSE_UID = new ThingUID("modbus", "hager-flow-evse", "evse1");
    private static final ThingUID MODBUS_BRIDGE_UID = new ThingUID("modbus", "tcp", "flow");

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
        when(bridge.getUID()).thenReturn(EMS_UID);
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

    /**
     * The energy manager sets its status to UNKNOWN before it schedules its own startup. A device initialized in that
     * window has no communication interface yet and must start polling once the energy manager is online.
     */
    @Test
    public void testDeviceStartsPollingWhenEnergyManagerBecomesAvailable() {
        ModbusCommunicationInterface comms = mockComms();
        HagerFlowEmsHandler ems = mock(HagerFlowEmsHandler.class);
        when(ems.getComms()).thenReturn(null);

        Thing thing = deviceThing();
        ThingHandlerCallback callback = deviceCallback(thing, ems);
        HagerFlowDeviceHandler handler = new HagerFlowDeviceHandler(thing, HagerFlowRole.EVSE);
        handler.setCallback(callback);

        handler.initialize();

        verify(callback, timeout(TIMEOUT_MS)).statusUpdated(eq(thing),
                argThat(info -> info.getStatus() == ThingStatus.OFFLINE
                        && info.getStatusDetail() == ThingStatusDetail.BRIDGE_OFFLINE));
        verify(comms, never()).registerRegularPoll(any(), anyLong(), anyLong(), any(), any());

        when(ems.getComms()).thenReturn(comms);
        handler.bridgeStatusChanged(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));

        verify(comms, timeout(TIMEOUT_MS).atLeastOnce()).registerRegularPoll(any(), anyLong(), anyLong(), any(), any());
        verify(callback, never()).statusUpdated(eq(thing), argThat(info -> info.getStatus() == ThingStatus.ONLINE));
    }

    @Test
    public void testDeviceRestartsPollingWhenEnergyManagerConnectionIsReplaced() {
        ModbusCommunicationInterface first = mockComms();
        ModbusCommunicationInterface second = mockComms();
        HagerFlowEmsHandler ems = mock(HagerFlowEmsHandler.class);
        when(ems.getComms()).thenReturn(first);

        Thing thing = deviceThing();
        ThingHandlerCallback callback = deviceCallback(thing, ems);
        HagerFlowDeviceHandler handler = new HagerFlowDeviceHandler(thing, HagerFlowRole.EVSE);
        handler.setCallback(callback);
        handler.initialize();
        verify(first, timeout(TIMEOUT_MS).atLeastOnce()).registerRegularPoll(any(), anyLong(), anyLong(), any(), any());

        when(ems.getComms()).thenReturn(second);
        handler.bridgeStatusChanged(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));

        verify(second, timeout(TIMEOUT_MS).atLeastOnce()).registerRegularPoll(any(), anyLong(), anyLong(), any(),
                any());
        verify(first, timeout(TIMEOUT_MS).atLeastOnce()).unregisterRegularPoll(any());
    }

    @Test
    public void testDeviceStopsPollingWhenBridgeGoesOffline() {
        ModbusCommunicationInterface comms = mockComms();
        HagerFlowEmsHandler ems = mock(HagerFlowEmsHandler.class);
        when(ems.getComms()).thenReturn(comms);

        Thing thing = deviceThing();
        ThingHandlerCallback callback = deviceCallback(thing, ems);
        HagerFlowDeviceHandler handler = new HagerFlowDeviceHandler(thing, HagerFlowRole.EVSE);
        handler.setCallback(callback);
        handler.initialize();
        verify(comms, timeout(TIMEOUT_MS).atLeastOnce()).registerRegularPoll(any(), anyLong(), anyLong(), any(), any());

        handler.bridgeStatusChanged(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.NONE, null));

        verify(comms, timeout(TIMEOUT_MS).atLeastOnce()).unregisterRegularPoll(any());
        verify(callback, timeout(TIMEOUT_MS)).statusUpdated(eq(thing),
                argThat(info -> info.getStatus() == ThingStatus.OFFLINE
                        && info.getStatusDetail() == ThingStatusDetail.BRIDGE_OFFLINE));
    }

    @Test
    public void testDisposeStopsRegisteredPolls() {
        ModbusCommunicationInterface comms = mockComms();
        HagerFlowEmsHandler ems = mock(HagerFlowEmsHandler.class);
        when(ems.getComms()).thenReturn(comms);

        Thing thing = deviceThing();
        ThingHandlerCallback callback = deviceCallback(thing, ems);
        HagerFlowDeviceHandler handler = new HagerFlowDeviceHandler(thing, HagerFlowRole.EVSE);
        handler.setCallback(callback);
        handler.initialize();
        verify(comms, timeout(TIMEOUT_MS).atLeastOnce()).registerRegularPoll(any(), anyLong(), anyLong(), any(), any());

        handler.dispose();

        verify(comms, timeout(TIMEOUT_MS).atLeastOnce()).unregisterRegularPoll(any());
    }

    @Test
    public void testEmsStartsPollingWhenModbusBridgeBecomesAvailable() {
        ModbusCommunicationInterface comms = mockComms();
        ModbusEndpointThingHandler endpoint = mock(ModbusEndpointThingHandler.class,
                withSettings().extraInterfaces(BridgeHandler.class));
        when(endpoint.getCommunicationInterface()).thenReturn(null);
        Bridge modbusBridge = modbusBridge(endpoint);

        Bridge emsThing = mock(Bridge.class);
        when(emsThing.getUID()).thenReturn(EMS_UID);
        when(emsThing.getBridgeUID()).thenReturn(MODBUS_BRIDGE_UID);
        when(emsThing.getConfiguration()).thenReturn(configuration(0, 5000));
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        when(callback.getBridge(MODBUS_BRIDGE_UID)).thenReturn(modbusBridge);

        HagerFlowEmsHandler handler = new HagerFlowEmsHandler(emsThing);
        handler.setCallback(callback);
        handler.initialize();

        verify(callback, timeout(TIMEOUT_MS)).statusUpdated(eq((Thing) emsThing),
                argThat(info -> info.getStatus() == ThingStatus.OFFLINE
                        && info.getStatusDetail() == ThingStatusDetail.BRIDGE_OFFLINE));

        when(endpoint.getCommunicationInterface()).thenReturn(comms);
        handler.bridgeStatusChanged(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));

        verify(comms, timeout(TIMEOUT_MS).atLeastOnce()).registerRegularPoll(any(), anyLong(), anyLong(), any(), any());
    }

    /**
     * A startup which is already running when the Thing is disposed must not register polls afterwards. The latches
     * hold the scheduled startup inside the bridge lookup, so the disposal deterministically happens after the startup
     * began and before it installs its poller - cancelling the queued task alone does not cover this case.
     */
    @Test
    public void testDeviceDisposeWhileStartupIsRunningRegistersNoPoll() throws Exception {
        ModbusCommunicationInterface comms = mockComms();
        CountDownLatch startupEntered = new CountDownLatch(1);
        CountDownLatch releaseStartup = new CountDownLatch(1);
        HagerFlowEmsHandler ems = mock(HagerFlowEmsHandler.class);
        when(ems.getComms()).thenAnswer(invocation -> {
            startupEntered.countDown();
            releaseStartup.await(TIMEOUT_MS, TimeUnit.MILLISECONDS);
            return comms;
        });

        Thing thing = deviceThing();
        HagerFlowDeviceHandler handler = new HagerFlowDeviceHandler(thing, HagerFlowRole.EVSE);
        handler.setCallback(deviceCallback(thing, ems));

        handler.initialize();
        assertTrue(startupEntered.await(TIMEOUT_MS, TimeUnit.MILLISECONDS), "startup did not reach the bridge");

        handler.dispose();
        releaseStartup.countDown();

        verify(comms, after(STARTUP_SETTLE_MS).never()).registerRegularPoll(any(), anyLong(), anyLong(), any(), any());
    }

    @Test
    public void testEmsDisposeWhileStartupIsRunningRegistersNoPoll() throws Exception {
        ModbusCommunicationInterface comms = mockComms();
        CountDownLatch startupEntered = new CountDownLatch(1);
        CountDownLatch releaseStartup = new CountDownLatch(1);
        ModbusEndpointThingHandler endpoint = mock(ModbusEndpointThingHandler.class,
                withSettings().extraInterfaces(BridgeHandler.class));
        when(endpoint.getCommunicationInterface()).thenAnswer(invocation -> {
            startupEntered.countDown();
            releaseStartup.await(TIMEOUT_MS, TimeUnit.MILLISECONDS);
            return comms;
        });
        Bridge modbusBridge = modbusBridge(endpoint);

        Bridge emsThing = mock(Bridge.class);
        when(emsThing.getUID()).thenReturn(EMS_UID);
        when(emsThing.getBridgeUID()).thenReturn(MODBUS_BRIDGE_UID);
        when(emsThing.getConfiguration()).thenReturn(configuration(0, 5000));
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        when(callback.getBridge(MODBUS_BRIDGE_UID)).thenReturn(modbusBridge);

        HagerFlowEmsHandler handler = new HagerFlowEmsHandler(emsThing);
        handler.setCallback(callback);

        handler.initialize();
        assertTrue(startupEntered.await(TIMEOUT_MS, TimeUnit.MILLISECONDS), "startup did not reach the bridge");

        handler.dispose();
        releaseStartup.countDown();

        verify(comms, after(STARTUP_SETTLE_MS).never()).registerRegularPoll(any(), anyLong(), anyLong(), any(), any());
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

    private ModbusCommunicationInterface mockComms() {
        ModbusCommunicationInterface comms = mock(ModbusCommunicationInterface.class);
        when(comms.registerRegularPoll(any(), anyLong(), anyLong(), any(), any()))
                .thenAnswer(invocation -> mock(PollTask.class));
        return comms;
    }

    private Bridge modbusBridge(ModbusEndpointThingHandler endpoint) {
        Bridge bridge = mock(Bridge.class);
        when(bridge.getUID()).thenReturn(MODBUS_BRIDGE_UID);
        when(bridge.getLabel()).thenReturn("Hager Flow TCP");
        doReturn(endpoint).when(bridge).getHandler();
        return bridge;
    }

    private ThingHandlerCallback deviceCallback(Thing thing, HagerFlowEmsHandler ems) {
        Bridge emsBridge = mock(Bridge.class);
        when(emsBridge.getUID()).thenReturn(EMS_UID);
        when(emsBridge.getHandler()).thenReturn(ems);
        when(thing.getBridgeUID()).thenReturn(EMS_UID);
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        when(callback.getBridge(EMS_UID)).thenReturn(emsBridge);
        return callback;
    }

    private Configuration configuration(int slaveId, int refresh) {
        Map<String, Object> config = new HashMap<>();
        config.put("slaveId", slaveId);
        config.put("refresh", refresh);
        return new Configuration(config);
    }

    private Thing deviceThing() {
        Thing thing = mock(Thing.class);
        when(thing.getUID()).thenReturn(EVSE_UID);
        when(thing.getConfiguration()).thenReturn(configuration(1, 5000));
        return thing;
    }
}
