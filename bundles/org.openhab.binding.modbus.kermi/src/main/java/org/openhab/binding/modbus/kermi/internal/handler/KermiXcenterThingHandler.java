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

import static org.openhab.binding.modbus.kermi.internal.KermiBindingConstants.ALARM_STATE_CHANNEL;
import static org.openhab.binding.modbus.kermi.internal.KermiBindingConstants.COP_CHANNEL;
import static org.openhab.binding.modbus.kermi.internal.KermiBindingConstants.COP_COOLING_CHANNEL;
import static org.openhab.binding.modbus.kermi.internal.KermiBindingConstants.COP_DRINKINGWATER_CHANNEL;
import static org.openhab.binding.modbus.kermi.internal.KermiBindingConstants.COP_HEATING_CHANNEL;
import static org.openhab.binding.modbus.kermi.internal.KermiBindingConstants.ELECTRIC_POWER_CHANNEL;
import static org.openhab.binding.modbus.kermi.internal.KermiBindingConstants.ELECTRIC_POWER_COOLING_CHANNEL;
import static org.openhab.binding.modbus.kermi.internal.KermiBindingConstants.ELECTRIC_POWER_DRINKINGWATER_CHANNEL;
import static org.openhab.binding.modbus.kermi.internal.KermiBindingConstants.ELECTRIC_POWER_HEATING_CHANNEL;
import static org.openhab.binding.modbus.kermi.internal.KermiBindingConstants.EXIT_TEMPERATURE_CHANNEL;
import static org.openhab.binding.modbus.kermi.internal.KermiBindingConstants.FLOW_SPEED_CHANNEL;
import static org.openhab.binding.modbus.kermi.internal.KermiBindingConstants.FLOW_TEMPERATURE_CHANNEL;
import static org.openhab.binding.modbus.kermi.internal.KermiBindingConstants.GLOBAL_STATE_CHANNEL;
import static org.openhab.binding.modbus.kermi.internal.KermiBindingConstants.GLOBAL_STATE_ID_CHANNEL;
import static org.openhab.binding.modbus.kermi.internal.KermiBindingConstants.INCOMING_TEMPERATURE_CHANNEL;
import static org.openhab.binding.modbus.kermi.internal.KermiBindingConstants.POWER_CHANNEL;
import static org.openhab.binding.modbus.kermi.internal.KermiBindingConstants.POWER_COOLING_CHANNEL;
import static org.openhab.binding.modbus.kermi.internal.KermiBindingConstants.POWER_DRINKINGWATER_CHANNEL;
import static org.openhab.binding.modbus.kermi.internal.KermiBindingConstants.POWER_HEATING_CHANNEL;
import static org.openhab.binding.modbus.kermi.internal.KermiBindingConstants.PV_POWER_CHANNEL;
import static org.openhab.binding.modbus.kermi.internal.KermiBindingConstants.PV_STATE_CHANNEL;
import static org.openhab.binding.modbus.kermi.internal.KermiBindingConstants.PV_TARGET_TEMPERATURE_DRINKINGWATER_CHANNEL;
import static org.openhab.binding.modbus.kermi.internal.KermiBindingConstants.PV_TARGET_TEMPERATURE_HEATING_CHANNEL;
import static org.openhab.binding.modbus.kermi.internal.KermiBindingConstants.RETURN_TEMPERATURE_CHANNEL;
import static org.openhab.binding.modbus.kermi.internal.KermiBindingConstants.TEMPERATURE_SENSOR_OUTSIDE_CHANNEL;
import static org.openhab.binding.modbus.kermi.internal.KermiBindingConstants.WORKHOURS_COMPRESSOR_CHANNEL;
import static org.openhab.binding.modbus.kermi.internal.KermiBindingConstants.WORKHOURS_FAN_CHANNEL;
import static org.openhab.binding.modbus.kermi.internal.KermiBindingConstants.WORKHOURS_STORAGE_LOADING_PUMP_CHANNEL;
import static org.openhab.binding.modbus.kermi.internal.modbus.KermiModbusConstans.ALARM_REG_SIZE;
import static org.openhab.binding.modbus.kermi.internal.modbus.KermiModbusConstans.ALARM_REG_START;
import static org.openhab.binding.modbus.kermi.internal.modbus.KermiModbusConstans.CHARGING_CIRCUIT_REG_SIZE;
import static org.openhab.binding.modbus.kermi.internal.modbus.KermiModbusConstans.CHARGING_CIRCUIT_REG_START;
import static org.openhab.binding.modbus.kermi.internal.modbus.KermiModbusConstans.ENERGY_SOURCE_REG_SIZE;
import static org.openhab.binding.modbus.kermi.internal.modbus.KermiModbusConstans.ENERGY_SOURCE_REG_START;
import static org.openhab.binding.modbus.kermi.internal.modbus.KermiModbusConstans.POWER_REG_SIZE;
import static org.openhab.binding.modbus.kermi.internal.modbus.KermiModbusConstans.POWER_REG_START;
import static org.openhab.binding.modbus.kermi.internal.modbus.KermiModbusConstans.PV_MODULATION_REG_SIZE;
import static org.openhab.binding.modbus.kermi.internal.modbus.KermiModbusConstans.PV_MODULATION_REG_START;
import static org.openhab.binding.modbus.kermi.internal.modbus.KermiModbusConstans.SLOW_POLL_REFRESH_TIME_MS;
import static org.openhab.binding.modbus.kermi.internal.modbus.KermiModbusConstans.STATE_POLL_REFRESH_TIME_MS;
import static org.openhab.binding.modbus.kermi.internal.modbus.KermiModbusConstans.STATE_REG_SIZE;
import static org.openhab.binding.modbus.kermi.internal.modbus.KermiModbusConstans.STATE_REG_START;
import static org.openhab.binding.modbus.kermi.internal.modbus.KermiModbusConstans.WORK_HOURS_REG_SIZE;
import static org.openhab.binding.modbus.kermi.internal.modbus.KermiModbusConstans.WORK_HOURS_REG_START;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.modbus.handler.EndpointNotInitializedException;
import org.openhab.binding.modbus.handler.ModbusEndpointThingHandler;
import org.openhab.binding.modbus.kermi.internal.KermiConfiguration;
import org.openhab.binding.modbus.kermi.internal.dto.AlarmDTO;
import org.openhab.binding.modbus.kermi.internal.dto.ChargingCircuitDTO;
import org.openhab.binding.modbus.kermi.internal.dto.EnergySourceDTO;
import org.openhab.binding.modbus.kermi.internal.dto.PowerDTO;
import org.openhab.binding.modbus.kermi.internal.dto.PvDTO;
import org.openhab.binding.modbus.kermi.internal.dto.StateDTO;
import org.openhab.binding.modbus.kermi.internal.dto.WorkHoursDTO;
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

    static final String STATE_AND_ALARM_READ_ERRORS = "Status And Alarm Modbus Read Errors";
    static final String STATE_READ_ERROR = "Information Modbus Read Error";
    static final String DATA_READ_ERROR = "Data Modbus Read Error";
    static final String PV_READ_ERROR = "PV Modbus Read Error";

    static final String ALARM_GROUP = "xcenter-alarm";
    static final String STATE_GROUP = "xcenter-state";
    static final String ENERGYSOURCE_GROUP = "xcenter-energysource";
    static final String CHARGINGCIRCUIT_GROUP = "xcenter-chargingcircuit";
    static final String POWER_GROUP = "xcenter-power";
    static final String WORKHOURS_GROUP = "xcenter-workhours";
    static final String PV_GROUP = "xcenter-pvmodulation";

    // Energy source
    private ChannelUID exitTemperatureChannel;
    private ChannelUID incomingTemperatureChannel;
    private ChannelUID outsideTemperatureChannel;

    // Charging circuit
    private ChannelUID flowTemperatureChannel;
    private ChannelUID returnFlowTemperatureChannel;
    private ChannelUID flowSpeedChannel;

    // Power
    private ChannelUID copChannel;
    private ChannelUID copHeatingChannel;
    private ChannelUID copDrinkingWaterChannel;
    private ChannelUID copCoolingChannel;

    private ChannelUID powerChannel;
    private ChannelUID powerHeatingChannel;
    private ChannelUID powerDrinkingWaterChannel;
    private ChannelUID powerCoolingChannel;

    private ChannelUID electricPowerChannel;
    private ChannelUID electricPowerHeatingChannel;
    private ChannelUID electricPowerDrinkingWaterChannel;
    private ChannelUID electricPowerCoolingChannel;

    // Global State
    private ChannelUID globalStateChannel;
    private ChannelUID globalStateIdChannel;

    // Alarm State
    private ChannelUID alarmStateChannel;

    // Work hours
    private ChannelUID workHoursFanChannel;
    private ChannelUID workHoursStorageLoadingPumpChannel;
    private ChannelUID workHoursCompressorChannel;

    // PV Modulation
    private ChannelUID pvStateChannel;
    private ChannelUID pvPowerChannel;
    private ChannelUID pvTargetTemperatureHeatingChannel; // READ-WRITE
    private ChannelUID pvTargetTemperatureDrinkingWaterChannel; // READ-WRITE

    // private final ArrayList<E3DCWallboxThingHandler> listeners = new ArrayList<>();
    private final Logger logger = LoggerFactory.getLogger(KermiXcenterThingHandler.class);

    private final Parser alarmParser = new Parser(Data.DataType.ALARM_STATE);
    private ReadStatus alarmRead = ReadStatus.NOT_RECEIVED;
    private final Parser chargingCircuitParser = new Parser(Data.DataType.CHARGING_CIRCUIT);
    private ReadStatus chargingCircuitRead = ReadStatus.NOT_RECEIVED;
    private final Parser energySourceParser = new Parser(Data.DataType.ENERGY_SOURCE);
    private ReadStatus energySourceRead = ReadStatus.NOT_RECEIVED;
    private final Parser powerParser = new Parser(Data.DataType.POWER);
    private ReadStatus powerRead = ReadStatus.NOT_RECEIVED;
    private final Parser pvParser = new Parser(Data.DataType.PV);
    private ReadStatus pvRead = ReadStatus.NOT_RECEIVED;
    private final Parser stateParser = new Parser(Data.DataType.STATE);
    private ReadStatus stateRead = ReadStatus.NOT_RECEIVED;
    private final Parser workHoursParser = new Parser(Data.DataType.WORK_HOURS);
    private ReadStatus workHoursRead = ReadStatus.NOT_RECEIVED;

    private @Nullable PollTask alarmPoller;
    private @Nullable PollTask chargingCircuitPoller;
    private @Nullable PollTask energySourcePoller;
    private @Nullable PollTask powerPoller;
    private @Nullable PollTask pvPoller;
    private @Nullable PollTask statePoller;
    private @Nullable PollTask workHourPoller;

    private List<@Nullable PollTask> pollTasks = new ArrayList<>();

    // private @Nullable PollTask testPoller;
    private @Nullable KermiConfiguration config;

    /**
     * Communication interface to the slave endpoint we're connecting to
     */
    protected volatile @Nullable ModbusCommunicationInterface comms = null;
    private int slaveId;

    public KermiXcenterThingHandler(Bridge thing) {
        super(thing);

        // STATE
        globalStateChannel = channelUID(thing, STATE_GROUP, GLOBAL_STATE_CHANNEL);
        globalStateIdChannel = channelUID(thing, STATE_GROUP, GLOBAL_STATE_ID_CHANNEL);

        alarmStateChannel = channelUID(thing, ALARM_GROUP, ALARM_STATE_CHANNEL);

        // Energy source
        exitTemperatureChannel = channelUID(thing, ENERGYSOURCE_GROUP, EXIT_TEMPERATURE_CHANNEL);
        incomingTemperatureChannel = channelUID(thing, ENERGYSOURCE_GROUP, INCOMING_TEMPERATURE_CHANNEL);
        outsideTemperatureChannel = channelUID(thing, ENERGYSOURCE_GROUP, TEMPERATURE_SENSOR_OUTSIDE_CHANNEL);

        // Loading circuit
        flowTemperatureChannel = channelUID(thing, CHARGINGCIRCUIT_GROUP, FLOW_TEMPERATURE_CHANNEL);
        returnFlowTemperatureChannel = channelUID(thing, CHARGINGCIRCUIT_GROUP, RETURN_TEMPERATURE_CHANNEL);
        flowSpeedChannel = channelUID(thing, CHARGINGCIRCUIT_GROUP, FLOW_SPEED_CHANNEL);

        // Power
        copChannel = channelUID(thing, POWER_GROUP, COP_CHANNEL);
        copHeatingChannel = channelUID(thing, POWER_GROUP, COP_HEATING_CHANNEL);
        copDrinkingWaterChannel = channelUID(thing, POWER_GROUP, COP_DRINKINGWATER_CHANNEL);
        copCoolingChannel = channelUID(thing, POWER_GROUP, COP_COOLING_CHANNEL);

        powerChannel = channelUID(thing, POWER_GROUP, POWER_CHANNEL);
        powerHeatingChannel = channelUID(thing, POWER_GROUP, POWER_HEATING_CHANNEL);
        powerDrinkingWaterChannel = channelUID(thing, POWER_GROUP, POWER_DRINKINGWATER_CHANNEL);
        powerCoolingChannel = channelUID(thing, POWER_GROUP, POWER_COOLING_CHANNEL);

        electricPowerChannel = channelUID(thing, POWER_GROUP, ELECTRIC_POWER_CHANNEL);
        electricPowerHeatingChannel = channelUID(thing, POWER_GROUP, ELECTRIC_POWER_HEATING_CHANNEL);
        electricPowerDrinkingWaterChannel = channelUID(thing, POWER_GROUP, ELECTRIC_POWER_DRINKINGWATER_CHANNEL);
        electricPowerCoolingChannel = channelUID(thing, POWER_GROUP, ELECTRIC_POWER_COOLING_CHANNEL);

        // Work hours
        workHoursFanChannel = channelUID(thing, WORKHOURS_GROUP, WORKHOURS_FAN_CHANNEL);
        workHoursStorageLoadingPumpChannel = channelUID(thing, WORKHOURS_GROUP, WORKHOURS_STORAGE_LOADING_PUMP_CHANNEL);
        workHoursCompressorChannel = channelUID(thing, WORKHOURS_GROUP, WORKHOURS_COMPRESSOR_CHANNEL);

        // PV Modulation
        pvStateChannel = channelUID(thing, PV_GROUP, PV_STATE_CHANNEL);
        pvPowerChannel = channelUID(thing, PV_GROUP, PV_POWER_CHANNEL);
        pvTargetTemperatureHeatingChannel = channelUID(thing, PV_GROUP, PV_TARGET_TEMPERATURE_HEATING_CHANNEL); // READ-WRITE
        pvTargetTemperatureDrinkingWaterChannel = channelUID(thing, PV_GROUP,
                PV_TARGET_TEMPERATURE_DRINKINGWATER_CHANNEL); // READ-WRITE
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

            if (config == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Kermi Configuration missing");
                return;
            }
            ModbusCommunicationInterface localComms = connectEndpoint();
            if (localComms != null) {

                // very slow requests
                ModbusReadRequestBlueprint workHoursRequest = new ModbusReadRequestBlueprint(slaveId,
                        ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, WORK_HOURS_REG_START, WORK_HOURS_REG_SIZE, 3);
                workHourPoller = localComms.registerRegularPoll(workHoursRequest, SLOW_POLL_REFRESH_TIME_MS, 0,
                        this::handleWorkHoursResult, this::handleWorkHoursFailure);

                pollTasks.add(workHourPoller);

                // register low speed state & alarm poller
                ModbusReadRequestBlueprint alarmRequest = new ModbusReadRequestBlueprint(slaveId,
                        ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, ALARM_REG_START, ALARM_REG_SIZE, 3);
                alarmPoller = localComms.registerRegularPoll(alarmRequest, STATE_POLL_REFRESH_TIME_MS, 0,
                        this::handleAlarmResult, this::handleAlarmFailure);

                pollTasks.add(alarmPoller);

                ModbusReadRequestBlueprint stateRequest = new ModbusReadRequestBlueprint(slaveId,
                        ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, STATE_REG_START, STATE_REG_SIZE, 3);
                statePoller = localComms.registerRegularPoll(stateRequest, STATE_POLL_REFRESH_TIME_MS, 0,
                        this::handleStateResult, this::handleStateFailure);

                pollTasks.add(statePoller);

                // default polling speed
                ModbusReadRequestBlueprint chargingcircuitRequest = new ModbusReadRequestBlueprint(slaveId,
                        ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, CHARGING_CIRCUIT_REG_START,
                        CHARGING_CIRCUIT_REG_SIZE, 3);
                chargingCircuitPoller = localComms.registerRegularPoll(chargingcircuitRequest, localConfig.refresh, 0,
                        this::handleChargingCircuitResult, this::handleChargingCircuitFailure);

                pollTasks.add(chargingCircuitPoller);

                ModbusReadRequestBlueprint energySourceRequest = new ModbusReadRequestBlueprint(slaveId,
                        ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, ENERGY_SOURCE_REG_START, ENERGY_SOURCE_REG_SIZE,
                        3);
                energySourcePoller = localComms.registerRegularPoll(energySourceRequest, localConfig.refresh, 0,
                        this::handleEnergySourceResult, this::handleEnergySourceFailure);

                pollTasks.add(energySourcePoller);

                ModbusReadRequestBlueprint powerRequest = new ModbusReadRequestBlueprint(slaveId,
                        ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, POWER_REG_START, POWER_REG_SIZE, 3);
                powerPoller = localComms.registerRegularPoll(powerRequest, localConfig.refresh, 0,
                        this::handlePowerResult, this::handlePowerFailure);

                pollTasks.add(powerPoller);

                if (localConfig.pvEnabled) {
                    ModbusReadRequestBlueprint pvRequest = new ModbusReadRequestBlueprint(slaveId,
                            ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, PV_MODULATION_REG_START,
                            PV_MODULATION_REG_SIZE, 3);
                    pvPoller = localComms.registerRegularPoll(pvRequest, localConfig.refresh, 0, this::handlePvResult,
                            this::handlePvFailure);

                    pollTasks.add(pvPoller);
                }

                /*
                 * ModbusReadRequestBlueprint testPollerRequest = new ModbusReadRequestBlueprint(slaveId,
                 * ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, 1, 111, 3);
                 * testPoller = localComms.registerRegularPoll(testPollerRequest, SLOW_POLL_REFRESH_TIME_MS, 0,
                 * this::handleTestPollerResult, this::handleTestPollerFailure);
                 */

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
     * @param group String of channel group
     * @param id String the channel id in that group
     * @return the globally unique channel uid
     */
    private ChannelUID channelUID(Thing t, String group, String id) {
        return new ChannelUID(t.getUID(), group, id);
    }

    /******** Start ResultHandler *****************/

    private void handleAlarmResult(AsyncModbusReadResult result) {
        if (alarmRead != ReadStatus.READ_SUCCESS) {
            // update status only if bit switches
            alarmRead = ReadStatus.READ_SUCCESS;
            updateStatus();
        }
        alarmParser.handle(result);
        Optional<Data> dtoOpt = alarmParser.parse(Data.DataType.ALARM_STATE);
        if (dtoOpt.isPresent()) {
            AlarmDTO alarmDTO = (AlarmDTO) dtoOpt.get();
            updateState(alarmStateChannel, alarmDTO.alarmIsActive);
        } else {
            logger.debug("Unable to get {} from provider {}", Data.DataType.ALARM_STATE, alarmParser.toString());
        }
    }

    private void handleAlarmFailure(AsyncModbusFailure<ModbusReadRequestBlueprint> result) {
        if (alarmRead != ReadStatus.READ_FAILED) {
            // update status only if bit switches
            alarmRead = ReadStatus.READ_FAILED;
            logger.debug("Cause of failure: {}", result.getCause().getMessage());
            updateStatus();
        }
    }

    private void handleChargingCircuitResult(AsyncModbusReadResult result) {
        if (chargingCircuitRead != ReadStatus.READ_SUCCESS) {
            // update status only if bit switches
            chargingCircuitRead = ReadStatus.READ_SUCCESS;
            updateStatus();
        }
        chargingCircuitParser.handle(result);
        Optional<Data> dtoOpt = chargingCircuitParser.parse(Data.DataType.CHARGING_CIRCUIT);
        if (dtoOpt.isPresent()) {
            ChargingCircuitDTO chargingCircuitDTO = (ChargingCircuitDTO) dtoOpt.get();
            updateState(flowTemperatureChannel, chargingCircuitDTO.flowTemperature);
            updateState(returnFlowTemperatureChannel, chargingCircuitDTO.returnFlowTemperature);
            updateState(flowSpeedChannel, chargingCircuitDTO.flowSpeed);
        } else {
            logger.debug("Unable to get {} from provider {}", Data.DataType.CHARGING_CIRCUIT,
                    chargingCircuitParser.toString());
        }
    }

    private void handleChargingCircuitFailure(AsyncModbusFailure<ModbusReadRequestBlueprint> result) {
        if (chargingCircuitRead != ReadStatus.READ_FAILED) {
            // update status only if bit switches
            chargingCircuitRead = ReadStatus.READ_FAILED;
            logger.debug("Cause of failure: {}", result.getCause().getMessage());
            updateStatus();
        }
    }

    private void handleEnergySourceResult(AsyncModbusReadResult result) {
        if (energySourceRead != ReadStatus.READ_SUCCESS) {
            // update status only if bit switches
            energySourceRead = ReadStatus.READ_SUCCESS;
            updateStatus();
        }
        energySourceParser.handle(result);
        Optional<Data> dtoOpt = energySourceParser.parse(Data.DataType.ENERGY_SOURCE);
        if (dtoOpt.isPresent()) {
            EnergySourceDTO energySourceDTO = (EnergySourceDTO) dtoOpt.get();
            updateState(exitTemperatureChannel, energySourceDTO.exitTemperature);
            updateState(incomingTemperatureChannel, energySourceDTO.incomingTemperature);
            updateState(outsideTemperatureChannel, energySourceDTO.outsideTemperature);
        } else {
            logger.debug("Unable to get {} from provider {}", Data.DataType.ENERGY_SOURCE,
                    energySourceParser.toString());
        }
    }

    private void handleEnergySourceFailure(AsyncModbusFailure<ModbusReadRequestBlueprint> result) {
        if (energySourceRead != ReadStatus.READ_FAILED) {
            // update status only if bit switches
            energySourceRead = ReadStatus.READ_FAILED;
            logger.debug("Cause of failure: {}", result.getCause().getMessage());
            updateStatus();
        }
    }

    private void handlePowerResult(AsyncModbusReadResult result) {
        if (powerRead != ReadStatus.READ_SUCCESS) {
            // update status only if bit switches
            powerRead = ReadStatus.READ_SUCCESS;
            updateStatus();
        }
        powerParser.handle(result);
        Optional<Data> dtoOpt = powerParser.parse(Data.DataType.POWER);
        if (dtoOpt.isPresent()) {
            PowerDTO powerDTO = (PowerDTO) dtoOpt.get();

            // TODO Implement power
            updateState(copChannel, powerDTO.cop);
            updateState(copHeatingChannel, powerDTO.copHeating);
            updateState(copDrinkingWaterChannel, powerDTO.copDrinkingwater);
            updateState(copCoolingChannel, powerDTO.copCooling);

            updateState(powerChannel, powerDTO.power);
            updateState(powerHeatingChannel, powerDTO.powerHeating);
            updateState(powerDrinkingWaterChannel, powerDTO.powerDrinkingwater);
            updateState(powerCoolingChannel, powerDTO.powerCooling);

            updateState(electricPowerChannel, powerDTO.electricPower);
            updateState(electricPowerHeatingChannel, powerDTO.electricPowerHeating);
            updateState(electricPowerDrinkingWaterChannel, powerDTO.electricPowerDrinkingwater);
            updateState(electricPowerCoolingChannel, powerDTO.electricPowerCooling);

        } else {
            logger.debug("Unable to get {} from provider {}", Data.DataType.POWER, powerParser.toString());
        }

        // Reactivate when KermiKomponents are included / implemented
        // listeners.forEach(l -> {
        // l.handle(result);
        // });
    }

    private void handlePowerFailure(AsyncModbusFailure<ModbusReadRequestBlueprint> result) {
        if (powerRead != ReadStatus.READ_FAILED) {
            // update status only if bit switches
            powerRead = ReadStatus.READ_FAILED;
            logger.debug("Cause of failure: {}", result.getCause().getMessage());
            updateStatus();
        }
        // Reactivate when KermiKomponents are included / implemented
        // listeners.forEach(l -> {
        // l.handleError(result);
        // });
    }

    private void handlePvResult(AsyncModbusReadResult result) {
        if (pvRead != ReadStatus.READ_SUCCESS) {
            // update status only if bit switches
            pvRead = ReadStatus.READ_SUCCESS;
            updateStatus();
        }
        pvParser.handle(result);
        Optional<Data> dtoOpt = pvParser.parse(Data.DataType.PV);
        if (dtoOpt.isPresent()) {
            PvDTO pvDTO = (PvDTO) dtoOpt.get();
            updateState(pvStateChannel, pvDTO.pvModulationActive);
            updateState(pvPowerChannel, pvDTO.pvModulationPower);
            updateState(pvTargetTemperatureHeatingChannel, pvDTO.pvTargetTemperatureHeating);
            updateState(pvTargetTemperatureDrinkingWaterChannel, pvDTO.pvTargetTemperatureDrinkingwater);
        } else {
            logger.debug("Unable to get {} from provider {}", Data.DataType.PV, pvParser.toString());
        }
    }

    private void handlePvFailure(AsyncModbusFailure<ModbusReadRequestBlueprint> result) {
        if (pvRead != ReadStatus.READ_FAILED) {
            // update status only if bit switches
            pvRead = ReadStatus.READ_FAILED;
            logger.debug("Cause of failure: {}", result.getCause().getMessage());
            updateStatus();
        }
    }

    void handleStateResult(AsyncModbusReadResult result) {
        if (stateRead != ReadStatus.READ_SUCCESS) {
            // update status only if bit switches
            stateRead = ReadStatus.READ_SUCCESS;
            updateStatus();
        }
        stateParser.handle(result);
        Optional<Data> dtoOpt = stateParser.parse(Data.DataType.STATE);
        if (dtoOpt.isPresent()) {
            StateDTO dto = (StateDTO) dtoOpt.get();
            updateState(globalStateChannel, dto.globalState);
            updateState(globalStateIdChannel, dto.globalStateId);
        } else {
            logger.debug("Unable to get {} from provider {}", Data.DataType.STATE, stateParser.toString());
        }
    }

    void handleStateFailure(AsyncModbusFailure<ModbusReadRequestBlueprint> result) {
        if (stateRead != ReadStatus.READ_FAILED) {
            // update status only if bit switches
            stateRead = ReadStatus.READ_FAILED;
            logger.debug("Cause of failure: {}", result.getCause().getMessage());
            updateStatus();
        }
    }

    private void handleWorkHoursResult(AsyncModbusReadResult result) {
        workHoursParser.handle(result);
        Optional<Data> dtoOpt = workHoursParser.parse(Data.DataType.WORK_HOURS);
        if (dtoOpt.isPresent()) {
            WorkHoursDTO dto = (WorkHoursDTO) dtoOpt.get();
            updateState(workHoursFanChannel, dto.workHoursFan);
            updateState(workHoursStorageLoadingPumpChannel, dto.workHoursStorageLoadingPump);
            updateState(workHoursCompressorChannel, dto.workHoursCompressor);
        } else {
            logger.debug("Unable to get {} from provider {}", Data.DataType.WORK_HOURS, workHoursParser.toString());
        }
    }

    private void handleWorkHoursFailure(AsyncModbusFailure<ModbusReadRequestBlueprint> result) {
        if (workHoursRead != ReadStatus.READ_FAILED) {
            // update status only if bit switches
            workHoursRead = ReadStatus.READ_FAILED;
            logger.debug("Cause of failure: {}", result.getCause().getMessage());
            updateStatus();
        }
    }

    @Override
    public void dispose() {
        ModbusCommunicationInterface localComms = comms;
        if (localComms != null) {
            for (PollTask p : pollTasks) {
                PollTask localPoller = p;
                if (localPoller != null) {
                    localComms.unregisterRegularPoll(localPoller);
                }
            }
            /*
             * PollTask localInfoPoller = statePoller;
             * if (localInfoPoller != null) {
             * localComms.unregisterRegularPoll(localInfoPoller);
             * }
             * PollTask localDataPoller = dataPoller;
             * if (localDataPoller != null) {
             * localComms.unregisterRegularPoll(localDataPoller);
             * }
             */
        }
        // Comms will be close()'d by endpoint thing handler
        comms = null;
    }

    private void updateStatus() {
        logger.debug("Status update: State {} Data {} WorkHours {} PV {} ", stateRead, powerRead, workHoursRead,
                pvRead);
        if (stateRead != ReadStatus.NOT_RECEIVED) { // && dataRead != ReadStatus.NOT_RECEIVED
            if (stateRead == ReadStatus.READ_SUCCESS) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, STATE_READ_ERROR);
            }
            if (stateRead == alarmRead) {
                // both reads are ok or else both failed
                if (stateRead == ReadStatus.READ_SUCCESS) {
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            STATE_AND_ALARM_READ_ERRORS);
                }
            } else {
                // either info or data read failed - update status with details
                if (stateRead == ReadStatus.READ_FAILED) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, STATE_READ_ERROR);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, DATA_READ_ERROR);
                }
            }
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
