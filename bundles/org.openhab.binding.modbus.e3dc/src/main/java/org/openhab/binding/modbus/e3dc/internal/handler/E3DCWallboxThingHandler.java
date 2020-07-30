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
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.modbus.e3dc.internal.E3DCWallboxConfiguration;
import org.openhab.binding.modbus.e3dc.internal.dto.DataConverter;
import org.openhab.binding.modbus.e3dc.internal.dto.WallboxArray;
import org.openhab.binding.modbus.e3dc.internal.dto.WallboxBlock;
import org.openhab.binding.modbus.e3dc.internal.modbus.Data.DataType;
import org.openhab.binding.modbus.e3dc.internal.modbus.DataListener;
import org.openhab.binding.modbus.e3dc.internal.modbus.Parser;
import org.openhab.binding.modbus.handler.ModbusEndpointThingHandler;
import org.openhab.io.transport.modbus.AsyncModbusFailure;
import org.openhab.io.transport.modbus.AsyncModbusReadResult;
import org.openhab.io.transport.modbus.ModbusReadRequestBlueprint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link E3DCWallboxThingHandler} Basic modbus connection towards the E3DC device
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class E3DCWallboxThingHandler extends BaseThingHandler implements DataListener {
    private final Logger logger = LoggerFactory.getLogger(E3DCWallboxThingHandler.class);
    private final Parser dataParser = new Parser(DataType.DATA);
    private ThingStatus myStatus = ThingStatus.UNKNOWN;
    private BitSet currentBitSet = new BitSet(16);
    private @Nullable E3DCWallboxConfiguration config;
    private @Nullable E3DCThingHandler bridgeHandler;

    public E3DCWallboxThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof OnOffType) {
            logger.info("Command {} CUID {}", command, channelUID);
            logger.info("getId {}", channelUID.getId());
            logger.info("getIdWithoutGroup {}", channelUID.getIdWithoutGroup());
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
                logger.info("Send {}", writeValue);
            }
            E3DCThingHandler localBridgeHandler = bridgeHandler;
            if (localBridgeHandler == null) {
                Bridge b = getBridge();
                if (b != null) {
                    localBridgeHandler = (E3DCThingHandler) b.getHandler();
                    bridgeHandler = localBridgeHandler;
                }
            }
            if (localBridgeHandler != null) {
                int wallboxId = getWallboxId(config);
                if (wallboxId != -1) {
                    localBridgeHandler.wallboxSet(wallboxId, writeValue);
                }
            }
        }
    }

    @Override
    public void initialize() {
        setStatus(ThingStatus.UNKNOWN);
        scheduler.execute(() -> {
            config = getConfigAs(E3DCWallboxConfiguration.class);
            Bridge bridge = getBridge();
            if (bridge != null) {
                ThingHandler handler = bridge.getHandler();
                if (handler != null) {
                    logger.info("Bridge Handler: {}", handler.toString());
                    bridgeHandler = ((E3DCThingHandler) handler);
                    bridgeHandler.addDataListener(this);
                } else {
                    logger.info("Thing Handler null");
                }
            } else {
                logger.info("Bridge null");
            }
        });
    }

    private void turnOnline() {
        if (myStatus != ThingStatus.ONLINE) {
            setStatus(ThingStatus.ONLINE);
        }
    }

    @Override
    public void dispose() {
    }

    private void setStatus(ThingStatus status) {
        myStatus = status;
        updateStatus(myStatus);
    }

    /**
     * Get the endpoint handler from the bridge this handler is connected to
     * Checks that we're connected to the right type of bridge
     *
     * @return the endpoint handler or null if the bridge does not exist
     */
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

        if (handler instanceof ModbusEndpointThingHandler) {
            ModbusEndpointThingHandler slaveEndpoint = (ModbusEndpointThingHandler) handler;
            return slaveEndpoint;
        } else {
            logger.debug("Unexpected bridge handler: {}", handler);
            return null;
        }
    }

    /**
     * Returns the channel UID for the specified group and channel id
     *
     * @param string the channel group
     * @param string the channel id in that group
     * @return the globally unique channel uid
     */
    private ChannelUID channelUID(String group, String id) {
        return new ChannelUID(getThing().getUID(), group, id);
    }

    private int getWallboxId(@Nullable E3DCWallboxConfiguration c) {
        if (c != null) {
            return c.wallboxId;
        } else {
            return -1;
        }
    }

    @Override
    public void handle(AsyncModbusReadResult result) {
        turnOnline();
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

    @Override
    public void handleError(AsyncModbusFailure<ModbusReadRequestBlueprint> result) {
        // TODO Auto-generated method stub
    }
}
