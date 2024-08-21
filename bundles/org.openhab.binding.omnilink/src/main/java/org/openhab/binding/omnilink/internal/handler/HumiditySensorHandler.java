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
package org.openhab.binding.omnilink.internal.handler;

import static org.openhab.binding.omnilink.internal.OmnilinkBindingConstants.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.measure.quantity.Dimensionless;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.omnilink.internal.TemperatureFormat;
import org.openhab.binding.omnilink.internal.discovery.ObjectPropertyRequest;
import org.openhab.binding.omnilink.internal.discovery.ObjectPropertyRequests;
import org.openhab.binding.omnilink.internal.exceptions.BridgeOfflineException;
import org.openhab.core.library.types.QuantityType;
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
import com.digitaldan.jomnilinkII.MessageTypes.properties.AuxSensorProperties;
import com.digitaldan.jomnilinkII.MessageTypes.statuses.ExtendedAuxSensorStatus;
import com.digitaldan.jomnilinkII.OmniInvalidResponseException;
import com.digitaldan.jomnilinkII.OmniUnknownMessageTypeException;

/**
 * The {@link HumiditySensorHandler} defines some methods that are used to
 * interface with an OmniLink Humidity Sensor. This by extension also defines
 * the Humidity Sensor thing that openHAB will be able to pick up and interface
 * with.
 *
 * @author Craig Hamilton - Initial contribution
 * @author Ethan Dye - openHAB3 rewrite
 */
@NonNullByDefault
public class HumiditySensorHandler extends AbstractOmnilinkStatusHandler<ExtendedAuxSensorStatus> {
    private final Logger logger = LoggerFactory.getLogger(HumiditySensorHandler.class);
    private final int thingID = getThingNumber();
    public @Nullable String number;

    public HumiditySensorHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();
        final OmnilinkBridgeHandler bridgeHandler = getOmnilinkBridgeHandler();
        if (bridgeHandler != null) {
            updateHumiditySensorProperties(bridgeHandler);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Received null bridge while initializing Humidity Sensor!");
        }
    }

    private void updateHumiditySensorProperties(OmnilinkBridgeHandler bridgeHandler) {
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

        if (command instanceof RefreshType) {
            retrieveStatus().ifPresentOrElse(this::updateChannels, () -> updateStatus(ThingStatus.OFFLINE,
                    ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "Received null status update!"));
            return;
        }

        if (!(command instanceof QuantityType)) {
            logger.debug("Invalid command: {}, must be QuantityType", command);
            return;
        }

        switch (channelUID.getId()) {
            case CHANNEL_AUX_LOW_SETPOINT:
                sendOmnilinkCommand(CommandMessage.CMD_THERMO_SET_HEAT_POINT,
                        TemperatureFormat.FAHRENHEIT.formatToOmni(((QuantityType<Dimensionless>) command).floatValue()),
                        thingID);
                break;
            case CHANNEL_AUX_HIGH_SETPOINT:
                sendOmnilinkCommand(CommandMessage.CMD_THERMO_SET_COOL_POINT,
                        TemperatureFormat.FAHRENHEIT.formatToOmni(((QuantityType<Dimensionless>) command).floatValue()),
                        thingID);
                break;
            default:
                logger.warn("Unknown channel for Humdity Sensor thing: {}", channelUID);
        }
    }

    @Override
    public void updateChannels(ExtendedAuxSensorStatus status) {
        logger.debug("updateChannels called for Humidity Sensor status: {}", status);
        updateState(CHANNEL_AUX_HUMIDITY,
                new QuantityType<>(TemperatureFormat.FAHRENHEIT.omniToFormat(status.getTemperature()), Units.PERCENT));
        updateState(CHANNEL_AUX_LOW_SETPOINT,
                new QuantityType<>(TemperatureFormat.FAHRENHEIT.omniToFormat(status.getHeatSetpoint()), Units.PERCENT));
        updateState(CHANNEL_AUX_HIGH_SETPOINT,
                new QuantityType<>(TemperatureFormat.FAHRENHEIT.omniToFormat(status.getCoolSetpoint()), Units.PERCENT));
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
                logger.debug("Received null bridge while updating Humidity Sensor status!");
                return Optional.empty();
            }
        } catch (OmniInvalidResponseException | OmniUnknownMessageTypeException | BridgeOfflineException e) {
            logger.debug("Received exception while refreshing Humidity Sensor status: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
