/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.vallox.handler;

import java.io.IOException;
import java.math.BigDecimal;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.vallox.ValloxBindingConstants;
import org.openhab.binding.vallox.internal.serial.StatusChangeListener;
import org.openhab.binding.vallox.internal.serial.Telegram;
import org.openhab.binding.vallox.internal.serial.ValloxProperty;
import org.openhab.binding.vallox.internal.serial.ValloxSerialInterface;
import org.openhab.binding.vallox.internal.serial.ValloxStore;
import org.openhab.binding.vallox.internal.serial.ValueChangeListener;
import org.openhab.binding.vallox.internal.serial.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ValloxSerialHandler} handles all commands of the
 * ESH runtime and interacts with the serial interface of the Vallox
 * venting unit.
 *
 * For the interaction with the ESH runtime it implements {@link ThingHandler}.
 * For getting informed about events of the vallox unit, it implements
 * {@link ValueChangeListener} and {@StatusChangeListener}.
 *
 * When initializing a Thing for this binding, it opens a new connection
 * to the vallox unit (currently via a TCP socket to a RS485 gateway),
 * starts a thread for listening for telegrams and a second thread for
 * a heartbeat signal. The latter is used to reliably discover when
 * the connection is lost in order to try to reconnect.
 *
 * @author Hauke Fuhrmann - Initial contribution
 *
 */
public class ValloxSerialHandler extends BaseThingHandler
        implements ThingHandler, ValueChangeListener, StatusChangeListener {

    private final Logger logger = LoggerFactory.getLogger(ValloxSerialHandler.class);
    private ValloxSerialInterface vallox;

    public ValloxSerialHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void dispose() {
        logger.debug("dispose()");
        if (vallox != null) {
            vallox.stopListening();
            vallox.getValueListener().remove(this);
            vallox.close();
            vallox = null;
        }
    }

    @Override
    public void initialize() {
        logger.debug("initialize()");
        updateStatus(ThingStatus.UNKNOWN);
        if (vallox == null) {
            vallox = new ValloxSerialInterface(this.scheduler);
            try {
                Configuration configuration = getThing().getConfiguration();
                String host = (String) configuration.get(ValloxBindingConstants.PARAMETER_HOST);
                logger.debug("vallox.host={}", host);
                BigDecimal port;
                Object portObject = configuration.get(ValloxBindingConstants.PARAMETER_PORT);
                logger.debug("vallox.port={} of type {}", portObject, portObject.getClass().getName());
                // this might throw NumberFormatException if the object is passed as a different type, e.g. String
                port = (BigDecimal) portObject;
                vallox.connect(host, port.intValue());
                vallox.getValueListener().add(this);
                vallox.getStatusListener().add(this);
                vallox.startListening();
                vallox.startHeartbeat();
                updateStatus(ThingStatus.ONLINE);
            } catch (IOException e) {
                String message = "Failed to start connection to Vallox serial interface. ";
                logger.warn(message, e);
                // want a readable message here
                // as thing status is shown in GUI to end users, it should be very comprehensible
                this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, message + e.getMessage());
            } catch (NumberFormatException e) {
                String message = "Failed to read input for port parameter of Vallox interface. Must be a plain positive integer. Don't use quotes in a .thing-File!";
                logger.warn(message, e);
                // want a readable message here
                // as thing status is shown in GUI to end users, it should be very comprehensible
                this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, message + e.getMessage());
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (vallox == null) {
            return;
        }
        try {
            ValloxProperty channelProperty = ValloxProperty.getProperty(channelUID.getId());
            if (command instanceof RefreshType) {
                synchronized (this) {
                    vallox.sendPoll(channelProperty);
                    Thread.sleep(200);
                }
            } else if (command instanceof DecimalType) {
                handleDecimalCommand((DecimalType) command, channelProperty);
            } else if (command instanceof OnOffType) {
                switch (channelProperty) {
                    case POWER_STATE:
                    case CO2_ADJUST_STATE:
                    case HUMIDITY_ADJUST_STATE:
                    case HEATING_STATE:
                        // send the first 4 bits of the Select byte; others are readonly
                        // 1 1 1 1 1 1 1 1
                        // | | | | | | | |
                        // | | | | | | | +- 0 Power state
                        // | | | | | | +--- 1 CO2 Adjust state
                        // | | | | | +----- 2 %RH adjust state
                        // | | | | +------- 3 Heating state
                        // | | | +--------- 4 Filterguard indicator
                        // | | +----------- 5 Heating indicator
                        // | +------------- 6 Fault indicator
                        // +--------------- 7 service reminder
                        byte newVal = (byte) (((OnOffType) command == OnOffType.ON) ? 1 : 0);
                        ValloxStore s = vallox.getValloxStore();
                        ValloxProperty c = channelProperty;
                        byte v = 0;
                        // if the variable to set is the corresponding bit, set it; otherwise get the value from store
                        // Bit 4
                        v += (c == ValloxProperty.HEATING_STATE) ? newVal : (s.heatingState ? 1 : 0);
                        v = (byte) (v << 1);
                        // Bit 3
                        v += (c == ValloxProperty.HUMIDITY_ADJUST_STATE) ? newVal : (s.humidityAdjustState ? 1 : 0);
                        v = (byte) (v << 1);
                        // Bit 2
                        v += (c == ValloxProperty.CO2_ADJUST_STATE) ? newVal : (s.cO2AdjustState ? 1 : 0);
                        v = (byte) (v << 1);
                        // Bit 1
                        v += (c == ValloxProperty.POWER_STATE) ? newVal : (s.powerState ? 1 : 0);
                        vallox.send(Variable.SELECT.getKey(), v);
                    default:
                        logger.warn(
                                "Trying to send OnOffType set-command to not supported channel; either read-only or other type required: {} Ignoring.",
                                channelProperty);
                        break;
                }
            }
        } catch (IOException | InterruptedException e) {
            logger.warn("Failed to handle command to Vallox serial interface. ", e);
        }
    }

    private void handleDecimalCommand(DecimalType command, ValloxProperty channelProperty) throws IOException {
        byte value = command.byteValue();
        logger.debug("Setting channel {} to value {}.", channelProperty, value);
        switch (channelProperty) {
            case FAN_SPEED:
                vallox.send(Variable.FAN_SPEED.getKey(), Telegram.convertBackFanSpeed(value));
                break;
            case FAN_SPEED_MAX:
                vallox.send(Variable.FAN_SPEED_MAX.getKey(), Telegram.convertBackFanSpeed(value));
                break;
            case FAN_SPEED_MIN:
                vallox.send(Variable.FAN_SPEED_MIN.getKey(), Telegram.convertBackFanSpeed(value));
                break;
            case DC_FAN_OUTPUT_ADJUSTMENT:
                vallox.send(Variable.DC_FAN_OUTPUT_ADJUSTMENT.getKey(), value);
                break;
            case DC_FAN_INPUT_ADJUSTMENT:
                vallox.send(Variable.DC_FAN_INPUT_ADJUSTMENT.getKey(), value);
                break;
            case HRC_BYPASS_THRESHOLD:
                vallox.send(Variable.HRC_BYPASS.getKey(), Telegram.convertBackTemperature(value));
                break;
            case INPUT_FAN_STOP_THRESHOLD:
                vallox.send(Variable.INPUT_FAN_STOP.getKey(), Telegram.convertBackTemperature(value));
                break;
            case HEATING_SETPOINT:
                vallox.send(Variable.HEATING_SET_POINT.getKey(), Telegram.convertBackTemperature(value));
                break;
            case PRE_HEATING_SETPOINT:
                vallox.send(Variable.PRE_HEATING_SET_POINT.getKey(), Telegram.convertBackTemperature(value));
                break;
            case CELL_DEFROSTING_THRESHOLD:
                vallox.send(Variable.CELL_DEFROSTING.getKey(), Telegram.convertBackTemperature(value));
                break;
            case POWER_STATE:
            case CO2_ADJUST_STATE:
            case HUMIDITY_ADJUST_STATE:
            case HEATING_STATE:
                logger.warn("Trying to send DecimalType command to OnOffType channel: {} Ignoring.", channelProperty);
                break;
            default:
                logger.warn("Trying to send set-command to read-only channel: {} Ignoring.", channelProperty);
                break;
        }
    }

    @Override
    public void notifyChanged(ValloxProperty prop) {
        ValloxStore vs = vallox.getValloxStore();
        ChannelUID channel = new ChannelUID(this.getThing().getUID(), prop.getChannelName());
        State state = null;
        switch (prop) {
            case ADJUSTMENT_INTERVAL_MINUTES:
                state = new DecimalType(vs.adjustmentIntervalMinutes);
                break;
            case AUTOMATIC_HUMIDITY_LEVEL_SEEKER_STATE:
                state = getOnOff(vs.automaticHumidityLevelSeekerState);
                break;
            case AVERAGE_EFFICIENCY:
                state = new DecimalType(vs.averageEfficiency);
                break;
            case BASIC_HUMIDITY_LEVEL:
                state = new DecimalType(vs.basicHumidityLevel);
                break;
            case BOOST_SWITCH_MODE:
                state = getOnOff(vs.boostSwitchMode);
                break;
            case CASCADE_ADJUST:
                state = getOnOff(vs.cascadeAdjust);
                break;
            case CELL_DEFROSTING_THRESHOLD:
                state = new DecimalType(vs.cellDefrostingThreshold);
                break;
            case CO2_ADJUST_STATE:
                state = getOnOff(vs.cO2AdjustState);
                break;
            case CO2_HIGH:
                state = new DecimalType(vs.cO2High);
                break;
            case CO2_LOW:
                state = new DecimalType(vs.cO2Low);
                break;
            case CO2_SETPOINT_HIGH:
                state = new DecimalType(vs.cO2SetPointHigh);
                break;
            case CO2_SETPOINT_LOW:
                state = new DecimalType(vs.cO2SetPointLow);
                break;
            case DAMPER_MOTOR_POSITION:
                state = getOnOff(vs.damperMotorPosition);
                break;
            case DC_FAN_INPUT_ADJUSTMENT:
                state = new DecimalType(vs.dCFanInputAdjustment);
                break;
            case DC_FAN_OUTPUT_ADJUSTMENT:
                state = new DecimalType(vs.dCFanOutputAdjustment);
                break;
            case EXHAUST_FAN_OFF:
                state = getOnOff(vs.exhaustFanOff);
                break;
            case FAN_SPEED:
                state = new DecimalType(vs.fanSpeed);
                break;
            case FAN_SPEED_MAX:
                state = new DecimalType(vs.fanSpeedMax);
                break;
            case FAN_SPEED_MIN:
                state = new DecimalType(vs.fanSpeedMin);
                break;
            case FAULT_INDICATOR:
                state = getOnOff(vs.faultIndicator);
                break;
            case FAULT_SIGNAL_RELAY_CLOSED:
                state = getOnOff(vs.faultSignalRelayClosed);
                break;
            case FILTER_GUARD_INDICATOR:
                state = getOnOff(vs.filterGuardIndicator);
                break;
            case FIRE_PLACE_BOOSTER_CLOSED:
                state = getOnOff(vs.firePlaceBoosterClosed);
                break;
            case HEATING_INDICATOR:
                state = getOnOff(vs.heatingIndicator);
                break;
            case HEATING_SETPOINT:
                state = new DecimalType(vs.heatingSetPoint);
                break;
            case HEATING_STATE:
                state = getOnOff(vs.heatingState);
                break;
            case HRC_BYPASS_THRESHOLD:
                state = new DecimalType(vs.hrcBypassThreshold);
                break;
            case HUMIDITY:
                state = new DecimalType(vs.humidity);
                break;
            case HUMIDITY_ADJUST_STATE:
                state = getOnOff(vs.humidityAdjustState);
                break;
            case HUMIDITY_SENSOR_1:
                state = new DecimalType(vs.humiditySensor1);
                break;
            case HUMIDITY_SENSOR_2:
                state = new DecimalType(vs.humiditySensor2);
                break;
            case INCOMMING_CURRENT:
                state = new DecimalType(vs.incommingCurrent);
                break;
            case IN_EFFICIENCY:
                state = new DecimalType(vs.inEfficiency);
                break;
            case INPUT_FAN_STOP_THRESHOLD:
                state = new DecimalType(vs.inputFanStopThreshold);
                break;
            case IO_PORT_MULTI_PURPOSE_1:
                state = new DecimalType(vs.ioPortMultiPurpose1);
                break;
            case IO_PORT_MULTI_PURPOSE_2:
                state = new DecimalType(vs.ioPortMultiPurpose2);
                break;
            case LAST_ERROR_NUMBER:
                state = new DecimalType(vs.lastErrorNumber);
                break;
            case MAX_SPEED_LIMIT_MODE:
                state = getOnOff(vs.maxSpeedLimitMode);
                break;
            case OUT_EFFICIENCY:
                state = new DecimalType(vs.outEfficiency);
                break;
            case POST_HEATING_ON:
                state = getOnOff(vs.postHeatingOn);
                break;
            case POWER_STATE:
                state = getOnOff(vs.powerState);
                break;
            case PRE_HEATING_ON:
                state = getOnOff(vs.preHeatingOn);
                break;
            case PRE_HEATING_SETPOINT:
                state = new DecimalType(vs.preHeatingSetPoint);
                break;
            case PROGRAM:
                state = new DecimalType(vs.program);
                break;
            case PROGRAM_2:
                state = new DecimalType(vs.program2);
                break;
            case RADIATOR_TYPE:
                state = getOnOff(vs.radiatorType);
                break;
            case SELECT_STATUS:
                state = new DecimalType(vs.selectStatus);
                break;
            case SERVICE_REMINDER:
                state = new DecimalType(vs.serviceReminder);
                break;
            case SERVICE_REMINDER_INDICATOR:
                state = getOnOff(vs.serviceReminderIndicator);
                break;
            case SUPPLY_FAN_OFF:
                state = getOnOff(vs.supplyFanOff);
                break;
            case TEMP_EXHAUST:
                state = new DecimalType(vs.tempExhaust);
                break;
            case TEMP_INCOMMING:
                state = new DecimalType(vs.tempIncomming);
                break;
            case TEMP_INSIDE:
                state = new DecimalType(vs.tempInside);
                break;
            case TEMP_OUTSIDE:
                state = new DecimalType(vs.tempOutside);
                break;
            default:
                logger.warn("Got update notification for unknown vallox property: {}", channel);
                break;
        }
        // do not check whether value really has changed to reduce amount of updates
        // see https://community.openhab.org/t/get-state-of-things-channel-in-basethinghandler/33602
        this.updateState(channel, state);
        logger.debug("Updated state for channel {} to {}", channel, state);
    }

    @Override
    public void statusChanged(ThingStatus status, ThingStatusDetail detail, String message) {
        if (message == null) {
            if (detail == ThingStatusDetail.NONE) {
                updateStatus(status);
            } else {
                updateStatus(status, detail);
            }
        } else {
            updateStatus(status, detail, message);
        }
    }

    private static OnOffType getOnOff(boolean on) {
        return on ? OnOffType.ON : OnOffType.OFF;
    }

}
