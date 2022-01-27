/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.modbus.e3dc.internal.handler;

import static org.openhab.binding.modbus.e3dc.internal.E3DCBindingConstants.*;
import static org.openhab.binding.modbus.e3dc.internal.modbus.E3DCModbusConstans.*;

import java.util.BitSet;
import java.util.Optional;
import java.util.OptionalInt;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.modbus.e3dc.internal.E3DCWallboxConfiguration;
import org.openhab.binding.modbus.e3dc.internal.dto.DataConverter;
import org.openhab.binding.modbus.e3dc.internal.dto.WallboxArray;
import org.openhab.binding.modbus.e3dc.internal.dto.WallboxBlock;
import org.openhab.binding.modbus.e3dc.internal.modbus.Data;
import org.openhab.binding.modbus.e3dc.internal.modbus.Data.DataType;
import org.openhab.binding.modbus.e3dc.internal.modbus.Parser;
import org.openhab.core.io.transport.modbus.AsyncModbusFailure;
import org.openhab.core.io.transport.modbus.AsyncModbusReadResult;
import org.openhab.core.io.transport.modbus.ModbusCommunicationInterface;
import org.openhab.core.io.transport.modbus.ModbusReadRequestBlueprint;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;
import org.openhab.core.io.transport.modbus.ModbusWriteRegisterRequestBlueprint;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link E3DCWallboxThingHandler} Basic modbus connection towards the E3DC device
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class E3DCWallboxThingHandler extends BaseThingHandler {
    public enum ReadWriteSuccess {
        NOT_RECEIVED,
        SUCCESS,
        FAILED
    }

    private static final String READ_WRITE_ERROR = "Modbus Data Read/Write Error";
    private static final String READ_ERROR = "Modbus Read Error";
    private static final String WRITE_ERROR = "Modbus Write Error";

    ChannelUID wbAvailableChannel;
    ChannelUID wbSunmodeChannel;
    ChannelUID wbChargingAbortedChannel;
    ChannelUID wbChargingChannel;
    ChannelUID wbJackLockedChannel;
    ChannelUID wbJackPluggedChannel;
    ChannelUID wbSchukoOnChannel;
    ChannelUID wbSchukoPluggedChannel;
    ChannelUID wbSchukoLockedChannel;
    ChannelUID wbSchukoRelay16Channel;
    ChannelUID wbRelay16Channel;
    ChannelUID wbRelay32Channel;
    ChannelUID wb1phaseChannel;

    private final Logger logger = LoggerFactory.getLogger(E3DCWallboxThingHandler.class);
    private final Parser dataParser = new Parser(DataType.DATA);
    private ReadWriteSuccess dataRead = ReadWriteSuccess.NOT_RECEIVED;
    private ReadWriteSuccess dataWrite = ReadWriteSuccess.NOT_RECEIVED;
    private volatile BitSet currentBitSet = new BitSet(16);
    private @Nullable E3DCWallboxConfiguration config;
    private @Nullable E3DCThingHandler bridgeHandler;

    public E3DCWallboxThingHandler(Thing thing) {
        super(thing);
        wbAvailableChannel = new ChannelUID(thing.getUID(), WB_AVAILABLE_CHANNEL);
        wbSunmodeChannel = new ChannelUID(thing.getUID(), WB_SUNMODE_CHANNEL);
        wbChargingAbortedChannel = new ChannelUID(thing.getUID(), WB_CHARGING_ABORTED_CHANNEL);
        wbChargingChannel = new ChannelUID(thing.getUID(), WB_CHARGING_CHANNEL);
        wbJackLockedChannel = new ChannelUID(thing.getUID(), WB_JACK_LOCKED_CHANNEL);
        wbJackPluggedChannel = new ChannelUID(thing.getUID(), WB_JACK_PLUGGED_CHANNEL);
        wbSchukoOnChannel = new ChannelUID(thing.getUID(), WB_SCHUKO_ON_CHANNEL);
        wbSchukoPluggedChannel = new ChannelUID(thing.getUID(), WB_SCHUKO_PLUGGED_CHANNEL);
        wbSchukoLockedChannel = new ChannelUID(thing.getUID(), WB_SCHUKO_LOCKED_CHANNEL);
        wbSchukoRelay16Channel = new ChannelUID(thing.getUID(), WB_SCHUKO_RELAY_16A_CHANNEL);
        wbRelay16Channel = new ChannelUID(thing.getUID(), WB_RELAY_16A_CHANNEL);
        wbRelay32Channel = new ChannelUID(thing.getUID(), WB_RELAY_32A_CHANNEL);
        wb1phaseChannel = new ChannelUID(thing.getUID(), WB_1PHASE_CHANNEL);
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        config = getConfigAs(E3DCWallboxConfiguration.class);
        Bridge bridge = getBridge();
        if (bridge != null) {
            ThingHandler handler = bridge.getHandler();
            if (handler != null) {
                bridgeHandler = ((E3DCThingHandler) handler);
            } else {
                logger.warn("Thing Handler null");
            }
        } else {
            logger.warn("Bridge null");
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof OnOffType) {
            int writeValue = 0;
            synchronized (this) {
                if (channelUID.getIdWithoutGroup().equals(WB_SUNMODE_CHANNEL)) {
                    currentBitSet.set(WB_SUNMODE_BIT, command.equals(OnOffType.ON));
                } else if (channelUID.getIdWithoutGroup().equals(WB_CHARGING_ABORTED_CHANNEL)) {
                    currentBitSet.set(WB_CHARGING_ABORTED_BIT, command.equals(OnOffType.ON));
                } else if (channelUID.getIdWithoutGroup().equals(WB_SCHUKO_ON_CHANNEL)) {
                    currentBitSet.set(WB_SCHUKO_ON_BIT, command.equals(OnOffType.ON));
                } else if (channelUID.getIdWithoutGroup().equals(WB_1PHASE_CHANNEL)) {
                    currentBitSet.set(WB_1PHASE_BIT, command.equals(OnOffType.ON));
                }
                writeValue = DataConverter.toInt(currentBitSet);
                logger.debug("Wallbox write {}", writeValue);
            }
            OptionalInt wallboxId = getWallboxId(config);
            if (wallboxId.isPresent()) {
                wallboxSet(wallboxId.getAsInt(), writeValue);
            }
        }
    }

    /**
     * Wallbox Settings can be changed with one Integer
     *
     * @param wallboxId needed to calculate right register
     * @param writeValue integer to be written
     */
    public void wallboxSet(int wallboxId, int writeValue) {
        E3DCThingHandler localBridgeHandler = bridgeHandler;
        if (localBridgeHandler != null) {
            ModbusCommunicationInterface comms = localBridgeHandler.getComms();
            if (comms != null) {
                ModbusRegisterArray regArray = new ModbusRegisterArray(writeValue);
                ModbusWriteRegisterRequestBlueprint writeBluePrint = new ModbusWriteRegisterRequestBlueprint(
                        localBridgeHandler.getSlaveId(), WALLBOX_REG_START + wallboxId, regArray, false, 3);
                comms.submitOneTimeWrite(writeBluePrint, result -> {
                    if (dataWrite != ReadWriteSuccess.SUCCESS) {
                        dataWrite = ReadWriteSuccess.SUCCESS;
                        updateStatus();
                    }
                    logger.debug("E3DC Modbus write response! {}", result.getResponse().toString());
                }, failure -> {
                    if (dataWrite != ReadWriteSuccess.FAILED) {
                        dataWrite = ReadWriteSuccess.FAILED;
                        updateStatus();
                    }
                    logger.warn("E3DC Modbus write error! {}", failure.getRequest().toString());
                });
            }
        }
    }

    private OptionalInt getWallboxId(@Nullable E3DCWallboxConfiguration c) {
        if (c != null) {
            return OptionalInt.of(c.wallboxId);
        } else {
            return OptionalInt.empty();
        }
    }

    public void handle(AsyncModbusReadResult result) {
        if (dataRead != ReadWriteSuccess.SUCCESS) {
            dataRead = ReadWriteSuccess.SUCCESS;
            updateStatus();
        }
        dataParser.handle(result);
        Optional<Data> wbArrayOpt = dataParser.parse(DataType.WALLBOX);
        if (wbArrayOpt.isPresent()) {
            WallboxArray wbArray = (WallboxArray) wbArrayOpt.get();
            OptionalInt wallboxId = getWallboxId(config);
            if (wallboxId.isPresent()) {
                Optional<WallboxBlock> blockOpt = wbArray.getWallboxBlock(wallboxId.getAsInt());
                if (blockOpt.isPresent()) {
                    WallboxBlock block = blockOpt.get();
                    synchronized (this) {
                        currentBitSet = block.getBitSet();
                    }
                    updateState(wbAvailableChannel, block.wbAvailable);
                    updateState(wbSunmodeChannel, block.wbSunmode);
                    updateState(wbChargingAbortedChannel, block.wbChargingAborted);
                    updateState(wbChargingChannel, block.wbCharging);
                    updateState(wbJackLockedChannel, block.wbJackLocked);
                    updateState(wbJackPluggedChannel, block.wbJackPlugged);
                    updateState(wbSchukoOnChannel, block.wbSchukoOn);
                    updateState(wbSchukoPluggedChannel, block.wbSchukoPlugged);
                    updateState(wbSchukoLockedChannel, block.wbSchukoLocked);
                    updateState(wbSchukoRelay16Channel, block.wbSchukoRelay16);
                    updateState(wbRelay16Channel, block.wbRelay16);
                    updateState(wbRelay32Channel, block.wbRelay32);
                    updateState(wb1phaseChannel, block.wb1phase);
                } else {
                    logger.debug("Unable to get ID {} from WallboxArray", wallboxId);
                }
            } else {
                logger.debug("Wallbox ID {} not valid", wallboxId);
            }
        } else {
            logger.debug("Unable to get {} from Bridge", DataType.WALLBOX);
        }
    }

    public void handleError(AsyncModbusFailure<ModbusReadRequestBlueprint> result) {
        if (dataRead != ReadWriteSuccess.FAILED) {
            dataRead = ReadWriteSuccess.FAILED;
            updateStatus();
        }
    }

    private void updateStatus() {
        if (dataWrite == ReadWriteSuccess.NOT_RECEIVED) {
            // read success / write not happened yet => go online / offline
            if (dataRead == ReadWriteSuccess.SUCCESS) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, READ_ERROR);
            }
        } else {
            if (dataRead == dataWrite) {
                // read and write same status - either go online or offline
                if (dataRead == ReadWriteSuccess.SUCCESS) {
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, READ_WRITE_ERROR);
                }
            } else {
                // either read or write failed - go offline with detailed status
                if (dataRead == ReadWriteSuccess.SUCCESS) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, WRITE_ERROR);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, READ_ERROR);
                }
            }
        }
    }
}
