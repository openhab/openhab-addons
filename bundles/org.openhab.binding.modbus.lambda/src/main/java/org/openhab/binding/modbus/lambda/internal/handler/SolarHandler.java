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

import java.util.Objects;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.modbus.handler.EndpointNotInitializedException;
import org.openhab.binding.modbus.handler.ModbusEndpointThingHandler;
import org.openhab.binding.modbus.lambda.internal.SolarConfiguration;
import org.openhab.binding.modbus.lambda.internal.dto.SolarBlock;
import org.openhab.binding.modbus.lambda.internal.dto.SolarReg50Block;
import org.openhab.binding.modbus.lambda.internal.parser.SolarBlockParser;
import org.openhab.binding.modbus.lambda.internal.parser.SolarReg50BlockParser;
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
 * @author Paul Frank - Initial contribution
 * @author Christian Koch - modified for lambda heat pump based on stiebeleltron binding for modbus
 */
@NonNullByDefault
public class SolarHandler extends BaseThingHandler {

    public abstract class AbstractBasePoller {

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
            SolarConfiguration myconfig = Objects.requireNonNull(SolarHandler.this.config);

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
    private final SolarReg50BlockParser solarReg50BlockParser = new SolarReg50BlockParser();

    private volatile @Nullable AbstractBasePoller solarPoller = null;
    private volatile @Nullable AbstractBasePoller solarReg50Poller = null;

    protected volatile @Nullable ModbusCommunicationInterface comms = null;

    private volatile int slaveId;

    public SolarHandler(Thing thing) {
        super(thing);
    }

    /**
     * @param command get the value of this command.
     * 
     * @return short the value of the command as short
     */

    /**
     * @param address address of the value to be written on the modbus
     * 
     * @param shortValue value to be written on the modbus
     */
    protected void writeInt16(int address, short shortValue) {
        SolarConfiguration myconfig = Objects.requireNonNull(SolarHandler.this.config);
        ModbusCommunicationInterface mycomms = SolarHandler.this.comms;

        if (myconfig == null || mycomms == null) {
            throw new IllegalStateException("registerPollTask called without proper configuration");
        }
        // big endian byte ordering
        byte hi = (byte) (shortValue >> 8);
        byte lo = (byte) shortValue;
        ModbusRegisterArray data = new ModbusRegisterArray(hi, lo);

        ModbusWriteRegisterRequestBlueprint request = new ModbusWriteRegisterRequestBlueprint(slaveId, address, data,
                true, myconfig.getMaxTries());

        mycomms.submitOneTimeWrite(request, result -> {
            if (hasConfigurationError()) {
                return;
            }
            SolarHandler.this.updateStatus(ThingStatus.ONLINE);
        }, failure -> {
            SolarHandler.this.handleWriteError(failure);
        });
    }

    private short getScaledInt16Value(Command command) throws LambdaException {
        if (command instanceof QuantityType<?> quantityCommand) {
            QuantityType<?> c = quantityCommand.toUnit(CELSIUS);
            if (c != null) {
                return (short) (c.doubleValue() * 10);
            } else {
                throw new LambdaException("Unsupported unit");
            }
        }
        if (command instanceof DecimalType c) {
            return (short) (c.doubleValue() * 10);
        }
        throw new LambdaException("Unsupported command type");
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
                    case GROUP_SOLAR_REG50:
                        poller = solarReg50Poller;
                        break;
                    default:
                        poller = null;
                        break;
                }
                if (poller != null) {
                    logger.trace("Solar: Polling initiated");
                    poller.poll();
                }
            }
        } else {
            try {
                if (GROUP_SOLAR_REG50.equals(channelUID.getGroupId())) {
                    switch (channelUID.getIdWithoutGroup()) {
                        case CHANNEL_SOLAR_MAXIMUM_BUFFER_TEMPERATURE:
                            writeInt16(reg50baseadress, getScaledInt16Value(command));
                            break;
                        case CHANNEL_SOLAR_BUFFER_CHANGEOVER_TEMPERATURE:
                            writeInt16(reg50baseadress + 1, getScaledInt16Value(command));
                            break;
                    }
                }
            } catch (LambdaException error) {
                if (hasConfigurationError() || getThing().getStatus() == ThingStatus.OFFLINE) {
                    return;
                }
                String cls = error.getClass().getName();
                String msg = error.getMessage();

                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        String.format("Error with: %s: %s", cls, msg));
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
    private int reg50baseadress;

    private void startUp() {
        if (comms != null) {
            return;
        }

        ModbusEndpointThingHandler slaveEndpointThingHandler = getEndpointThingHandler();
        if (slaveEndpointThingHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
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

        SolarConfiguration myconfig = Objects.requireNonNull(SolarHandler.this.config);

        // Base address for solar is 4000 as mentioned in README.md
        baseadress = 4000 + 100 * myconfig.getSubindex();
        reg50baseadress = baseadress + 50;

        if (solarPoller == null) {
            AbstractBasePoller poller = new AbstractBasePoller() {
                @Override
                protected void handlePolledData(ModbusRegisterArray registers) {
                    handlePolledSolarData(registers);
                }
            };

            poller.registerPollTask(baseadress, 5, ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS);
            solarPoller = poller;
        }
        if (solarReg50Poller == null) {
            AbstractBasePoller poller = new AbstractBasePoller() {
                @Override
                protected void handlePolledData(ModbusRegisterArray registers) {
                    handlePolledSolarReg50Data(registers);
                }
            };

            poller.registerPollTask(reg50baseadress, 2, ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS);
            solarReg50Poller = poller;
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

        poller = solarReg50Poller;
        if (poller != null) {
            poller.unregisterPollTask();
            solarReg50Poller = null;
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
        updateState(channelUID(GROUP_SOLAR, CHANNEL_SOLAR_ERROR_NUMBER), new DecimalType(block.solarErrorNumber));
        updateState(channelUID(GROUP_SOLAR, CHANNEL_SOLAR_OPERATING_STATE), new DecimalType(block.solarOperatingState));
        updateState(channelUID(GROUP_SOLAR, CHANNEL_SOLAR_COLLECTOR_TEMPERATURE),
                getScaled(block.solarCollectorTemperature, CELSIUS, -1.0));
        updateState(channelUID(GROUP_SOLAR, CHANNEL_SOLAR_BUFFER1_TEMPERATURE),
                getScaled(block.solarBuffer1Temperature, CELSIUS, -1.0));
        updateState(channelUID(GROUP_SOLAR, CHANNEL_SOLAR_BUFFER2_TEMPERATURE),
                getScaled(block.solarBuffer2Temperature, CELSIUS, -1.0));

        resetCommunicationError();
    }

    protected void handlePolledSolarReg50Data(ModbusRegisterArray registers) {
        SolarReg50Block block = solarReg50BlockParser.parse(registers);

        updateState(channelUID(GROUP_SOLAR_REG50, CHANNEL_SOLAR_MAXIMUM_BUFFER_TEMPERATURE),
                getScaled(block.solarMaximumBufferTemperature, CELSIUS, -1.0));
        updateState(channelUID(GROUP_SOLAR_REG50, CHANNEL_SOLAR_BUFFER_CHANGEOVER_TEMPERATURE),
                getScaled(block.solarBufferChangeoverTemperature, CELSIUS, -1.0));

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

    /**
     * Handle errors received during communication
     */
    protected void handleWriteError(AsyncModbusFailure<ModbusWriteRequestBlueprint> failure) {
        // Ignore all incoming data and errors if configuration is not correct
        if (hasConfigurationError() || getThing().getStatus() == ThingStatus.OFFLINE) {
            return;
        }
        String msg = failure.getCause().getMessage();
        String cls = failure.getCause().getClass().getName();
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                String.format("Error with write: %s: %s", cls, msg));
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
