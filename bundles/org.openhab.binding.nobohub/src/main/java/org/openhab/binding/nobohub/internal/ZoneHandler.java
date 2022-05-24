/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.nobohub.internal;

import static org.openhab.binding.nobohub.internal.NoboHubBindingConstants.CHANNEL_ZONE_ACTIVE_WEEK_PROFILE;
import static org.openhab.binding.nobohub.internal.NoboHubBindingConstants.CHANNEL_ZONE_ACTIVE_WEEK_PROFILE_NAME;
import static org.openhab.binding.nobohub.internal.NoboHubBindingConstants.CHANNEL_ZONE_CALCULATED_WEEK_PROFILE_STATUS;
import static org.openhab.binding.nobohub.internal.NoboHubBindingConstants.CHANNEL_ZONE_COMFORT_TEMPERATURE;
import static org.openhab.binding.nobohub.internal.NoboHubBindingConstants.CHANNEL_ZONE_CURRENT_TEMPERATURE;
import static org.openhab.binding.nobohub.internal.NoboHubBindingConstants.CHANNEL_ZONE_ECO_TEMPERATURE;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nobohub.internal.model.NoboDataException;
import org.openhab.binding.nobohub.internal.model.WeekProfile;
import org.openhab.binding.nobohub.internal.model.WeekProfileStatus;
import org.openhab.binding.nobohub.internal.model.Zone;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.StateOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shows information about a named Zone in the Nobø Hub.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class ZoneHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(ZoneHandler.class);

    private final WeekProfileStateDescriptionOptionsProvider weekProfileStateDescriptionOptionsProvider;

    protected @Nullable Integer id;

    public ZoneHandler(Thing thing,
            WeekProfileStateDescriptionOptionsProvider weekProfileStateDescriptionOptionsProvider) {
        super(thing);
        this.weekProfileStateDescriptionOptionsProvider = weekProfileStateDescriptionOptionsProvider;
    }

    public void onUpdate(Zone zone) {
        logger.debug("Updating zone: {}", zone.getName());
        updateStatus(ThingStatus.ONLINE);

        DecimalType comfortTemperature = new DecimalType(zone.getComfortTemperature());
        updateState(CHANNEL_ZONE_COMFORT_TEMPERATURE, comfortTemperature);
        DecimalType ecoTemperature = new DecimalType(zone.getEcoTemperature());
        updateState(CHANNEL_ZONE_ECO_TEMPERATURE, ecoTemperature);

        Double temp = zone.getTemperature();
        if (temp != null && temp != Double.NaN) {
            try {
                DecimalType currentTemperature = new DecimalType(temp);
                updateState(CHANNEL_ZONE_CURRENT_TEMPERATURE, currentTemperature);
            } catch (NumberFormatException nfe) {
                logger.debug("Could not set decimal value to temperature: {}", temp);
            }
        }

        int activeWeekProfileId = zone.getActiveWeekProfileId();
        Bridge noboHub = getBridge();
        if (null != noboHub) {
            logger.debug("Updating zone: {} at hub brige: {}", zone.getName(),
                    noboHub.getStatusInfo().getStatus().name());
            NoboHubBridgeHandler hubHandler = (NoboHubBridgeHandler) noboHub.getHandler();
            if (hubHandler != null) {
                WeekProfile weekProfile = hubHandler.getWeekProfile(activeWeekProfileId);
                if (null != weekProfile) {
                    updateState(CHANNEL_ZONE_ACTIVE_WEEK_PROFILE_NAME, StringType.valueOf(weekProfile.getName()));
                    updateState(CHANNEL_ZONE_ACTIVE_WEEK_PROFILE,
                            DecimalType.valueOf(String.valueOf(weekProfile.getId())));
                    try {
                        WeekProfileStatus weekProfileStatus = weekProfile.getStatusAt(LocalDateTime.now());
                        updateState(CHANNEL_ZONE_CALCULATED_WEEK_PROFILE_STATUS,
                                StringType.valueOf(weekProfileStatus.name()));
                    } catch (NoboDataException nde) {
                        logger.error("Failed getting current week profile status", nde);
                    }
                }

                List<StateOption> options = new ArrayList<>();
                logger.debug("Updating week profile state description options for zone {}.", zone.getName());
                for (WeekProfile wp : hubHandler.getWeekProfiles()) {
                    options.add(new StateOption(String.valueOf(wp.getId()), wp.getName()));
                }
                logger.debug("State options {}.", options.size() + " first: " + options.get(0));
                weekProfileStateDescriptionOptionsProvider.setStateOptions(
                        new ChannelUID(getThing().getUID(), CHANNEL_ZONE_ACTIVE_WEEK_PROFILE), options);
            }
        }

        updateProperty("name", zone.getName());
        updateProperty("id", Integer.toString(zone.getId()));
    }

    @Override
    public void initialize() {
        this.id = getConfigAs(ZoneConfiguration.class).id;
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            logger.debug("Refreshing channel {}", channelUID);

            if (null != id) {
                Zone zone = getZone();
                if (null == zone) {
                    logger.error("Could not find Zone with id {} for channel {}", id, channelUID);
                    updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.GONE);
                } else {
                    onUpdate(zone);
                    Bridge noboHub = getBridge();
                    if (null != noboHub) {
                        NoboHubBridgeHandler hubHandler = (NoboHubBridgeHandler) noboHub.getHandler();
                        if (null != hubHandler) {
                            WeekProfile weekProfile = hubHandler.getWeekProfile(zone.getActiveWeekProfileId());
                            if (null != weekProfile) {
                                String weekProfileName = weekProfile.getName();
                                StringType weekProfileValue = StringType.valueOf(weekProfileName);
                                updateState(CHANNEL_ZONE_ACTIVE_WEEK_PROFILE_NAME, weekProfileValue);
                            }
                        }
                    }
                }
            } else {
                updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.GONE);
                logger.error("id not set for channel {}", channelUID);
            }

            return;
        }

        if (CHANNEL_ZONE_COMFORT_TEMPERATURE.equals(channelUID.getId())) {
            Zone zone = getZone();
            if (zone != null) {
                if (command instanceof DecimalType) {
                    DecimalType comfortTemp = (DecimalType) command;
                    logger.debug("Set comfort temp for zone {} to {}", zone.getName(), comfortTemp.doubleValue());
                    zone.setComfortTemperature(comfortTemp.intValue());
                    sendCommand(zone.generateCommandString("U00"));
                }
            }

            return;
        }

        if (CHANNEL_ZONE_ECO_TEMPERATURE.equals(channelUID.getId())) {
            Zone zone = getZone();
            if (zone != null) {
                if (command instanceof DecimalType) {
                    DecimalType ecoTemp = (DecimalType) command;
                    logger.debug("Set eco temp for zone {} to {}", zone.getName(), ecoTemp.doubleValue());
                    zone.setEcoTemperature(ecoTemp.intValue());
                    sendCommand(zone.generateCommandString("U00"));
                }
            }
            return;
        }

        if (CHANNEL_ZONE_ACTIVE_WEEK_PROFILE.equals(channelUID.getId())) {
            Zone zone = getZone();
            if (zone != null) {
                if (command instanceof DecimalType) {
                    DecimalType weekProfileId = (DecimalType) command;
                    logger.debug("Set week profile for zone {} to {}", zone.getName(), weekProfileId);
                    zone.setWeekProfile(weekProfileId.intValue());
                    sendCommand(zone.generateCommandString("U00"));
                }
            }

            return;
        }

        logger.debug("Unhandled zone command {}: {}", channelUID.getId(), command);
    }

    public @Nullable Integer getZoneId() {
        return id;
    }

    private void sendCommand(String command) {
        Bridge noboHub = getBridge();
        if (null != noboHub) {
            NoboHubBridgeHandler hubHandler = (NoboHubBridgeHandler) noboHub.getHandler();
            if (null != hubHandler) {
                hubHandler.sendCommand(command);
            }
        }
    }

    private @Nullable Zone getZone() {
        Bridge noboHub = getBridge();
        if (null != noboHub) {
            NoboHubBridgeHandler hubHandler = (NoboHubBridgeHandler) noboHub.getHandler();
            if (null != hubHandler && null != id) {
                Integer zid = Helpers.castToNonNull(id, "id");
                return hubHandler.getZone(zid);
            }
        }

        return null;
    }
}
