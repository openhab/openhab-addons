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

    private Logger logger = LoggerFactory.getLogger(ValloxSerialHandler.class);
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
            vallox = new ValloxSerialInterface();
            try {
                Configuration configuration = getThing().getConfiguration();
                String host = (String) configuration.get(ValloxBindingConstants.PARAMETER_HOST);
                BigDecimal port = (BigDecimal) configuration.get(ValloxBindingConstants.PARAMETER_PORT);
                vallox.connect(host, port.intValue());
                vallox.getValueListener().add(this);
                vallox.getStatusListener().add(this);
                vallox.startListening();
                vallox.startHeartbeat();
                updateStatus(ThingStatus.ONLINE);
            } catch (IOException e) {
                String message = "Failed to start connection to Vallox serial interface. ";
                logger.error(message, e);
                // want a readable message here
                // as thing status is shown in GUI to end users, it should be very comprehensible
                this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, message + e.getMessage());
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (vallox == null) {
            return;
        }
        try {
            ValloxProperty channelProperty = ValloxProperty.valueOf(channelUID.getId());
            if (command instanceof RefreshType) {
                synchronized (this) {
                    vallox.sendPoll(channelProperty);
                    Thread.sleep(200);
                }
            } else if (command instanceof DecimalType) {
                handleDecimalCommand((DecimalType) command, channelProperty);
            } else if (command instanceof OnOffType) {
                switch (channelProperty) {
                    case PowerState:
                    case CO2AdjustState:
                    case HumidityAdjustState:
                    case HeatingState:
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
                        v += (c == ValloxProperty.HeatingState) ? newVal : (s.heatingState ? 1 : 0);
                        v = (byte) (v << 1);
                        // Bit 3
                        v += (c == ValloxProperty.HumidityAdjustState) ? newVal : (s.humidityAdjustState ? 1 : 0);
                        v = (byte) (v << 1);
                        // Bit 2
                        v += (c == ValloxProperty.CO2AdjustState) ? newVal : (s.cO2AdjustState ? 1 : 0);
                        v = (byte) (v << 1);
                        // Bit 1
                        v += (c == ValloxProperty.PowerState) ? newVal : (s.powerState ? 1 : 0);
                        vallox.send(Variable.SELECT.getKey(), v);
                    default:
                        logger.warn(
                                "Trying to send OnOffType set-command to not supported channel; either read-only or other type required: {} Ignoring.",
                                channelProperty);
                        break;
                }
            }
        } catch (IOException | InterruptedException e) {
            logger.error("Failed to handle command to Vallox serial interface. ", e);
        }
    }

    private void handleDecimalCommand(DecimalType command, ValloxProperty channelProperty) throws IOException {
        byte value = command.byteValue();
        logger.debug("Setting channel {} to value {}.", channelProperty, value);
        switch (channelProperty) {
            case FanSpeed:
                vallox.send(Variable.FAN_SPEED.getKey(), Telegram.convertBackFanSpeed(value));
                break;
            case FanSpeedMax:
                vallox.send(Variable.FAN_SPEED_MAX.getKey(), Telegram.convertBackFanSpeed(value));
                break;
            case FanSpeedMin:
                vallox.send(Variable.FAN_SPEED_MIN.getKey(), Telegram.convertBackFanSpeed(value));
                break;
            case DCFanOutputAdjustment:
                vallox.send(Variable.DC_FAN_OUTPUT_ADJUSTMENT.getKey(), value);
                break;
            case DCFanInputAdjustment:
                vallox.send(Variable.DC_FAN_INPUT_ADJUSTMENT.getKey(), value);
                break;
            case HrcBypassThreshold:
                vallox.send(Variable.HRC_BYPASS.getKey(), Telegram.convertBackTemperature(value));
                break;
            case InputFanStopThreshold:
                vallox.send(Variable.INPUT_FAN_STOP.getKey(), Telegram.convertBackTemperature(value));
                break;
            case HeatingSetPoint:
                vallox.send(Variable.HEATING_SET_POINT.getKey(), Telegram.convertBackTemperature(value));
                break;
            case PreHeatingSetPoint:
                vallox.send(Variable.PRE_HEATING_SET_POINT.getKey(), Telegram.convertBackTemperature(value));
                break;
            case CellDefrostingThreshold:
                vallox.send(Variable.CELL_DEFROSTING.getKey(), Telegram.convertBackTemperature(value));
                break;
            case PowerState:
            case CO2AdjustState:
            case HumidityAdjustState:
            case HeatingState:
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
        ChannelUID channel = new ChannelUID(this.getThing().getUID(), prop.toString());
        State state = null;
        switch (prop) {
            case AdjustmentIntervalMinutes:
                state = new DecimalType(vs.adjustmentIntervalMinutes);
                break;
            case AutomaticHumidityLevelSeekerState:
                state = getOnOff(vs.automaticHumidityLevelSeekerState);
                break;
            case AverageEfficiency:
                state = new DecimalType(vs.averageEfficiency);
                break;
            case BasicHumidityLevel:
                state = new DecimalType(vs.basicHumidityLevel);
                break;
            case BoostSwitchMode:
                state = getOnOff(vs.boostSwitchMode);
                break;
            case CascadeAdjust:
                state = getOnOff(vs.cascadeAdjust);
                break;
            case CellDefrostingThreshold:
                state = new DecimalType(vs.cellDefrostingThreshold);
                break;
            case CO2AdjustState:
                state = getOnOff(vs.cO2AdjustState);
                break;
            case CO2High:
                state = new DecimalType(vs.cO2High);
                break;
            case CO2Low:
                state = new DecimalType(vs.cO2Low);
                break;
            case CO2SetPointHigh:
                state = new DecimalType(vs.cO2SetPointHigh);
                break;
            case CO2SetPointLow:
                state = new DecimalType(vs.cO2SetPointLow);
                break;
            case DamperMotorPosition:
                state = getOnOff(vs.damperMotorPosition);
                break;
            case DCFanInputAdjustment:
                state = new DecimalType(vs.dCFanInputAdjustment);
                break;
            case DCFanOutputAdjustment:
                state = new DecimalType(vs.dCFanOutputAdjustment);
                break;
            case ExhaustFanOff:
                state = getOnOff(vs.exhaustFanOff);
                break;
            case FanSpeed:
                state = new DecimalType(vs.fanSpeed);
                break;
            case FanSpeedMax:
                state = new DecimalType(vs.fanSpeedMax);
                break;
            case FanSpeedMin:
                state = new DecimalType(vs.fanSpeedMin);
                break;
            case FaultIndicator:
                state = getOnOff(vs.faultIndicator);
                break;
            case FaultSignalRelayClosed:
                state = getOnOff(vs.faultSignalRelayClosed);
                break;
            case FilterGuardIndicator:
                state = getOnOff(vs.filterGuardIndicator);
                break;
            case FirePlaceBoosterClosed:
                state = getOnOff(vs.firePlaceBoosterClosed);
                break;
            case HeatingIndicator:
                state = getOnOff(vs.heatingIndicator);
                break;
            case HeatingSetPoint:
                state = new DecimalType(vs.heatingSetPoint);
                break;
            case HeatingState:
                state = getOnOff(vs.heatingState);
                break;
            case HrcBypassThreshold:
                state = new DecimalType(vs.hrcBypassThreshold);
                break;
            case Humidity:
                state = new DecimalType(vs.humidity);
                break;
            case HumidityAdjustState:
                state = getOnOff(vs.humidityAdjustState);
                break;
            case HumiditySensor1:
                state = new DecimalType(vs.humiditySensor1);
                break;
            case HumiditySensor2:
                state = new DecimalType(vs.humiditySensor2);
                break;
            case IncommingCurrent:
                state = new DecimalType(vs.incommingCurrent);
                break;
            case InEfficiency:
                state = new DecimalType(vs.inEfficiency);
                break;
            case InputFanStopThreshold:
                state = new DecimalType(vs.inputFanStopThreshold);
                break;
            case IoPortMultiPurpose1:
                state = new DecimalType(vs.ioPortMultiPurpose1);
                break;
            case IoPortMultiPurpose2:
                state = new DecimalType(vs.ioPortMultiPurpose2);
                break;
            case LastErrorNumber:
                state = new DecimalType(vs.lastErrorNumber);
                break;
            case MaxSpeedLimitMode:
                state = getOnOff(vs.maxSpeedLimitMode);
                break;
            case OutEfficiency:
                state = new DecimalType(vs.outEfficiency);
                break;
            case PostHeatingOn:
                state = getOnOff(vs.postHeatingOn);
                break;
            case PowerState:
                state = getOnOff(vs.powerState);
                break;
            case PreHeatingOn:
                state = getOnOff(vs.preHeatingOn);
                break;
            case PreHeatingSetPoint:
                state = new DecimalType(vs.preHeatingSetPoint);
                break;
            case Program:
                state = new DecimalType(vs.program);
                break;
            case Program2:
                state = new DecimalType(vs.program2);
                break;
            case RadiatorType:
                state = getOnOff(vs.radiatorType);
                break;
            case SelectStatus:
                state = new DecimalType(vs.selectStatus);
                break;
            case ServiceReminder:
                state = new DecimalType(vs.serviceReminder);
                break;
            case ServiceReminderIndicator:
                state = getOnOff(vs.serviceReminderIndicator);
                break;
            case SupplyFanOff:
                state = getOnOff(vs.supplyFanOff);
                break;
            case TempExhaust:
                state = new DecimalType(vs.tempExhaust);
                break;
            case TempIncomming:
                state = new DecimalType(vs.tempIncomming);
                break;
            case TempInside:
                state = new DecimalType(vs.tempInside);
                break;
            case TempOutside:
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
