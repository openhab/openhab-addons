/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.ems.internal.handler;

import static org.openhab.binding.ems.internal.EMSBindingConstants.CHANNEL_1;

import java.util.Calendar;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ems.internal.EMSConfiguration;
import org.openhab.binding.ems.utils.Formulas;
import org.openhab.core.library.types.PointType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EMSHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class EMSHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(EMSHandler.class);
    // Granularity of forecast in minutes
    private final static int FORECAST_GRANULARITY_MIN = 5;

    private @Nullable EMSConfiguration config;
    private Optional<Calendar> lastUpdate = Optional.empty();
    private Optional<WeatherForecast> wf = Optional.empty();
    private double latitude = 0;
    private double longitude = 0;

    public EMSHandler(Thing thing, @Nullable PointType pt) {
        super(thing);
        if (pt != null) {
            latitude = pt.getLatitude().doubleValue();
            longitude = pt.getLongitude().doubleValue();
        } else {
            logger.warn("No Location given - forecast will not work without havin location!");
        }

    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (CHANNEL_1.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {
                // TODO: handle data refresh
            }

            // TODO: handle command

            // Note: if communication with thing fails for some reason,
            // indicate that by setting the status with detail information:
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
            // "Could not control device at IP address x.x.x.x");
        }
    }

    @Override
    public void initialize() {
        setConfiguration(getConfigAs(EMSConfiguration.class));
        updateStatus(ThingStatus.ONLINE);
        wf = Optional.of(new WeatherForecast(latitude, longitude, config.owmApiKey));
        scheduler.scheduleAtFixedRate(this::update, 0, 5, TimeUnit.MINUTES);
    }

    public void setConfiguration(EMSConfiguration c) {
        config = c;
    }

    public void update() {
        Calendar update = Calendar.getInstance();
        if (newHour(lastUpdate, update)) {

        }
        if (newDay(lastUpdate, update)) {
            generatePrediction();
        }
        lastUpdate = Optional.of(update);
    }

    public void generatePrediction() {
        wf = Optional.of(new WeatherForecast(latitude, longitude, config.owmApiKey));
        Calendar date = Calendar.getInstance();
        for (int days = 0; days < 7; days++) {
            int observationDay = date.get(Calendar.DAY_OF_MONTH);
            String observationDaate = (date.get(Calendar.DAY_OF_MONTH) + 1) + "." + (date.get(Calendar.MONTH) + 1) + "."
                    + date.get(Calendar.YEAR);
            double dailyProduction = 0;
            int dCounter = 0;
            double dCloduiness = 0;
            while (observationDay == date.get(Calendar.DAY_OF_MONTH)) {
                int forecast_hour = date.get(Calendar.HOUR_OF_DAY);
                int forecast_minute = date.get(Calendar.MINUTE);
                double sunHeight = Formulas.round(Formulas.sunPositionDIN(date.get(Calendar.YEAR),
                        date.get(Calendar.MONTH) + 1, date.get(Calendar.DAY_OF_MONTH) + 1, forecast_hour,
                        forecast_minute * FORECAST_GRANULARITY_MIN, 0, latitude, 10, 2), 1);
                if (sunHeight > 0) {
                    int cloudiness = wf.get().getCloudiness(observationDay, forecast_hour);
                    // logger.info("Cloudiness on {} {}: {}", observationDaate, forecast_hour, cloudiness);
                    double radiationInfo = Formulas.getRadiationInfo(Formulas.getCalendar(date.get(Calendar.YEAR),
                            date.get(Calendar.MONTH) + 1, date.get(Calendar.DAY_OF_MONTH) + 1, forecast_hour,
                            forecast_minute * FORECAST_GRANULARITY_MIN), sunHeight, latitude);
                    double adjustedRadiationInfo = radiationInfo * (100 - cloudiness) / 100;
                    double production = Formulas
                            .round(9.75 * adjustedRadiationInfo / 1000 * FORECAST_GRANULARITY_MIN / 60, 3);
                    double cloudProduction = Formulas.round(production * 100 - cloudiness / 100, 3);
                    if (cloudiness > 20 && cloudiness < 80) {
                        logger.info("Cloudiness: {} Factor {}", cloudiness, Double.valueOf(cloudiness) / 100.0);
                    }
                    // logger.info("Best Production: {} Cloud Production {}", production,
                    // Double.valueOf(cloudiness) / 100.0);
                    dailyProduction += production;// * (100 - cloudiness) / 100;
                    dCloduiness += cloudiness;
                    dCounter++;
                }
                date.add(Calendar.MINUTE, FORECAST_GRANULARITY_MIN);
            }
            // updateState(new ChannelUID("pv-prediction-today"),
            // QuantityType.valueOf(dailyProduction, Units.KILOWATT_HOUR));
            dailyProduction = Formulas.round(dailyProduction, 0);
            dCloduiness = Formulas.round(dCloduiness / dCounter, 0);
            logger.info("Prediction for {} is {} with ~ {}% cloudiness", observationDaate, dailyProduction,
                    dCloduiness);
        }
    }

    private boolean newHour(Optional<Calendar> lastUpdate2, Calendar update) {
        return lastUpdate2.isEmpty() ? true : lastUpdate2.get().get(Calendar.HOUR) != update.get(Calendar.HOUR);
    }

    private boolean newDay(Optional<Calendar> lastUpdate2, Calendar update) {
        return lastUpdate2.isEmpty() ? true
                : lastUpdate2.get().get(Calendar.DAY_OF_YEAR) != update.get(Calendar.DAY_OF_YEAR);
    }
}
