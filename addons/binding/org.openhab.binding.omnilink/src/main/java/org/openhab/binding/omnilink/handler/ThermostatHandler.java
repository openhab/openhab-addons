/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.omnilink.handler;

import java.math.BigInteger;
import java.util.Optional;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.omnilink.OmnilinkBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitaldan.jomnilinkII.Message;
import com.digitaldan.jomnilinkII.OmniInvalidResponseException;
import com.digitaldan.jomnilinkII.OmniUnknownMessageTypeException;
import com.digitaldan.jomnilinkII.MessageTypes.ObjectStatus;
import com.digitaldan.jomnilinkII.MessageTypes.statuses.ExtendedThermostatStatus;

/**
 *
 * @author Craig Hamilton
 *
 */
public class ThermostatHandler extends AbstractOmnilinkStatusHandler<ExtendedThermostatStatus> {

    private enum ThermostatStatus {
        HEATING(0, 1),
        COOLING(1, 2),
        HUMIDIFYING(2, 3),
        DEHUMIDIFYING(3, 4);

        private final int bit;
        private final int modeValue;

        private ThermostatStatus(int bit, int modeValue) {
            this.bit = bit;
            this.modeValue = modeValue;
        }
    }

    private Logger logger = LoggerFactory.getLogger(ThermostatHandler.class);

    public ThermostatHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Thermostat Command Received.  ChannelUID({}) Command({})", channelUID, command);

        int thermostatID = getThingNumber();
        String channelID = channelUID.getId();

        switch (channelID) {
            case OmnilinkBindingConstants.CHANNEL_THERMO_SYSTEM_MODE: {
                int mode = ((DecimalType) command).intValue();
                sendOmnilinkCommand(OmniLinkCmd.CMD_THERMO_SET_SYSTEM_MODE.getNumber(), mode, thermostatID);
            }
                break;
            case OmnilinkBindingConstants.CHANNEL_THERMO_FAN_MODE: {
                int mode = ((DecimalType) command).intValue();
                sendOmnilinkCommand(OmniLinkCmd.CMD_THERMO_SET_FAN_MODE.getNumber(), mode, thermostatID);
            }
                break;
            case OmnilinkBindingConstants.CHANNEL_THERMO_HOLD_MODE: {
                int mode = ((DecimalType) command).intValue();
                sendOmnilinkCommand(OmniLinkCmd.CMD_THERMO_SET_HOLD_MODE.getNumber(), mode, thermostatID);
            }
                break;
            case OmnilinkBindingConstants.CHANNEL_THERMO_HEAT_SETPOINT: {
                TemperatureFormat temperatureFormat = getOmnilinkBridgeHandler().getTemperatureFormat();
                int setpoint = ((DecimalType) command).intValue();
                setpoint = temperatureFormat.formatToOmni(setpoint);
                sendOmnilinkCommand(OmniLinkCmd.CMD_THERMO_SET_HEAT_POINT.getNumber(), setpoint, thermostatID);
            }
                break;
            case OmnilinkBindingConstants.CHANNEL_THERMO_COOL_SETPOINT: {
                TemperatureFormat temperatureFormat = getOmnilinkBridgeHandler().getTemperatureFormat();
                int setpoint = ((DecimalType) command).intValue();
                setpoint = temperatureFormat.formatToOmni(setpoint);
                sendOmnilinkCommand(OmniLinkCmd.CMD_THERMO_SET_COOL_POINT.getNumber(), setpoint, thermostatID);
            }
                break;
            case OmnilinkBindingConstants.CHANNEL_THERMO_HUMIDIFY_SETPOINT: {
                int setpoint = ((DecimalType) command).intValue();
                // Humdity is stored using fahrenheit
                setpoint = TemperatureFormat.FAHRENHEIT.formatToOmni(setpoint);
                sendOmnilinkCommand(OmniLinkCmd.CMD_THERMO_SET_HUMDIFY_POINT.getNumber(), setpoint, thermostatID);
            }
                break;
            case OmnilinkBindingConstants.CHANNEL_THERMO_DEHUMIDIFY_SETPOINT: {
                int setpoint = ((DecimalType) command).intValue();
                // Humdity is stored using fahrenheit
                setpoint = TemperatureFormat.FAHRENHEIT.formatToOmni(setpoint);
                sendOmnilinkCommand(OmniLinkCmd.CMD_THERMO_SET_DEHUMIDIFY_POINT.getNumber(), setpoint, thermostatID);
            }
                break;
            default:
                logger.warn("Channel ID ({}) not processed", channelID);
                break;

        }

    }

    @Override
    protected void updateChannels(ExtendedThermostatStatus thermostatStatus) {
        logger.debug("Thermostat Status {}", thermostatStatus);
        handleThermostatAlarms(thermostatStatus);
        handleThermostatRunStatus(thermostatStatus);
        handleTemperatureStatus(thermostatStatus);
        handleHumidityStatus(thermostatStatus);
        handleSystemModeStatus(thermostatStatus);
        handleFanStatus(thermostatStatus);
        handleHoldStatus(thermostatStatus);
    }

    private void handleFanStatus(ExtendedThermostatStatus status) {
        /*
         * The fan mode is as follows:
         * 0 Auto
         * 1 On
         * 2 Cycle
         */
        updateState(OmnilinkBindingConstants.CHANNEL_THERMO_FAN_MODE, new DecimalType(status.getFan()));
    }

    private void handleSystemModeStatus(ExtendedThermostatStatus status) {
        /*
         * The system mode is as follows:
         * 0 Off
         * 1 Heat
         * 2 Cool
         * 3 Auto
         * 4 Emergency heat
         */
        updateState(OmnilinkBindingConstants.CHANNEL_THERMO_SYSTEM_MODE, new DecimalType(status.getMode()));
    }

    private void handleHumidityStatus(ExtendedThermostatStatus status) {
        // Humidity is reported in the Omni temperature format where Fahrenheit temperatures 0-100 correspond to 0-100%
        // relative humidity
        updateState(OmnilinkBindingConstants.CHANNEL_THERMO_HUMIDITY,
                new DecimalType(TemperatureFormat.FAHRENHEIT.omniToFormat(status.getHumidity())));
        updateState(OmnilinkBindingConstants.CHANNEL_THERMO_HUMIDIFY_SETPOINT,
                new DecimalType(TemperatureFormat.FAHRENHEIT.omniToFormat(status.getHumiditySetpoint())));
        updateState(OmnilinkBindingConstants.CHANNEL_THERMO_DEHUMIDIFY_SETPOINT,
                new DecimalType(TemperatureFormat.FAHRENHEIT.omniToFormat(status.getDehumidifySetpoint())));
    }

    private void handleTemperatureStatus(ExtendedThermostatStatus status) {
        TemperatureFormat temperatureFormat = getOmnilinkBridgeHandler().getTemperatureFormat();

        updateState(OmnilinkBindingConstants.CHANNEL_THERMO_TEMP,
                new DecimalType(temperatureFormat.omniToFormat(status.getTemperature())));
        updateState(OmnilinkBindingConstants.CHANNEL_THERMO_OUTDOOR_TEMP,
                new DecimalType(temperatureFormat.omniToFormat(status.getOutdoorTemp())));
        updateState(OmnilinkBindingConstants.CHANNEL_THERMO_COOL_SETPOINT,
                new DecimalType(temperatureFormat.omniToFormat(status.getCoolSetpoint())));
        updateState(OmnilinkBindingConstants.CHANNEL_THERMO_HEAT_SETPOINT,
                new DecimalType(temperatureFormat.omniToFormat(status.getHeatSetpoint())));
    }

    private void handleHoldStatus(ExtendedThermostatStatus status) {
        /*
         * The hold status is as follows:
         * 0 Off
         * 1 Hold
         * 2 Vacation Hold
         * Other Hold
         */
        int holdStatus = status.getHold();
        if (holdStatus > 2) {
            holdStatus = 1;
        }
        updateState(OmnilinkBindingConstants.CHANNEL_THERMO_HOLD_MODE, new DecimalType(holdStatus));
    }

    private void handleThermostatRunStatus(ExtendedThermostatStatus status) {
        /*
         * The bits in the heating/cooling/humidifying/dehumidifying status byte are shown below. The corresponding bit
         * is set if the
         * thermostat is currently performing that action.
         * Bit 0 Heating
         * Bit 1 Cooling
         * Bit 2 Humidifying
         * Bit 3 Dehumidifying
         */

        BigInteger thermostatStatus = BigInteger.valueOf(status.getExtendedStatus());
        if (thermostatStatus.testBit(ThermostatStatus.HEATING.bit)) {
            updateState(OmnilinkBindingConstants.CHANNEL_THERMO_STATUS,
                    new DecimalType(ThermostatStatus.HEATING.modeValue));
        } else if (thermostatStatus.testBit(ThermostatStatus.COOLING.bit)) {
            updateState(OmnilinkBindingConstants.CHANNEL_THERMO_STATUS,
                    new DecimalType(ThermostatStatus.COOLING.modeValue));
        } else if (thermostatStatus.testBit(ThermostatStatus.HUMIDIFYING.bit)) {
            updateState(OmnilinkBindingConstants.CHANNEL_THERMO_STATUS,
                    new DecimalType(ThermostatStatus.HUMIDIFYING.modeValue));
        } else if (thermostatStatus.testBit(ThermostatStatus.DEHUMIDIFYING.bit)) {
            updateState(OmnilinkBindingConstants.CHANNEL_THERMO_STATUS,
                    new DecimalType(ThermostatStatus.DEHUMIDIFYING.modeValue));
        } else {
            updateState(OmnilinkBindingConstants.CHANNEL_THERMO_STATUS, new DecimalType(0));
        }
    }

    private void handleThermostatAlarms(ExtendedThermostatStatus status) {
        BigInteger thermostatAlarms = BigInteger.valueOf(status.getStatus());

        // Communications Failure is bit 0
        State communicationsFailure = thermostatAlarms.testBit(0) ? OpenClosedType.CLOSED : OpenClosedType.OPEN;

        // Freeze Alarm is bit 1
        State freezeAlarm = thermostatAlarms.testBit(1) ? OpenClosedType.CLOSED : OpenClosedType.OPEN;

        updateState(OmnilinkBindingConstants.CHANNEL_THERMO_COMM_FAILURE, communicationsFailure);
        updateState(OmnilinkBindingConstants.CHANNEL_THERMO_FREEZE_ALARM, freezeAlarm);
    }

    @Override
    protected Optional<ExtendedThermostatStatus> retrieveStatus() {
        try {
            int thermostatID = getThingNumber();
            logger.debug("Requesting status for thermostat ID: {}", thermostatID);
            ObjectStatus objStatus = getOmnilinkBridgeHandler().requestObjectStatus(Message.OBJ_TYPE_THERMO,
                    thermostatID, thermostatID, true);
            return Optional.of((ExtendedThermostatStatus) objStatus.getStatuses()[0]);
        } catch (OmniInvalidResponseException | OmniUnknownMessageTypeException | BridgeOfflineException e) {
            logger.debug("Unexpected exception refreshing unit:", e);
            return Optional.empty();
        }
    }

}
