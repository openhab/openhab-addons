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

import java.util.ArrayList;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.modbus.e3dc.internal.E3DCConfiguration;
import org.openhab.binding.modbus.e3dc.internal.dto.EmergencyBlock;
import org.openhab.binding.modbus.e3dc.internal.dto.InfoBlock;
import org.openhab.binding.modbus.e3dc.internal.dto.PowerBlock;
import org.openhab.binding.modbus.e3dc.internal.dto.StringBlock;
import org.openhab.binding.modbus.e3dc.internal.modbus.Data;
import org.openhab.binding.modbus.e3dc.internal.modbus.Data.DataType;
import org.openhab.binding.modbus.e3dc.internal.modbus.Parser;
import org.openhab.binding.modbus.handler.EndpointNotInitializedException;
import org.openhab.binding.modbus.handler.ModbusEndpointThingHandler;
import org.openhab.core.io.transport.modbus.AsyncModbusFailure;
import org.openhab.core.io.transport.modbus.AsyncModbusReadResult;
import org.openhab.core.io.transport.modbus.ModbusCommunicationInterface;
import org.openhab.core.io.transport.modbus.ModbusReadFunctionCode;
import org.openhab.core.io.transport.modbus.ModbusReadRequestBlueprint;
import org.openhab.core.io.transport.modbus.PollTask;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link E3DCThingHandler} Basic modbus connection towards the E3DC device
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class E3DCThingHandler extends BaseBridgeHandler {
    public enum ReadStatus {
        NOT_RECEIVED,
        READ_SUCCESS,
        READ_FAILED
    }

    static final String INFO_DATA_READ_ERROR = "Information And Data Modbus Read Errors";
    static final String INFO_READ_ERROR = "Information Modbus Read Error";
    static final String DATA_READ_ERROR = "Data Modbus Read Error";

    static final String INFO_GROUP = "info";
    static final String EMERGENCY_GROUP = "emergency";
    static final String POWER_GROUP = "power";
    static final String STRINGS_GROUP = "strings";

    private ChannelUID modbusIdChannel;
    private ChannelUID modbusVersionChannel;
    private ChannelUID supportedRegistersChannel;
    private ChannelUID manufacturerChannel;
    private ChannelUID modelNameChannel;
    private ChannelUID serialNumberChannel;
    private ChannelUID firmwareChannel;

    private ChannelUID epStatusChannel;
    private ChannelUID batteryChargingLockedChannel;
    private ChannelUID batteryDischargingLockedChannel;
    private ChannelUID epPossibleChannel;
    private ChannelUID weatherPredictedChargingChannel;
    private ChannelUID regulationStatusChannel;
    private ChannelUID chargeLockTimeChannel;
    private ChannelUID dischargeLockTimeChannel;

    private ChannelUID pvPowerSupplyChannel;
    private ChannelUID batteryPowerSupplyChannel;
    private ChannelUID batteryPowerConsumptionChannel;
    private ChannelUID householdPowerConsumptionChannel;
    private ChannelUID gridPowerConsumpitionChannel;
    private ChannelUID gridPowerSupplyChannel;
    private ChannelUID externalPowerSupplyChannel;
    private ChannelUID wallboxPowerConsumptionChannel;
    private ChannelUID wallboxPVPowerConsumptionChannel;
    private ChannelUID autarkyChannel;
    private ChannelUID selfConsumptionChannel;
    private ChannelUID batterySOCChannel;

    private ChannelUID string1AmpereChannel;
    private ChannelUID string1VoltChannel;
    private ChannelUID string1WattChannel;
    private ChannelUID string2AmpereChannel;
    private ChannelUID string2VoltChannel;
    private ChannelUID string2WattChannel;
    private ChannelUID string3AmpereChannel;
    private ChannelUID string3VoltChannel;
    private ChannelUID string3WattChannel;

    private final ArrayList<E3DCWallboxThingHandler> listeners = new ArrayList<E3DCWallboxThingHandler>();
    private final Logger logger = LoggerFactory.getLogger(E3DCThingHandler.class);
    private final Parser dataParser = new Parser(DataType.DATA);
    private ReadStatus dataRead = ReadStatus.NOT_RECEIVED;
    private final Parser infoParser = new Parser(DataType.INFO);
    private ReadStatus infoRead = ReadStatus.NOT_RECEIVED;
    private @Nullable PollTask infoPoller;
    private @Nullable PollTask dataPoller;
    private @Nullable E3DCConfiguration config;

    /**
     * Communication interface to the slave endpoint we're connecting to
     */
    protected volatile @Nullable ModbusCommunicationInterface comms = null;
    private int slaveId;

    public E3DCThingHandler(Bridge thing) {
        super(thing);

        modbusIdChannel = channelUID(thing, INFO_GROUP, MODBUS_ID_CHANNEL);
        modbusVersionChannel = channelUID(thing, INFO_GROUP, MODBUS_FIRMWARE_CHANNEL);
        supportedRegistersChannel = channelUID(thing, INFO_GROUP, SUPPORTED_REGISTERS_CHANNEL);
        manufacturerChannel = channelUID(thing, INFO_GROUP, MANUFACTURER_NAME_CHANNEL);
        modelNameChannel = channelUID(thing, INFO_GROUP, MODEL_NAME_CHANNEL);
        serialNumberChannel = channelUID(thing, INFO_GROUP, SERIAL_NUMBER_CHANNEL);
        firmwareChannel = channelUID(thing, INFO_GROUP, FIRMWARE_RELEASE_CHANNEL);

        epStatusChannel = channelUID(thing, EMERGENCY_GROUP, EMERGENCY_POWER_STATUS);
        batteryChargingLockedChannel = channelUID(thing, EMERGENCY_GROUP, BATTERY_CHARGING_LOCKED);
        batteryDischargingLockedChannel = channelUID(thing, EMERGENCY_GROUP, BATTERY_DISCHARGING_LOCKED);
        epPossibleChannel = channelUID(thing, EMERGENCY_GROUP, EMERGENCY_POWER_POSSIBLE);
        weatherPredictedChargingChannel = channelUID(thing, EMERGENCY_GROUP, WEATHER_PREDICTED_CHARGING);
        regulationStatusChannel = channelUID(thing, EMERGENCY_GROUP, REGULATION_STATUS);
        chargeLockTimeChannel = channelUID(thing, EMERGENCY_GROUP, CHARGE_LOCK_TIME);
        dischargeLockTimeChannel = channelUID(thing, EMERGENCY_GROUP, DISCHARGE_LOCK_TIME);

        pvPowerSupplyChannel = channelUID(thing, POWER_GROUP, PV_POWER_SUPPLY_CHANNEL);
        batteryPowerSupplyChannel = channelUID(thing, POWER_GROUP, BATTERY_POWER_SUPPLY_CHANNEL);
        batteryPowerConsumptionChannel = channelUID(thing, POWER_GROUP, BATTERY_POWER_CONSUMPTION);
        householdPowerConsumptionChannel = channelUID(thing, POWER_GROUP, HOUSEHOLD_POWER_CONSUMPTION_CHANNEL);
        gridPowerConsumpitionChannel = channelUID(thing, POWER_GROUP, GRID_POWER_CONSUMPTION_CHANNEL);
        gridPowerSupplyChannel = channelUID(thing, POWER_GROUP, GRID_POWER_SUPPLY_CHANNEL);
        externalPowerSupplyChannel = channelUID(thing, POWER_GROUP, EXTERNAL_POWER_SUPPLY_CHANNEL);
        wallboxPowerConsumptionChannel = channelUID(thing, POWER_GROUP, WALLBOX_POWER_CONSUMPTION_CHANNEL);
        wallboxPVPowerConsumptionChannel = channelUID(thing, POWER_GROUP, WALLBOX_PV_POWER_CONSUMPTION_CHANNEL);
        autarkyChannel = channelUID(thing, POWER_GROUP, AUTARKY_CHANNEL);
        selfConsumptionChannel = channelUID(thing, POWER_GROUP, SELF_CONSUMPTION_CHANNEL);
        batterySOCChannel = channelUID(thing, POWER_GROUP, BATTERY_STATE_OF_CHARGE_CHANNEL);

        string1AmpereChannel = channelUID(thing, STRINGS_GROUP, STRING1_DC_CURRENT_CHANNEL);
        string1VoltChannel = channelUID(thing, STRINGS_GROUP, STRING1_DC_VOLTAGE_CHANNEL);
        string1WattChannel = channelUID(thing, STRINGS_GROUP, STRING1_DC_OUTPUT_CHANNEL);
        string2AmpereChannel = channelUID(thing, STRINGS_GROUP, STRING2_DC_CURRENT_CHANNEL);
        string2VoltChannel = channelUID(thing, STRINGS_GROUP, STRING2_DC_VOLTAGE_CHANNEL);
        string2WattChannel = channelUID(thing, STRINGS_GROUP, STRING2_DC_OUTPUT_CHANNEL);
        string3AmpereChannel = channelUID(thing, STRINGS_GROUP, STRING3_DC_CURRENT_CHANNEL);
        string3VoltChannel = channelUID(thing, STRINGS_GROUP, STRING3_DC_VOLTAGE_CHANNEL);
        string3WattChannel = channelUID(thing, STRINGS_GROUP, STRING3_DC_OUTPUT_CHANNEL);
    }

    public @Nullable ModbusCommunicationInterface getComms() {
        return comms;
    }

    public int getSlaveId() {
        return slaveId;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // no control of E3DC device possible yet
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        scheduler.execute(() -> {
            E3DCConfiguration localConfig = getConfigAs(E3DCConfiguration.class);
            config = localConfig;
            ModbusCommunicationInterface localComms = connectEndpoint();
            if (localComms != null) {
                // register low speed info poller
                ModbusReadRequestBlueprint infoRequest = new ModbusReadRequestBlueprint(slaveId,
                        ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, INFO_REG_START, INFO_REG_SIZE, 3);
                infoPoller = localComms.registerRegularPoll(infoRequest, INFO_POLL_REFRESH_TIME_MS, 0,
                        this::handleInfoResult, this::handleInfoFailure);

                ModbusReadRequestBlueprint dataRequest = new ModbusReadRequestBlueprint(slaveId,
                        ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, POWER_REG_START,
                        REGISTER_LENGTH - INFO_REG_SIZE, 3);
                if (config != null) {
                    dataPoller = localComms.registerRegularPoll(dataRequest, localConfig.refresh, 0,
                            this::handleDataResult, this::handleDataFailure);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "E3DC Configuration missing");
                }
            } // else state handling performed in connectEndPoint function
        });
    }

    /**
     * Get a reference to the modbus endpoint
     */
    private @Nullable ModbusCommunicationInterface connectEndpoint() {
        if (comms != null) {
            return comms;
        }

        ModbusEndpointThingHandler slaveEndpointThingHandler = getEndpointThingHandler();
        if (slaveEndpointThingHandler == null) {
            @SuppressWarnings("null")
            String label = Optional.ofNullable(getBridge()).map(b -> b.getLabel()).orElse("<null>");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                    String.format("Bridge '%s' is offline", label));
            return null;
        }
        try {
            slaveId = slaveEndpointThingHandler.getSlaveId();
            comms = slaveEndpointThingHandler.getCommunicationInterface();
        } catch (EndpointNotInitializedException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    String.format("Slave Endpoint not initialized"));
            return null;
        }
        if (comms == null) {
            @SuppressWarnings("null")
            String label = Optional.ofNullable(getBridge()).map(b -> b.getLabel()).orElse("<null>");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                    String.format("Bridge '%s' not completely initialized", label));
            return null;
        } else {
            return comms;
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
    private ChannelUID channelUID(Thing t, String group, String id) {
        return new ChannelUID(t.getUID(), group, id);
    }

    void handleInfoResult(AsyncModbusReadResult result) {
        if (infoRead != ReadStatus.READ_SUCCESS) {
            // update status only if bit switches
            infoRead = ReadStatus.READ_SUCCESS;
            updateStatus();
        }
        infoParser.handle(result);
        Optional<Data> blockOpt = infoParser.parse(DataType.INFO);
        if (blockOpt.isPresent()) {
            InfoBlock block = (InfoBlock) blockOpt.get();
            updateState(modbusIdChannel, block.modbusId);
            updateState(modbusVersionChannel, block.modbusVersion);
            updateState(supportedRegistersChannel, block.supportedRegisters);
            updateState(manufacturerChannel, block.manufacturer);
            updateState(modelNameChannel, block.modelName);
            updateState(serialNumberChannel, block.serialNumber);
            updateState(firmwareChannel, block.firmware);
        } else {
            logger.debug("Unable to get {} from provider {}", DataType.INFO, dataParser.toString());
        }
    }

    void handleInfoFailure(AsyncModbusFailure<ModbusReadRequestBlueprint> result) {
        if (infoRead != ReadStatus.READ_FAILED) {
            // update status only if bit switches
            infoRead = ReadStatus.READ_FAILED;
            updateStatus();
        }
    }

    void handleDataResult(AsyncModbusReadResult result) {
        if (dataRead != ReadStatus.READ_SUCCESS) {
            // update status only if bit switches
            dataRead = ReadStatus.READ_SUCCESS;
            updateStatus();
        }
        dataParser.handle(result);
        // Update channels in emergency group
        {
            Optional<Data> blockOpt = dataParser.parse(DataType.EMERGENCY);
            if (blockOpt.isPresent()) {
                EmergencyBlock block = (EmergencyBlock) blockOpt.get();
                updateState(epStatusChannel, block.epStatus);
                updateState(batteryChargingLockedChannel, block.batteryChargingLocked);
                updateState(batteryDischargingLockedChannel, block.batteryDischargingLocked);
                updateState(epPossibleChannel, block.epPossible);
                updateState(weatherPredictedChargingChannel, block.weatherPredictedCharging);
                updateState(regulationStatusChannel, block.regulationStatus);
                updateState(chargeLockTimeChannel, block.chargeLockTime);
                updateState(dischargeLockTimeChannel, block.dischargeLockTime);
            } else {
                logger.debug("Unable to get {} from provider {}", DataType.EMERGENCY, dataParser.toString());
            }
        }

        // Update channels in power group
        {
            Optional<Data> blockOpt = dataParser.parse(DataType.POWER);
            if (blockOpt.isPresent()) {
                PowerBlock block = (PowerBlock) blockOpt.get();
                updateState(pvPowerSupplyChannel, block.pvPowerSupply);
                updateState(batteryPowerSupplyChannel, block.batteryPowerSupply);
                updateState(batteryPowerConsumptionChannel, block.batteryPowerConsumption);
                updateState(householdPowerConsumptionChannel, block.householdPowerConsumption);
                updateState(gridPowerConsumpitionChannel, block.gridPowerConsumpition);
                updateState(gridPowerSupplyChannel, block.gridPowerSupply);
                updateState(externalPowerSupplyChannel, block.externalPowerSupply);
                updateState(wallboxPowerConsumptionChannel, block.wallboxPowerConsumption);
                updateState(wallboxPVPowerConsumptionChannel, block.wallboxPVPowerConsumption);
                updateState(autarkyChannel, block.autarky);
                updateState(selfConsumptionChannel, block.selfConsumption);
                updateState(batterySOCChannel, block.batterySOC);
            } else {
                logger.debug("Unable to get {} from provider {}", DataType.POWER, dataParser.toString());
            }
        }

        // Update channels in strings group
        {
            Optional<Data> blockOpt = dataParser.parse(DataType.STRINGS);
            if (blockOpt.isPresent()) {
                StringBlock block = (StringBlock) blockOpt.get();
                updateState(string1AmpereChannel, block.string1Ampere);
                updateState(string1VoltChannel, block.string1Volt);
                updateState(string1WattChannel, block.string1Watt);
                updateState(string2AmpereChannel, block.string2Ampere);
                updateState(string2VoltChannel, block.string2Volt);
                updateState(string2WattChannel, block.string2Watt);
                updateState(string3AmpereChannel, block.string3Ampere);
                updateState(string3VoltChannel, block.string3Volt);
                updateState(string3WattChannel, block.string3Watt);
            } else {
                logger.debug("Unable to get {} from provider {}", DataType.STRINGS, dataParser.toString());
            }
        }

        listeners.forEach(l -> {
            l.handle(result);
        });
    }

    void handleDataFailure(AsyncModbusFailure<ModbusReadRequestBlueprint> result) {
        if (dataRead != ReadStatus.READ_FAILED) {
            // update status only if bit switches
            dataRead = ReadStatus.READ_FAILED;
            updateStatus();
        }
        listeners.forEach(l -> {
            l.handleError(result);
        });
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

    private void updateStatus() {
        logger.debug("Status update: Info {} Data {} ", infoRead, dataRead);
        if (infoRead != ReadStatus.NOT_RECEIVED && dataRead != ReadStatus.NOT_RECEIVED) {
            if (infoRead == dataRead) {
                // both reads are ok or else both failed
                if (infoRead == ReadStatus.READ_SUCCESS) {
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, INFO_DATA_READ_ERROR);
                }
            } else {
                // either info or data read failed - update status with details
                if (infoRead == ReadStatus.READ_FAILED) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, INFO_READ_ERROR);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, DATA_READ_ERROR);
                }
            }
        } // else - one status isn't received yet - wait until both Modbus polls returns either success or error
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        listeners.add((E3DCWallboxThingHandler) childHandler);
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        listeners.remove(childHandler);
    }
}
