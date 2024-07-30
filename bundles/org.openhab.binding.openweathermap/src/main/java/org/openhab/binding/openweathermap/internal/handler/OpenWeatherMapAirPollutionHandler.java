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
package org.openhab.binding.openweathermap.internal.handler;

import static org.openhab.binding.openweathermap.internal.OpenWeatherMapBindingConstants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.openweathermap.internal.config.OpenWeatherMapAirPollutionConfiguration;
import org.openhab.binding.openweathermap.internal.connection.OpenWeatherMapConnection;
import org.openhab.binding.openweathermap.internal.dto.OpenWeatherMapJsonAirPollutionData;
import org.openhab.core.i18n.CommunicationException;
import org.openhab.core.i18n.ConfigurationException;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

/**
 * The {@link OpenWeatherMapAirPollutionHandler} is responsible for handling commands, which are sent to one of the
 * channels.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class OpenWeatherMapAirPollutionHandler extends AbstractOpenWeatherMapHandler {

    private final Logger logger = LoggerFactory.getLogger(OpenWeatherMapAirPollutionHandler.class);

    private static final String CHANNEL_GROUP_HOURLY_FORECAST_PREFIX = "forecastHours";
    private static final Pattern CHANNEL_GROUP_HOURLY_FORECAST_PREFIX_PATTERN = Pattern
            .compile(CHANNEL_GROUP_HOURLY_FORECAST_PREFIX + "([0-9]*)");

    // keeps track of the parsed count
    private int forecastHours = 0;

    private @Nullable OpenWeatherMapJsonAirPollutionData airPollutionData;
    private @Nullable OpenWeatherMapJsonAirPollutionData airPollutionForecastData;

    public OpenWeatherMapAirPollutionHandler(Thing thing, final TimeZoneProvider timeZoneProvider) {
        super(thing, timeZoneProvider);
    }

    @Override
    public void initialize() {
        super.initialize();
        logger.debug("Initialize OpenWeatherMapAirPollutionHandler handler '{}'.", getThing().getUID());
        OpenWeatherMapAirPollutionConfiguration config = getConfigAs(OpenWeatherMapAirPollutionConfiguration.class);

        boolean configValid = true;
        int newForecastHours = config.forecastHours;
        if (newForecastHours < 0 || newForecastHours > 120) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-not-supported-air-pollution-number-of-hours");
            configValid = false;
        }

        if (configValid) {
            logger.debug("Rebuilding thing '{}'.", getThing().getUID());
            List<Channel> toBeAddedChannels = new ArrayList<>();
            List<Channel> toBeRemovedChannels = new ArrayList<>();
            if (forecastHours != newForecastHours) {
                logger.debug("Rebuilding air pollution channel groups.");
                if (forecastHours > newForecastHours) {
                    for (int i = newForecastHours + 1; i <= forecastHours; i++) {
                        toBeRemovedChannels.addAll(removeChannelsOfGroup(
                                CHANNEL_GROUP_HOURLY_FORECAST_PREFIX + ((i < 10) ? "0" : "") + Integer.toString(i)));
                    }
                } else {
                    for (int i = forecastHours + 1; i <= newForecastHours; i++) {
                        toBeAddedChannels.addAll(createChannelsForGroup(
                                CHANNEL_GROUP_HOURLY_FORECAST_PREFIX + ((i < 10) ? "0" : "") + Integer.toString(i),
                                CHANNEL_GROUP_TYPE_AIR_POLLUTION_FORECAST));
                    }
                }
                forecastHours = newForecastHours;
            }
            ThingBuilder builder = editThing().withoutChannels(toBeRemovedChannels);
            for (Channel channel : toBeAddedChannels) {
                builder.withChannel(channel);
            }
            updateThing(builder.build());
        }
    }

    @Override
    protected boolean requestData(OpenWeatherMapConnection connection)
            throws CommunicationException, ConfigurationException {
        logger.debug("Update air pollution data of thing '{}'.", getThing().getUID());
        try {
            airPollutionData = connection.getAirPollutionData(location);
            if (forecastHours > 0) {
                airPollutionForecastData = connection.getAirPollutionForecastData(location);
            }
            return true;
        } catch (JsonSyntaxException e) {
            logger.debug("JsonSyntaxException occurred during execution: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    protected void updateChannel(ChannelUID channelUID) {
        String channelGroupId = channelUID.getGroupId();
        if (channelGroupId == null) {
            logger.debug("Cannot update {} as it has no GroupId", channelUID);
            return;
        }
        switch (channelGroupId) {
            case CHANNEL_GROUP_CURRENT_AIR_POLLUTION:
                updateCurrentAirPollutionChannel(channelUID);
                break;
            default:
                Matcher m = CHANNEL_GROUP_HOURLY_FORECAST_PREFIX_PATTERN.matcher(channelUID.getGroupId());
                int i;
                if (m.find() && (i = Integer.parseInt(m.group(1))) > 0 && i <= 120) {
                    updateHourlyForecastChannel(channelUID, i);
                }
                break;
        }
    }

    /**
     * Update the channel from the last OpenWeatherMap data retrieved.
     *
     * @param channelUID the id identifying the channel to be updated
     */
    private void updateCurrentAirPollutionChannel(ChannelUID channelUID) {
        String channelId = channelUID.getIdWithoutGroup();
        String channelGroupId = channelUID.getGroupId();
        OpenWeatherMapJsonAirPollutionData localAirPollutionData = airPollutionData;
        if (localAirPollutionData != null && !localAirPollutionData.list.isEmpty()) {
            org.openhab.binding.openweathermap.internal.dto.airpollution.List currentData = localAirPollutionData.list
                    .get(0);
            State state = UnDefType.UNDEF;
            switch (channelId) {
                case CHANNEL_TIME_STAMP:
                    state = getDateTimeTypeState(currentData.dt);
                    break;
                case CHANNEL_AIR_QUALITY_INDEX:
                    state = new DecimalType(currentData.airQualityIndex.index);
                    break;
                case CHANNEL_PARTICULATE_MATTER_2_5:
                    state = getQuantityTypeState(currentData.measurements.particulateMatter2dot5,
                            Units.MICROGRAM_PER_CUBICMETRE);
                    break;
                case CHANNEL_PARTICULATE_MATTER_10:
                    state = getQuantityTypeState(currentData.measurements.particulateMatter10,
                            Units.MICROGRAM_PER_CUBICMETRE);
                    break;
                case CHANNEL_CARBON_MONOXIDE:
                    state = getQuantityTypeState(currentData.measurements.carbonMonoxide,
                            Units.MICROGRAM_PER_CUBICMETRE);
                    break;
                case CHANNEL_NITROGEN_MONOXIDE:
                    state = getQuantityTypeState(currentData.measurements.nitrogenMonoxide,
                            Units.MICROGRAM_PER_CUBICMETRE);
                    break;
                case CHANNEL_NITROGEN_DIOXIDE:
                    state = getQuantityTypeState(currentData.measurements.nitrogenDioxide,
                            Units.MICROGRAM_PER_CUBICMETRE);
                    break;
                case CHANNEL_OZONE:
                    state = getQuantityTypeState(currentData.measurements.ozone, Units.MICROGRAM_PER_CUBICMETRE);
                    break;
                case CHANNEL_SULPHUR_DIOXIDE:
                    state = getQuantityTypeState(currentData.measurements.sulphurDioxide,
                            Units.MICROGRAM_PER_CUBICMETRE);
                    break;
                case CHANNEL_AMMONIA:
                    state = getQuantityTypeState(currentData.measurements.ammonia, Units.MICROGRAM_PER_CUBICMETRE);
                    break;
            }
            logger.debug("Update channel '{}' of group '{}' with new state '{}'.", channelId, channelGroupId, state);
            updateState(channelUID, state);
        } else {
            logger.debug("No air pollution data available to update channel '{}' of group '{}'.", channelId,
                    channelGroupId);
        }
    }

    /**
     * Update the channel from the last OpenWeatherMap data retrieved.
     *
     * @param channelUID the id identifying the channel to be updated
     * @param count
     */
    private void updateHourlyForecastChannel(ChannelUID channelUID, int count) {
        String channelId = channelUID.getIdWithoutGroup();
        String channelGroupId = channelUID.getGroupId();
        OpenWeatherMapJsonAirPollutionData localAirPollutionForecastData = airPollutionForecastData;
        if (localAirPollutionForecastData != null && localAirPollutionForecastData.list.size() >= count) {
            org.openhab.binding.openweathermap.internal.dto.airpollution.List forecastData = localAirPollutionForecastData.list
                    .get(count - 1);
            State state = UnDefType.UNDEF;
            switch (channelId) {
                case CHANNEL_TIME_STAMP:
                    state = getDateTimeTypeState(forecastData.dt);
                    break;
                case CHANNEL_AIR_QUALITY_INDEX:
                    state = new DecimalType(forecastData.airQualityIndex.index);
                    break;
                case CHANNEL_PARTICULATE_MATTER_2_5:
                    state = getQuantityTypeState(forecastData.measurements.particulateMatter2dot5,
                            Units.MICROGRAM_PER_CUBICMETRE);
                    break;
                case CHANNEL_PARTICULATE_MATTER_10:
                    state = getQuantityTypeState(forecastData.measurements.particulateMatter10,
                            Units.MICROGRAM_PER_CUBICMETRE);
                    break;
                case CHANNEL_CARBON_MONOXIDE:
                    state = getQuantityTypeState(forecastData.measurements.carbonMonoxide,
                            Units.MICROGRAM_PER_CUBICMETRE);
                    break;
                case CHANNEL_NITROGEN_MONOXIDE:
                    state = getQuantityTypeState(forecastData.measurements.nitrogenMonoxide,
                            Units.MICROGRAM_PER_CUBICMETRE);
                    break;
                case CHANNEL_NITROGEN_DIOXIDE:
                    state = getQuantityTypeState(forecastData.measurements.nitrogenDioxide,
                            Units.MICROGRAM_PER_CUBICMETRE);
                    break;
                case CHANNEL_OZONE:
                    state = getQuantityTypeState(forecastData.measurements.ozone, Units.MICROGRAM_PER_CUBICMETRE);
                    break;
                case CHANNEL_SULPHUR_DIOXIDE:
                    state = getQuantityTypeState(forecastData.measurements.sulphurDioxide,
                            Units.MICROGRAM_PER_CUBICMETRE);
                    break;
                case CHANNEL_AMMONIA:
                    state = getQuantityTypeState(forecastData.measurements.ammonia, Units.MICROGRAM_PER_CUBICMETRE);
                    break;
            }
            logger.debug("Update channel '{}' of group '{}' with new state '{}'.", channelId, channelGroupId, state);
            updateState(channelUID, state);
        } else {
            logger.debug("No air pollution data available to update channel '{}' of group '{}'.", channelId,
                    channelGroupId);
        }
    }
}
