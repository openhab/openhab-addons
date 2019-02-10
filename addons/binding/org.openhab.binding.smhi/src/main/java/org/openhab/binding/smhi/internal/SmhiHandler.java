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

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
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

    protected static final String URL = "http://opendata-download-metfcst.smhi.se/api/category/pmp3g/version/2/geotype/point/lon/%s/lat/%s/data.json";

    // Timeout for weather data requests.
    private static final int SMHI_TIMEOUT = 5000;

    private Gson gson = new GsonBuilder().create();

    private String apiRequest = "";

    @Nullable
    private SmhiConfiguration config;

    public SmhiHandler(Thing thing) {
        super(thing);
    }

    private SmhiParameterTables pt = new SmhiParameterTables();

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (CHANNEL_TEMPERATURE.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {
                queryApiAndUpdateChannels();
            }
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing SMHI weather handler.");

        config = getConfigAs(SmhiConfiguration.class);

        updateStatus(ThingStatus.UNKNOWN);

        // Example for background initialization:
        scheduler.execute(() -> {
            boolean configValid = true;

            DecimalFormat df = new DecimalFormat("##.######");
            DecimalFormatSymbols custom = new DecimalFormatSymbols();
            custom.setDecimalSeparator('.');
            df.setDecimalFormatSymbols(custom);

            String[] parts = config.weatherlocation.split(",");
            double dlatitude = Double.valueOf(parts[0]);
            double dlongitude = Double.valueOf(parts[1]);
            String latitude = df.format(Double.valueOf(parts[0]));
            String longitude = df.format(Double.valueOf(parts[1]));

            if (!is_valid_gps_coordinate(dlatitude, dlongitude)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Location is not a valid GPS coordinate.");
                configValid = false;
            }
            /*
             * ArrayList<String> polygon_lat_long_pairs = new ArrayList<String>();
             * polygon_lat_long_pairs.add("0.00000,70.666011"); // -8.553029
             * polygon_lat_long_pairs.add("37.934697,70.742227");
             * polygon_lat_long_pairs.add("27.392184,52.542473");
             * polygon_lat_long_pairs.add("2.250475,52.500440");
             *
             * if (!coordinate_is_inside_polygon(dlatitude, dlongitude, polygon_lat_long_pairs)) {
             * updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
             * "Location not within SMHI Covered area.");
             * configValid = false;
             * }
             */
            if (configValid) {

                apiRequest = String.format(URL, longitude, latitude);
                updateStatus(ThingStatus.ONLINE);
                startAutomaticRefresh(config.refresh);
            }
        });
    }

    private void startAutomaticRefresh(int refresh) {
        if (refreshJob == null || refreshJob.isCancelled()) {
            Runnable runnable = () -> {
                queryApiAndUpdateChannels();
            };
            refreshJob = scheduler.scheduleWithFixedDelay(runnable, 10, refresh * 60, TimeUnit.SECONDS);
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

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.HOUR_OF_DAY, 1);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            Date currentvaliddate = calendar.getTime();

            calendar.set(Calendar.HOUR_OF_DAY, 13);
            calendar.add(Calendar.DATE, 1);
            Date tomorrowvaliddate = calendar.getTime();

            calendar.add(Calendar.DATE, 1);
            Date day2validdate = calendar.getTime();

            calendar.add(Calendar.DATE, 1);
            Date day3validdate = calendar.getTime();

            SimpleDateFormat cdtf = new SimpleDateFormat("yyyy-MM-dd HH:00:00");

            SimpleDateFormat cdf = new SimpleDateFormat("yyyy-MM-dd");

            final List<SmhiTimeSeries> timeSeries = dataList.getTimeSeries();

            List<SmhiParameters> parameters;

            HashMap<String, SmhiParameters> hashParameters = new HashMap<String, SmhiParameters>();

            double temp;

            double tmin, tmin1, tmin2, tmin3;
            tmin = tmin1 = tmin2 = tmin3 = 100;
            double tmax, tmax1, tmax2, tmax3;
            tmax = tmax1 = tmax2 = tmax3 = -100;

            while (timeSeries.size() > 1) {
                logger.debug("Handeling date : " + timeSeries.get(0).getValidTime());

                parameters = timeSeries.get(0).getParameters();

                for (SmhiParameters parameter : parameters) {
                    hashParameters.put(parameter.name, parameter);
                }

                if (cdtf.format(currentvaliddate).equals(cdtf.format(timeSeries.get(0).getValidTime()))) {
                    logger.debug("Getting Weatherdata for : " + timeSeries.get(0).getValidTime());
                    getThing().getChannels().stream().map(Channel::getUID)
                            .filter(channelUID -> isLinked(channelUID)
                                    && CHANNEL_GROUP_CURRENT_WEATHER.equals(channelUID.getGroupId()))
                            .forEach(channelUID -> {
                                State state = getValue(channelUID.getIdWithoutGroup(), hashParameters,
                                        timeSeries.get(0).getValidTime());
                                updateState(channelUID, state);
                            });
                }

                if (cdtf.format(tomorrowvaliddate).equals(cdtf.format(timeSeries.get(0).getValidTime()))) {
                    logger.debug("Getting Weatherdata for : " + timeSeries.get(0).getValidTime());
                    getThing().getChannels().stream().map(Channel::getUID)
                            .filter(channelUID -> isLinked(channelUID)
                                    && CHANNEL_GROUP_FORECAST_TOMORROW.equals(channelUID.getGroupId()))
                            .forEach(channelUID -> {
                                State state = getValue(channelUID.getIdWithoutGroup(), hashParameters,
                                        timeSeries.get(0).getValidTime());
                                updateState(channelUID, state);
                            });
                }

                if (cdtf.format(day2validdate).equals(cdtf.format(timeSeries.get(0).getValidTime()))) {
                    logger.debug("Getting Weatherdata for : " + timeSeries.get(0).getValidTime());
                    getThing().getChannels().stream().map(Channel::getUID)
                            .filter(channelUID -> isLinked(channelUID)
                                    && CHANNEL_GROUP_DAILY_FORECAST_DAY2.equals(channelUID.getGroupId()))
                            .forEach(channelUID -> {
                                State state = getValue(channelUID.getIdWithoutGroup(), hashParameters,
                                        timeSeries.get(0).getValidTime());
                                updateState(channelUID, state);
                            });
                }

                if (cdtf.format(day3validdate).equals(cdtf.format(timeSeries.get(0).getValidTime()))) {
                    logger.debug("Getting Weatherdata for : " + timeSeries.get(0).getValidTime());
                    getThing().getChannels().stream().map(Channel::getUID)
                            .filter(channelUID -> isLinked(channelUID)
                                    && CHANNEL_GROUP_DAILY_FORECAST_DAY3.equals(channelUID.getGroupId()))
                            .forEach(channelUID -> {
                                State state = getValue(channelUID.getIdWithoutGroup(), hashParameters,
                                        timeSeries.get(0).getValidTime());
                                updateState(channelUID, state);
                            });
                }

                temp = hashParameters.get(CHANNEL_TEMPERATURE_JSON).getValues()[0];

                if (cdf.format(currentvaliddate).equals(cdf.format(timeSeries.get(0).getValidTime()))) {
                    if (temp < tmin) {
                        tmin = temp;
                    }
                    if (temp > tmax) {
                        tmax = temp;
                    }
                } else if (cdf.format(tomorrowvaliddate).equals(cdf.format(timeSeries.get(0).getValidTime()))) {
                    if (temp < tmin1) {
                        tmin1 = temp;
                    }
                    if (temp > tmax1) {
                        tmax1 = temp;
                    }
                } else if (cdf.format(day2validdate).equals(cdf.format(timeSeries.get(0).getValidTime()))) {
                    if (temp < tmin2) {
                        tmin2 = temp;
                    }
                    if (temp > tmax2) {
                        tmax2 = temp;
                    }
                } else if (cdf.format(day3validdate).equals(cdf.format(timeSeries.get(0).getValidTime()))) {
                    if (temp < tmin3) {
                        tmin3 = temp;
                    }
                    if (temp > tmax3) {
                        tmax3 = temp;
                    }
                }

                parameters.clear();
                hashParameters.clear();
                timeSeries.remove(0);
            }

            // update channels min max
            logger.debug("Updating min/max channels");
            updateState("current#temperature-min", new DecimalType(tmin));
            updateState("current#temperature-max", new DecimalType(tmax));
            updateState("forecastTomorrow#temperature-min", new DecimalType(tmin1));
            updateState("forecastTomorrow#temperature-max", new DecimalType(tmax1));
            updateState("forecastDay2#temperature-min", new DecimalType(tmin2));
            updateState("forecastDay2#temperature-max", new DecimalType(tmax2));
            updateState("forecastDay3#temperature-min", new DecimalType(tmin3));
            updateState("forecastDay3#temperature-max", new DecimalType(tmax3));
        } catch (final Exception e) {
            logger.debug("Exception occurred during execution: {}", e.getMessage(), e);

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
    }

    public State getValue(String channelId, HashMap<String, SmhiParameters> hashParameters, Date vt) {
        logger.debug("Getting Weatherdata for : " + channelId);

        switch (channelId) {
            case CHANNEL_TIME_STAMP:
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                return new DateTimeType(df.format(vt));
            case CHANNEL_CONDITION:
                return new StringType(
                        pt.getCondition().get(hashParameters.get(CHANNEL_CONDITION_ID_JSON).getValues()[0].intValue()));
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
                return new StringType(pt.getPcat()
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

    public static boolean coordinate_is_inside_polygon(double latitude, double longitude,
            ArrayList<String> polygon_lat_long_pairs) {

        double PI = 3.14159265;

        ArrayList<Double> lat_array = new ArrayList<Double>();
        ArrayList<Double> long_array = new ArrayList<Double>();

        for (String s : polygon_lat_long_pairs) {
            lat_array.add(Double.parseDouble(s.split(",")[0]));
            long_array.add(Double.parseDouble(s.split(",")[1]));
        }

        int i;
        double angle = 0;
        double point1_lat;
        double point1_long;
        double point2_lat;
        double point2_long;
        int n = lat_array.size();

        for (i = 0; i < n; i++) {
            point1_lat = lat_array.get(i) - latitude;
            point1_long = long_array.get(i) - longitude;
            point2_lat = lat_array.get((i + 1) % n) - latitude;
            // you should have paid more attention in high school geometry.
            point2_long = long_array.get((i + 1) % n) - longitude;
            angle += Angle2D(point1_lat, point1_long, point2_lat, point2_long);
        }

        if (Math.abs(angle) < PI) {
            return false;
        } else {
            return true;
        }
    }

    public static double Angle2D(double y1, double x1, double y2, double x2) {

        double PI = 3.14159265;
        double TWOPI = 2 * PI;

        double dtheta, theta1, theta2;

        theta1 = Math.atan2(y1, x1);
        theta2 = Math.atan2(y2, x2);
        dtheta = theta2 - theta1;
        while (dtheta > PI) {
            dtheta -= TWOPI;
        }
        while (dtheta < -PI) {
            dtheta += TWOPI;
        }

        return (dtheta);
    }

    public static boolean is_valid_gps_coordinate(double latitude, double longitude) {
        // This is a bonus function, it's unused, to reject invalid lat/longs.
        if (latitude > -90 && latitude < 90 && longitude > -180 && longitude < 180) {
            return true;
        }
        return false;
    }

}
