/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.openwebnet.handler;

import static org.openhab.binding.openwebnet.OpenWebNetBindingConstants.*;
import static org.openhab.core.library.unit.SIUnits.CELSIUS;

import java.math.BigDecimal;
import java.util.Set;

import javax.measure.Unit;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.openwebnet.OpenWebNetBindingConstants;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
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
import org.openwebnet4j.message.Thermoregulation.FAN_COIL_SPEED;
import org.openwebnet4j.message.Thermoregulation.FUNCTION;
import org.openwebnet4j.message.Thermoregulation.OPERATION_MODE;
import org.openwebnet4j.message.Thermoregulation.VALVE_OR_ACTUATOR_STATUS;
import org.openwebnet4j.message.Thermoregulation.WHAT;
import org.openwebnet4j.message.Where;
import org.openwebnet4j.message.WhereThermo;
import org.openwebnet4j.message.Who;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OpenWebNetThermoregulationHandler} is responsible for handling commands/messages for a Thermoregulation
 * OpenWebNet device. It extends the abstract {@link OpenWebNetThingHandler}.
 *
 * @author Massimo Valla - Initial contribution
 * @author Andrea Conte - Thermoregulation
 * @author Gilberto Cocchi - Thermoregulation
 */
@NonNullByDefault
public class OpenWebNetThermoregulationHandler extends OpenWebNetThingHandler {

    private final Logger logger = LoggerFactory.getLogger(OpenWebNetThermoregulationHandler.class);

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = OpenWebNetBindingConstants.THERMOREGULATION_SUPPORTED_THING_TYPES;

    private Double currentSetPointTemp = 11.5d; // 11.5 is the default setTemp used in MyHomeUP mobile app

    private Thermoregulation.FUNCTION currentFunction = Thermoregulation.FUNCTION.GENERIC;

    public OpenWebNetThermoregulationHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        super.bridgeStatusChanged(bridgeStatusInfo);

        // when the bridge is ONLINE request for thing states (temp, setTemp, fanSpeed...)
        if (bridgeStatusInfo.getStatus().equals(ThingStatus.ONLINE)) {
            logger.debug("bridgeStatusChanged() thing={}", thing.getUID());

            if (deviceWhere != null) {
                String w = deviceWhere.value();
                try {
                    // request single channels updates
                    send(Thermoregulation.requestTemperature(w));
                    send(Thermoregulation.requestSetPointTemperature(w));
                    send(Thermoregulation.requestFanCoilSpeed(w));
                    send(Thermoregulation.requestMode(w));
                    send(Thermoregulation.requestValveStatus(w));
                    send(Thermoregulation.requestActuatorStatus(w));
                } catch (OWNException e) {
                    logger.error("bridgeStatusChanged() OWNException thingUID={}: {}", thing.getUID(), e.getMessage());
                }
            }
        }
    }

    @Override
    protected void handleChannelCommand(ChannelUID channel, Command command) {
        switch (channel.getId()) {
            case CHANNEL_TEMP_SETPOINT:
                handleSetpointCommand(command);
                break;
            case CHANNEL_FUNCTION:
                handleFunction(command);
                break;
            case CHANNEL_MODE:
                handleMode(command);
                break;
            case CHANNEL_FAN_SPEED:
                handleSetFanSpeedCommand(command);
                break;
            default: {
                logger.warn("handleChannelCommand() Unsupported ChannelUID {}", channel.getId());
            }
        }
    }

    @Override
    protected void requestChannelState(ChannelUID channel) {
        logger.debug("requestChannelState() thingUID={} channel={}", thing.getUID(), channel.getId());
        if (deviceWhere != null) {
            String w = deviceWhere.value();
            try {
                // for bus_thermostat request single channels updates
                send(Thermoregulation.requestTemperature(w));
                send(Thermoregulation.requestSetPointTemperature(w));
                send(Thermoregulation.requestFanCoilSpeed(w));
                send(Thermoregulation.requestMode(w));
                send(Thermoregulation.requestValveStatus(w));
                send(Thermoregulation.requestActuatorStatus(w));
            } catch (OWNException e) {
                logger.error("requestChannelState() OWNException thingUID={} channel={}: {}", thing.getUID(),
                        channel.getId(), e.getMessage());
            }
        }
    }

    @Override
    protected Where buildBusWhere(String wStr) throws IllegalArgumentException {
        return new WhereThermo(wStr);
    }

    @Override
    protected String ownIdPrefix() {
        return Who.THERMOREGULATION.value().toString();
    }

    private void handleSetFanSpeedCommand(Command command) {
        logger.debug("handleSetFanSpeedCommand() (command={})", command);
        if (command instanceof StringType) {
            Where w = deviceWhere;
            if (w != null) {
                try {
                    FAN_COIL_SPEED speed = FAN_COIL_SPEED.valueOf(command.toString());
                    send(Thermoregulation.requestWriteFanCoilSpeed(w.value(), speed));
                } catch (OWNException e) {
                    logger.warn("handleSetFanSpeedCommand() {}", e.getMessage());
                } catch (IllegalArgumentException e) {
                    logger.warn("handleSetFanSpeedCommand() Unsupported command {} for thing {}", command,
                            getThing().getUID());
                    return;
                }
            }
        } else {
            logger.warn("handleSetFanSpeedCommand() Unsupported command {} for thing {}", command, getThing().getUID());
        }
    }

    private void handleSetpointCommand(Command command) {
        logger.debug("handleSetpointCommand() (command={})", command);
        if (command instanceof QuantityType || command instanceof DecimalType) {
            Where w = deviceWhere;
            if (w != null) {
                BigDecimal value = BigDecimal.ZERO;
                if (command instanceof QuantityType) {
                    Unit<Temperature> unit = CELSIUS;
                    QuantityType<Temperature> quantity = commandToQuantityType(command, unit);
                    value = quantity.toBigDecimal();
                } else {
                    value = ((DecimalType) command).toBigDecimal();
                }
                try {
                    send(Thermoregulation.requestWriteSetpointTemperature(w.value(), value.floatValue(),
                            currentFunction));
                } catch (MalformedFrameException | OWNException e) {
                    logger.warn("handleSetpointCommand() {}", e.getMessage());
                }
            }
        } else {
            logger.warn("handleSetpointCommand() Unsupported command {} for thing {}", command, getThing().getUID());
        }
    }

    private void handleMode(Command command) {
        if (command instanceof StringType) {
            Where w = deviceWhere;
            if (w != null) {
                try {
                    OPERATION_MODE mode = OPERATION_MODE.valueOf(((StringType) command).toString());
                    if (mode == OPERATION_MODE.MANUAL) {
                        logger.debug("handleMode() mode={} function={} setPointTemp={}°", mode.toString(),
                                currentFunction.toString(), currentSetPointTemp);
                    } else {
                        logger.debug("handleMode() mode={} function={}", mode.toString(), currentFunction.toString());
                    }
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
        logger.debug("handleFunction() (command={})", command);
        if (command instanceof StringType) {
            Where w = deviceWhere;
            if (w != null) {
                try {
                    FUNCTION function = FUNCTION.valueOf(((StringType) command).toString());
                    logger.debug("handleFunction() mode={}", function.toString());
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

    private QuantityType<Temperature> commandToQuantityType(Command command, Unit<Temperature> unit) {
        return new QuantityType<Temperature>(command.toFullString());
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
            if (msg.getDim() == Thermoregulation.DIM.TEMPERATURE
                    || msg.getDim() == Thermoregulation.DIM.PROBE_TEMPERATURE) {
                updateTemperature((Thermoregulation) msg);
            } else if (msg.getDim() == Thermoregulation.DIM.TEMP_SETPOINT
                    || msg.getDim() == Thermoregulation.DIM.COMPLETE_PROBE_STATUS) {
                updateSetpoint((Thermoregulation) msg);
            } else if (msg.getDim() == Thermoregulation.DIM.VALVES_STATUS) {
                updateValveStatus((Thermoregulation) msg);
            } else if (msg.getDim() == Thermoregulation.DIM.ACTUATOR_STATUS) {
                updateActuatorStatus((Thermoregulation) msg);
            } else if (msg.getDim() == Thermoregulation.DIM.FAN_COIL_SPEED) {
                updateFanCoilSpeed((Thermoregulation) msg);
            } else {
                logger.debug("handleMessage() Ignoring unsupported DIM {} for thing {}. Frame={}", msg.getDim(),
                        getThing().getUID(), msg);
            }
        }
    }

    private void updateModeAndFunction(Thermoregulation tmsg) {
        logger.debug("updateModeAndFunction() for thing: {} msg={}", thing.getUID(), tmsg);

        if (tmsg.getWhat() == null) {
            logger.warn("updateModeAndFunction() Could not parse Mode from: {}", tmsg.getFrameValue());
            return;
        }

        try {
            logger.debug("updateModeAndFunction() WHAT={}", tmsg.getWhat());
            WHAT w = WHAT.fromValue(tmsg.getWhat().value());

            OPERATION_MODE mode = w.mode();
            FUNCTION function = w.function();

            if (w == WHAT.HEATING) {
                function = FUNCTION.HEATING;
            } else if (w == WHAT.CONDITIONING) {
                function = FUNCTION.COOLING;
            }

            if (mode == OPERATION_MODE.MANUAL) {
                logger.debug("updateModeAndFunction() function={} mode={} setPointTemp={}°", function.toString(),
                        mode.toString(), currentSetPointTemp);
            } else {
                logger.debug("updateModeAndFunction() function={} mode={}", function.toString(), mode.toString());
            }

            updateState(CHANNEL_MODE, new StringType(mode.toString()));
            updateState(CHANNEL_FUNCTION, new StringType(function.toString()));

            // store current function
            currentFunction = function;
        } catch (Exception e) {
            logger.warn("updateModeAndFunction() FrameException on frame {}: {}", tmsg, e.getMessage());
            // do not update channel state, simply skip wrong message
        }
    }

    private void updateTemperature(Thermoregulation tmsg) {
        logger.debug("updateTemperature() for thing: {}", thing.getUID());
        try {
            Double temp = Thermoregulation.parseTemperature(tmsg);
            updateState(CHANNEL_TEMPERATURE, new DecimalType(temp));
        } catch (FrameException e) {
            logger.warn("updateTemperature() FrameException on frame {}: {}", tmsg, e.getMessage());
            updateState(CHANNEL_TEMPERATURE, UnDefType.UNDEF);
        }
    }

    private void updateSetpoint(Thermoregulation tmsg) {
        logger.debug("updateSetpoint() for thing: {}", thing.getUID());
        String channelID = CHANNEL_TEMP_SETPOINT;
        try {
            Double temp = Thermoregulation.parseTemperature(tmsg);
            updateState(channelID, new DecimalType(temp));
            // store current setPoint T
            currentSetPointTemp = temp;
        } catch (FrameException e) {
            logger.warn("updateSetpoint() FrameException on frame {}: {}", tmsg, e.getMessage());
            updateState(channelID, UnDefType.UNDEF);
        }
    }

    private void updateFanCoilSpeed(Thermoregulation tmsg) {
        logger.debug("updateFanCoilSpeed() for thing: {}", thing.getUID());

        try {
            FAN_COIL_SPEED speed = Thermoregulation.parseFanCoilSpeed(tmsg);
            updateState(CHANNEL_FAN_SPEED, new StringType(speed.toString()));
        } catch (FrameException e) {
            logger.warn("updateFanCoilSpeed() FrameException on frame {}: {}", tmsg, e.getMessage());
            updateState(CHANNEL_FAN_SPEED, UnDefType.UNDEF);
        }
    }

    private void updateValveStatus(Thermoregulation tmsg) {
        logger.debug("updateValveStatus() for thing: {}", thing.getUID());

        try {
            VALVE_OR_ACTUATOR_STATUS cv = Thermoregulation.parseValveStatus(tmsg, WHAT.CONDITIONING);
            updateState(CHANNEL_CONDITIONING_VALVE, new StringType(cv.toString()));

            VALVE_OR_ACTUATOR_STATUS hv = Thermoregulation.parseValveStatus(tmsg, WHAT.HEATING);
            updateState(CHANNEL_HEATING_VALVE, new StringType(hv.toString()));
        } catch (FrameException e) {
            logger.warn("updateValveStatus() FrameException on frame {}: {}", tmsg, e.getMessage());
            updateState(CHANNEL_CONDITIONING_VALVE, UnDefType.UNDEF);
            updateState(CHANNEL_HEATING_VALVE, UnDefType.UNDEF);
        }
    }

    private void updateActuatorStatus(Thermoregulation tmsg) {
        logger.debug("updateActuatorStatus() for thing: {}", thing.getUID());
        try {
            VALVE_OR_ACTUATOR_STATUS hv = Thermoregulation.parseActuatorStatus(tmsg);
            updateState(CHANNEL_ACTUATOR, new StringType(hv.toString()));
        } catch (FrameException e) {
            logger.warn("updateActuatorStatus() FrameException on frame {}: {}", tmsg, e.getMessage());
            updateState(CHANNEL_ACTUATOR, UnDefType.UNDEF);
        }
    }

    @Override
    protected void refreshDevice(boolean refreshAll) {
        if (deviceWhere != null) {
            String w = deviceWhere.value();
            try {
                // for bus_thermostat request single channels updates
                send(Thermoregulation.requestTemperature(w));
                send(Thermoregulation.requestSetPointTemperature(w));
                send(Thermoregulation.requestFanCoilSpeed(w));
                send(Thermoregulation.requestMode(w));
                send(Thermoregulation.requestValveStatus(w));
                send(Thermoregulation.requestActuatorStatus(w));
            } catch (OWNException e) {
                logger.warn("refreshDevice() where='{}' returned OWNException {}", w, e.getMessage());
            }
        }
    }
}
