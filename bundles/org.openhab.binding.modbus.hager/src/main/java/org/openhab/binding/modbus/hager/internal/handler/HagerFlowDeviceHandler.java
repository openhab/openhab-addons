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

    private final HagerFlowRole role;
    private final HagerFlowChannels channels;

    private volatile @Nullable ModbusCommunicationInterface comms;
    private @Nullable RegisterPoller poller;

    public HagerFlowDeviceHandler(Thing thing, HagerFlowRole role) {
        super(thing);
        this.role = role;
        channels = new HagerFlowChannels(thing, role.registers(), this::updateState);
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        HagerFlowConfiguration config = getConfigAs(HagerFlowConfiguration.class);
        LOGGER.debug("[{}] Initializing {} with slave id {} and refresh {} ms", thing.getUID(), role.thingType(),
                config.slaveId, config.refresh);
        Optional<String> problem = role.validate(config.slaveId, config.refresh);
        if (problem.isPresent()) {
            LOGGER.debug("[{}] Rejected configuration: {}", thing.getUID(), problem.get());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, problem.get());
            return;
        }
        scheduler.execute(() -> start(config));
    }

    private void start(HagerFlowConfiguration config) {
        ModbusCommunicationInterface communicationInterface = connectEndpoint();
        if (communicationInterface == null) {
            return;
        }
        RegisterPoller newPoller = new RegisterPoller(thing.getUID().toString(), config.slaveId, config.refresh,
                role.registers(), this);
        RegisterPoller previous;
        synchronized (this) {
            previous = poller;
            poller = newPoller;
        }
        if (previous != null) {
            previous.stop();
        }
        newPoller.start(communicationInterface);
    }

    private @Nullable ModbusCommunicationInterface connectEndpoint() {
        ModbusCommunicationInterface existing = comms;
        if (existing != null) {
            return existing;
        }
        Bridge bridge = getBridge();
        ThingHandler handler = bridge == null ? null : bridge.getHandler();
        if (!(handler instanceof HagerFlowEmsHandler ems)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED, BRIDGE_NOT_INITIALIZED);
            LOGGER.debug("[{}] Bridge {} is not an energy manager, {}: {}", thing.getUID(),
                    bridge == null ? "<null>" : bridge.getUID(), BRIDGE_NOT_INITIALIZED,
                    handler == null ? "<null>" : handler.getClass().getSimpleName());
            return null;
        }
        ModbusCommunicationInterface communicationInterface = ems.getComms();
        if (communicationInterface == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, BRIDGE_NOT_INITIALIZED);
            LOGGER.debug("[{}] Energy manager {} has no communication interface yet", thing.getUID(),
                    bridge == null ? "<null>" : bridge.getUID());
            return null;
        }
        LOGGER.debug("[{}] Using Modbus connection of energy manager {}", thing.getUID(),
                bridge == null ? "<null>" : bridge.getUID());
        comms = communicationInterface;
        return communicationInterface;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channelId = channelUID.getIdWithoutGroup();
        Optional<Register> register = channels.register(channelId);
        if (register.isEmpty()) {
            LOGGER.debug("[{}] Unknown channel {}, command {} ignored", thing.getUID(), channelUID, command);
            return;
        }
        RegisterPoller current = poller;
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
            case FAILED -> updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    HagerFlowBindingConstants.READ_ERROR);
            case PENDING -> LOGGER.debug("Waiting for the first Modbus response");
        }
    }

    @Override
    public void dispose() {
        RegisterPoller current;
        synchronized (this) {
            current = poller;
            poller = null;
        }
        if (current != null) {
            current.stop();
        }
        // the communication interface is owned by the endpoint Thing handler
        comms = null;
        super.dispose();
    }
}
