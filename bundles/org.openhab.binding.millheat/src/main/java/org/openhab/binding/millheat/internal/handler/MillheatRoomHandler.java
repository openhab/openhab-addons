/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.millheat.internal.config.MillheatRoomConfiguration;
import org.openhab.binding.millheat.internal.model.MillheatModel;
import org.openhab.binding.millheat.internal.model.ModeType;
import org.openhab.binding.millheat.internal.model.Room;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tec.uom.se.unit.Units;

/**
 * The {@link MillheatRoomHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Arne Seime - Initial contribution
 */
@NonNullByDefault
public class MillheatRoomHandler extends MillheatBaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(MillheatRoomHandler.class);
    private @NonNullByDefault({}) MillheatRoomConfiguration config;

    public MillheatRoomHandler(final Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        handleCommand(channelUID, command, getMillheatModel());
    }

    private void updateRoomTemperature(final Long roomId, final Command command, final ModeType modeType) {
        final MillheatAccountHandler accountHandler = getAccountHandler();
        if (accountHandler != null) {
            accountHandler.updateRoomTemperature(config.roomId, command, modeType);
        }
    }

    @Override
    protected void handleCommand(@NonNull final ChannelUID channelUID, @NonNull final Command command,
            @NonNull final MillheatModel model) {
        final Optional<Room> optionalRoom = model.findRoomById(config.roomId);
        if (optionalRoom.isPresent()) {
            updateStatus(ThingStatus.ONLINE);
            final Room room = optionalRoom.get();
            if (CHANNEL_CURRENT_TEMPERATURE.equals(channelUID.getId())) {
                if (command instanceof RefreshType) {
                    updateState(channelUID, new QuantityType<>(room.getCurrentTemp(), Units.CELSIUS));
                }
            } else if (CHANNEL_CURRENT_MODE.equals(channelUID.getId())) {
                if (command instanceof RefreshType) {
                    updateState(channelUID, new StringType(room.getMode().toString()));
                }
            } else if (CHANNEL_PROGRAM.equals(channelUID.getId())) {
                if (command instanceof RefreshType) {
                    updateState(channelUID, new StringType(room.getRoomProgramName()));
                }
            } else if (CHANNEL_COMFORT_TEMPERATURE.equals(channelUID.getId())) {
                if (command instanceof RefreshType) {
                    updateState(channelUID, new QuantityType<>(room.getComfortTemp(), Units.CELSIUS));
                } else {
                    updateRoomTemperature(config.roomId, command, ModeType.COMFORT);
                }
            } else if (CHANNEL_SLEEP_TEMPERATURE.equals(channelUID.getId())) {
                if (command instanceof RefreshType) {
                    updateState(channelUID, new QuantityType<>(room.getSleepTemp(), Units.CELSIUS));
                } else {
                    updateRoomTemperature(config.roomId, command, ModeType.SLEEP);
                }
            } else if (CHANNEL_AWAY_TEMPERATURE.equals(channelUID.getId())) {
                if (command instanceof RefreshType) {
                    updateState(channelUID, new QuantityType<>(room.getAwayTemp(), Units.CELSIUS));
                } else {
                    updateRoomTemperature(config.roomId, command, ModeType.AWAY);
                }
            } else if (CHANNEL_TARGET_TEMPERATURE.equals(channelUID.getId())) {
                if (command instanceof RefreshType) {
                    final Integer targetTemperature = room.getTargetTemperature();
                    if (targetTemperature != null) {
                        updateState(channelUID, new QuantityType<>(targetTemperature, Units.CELSIUS));
                    } else {
                        updateState(channelUID, UnDefType.UNDEF);
                    }
                }
            } else if (CHANNEL_HEATING_ACTIVE.equals(channelUID.getId())) {
                if (command instanceof RefreshType) {
                    updateState(channelUID, room.isHeatingActive() ? OnOffType.ON : OnOffType.OFF);
                }
            } else {
                logger.debug("Received command {} on channel {}, but this channel is not handled or supported by {}",
                        channelUID.getId(), command.toString(), this.getThing().getUID());
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.GONE);
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(MillheatRoomConfiguration.class);
        logger.debug("Initializing Millheat heater using config {}", config);
        final Optional<Room> room = getMillheatModel().findRoomById(config.roomId);
        if (room.isPresent()) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE);
        }
    }
}
