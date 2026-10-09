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
import org.openhab.binding.modbus.hager.internal.modbus.ModbusRegisterTypes.Register;
import org.openhab.binding.modbus.hager.internal.modbus.RegisterPoller;
import org.openhab.binding.modbus.handler.ModbusEndpointThingHandler;
import org.openhab.core.io.transport.modbus.ModbusCommunicationInterface;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
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

    private final HagerFlowRole role = HagerFlowRole.EMS;
    private final HagerFlowChannels channels;

    private volatile @Nullable ModbusCommunicationInterface comms;
    private @Nullable RegisterPoller poller;

    public HagerFlowEmsHandler(Bridge thing) {
        super(thing);
        channels = new HagerFlowChannels(thing, role.registers(), this::updateState);
    }

    /**
     * @return the Modbus communication interface of the bridge this Thing is attached to
     */
    public @Nullable ModbusCommunicationInterface getComms() {
        return comms;
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
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED, NO_BRIDGE);
            LOGGER.debug("[{}] {}", thing.getUID(), NO_BRIDGE);
            return null;
        }
        ThingHandler handler = bridge.getHandler();
        if (!(handler instanceof ModbusEndpointThingHandler endpoint)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED, NOT_A_MODBUS_BRIDGE);
            LOGGER.debug("[{}] {}: {}", thing.getUID(), NOT_A_MODBUS_BRIDGE, bridge.getUID());
            return null;
        }
        ModbusCommunicationInterface communicationInterface = endpoint.getCommunicationInterface();
        if (communicationInterface == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                    String.format(BRIDGE_NOT_INITIALIZED, bridge.getLabel()));
            LOGGER.debug("[{}] Bridge {} is not initialized yet", thing.getUID(), bridge.getUID());
            return null;
        }
        LOGGER.debug("[{}] Using Modbus bridge {}", thing.getUID(), bridge.getUID());
        comms = communicationInterface;
        return communicationInterface;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // all registers of the energy manager are read only; refreshing reads the block of the channel immediately
        if (command instanceof RefreshType) {
            RegisterPoller current = poller;
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
