/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.openwebnet.internal.handler;

import static org.openhab.binding.openwebnet.internal.OpenWebNetBindingConstants.*;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.openwebnet.internal.OpenWebNetBindingConstants;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.UnDefType;
import org.openwebnet4j.communication.OWNException;
import org.openwebnet4j.message.BaseOpenMessage;
import org.openwebnet4j.message.FrameException;
import org.openwebnet4j.message.MalformedFrameException;
import org.openwebnet4j.message.Thermoregulation;
import org.openwebnet4j.message.Thermoregulation.WhatThermo;
import org.openwebnet4j.message.Where;
import org.openwebnet4j.message.WhereThermo;
import org.openwebnet4j.message.Who;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OpenWebNetThermoregulationHandler} is responsible for handling
 * commands/messages for Thermoregulation
 * Things. It extends the abstract {@link OpenWebNetThingHandler}.
 *
 * @author Massimo Valla - Initial contribution
 * @author Andrea Conte - Thermoregulation
 * @author Gilberto Cocchi - Thermoregulation
 */
@NonNullByDefault
public class OpenWebNetThermoregulationHandler extends OpenWebNetThingHandler {

    private final Logger logger = LoggerFactory.getLogger(OpenWebNetThermoregulationHandler.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = OpenWebNetBindingConstants.THERMOREGULATION_SUPPORTED_THING_TYPES;

    private double currentSetPointTemp = 11.5d; // 11.5 is the default setTemp used in MyHomeUP mobile app

    private Thermoregulation.Function currentFunction = Thermoregulation.Function.GENERIC;
    private Thermoregulation.OperationMode currentMode = Thermoregulation.OperationMode.MANUAL;

    private boolean isStandAlone = false;

    private boolean isCentralUnit = false;

    private String programNumber = "1";

    private static Set<String> probesInProtection = new HashSet<String>();
    private static Set<String> probesInOFF = new HashSet<String>();
    private static Set<String> probesInManual = new HashSet<String>();

    private static final String CU_REMOTE_CONTROL_ENABLED = "ENABLED";
    private static final String CU_REMOTE_CONTROL_DISABLED = "DISABLED";
    private static final String CU_BATTERY_OK = "OK";
    private static final String CU_BATTERY_KO = "KO";
    private static final Integer UNDOCUMENTED_WHAT_4001 = 4001;
    private static final Integer UNDOCUMENTED_WHAT_4002 = 4002;

    public OpenWebNetThermoregulationHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();

        ThingTypeUID thingType = thing.getThingTypeUID();
        isCentralUnit = OpenWebNetBindingConstants.THING_TYPE_BUS_THERMO_CU.equals(thingType);

        if (!isCentralUnit) {
            Object standAloneConfig = getConfig().get(OpenWebNetBindingConstants.CONFIG_PROPERTY_STANDALONE);
            if (standAloneConfig != null) {
                // null in case of thermo_sensor
                isStandAlone = Boolean.parseBoolean(standAloneConfig.toString());
            }
        } else {
            // central unit must have WHERE=0
            if (!deviceWhere.value().equals("0")) {
                logger.warn("initialize() Invalid WHERE={} for Central Unit.", deviceWhere.value());

                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/offline.conf-error-where");
            }

            // reset state of signal channels (they will be setted when specific messages
            // are received)
            updateState(CHANNEL_CU_AT_LEAST_ONE_PROBE_MANUAL, OnOffType.OFF);
            updateState(CHANNEL_CU_AT_LEAST_ONE_PROBE_OFF, OnOffType.OFF);
            updateState(CHANNEL_CU_AT_LEAST_ONE_PROBE_PROTECTION, OnOffType.OFF);
            updateState(CHANNEL_CU_SCENARIO_PROGRAM_NUMBER, new DecimalType(programNumber));
            updateState(CHANNEL_CU_WEEKLY_PROGRAM_NUMBER, new DecimalType(programNumber));
            updateState(CHANNEL_CU_FAILURE_DISCOVERED, OnOffType.OFF);
        }
    }

    @Override
    protected void handleChannelCommand(ChannelUID channel, Command command) {
        switch (channel.getId()) {
            case CHANNEL_TEMP_SETPOINT:
                handleSetpoint(command);
                break;
            case CHANNEL_FUNCTION:
                handleFunction(command);
                break;
            case CHANNEL_MODE:
                handleMode(command);
                break;
            case CHANNEL_FAN_SPEED:
                handleSetFanSpeed(command);
                break;
            case CHANNEL_CU_WEEKLY_PROGRAM_NUMBER:
            case CHANNEL_CU_SCENARIO_PROGRAM_NUMBER:
                handleSetProgramNumber(command);
                break;
            default: {
                logger.warn("handleChannelCommand() Unsupported ChannelUID {}", channel.getId());
            }
        }
    }

    @Override
    protected void requestChannelState(ChannelUID channel) {
        super.requestChannelState(channel);
        refreshDevice(false);
    }

    @Override
    protected Where buildBusWhere(String wStr) throws IllegalArgumentException {
        return new WhereThermo(wStr);
    }

    @Override
    protected String ownIdPrefix() {
        return Who.THERMOREGULATION.value().toString();
    }

    private void handleSetFanSpeed(Command command) {
        if (command instanceof StringType) {
            Where w = deviceWhere;
            if (w != null) {
                try {
                    Thermoregulation.FanCoilSpeed speed = Thermoregulation.FanCoilSpeed.valueOf(command.toString());
                    send(Thermoregulation.requestWriteFanCoilSpeed(w.value(), speed));
                } catch (OWNException e) {
                    logger.warn("handleSetFanSpeed() {}", e.getMessage());
                } catch (IllegalArgumentException e) {
                    logger.warn("handleSetFanSpeed() Unsupported command {} for thing {}", command,
                            getThing().getUID());
                    return;
                }
            }
        } else {
            logger.warn("handleSetFanSpeed() Unsupported command {} for thing {}", command, getThing().getUID());
        }
    }

    private void handleSetProgramNumber(Command command) {
        if (command instanceof DecimalType) {
            if (!isCentralUnit) {
                logger.warn("handleSetProgramNumber() This command can be sent only for a Central Unit.");
                return;
            }

            programNumber = command.toString();
            logger.debug("handleSetProgramNumber() Program number set to {}", programNumber);

            // force OperationMode update if we are already in SCENARIO o WEEKLY mode
            if (currentMode.isScenario() || currentMode.isWeekly()) {
                try {
                    Thermoregulation.OperationMode new_mode = Thermoregulation.OperationMode
                            .valueOf(currentMode.mode() + "_" + programNumber);
                    logger.debug("handleSetProgramNumber() new mode {}", new_mode);
                    send(Thermoregulation.requestWriteMode(getWhere(""), new_mode, currentFunction,
                            currentSetPointTemp));
                } catch (OWNException e) {
                    logger.warn("handleSetProgramNumber() {}", e.getMessage());
                } catch (IllegalArgumentException e) {
                    logger.warn("handleSetProgramNumber() Unsupported command {} for thing {}", command,
                            getThing().getUID());
                }
            }

        } else {
            logger.warn("handleSetProgramNumber() Unsupported command {} for thing {}", command, getThing().getUID());
        }
    }

    private void handleSetpoint(Command command) {
        if (command instanceof QuantityType || command instanceof DecimalType) {
            Where w = deviceWhere;
            if (w != null) {
                double newTemp = 0;
                if (command instanceof QuantityType) {
                    QuantityType<?> tempCelsius = ((QuantityType<?>) command).toUnit(SIUnits.CELSIUS);
                    if (tempCelsius != null) {
                        newTemp = tempCelsius.doubleValue();
                    }
                } else {
                    newTemp = ((DecimalType) command).doubleValue();
                }
                try {
                    send(Thermoregulation.requestWriteSetpointTemperature(getWhere(w.value()), newTemp,
                            currentFunction));
                } catch (MalformedFrameException | OWNException e) {
                    logger.warn("handleSetpoint() {}", e.getMessage());
                }
            }
        } else {
            logger.warn("handleSetpoint() Unsupported command {} for thing {}", command, getThing().getUID());
        }
    }

    private void handleMode(Command command) {
        if (command instanceof StringType) {
            Where w = deviceWhere;
            if (w != null) {
                try {
                    Thermoregulation.OperationMode new_mode = Thermoregulation.OperationMode.OFF;

                    if (isCentralUnit && WhatThermo.isComplex(command.toString())) {
                        new_mode = Thermoregulation.OperationMode.valueOf(command.toString() + "_" + programNumber);

                        // store current mode
                        currentMode = new_mode;
                    } else {
                        new_mode = Thermoregulation.OperationMode.valueOf(command.toString());
                    }
                    send(Thermoregulation.requestWriteMode(getWhere(w.value()), new_mode, currentFunction,
                            currentSetPointTemp));
                } catch (OWNException e) {
                    logger.warn("handleMode() {}", e.getMessage());
                } catch (IllegalArgumentException e) {
                    logger.warn("handleMode() Unsupported command {} for thing {}", command, getThing().getUID());
                    return;
                }
            }
        } else {
            logger.warn("handleMode() Unsupported command {} for thing {}", command, getThing().getUID());
        }
    }

    private String getWhere(String where) {
        if (isCentralUnit) {
            return "#0";
        } else {
            return isStandAlone ? where : "#" + where;
        }
    }

    private void handleFunction(Command command) {
        if (command instanceof StringType) {
            Where w = deviceWhere;
            if (w != null) {
                try {
                    Thermoregulation.Function function = Thermoregulation.Function.valueOf(command.toString());
                    send(Thermoregulation.requestWriteFunction(w.value(), function));
                } catch (OWNException e) {
                    logger.warn("handleFunction() {}", e.getMessage());
                } catch (IllegalArgumentException e) {
                    logger.warn("handleFunction() Unsupported command {} for thing {}", command, getThing().getUID());
                    return;
                }
            }
        } else {
            logger.warn("handleFunction() Unsupported command {} for thing {}", command, getThing().getUID());
        }
    }

    @Override
    protected void handleMessage(BaseOpenMessage msg) {
        super.handleMessage(msg);

        if (isCentralUnit) {
            if (msg.getWhat() == null) {
                return;
            }

            // there isn't a message used for setting OK for battery status so let's assume
            // it's OK and then change to KO if according message is received
            updateCUBatteryStatus(CU_BATTERY_OK);

            // same in case of Failure Discovered
            updateCUFailureDiscovered(OnOffType.OFF);

            if (msg.getWhat() == Thermoregulation.WhatThermo.REMOTE_CONTROL_DISABLED) {
                updateCURemoteControlStatus(CU_REMOTE_CONTROL_DISABLED);
            } else if (msg.getWhat() == Thermoregulation.WhatThermo.REMOTE_CONTROL_ENABLED) {
                updateCURemoteControlStatus(CU_REMOTE_CONTROL_ENABLED);
            } else if (msg.getWhat() == Thermoregulation.WhatThermo.BATTERY_KO) {
                updateCUBatteryStatus(CU_BATTERY_KO);
            } else if (msg.getWhat() == Thermoregulation.WhatThermo.AT_LEAST_ONE_PROBE_OFF) {
                updateCUAtLeastOneProbeOff(OnOffType.ON);
            } else if (msg.getWhat() == Thermoregulation.WhatThermo.AT_LEAST_ONE_PROBE_ANTIFREEZE) {
                updateCUAtLeastOneProbeProtection(OnOffType.ON);
            } else if (msg.getWhat() == Thermoregulation.WhatThermo.AT_LEAST_ONE_PROBE_MANUAL) {
                updateCUAtLeastOneProbeManual(OnOffType.ON);
            } else if (msg.getWhat() == Thermoregulation.WhatThermo.FAILURE_DISCOVERED) {
                updateCUFailureDiscovered(OnOffType.ON);
            } // must intercept all possibile WHATs
            else if (msg.getWhat() == Thermoregulation.WhatThermo.RELEASE_SENSOR_LOCAL_ADJUST) { // will be implemented
                                                                                                 // soon
                logger.debug("handleMessage() Ignoring unsupported WHAT {}. Frame={}", msg.getWhat(), msg);
            } else if (msg.getWhat().value() == UNDOCUMENTED_WHAT_4001) {
                logger.debug("handleMessage() Ignoring unsupported WHAT {}. Frame={}", msg.getWhat(), msg);
            } else if (msg.getWhat().value() == UNDOCUMENTED_WHAT_4002) {
                logger.debug("handleMessage() Ignoring unsupported WHAT {}. Frame={}", msg.getWhat(), msg);
            } else {
                // check and update values of other channel (mode, function, temp)
                updateModeAndFunction((Thermoregulation) msg);
                updateSetpoint((Thermoregulation) msg);
            }
            return;
        }

        if (msg.isCommand()) {
            updateModeAndFunction((Thermoregulation) msg);
        } else {
            if (msg.getDim() == null) {
                return;
            }
            if (msg.getDim() == Thermoregulation.DimThermo.TEMPERATURE
                    || msg.getDim() == Thermoregulation.DimThermo.PROBE_TEMPERATURE) {
                updateTemperature((Thermoregulation) msg);
            } else if (msg.getDim() == Thermoregulation.DimThermo.TEMP_SETPOINT
                    || msg.getDim() == Thermoregulation.DimThermo.COMPLETE_PROBE_STATUS) {
                updateSetpoint((Thermoregulation) msg);
            } else if (msg.getDim() == Thermoregulation.DimThermo.VALVES_STATUS) {
                updateValveStatus((Thermoregulation) msg);
            } else if (msg.getDim() == Thermoregulation.DimThermo.ACTUATOR_STATUS) {
                updateActuatorStatus((Thermoregulation) msg);
            } else if (msg.getDim() == Thermoregulation.DimThermo.FAN_COIL_SPEED) {
                updateFanCoilSpeed((Thermoregulation) msg);
            } else if (msg.getDim() == Thermoregulation.DimThermo.OFFSET) {
                updateLocalOffset((Thermoregulation) msg);
            } else {
                logger.debug("handleMessage() Ignoring unsupported DIM {} for thing {}. Frame={}", msg.getDim(),
                        getThing().getUID(), msg);
            }
        }
    }

    private void updateModeAndFunction(Thermoregulation tmsg) {
        if (tmsg.getWhat() == null) {
            logger.debug("updateModeAndFunction() Could not parse Mode or Function from {} (what is null)",
                    tmsg.getFrameValue());
            return;
        }
        Thermoregulation.WhatThermo w = Thermoregulation.WhatThermo.fromValue(tmsg.getWhat().value());

        if (w.getMode() == null) {
            logger.debug("updateModeAndFunction() Could not parse Mode from: {}", tmsg.getFrameValue());
            return;
        }

        if (w.getFunction() == null) {
            logger.debug("updateModeAndFunction() Could not parse Function from: {}", tmsg.getFrameValue());
            return;
        }

        Thermoregulation.OperationMode operationMode = w.getMode();
        Thermoregulation.Function function = w.getFunction();

        // keep track of thermostats (zones) status
        if (!isCentralUnit && (!((WhereThermo) deviceWhere).isProbe())) {
            if (operationMode == Thermoregulation.OperationMode.OFF) {
                probesInManual.remove(tmsg.getWhere().value());
                probesInProtection.remove(tmsg.getWhere().value());
                if (probesInOFF.add(tmsg.getWhere().value())) {
                    logger.debug("atLeastOneProbeInOFF: added WHERE ---> {}", tmsg.getWhere());
                }
            } else if (operationMode == Thermoregulation.OperationMode.PROTECTION) {
                probesInManual.remove(tmsg.getWhere().value());
                probesInOFF.remove(tmsg.getWhere().value());
                if (probesInProtection.add(tmsg.getWhere().value())) {
                    logger.debug("atLeastOneProbeInProtection: added WHERE ---> {}", tmsg.getWhere());
                }
            } else if (operationMode == Thermoregulation.OperationMode.MANUAL) {
                probesInProtection.remove(tmsg.getWhere().value());
                probesInOFF.remove(tmsg.getWhere().value());
                if (probesInManual.add(tmsg.getWhere().value())) {
                    logger.debug("atLeastOneProbeInManual: added WHERE ---> {}", tmsg.getWhere());
                }
            }

            if (probesInOFF.isEmpty()) {
                updateCUAtLeastOneProbeOff(OnOffType.OFF);
            }
            if (probesInProtection.isEmpty()) {
                updateCUAtLeastOneProbeProtection(OnOffType.OFF);
            }
            if (probesInManual.isEmpty()) {
                updateCUAtLeastOneProbeManual(OnOffType.OFF);
            }
        }

        updateState(CHANNEL_FUNCTION, new StringType(function.toString()));

        // must convert from OperationMode to Mode and set ProgramNumber when necessary
        updateState(CHANNEL_MODE, new StringType(operationMode.mode()));
        if (operationMode.isScenario()) {
            logger.debug("updateModeAndFunction() set SCENARIO program to: {}", operationMode.programNumber());
            updateState(CHANNEL_CU_SCENARIO_PROGRAM_NUMBER, new DecimalType(operationMode.programNumber()));
        }
        if (operationMode.isWeekly()) {
            logger.debug("updateModeAndFunction() set WEEKLY program to: {}", operationMode.programNumber());
            updateState(CHANNEL_CU_WEEKLY_PROGRAM_NUMBER, new DecimalType(operationMode.programNumber()));
        }

        // store current function
        currentFunction = function;

        // in case of central unit store also current operation mode
        if (isCentralUnit) {
            currentMode = operationMode;
        }
    }

    private void updateTemperature(Thermoregulation tmsg) {
        try {
            double temp = Thermoregulation.parseTemperature(tmsg);
            updateState(CHANNEL_TEMPERATURE, getAsQuantityTypeOrNull(temp, SIUnits.CELSIUS));
        } catch (FrameException e) {
            logger.warn("updateTemperature() FrameException on frame {}: {}", tmsg, e.getMessage());
            updateState(CHANNEL_TEMPERATURE, UnDefType.UNDEF);
        }
    }

    private void updateSetpoint(Thermoregulation tmsg) {
        try {
            double temp = 11.5d;
            if (isCentralUnit) {
                if (tmsg.getWhat() == null) {
                    // it should be like *4*WHAT#TTTT*#0##
                    logger.debug("updateSetpoint() Could not parse function from {} (what is null)",
                            tmsg.getFrameValue());
                    return;
                }

                String[] parameters = tmsg.getWhatParams();
                if (parameters.length > 0) {
                    temp = Thermoregulation.decodeTemperature(parameters[0]);
                    logger.debug("updateSetpoint() parsed temperature from {}: {} ---> {}", tmsg.toStringVerbose(),
                            parameters[0], temp);
                }
            } else {
                temp = Thermoregulation.parseTemperature(tmsg);
            }
            updateState(CHANNEL_TEMP_SETPOINT, getAsQuantityTypeOrNull(temp, SIUnits.CELSIUS));
            currentSetPointTemp = temp;
        } catch (NumberFormatException e) {
            logger.warn("updateSetpoint() NumberFormatException on frame {}: {}", tmsg, e.getMessage());
            updateState(CHANNEL_TEMP_SETPOINT, UnDefType.UNDEF);
        } catch (FrameException e) {
            logger.warn("updateSetpoint() FrameException on frame {}: {}", tmsg, e.getMessage());
            updateState(CHANNEL_TEMP_SETPOINT, UnDefType.UNDEF);
        }
    }

    private void updateFanCoilSpeed(Thermoregulation tmsg) {
        try {
            Thermoregulation.FanCoilSpeed speed = Thermoregulation.parseFanCoilSpeed(tmsg);
            updateState(CHANNEL_FAN_SPEED, new StringType(speed.toString()));
        } catch (FrameException e) {
            logger.warn("updateFanCoilSpeed() FrameException on frame {}: {}", tmsg, e.getMessage());
            updateState(CHANNEL_FAN_SPEED, UnDefType.UNDEF);
        }
    }

    private void updateValveStatus(Thermoregulation tmsg) {
        try {
            Thermoregulation.ValveOrActuatorStatus cv = Thermoregulation.parseValveStatus(tmsg,
                    Thermoregulation.WhatThermo.CONDITIONING);
            updateState(CHANNEL_CONDITIONING_VALVES, new StringType(cv.toString()));

            Thermoregulation.ValveOrActuatorStatus hv = Thermoregulation.parseValveStatus(tmsg,
                    Thermoregulation.WhatThermo.HEATING);
            updateState(CHANNEL_HEATING_VALVES, new StringType(hv.toString()));
        } catch (FrameException e) {
            logger.warn("updateValveStatus() FrameException on frame {}: {}", tmsg, e.getMessage());
            updateState(CHANNEL_CONDITIONING_VALVES, UnDefType.UNDEF);
            updateState(CHANNEL_HEATING_VALVES, UnDefType.UNDEF);
        }
    }

    private void updateActuatorStatus(Thermoregulation tmsg) {
        try {
            Thermoregulation.ValveOrActuatorStatus hv = Thermoregulation.parseActuatorStatus(tmsg);
            updateState(CHANNEL_ACTUATORS, new StringType(hv.toString()));
        } catch (FrameException e) {
            logger.warn("updateActuatorStatus() FrameException on frame {}: {}", tmsg, e.getMessage());
            updateState(CHANNEL_ACTUATORS, UnDefType.UNDEF);
        }
    }

    private void updateLocalOffset(Thermoregulation tmsg) {
        try {
            Thermoregulation.LocalOffset offset = Thermoregulation.parseLocalOffset(tmsg);
            updateState(CHANNEL_LOCAL_OFFSET, new StringType(offset.toString()));
            logger.debug("updateLocalOffset() {}: {}", tmsg, offset.toString());

        } catch (FrameException e) {
            logger.warn("updateLocalOffset() FrameException on frame {}: {}", tmsg, e.getMessage());
            updateState(CHANNEL_LOCAL_OFFSET, UnDefType.UNDEF);
        }
    }

    private void updateCURemoteControlStatus(String status) {
        updateState(CHANNEL_CU_REMOTE_CONTROL, new StringType(status));
        logger.debug("updateCURemoteControlStatus(): {}", status);
    }

    private void updateCUBatteryStatus(String status) {
        updateState(CHANNEL_CU_BATTERY_STATUS, new StringType(status));

        if (status == CU_BATTERY_KO) { // do not log default value (which is automatically setted)
            logger.debug("updateCUBatteryStatus(): {}", status);
        }
    }

    private void updateCUFailureDiscovered(OnOffType status) {
        updateState(CHANNEL_CU_FAILURE_DISCOVERED, status);

        if (status == OnOffType.ON) { // do not log default value (which is automatically setted)
            logger.debug("updateCUFailureDiscovered(): {}", status);
        }
    }

    private void updateCUAtLeastOneProbeOff(OnOffType status) {
        updateState(CHANNEL_CU_AT_LEAST_ONE_PROBE_OFF, status);
        logger.debug("updateCUAtLeastOneProbeOff(): {}", status);
    }

    private void updateCUAtLeastOneProbeProtection(OnOffType status) {
        updateState(CHANNEL_CU_AT_LEAST_ONE_PROBE_PROTECTION, status);
        logger.debug("updateCUAtLeastOneProbeProtection(): {}", status);
    }

    private void updateCUAtLeastOneProbeManual(OnOffType status) {
        updateState(CHANNEL_CU_AT_LEAST_ONE_PROBE_MANUAL, status);
        logger.debug("updateCUAtLeastOneProbeManual(): {}", status);
    }

    private Boolean channelExists(String channelID) {
        return thing.getChannel(channelID) != null;
    }

    @Override
    protected void refreshDevice(boolean refreshAll) {
        logger.debug("--- refreshDevice() : refreshing SINGLE... ({})", thing.getUID());
        if (isCentralUnit) {
            // TODO: 4 zone central -> zone #0 CAN be also a zone with its temp.. with
            // 99-zones central no! let's assume it's a 99 zone
            try {
                send(Thermoregulation.requestStatus("#0"));
            } catch (OWNException e) {
                logger.warn("refreshDevice() central unit returned OWNException {}", e.getMessage());
            }

            return;
        }

        if (deviceWhere != null) {

            String w = deviceWhere.value();
            try {
                send(Thermoregulation.requestTemperature(w));

                if (!((WhereThermo) deviceWhere).isProbe()) {
                    // for bus_thermo_zone request also other single channels updates
                    send(Thermoregulation.requestSetPointTemperature(w));
                    send(Thermoregulation.requestMode(w));

                    // refresh ONLY subscribed channels
                    if (channelExists(CHANNEL_FAN_SPEED)) {
                        send(Thermoregulation.requestFanCoilSpeed(w));
                    }

                    if (channelExists(CHANNEL_CONDITIONING_VALVES) || channelExists(CHANNEL_HEATING_VALVES)) {
                        send(Thermoregulation.requestValvesStatus(w));
                    }

                    if (channelExists(CHANNEL_ACTUATORS)) {
                        send(Thermoregulation.requestActuatorsStatus(w));
                    }

                    if (channelExists(CHANNEL_LOCAL_OFFSET)) {
                        send(Thermoregulation.requestLocalOffset(w));
                    }
                }
            } catch (OWNException e) {
                logger.warn("refreshDevice() where='{}' returned OWNException {}", w, e.getMessage());
            }
        }
    }
}
