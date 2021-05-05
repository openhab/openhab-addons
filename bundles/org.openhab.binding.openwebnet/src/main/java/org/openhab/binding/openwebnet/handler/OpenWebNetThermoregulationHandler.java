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

import static org.openhab.binding.openwebnet.OpenWebNetBindingConstants.CHANNEL_FAN_SPEED;
import static org.openhab.binding.openwebnet.OpenWebNetBindingConstants.CHANNEL_FUNCTION;
import static org.openhab.binding.openwebnet.OpenWebNetBindingConstants.CHANNEL_MODE;
import static org.openhab.binding.openwebnet.OpenWebNetBindingConstants.CHANNEL_TEMPERATURE;
import static org.openhab.binding.openwebnet.OpenWebNetBindingConstants.CHANNEL_TEMP_SETPOINT;
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

    // private enum Mode {
    // // TODO make it a single map and integrate it with Thermoregulation.WHAT to have automatic translation
    // UNKNOWN("UNKNOWN"),
    // AUTO("AUTO"),
    // MANUAL("MANUAL"),
    // PROTECTION("PROTECTION"),
    // OFF("OFF");

    // private final String mode;

    // Mode(final String mode) {
    // this.mode = mode;
    // }

    // @Override
    // public String toString() {
    // return mode;
    // }
    // }

    // private enum ThermoFunction {
    // UNKNOWN(-1),
    // COOL(0),
    // HEAT(1),
    // GENERIC(3);

    // private final int function;

    // ThermoFunction(final int f) {
    // this.function = f;
    // }

    // public int getValue() {
    // return function;
    // }
    // }

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = OpenWebNetBindingConstants.THERMOREGULATION_SUPPORTED_THING_TYPES;

    // private boolean isCentralUnit = false;
    // private Mode currentSetMode = Mode.UNKNOWN;
    // private Mode currentActiveMode = Mode.UNKNOWN;
    // private ThermoFunction thermoFunction = ThermoFunction.UNKNOWN;
    // private Thermoregulation.LOCAL_OFFSET localOffset = Thermoregulation.LOCAL_OFFSET.NORMAL;

    // 11.5 is the default setTemp used in MyHomeUP mobile app
    private Double currentSetPointTemp = 11.5d;

    private Thermoregulation.FUNCTION currentFunction = Thermoregulation.FUNCTION.GENERIC;

    public OpenWebNetThermoregulationHandler(Thing thing) {
        super(thing);
        // TODO not yet supported
        // if (OpenWebNetBindingConstants.THING_TYPE_BUS_THERMO_CENTRAL_UNIT.equals(thing.getThingTypeUID())) {
        // isCentralUnit = true;
        // }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        super.bridgeStatusChanged(bridgeStatusInfo);

        // when the bridge is ONLINE request for thing states (temp, setTemp, fanSpeed...)
        if (bridgeStatusInfo.getStatus().equals(ThingStatus.ONLINE)) {
            logger.debug("bridgeStatusChanged() thing={}", thing.getUID());

            try {
                // request single channels updates
                bridgeHandler.gateway.send(Thermoregulation.requestTemperature(deviceWhere.value()));
                bridgeHandler.gateway.send(Thermoregulation.requestSetPointTemperature(deviceWhere.value()));
                bridgeHandler.gateway.send(Thermoregulation.requestFanCoilSpeed(deviceWhere.value()));
                bridgeHandler.gateway.send(Thermoregulation.requestMode(deviceWhere.value()));
            } catch (OWNException e) {
                logger.error("bridgeStatusChanged() OWNException thingUID={}: {}", thing.getUID(), e.getMessage());
            }
        }
    }

    @Override
    protected void handleChannelCommand(ChannelUID channel, Command command) {
        switch (channel.getId()) {
            // TODO case CHANNEL_ALL_TEMP_SETPOINT:
            case CHANNEL_TEMP_SETPOINT:
                handleSetpointCommand(command);
                break;
            // TODO case CHANNEL_ALL_SET_MODE:
            // TODO case CHANNEL_SET_MODE:
            // handleModeCommand(command);
            // logger.trace("handleChannelCommand() Unsupported handleModeCommand! {}", channel.getId());
            // break;
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

        try {
            // this works for bus_thermostat, not for central unit
            // bridgeHandler.gateway.send(Thermoregulation.requestStatus(deviceWhere.value()));

            // for bus_thermostat request single channels updates
            bridgeHandler.gateway.send(Thermoregulation.requestTemperature(deviceWhere.value()));
            bridgeHandler.gateway.send(Thermoregulation.requestSetPointTemperature(deviceWhere.value()));
            bridgeHandler.gateway.send(Thermoregulation.requestFanCoilSpeed(deviceWhere.value()));
            bridgeHandler.gateway.send(Thermoregulation.requestMode(deviceWhere.value()));
        } catch (OWNException e) {
            logger.error("requestChannelState() OWNException thingUID={} channel={}: {}", thing.getUID(),
                    channel.getId(), e.getMessage());
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
            FAN_COIL_SPEED speed = FAN_COIL_SPEED.valueOf(command.toString());

            try {
                bridgeHandler.gateway.send(Thermoregulation.requestWriteFanCoilSpeed(deviceWhere.value(), speed));
            } catch (OWNException e) {
                logger.warn("handleSetFanSpeedCommand() {}", e.getMessage());
            }
        } else {
            logger.warn("handleSetFanSpeedCommand() Cannot handle command {} for thing {}", command,
                    getThing().getUID());
        }
    }

    private void handleSetpointCommand(Command command) {
        logger.debug("handleSetpointCommand() (command={})", command);

        if (command instanceof QuantityType || command instanceof DecimalType) {
            BigDecimal value = BigDecimal.ZERO;
            if (command instanceof QuantityType) {
                Unit<Temperature> unit = CELSIUS;
                QuantityType<Temperature> quantity = commandToQuantityType(command, unit);
                value = quantity.toBigDecimal();
            } else {
                value = ((DecimalType) command).toBigDecimal();
            }

            try {
                Thermoregulation mm = Thermoregulation.requestWriteSetpointTemperature(deviceWhere.value(),
                        value.floatValue(), currentFunction);

                bridgeHandler.gateway.send(mm);
            } catch (MalformedFrameException | OWNException e) {
                logger.warn("handleSetpointCommand() {}", e.getMessage());
            }
        } else {
            logger.warn("handleSetpointCommand() Cannot handle command {} for thing {}", command, getThing().getUID());
        }
    }

    private void handleMode(Command command) {
        if (command instanceof StringType) {
            try {

                OPERATION_MODE mode = OPERATION_MODE.valueOf(((StringType) command).toString());
                if (mode == OPERATION_MODE.MANUAL)
                    logger.debug("handleMode() mode={} function={} setPointTemp={}°", mode.toString(),
                            currentFunction.toString(), currentSetPointTemp);
                else
                    logger.debug("handleMode() mode={} function={}", mode.toString(), currentFunction.toString());

                bridgeHandler.gateway.send(Thermoregulation.requestWriteMode(deviceWhere.value(), mode, currentFunction,
                        currentSetPointTemp));
            } catch (OWNException e) {
                logger.warn("handleMode() {}", e.getMessage());
            }

        } else {
            logger.warn("Cannot handle command {} for thing {}", command, getThing().getUID());
        }
    }

    private void handleFunction(Command command) {
        logger.debug("handleFunction() (command={})", command);

        if (command instanceof StringType) {
            try {

                FUNCTION function = FUNCTION.valueOf(((StringType) command).toString());
                logger.debug("handleFunction() mode={}", function.toString());

                bridgeHandler.gateway.send(Thermoregulation.requestWriteFunction(deviceWhere.value(), function));
            } catch (OWNException e) {
                logger.warn("handleFunction() {}", e.getMessage());
            }

        } else {
            logger.warn("Cannot handle command {} for thing {}", command, getThing().getUID());
        }
    }

    // TODO not yet supported
    // private void handleModeCommand(Command command) {
    // logger.debug("handleModeCommand() (command={})", command);
    // if (command instanceof StringType) {
    // Thermoregulation.WHAT modeWhat = null;
    // try {
    // Mode mode = Mode.valueOf(((StringType) command).toString());
    // modeWhat = modeToWhat(mode);
    // } catch (IllegalArgumentException e) {
    // logger.warn("Cannot handle command {} for thing {}. Exception: {}", command, getThing().getUID(),
    // e.getMessage());
    // return;
    // }
    // logger.debug("handleModeCommand() modeWhat={}", modeWhat);
    // if (modeWhat != null) {
    // try {
    // bridgeHandler.gateway.send(Thermoregulation.requestWriteSetMode(deviceWhere.value(), modeWhat));
    // } catch (MalformedFrameException | OWNException e) {
    // logger.warn("handleModeCommand() {}", e.getMessage());
    // }
    // } else {
    // logger.warn("Cannot handle command {} for thing {}", command, getThing().getUID());
    // }
    // } else {
    // logger.warn("Cannot handle command {} for thing {}", command, getThing().getUID());
    // }
    // }

    private QuantityType<Temperature> commandToQuantityType(Command command, Unit<Temperature> unit) {
        return new QuantityType<Temperature>(command.toFullString());
    }

    @Override
    protected void handleMessage(BaseOpenMessage msg) {
        super.handleMessage(msg);
        if (msg.isCommand()) {
            updateModeAndFunction((Thermoregulation) msg);
        } else {
            if (msg.getDim() == null)
                return;

            if (msg.getDim() == Thermoregulation.DIM.TEMPERATURE) {
                updateTemperature((Thermoregulation) msg);
            } else if (msg.getDim() == Thermoregulation.DIM.TEMP_SETPOINT
                    || msg.getDim() == Thermoregulation.DIM.COMPLETE_PROBE_STATUS) {
                updateSetpoint((Thermoregulation) msg);

                // TODO not applicable for bus_thermostat
                // } else if (msg.getDim() == Thermoregulation.DIM.OFFSET) {
                // updateLocalMode((Thermoregulation) msg);
                // } else if (msg.getDim() == Thermoregulation.DIM.ACTUATOR_STATUS) {
                // updateActuatorStatus((Thermoregulation) msg);
                // } else if (msg.getDim() == Thermoregulation.DIM.TEMP_TARGET) {
                // updateTargetTemp((Thermoregulation) msg);
            } else if (msg.getDim() == Thermoregulation.DIM.FAN_COIL_SPEED) {
                updateFanCoilSpeed((Thermoregulation) msg);
            } else {
                logger.debug("handleMessage() Ignoring unsupported DIM {} for thing {}. Frame={}", msg.getDim(),
                        getThing().getUID(), msg);
            }
        }
    }

    // private void updateMode(Thermoregulation tmsg) {
    // logger.debug("updateMode() for thing: {} msg={}", thing.getUID(), tmsg);

    // try {
    // OPERATION_MODE mode = Thermoregulation.parseMode(tmsg);

    // if (mode == OPERATION_MODE.MANUAL)
    // logger.debug("updateMode() mode={} setPointTemp={}°", mode.toString(), currentSetPointTemp);
    // else
    // logger.debug("updateMode() mode={}", mode.toString());

    // updateState(CHANNEL_MODE, new StringType(mode.toString()));
    // } catch (FrameException e) {
    // logger.warn("updateMode() FrameException on frame {}: {}", tmsg, e.getMessage());
    // // do not update channel state, simply skip wrong message
    // }
    // }

    // private void updateFunction(Thermoregulation tmsg) {
    // logger.debug("updateFunction() for thing: {} msg={}", thing.getUID(), tmsg);

    // try {
    // FUNCTION function = Thermoregulation.parseFunction(tmsg);
    // logger.debug("updateFunction() FUNCTION={}", function.toString());
    // updateState(CHANNEL_FUNCTION, new StringType(function.toString()));

    // // store current function
    // currentFunction = function;
    // } catch (FrameException e) {
    // logger.warn("updateFunction() FrameException on frame {}: {}", tmsg, e.getMessage());
    // // do not update channel state, simply skip wrong message
    // }
    // }

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

            if (w == WHAT.HEATING)
                function = FUNCTION.HEATING;
            else if (w == WHAT.CONDITIONING)
                function = FUNCTION.COOLING;

            if (mode == OPERATION_MODE.MANUAL)
                logger.debug("updateModeAndFunction() function={} mode={} setPointTemp={}°", function.toString(),
                        mode.toString(), currentSetPointTemp);
            else
                logger.debug("updateModeAndFunction() function={} mode={}", function.toString(), mode.toString());

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

        // TODO not yet supported
        // if (isCentralUnit)
        // channelID = CHANNEL_ALL_TEMP_SETPOINT;

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

    // TODO not yet supported
    // private void updateLocalMode(Thermoregulation msg) {
    // logger.debug("updateLocalMode() for thing: {}", thing.getUID());
    // LOCAL_OFFSET newOffset;
    // try {
    // newOffset = msg.getLocalOffset();
    // if (newOffset != null) {
    // localOffset = newOffset;
    // logger.debug("updateLocalMode() new localMode={}", localOffset);
    // updateState(CHANNEL_LOCAL_MODE, new StringType(localOffset.getLabel()));
    // } else {
    // logger.warn("updateLocalMode() unrecognized local offset: {}", msg);
    // }
    // } catch (FrameException e) {
    // logger.warn("updateSetpoint() FrameException on frame {}: {}", msg, e.getMessage());
    // }
    // }

    // TODO not yet supported
    // private void updateActuatorStatus(Thermoregulation msg) {
    // logger.debug("updateActuatorStatus() for thing: {}", thing.getUID());
    // int actuator = msg.getActuator();
    // if (actuator == 1) {
    // updateState(CHANNEL_HEATING,
    // (msg.getActuatorStatus(actuator) == Thermoregulation.ACTUATOR_STATUS_ON ? OnOffType.ON
    // : OnOffType.OFF));
    // } else if (actuator == 2) {
    // updateState(CHANNEL_COOLING,
    // (msg.getActuatorStatus(actuator) == Thermoregulation.ACTUATOR_STATUS_ON ? OnOffType.ON
    // : OnOffType.OFF));
    // } else {
    // logger.warn("==OWN:ThermoHandler== actuator number {} is not handled for thing: {}", actuator,
    // thing.getUID());
    // }
    // }

    // TODO not yet supported
    // private void updateTargetTemp(Thermoregulation tmsg) {
    // logger.debug("updateTargetTemp() for thing: {}", thing.getUID());

    // try {
    // Double temp = Thermoregulation.parseTemperature(tmsg);
    // updateState(CHANNEL_TEMP_TARGET, new DecimalType(temp));
    // } catch (FrameException e) {
    // logger.warn("updateTargetTemp() FrameException on frame {}: {}", tmsg, e.getMessage());
    // updateState(CHANNEL_TEMP_TARGET, UnDefType.UNDEF);
    // }
    // }

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

    @Override
    protected void refreshDevice(boolean refreshAll) {
        requestChannelState(new ChannelUID("any:any:any:any"));
    }

    // private static Mode whatToMode(Thermoregulation.WHAT w) {
    // try {
    // Mode m = Mode.UNKNOWN;
    // switch (w) {
    // case PROGRAM_HEATING:
    // case PROGRAM_CONDITIONING:
    // case PROGRAM_GENERIC:
    // m = Mode.AUTO;
    // break;
    // case MANUAL_HEATING:
    // case MANUAL_CONDITIONING:
    // case MANUAL_GENERIC:
    // m = Mode.MANUAL;
    // break;
    // case PROTECTION_HEATING:
    // case PROTECTION_CONDITIONING:
    // case PROTECTION_GENERIC:
    // m = Mode.PROTECTION;
    // break;
    // case OFF_HEATING:
    // case OFF_CONDITIONING:
    // case OFF_GENERIC:
    // m = Mode.OFF;
    // break;
    // case CONDITIONING:
    // break;
    // case GENERIC:
    // break;
    // case HEATING:
    // break;
    // case HOLIDAY_CONDITIONING:
    // case HOLIDAY_GENERIC:
    // case HOLIDAY_HEATING:
    // default:
    // break;
    // }
    // return m;
    // } catch (Exception e) {
    // return Mode.UNKNOWN;
    // }
    // }

    // private Thermoregulation.WHAT modeToWhat(Mode m) {
    // Thermoregulation.WHAT newWhat = Thermoregulation.WHAT.GENERIC;
    // try {
    // switch (m) {
    // case AUTO:
    // if (thermoFunction == ThermoFunction.GENERIC) {
    // newWhat = Thermoregulation.WHAT.PROGRAM_GENERIC;
    // } else if (thermoFunction == ThermoFunction.COOL) {
    // newWhat = Thermoregulation.WHAT.PROGRAM_CONDITIONING;
    // } else {
    // newWhat = Thermoregulation.WHAT.PROGRAM_HEATING;
    // }
    // break;
    // case MANUAL:
    // if (thermoFunction == ThermoFunction.GENERIC) {
    // newWhat = Thermoregulation.WHAT.MANUAL_GENERIC;
    // } else if (thermoFunction == ThermoFunction.COOL) {
    // newWhat = Thermoregulation.WHAT.MANUAL_CONDITIONING;
    // } else {
    // newWhat = Thermoregulation.WHAT.MANUAL_HEATING;
    // }
    // break;
    // case PROTECTION:
    // if (thermoFunction == ThermoFunction.GENERIC) {
    // newWhat = Thermoregulation.WHAT.PROTECTION_GENERIC;
    // } else if (thermoFunction == ThermoFunction.COOL) {
    // newWhat = Thermoregulation.WHAT.PROTECTION_CONDITIONING;
    // } else {
    // newWhat = Thermoregulation.WHAT.PROTECTION_HEATING;
    // }
    // break;
    // case OFF:
    // if (thermoFunction == ThermoFunction.GENERIC) {
    // newWhat = Thermoregulation.WHAT.OFF_GENERIC;
    // } else if (thermoFunction == ThermoFunction.COOL) {
    // newWhat = Thermoregulation.WHAT.OFF_CONDITIONING;
    // } else {
    // newWhat = Thermoregulation.WHAT.OFF_HEATING;
    // }
    // break;
    // }

    // return newWhat;
    // } catch (Exception e) {
    // return Thermoregulation.WHAT.GENERIC;
    // }
    // }
}
