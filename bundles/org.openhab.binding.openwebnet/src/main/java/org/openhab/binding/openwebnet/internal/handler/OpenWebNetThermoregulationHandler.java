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
package org.openhab.binding.openwebnet.internal.handler;

import static org.openhab.binding.openwebnet.internal.OpenWebNetBindingConstants.*;

import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
import org.openwebnet4j.communication.Response;
import org.openwebnet4j.message.BaseOpenMessage;
import org.openwebnet4j.message.FrameException;
import org.openwebnet4j.message.MalformedFrameException;
import org.openwebnet4j.message.Thermoregulation;
import org.openwebnet4j.message.Thermoregulation.DimThermo;
import org.openwebnet4j.message.Thermoregulation.Function;
import org.openwebnet4j.message.Thermoregulation.OperationMode;
import org.openwebnet4j.message.Thermoregulation.WhatThermo;
import org.openwebnet4j.message.Thermoregulation.WhatThermoType;
import org.openwebnet4j.message.Where;
import org.openwebnet4j.message.WhereThermo;
import org.openwebnet4j.message.Who;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OpenWebNetThermoregulationHandler} is responsible for handling
 * commands/messages for Thermoregulation Things. It extends the abstract
 * {@link OpenWebNetThingHandler}.
 *
 * @author Massimo Valla - Initial contribution. Added support for 4-zones CU.
 *         Rafactoring and fixed CU state channels updates. Completed support
 *         for HOLIDAY/VACATION modes and refactored mode handling.
 * @author Gilberto Cocchi - Initial contribution.
 * @author Andrea Conte - Added support for 99-zone CU and CU state channels.
 */
@NonNullByDefault
public class OpenWebNetThermoregulationHandler extends OpenWebNetThingHandler {

    private final Logger logger = LoggerFactory.getLogger(OpenWebNetThermoregulationHandler.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = OpenWebNetBindingConstants.THERMOREGULATION_SUPPORTED_THING_TYPES;

    private double currentSetPointTemp = 20.0d;

    private Thermoregulation.@Nullable Function currentFunction = null;
    private Thermoregulation.@Nullable OperationMode currentMode = null;
    private int currentWeeklyPrgNum = 1;
    private int currentScenarioPrgNum = 1;
    private int currentVacationDays = 1;

    private boolean isStandAlone = true; // true if zone is not associated to a CU
    private boolean isCentralUnit = false;

    private boolean cuAtLeastOneProbeOff = false;
    private boolean cuAtLeastOneProbeProtection = false;
    private boolean cuAtLeastOneProbeManual = false;
    private String cuBatteryStatus = CU_BATTERY_OK;
    private boolean cuFailureDiscovered = false;

    private @Nullable ScheduledFuture<?> cuStateChannelsUpdateSchedule;

    public static final int CU_STATE_CHANNELS_UPDATE_DELAY = 1500; // msec

    private static final String CU_REMOTE_CONTROL_ENABLED = "ENABLED";
    private static final String CU_REMOTE_CONTROL_DISABLED = "DISABLED";
    private static final String CU_BATTERY_OK = "OK";
    private static final String CU_BATTERY_KO = "KO";
    private static final String MODE_WEEKLY = "WEEKLY";

    public OpenWebNetThermoregulationHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();
        ThingTypeUID thingType = thing.getThingTypeUID();
        isCentralUnit = OpenWebNetBindingConstants.THING_TYPE_BUS_THERMO_CU.equals(thingType);
        if (!isCentralUnit) {
            if (!((WhereThermo) deviceWhere).isProbe()) {
                Object standAloneConfig = getConfig().get(OpenWebNetBindingConstants.CONFIG_PROPERTY_STANDALONE);
                if (standAloneConfig != null) {
                    isStandAlone = Boolean.parseBoolean(standAloneConfig.toString());
                }
                logger.debug("@@@@  THERMO ZONE INITIALIZE isStandAlone={}", isStandAlone);
            }
        } else {
            // central unit must have WHERE=#0 or WHERE=0 or WHERE=#0#n
            String w = deviceWhere.value();
            if (w == null || !("0".equals(w) || "#0".equals(w) || w.startsWith("#0#"))) {
                logger.warn("initialize() Invalid WHERE={} for Central Unit.", deviceWhere.value());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/offline.conf-error-where");
                return;
            }
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
                handleSetProgramNumber(channel, command);
                break;
            case CHANNEL_CU_VACATION_DAYS:
                handleVacationDays(channel, command);
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
    protected Who getManagedWho() {
        return Who.THERMOREGULATION;
    }

    private void handleSetFanSpeed(Command command) {
        if (command instanceof StringType) {
            Where w = deviceWhere;
            if (w == null) {
                logger.warn("handleSetFanSpeed() Where is null for thing {}", getThing().getUID());
                return;
            }
            try {
                Thermoregulation.FanCoilSpeed speed = Thermoregulation.FanCoilSpeed.valueOf(command.toString());
                send(Thermoregulation.requestWriteFanCoilSpeed(w.value(), speed));
            } catch (OWNException e) {
                logger.warn("handleSetFanSpeed() {}", e.getMessage());
            } catch (IllegalArgumentException e) {
                logger.warn("handleSetFanSpeed() Unsupported command {} for thing {}", command, getThing().getUID());
                return;
            }
        } else {
            logger.warn("handleSetFanSpeed() Unsupported command {} for thing {}", command, getThing().getUID());
        }
    }

    private void handleSetProgramNumber(ChannelUID channel, Command command) {
        if (command instanceof DecimalType) {
            if (!isCentralUnit) {
                logger.warn("handleSetProgramNumber() This command can be sent only for a Central Unit.");
                return;
            }
            Where w = deviceWhere;
            if (w == null) {
                logger.warn("handleSetProgramNumber() Where is null for thing {}", getThing().getUID());
                return;
            }

            int programNumber = ((DecimalType) command).intValue();
            boolean updateOpMode = false;

            if (CHANNEL_CU_WEEKLY_PROGRAM_NUMBER.equals(channel.getId())) {
                if (programNumber < 1 || programNumber > 3) {
                    logger.warn("handleSetProgramNumber() Invalid program number {} for thing {}", command,
                            getThing().getUID());
                    return;
                }
                updateOpMode = (currentMode == Thermoregulation.OperationMode.WEEKLY);
                currentWeeklyPrgNum = programNumber;
                logger.debug("handleSetProgramNumber() currentWeeklyPrgNum changed to: {}", programNumber);
            } else {
                if (programNumber < 1 || programNumber > 16) {
                    logger.warn("handleSetProgramNumber() Invalid program number {} for thing {}", command,
                            getThing().getUID());
                    return;
                }
                updateOpMode = (currentMode == Thermoregulation.OperationMode.SCENARIO);
                currentScenarioPrgNum = programNumber;
                logger.debug("handleSetProgramNumber() currentScenarioPrgNum changed to: {}", programNumber);
            }

            // force OperationMode update if we are already in SCENARIO or WEEKLY mode
            if (updateOpMode) {
                try {
                    send(Thermoregulation.requestWriteWeeklyScenarioMode(getWhere(w.value()), currentMode,
                            currentFunction, programNumber));
                } catch (OWNException e) {
                    logger.warn("handleSetProgramNumber() {}", e.getMessage());
                }
            } else { // just update channel
                updateState(channel, new DecimalType(programNumber));
            }
        } else {
            logger.warn("handleSetProgramNumber() Unsupported command {} for thing {}", command, getThing().getUID());
        }
    }

    private void handleVacationDays(ChannelUID channel, Command command) {
        if (command instanceof DecimalType) {
            Where w = deviceWhere;
            if (w == null) {
                logger.warn("handleVacationDays() Where is null for thing {}", getThing().getUID());
                return;
            }
            if (!isCentralUnit) {
                logger.warn("handleVacationDays() This command can be sent only for a Central Unit.");
                return;
            }
            int vacationDays = ((DecimalType) command).intValue();

            if (vacationDays < 1 || vacationDays > 255) {
                logger.warn("handleVacationDays() vacation days must be between 1 and 255");
                return;
            }

            currentVacationDays = vacationDays;
            logger.debug("handleVacationDays() currentVacationDays changed to: {}", currentVacationDays);

            // force OperationMode update if we are already in VACATION mode
            if (currentMode == Thermoregulation.OperationMode.VACATION) {
                try {
                    send(Thermoregulation.requestWriteVacationMode(getWhere(w.value()), currentFunction,
                            currentVacationDays, currentWeeklyPrgNum));
                } catch (OWNException e) {
                    logger.warn("handleVacationDays() {}", e.getMessage());
                } catch (IllegalArgumentException e) {
                    logger.warn("handleVacationDays() Unsupported command {} for thing {}", command,
                            getThing().getUID());
                }
            } else { // just update channel
                updateState(channel, new DecimalType(currentVacationDays));
            }
        } else {
            logger.warn("handleVacationDays() Unsupported command {} for thing {}", command, getThing().getUID());
        }
    }

    private void handleSetpoint(Command command) {
        if (command instanceof QuantityType || command instanceof DecimalType) {
            Where w = deviceWhere;
            if (w == null) {
                logger.warn("handleSetpoint() Where is null for thing {}", getThing().getUID());
                return;
            }
            double newTemp = 0;
            if (command instanceof QuantityType) {
                QuantityType<?> tempCelsius = ((QuantityType<?>) command).toUnit(SIUnits.CELSIUS);
                if (tempCelsius != null) {
                    newTemp = tempCelsius.doubleValue();
                }
            } else {
                newTemp = ((DecimalType) command).doubleValue();
            }
            if (newTemp >= 5.0 && newTemp <= 40.0) {
                try {
                    Response res;
                    res = send(Thermoregulation.requestWriteSetpointTemperature(getWhere(w.value()), newTemp,
                            currentFunction));
                    // For zones no setPoint temperature confirmation message is returned from gw,
                    // so we update channel with current requested temp on successful response.
                    if (!isCentralUnit && res != null && res.isSuccess()) {
                        updateState(CHANNEL_TEMP_SETPOINT, getAsQuantityTypeOrNull(newTemp, SIUnits.CELSIUS));
                        currentSetPointTemp = newTemp;
                    }
                } catch (MalformedFrameException | OWNException e) {
                    logger.warn("handleSetpoint() {}", e.getMessage());
                }
            } else {
                logger.info("handleSetpoint() Setpoint temperature must be between 5°C and 40°C for thing {}",
                        getThing().getUID());
            }
        } else {
            logger.warn("handleSetpoint() Unsupported command {} for thing {}", command, getThing().getUID());
        }
    }

    private void handleMode(Command command) {
        if (command instanceof StringType) {
            Where w = deviceWhere;
            if (w == null) {
                logger.warn("handleMode() Where is null for thing {}", getThing().getUID());
                return;
            }

            String whStr = getWhere(w.value());
            String cmdStr = command.toString().toUpperCase();
            OperationMode newMode;
            try {
                newMode = OperationMode.valueOf(cmdStr);
            } catch (IllegalArgumentException e) {
                logger.warn("handleMode() Unsupported command {} for thing {}", command, getThing().getUID());
                return;
            }
            Thermoregulation tMsg = null;
            if (isCentralUnit) { // CU accepted modes: MANUAL, OFF, PROTECTION, WEEKLY, SCENARIO, HOLIDAY,
                                 // VACATION
                switch (newMode) {
                    case MANUAL:
                    case OFF:
                    case PROTECTION:
                        tMsg = Thermoregulation.requestWriteMode(whStr, newMode, currentFunction, currentSetPointTemp);
                        break;
                    case WEEKLY:
                    case SCENARIO:
                        int programNumber = (MODE_WEEKLY.equals(cmdStr) ? currentWeeklyPrgNum : currentScenarioPrgNum);
                        tMsg = Thermoregulation.requestWriteWeeklyScenarioMode(whStr, newMode, currentFunction,
                                programNumber);
                        break;
                    case HOLIDAY:
                        tMsg = Thermoregulation.requestWriteHolidayMode(whStr, currentFunction, currentWeeklyPrgNum);
                        break;
                    case VACATION:
                        tMsg = Thermoregulation.requestWriteVacationMode(whStr, currentFunction, currentVacationDays,
                                currentWeeklyPrgNum);
                        break;
                    default:
                        logger.warn("handleMode() Unsupported command {} for CU thing {}", command,
                                getThing().getUID());
                }
            } else {// Zone accepted modes: MANUAL, OFF, PROTECTION, AUTO
                switch (newMode) {
                    case MANUAL:
                        tMsg = Thermoregulation.requestWriteMode(whStr, newMode, currentFunction, currentSetPointTemp);
                        break;
                    case OFF:
                    case PROTECTION:
                    case AUTO:
                        tMsg = Thermoregulation.requestWriteMode(whStr, newMode, Function.GENERIC, currentSetPointTemp);
                        break;
                    default:
                        logger.warn("handleMode() Unsupported command {} for ZONE thing {}", command,
                                getThing().getUID());
                }
            }

            if (tMsg != null) {
                try {
                    send(tMsg);
                } catch (OWNException e) {
                    logger.warn("handleMode() {}", e.getMessage());
                }
            } else {
                logger.warn("handleMode() Unsupported command {} for thing {}", command, getThing().getUID());
            }
        }
    }

    private String getWhere(String where) {
        if (isCentralUnit) {
            if (where.charAt(0) == '#') {
                return where;
            } else { // to support old configurations for CU with where="0"
                return "#" + where;
            }
        } else {
            return isStandAlone ? where : "#" + where;
        }
    }

    private void handleFunction(Command command) {
        if (command instanceof StringType) {
            Where w = deviceWhere;
            if (w == null) {
                logger.warn("handleFunction() Where is null for thing {}", getThing().getUID());
                return;
            }
            try {
                Thermoregulation.Function function = Thermoregulation.Function.valueOf(command.toString());
                send(Thermoregulation.requestWriteFunction(w.value(), function));
            } catch (OWNException e) {
                logger.warn("handleFunction() {}", e.getMessage());
            } catch (IllegalArgumentException e) {
                logger.warn("handleFunction() Unsupported command {} for thing {}", command, getThing().getUID());
                return;
            }
        } else {
            logger.warn("handleFunction() Unsupported command {} for thing {}", command, getThing().getUID());
        }
    }

    @Override
    protected void handleMessage(BaseOpenMessage msg) {
        super.handleMessage(msg);
        logger.debug("@@@@ Thermo.handleMessage(): {}", msg.toStringVerbose());
        Thermoregulation tmsg = (Thermoregulation) msg;
        if (isCentralUnit) {
            WhatThermo tWhat = (WhatThermo) msg.getWhat();
            if (tWhat == null) {
                logger.debug("handleMessage() Ignoring unsupported WHAT {}. Frame={}", tWhat, msg);
                return;
            }
            if (tWhat.value() > 40) {
                // it's a CU mode event, CU state events will follow shortly, so let's reset
                // their values
                resetCUState();
            }
            switch (tWhat.getType()) {
                case AT_LEAST_ONE_PROBE_ANTIFREEZE:
                    cuAtLeastOneProbeProtection = true;
                    break;
                case AT_LEAST_ONE_PROBE_MANUAL:
                    cuAtLeastOneProbeManual = true;
                    break;
                case AT_LEAST_ONE_PROBE_OFF:
                    cuAtLeastOneProbeOff = true;
                    break;
                case BATTERY_KO:
                    cuBatteryStatus = CU_BATTERY_KO;
                    break;
                case FAILURE_DISCOVERED:
                    cuFailureDiscovered = true;
                    break;
                case RELEASE_SENSOR_LOCAL_ADJUST:
                    logger.debug("handleMessage(): Ignoring unsupported WHAT {}. Frame={}", tWhat, msg);
                    break;
                case REMOTE_CONTROL_DISABLED:
                    updateCURemoteControlStatus(CU_REMOTE_CONTROL_DISABLED);
                    break;
                case REMOTE_CONTROL_ENABLED:
                    updateCURemoteControlStatus(CU_REMOTE_CONTROL_ENABLED);
                    break;
                default:
                    // check and update values of other channels (mode, function, temp)
                    updateModeAndFunction(tmsg);
                    updateSetpoint(tmsg);
                    break;
            }
            return;
        }

        if (tmsg.isCommand()) {
            updateModeAndFunction(tmsg);
        } else {
            DimThermo dim = (DimThermo) tmsg.getDim();
            switch (dim) {
                case TEMP_SETPOINT:
                    updateSetpoint(tmsg);
                    break;
                case COMPLETE_PROBE_STATUS:
                    updateTargetTemperature(tmsg);
                    break;
                case PROBE_TEMPERATURE:
                case TEMPERATURE:
                    updateTemperature(tmsg);
                    break;
                case ACTUATOR_STATUS:
                    updateActuatorStatus(tmsg);
                    break;
                case FAN_COIL_SPEED:
                    updateFanCoilSpeed(tmsg);
                    break;
                case OFFSET:
                    updateLocalOffset(tmsg);
                    break;
                case VALVES_STATUS:
                    updateValveStatus(tmsg);
                    break;
                default:
                    logger.debug("handleMessage() Ignoring unsupported DIM {} for thing {}. Frame={}", tmsg.getDim(),
                            getThing().getUID(), tmsg);
                    break;
            }
        }
    }

    private void updateModeAndFunction(Thermoregulation tmsg) {
        if (tmsg.getWhat() == null) {
            logger.warn("updateModeAndFunction() Could not parse What {} (WHAT is null)", tmsg.getFrameValue());
            return;
        }
        Thermoregulation.WhatThermo what = tmsg.new WhatThermo(tmsg.getWhat().value());

        if (what.getFunction() == null) {
            logger.warn("updateModeAndFunction() Could not parse Function from: {}", tmsg.getFrameValue());
            return;
        }
        // update Function if it's not GENERIC
        Thermoregulation.Function function = what.getFunction();
        if (function != Function.GENERIC) {
            if (currentFunction != function) {
                updateState(CHANNEL_FUNCTION, new StringType(function.toString()));
                currentFunction = function;
            }
        }
        if (what.getType() == WhatThermoType.HEATING || what.getType() == WhatThermoType.CONDITIONING
                || what.getType() == WhatThermoType.GENERIC) {
            // *4*1*z## and *4*0*z## do not tell us which mode is the zone now, so let's
            // skip
            return;
        }

        // update Mode
        Thermoregulation.OperationMode operationMode = null;

        operationMode = what.getMode();

        if (operationMode != null) {
            // set ProgramNumber/vacationDays channels when necessary
            switch (operationMode) {
                case VACATION:
                    updateVacationDays(tmsg);
                    break;
                case WEEKLY:
                case SCENARIO:
                    updateProgramNumber(tmsg);
                    break;
                default:
                    break;
            }
            if (!isCentralUnit && !(operationMode == OperationMode.AUTO || operationMode == OperationMode.OFF
                    || operationMode == OperationMode.PROTECTION || operationMode == OperationMode.MANUAL)) {
                logger.warn("updateModeAndFunction() Unsupported mode for zone {} from message: {}",
                        getThing().getUID(), tmsg.getFrameValue());
                return;
            } else {
                if (currentMode != operationMode) {
                    updateState(CHANNEL_MODE, new StringType(operationMode.name()));
                    currentMode = operationMode;
                }
            }
        } else {
            logger.warn("updateModeAndFunction() Unrecognized mode from message: {}", tmsg.getFrameValue());
        }
    }

    private void updateVacationDays(Thermoregulation tmsg) {
        @Nullable
        Integer vDays = ((WhatThermo) tmsg.getWhat()).vacationDays();
        if (vDays != null) {
            logger.debug("{} - updateVacationDays() set VACATION DAYS to: {}", getThing().getUID(), vDays);
            updateState(CHANNEL_CU_VACATION_DAYS, new DecimalType(vDays));
            currentVacationDays = vDays;
        }
    }

    private void updateProgramNumber(Thermoregulation tmsg) {
        @Nullable
        WhatThermo wt = (WhatThermo) tmsg.getWhat();
        if (wt == null) {
            return;
        }
        Integer prNum = wt.programNumber();
        if (prNum != null) {
            if (wt.getMode() == OperationMode.SCENARIO) {
                logger.debug("{} - updateProgramNumber() set SCENARIO program to: {}", getThing().getUID(), prNum);
                updateState(CHANNEL_CU_SCENARIO_PROGRAM_NUMBER, new DecimalType(prNum));
                currentScenarioPrgNum = prNum;
            } else if (wt.getMode() == OperationMode.WEEKLY) {
                logger.debug("{} - updateProgramNumber() set WEEKLY program to: {}", getThing().getUID(), prNum);
                updateState(CHANNEL_CU_WEEKLY_PROGRAM_NUMBER, new DecimalType(prNum));
                currentWeeklyPrgNum = prNum;
            }
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

    private void updateTargetTemperature(Thermoregulation tmsg) {
        try {
            double temp = Thermoregulation.parseTemperature(tmsg);
            updateState(CHANNEL_TEMP_TARGET, getAsQuantityTypeOrNull(temp, SIUnits.CELSIUS));
        } catch (FrameException e) {
            logger.warn("updateTargetTemperature() FrameException on frame {}: {}", tmsg, e.getMessage());
            updateState(CHANNEL_TEMP_TARGET, UnDefType.UNDEF);
        }
    }

    private void updateSetpoint(Thermoregulation tmsg) {
        try {
            double newTemp = -1;
            if (isCentralUnit) {
                WhatThermo tw = (WhatThermo) tmsg.getWhat();
                if (tw == null) {
                    logger.warn("updateSetpoint() Could not parse what from {} (what is null)", tmsg.getFrameValue());
                    return;
                }
                String[] parameters = tmsg.getWhatParams();
                if (parameters.length > 0 && tw.getType() == WhatThermoType.MANUAL) {
                    // manual setpoint frame should be like *4*110#TTTT*#0##
                    newTemp = Thermoregulation.decodeTemperature(parameters[0]);
                    logger.debug("updateSetpoint() parsed temperature from {}: {} ---> {}", tmsg.toStringVerbose(),
                            parameters[0], newTemp);
                }
            } else {
                newTemp = Thermoregulation.parseTemperature(tmsg);
            }
            if (newTemp > 0) {
                updateState(CHANNEL_TEMP_SETPOINT, getAsQuantityTypeOrNull(newTemp, SIUnits.CELSIUS));
                currentSetPointTemp = newTemp;
            }
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
        } catch (NumberFormatException e) {
            logger.warn("updateFanCoilSpeed() NumberFormatException on frame {}: {}", tmsg, e.getMessage());
            updateState(CHANNEL_FAN_SPEED, UnDefType.UNDEF);
        } catch (FrameException e) {
            logger.warn("updateFanCoilSpeed() FrameException on frame {}: {}", tmsg, e.getMessage());
            updateState(CHANNEL_FAN_SPEED, UnDefType.UNDEF);
        }
    }

    private void updateValveStatus(Thermoregulation tmsg) {
        try {
            Thermoregulation.ValveOrActuatorStatus cv = Thermoregulation.parseValveStatus(tmsg,
                    Thermoregulation.WhatThermoType.CONDITIONING);
            updateState(CHANNEL_CONDITIONING_VALVES, new StringType(cv.toString()));
            updateHeatingCooling(false, cv);

            Thermoregulation.ValveOrActuatorStatus hv = Thermoregulation.parseValveStatus(tmsg,
                    Thermoregulation.WhatThermoType.HEATING);
            updateState(CHANNEL_HEATING_VALVES, new StringType(hv.toString()));
            updateHeatingCooling(true, hv);

        } catch (FrameException e) {
            logger.warn("updateValveStatus() FrameException on frame {}: {}", tmsg, e.getMessage());
            updateState(CHANNEL_CONDITIONING_VALVES, UnDefType.UNDEF);
            updateState(CHANNEL_HEATING_VALVES, UnDefType.UNDEF);
        }
    }

    private void updateHeatingCooling(boolean heating, Thermoregulation.ValveOrActuatorStatus v) {
        boolean state = false;
        switch (v) {
            case STOP:
            case CLOSED:
            case OFF:
            case OFF_FAN_COIL:
            case OFF_SPEED_1:
            case OFF_SPEED_2:
            case OFF_SPEED_3:
                state = false;
                break;
            case ON:
            case ON_FAN_COIL:
            case ON_SPEED_1:
            case ON_SPEED_2:
            case ON_SPEED_3:
            case OPENED:
                state = true;
                break;
        }

        if (heating) {
            updateState(CHANNEL_HEATING, OnOffType.from(state));
        } else {
            updateState(CHANNEL_COOLING, OnOffType.from(state));
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

    private void resetCUState() {
        logger.debug("########### resetting CU state");
        cuAtLeastOneProbeOff = false;
        cuAtLeastOneProbeProtection = false;
        cuAtLeastOneProbeManual = false;
        cuBatteryStatus = CU_BATTERY_OK;
        cuFailureDiscovered = false;

        cuStateChannelsUpdateSchedule = scheduler.schedule(() -> {
            updateCUStateChannels();
        }, CU_STATE_CHANNELS_UPDATE_DELAY, TimeUnit.MILLISECONDS);
    }

    private void updateCUStateChannels() {
        updateState(CHANNEL_CU_AT_LEAST_ONE_PROBE_OFF, OnOffType.from(cuAtLeastOneProbeOff));
        updateState(CHANNEL_CU_AT_LEAST_ONE_PROBE_PROTECTION, OnOffType.from(cuAtLeastOneProbeProtection));
        updateState(CHANNEL_CU_AT_LEAST_ONE_PROBE_MANUAL, OnOffType.from(cuAtLeastOneProbeManual));
        updateState(CHANNEL_CU_BATTERY_STATUS, new StringType(cuBatteryStatus));
        updateState(CHANNEL_CU_FAILURE_DISCOVERED, OnOffType.from(cuFailureDiscovered));
    }

    private Boolean channelExists(String channelID) {
        return thing.getChannel(channelID) != null;
    }

    @Override
    protected void refreshDevice(boolean refreshAll) {
        logger.debug("--- refreshDevice() : refreshing SINGLE... ({})", thing.getUID());

        Where w = deviceWhere;
        if (w == null) {
            logger.warn("refreshDevice() Where is null for thing {}", getThing().getUID());
            return;
        }

        String whereStr = w.value();

        if (isCentralUnit) {
            try {
                send(Thermoregulation.requestStatus(getWhere(whereStr)));
            } catch (OWNException e) {
                logger.warn("refreshDevice() central unit returned OWNException {}", e.getMessage());
            }
            return;
        }

        try {
            send(Thermoregulation.requestTemperature(whereStr));

            if (!((WhereThermo) w).isProbe()) {
                // for bus_thermo_zone request also other single channels updates
                send(Thermoregulation.requestSetPointTemperature(whereStr));
                send(Thermoregulation.requestMode(whereStr));

                // refresh ONLY subscribed channels
                if (channelExists(CHANNEL_FAN_SPEED)) {
                    send(Thermoregulation.requestFanCoilSpeed(whereStr));
                }
                if (channelExists(CHANNEL_CONDITIONING_VALVES) || channelExists(CHANNEL_HEATING_VALVES)) {
                    send(Thermoregulation.requestValvesStatus(whereStr));
                }
                if (channelExists(CHANNEL_ACTUATORS)) {
                    send(Thermoregulation.requestActuatorsStatus(whereStr));
                }
                if (channelExists(CHANNEL_LOCAL_OFFSET)) {
                    send(Thermoregulation.requestLocalOffset(whereStr));
                }
            }
        } catch (OWNException e) {
            logger.warn("refreshDevice() where='{}' returned OWNException {}", whereStr, e.getMessage());
        }
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> s = cuStateChannelsUpdateSchedule;
        if (s != null) {
            s.cancel(false);
            logger.debug("dispose() - scheduler stopped.");
        }
        super.dispose();
    }
}
