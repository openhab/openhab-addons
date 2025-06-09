/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.modbus.lambda.internal.handler;

import static org.openhab.binding.modbus.lambda.internal.LambdaBindingConstants.*;
import static org.openhab.core.library.unit.SIUnits.CELSIUS;
import static org.openhab.core.library.unit.Units.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.modbus.handler.EndpointNotInitializedException;
import org.openhab.binding.modbus.handler.ModbusEndpointThingHandler;
import org.openhab.binding.modbus.lambda.internal.SolarConfiguration;
import org.openhab.binding.modbus.lambda.internal.dto.SolarBlock;
import org.openhab.binding.modbus.lambda.internal.parser.SolarBlockParser;
import org.openhab.core.io.transport.modbus.AsyncModbusFailure;
import org.openhab.core.io.transport.modbus.ModbusCommunicationInterface;
import org.openhab.core.io.transport.modbus.ModbusReadFunctionCode;
import org.openhab.core.io.transport.modbus.ModbusReadRequestBlueprint;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;
import org.openhab.core.io.transport.modbus.ModbusWriteRegisterRequestBlueprint;
import org.openhab.core.io.transport.modbus.ModbusWriteRequestBlueprint;
import org.openhab.core.io.transport.modbus.PollTask;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
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
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SolarHandler} is responsible for handling commands,
 * which are sent to one of the channels and for polling the modbus
 * for solar thermic component data.
 *
 * @author Christian Koch - christian@koch-bensheim.de - Initial contribution
 */
@NonNullByDefault
public class SolarHandler extends BaseThingHandler {

    public abstract class AbstractBasePoller {

        private final Logger logger = LoggerFactory.getLogger(SolarHandler.class);

        private volatile @Nullable PollTask pollTask;

        public synchronized void unregisterPollTask() {
            PollTask task = pollTask;
            if (task == null) {
                return;
            }

            ModbusCommunicationInterface mycomms = SolarHandler.this.comms;
            if (mycomms != null) {
                mycomms.unregisterRegularPoll(task);
            }
            pollTask = null;
        }

        public synchronized void registerPollTask(int address, int length, ModbusReadFunctionCode readFunctionCode) {
            ModbusCommunicationInterface mycomms = SolarHandler.this.comms;
            SolarConfiguration myconfig = SolarHandler.this.config;

            if (myconfig == null || mycomms == null) {
                throw new IllegalStateException("Solar: registerPollTask called without proper configuration");
            }

            ModbusReadRequestBlueprint request = new ModbusReadRequestBlueprint(getSlaveId(), readFunctionCode, address,
                    length, myconfig.getMaxTries());

            long refreshMillis = myconfig.getRefreshMillis();

            pollTask = mycomms.registerRegularPoll(request, refreshMillis, 1000, result -> {
                result.getRegisters().ifPresent(this::handlePolledData);
                if (getThing().getStatus() != ThingStatus.ONLINE) {
                    updateStatus(ThingStatus.ONLINE);
                }
            }, SolarHandler.this::handleReadError);
        }

        public synchronized void poll() {
            PollTask task = pollTask;
            ModbusCommunicationInterface mycomms = SolarHandler.this.comms;
            if (task != null && mycomms != null) {
                mycomms.submitOneTimePoll(task.getRequest(), task.getResultCallback(), task.getFailureCallback());
            }
        }

        protected abstract void handlePolledData(ModbusRegisterArray registers);
    }

    private final Logger logger = LoggerFactory.getLogger(SolarHandler.class);

    protected @Nullable SolarConfiguration config = null;

    private final SolarBlockParser solarBlockParser = new SolarBlockParser();

    private volatile @Nullable AbstractBasePoller solarPoller = null;

    protected volatile @Nullable ModbusCommunicationInterface comms = null;

    private volatile int slaveId;

    public SolarHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (RefreshType.REFRESH == command) {
            String groupId = channelUID.getGroupId();
            if (groupId != null) {
                AbstractBasePoller poller;
                switch (groupId) {
                    case GROUP_SOLAR:
                        poller = solarPoller;
                        break;
                    default:
                        poller = null;
                        break;
                }
                if (poller != null) {
                    poller.poll();
                }
            }
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(SolarConfiguration.class);

        logger.debug("Initializing solar thing with properties: {}", thing.getProperties());

        startUp();
    }

    private int baseadress;

    private void startUp() {
        if (comms != null) {
            return;
        }

        ModbusEndpointThingHandler slaveEndpointThingHandler = getEndpointThingHandler();
        if (slaveEndpointThingHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Bridge is offline");
            return;
        }

        try {
            slaveId = slaveEndpointThingHandler.getSlaveId();
            comms = slaveEndpointThingHandler.getCommunicationInterface();
        } catch (EndpointNotInitializedException e) {
            // this will be handled below as endpoint remains null
        }

        if (comms == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            logger.debug("CommunicationInterface of Solar is null, Thing & Bridge are offline");
            return;
        }

        if (config == null) {
            logger.debug("Invalid comms/config/manager ref for lambda solar handler");
            return;
        }

        SolarConfiguration myconfig = SolarHandler.this.config;

        // Base address for solar is 4000 as mentioned in README.md
        baseadress = 4000 + 100 * myconfig.getSubindex();

        if (solarPoller == null) {
            AbstractBasePoller poller = new AbstractBasePoller() {
                @Override
                protected void handlePolledData(ModbusRegisterArray registers) {
                    handlePolledSolarData(registers);
                }
            };

            // Register length needs to be determined from the modbus description document
            poller.registerPollTask(baseadress, 20, ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS);
            solarPoller = poller;
        }

        updateStatus(ThingStatus.UNKNOWN);
    }

    @Override
    public void dispose() {
        tearDown();
    }

    private void tearDown() {
        AbstractBasePoller poller = solarPoller;
        if (poller != null) {
            poller.unregisterPollTask();
            solarPoller = null;
        }

        comms = null;
    }

    public int getSlaveId() {
        return slaveId;
    }

    private @Nullable ModbusEndpointThingHandler getEndpointThingHandler() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            logger.debug("Bridge is null");
            return null;
        }
        if (bridge.getStatus() != ThingStatus.ONLINE) {
            logger.debug("Bridge is not online");
            return null;
        }

        ThingHandler handler = bridge.getHandler();
        if (handler == null) {
            logger.debug("Bridge handler is null");
            return null;
        }
        if (handler instanceof ModbusEndpointThingHandler thingHandler) {
            return thingHandler;
        } else {
            throw new IllegalStateException("Unexpected bridge handler: " + handler.toString());
        }
    }

    protected State getScaled(Number value, Unit<?> unit, Double pow) {
        double factor = Math.pow(10, pow);
        return QuantityType.valueOf(value.doubleValue() * factor, unit);
    }

    protected void handlePolledSolarData(ModbusRegisterArray registers) {
        SolarBlock block = solarBlockParser.parse(registers);

        // Update solar channels
        updateState(channelUID(GROUP_SOLAR, CHANNEL_SOLAR_ERROR_NUMBER),
                new DecimalType(block.solarErrorNumber));
        updateState(channelUID(GROUP_SOLAR, CHANNEL_SOLAR_OPERATING_STATE),
                new DecimalType(block.solarOperatingState));
        updateState(channelUID(GROUP_SOLAR, CHANNEL_SOLAR_COLLECTOR_TEMPERATURE),
                getScaled(block.solarCollectorTemperature, CELSIUS, -1.0));
        updateState(channelUID(GROUP_SOLAR, CHANNEL_SOLAR_STORAGE_TEMPERATURE),
                getScaled(block.solarStorageTemperature, CELSIUS, -1.0));
        updateState(channelUID(GROUP_SOLAR, CHANNEL_SOLAR_PUMP_SPEED),
                new DecimalType(block.solarPumpSpeed));
        updateState(channelUID(GROUP_SOLAR, CHANNEL_SOLAR_HEAT_QUANTITY),
                getScaled(block.solarHeatQuantity, KILOWATT_HOUR, -1.0));
        updateState(channelUID(GROUP_SOLAR, CHANNEL_SOLAR_POWER_OUTPUT),
                getScaled(block.solarPowerOutput, WATT, 0.0));
        updateState(channelUID(GROUP_SOLAR, CHANNEL_SOLAR_OPERATING_HOURS),
                new DecimalType(block.solarOperatingHours));

        resetCommunicationError();
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        super.bridgeStatusChanged(bridgeStatusInfo);

        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            startUp();
        } else if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            tearDown();
        }
    }

    protected void handleReadError(AsyncModbusFailure<ModbusReadRequestBlueprint> failure) {
        if (hasConfigurationError() || getThing().getStatus() == ThingStatus.OFFLINE) {
            return;
        }
        String msg = failure.getCause().getMessage();
        String cls = failure.getCause().getClass().getName();
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                String.format("Error with read: %s: %s", cls, msg));
    }

    protected boolean hasConfigurationError() {
        ThingStatusInfo statusInfo = getThing().getStatusInfo();
        return statusInfo.getStatus() == ThingStatus.OFFLINE
                && statusInfo.getStatusDetail() == ThingStatusDetail.CONFIGURATION_ERROR;
    }

    protected void resetCommunicationError() {
        ThingStatusInfo statusInfo = thing.getStatusInfo();
        if (ThingStatus.OFFLINE.equals(statusInfo.getStatus())
                && ThingStatusDetail.COMMUNICATION_ERROR.equals(statusInfo.getStatusDetail())) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    ChannelUID channelUID(String group, String id) {
        return new ChannelUID(getThing().getUID(), group, id);
    }
}