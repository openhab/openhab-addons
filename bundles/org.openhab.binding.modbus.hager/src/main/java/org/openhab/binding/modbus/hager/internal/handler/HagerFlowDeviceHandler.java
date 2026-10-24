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
import org.openhab.binding.modbus.hager.internal.modbus.ModbusRegisterTypes.Access;
import org.openhab.binding.modbus.hager.internal.modbus.ModbusRegisterTypes.Register;
import org.openhab.binding.modbus.hager.internal.modbus.RegisterPoller;
import org.openhab.core.io.transport.modbus.ModbusCommunicationInterface;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HagerFlowDeviceHandler} serves all Hager Flow roles which are subordinate to the energy manager. Every
 * role reads its own Modbus unit id, so each of them is a Thing of its own.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class HagerFlowDeviceHandler extends BaseThingHandler implements RegisterPoller.Listener {

    private static final Logger LOGGER = LoggerFactory.getLogger(HagerFlowDeviceHandler.class);

    private static final String BRIDGE_NOT_INITIALIZED = "Communication interface of the energy manager is not available";
    private static final String BRIDGE_OFFLINE = "Energy manager is offline";

    private final HagerFlowRole role;
    private final HagerFlowChannels channels;
    private final Object lifecycleLock = new Object();

    // guarded by lifecycleLock
    private @Nullable RegisterPoller poller;
    private @Nullable ScheduledFuture<?> startupTask;
    private long generation;

    private volatile @Nullable HagerFlowConfiguration config;

    public HagerFlowDeviceHandler(Thing thing, HagerFlowRole role) {
        super(thing);
        this.role = role;
        channels = new HagerFlowChannels(thing, role.registers(), this::updateState);
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
     * stopped and the connection of the energy manager is acquired again. Called on initialization and whenever the
     * bridge becomes available, so a startup before the energy manager provided its communication interface is
     * repeated instead of leaving the Thing without polls.
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
        ThingHandler handler = bridge == null ? null : bridge.getHandler();
        if (!(handler instanceof HagerFlowEmsHandler ems)) {
            updateStatus(ThingStatusDetail.BRIDGE_UNINITIALIZED, BRIDGE_NOT_INITIALIZED);
            LOGGER.debug("[{}] Bridge {} is not an energy manager, {}: {}", thing.getUID(),
                    bridge == null ? "<null>" : bridge.getUID(), BRIDGE_NOT_INITIALIZED,
                    handler == null ? "<null>" : handler.getClass().getSimpleName());
            return null;
        }
        ModbusCommunicationInterface communicationInterface = ems.getComms();
        if (communicationInterface == null) {
            updateStatus(ThingStatusDetail.BRIDGE_OFFLINE, BRIDGE_NOT_INITIALIZED);
            LOGGER.debug("[{}] Energy manager {} has no communication interface yet", thing.getUID(),
                    bridge == null ? "<null>" : bridge.getUID());
            return null;
        }
        LOGGER.debug("[{}] Using Modbus connection of energy manager {}", thing.getUID(),
                bridge == null ? "<null>" : bridge.getUID());
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
        String channelId = channelUID.getIdWithoutGroup();
        Optional<Register> register = channels.register(channelId);
        if (register.isEmpty()) {
            LOGGER.debug("[{}] Unknown channel {}, command {} ignored", thing.getUID(), channelUID, command);
            return;
        }
        RegisterPoller current = currentPoller();
        if (current == null) {
            LOGGER.debug("[{}] Not handling {} for {}, polls are not running", thing.getUID(), command, channelUID);
            return;
        }
        if (command instanceof RefreshType) {
            current.refresh(register.get());
            return;
        }
        Register writable = register.get();
        if (writable.access() != Access.READ_WRITE) {
            LOGGER.debug("[{}] Register {} is read only, command {} ignored", thing.getUID(), writable.channelId(),
                    command);
            return;
        }
        if (command instanceof OnOffType onOff) {
            current.write(writable,
                    OnOffType.ON.equals(onOff) ? HagerFlowBindingConstants.ON : HagerFlowBindingConstants.OFF);
        } else {
            LOGGER.debug("[{}] Command {} is not supported for channel {}", thing.getUID(), command, channelId);
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
