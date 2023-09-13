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
package org.openhab.binding.omnilink.internal.handler;

import static org.openhab.binding.omnilink.internal.OmnilinkBindingConstants.*;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.omnilink.internal.TemperatureFormat;
import org.openhab.binding.omnilink.internal.discovery.ObjectPropertyRequest;
import org.openhab.binding.omnilink.internal.discovery.ObjectPropertyRequests;
import org.openhab.binding.omnilink.internal.exceptions.BridgeOfflineException;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitaldan.jomnilinkII.Message;
import com.digitaldan.jomnilinkII.MessageTypes.CommandMessage;
import com.digitaldan.jomnilinkII.MessageTypes.ObjectStatus;
import com.digitaldan.jomnilinkII.MessageTypes.properties.AreaProperties;
import com.digitaldan.jomnilinkII.MessageTypes.properties.ThermostatProperties;
import com.digitaldan.jomnilinkII.MessageTypes.statuses.ExtendedThermostatStatus;
import com.digitaldan.jomnilinkII.OmniInvalidResponseException;
import com.digitaldan.jomnilinkII.OmniUnknownMessageTypeException;

/**
 * The {@link ThermostatHandler} defines some methods that are used to
 * interface with an OmniLink Thermostat. This by extension also defines the
 * Thermostat thing that openHAB will be able to pick up and interface with.
 *
 * @author Craig Hamilton - Initial contribution
 * @author Ethan Dye - openHAB3 rewrite
 */
@NonNullByDefault
public class ThermostatHandler extends AbstractOmnilinkStatusHandler<ExtendedThermostatStatus> {
    private final Logger logger = LoggerFactory.getLogger(ThermostatHandler.class);
    private final int thingID = getThingNumber();
    public @Nullable String number;

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

    public ThermostatHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();
        final OmnilinkBridgeHandler bridgeHandler = getOmnilinkBridgeHandler();
        if (bridgeHandler != null) {
            updateThermostatProperties(bridgeHandler);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Received null bridge while initializing Thermostat!");
        }
    }

    private void updateThermostatProperties(OmnilinkBridgeHandler bridgeHandler) {
        final List<AreaProperties> areas = getAreaProperties();
        if (areas != null) {
            for (AreaProperties areaProperties : areas) {
                int areaFilter = bitFilterForArea(areaProperties);

                ObjectPropertyRequest<ThermostatProperties> objectPropertyRequest = ObjectPropertyRequest
                        .builder(bridgeHandler, ObjectPropertyRequests.THERMOSTAT, thingID, 0).selectNamed()
                        .areaFilter(areaFilter).build();

                for (ThermostatProperties thermostatProperties : objectPropertyRequest) {
                    Map<String, String> properties = editProperties();
                    properties.put(THING_PROPERTIES_NAME, thermostatProperties.getName());
                    properties.put(THING_PROPERTIES_AREA, Integer.toString(areaProperties.getNumber()));
                    updateProperties(properties);
                }
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand called for channel: {}, command: {}", channelUID, command);
        final OmnilinkBridgeHandler bridgeHandler = getOmnilinkBridgeHandler();
        Optional<TemperatureFormat> temperatureFormat = Optional.empty();

        if (command instanceof RefreshType) {
            retrieveStatus().ifPresentOrElse(this::updateChannels, () -> updateStatus(ThingStatus.OFFLINE,
                    ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "Received null status update!"));
            return;
        }

        if (!(command instanceof DecimalType) && !(command instanceof QuantityType)) {
            logger.debug("Invalid command: {}, must be DecimalType or QuantityType", command);
            return;
        }
        if (bridgeHandler != null) {
            temperatureFormat = bridgeHandler.getTemperatureFormat();
            if (temperatureFormat.isEmpty()) {
                logger.warn("Receieved null temperature format!");
                return;
            }
        } else {
            logger.warn("Could not connect to Bridge, failed to get temperature format!");
            return;
        }

        switch (channelUID.getId()) {
            case CHANNEL_THERMO_SYSTEM_MODE:
                sendOmnilinkCommand(CommandMessage.CMD_THERMO_SET_SYSTEM_MODE, ((DecimalType) command).intValue(),
                        thingID);
                break;
            case CHANNEL_THERMO_FAN_MODE:
                sendOmnilinkCommand(CommandMessage.CMD_THERMO_SET_FAN_MODE, ((DecimalType) command).intValue(),
                        thingID);
                break;
            case CHANNEL_THERMO_HOLD_STATUS:
                sendOmnilinkCommand(CommandMessage.CMD_THERMO_SET_HOLD_MODE, ((DecimalType) command).intValue(),
                        thingID);
                break;
            case CHANNEL_THERMO_HEAT_SETPOINT:
                sendOmnilinkCommand(CommandMessage.CMD_THERMO_SET_HEAT_POINT,
                        temperatureFormat.get().formatToOmni(((QuantityType<Temperature>) command).floatValue()),
                        thingID);
                break;
            case CHANNEL_THERMO_COOL_SETPOINT:
                sendOmnilinkCommand(CommandMessage.CMD_THERMO_SET_COOL_POINT,
                        temperatureFormat.get().formatToOmni(((QuantityType<Temperature>) command).floatValue()),
                        thingID);
                break;
            case CHANNEL_THERMO_HUMIDIFY_SETPOINT:
                sendOmnilinkCommand(CommandMessage.CMD_THERMO_SET_HUMDIFY_POINT,
                        TemperatureFormat.FAHRENHEIT.formatToOmni(((QuantityType<Dimensionless>) command).floatValue()),
                        thingID);
                break;
            case CHANNEL_THERMO_DEHUMIDIFY_SETPOINT:
                sendOmnilinkCommand(CommandMessage.CMD_THERMO_SET_DEHUMIDIFY_POINT,
                        TemperatureFormat.FAHRENHEIT.formatToOmni(((QuantityType<Dimensionless>) command).floatValue()),
                        thingID);
                break;
            default:
                logger.warn("Unknown channel for Thermostat thing: {}", channelUID);
        }
    }

    @Override
    protected void updateChannels(ExtendedThermostatStatus status) {
        logger.debug("updateChannels called for Thermostat status: {}", status);
        final OmnilinkBridgeHandler bridgeHandler = getOmnilinkBridgeHandler();

        // Thermostat communication status
        BigInteger thermostatAlarms = BigInteger.valueOf(status.getStatus());
        updateState(CHANNEL_THERMO_COMM_FAILURE,
                thermostatAlarms.testBit(0) ? OpenClosedType.CLOSED : OpenClosedType.OPEN);
        updateState(CHANNEL_THERMO_FREEZE_ALARM,
                thermostatAlarms.testBit(1) ? OpenClosedType.CLOSED : OpenClosedType.OPEN);

        // Thermostat operation status
        BigInteger thermostatStatus = BigInteger.valueOf(status.getExtendedStatus());
        if (thermostatStatus.testBit(ThermostatStatus.HEATING.bit)) {
            updateState(CHANNEL_THERMO_STATUS, new DecimalType(ThermostatStatus.HEATING.modeValue));
        } else if (thermostatStatus.testBit(ThermostatStatus.COOLING.bit)) {
            updateState(CHANNEL_THERMO_STATUS, new DecimalType(ThermostatStatus.COOLING.modeValue));
        } else if (thermostatStatus.testBit(ThermostatStatus.HUMIDIFYING.bit)) {
            updateState(CHANNEL_THERMO_STATUS, new DecimalType(ThermostatStatus.HUMIDIFYING.modeValue));
        } else if (thermostatStatus.testBit(ThermostatStatus.DEHUMIDIFYING.bit)) {
            updateState(CHANNEL_THERMO_STATUS, new DecimalType(ThermostatStatus.DEHUMIDIFYING.modeValue));
        } else {
            updateState(CHANNEL_THERMO_STATUS, new DecimalType(0));
        }

        // Thermostat temperature status
        if (bridgeHandler != null) {
            Optional<TemperatureFormat> temperatureFormat = bridgeHandler.getTemperatureFormat();
            if (temperatureFormat.isPresent()) {
                updateState(CHANNEL_THERMO_CURRENT_TEMP, new QuantityType<>(
                        temperatureFormat.get().omniToFormat(status.getCurrentTemperature()),
                        temperatureFormat.get().getFormatNumber() == 1 ? ImperialUnits.FAHRENHEIT : SIUnits.CELSIUS));
                updateState(CHANNEL_THERMO_OUTDOOR_TEMP, new QuantityType<>(
                        temperatureFormat.get().omniToFormat(status.getOutdoorTemperature()),
                        temperatureFormat.get().getFormatNumber() == 1 ? ImperialUnits.FAHRENHEIT : SIUnits.CELSIUS));
                updateState(CHANNEL_THERMO_COOL_SETPOINT, new QuantityType<>(
                        temperatureFormat.get().omniToFormat(status.getCoolSetpoint()),
                        temperatureFormat.get().getFormatNumber() == 1 ? ImperialUnits.FAHRENHEIT : SIUnits.CELSIUS));
                updateState(CHANNEL_THERMO_HEAT_SETPOINT, new QuantityType<>(
                        temperatureFormat.get().omniToFormat(status.getHeatSetpoint()),
                        temperatureFormat.get().getFormatNumber() == 1 ? ImperialUnits.FAHRENHEIT : SIUnits.CELSIUS));
            } else {
                logger.warn("Receieved null temperature format, could not update Thermostat channels!");
            }
        } else {
            logger.warn("Could not connect to Bridge, failed to get temperature format!");
            return;
        }

        // Thermostat humidity status
        updateState(CHANNEL_THERMO_HUMIDITY, new QuantityType<>(
                TemperatureFormat.FAHRENHEIT.omniToFormat(status.getCurrentHumidity()), Units.PERCENT));
        updateState(CHANNEL_THERMO_HUMIDIFY_SETPOINT, new QuantityType<>(
                TemperatureFormat.FAHRENHEIT.omniToFormat(status.getHumidifySetpoint()), Units.PERCENT));
        updateState(CHANNEL_THERMO_DEHUMIDIFY_SETPOINT, new QuantityType<>(
                TemperatureFormat.FAHRENHEIT.omniToFormat(status.getDehumidifySetpoint()), Units.PERCENT));

        // Thermostat mode, fan, and hold status
        updateState(CHANNEL_THERMO_SYSTEM_MODE, new DecimalType(status.getSystemMode()));
        updateState(CHANNEL_THERMO_FAN_MODE, new DecimalType(status.getFanMode()));
        updateState(CHANNEL_THERMO_HOLD_STATUS,
                new DecimalType(status.getHoldStatus() > 2 ? 1 : status.getHoldStatus()));
    }

    @Override
    protected Optional<ExtendedThermostatStatus> retrieveStatus() {
        try {
            final OmnilinkBridgeHandler bridgeHandler = getOmnilinkBridgeHandler();
            if (bridgeHandler != null) {
                ObjectStatus objStatus = bridgeHandler.requestObjectStatus(Message.OBJ_TYPE_THERMO, thingID, thingID,
                        true);
                return Optional.of((ExtendedThermostatStatus) objStatus.getStatuses()[0]);
            } else {
                logger.debug("Received null bridge while updating Thermostat status!");
                return Optional.empty();
            }
        } catch (OmniInvalidResponseException | OmniUnknownMessageTypeException | BridgeOfflineException e) {
            logger.debug("Received exception while refreshing Thermostat status: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
