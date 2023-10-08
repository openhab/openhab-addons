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
package org.openhab.binding.warmup.internal.handler;

import static org.openhab.binding.warmup.internal.WarmupBindingConstants.*;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.warmup.internal.api.MyWarmupApiException;
import org.openhab.binding.warmup.internal.model.query.LocationDTO;
import org.openhab.binding.warmup.internal.model.query.QueryResponseDTO;
import org.openhab.binding.warmup.internal.model.query.RoomDTO;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author James Melville - Initial contribution
 */
@NonNullByDefault
public class RoomHandler extends WarmupThingHandler implements WarmupRefreshListener {

    private final Logger logger = LoggerFactory.getLogger(RoomHandler.class);
    private @Nullable RoomConfigurationDTO config;

    public RoomHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();
        config = getConfigAs(RoomConfigurationDTO.class);
        if (config.getSerialNumber().length() == 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Serial Number not configured");
        } else {
            super.refreshFromServer();
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);
        if (CHANNEL_TARGET_TEMPERATURE.equals(channelUID.getId())
                && command instanceof QuantityType<?> quantityCommand) {
            setOverride(quantityCommand);
        }
        if (CHANNEL_FROST_PROTECTION_MODE.equals(channelUID.getId()) && command instanceof OnOffType onOffCommand) {
            toggleFrostProtectionMode(onOffCommand);
        }
    }

    /**
     * Process device list and populate room properties, status and state
     *
     * @param domain Data model representing all devices
     */
    @Override
    public void refresh(@Nullable QueryResponseDTO domain) {
        if (domain == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "No data from bridge");
        } else if (config != null) {
            final String serialNumber = config.getSerialNumber();
            for (LocationDTO location : domain.getData().getUser().getLocations()) {
                for (RoomDTO room : location.getRooms()) {
                    if (room.getThermostat4ies() != null && !room.getThermostat4ies().isEmpty()
                            && room.getThermostat4ies().get(0).getDeviceSN().equals(serialNumber)) {
                        if (room.getThermostat4ies().get(0).getLastPoll() > 10) {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                    "Thermostat has not polled for 10 minutes");
                        } else {
                            updateStatus(ThingStatus.ONLINE);

                            updateProperty(PROPERTY_ROOM_ID, room.getId());
                            updateProperty(PROPERTY_ROOM_NAME, room.getName());
                            updateProperty(PROPERTY_LOCATION_ID, location.getId());
                            updateProperty(PROPERTY_LOCATION_NAME, location.getName());

                            updateState(CHANNEL_CURRENT_TEMPERATURE, parseTemperature(room.getCurrentTemperature()));
                            updateState(CHANNEL_TARGET_TEMPERATURE, parseTemperature(room.getTargetTemperature()));
                            updateState(CHANNEL_OVERRIDE_DURATION, parseDuration(room.getOverrideDuration()));
                            updateState(CHANNEL_RUN_MODE, parseString(room.getRunMode()));
                            updateState(CHANNEL_FROST_PROTECTION_MODE,
                                    OnOffType.from(room.getRunMode().equals(FROST_PROTECTION_MODE)));
                        }
                        return;
                    }
                }
            }
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Room not found");
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Room not configured");
        }
    }

    private void setOverride(final QuantityType<?> command) {
        String roomId = getThing().getProperties().get(PROPERTY_ROOM_ID);
        String locationId = getThing().getProperties().get(PROPERTY_LOCATION_ID);

        QuantityType<?> temp = command.toUnit(SIUnits.CELSIUS);

        if (temp != null) {
            final int value = temp.multiply(BigDecimal.TEN).intValue();

            try {
                final MyWarmupAccountHandler bridgeHandler = getBridgeHandler();
                if (bridgeHandler != null && config != null) {
                    final int overrideDuration = config.getOverrideDuration();
                    if (overrideDuration > 0 && locationId != null && roomId != null) {
                        bridgeHandler.getApi().setOverride(locationId, roomId, value, overrideDuration);
                        refreshFromServer();
                    }
                }
            } catch (MyWarmupApiException e) {
                logger.debug("Set Override failed: {}", e.getMessage());
            }
        }
    }

    private void toggleFrostProtectionMode(OnOffType command) {
        String roomId = getThing().getProperties().get(PROPERTY_ROOM_ID);
        String locationId = getThing().getProperties().get(PROPERTY_LOCATION_ID);
        try {
            final MyWarmupAccountHandler bridgeHandler = getBridgeHandler();
            if (bridgeHandler != null && locationId != null && roomId != null) {
                bridgeHandler.getApi().toggleFrostProtectionMode(locationId, roomId, command);
                refreshFromServer();
            }
        } catch (MyWarmupApiException e) {
            logger.debug("Toggle Frost Protection failed: {}", e.getMessage());
        }
    }
}
