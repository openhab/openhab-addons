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
package org.openhab.binding.solarforecast.internal.forecastsolar.handler;

import static org.openhab.binding.solarforecast.internal.SolarForecastBindingConstants.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.solarforecast.internal.SolarForecastBindingConstants;
import org.openhab.binding.solarforecast.internal.actions.SolarForecast;
import org.openhab.binding.solarforecast.internal.actions.SolarForecastActions;
import org.openhab.binding.solarforecast.internal.actions.SolarForecastProvider;
import org.openhab.binding.solarforecast.internal.forecastsolar.ForecastSolarObject;
import org.openhab.binding.solarforecast.internal.forecastsolar.config.ForecastSolarBridgeConfiguration;
import org.openhab.binding.solarforecast.internal.utils.Utils;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.PointType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ForecastSolarBridgeHandler} is a non active handler instance. It will be triggerer by the bridge.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class ForecastSolarBridgeHandler extends BaseBridgeHandler implements SolarForecastProvider {
    private final Logger logger = LoggerFactory.getLogger(ForecastSolarBridgeHandler.class);
    private final PointType homeLocation;

    private List<ForecastSolarPlaneHandler> parts = new ArrayList<ForecastSolarPlaneHandler>();
    private Optional<ForecastSolarBridgeConfiguration> configuration = Optional.empty();
    private Optional<ScheduledFuture<?>> refreshJob = Optional.empty();

    public ForecastSolarBridgeHandler(Bridge bridge, PointType location) {
        super(bridge);
        homeLocation = location;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(SolarForecastActions.class);
    }

    @Override
    public void initialize() {
        ForecastSolarBridgeConfiguration config = getConfigAs(ForecastSolarBridgeConfiguration.class);
        if (config.location.equals(SolarForecastBindingConstants.AUTODETECT)) {
            Configuration editConfig = editConfiguration();
            editConfig.put("location", homeLocation.toString());
            updateConfiguration(editConfig);
            config = getConfigAs(ForecastSolarBridgeConfiguration.class);
        }
        configuration = Optional.of(config);
        updateStatus(ThingStatus.ONLINE);
        startSchedule(configuration.get().channelRefreshInterval);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    private void startSchedule(int interval) {
        /**
         * Interval given in minutes so seconds needs multiplier. Wait for 10 seconds until attached planes are created
         * and registered. If now waiting time is defined user will see some glitches e.g. after restart if no or not
         * all planes are initialized
         */
        refreshJob.ifPresentOrElse(job -> {
            if (job.isCancelled()) {
                refreshJob = Optional
                        .of(scheduler.scheduleWithFixedDelay(this::getData, 10, interval * 60, TimeUnit.SECONDS));
            } // else - scheduler is already running!
        }, () -> {
            refreshJob = Optional
                    .of(scheduler.scheduleWithFixedDelay(this::getData, 10, interval * 60, TimeUnit.SECONDS));
        });
    }

    /**
     * Get data for all planes. Synchronized to protect parts map from being modified during update
     */
    private synchronized void getData() {
        if (parts.isEmpty()) {
            logger.debug("No PV plane defined yet");
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        double actualSum = 0;
        double actualPowerSum = 0;
        double remainSum = 0;
        double todaySum = 0;
        double day1Sum = 0;
        double day2Sum = 0;
        double day3Sum = 0;
        for (Iterator<ForecastSolarPlaneHandler> iterator = parts.iterator(); iterator.hasNext();) {
            ForecastSolarPlaneHandler sfph = iterator.next();
            ForecastSolarObject fo = sfph.fetchData();
            actualSum += fo.getActualValue(now);
            actualPowerSum += fo.getActualPowerValue(now);
            remainSum += fo.getRemainingProduction(now);
            todaySum += fo.getDayTotal(now.toLocalDate());
            day1Sum += fo.getDayTotal(now.plusDays(1).toLocalDate());
            day2Sum += fo.getDayTotal(now.plusDays(2).toLocalDate());
            day3Sum += fo.getDayTotal(now.plusDays(3).toLocalDate());
        }
        updateState(CHANNEL_ACTUAL, Utils.getEnergyState(actualSum));
        updateState(CHANNEL_ACTUAL_POWER, Utils.getPowerState(actualPowerSum));
        updateState(CHANNEL_REMAINING, Utils.getEnergyState(remainSum));
        updateState(CHANNEL_TODAY, Utils.getEnergyState(todaySum));
        updateState(CHANNEL_DAY1, Utils.getEnergyState(day1Sum));
        updateState(CHANNEL_DAY2, Utils.getEnergyState(day2Sum));
        updateState(CHANNEL_DAY3, Utils.getEnergyState(day3Sum));
    }

    @Override
    public void dispose() {
        refreshJob.ifPresent(job -> job.cancel(true));
    }

    synchronized void addPlane(ForecastSolarPlaneHandler sfph) {
        parts.add(sfph);
        // update passive PV plane with necessary data
        if (configuration.isPresent()) {
            sfph.setLocation(new PointType(configuration.get().location));
            if (!EMPTY.equals(configuration.get().apiKey)) {
                sfph.setApiKey(configuration.get().apiKey);
            }
        }
        getData();
    }

    synchronized void removePlane(ForecastSolarPlaneHandler sfph) {
        parts.remove(sfph);
    }

    @Override
    public synchronized List<SolarForecast> getSolarForecasts() {
        List<SolarForecast> l = new ArrayList<SolarForecast>();
        parts.forEach(entry -> {
            l.addAll(entry.getSolarForecasts());
        });
        return l;
    }
}
