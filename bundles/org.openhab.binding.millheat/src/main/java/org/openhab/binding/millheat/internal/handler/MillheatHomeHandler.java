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

import java.time.Instant;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.millheat.internal.config.MillheatHomeConfiguration;
import org.openhab.binding.millheat.internal.model.Home;
import org.openhab.binding.millheat.internal.model.MillheatModel;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
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
 * The {@link MillheatHomeHandler} is responsible for handling home commands, for now vacation mode properties
 *
 * @author Arne Seime - Initial contribution
 * @author Petter L. H. Eide - Cloud API vacation endpoints
 */
@NonNullByDefault
public class MillheatHomeHandler extends MillheatBaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(MillheatHomeHandler.class);
    private @NonNullByDefault({}) MillheatHomeConfiguration config;
    /** Validated in initialize(), so the rest of the handler can rely on it being present. */
    private String homeId = "";

    public MillheatHomeHandler(final Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        handleCommand(channelUID, command, getMillheatModel());
    }

    private static State instantState(final @Nullable Instant instant) {
        return instant == null ? UnDefType.UNDEF : new DateTimeType(instant);
    }

    @Override
    protected void handleCommand(final ChannelUID channelUID, final Command command, final MillheatModel model) {
        final Optional<Home> optionalHome = model.findHomeById(homeId);
        if (optionalHome.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.GONE);
            return;
        }
        updateStatus(ThingStatus.ONLINE);
        final Home home = optionalHome.get();
        final String channelId = channelUID.getId();
        final boolean refresh = command instanceof RefreshType;

        switch (channelId) {
            case CHANNEL_HOME_VACATION_TARGET_TEMPERATURE -> {
                if (refresh) {
                    final Double temperature = home.getVacationTemperature();
                    updateState(channelUID,
                            temperature == null ? UnDefType.UNDEF : new QuantityType<>(temperature, SIUnits.CELSIUS));
                } else if (command instanceof QuantityType<?>) {
                    updateVacationModeProperty(home, MillheatAccountHandler.VACATION_PROP_TEMP, command);
                } else if (command instanceof DecimalType decimalCommand) {
                    updateVacationModeProperty(home, MillheatAccountHandler.VACATION_PROP_TEMP,
                            new QuantityType<>(decimalCommand, SIUnits.CELSIUS));
                }
            }
            case CHANNEL_HOME_VACATION_MODE -> {
                if (refresh) {
                    updateState(channelUID, OnOffType.from(home.isVacationModeActive()));
                } else if (command instanceof OnOffType) {
                    updateVacationModeProperty(home, MillheatAccountHandler.VACATION_PROP_MODE, command);
                }
            }
            case CHANNEL_HOME_VACATION_MODE_ADVANCED -> {
                if (refresh) {
                    updateState(channelUID, OnOffType.from(home.isAdvancedVacationMode()));
                } else if (command instanceof OnOffType) {
                    updateVacationModeProperty(home, MillheatAccountHandler.VACATION_PROP_ADVANCED, command);
                }
            }
            case CHANNEL_HOME_VACATION_MODE_START -> {
                if (refresh) {
                    updateState(channelUID, instantState(home.getVacationModeStart()));
                } else if (command instanceof DateTimeType) {
                    updateVacationModeProperty(home, MillheatAccountHandler.VACATION_PROP_START, command);
                }
            }
            case CHANNEL_HOME_VACATION_MODE_END -> {
                if (refresh) {
                    updateState(channelUID, instantState(home.getVacationModeEnd()));
                } else if (command instanceof DateTimeType) {
                    updateVacationModeProperty(home, MillheatAccountHandler.VACATION_PROP_END, command);
                }
            }
            default ->
                logger.debug("Received command {} on channel {}, but this channel is not handled or supported by {}",
                        command, channelId, getThing().getUID());
        }
    }

    private void updateVacationModeProperty(final Home home, final String property, final Command command) {
        getAccountHandler().ifPresent(handler -> handler.updateVacationProperty(home, property, command));
    }

    @Override
    public void initialize() {
        config = getConfigAs(MillheatHomeConfiguration.class);
        logger.debug("Initializing Mill home using config {}", config);
        final String configuredHomeId = config.homeId;
        if (configuredHomeId == null || configuredHomeId.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Home ID is required. Identifiers changed to UUIDs with the new Mill cloud API, so a numeric ID from an older configuration will not work; re-run discovery.");
            return;
        }
        homeId = configuredHomeId;
        updateStatus(getMillheatModel().findHomeById(homeId).isPresent() ? ThingStatus.ONLINE : ThingStatus.OFFLINE);
    }
}
