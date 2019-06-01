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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.millheat.internal.MillheatBindingConstants;
import org.openhab.binding.millheat.internal.config.MillheatHeaterConfiguration;
import org.openhab.binding.millheat.internal.model.Heater;
import org.openhab.binding.millheat.internal.model.MillheatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MillheatHeaterHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Arne Seime - Initial contribution
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

    @Override
    protected void handleCommand(final ChannelUID channelUID, @NonNull final Command command,
            @NonNull final MillheatModel model) {
        final Optional<Heater> optionalHeater = model.findHeaterByMacOrId(config.macAddress, config.heaterId);
        if (optionalHeater.isPresent()) {
            updateStatus(ThingStatus.ONLINE);
            final Heater heater = optionalHeater.get();
            if (MillheatBindingConstants.CHANNEL_CURRENT_TEMPERATURE.equals(channelUID.getId())) {
                if (command instanceof RefreshType) {
                    updateState(channelUID, new QuantityType<>(heater.getCurrentTemp(), SIUnits.CELSIUS));
                }
            } else if (MillheatBindingConstants.CHANNEL_HEATING_ACTIVE.equals(channelUID.getId())) {
                if (command instanceof RefreshType) {
                    updateState(channelUID, heater.isHeatingActive() ? OnOffType.ON : OnOffType.OFF);
                }
            } else if (MillheatBindingConstants.CHANNEL_FAN_ACTIVE.equals(channelUID.getId())) {
                if (command instanceof RefreshType) {
                    updateState(channelUID, heater.isFanActive() ? OnOffType.ON : OnOffType.OFF);
                } else if (heater.isCanChangeTemp() && heater.getRoom() == null) {
                    updateIndependentHeaterProperties(null, null, command);
                } else {
                    logger.debug("Heater {} cannot change temperature and is in a room", getThing().getUID());
                }
            } else if (MillheatBindingConstants.CHANNEL_WINDOW_STATE.equals(channelUID.getId())) {
                if (command instanceof RefreshType) {
                    updateState(channelUID, heater.isWindowOpen() ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
                }
            } else if (MillheatBindingConstants.CHANNEL_INDEPENDENT.equals(channelUID.getId())) {
                if (command instanceof RefreshType) {
                    updateState(channelUID, heater.getRoom() == null ? OnOffType.ON : OnOffType.OFF);
                }
            } else if (MillheatBindingConstants.CHANNEL_CURRENT_POWER.equals(channelUID.getId())) {
                if (command instanceof RefreshType) {
                    if (config.power != null) {
                        if (heater.isHeatingActive()) {
                            updateState(channelUID, new QuantityType<>(config.power, SmartHomeUnits.WATT));
                        } else {
                            updateState(channelUID, new QuantityType<>(0, SmartHomeUnits.WATT));
                        }
                    } else {
                        updateState(channelUID, UnDefType.UNDEF);
                        logger.debug(
                                "Cannot update power for heater as the nominal power has not been configured for thing {}",
                                getThing().getUID());
                    }
                }
            } else if (MillheatBindingConstants.CHANNEL_TARGET_TEMPERATURE.equals(channelUID.getId())) {
                if (command instanceof RefreshType) {
                    if (heater.isCanChangeTemp() && heater.getTargetTemp() != null) {
                        updateState(channelUID, new QuantityType<>(heater.getTargetTemp(), SIUnits.CELSIUS));
                    } else if (heater.getRoom() != null) {
                        final Integer targetTemperature = heater.getRoom().getTargetTemperature();
                        if (targetTemperature != null) {
                            updateState(channelUID, new QuantityType<>(targetTemperature, SIUnits.CELSIUS));
                        } else {
                            updateState(channelUID, UnDefType.UNDEF);
                        }
                    } else {
                        logger.info(
                                "Heater {} is neither connected to a room or marked as standalone. Someting is wrong, heater data: {}",
                                getThing().getUID(), heater);
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
                    }
                } else {
                    if (heater.isCanChangeTemp() && heater.getRoom() == null) {
                        updateIndependentHeaterProperties(command, null, null);
                    }
                }
            } else if (MillheatBindingConstants.CHANNEL_MASTER_SWITCH.equals(channelUID.getId())) {
                if (command instanceof RefreshType) {
                    updateState(channelUID, heater.isPowerStatus() ? OnOffType.ON : OnOffType.OFF);
                } else {
                    if (heater.isCanChangeTemp() && heater.getRoom() == null) {
                        updateIndependentHeaterProperties(null, command, null);
                    } else {
                        // Just overwrite with old state
                        updateState(channelUID, heater.isPowerStatus() ? OnOffType.ON : OnOffType.OFF);
                    }
                }
            } else {
                logger.debug("Received command {} on channel {}, but this channel is not handled or supported by {}",
                        channelUID.getId(), command.toString(), this.getThing().getUID());
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.GONE);
        }
    }

    private void updateIndependentHeaterProperties(@Nullable final Command temperatureCommand,
            @Nullable final Command masterOnOffCommand, @Nullable final Command fanCommand) {
        final MillheatAccountHandler accountHandler = getAccountHandler();
        if (accountHandler != null) {
            accountHandler.updateIndependentHeaterProperties(config.macAddress, config.heaterId, temperatureCommand,
                    masterOnOffCommand, fanCommand);
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(MillheatHeaterConfiguration.class);
        logger.debug("Initializing Millheat heater using config {}", config);
        if (config.heaterId == null && config.macAddress == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        } else {
            final Optional<Heater> heater = getMillheatModel().findHeaterByMacOrId(config.macAddress, config.heaterId);
            if (heater.isPresent()) {
                addOptionalChannels(heater.get());
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
        }
    }

    private void addOptionalChannels(final Heater heater) {
        final List<Channel> newChannels = new ArrayList<>();
        newChannels.addAll(getThing().getChannels());
        if (heater.isCanChangeTemp() && heater.getRoom() == null) {
            // Add power switch channel
            newChannels
                    .add(ChannelBuilder
                            .create(new ChannelUID(getThing().getUID(), MillheatBindingConstants.CHANNEL_MASTER_SWITCH),
                                    "Switch")
                            .withType(MillheatBindingConstants.CHANNEL_TYPE_MASTER_SWITCH_UID).build());
            // Add independent heater target temperature
            newChannels.add(ChannelBuilder
                    .create(new ChannelUID(getThing().getUID(), MillheatBindingConstants.CHANNEL_TARGET_TEMPERATURE),
                            "Number:Temperature")
                    .withType(MillheatBindingConstants.CHANNEL_TYPE_TARGET_TEMPERATURE_HEATER_UID).build());
        }

        updateThing(editThing().withChannels(newChannels).build());
    }
}
