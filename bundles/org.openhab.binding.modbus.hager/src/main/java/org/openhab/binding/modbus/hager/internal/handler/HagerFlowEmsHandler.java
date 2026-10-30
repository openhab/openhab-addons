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

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.modbus.hager.internal.HagerFlowBindingConstants;
import org.openhab.binding.modbus.hager.internal.HagerFlowRole;
import org.openhab.binding.modbus.hager.internal.config.HagerFlowConfiguration;
import org.openhab.binding.modbus.hager.internal.modbus.ChannelUpdate;
import org.openhab.binding.modbus.hager.internal.modbus.ModbusRegisterTypes.Register;
import org.openhab.binding.modbus.hager.internal.modbus.RegisterPoller;
import org.openhab.binding.modbus.handler.ModbusEndpointThingHandler;
import org.openhab.core.io.transport.modbus.ModbusCommunicationInterface;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HagerFlowEmsHandler} reads the registers of the Hager Flow energy manager. It also lends the Modbus
 * communication interface to the Things of the other roles, which use their own unit ids on the same connection.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class HagerFlowEmsHandler extends BaseBridgeHandler implements RegisterPoller.Listener {

    private static final Logger LOGGER = LoggerFactory.getLogger(HagerFlowEmsHandler.class);

    private static final String NO_BRIDGE = "No Modbus bridge configured";
    private static final String NOT_A_MODBUS_BRIDGE = "Bridge is not a Modbus endpoint";
    private static final String BRIDGE_NOT_INITIALIZED = "Bridge '%s' is not initialized";
    private static final String BRIDGE_OFFLINE = "Modbus bridge is offline";

    private final HagerFlowRole role = HagerFlowRole.EMS;
    private final HagerFlowChannels channels;
    private final Object lifecycleLock = new Object();

    // guarded by lifecycleLock
    private @Nullable RegisterPoller poller;
    private @Nullable ScheduledFuture<?> startupTask;
    private long generation;

    private volatile @Nullable HagerFlowConfiguration config;

    public HagerFlowEmsHandler(Bridge thing) {
        super(thing);
        channels = new HagerFlowChannels(thing, role.registers(), this::updateState);
    }

    /**
     * @return the Modbus communication interface of the bridge this Thing is attached to; it is read from the bridge on
     *         every call, so a replaced bridge connection is never served from a stale cache
     */
    public @Nullable ModbusCommunicationInterface getComms() {
        Bridge bridge = getBridge();
        ThingHandler handler = bridge == null ? null : bridge.getHandler();
        return handler instanceof ModbusEndpointThingHandler endpoint ? endpoint.getCommunicationInterface() : null;
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        HagerFlowConfiguration currentConfig = getConfigAs(HagerFlowConfiguration.class);
        LOGGER.debug("[{}] Initializing {} with slave id {} and refresh {} ms", thing.getUID(), role.thingType(),
                currentConfig.slaveId, currentConfig.refresh);
        Optional<String> problem = role.validate(currentConfig.slaveId, currentConfig.refresh);
        if (problem.isPresent()) {
            LOGGER.debug("[{}] Rejected configuration: {}", thing.getUID(), problem.get());
            updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, problem.get());
            return;
        }
        config = currentConfig;
        restart(currentConfig);
    }

    /**
     * Restarts the polls from scratch: the queued startup of a previous run is invalidated, the running polls are
     * stopped and the Modbus connection is acquired again. Called on initialization and whenever the bridge becomes
     * available, so a startup before the bridge was ready is repeated instead of leaving the Thing without polls.
     *
     * @param currentConfig the current Thing configuration
     */
    void restart(HagerFlowConfiguration currentConfig) {
        RegisterPoller previous;
        long expectedGeneration;
        synchronized (lifecycleLock) {
            expectedGeneration = ++generation;
            cancelStartupTask();
            previous = poller;
            poller = null;
            // scheduled while holding the lock, so a concurrent dispose cancels it or invalidates its generation
            startupTask = scheduler.schedule(() -> start(expectedGeneration, currentConfig), 0, TimeUnit.MILLISECONDS);
        }
        if (previous != null) {
            previous.stop();
        }
    }

    private void start(long expectedGeneration, HagerFlowConfiguration currentConfig) {
        synchronized (lifecycleLock) {
            if (generation != expectedGeneration) {
                LOGGER.debug("[{}] Discarding startup of obsolete generation {}", thing.getUID(), expectedGeneration);
                return;
            }
        }
        ModbusCommunicationInterface communicationInterface = connectEndpoint();
        if (communicationInterface == null) {
            // connectEndpoint reported the reason; the next bridge status change triggers another attempt
            return;
        }
        RegisterPoller newPoller = new RegisterPoller(thing.getUID().toString(), currentConfig.slaveId,
                currentConfig.refresh, role.registers(), this);
        synchronized (lifecycleLock) {
            if (generation != expectedGeneration) {
                LOGGER.debug("[{}] Discarding startup of obsolete generation {}", thing.getUID(), expectedGeneration);
                return;
            }
            poller = newPoller;
        }
        // started outside the lock: a dispose which already stopped this poller makes the start a no-op instead of
        // leaving polls registered behind an invalidated handler
        newPoller.start(communicationInterface);
    }

    private @Nullable ModbusCommunicationInterface connectEndpoint() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatusDetail.BRIDGE_UNINITIALIZED, NO_BRIDGE);
            LOGGER.debug("[{}] {}", thing.getUID(), NO_BRIDGE);
            return null;
        }
        ThingHandler handler = bridge.getHandler();
        if (!(handler instanceof ModbusEndpointThingHandler endpoint)) {
            updateStatus(ThingStatusDetail.BRIDGE_UNINITIALIZED, NOT_A_MODBUS_BRIDGE);
            LOGGER.debug("[{}] {}: {}", thing.getUID(), NOT_A_MODBUS_BRIDGE, bridge.getUID());
            return null;
        }
        ModbusCommunicationInterface communicationInterface = endpoint.getCommunicationInterface();
        if (communicationInterface == null) {
            updateStatus(ThingStatusDetail.BRIDGE_OFFLINE, String.format(BRIDGE_NOT_INITIALIZED, bridge.getLabel()));
            LOGGER.debug("[{}] Bridge {} is not initialized yet", thing.getUID(), bridge.getUID());
            return null;
        }
        LOGGER.debug("[{}] Using Modbus bridge {}", thing.getUID(), bridge.getUID());
        return communicationInterface;
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        HagerFlowConfiguration currentConfig = config;
        if (currentConfig == null) {
            return;
        }
        switch (bridgeStatusInfo.getStatus()) {
            case ONLINE -> restart(currentConfig);
            case OFFLINE, UNINITIALIZED -> {
                stopPolling();
                updateStatus(ThingStatusDetail.BRIDGE_OFFLINE, BRIDGE_OFFLINE);
            }
            default -> LOGGER.debug("[{}] Ignoring bridge status {}", thing.getUID(), bridgeStatusInfo.getStatus());
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // all registers of the energy manager are read only; refreshing reads the block of the channel immediately
        if (command instanceof RefreshType) {
            RegisterPoller current = currentPoller();
            Optional<Register> register = channels.register(channelUID.getIdWithoutGroup());
            if (current == null || register.isEmpty()) {
                LOGGER.debug("Cannot refresh channel {}", channelUID);
                return;
            }
            current.refresh(register.get());
        }
    }

    @Override
    public void onUpdates(List<ChannelUpdate> updates) {
        channels.apply(updates);
    }

    @Override
    public void onStatus(RegisterPoller.PollStatus status) {
        switch (status) {
            case OK -> updateStatus(ThingStatus.ONLINE);
            case FAILED -> updateStatus(ThingStatusDetail.COMMUNICATION_ERROR, HagerFlowBindingConstants.READ_ERROR);
            case PENDING -> LOGGER.debug("Waiting for the first Modbus response");
        }
    }

    @Override
    public void dispose() {
        config = null;
        stopPolling();
        super.dispose();
    }

    private void stopPolling() {
        RegisterPoller current;
        synchronized (lifecycleLock) {
            generation++;
            cancelStartupTask();
            current = poller;
            poller = null;
        }
        if (current != null) {
            current.stop();
        }
    }

    private @Nullable RegisterPoller currentPoller() {
        synchronized (lifecycleLock) {
            return poller;
        }
    }

    /**
     * Cancels a startup task which was scheduled but did not start yet. Must be called while holding
     * {@link #lifecycleLock}.
     */
    private void cancelStartupTask() {
        ScheduledFuture<?> task = startupTask;
        startupTask = null;
        if (task != null) {
            task.cancel(false);
        }
    }
}
