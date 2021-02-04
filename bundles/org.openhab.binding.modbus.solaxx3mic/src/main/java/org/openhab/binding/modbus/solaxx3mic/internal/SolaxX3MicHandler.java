/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.modbus.solaxx3mic.internal;


import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Optional;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.modbus.handler.BaseModbusThingHandler;
import org.openhab.core.io.transport.modbus.AsyncModbusFailure;
import org.openhab.core.io.transport.modbus.ModbusReadFunctionCode;
import org.openhab.core.io.transport.modbus.ModbusReadRequestBlueprint;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SolaxX3MicHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Stanislaw Wawszczak - Initial contribution
 */
@NonNullByDefault
public class SolaxX3MicHandler extends BaseModbusThingHandler {

    /**
     * Logger instance
     */
    private final Logger logger = LoggerFactory.getLogger(SolaxX3MicHandler.class);

    /**
     * Configuration instance
     */
    private @Nullable SolaxX3MicConfiguration config;


    public SolaxX3MicHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Currently we do not support any commands
    }

    @Override
    public void modbusInitialize() {
        config = getConfigAs(SolaxX3MicConfiguration.class);
        logger.debug("Initializing thing with properties: {} and config {}", thing.getProperties(), config.toString());

        // Try properties first
        @Nullable
        RegisterBlock inputBlock = getRegisterBlockFromConfig(RegisterBlockFunction.INPUT_REGISTER_BLOCK);

        if (inputBlock != null) {
            updateStatus(ThingStatus.UNKNOWN);
            registerPollTask(inputBlock);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Solax X3 Mic item should have the address and length configuration set");
            return;
        }
    }

    /**
     * Load configuration from main configuration
     */
    private @Nullable RegisterBlock getRegisterBlockFromConfig(RegisterBlockFunction function) {
        SolaxX3MicConfiguration myconfig = config; // this is because of bug in Nullness checker
        // without local reference, there is an warning about potential null access
        if (myconfig == null) {
            return null;
        }
        int blockAddress = 0;
        int blockLength = 0;
        if (function == RegisterBlockFunction.INPUT_REGISTER_BLOCK) {
            blockAddress = myconfig.inputAddress;
            blockLength = myconfig.inputBlockLength;
        }
        if (function == RegisterBlockFunction.HOLDING_REGISTER_BLOCK) {
            blockAddress = myconfig.holdingAddress;
            blockLength = myconfig.holdingBlockLength;
        }
        return new RegisterBlock(blockAddress, blockLength, function);
    }

    /**
     * Register poll task
     * This is where we set up our regular poller
     */
    private synchronized void registerPollTask(RegisterBlock mainBlock) {
        SolaxX3MicConfiguration myconfig = config; // this is because of bug in Nullness checker      
        logger.debug("Setting up regular polling");

        ModbusReadRequestBlueprint request = new ModbusReadRequestBlueprint(getSlaveId(),
                ModbusReadFunctionCode.READ_INPUT_REGISTERS, mainBlock.address, mainBlock.length, myconfig.maxTries);

        long refreshMillis = myconfig.getRefreshMillis();

        registerRegularPoll(request, refreshMillis, 1000, result -> {
            result.getRegisters().ifPresent(this::handlePolledData);
            if (getThing().getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            }
        }, this::handleError);
    }

    /**
     * This method should handle incoming poll data, and update the channels
     * with the values received
     */
    protected void handlePolledData(ModbusRegisterArray registers) {
        Thing mything = this.getThing();
        // logger.debug("Invoked loopback for handling polled data");
        for (Channel localchannel : mything.getChannels()) {
            // logger.debug("Handling Channel with UID = {}", localchannel.getUID());
            SolaxX3MicChannelConfiguration solaxChannelConfig = localchannel.getConfiguration()
                    .as(SolaxX3MicChannelConfiguration.class);
            Long value = 0L;
            switch (solaxChannelConfig.registerType) {
                case "INT":
                    value = ModbusParser.extractInt32(registers, solaxChannelConfig.registerNumber, true, 0);
                    break;
                case "INT_BIGENDIAN":
                    value = ModbusParser.extractInt32(registers, solaxChannelConfig.registerNumber, false, 0);
                    break;
                case "SHORT":
                    value = (long) ModbusParser.extractInt16(registers, solaxChannelConfig.registerNumber, (short) 0);
                    break;
                case "USHORT":
                    value = (long) ModbusParser.extractUInt16(registers, solaxChannelConfig.registerNumber, 0);
                    break;
            }
            if (solaxChannelConfig.registerUnit == "STATUS") {
                InverterStatus status = InverterStatus.getByCode(value.intValue());
                updateState(localchannel.getUID(), status == null ? UnDefType.UNDEF : new StringType(status.name()));
            } else {
                try {
                    Field field = Units.class.getDeclaredField(solaxChannelConfig.registerUnit);
                    Unit<?> unit = (Unit<?>) field.get(field.getClass());
                    if (unit != null) {
                        State s = getScaled(value, solaxChannelConfig.registerScaleFactor, unit);
                        // logger.debug("value of channel is {} (real value = {}, scaleFactor = {}, unit = {}",
                        // s.toString(), value, solaxChannelConfig.registerScaleFactor, unit.toString());
                        updateState(localchannel.getUID(), s);
                    } else {
                        throw new IllegalAccessException();
                    }
                } catch (NoSuchFieldException ex) {
                    logger.warn("Incorrectly set up of Channel UUID = {}, ex = {}", localchannel.getUID(),
                            ex.getMessage());
                } catch (IllegalAccessException ex) {
                    logger.error("Illegal access exception during reflection to Units!");
                }
            }
        }
    }

    /**
     * Handle errors received during communication
     */
    private void handleError(AsyncModbusFailure<ModbusReadRequestBlueprint> error) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                "Failed to retrieve data: " + error.getCause().getMessage());
    }
    /**
     * Returns value multiplied by the 10 on the power of scaleFactory
     *
     * @param value the value to alter
     * @param scaleFactor the scale factor to use (may be negative)
     * @return the scaled value as a DecimalType
     */
    protected State getScaled(Optional<? extends Number> value, Optional<Integer> scaleFactor, Unit<?> unit) {
        if (!value.isPresent() || !scaleFactor.isPresent()) {
            return UnDefType.UNDEF;
        }
        return getScaled(value.get().longValue(), scaleFactor.get(), unit);
    }

    /**
     * Returns value multiplied by the 10 on the power of scaleFactory
     *
     * @param value the value to alter
     * @param scaleFactor the scale factor to use (may be negative)
     * @return the scaled value as a DecimalType
     */
    protected State getScaled(Optional<? extends Number> value, int scaleFactor, Unit<?> unit) {
        return getScaled(value, Optional.of(scaleFactor), unit);
    }

    /**
     * Returns value multiplied by the 10 on the power of scaleFactory
     *
     * @param value the value to alter
     * @param scaleFactor the scale factor to use (may be negative)
     * @return the scaled value as a DecimalType
     */
    protected State getScaled(Number value, int scaleFactor, Unit<?> unit) {
        if (scaleFactor == 0) {
            return new QuantityType<>(value.longValue(), unit);
        }
        return new QuantityType<>(BigDecimal.valueOf(value.longValue(), scaleFactor * -1), unit);
    }
}
