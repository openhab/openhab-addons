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
package org.openhab.binding.openwebnet.internal.handler;

import static org.openhab.binding.openwebnet.internal.OpenWebNetBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.openwebnet.internal.OpenWebNetBindingConstants;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.UnDefType;
import org.openwebnet4j.communication.OWNException;
import org.openwebnet4j.message.BaseOpenMessage;
import org.openwebnet4j.message.FrameException;
import org.openwebnet4j.message.MalformedFrameException;
import org.openwebnet4j.message.Thermoregulation;
import org.openwebnet4j.message.Where;
import org.openwebnet4j.message.WhereThermo;
import org.openwebnet4j.message.Who;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OpenWebNetThermoregulationHandler} is responsible for handling commands/messages for Thermoregulation
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

    private boolean isTempSensor = false; // is the thing a sensor ?

    private double currentSetPointTemp = 11.5d; // 11.5 is the default setTemp used in MyHomeUP mobile app

    private Thermoregulation.Function currentFunction = Thermoregulation.Function.GENERIC;

    public OpenWebNetThermoregulationHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        super.bridgeStatusChanged(bridgeStatusInfo);
        // when the bridge is ONLINE request for thing states (temp, setTemp, fanSpeed...)
        if (bridgeStatusInfo.getStatus().equals(ThingStatus.ONLINE)) {
            refreshDevice(false);
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
            default: {
                logger.warn("handleChannelCommand() Unsupported ChannelUID {}", channel.getId());
            }
        }
    }

    @Override
    protected void requestChannelState(ChannelUID channel) {
        refreshDevice(false);
    }

    @Override
    protected Where buildBusWhere(String wStr) throws IllegalArgumentException {
        WhereThermo wt = new WhereThermo(wStr);
        if (wt.isProbe()) {
            isTempSensor = true;
        }
        return wt;
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
                    send(Thermoregulation.requestWriteSetpointTemperature(w.value(), newTemp, currentFunction));
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
                    Thermoregulation.OperationMode mode = Thermoregulation.OperationMode.valueOf(command.toString());
                    send(Thermoregulation.requestWriteMode(w.value(), mode, currentFunction, currentSetPointTemp));
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

        if (w.mode() == null) {
            logger.debug("updateModeAndFunction() Could not parse Mode from: {}", tmsg.getFrameValue());
            return;
        }
        if (w.function() == null) {
            logger.debug("updateModeAndFunction() Could not parse Function from: {}", tmsg.getFrameValue());
            return;
        }

        Thermoregulation.OperationMode mode = w.mode();
        Thermoregulation.Function function = w.function();

        if (w == Thermoregulation.WhatThermo.HEATING) {
            function = Thermoregulation.Function.HEATING;
        } else if (w == Thermoregulation.WhatThermo.CONDITIONING) {
            function = Thermoregulation.Function.COOLING;
        }

        updateState(CHANNEL_MODE, new StringType(mode.toString()));
        updateState(CHANNEL_FUNCTION, new StringType(function.toString()));

        // store current function
        currentFunction = function;
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
            double temp = Thermoregulation.parseTemperature(tmsg);
            updateState(CHANNEL_TEMP_SETPOINT, getAsQuantityTypeOrNull(temp, SIUnits.CELSIUS));
            currentSetPointTemp = temp;
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

    @Override
    protected void refreshDevice(boolean refreshAll) {
        if (deviceWhere != null) {
            String w = deviceWhere.value();
            try {
                send(Thermoregulation.requestTemperature(w));
                if (!this.isTempSensor) {
                    // for bus_thermo_zone request also other single channels updates
                    send(Thermoregulation.requestSetPointTemperature(w));
                    send(Thermoregulation.requestFanCoilSpeed(w));
                    send(Thermoregulation.requestMode(w));
                    send(Thermoregulation.requestValvesStatus(w));
                    send(Thermoregulation.requestActuatorsStatus(w));
                }
            } catch (OWNException e) {
                logger.warn("refreshDevice() where='{}' returned OWNException {}", w, e.getMessage());
            }
        }
    }
}
