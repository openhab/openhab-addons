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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.omnilink.internal.TemperatureFormat;
import org.openhab.binding.omnilink.internal.discovery.ObjectPropertyRequest;
import org.openhab.binding.omnilink.internal.discovery.ObjectPropertyRequests;
import org.openhab.binding.omnilink.internal.exceptions.BridgeOfflineException;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
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
import com.digitaldan.jomnilinkII.MessageTypes.properties.AuxSensorProperties;
import com.digitaldan.jomnilinkII.MessageTypes.statuses.ExtendedAuxSensorStatus;
import com.digitaldan.jomnilinkII.OmniInvalidResponseException;
import com.digitaldan.jomnilinkII.OmniUnknownMessageTypeException;

/**
 * The {@link TempSensorHandler} defines some methods that are used to interface
 * with an OmniLink Temperature Sensor. This by extension also defines the
 * Temperature Sensor thing that openHAB will be able to pick up and interface
 * with.
 *
 * @author Craig Hamilton - Initial contribution
 * @author Ethan Dye - openHAB3 rewrite
 */
@NonNullByDefault
public class TempSensorHandler extends AbstractOmnilinkStatusHandler<ExtendedAuxSensorStatus> {
    private final Logger logger = LoggerFactory.getLogger(TempSensorHandler.class);
    private final int thingID = getThingNumber();
    public @Nullable String number;

    public TempSensorHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();
        final OmnilinkBridgeHandler bridgeHandler = getOmnilinkBridgeHandler();
        if (bridgeHandler != null) {
            updateTempSensorProperties(bridgeHandler);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Received null bridge while initializing Temperature Sensor!");
        }
    }

    private void updateTempSensorProperties(OmnilinkBridgeHandler bridgeHandler) {
        final List<AreaProperties> areas = getAreaProperties();
        if (areas != null) {
            for (AreaProperties areaProperties : areas) {
                int areaFilter = bitFilterForArea(areaProperties);

                ObjectPropertyRequest<AuxSensorProperties> objectPropertyRequest = ObjectPropertyRequest
                        .builder(bridgeHandler, ObjectPropertyRequests.AUX_SENSORS, thingID, 0).selectNamed()
                        .areaFilter(areaFilter).build();

                for (AuxSensorProperties auxSensorProperties : objectPropertyRequest) {
                    Map<String, String> properties = editProperties();
                    properties.put(THING_PROPERTIES_NAME, auxSensorProperties.getName());
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

        if (!(command instanceof QuantityType)) {
            logger.debug("Invalid command: {}, must be QuantityType", command);
            return;
        }
        if (bridgeHandler != null) {
            temperatureFormat = bridgeHandler.getTemperatureFormat();
            if (!temperatureFormat.isPresent()) {
                logger.warn("Receieved null temperature format!");
                return;
            }
        } else {
            logger.warn("Could not connect to Bridge, failed to get temperature format!");
            return;
        }

        switch (channelUID.getId()) {
            case CHANNEL_AUX_LOW_SETPOINT:
                sendOmnilinkCommand(CommandMessage.CMD_THERMO_SET_HEAT_POINT,
                        temperatureFormat.get().formatToOmni(((QuantityType<Temperature>) command).floatValue()),
                        thingID);
                break;
            case CHANNEL_AUX_HIGH_SETPOINT:
                sendOmnilinkCommand(CommandMessage.CMD_THERMO_SET_COOL_POINT,
                        temperatureFormat.get().formatToOmni(((QuantityType<Temperature>) command).floatValue()),
                        thingID);
                break;
            default:
                logger.warn("Unknown channel for Temperature Sensor thing: {}", channelUID);
        }
    }

    @Override
    public void updateChannels(ExtendedAuxSensorStatus status) {
        final OmnilinkBridgeHandler bridgeHandler = getOmnilinkBridgeHandler();
        if (bridgeHandler != null) {
            Optional<TemperatureFormat> temperatureFormat = bridgeHandler.getTemperatureFormat();
            if (temperatureFormat.isPresent()) {
                updateState(CHANNEL_AUX_TEMP, new QuantityType<>(
                        temperatureFormat.get().omniToFormat(status.getTemperature()),
                        temperatureFormat.get().getFormatNumber() == 1 ? ImperialUnits.FAHRENHEIT : SIUnits.CELSIUS));
                updateState(CHANNEL_AUX_LOW_SETPOINT, new QuantityType<>(
                        temperatureFormat.get().omniToFormat(status.getCoolSetpoint()),
                        temperatureFormat.get().getFormatNumber() == 1 ? ImperialUnits.FAHRENHEIT : SIUnits.CELSIUS));
                updateState(CHANNEL_AUX_HIGH_SETPOINT, new QuantityType<>(
                        temperatureFormat.get().omniToFormat(status.getHeatSetpoint()),
                        temperatureFormat.get().getFormatNumber() == 1 ? ImperialUnits.FAHRENHEIT : SIUnits.CELSIUS));
            } else {
                logger.warn("Receieved null temperature format, could not update Temperature Sensor channels!");
            }
        } else {
            logger.debug("Received null bridge while updating Temperature Sensor channels!");
        }
    }

    @Override
    protected Optional<ExtendedAuxSensorStatus> retrieveStatus() {
        try {
            final OmnilinkBridgeHandler bridgeHandler = getOmnilinkBridgeHandler();
            if (bridgeHandler != null) {
                ObjectStatus objStatus = bridgeHandler.requestObjectStatus(Message.OBJ_TYPE_AUX_SENSOR, thingID,
                        thingID, true);
                return Optional.of((ExtendedAuxSensorStatus) objStatus.getStatuses()[0]);
            } else {
                logger.debug("Received null bridge while updating Temperature Sensor status!");
                return Optional.empty();
            }
        } catch (OmniInvalidResponseException | OmniUnknownMessageTypeException | BridgeOfflineException e) {
            logger.debug("Received exception while refreshing Temperature Sensor status: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
