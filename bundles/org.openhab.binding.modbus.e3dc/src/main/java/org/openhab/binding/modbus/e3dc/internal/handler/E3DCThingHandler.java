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

import java.util.ArrayList;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.modbus.e3dc.internal.dto.EmergencyBlock;
import org.openhab.binding.modbus.e3dc.internal.dto.InfoBlock;
import org.openhab.binding.modbus.e3dc.internal.dto.PowerBlock;
import org.openhab.binding.modbus.e3dc.internal.dto.StringBlock;
import org.openhab.binding.modbus.e3dc.internal.modbus.Data.DataType;
import org.openhab.binding.modbus.e3dc.internal.modbus.DataListener;
import org.openhab.binding.modbus.e3dc.internal.modbus.Parser;
import org.openhab.binding.modbus.handler.EndpointNotInitializedException;
import org.openhab.binding.modbus.handler.ModbusEndpointThingHandler;
import org.openhab.io.transport.modbus.AsyncModbusFailure;
import org.openhab.io.transport.modbus.AsyncModbusReadResult;
import org.openhab.io.transport.modbus.ModbusCommunicationInterface;
import org.openhab.io.transport.modbus.ModbusReadFunctionCode;
import org.openhab.io.transport.modbus.ModbusReadRequestBlueprint;
import org.openhab.io.transport.modbus.ModbusRegisterArray;
import org.openhab.io.transport.modbus.ModbusWriteRegisterRequestBlueprint;
import org.openhab.io.transport.modbus.PollTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link E3DCThingHandler} Basic modbus connection towards the E3DC device
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class E3DCThingHandler extends BaseBridgeHandler {
    private final ArrayList<DataListener> listeners = new ArrayList<DataListener>();

    private final Logger logger = LoggerFactory.getLogger(E3DCThingHandler.class);
    private final Parser dataParser = new Parser(DataType.DATA);
    private final Parser infoParser = new Parser(DataType.INFO);
    private ThingStatus myStatus = ThingStatus.UNKNOWN;
    private @Nullable PollTask infoPoller;
    private @Nullable PollTask dataPoller;

    /**
     * Communication interface to the slave endpoint we're connecting to
     */
    protected volatile @Nullable ModbusCommunicationInterface comms = null;
    private int slaveId;

    public E3DCThingHandler(Bridge thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // no control of E3DC device possible yet
    }

    @Override
    public void initialize() {
        setStatus(ThingStatus.UNKNOWN);
        // Example for background initialization:
        scheduler.execute(() -> {
            connectEndpoint();
            ModbusCommunicationInterface localComms = comms;
            if (localComms != null) {
                // register low speed info poller
                ModbusReadRequestBlueprint infoRequest = new ModbusReadRequestBlueprint(slaveId,
                        ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, INFO_REG_START, INFO_REG_SIZE, 3);
                infoPoller = localComms.registerRegularPoll(infoRequest, INFO_POLL_REFRESH_TIME_MS, 0,
                        this::handleInfoResult, this::handleInfoFailure);

                ModbusReadRequestBlueprint dataRequest = new ModbusReadRequestBlueprint(slaveId,
                        ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, POWER_REG_START,
                        REGISTER_LENGTH - INFO_REG_SIZE, 3);
                dataPoller = localComms.registerRegularPoll(dataRequest, DATA_POLL_REFRESH_TIME_MS_NOW_HARDCODED, 0,
                        this::handleDataResult, this::handleDataFailure);
            } else {
                // comms not available, status has been set
            }
        });
    }

    private void handleInfoResult(AsyncModbusReadResult result) {
        turnOnline();
        infoParser.handle(result);
        InfoBlock block = (InfoBlock) infoParser.parse(DataType.INFO);
        String group = "info";
        if (block != null) {
            updateState(channelUID(group, MODBUS_ID_CHANNEL), block.modbusId);
            updateState(channelUID(group, MODBUS_FIRMWARE_CHANNEL), block.modbusVersion);
            updateState(channelUID(group, SUPPORTED_REGSITERS_CHANNEL), block.supportedRegisters);
            updateState(channelUID(group, MANUFACTURER_NAME_CHANNEL), block.manufacturer);
            updateState(channelUID(group, MODEL_NAME_CHANNEL), block.modelName);
            updateState(channelUID(group, SERIAL_NUMBER_CHANNEL), block.serialNumber);
            updateState(channelUID(group, FIRMWARE_RELEASE_CHANNEL), block.firmware);
        } else {
            logger.debug("Unable to get {} from provider {}", DataType.INFO, dataParser.toString());
        }
    }

    private void handleInfoFailure(AsyncModbusFailure<ModbusReadRequestBlueprint> result) {
        // TODO: error handling
    }

    void handleDataResult(AsyncModbusReadResult result) {
        turnOnline();
        dataParser.handle(result);
        // Update channels in emergency group
        {
            EmergencyBlock block = (EmergencyBlock) dataParser.parse(DataType.EMERGENCY);
            String group = "emergency";
            if (block != null) {
                updateState(channelUID(group, EMERGENCY_POWER_STATUS), block.epStatus);
                updateState(channelUID(group, BATTERY_LOADING_LOCKED), block.batteryLoadingLocked);
                updateState(channelUID(group, BATTERY_UNLOADING_LOCKED), block.batterUnLoadingLocked);
                updateState(channelUID(group, EMERGENCY_POWER_POSSIBLE), block.epPossible);
                updateState(channelUID(group, WEATHER_PREDICTION_LOADING), block.weatherPredictedLoading);
                updateState(channelUID(group, REGULATION_STATUS), block.regulationStatus);
                updateState(channelUID(group, LOADING_LOCK_TIME), block.loadingLockTime);
                updateState(channelUID(group, UNLOADING_LOCKTIME), block.unloadingLockTime);
            } else {
                logger.debug("Unable to get {} from provider {}", DataType.EMERGENCY, dataParser.toString());
            }
        }

        // Update channels in power group
        {
            PowerBlock block = (PowerBlock) dataParser.parse(DataType.POWER);
            String group = "power";
            if (block != null) {
                updateState(channelUID(group, PV_POWER_SUPPLY_CHANNEL), block.pvPowerSupply);
                updateState(channelUID(group, BATTERY_POWER_SUPPLY_CHANNEL), block.batteryPowerSupply);
                updateState(channelUID(group, BATTERY_POWER_CONSUMPTION), block.batteryPowerConsumption);
                updateState(channelUID(group, HOUSEHOLD_POWER_CONSUMPTION_CHANNEL), block.householdPowerConsumption);
                updateState(channelUID(group, GRID_POWER_CONSUMPTION_CHANNEL), block.gridPowerConsumpition);
                updateState(channelUID(group, GRID_POWER_SUPPLY_CHANNEL), block.gridPowerSupply);
                updateState(channelUID(group, EXTERNAL_POWER_SUPPLY_CHANNEL), block.externalPowerSupply);
                updateState(channelUID(group, WALLBOX_POWER_CONSUMPTION_CHANNEL), block.wallboxPowerConsumption);
                updateState(channelUID(group, WALLBOX_PV_POWER_CONSUMPTION_CHANNEL), block.wallboxPVPowerConsumption);
                updateState(channelUID(group, AUTARKY), block.autarky);
                updateState(channelUID(group, SELF_CONSUMPTION), block.selfConsumption);
                updateState(channelUID(group, BATTERY_STATE_OF_CHARGE_CHANNEL), block.batterySOC);
            } else {
                logger.debug("Unable to get {} from provider {}", DataType.POWER, dataParser.toString());
            }
        }

        // Update channels in strings group
        {
            StringBlock block = (StringBlock) dataParser.parse(DataType.STRINGS);
            String group = "strings";
            if (block != null) {
                updateState(channelUID(group, STRING1_DC_CURRENT_CHANNEL), block.string1Ampere);
                updateState(channelUID(group, STRING1_DC_VOLTAGE_CHANNEL), block.string1Volt);
                updateState(channelUID(group, STRING1_DC_OUTPUT_CHANNEL), block.string1Watt);
                updateState(channelUID(group, STRING2_DC_CURRENT_CHANNEL), block.string2Ampere);
                updateState(channelUID(group, STRING2_DC_VOLTAGE_CHANNEL), block.string2Volt);
                updateState(channelUID(group, STRING2_DC_OUTPUT_CHANNEL), block.string2Watt);
                updateState(channelUID(group, STRING3_DC_CURRENT_CHANNEL), block.string3Ampere);
                updateState(channelUID(group, STRING3_DC_VOLTAGE_CHANNEL), block.string3Volt);
                updateState(channelUID(group, STRING3_DC_OUTPUT_CHANNEL), block.string3Watt);
            } else {
                logger.debug("Unable to get {} from provider {}", DataType.STRINGS, dataParser.toString());
            }
        }

        listeners.forEach(l -> {
            l.handle(result);
        });
    }

    void handleDataFailure(AsyncModbusFailure<ModbusReadRequestBlueprint> result) {
        // TODO: error handling
        listeners.forEach(l -> {
            l.handleError(result);
        });
    }

    private void turnOnline() {
        if (myStatus != ThingStatus.ONLINE) {
            setStatus(ThingStatus.ONLINE);
        }
    }

    @Override
    public void dispose() {
        ModbusCommunicationInterface localComms = comms;
        if (localComms != null) {
            PollTask localInfoPoller = infoPoller;
            if (localInfoPoller != null) {
                localComms.unregisterRegularPoll(localInfoPoller);
            }
            PollTask localDataPoller = dataPoller;
            if (localDataPoller != null) {
                localComms.unregisterRegularPoll(localDataPoller);
            }
        }
        // Comms will be close()'d by endpoint thing handler
        comms = null;
    }

    private void setStatus(ThingStatus status) {
        myStatus = status;
        updateStatus(myStatus);
    }

    /**
     * Wallbox Settings can be changed with one Integer
     *
     * @param wallboxId needed to calculate right register
     * @param writeValue integer to be written
     */
    public void wallboxSet(int wallboxId, int writeValue) {
        ModbusCommunicationInterface localComms = comms;
        if (localComms != null) {
            ModbusRegisterArray regArray = new ModbusRegisterArray(writeValue);
            ModbusWriteRegisterRequestBlueprint writeBluePrint = new ModbusWriteRegisterRequestBlueprint(slaveId,
                    WALLBOX_REG_START + wallboxId, regArray, false, 3);
            localComms.submitOneTimeWrite(writeBluePrint, result -> {
                // TODO: update thing status?
                logger.debug("E3DC Modbus write response! {}", result.getResponse().toString());
            }, failure -> {
                // TODO: update thing status?
                logger.warn("E3DC Modbus write error! {}", failure.getRequest().toString());
            });
        }
    }

    /**
     * Get a reference to the modbus endpoint
     */
    private void connectEndpoint() {
        if (comms != null) {
            return;
        }

        ModbusEndpointThingHandler slaveEndpointThingHandler = getEndpointThingHandler();
        if (slaveEndpointThingHandler == null) {
            @SuppressWarnings("null")
            String label = Optional.ofNullable(getBridge()).map(b -> b.getLabel()).orElse("<null>");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                    String.format("Bridge '%s' is offline", label));
            logger.debug("No bridge handler available -- aborting init for {}", label);
            return;
        }
        try {
            slaveId = slaveEndpointThingHandler.getSlaveId();
            comms = slaveEndpointThingHandler.getCommunicationInterface();
        } catch (EndpointNotInitializedException e) {
            // FIXME: this cannot be raised anymore, throws was left accidentally in the API
        }
        if (comms == null) {
            @SuppressWarnings("null")
            String label = Optional.ofNullable(getBridge()).map(b -> b.getLabel()).orElse("<null>");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                    String.format("Bridge '%s' not completely initialized", label));
            logger.debug("Bridge not initialized fully (no endpoint) -- aborting init for {}", this);
            return;
        }
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

    public synchronized void addDataListener(DataListener l) {
        listeners.add(l);
    }

    public synchronized void removeDataListener(DataListener l) {
        listeners.remove(l);
    }
}
