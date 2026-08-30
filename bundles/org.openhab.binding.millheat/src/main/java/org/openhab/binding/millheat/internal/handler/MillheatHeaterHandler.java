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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.millheat.internal.MillheatBindingConstants;
import org.openhab.binding.millheat.internal.config.MillheatHeaterConfiguration;
import org.openhab.binding.millheat.internal.model.Heater;
import org.openhab.binding.millheat.internal.model.MillheatModel;
import org.openhab.binding.millheat.internal.model.Room;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MillheatHeaterHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Arne Seime - Initial contribution
 * @author Petter L. H. Eide - Cloud API identifiers and measured power
 */
@NonNullByDefault
public class MillheatHeaterHandler extends MillheatBaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(MillheatHeaterHandler.class);
    private @NonNullByDefault({}) MillheatHeaterConfiguration config;

    public MillheatHeaterHandler(final Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        handleCommand(channelUID, command, getMillheatModel());
    }

    private static State temperatureState(final @Nullable Double celsius) {
        return celsius == null ? UnDefType.UNDEF : new QuantityType<>(celsius, SIUnits.CELSIUS);
    }

    @Override
    protected void handleCommand(final ChannelUID channelUID, final Command command, final MillheatModel model) {
        final Optional<Heater> optionalHeater = model.findHeaterByMacOrId(config.macAddress, config.heaterId);
        if (optionalHeater.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.GONE);
            return;
        }
        final Heater heater = optionalHeater.get();
        // The cloud reports whether it has heard from the device recently. The last known
        // measurements are still published, since they remain the best available reading, but the
        // thing status marks them as potentially stale.
        updateStatus(heater.isOnline() ? ThingStatus.ONLINE : ThingStatus.OFFLINE,
                heater.isOnline() ? ThingStatusDetail.NONE : ThingStatusDetail.COMMUNICATION_ERROR);

        final String channelId = channelUID.getId();
        final boolean refresh = command instanceof RefreshType;

        switch (channelId) {
            case MillheatBindingConstants.CHANNEL_CURRENT_TEMPERATURE -> {
                if (refresh) {
                    updateState(channelUID, temperatureState(heater.getCurrentTemp()));
                }
            }
            case MillheatBindingConstants.CHANNEL_HEATING_ACTIVE -> {
                if (refresh) {
                    updateState(channelUID, OnOffType.from(heater.isHeatingActive()));
                }
            }
            case MillheatBindingConstants.CHANNEL_FAN_ACTIVE -> {
                if (refresh) {
                    updateState(channelUID, OnOffType.from(heater.fanActive()));
                } else if (heater.canChangeTemp() && heater.isIndependent()) {
                    updateHeaterProperties(null, null, command);
                } else {
                    logger.debug("Heater {} follows a room program and cannot be controlled directly",
                            getThing().getUID());
                }
            }
            case MillheatBindingConstants.CHANNEL_WINDOW_STATE -> {
                if (refresh) {
                    updateState(channelUID, heater.windowOpen() ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
                }
            }
            case MillheatBindingConstants.CHANNEL_INDEPENDENT -> {
                if (refresh) {
                    updateState(channelUID, OnOffType.from(heater.isIndependent()));
                }
            }
            case MillheatBindingConstants.CHANNEL_CURRENT_POWER -> {
                if (refresh) {
                    // The cloud API measures this, so the nominal power configuration parameter
                    // that the old service required is no longer consulted.
                    final Double power = heater.getCurrentPower();
                    updateState(channelUID, power == null ? UnDefType.UNDEF : new QuantityType<>(power, Units.WATT));
                }
            }
            case MillheatBindingConstants.CHANNEL_TARGET_TEMPERATURE -> {
                if (refresh) {
                    final Room room = heater.getRoom();
                    updateState(channelUID,
                            temperatureState(heater.getTargetTemp() != null || room == null ? heater.getTargetTemp()
                                    : room.getTargetTemperature()));
                } else if (heater.canChangeTemp()) {
                    updateHeaterProperties(command, null, null);
                } else {
                    logger.debug("Heater {} follows a room program; set the room setpoint instead",
                            getThing().getUID());
                }
            }
            case MillheatBindingConstants.CHANNEL_MASTER_SWITCH -> {
                if (refresh) {
                    updateState(channelUID, OnOffType.from(heater.powerStatus()));
                } else if (heater.canChangeTemp() && heater.isIndependent()) {
                    updateHeaterProperties(null, command, null);
                } else {
                    // Reassert the known state so the item does not appear to have changed.
                    updateState(channelUID, OnOffType.from(heater.powerStatus()));
                }
            }
            default ->
                logger.debug("Received command {} on channel {}, but this channel is not handled or supported by {}",
                        command, channelId, getThing().getUID());
        }
    }

    private void updateHeaterProperties(final @Nullable Command temperatureCommand,
            final @Nullable Command masterOnOffCommand, final @Nullable Command fanCommand) {
        getAccountHandler().ifPresent(handler -> handler.updateIndependentHeaterProperties(config.macAddress,
                config.heaterId, temperatureCommand, masterOnOffCommand, fanCommand));
    }

    @Override
    public void initialize() {
        config = getConfigAs(MillheatHeaterConfiguration.class);
        logger.debug("Initializing Mill heater using config {}", config);
        final String configuredHeaterId = config.heaterId;
        final String configuredMac = config.macAddress;
        final boolean hasId = configuredHeaterId != null && !configuredHeaterId.isBlank();
        final boolean hasMac = configuredMac != null && !configuredMac.isBlank();
        if (!hasId && !hasMac) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Either MAC address or heater ID is required. Heater IDs changed to UUIDs with the new Mill cloud API, so a numeric ID from an older configuration will not work; re-run discovery.");
            return;
        }
        final Optional<Heater> heater = getMillheatModel().findHeaterByMacOrId(config.macAddress, config.heaterId);
        if (heater.isPresent()) {
            addOptionalChannels(heater.get());
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    /** Heaters outside a room are controlled directly, so they gain a switch and a setpoint. */
    private void addOptionalChannels(final Heater heater) {
        if (!heater.canChangeTemp() || !heater.isIndependent()) {
            return;
        }
        final List<Channel> newChannels = new ArrayList<>(getThing().getChannels());
        newChannels.add(ChannelBuilder
                .create(new ChannelUID(getThing().getUID(), MillheatBindingConstants.CHANNEL_MASTER_SWITCH), "Switch")
                .withType(MillheatBindingConstants.CHANNEL_TYPE_MASTER_SWITCH_UID).build());
        newChannels.add(ChannelBuilder
                .create(new ChannelUID(getThing().getUID(), MillheatBindingConstants.CHANNEL_TARGET_TEMPERATURE),
                        "Number:Temperature")
                .withType(MillheatBindingConstants.CHANNEL_TYPE_TARGET_TEMPERATURE_HEATER_UID).build());
        updateThing(editThing().withChannels(newChannels).build());
    }
}
