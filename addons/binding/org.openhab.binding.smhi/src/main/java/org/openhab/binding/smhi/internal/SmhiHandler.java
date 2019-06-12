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
package org.openhab.binding.smhi.internal;

import static org.openhab.binding.smhi.internal.SmhiBindingConstants.*;

import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.PointType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.smhi.internal.model.SmhiData;
import org.openhab.binding.smhi.internal.model.SmhiParameterTables;
import org.openhab.binding.smhi.internal.model.SmhiParameters;
import org.openhab.binding.smhi.internal.model.SmhiTimeSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The {@link smhiHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Michael Parment - Initial contribution
 */
@NonNullByDefault
public class SmhiHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SmhiHandler.class);
    private @Nullable ScheduledFuture<?> refreshJob;

    protected static final String smhiURL = "http://opendata-download-metfcst.smhi.se/api/category/pmp3g/version/2/geotype/point/lon/%s/lat/%s/data.json";

    protected @Nullable PointType location;

    // Timeout for weather data requests.
    private static final int SMHI_TIMEOUT = 5000;

    private Gson gson = new GsonBuilder().create();

    private String apiRequest = "";
    private String groupId = "";

    @Nullable
    private SmhiConfiguration config;

    public SmhiHandler(Thing thing) {
        super(thing);
    }

    private SmhiParameterTables parameterTable = new SmhiParameterTables();

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            queryApiAndUpdateChannels();
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing SMHI weather handler.");

        config = getConfigAs(SmhiConfiguration.class);

        if (StringUtils.trimToNull(config.location) == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "The 'location' parameter must be configured.");
            return;
        }

        try {
            location = new PointType(config.location);
        } catch (IllegalArgumentException e) {
            location = null;
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "The 'location' parameter could not be split into latitude and longitude.");
            return;
        }

        DecimalFormat df = new DecimalFormat("##.######");
        DecimalFormatSymbols custom = new DecimalFormatSymbols();
        custom.setDecimalSeparator('.');
        df.setDecimalFormatSymbols(custom);

        String[] parts = config.location.split(",");
        double dlatitude = Double.valueOf(parts[0]);
        double dlongitude = Double.valueOf(parts[1]);
        String latitude = df.format(Double.valueOf(parts[0]));
        String longitude = df.format(Double.valueOf(parts[1]));

        apiRequest = String.format(smhiURL, longitude, latitude);

        try {
            if (HttpUtil.executeUrl("GET", apiRequest, null, null, "application/json", SMHI_TIMEOUT)
                    .contains("out of bounds")) {

                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "The 'location' parameter is out of bound.");
                return;

            }

        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Error connecting to SMHI API.");
            return;
        }

        updateStatus(ThingStatus.ONLINE);

        startAutomaticRefresh(config.refresh);

    }

    private void startAutomaticRefresh(int refresh) {
        if (refreshJob == null || refreshJob.isCancelled()) {

            refreshJob = scheduler.scheduleWithFixedDelay(this::queryApiAndUpdateChannels, 10, refresh * 60,
                    TimeUnit.SECONDS);

        }
    }

    private void queryApiAndUpdateChannels() {
        logger.debug("Running queryApiAndUpdateChannels");

        String apiResponseJson = null;

        SmhiData dataList = null;

        try {
            logger.debug("Quering SMHI API: " + apiRequest);

            apiResponseJson = HttpUtil.executeUrl("GET", apiRequest, null, null, "application/json", SMHI_TIMEOUT);

            dataList = gson.fromJson(apiResponseJson, SmhiData.class);

            TimeZone timeZone = TimeZone.getTimeZone("UTC");
            Calendar calendar = Calendar.getInstance(timeZone);
            calendar.add(Calendar.HOUR, 1);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            Date currentValidDate = calendar.getTime();

            calendar.set(Calendar.HOUR_OF_DAY, 12);
            calendar.add(Calendar.DATE, 1);
            Date tomorrowValidDate = calendar.getTime();

            calendar.add(Calendar.DATE, 1);
            Date day2ValidDate = calendar.getTime();

            calendar.add(Calendar.DATE, 1);
            Date day3ValidDate = calendar.getTime();

            SimpleDateFormat cDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:00:00");
            cDateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            SimpleDateFormat cDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            cDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            logger.debug("Current date : " + cDateTimeFormat.format(currentValidDate));
            logger.debug("Tomorrow date : " + cDateTimeFormat.format(tomorrowValidDate));

            final List<SmhiTimeSeries> timeSeries = dataList.getTimeSeries();

            List<SmhiParameters> parameters;

            HashMap<String, SmhiParameters> hashParameters = new HashMap<String, SmhiParameters>();

            double temp;

            double temperatureMinToday, temperatureMinDay1, temperatureMinDay2, temperatureMinDay3;
            temperatureMinToday = temperatureMinDay1 = temperatureMinDay2 = temperatureMinDay3 = 100;
            double temperatureMaxToday, temperatureMaxDay1, temperatureMaxDay2, temperatureMaxDay3;
            temperatureMaxToday = temperatureMaxDay1 = temperatureMaxDay2 = temperatureMaxDay3 = -100;

            while (timeSeries.size() > 1) {

                logger.debug("Processing date : " + cDateTimeFormat.format(timeSeries.get(0).getValidTime()));

                parameters = timeSeries.get(0).getParameters();

                for (SmhiParameters parameter : parameters) {
                    hashParameters.put(parameter.name, parameter);
                }
                groupId = "";
                if (cDateTimeFormat.format(currentValidDate)
                        .equals(cDateTimeFormat.format(timeSeries.get(0).getValidTime()))) {
                    groupId = CHANNEL_GROUP_CURRENT_WEATHER;
                } else if (cDateTimeFormat.format(tomorrowValidDate)
                        .equals(cDateTimeFormat.format(timeSeries.get(0).getValidTime()))) {
                    groupId = CHANNEL_GROUP_FORECAST_TOMORROW;
                } else if (cDateTimeFormat.format(day2ValidDate)
                        .equals(cDateTimeFormat.format(timeSeries.get(0).getValidTime()))) {
                    groupId = CHANNEL_GROUP_DAILY_FORECAST_DAY2;
                } else if (cDateTimeFormat.format(day3ValidDate)
                        .equals(cDateTimeFormat.format(timeSeries.get(0).getValidTime()))) {
                    groupId = CHANNEL_GROUP_DAILY_FORECAST_DAY3;
                }

                if (groupId != "") {
                    logger.debug("Getting Weatherdata for : " + timeSeries.get(0).getValidTime());
                    getThing().getChannels().stream().map(Channel::getUID)
                            .filter(channelUID -> isLinked(channelUID) && groupId.equals(channelUID.getGroupId()))
                            .forEach(channelUID -> {
                                State state = extractValue(channelUID.getIdWithoutGroup(), hashParameters,
                                        timeSeries.get(0).getValidTime());
                                updateState(channelUID, state);
                            });
                }
                temp = hashParameters.get(CHANNEL_TEMPERATURE_JSON).getValues()[0];

                if (cDateFormat.format(currentValidDate).equals(cDateFormat.format(timeSeries.get(0).getValidTime()))) {
                    if (temp < temperatureMinToday) {
                        temperatureMinToday = temp;
                    }
                    if (temp > temperatureMaxToday) {
                        temperatureMaxToday = temp;
                    }
                } else if (cDateFormat.format(tomorrowValidDate)
                        .equals(cDateFormat.format(timeSeries.get(0).getValidTime()))) {
                    if (temp < temperatureMinDay1) {
                        temperatureMinDay1 = temp;
                    }
                    if (temp > temperatureMaxDay1) {
                        temperatureMaxDay1 = temp;
                    }
                } else if (cDateFormat.format(day2ValidDate)
                        .equals(cDateFormat.format(timeSeries.get(0).getValidTime()))) {
                    if (temp < temperatureMinDay2) {
                        temperatureMinDay2 = temp;
                    }
                    if (temp > temperatureMaxDay2) {
                        temperatureMaxDay2 = temp;
                    }
                } else if (cDateFormat.format(day3ValidDate)
                        .equals(cDateFormat.format(timeSeries.get(0).getValidTime()))) {
                    if (temp < temperatureMinDay3) {
                        temperatureMinDay3 = temp;
                    }
                    if (temp > temperatureMaxDay3) {
                        temperatureMaxDay3 = temp;
                    }
                }

                parameters.clear();
                hashParameters.clear();
                timeSeries.remove(0);
            }

            // update channels min max
            logger.debug("Updating min/max channels");
            updateState("current#temperature-min", new DecimalType(temperatureMinToday));
            updateState("current#temperature-max", new DecimalType(temperatureMaxToday));
            updateState("forecastTomorrow#temperature-min", new DecimalType(temperatureMinDay1));
            updateState("forecastTomorrow#temperature-max", new DecimalType(temperatureMaxDay1));
            updateState("forecastDay2#temperature-min", new DecimalType(temperatureMinDay2));
            updateState("forecastDay2#temperature-max", new DecimalType(temperatureMaxDay2));
            updateState("forecastDay3#temperature-min", new DecimalType(temperatureMinDay3));
            updateState("forecastDay3#temperature-max", new DecimalType(temperatureMaxDay3));

        } catch (IOException e) {
            logger.debug("Exception occurred during execution: {}", e.getMessage(), e);

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
    }

    public State extractValue(String channelId, HashMap<String, SmhiParameters> hashParameters, Date processingDate) {
        logger.debug("Getting Weatherdata for : " + channelId);

        switch (channelId) {
            case CHANNEL_TIME_STAMP:
                DateFormat cDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                cDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                return new DateTimeType(cDateFormat.format(processingDate));
            case CHANNEL_CONDITION:
                return new StringType(parameterTable.getCondition()
                        .get(hashParameters.get(CHANNEL_CONDITION_ID_JSON).getValues()[0].intValue()));
            case CHANNEL_CONDITION_ID:
                return new DecimalType(hashParameters.get(CHANNEL_CONDITION_ID_JSON).getValues()[0].intValue());
            case CHANNEL_TEMPERATURE:
                return new DecimalType(hashParameters.get(CHANNEL_TEMPERATURE_JSON).getValues()[0]);
            case CHANNEL_PRESSURE:
                return new DecimalType(hashParameters.get(CHANNEL_PRESSURE_JSON).getValues()[0]);
            case CHANNEL_HUMIDITY:
                return new DecimalType(hashParameters.get(CHANNEL_HUMIDITY_JSON).getValues()[0]);
            case CHANNEL_WIND_SPEED:
                return new DecimalType(hashParameters.get(CHANNEL_WIND_SPEED_JSON).getValues()[0]);
            case CHANNEL_WIND_DIRECTION:
                return new DecimalType(hashParameters.get(CHANNEL_WIND_DIRECTION_JSON).getValues()[0]);
            case CHANNEL_GUST_SPEED:
                return new DecimalType(hashParameters.get(CHANNEL_GUST_SPEED_JSON).getValues()[0]);
            case CHANNEL_THUNDER_PROBABILITY:
                return new DecimalType(hashParameters.get(CHANNEL_THUNDER_PROBABILITY_JSON).getValues()[0]);
            case CHANNEL_CLOUD_COVER:
                return new DecimalType(hashParameters.get(CHANNEL_CLOUD_COVER_JSON).getValues()[0] * 12.5);
            case CHANNEL_LOW_CLOUD_COVER:
                return new DecimalType(hashParameters.get(CHANNEL_LOW_CLOUD_COVER_JSON).getValues()[0] * 12.5);
            case CHANNEL_MEDIUM_CLOUD_COVER:
                return new DecimalType(hashParameters.get(CHANNEL_MEDIUM_CLOUD_COVER_JSON).getValues()[0] * 12.5);
            case CHANNEL_HIGH_CLOUD_COVER:
                return new DecimalType(hashParameters.get(CHANNEL_HIGH_CLOUD_COVER_JSON).getValues()[0] * 12.5);
            case CHANNEL_VISIBILITY:
                return new DecimalType(hashParameters.get(CHANNEL_VISIBILITY_JSON).getValues()[0]);
            case CHANNEL_PRECIPITATION_CATEGORY:
                return new StringType(parameterTable.getPcat()
                        .get(hashParameters.get(CHANNEL_PRECIPITATION_CATEGORY_ID_JSON).getValues()[0].intValue()));
            case CHANNEL_PRECIPITATION_CATEGORY_ID:
                return new DecimalType(
                        hashParameters.get(CHANNEL_PRECIPITATION_CATEGORY_ID_JSON).getValues()[0].intValue());
            case CHANNEL_MEAN_PRECIPITATION:
                return new DecimalType(hashParameters.get(CHANNEL_MEAN_PRECIPITATION_JSON).getValues()[0]);
            case CHANNEL_MEDIAN_PRECIPITATION:
                return new DecimalType(hashParameters.get(CHANNEL_MEDIAN_PRECIPITATION_JSON).getValues()[0]);
            case CHANNEL_MIN_PRECIPITATION:
                return new DecimalType(hashParameters.get(CHANNEL_MIN_PRECIPITATION_JSON).getValues()[0]);
            case CHANNEL_MAX_PRECIPITATION:
                return new DecimalType(hashParameters.get(CHANNEL_MAX_PRECIPITATION_JSON).getValues()[0]);
            case CHANNEL_FROZEN_PRECIPITATION:
                double fp = hashParameters.get(CHANNEL_FROZEN_PRECIPITATION_JSON).getValues()[0];
                if (fp == -9) {
                    fp = 0;
                }
                return new DecimalType(fp);
        }

        return UnDefType.NULL;
    }

}
