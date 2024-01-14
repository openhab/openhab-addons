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
package org.openhab.binding.nobohub.internal;

import static org.openhab.binding.nobohub.internal.NoboHubBindingConstants.CHANNEL_ZONE_ACTIVE_WEEK_PROFILE;
import static org.openhab.binding.nobohub.internal.NoboHubBindingConstants.CHANNEL_ZONE_ACTIVE_WEEK_PROFILE_NAME;
import static org.openhab.binding.nobohub.internal.NoboHubBindingConstants.CHANNEL_ZONE_CALCULATED_WEEK_PROFILE_STATUS;
import static org.openhab.binding.nobohub.internal.NoboHubBindingConstants.CHANNEL_ZONE_COMFORT_TEMPERATURE;
import static org.openhab.binding.nobohub.internal.NoboHubBindingConstants.CHANNEL_ZONE_CURRENT_TEMPERATURE;
import static org.openhab.binding.nobohub.internal.NoboHubBindingConstants.CHANNEL_ZONE_ECO_TEMPERATURE;
import static org.openhab.binding.nobohub.internal.NoboHubBindingConstants.PROPERTY_HOSTNAME;
import static org.openhab.binding.nobohub.internal.NoboHubBindingConstants.PROPERTY_ZONE_ID;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nobohub.internal.model.NoboDataException;
import org.openhab.binding.nobohub.internal.model.WeekProfile;
import org.openhab.binding.nobohub.internal.model.WeekProfileStatus;
import org.openhab.binding.nobohub.internal.model.Zone;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
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

    private final NoboHubTranslationProvider messages;

    protected int id;

    public ZoneHandler(Thing thing, NoboHubTranslationProvider messages,
            WeekProfileStateDescriptionOptionsProvider weekProfileStateDescriptionOptionsProvider) {
        super(thing);
        this.messages = messages;
        this.weekProfileStateDescriptionOptionsProvider = weekProfileStateDescriptionOptionsProvider;
    }

    public void onUpdate(Zone zone) {
        logger.debug("Updating zone: {}", zone.getName());
        updateStatus(ThingStatus.ONLINE);

        QuantityType<Temperature> comfortTemperature = new QuantityType<>(zone.getComfortTemperature(),
                SIUnits.CELSIUS);
        updateState(CHANNEL_ZONE_COMFORT_TEMPERATURE, comfortTemperature);
        QuantityType<Temperature> ecoTemperature = new QuantityType<>(zone.getEcoTemperature(), SIUnits.CELSIUS);
        updateState(CHANNEL_ZONE_ECO_TEMPERATURE, ecoTemperature);

        Double temp = zone.getTemperature();
        if (temp != null && !Double.isNaN(temp)) {
            QuantityType<Temperature> currentTemperature = new QuantityType<>(temp, SIUnits.CELSIUS);
            updateState(CHANNEL_ZONE_CURRENT_TEMPERATURE, currentTemperature);
        }

        int activeWeekProfileId = zone.getActiveWeekProfileId();
        Bridge noboHub = getBridge();
        if (null != noboHub) {
            logger.debug("Updating zone: {} at hub bridge: {}", zone.getName(),
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
                        logger.debug("Failed getting current week profile status", nde);
                    }
                }

                List<StateOption> options = new ArrayList<>();
                logger.debug("Updating week profile state description options for zone {}.", zone.getName());
                for (WeekProfile wp : hubHandler.getWeekProfiles()) {
                    options.add(new StateOption(String.valueOf(wp.getId()), wp.getName()));
                }
                logger.debug("State options count: {}. First: {}", options.size(),
                        (!options.isEmpty()) ? options.get(0) : 0);
                weekProfileStateDescriptionOptionsProvider.setStateOptions(
                        new ChannelUID(getThing().getUID(), CHANNEL_ZONE_ACTIVE_WEEK_PROFILE), options);
            }
        }

        Map<String, String> properties = editProperties();
        properties.put(PROPERTY_HOSTNAME, zone.getName());
        properties.put(PROPERTY_ZONE_ID, Integer.toString(zone.getId()));
        updateProperties(properties);
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

            Zone zone = getZone();
            if (null == zone) {
                logger.debug("Could not find Zone with id {} for channel {}", id, channelUID);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.GONE,
                        messages.getText("message.zone.notfound", id, channelUID));
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

            return;
        }

        if (CHANNEL_ZONE_COMFORT_TEMPERATURE.equals(channelUID.getId())) {
            Zone zone = getZone();
            if (zone != null) {
                if (command instanceof DecimalType comfortTemp) {
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
                if (command instanceof DecimalType ecoTemp) {
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
                if (command instanceof DecimalType weekProfileId) {
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
            if (null != hubHandler) {
                return hubHandler.getZone(id);
            }
        }

        return null;
    }
}
