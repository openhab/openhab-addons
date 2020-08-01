/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.modbus.e3dc.internal.E3DCWallboxConfiguration;
import org.openhab.binding.modbus.e3dc.internal.dto.DataConverter;
import org.openhab.binding.modbus.e3dc.internal.dto.WallboxArray;
import org.openhab.binding.modbus.e3dc.internal.dto.WallboxBlock;
import org.openhab.binding.modbus.e3dc.internal.modbus.Data.DataType;
import org.openhab.binding.modbus.e3dc.internal.modbus.Parser;
import org.openhab.io.transport.modbus.AsyncModbusFailure;
import org.openhab.io.transport.modbus.AsyncModbusReadResult;
import org.openhab.io.transport.modbus.ModbusCommunicationInterface;
import org.openhab.io.transport.modbus.ModbusReadRequestBlueprint;
import org.openhab.io.transport.modbus.ModbusRegisterArray;
import org.openhab.io.transport.modbus.ModbusWriteRegisterRequestBlueprint;
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

    private final Logger logger = LoggerFactory.getLogger(E3DCWallboxThingHandler.class);
    private final Parser dataParser = new Parser(DataType.DATA);
    private ReadWriteSuccess dataRead = ReadWriteSuccess.NOT_RECEIVED;
    private ReadWriteSuccess dataWrite = ReadWriteSuccess.NOT_RECEIVED;
    private BitSet currentBitSet = new BitSet(16);
    private @Nullable E3DCWallboxConfiguration config;
    private @Nullable E3DCThingHandler bridgeHandler;

    public E3DCWallboxThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        scheduler.execute(() -> {
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
        });
    }

    @Override
    public void dispose() {
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
            int wallboxId = getWallboxId(config);
            if (wallboxId != -1) {
                wallboxSet(wallboxId, writeValue);
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

    private int getWallboxId(@Nullable E3DCWallboxConfiguration c) {
        if (c != null) {
            return c.wallboxId;
        } else {
            return -1;
        }
    }

    public void handle(AsyncModbusReadResult result) {
        if (dataRead != ReadWriteSuccess.SUCCESS) {
            dataRead = ReadWriteSuccess.SUCCESS;
            updateStatus();
        }
        dataParser.handle(result);
        WallboxArray wbArray = (WallboxArray) dataParser.parse(DataType.WALLBOX);
        if (wbArray != null) {
            int wallboxId = getWallboxId(config);
            if (wallboxId != -1) {
                WallboxBlock block = wbArray.getWallboxBlock(wallboxId);
                if (block != null) {
                    synchronized (this) {
                        currentBitSet = block.getBitSet();
                    }
                    updateState(WB_AVAILABLE_CHANNEL, block.wbAvailable);
                    updateState(WB_SUNMODE_CHANNEL, block.wbSunmode);
                    updateState(WB_CHARGING_ABORTED_CHANNEL, block.wbChargingAborted);
                    updateState(WB_CHARGING_CHANNEL, block.wbCharging);
                    updateState(WB_JACK_LOCKED_CHANNEL, block.wbJackLocked);
                    updateState(WB_JACK_PLUGGED_CHANNEL, block.wbJackPlugged);
                    updateState(WB_SCHUKO_ON_CHANNEL, block.wbSchukoOn);
                    updateState(WB_SCHUKO_PLUGGED_CHANNEL, block.wbSchukoPlugged);
                    updateState(WB_SCHUKO_LOCKED_CHANNEL, block.wbSchukoLocked);
                    updateState(WB_SCHUKO_REALY_16A_CHANNEL, block.wbSchukoRelay16);
                    updateState(WB_REALY_16A_CHANNEL, block.wbRelay16);
                    updateState(WB_RELAY_32A_CHANNEL, block.wbRelay32);
                    updateState(WB_1PHASE_CHANNEL, block.wb1phase);
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
