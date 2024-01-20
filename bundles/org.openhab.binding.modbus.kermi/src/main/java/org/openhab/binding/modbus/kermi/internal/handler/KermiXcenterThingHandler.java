/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.modbus.kermi.internal.handler;

import static org.openhab.binding.modbus.kermi.internal.KermiBindingConstants.GLOBAL_STATE_CHANNEL;
import static org.openhab.binding.modbus.kermi.internal.KermiBindingConstants.GLOBAL_STATE_ID_CHANNEL;
import static org.openhab.binding.modbus.kermi.internal.modbus.KermiModbusConstans.STATE_POLL_REFRESH_TIME_MS;
import static org.openhab.binding.modbus.kermi.internal.modbus.KermiModbusConstans.STATE_REG_SIZE;
import static org.openhab.binding.modbus.kermi.internal.modbus.KermiModbusConstans.STATE_REG_START;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.modbus.handler.EndpointNotInitializedException;
import org.openhab.binding.modbus.handler.ModbusEndpointThingHandler;
import org.openhab.binding.modbus.kermi.internal.KermiConfiguration;
import org.openhab.binding.modbus.kermi.internal.dto.StateDTO;
import org.openhab.binding.modbus.kermi.internal.modbus.Data;
import org.openhab.binding.modbus.kermi.internal.modbus.Parser;
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
 * The {@link KermiXcenterThingHandler} Basic modbus connection to the Kermi device(s)
 *
 * @author Kai Neuhaus - Initial contribution
 */
@NonNullByDefault
public class KermiXcenterThingHandler extends BaseBridgeHandler {
    public enum ReadStatus {
        NOT_RECEIVED,
        READ_SUCCESS,
        READ_FAILED
    }

    static final String INFO_DATA_READ_ERROR = "Information And Data Modbus Read Errors";
    static final String INFO_READ_ERROR = "Information Modbus Read Error";
    static final String DATA_READ_ERROR = "Data Modbus Read Error";

    static final String STATE_GROUP = "xcenter-state";

    private ChannelUID globalStateChannel;
    private ChannelUID globalStateIdChannel;
    // private ChannelUID globalAlarmChannel;

    // private final ArrayList<E3DCWallboxThingHandler> listeners = new ArrayList<>();
    private final Logger logger = LoggerFactory.getLogger(KermiXcenterThingHandler.class);
    private final Parser dataParser = new Parser(Data.DataType.DATA);
    private ReadStatus dataRead = ReadStatus.NOT_RECEIVED;
    private final Parser infoParser = new Parser(Data.DataType.INFO);
    private ReadStatus infoRead = ReadStatus.NOT_RECEIVED;
    private @Nullable PollTask infoPoller;
    private @Nullable PollTask dataPoller;
    private @Nullable KermiConfiguration config;

    /**
     * Communication interface to the slave endpoint we're connecting to
     */
    protected volatile @Nullable ModbusCommunicationInterface comms = null;
    private int slaveId;

    public KermiXcenterThingHandler(Bridge thing) {
        super(thing);

        globalStateChannel = channelUID(thing, STATE_GROUP, GLOBAL_STATE_CHANNEL);
        globalStateIdChannel = channelUID(thing, STATE_GROUP, GLOBAL_STATE_ID_CHANNEL);
    }

    public @Nullable ModbusCommunicationInterface getComms() {
        return comms;
    }

    public int getSlaveId() {
        return slaveId;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // no control of Kermi device possible yet
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        scheduler.execute(() -> {
            KermiConfiguration localConfig = getConfigAs(KermiConfiguration.class);
            config = localConfig;
            ModbusCommunicationInterface localComms = connectEndpoint();
            if (localComms != null) {
                // register low speed info poller
                ModbusReadRequestBlueprint infoRequest = new ModbusReadRequestBlueprint(slaveId,
                        ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, STATE_REG_START, STATE_REG_SIZE, 3);
                infoPoller = localComms.registerRegularPoll(infoRequest, STATE_POLL_REFRESH_TIME_MS, 0,
                        this::handleInfoResult, this::handleInfoFailure);

                // ModbusReadRequestBlueprint dataRequest = new ModbusReadRequestBlueprint(slaveId,
                // ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, POWER_REG_START,
                // REGISTER_LENGTH - INFO_REG_SIZE, 3);
                // if (config != null) {
                // dataPoller = localComms.registerRegularPoll(dataRequest, localConfig.refresh, 0,
                // this::handleDataResult, this::handleDataFailure);
                // } else {
                // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                // "E3DC Configuration missing");
                // }
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

        if (handler instanceof ModbusEndpointThingHandler thingHandler) {
            return thingHandler;
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
        Optional<Data> dtoOpt = infoParser.parse(Data.DataType.INFO);
        if (dtoOpt.isPresent()) {
            StateDTO dto = (StateDTO) dtoOpt.get();
            updateState(globalStateChannel, dto.globalState);
            updateState(globalStateIdChannel, dto.globalStateId);
            // updateState(modbusVersionChannel, dto.modbusVersion);
            // updateState(supportedRegistersChannel, dto.supportedRegisters);
            // updateState(manufacturerChannel, dto.manufacturer);
            // updateState(modelNameChannel, dto.modelName);
            // updateState(serialNumberChannel, dto.serialNumber);
            // updateState(firmwareChannel, dto.firmware);
        } else {
            logger.debug("Unable to get {} from provider {}", Data.DataType.INFO, dataParser.toString());
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
        // {
        // Optional<Data> blockOpt = dataParser.parse(DataType.EMERGENCY);
        // if (blockOpt.isPresent()) {
        // EmergencyBlock block = (EmergencyBlock) blockOpt.get();
        // updateState(epStatusChannel, block.epStatus);
        // updateState(batteryChargingLockedChannel, block.batteryChargingLocked);
        // updateState(batteryDischargingLockedChannel, block.batteryDischargingLocked);
        // updateState(epPossibleChannel, block.epPossible);
        // updateState(weatherPredictedChargingChannel, block.weatherPredictedCharging);
        // updateState(regulationStatusChannel, block.regulationStatus);
        // updateState(chargeLockTimeChannel, block.chargeLockTime);
        // updateState(dischargeLockTimeChannel, block.dischargeLockTime);
        // } else {
        // logger.debug("Unable to get {} from provider {}", DataType.EMERGENCY, dataParser.toString());
        // }
        // }

        // Update channels in power group
        // {
        // Optional<Data> blockOpt = dataParser.parse(DataType.POWER);
        // if (blockOpt.isPresent()) {
        // PowerBlock block = (PowerBlock) blockOpt.get();
        // updateState(pvPowerSupplyChannel, block.pvPowerSupply);
        // updateState(batteryPowerSupplyChannel, block.batteryPowerSupply);
        // updateState(batteryPowerConsumptionChannel, block.batteryPowerConsumption);
        // updateState(householdPowerConsumptionChannel, block.householdPowerConsumption);
        // updateState(gridPowerConsumpitionChannel, block.gridPowerConsumpition);
        // updateState(gridPowerSupplyChannel, block.gridPowerSupply);
        // updateState(externalPowerSupplyChannel, block.externalPowerSupply);
        // updateState(wallboxPowerConsumptionChannel, block.wallboxPowerConsumption);
        // updateState(wallboxPVPowerConsumptionChannel, block.wallboxPVPowerConsumption);
        // updateState(autarkyChannel, block.autarky);
        // updateState(selfConsumptionChannel, block.selfConsumption);
        // updateState(batterySOCChannel, block.batterySOC);
        // if (config != null) {
        // if (config.batteryCapacity > 0) {
        // double soc = block.batterySOC.doubleValue();
        // QuantityType<Energy> charged = QuantityType.valueOf(soc * config.batteryCapacity / 100,
        // Units.KILOWATT_HOUR);
        // updateState(batteryChargedChannel, charged);
        // QuantityType<Energy> uncharged = QuantityType
        // .valueOf((100 - soc) * config.batteryCapacity / 100, Units.KILOWATT_HOUR);
        // updateState(batteryUnchargedChannel, uncharged);
        // }
        // }
        // } else {
        // logger.debug("Unable to get {} from provider {}", DataType.POWER, dataParser.toString());
        // }
        // }

        // Update channels in strings group
        // {
        // Optional<Data> blockOpt = dataParser.parse(DataType.STRINGS);
        // if (blockOpt.isPresent()) {
        // StringBlock block = (StringBlock) blockOpt.get();
        // updateState(string1AmpereChannel, block.string1Ampere);
        // updateState(string1VoltChannel, block.string1Volt);
        // updateState(string1WattChannel, block.string1Watt);
        // updateState(string2AmpereChannel, block.string2Ampere);
        // updateState(string2VoltChannel, block.string2Volt);
        // updateState(string2WattChannel, block.string2Watt);
        // updateState(string3AmpereChannel, block.string3Ampere);
        // updateState(string3VoltChannel, block.string3Volt);
        // updateState(string3WattChannel, block.string3Watt);
        // } else {
        // logger.debug("Unable to get {} from provider {}", DataType.STRINGS, dataParser.toString());
        // }
        // }

        // Reactivate when KermiKomponents are included / implemented
        // listeners.forEach(l -> {
        // l.handle(result);
        // });
    }

    void handleDataFailure(AsyncModbusFailure<ModbusReadRequestBlueprint> result) {
        if (dataRead != ReadStatus.READ_FAILED) {
            // update status only if bit switches
            dataRead = ReadStatus.READ_FAILED;
            updateStatus();
        }
        // Reactivate when KermiKomponents are included / implemented
        // listeners.forEach(l -> {
        // l.handleError(result);
        // });
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
        if (infoRead != ReadStatus.NOT_RECEIVED) { // && dataRead != ReadStatus.NOT_RECEIVED
            if (infoRead == ReadStatus.READ_SUCCESS) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, INFO_READ_ERROR);
            }
            // if (infoRead == dataRead) {
            // // both reads are ok or else both failed
            // if (infoRead == ReadStatus.READ_SUCCESS) {
            // updateStatus(ThingStatus.ONLINE);
            // } else {
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, INFO_DATA_READ_ERROR);
            // }
            // } else {
            // // either info or data read failed - update status with details
            // if (infoRead == ReadStatus.READ_FAILED) {
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, INFO_READ_ERROR);
            // } else {
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, DATA_READ_ERROR);
            // }
            // }
        } // else - one status isn't received yet - wait until both Modbus polls returns either success or error
    }

    /*
     * @Override
     * public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
     * listeners.add((E3DCWallboxThingHandler) childHandler);
     * }
     *
     * @Override
     * public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
     * listeners.remove(childHandler);
     * }
     */
}
