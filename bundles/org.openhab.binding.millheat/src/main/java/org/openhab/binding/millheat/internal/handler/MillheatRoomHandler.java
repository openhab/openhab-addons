/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.millheat.internal.handler;

import static org.openhab.binding.millheat.internal.MillheatBindingConstants.*;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.millheat.internal.config.MillheatRoomConfiguration;
import org.openhab.binding.millheat.internal.model.MillheatModel;
import org.openhab.binding.millheat.internal.model.ModeType;
import org.openhab.binding.millheat.internal.model.Room;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MillheatRoomHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Arne Seime - Initial contribution
 * @author Petter L. H. Eide - Cloud API identifiers and nullable measurements
 */
@NonNullByDefault
public class MillheatRoomHandler extends MillheatBaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(MillheatRoomHandler.class);
    private @NonNullByDefault({}) MillheatRoomConfiguration config;
    private String roomId = "";

    public MillheatRoomHandler(final Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        handleCommand(channelUID, command, getMillheatModel());
    }

    private void updateRoomTemperature(final Command command, final ModeType modeType) {
        getAccountHandler().ifPresent(handler -> handler.updateRoomTemperature(roomId, command, modeType));
    }

    private static State temperatureState(final @Nullable Double celsius) {
        return celsius == null ? UnDefType.UNDEF : new QuantityType<>(celsius, SIUnits.CELSIUS);
    }

    @Override
    protected void handleCommand(final ChannelUID channelUID, final Command command, final MillheatModel model) {
        final Optional<Room> optionalRoom = model.findRoomById(roomId);
        if (optionalRoom.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.GONE);
            return;
        }
        updateStatus(ThingStatus.ONLINE);
        final Room room = optionalRoom.get();
        final String channelId = channelUID.getId();
        final boolean refresh = command instanceof RefreshType;

        switch (channelId) {
            case CHANNEL_CURRENT_TEMPERATURE -> {
                if (refresh) {
                    updateState(channelUID, temperatureState(room.getCurrentTemp()));
                }
            }
            case CHANNEL_CURRENT_MODE -> {
                if (refresh) {
                    updateState(channelUID, new StringType(room.getMode().getApiValue()));
                }
            }
            case CHANNEL_PROGRAM -> {
                if (refresh) {
                    final String program = room.getRoomProgramName();
                    updateState(channelUID, program == null ? UnDefType.UNDEF : new StringType(program));
                }
            }
            case CHANNEL_COMFORT_TEMPERATURE -> {
                if (refresh) {
                    updateState(channelUID, temperatureState(room.getComfortTemp()));
                } else {
                    updateRoomTemperature(command, ModeType.COMFORT);
                }
            }
            case CHANNEL_SLEEP_TEMPERATURE -> {
                if (refresh) {
                    updateState(channelUID, temperatureState(room.getSleepTemp()));
                } else {
                    updateRoomTemperature(command, ModeType.SLEEP);
                }
            }
            case CHANNEL_AWAY_TEMPERATURE -> {
                if (refresh) {
                    updateState(channelUID, temperatureState(room.getAwayTemp()));
                } else {
                    updateRoomTemperature(command, ModeType.AWAY);
                }
            }
            case CHANNEL_TARGET_TEMPERATURE -> {
                if (refresh) {
                    updateState(channelUID, temperatureState(room.getTargetTemperature()));
                }
            }
            case CHANNEL_HEATING_ACTIVE -> {
                if (refresh) {
                    updateState(channelUID, OnOffType.from(room.isHeatingActive()));
                }
            }
            default ->
                logger.debug("Received command {} on channel {}, but this channel is not handled or supported by {}",
                        command, channelId, getThing().getUID());
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(MillheatRoomConfiguration.class);
        logger.debug("Initializing Mill room using config {}", config);
        final String configuredRoomId = config.roomId;
        if (configuredRoomId == null || configuredRoomId.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Room ID is required. Identifiers changed to UUIDs with the new Mill cloud API, so a numeric ID from an older configuration will not work; re-run discovery.");
            return;
        }
        roomId = configuredRoomId;
        updateStatus(getMillheatModel().findRoomById(roomId).isPresent() ? ThingStatus.ONLINE : ThingStatus.OFFLINE);
    }
}
