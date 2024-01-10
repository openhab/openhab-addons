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
package org.openhab.binding.solarforecast.internal.forecastsolar.handler;

import static org.openhab.binding.solarforecast.internal.SolarForecastBindingConstants.*;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
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

/**
 * The {@link ForecastSolarBridgeHandler} is a non active handler instance. It will be triggerer by the bridge.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class ForecastSolarBridgeHandler extends BaseBridgeHandler implements SolarForecastProvider {
    private final PointType homeLocation;

    private List<ForecastSolarPlaneHandler> planes = new ArrayList<ForecastSolarPlaneHandler>();
    private Optional<ForecastSolarBridgeConfiguration> configuration = Optional.empty();
    private Optional<ScheduledFuture<?>> refreshJob = Optional.empty();

    public ForecastSolarBridgeHandler(Bridge bridge, PointType location) {
        super(bridge);
        homeLocation = location;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(SolarForecastActions.class);
    }

    @Override
    public void initialize() {
        ForecastSolarBridgeConfiguration config = getConfigAs(ForecastSolarBridgeConfiguration.class);
        if (config.location.isEmpty()) {
            Configuration editConfig = editConfiguration();
            editConfig.put("location", homeLocation.toString());
            updateConfiguration(editConfig);
            config = getConfigAs(ForecastSolarBridgeConfiguration.class);
        }
        configuration = Optional.of(config);
        updateStatus(ThingStatus.ONLINE);
        refreshJob = Optional
                .of(scheduler.scheduleWithFixedDelay(this::getData, 0, REFRESH_ACTUAL_INTERVAL, TimeUnit.MINUTES));
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    /**
     * Get data for all planes. Synchronized to protect parts map from being modified during update
     */
    private synchronized void getData() {
        if (planes.isEmpty()) {
            return;
        }
        double energySum = 0;
        double powerSum = 0;
        double daySum = 0;
        for (Iterator<ForecastSolarPlaneHandler> iterator = planes.iterator(); iterator.hasNext();) {
            ForecastSolarPlaneHandler sfph = iterator.next();
            ForecastSolarObject fo = sfph.fetchData();
            ZonedDateTime now = ZonedDateTime.now(fo.getZone());
            energySum += fo.getActualEnergyValue(now);
            powerSum += fo.getActualPowerValue(now);
            daySum += fo.getDayTotal(now.toLocalDate());
        }
        updateState(CHANNEL_ENERGY_ACTUAL, Utils.getEnergyState(energySum));
        updateState(CHANNEL_ENERGY_REMAIN, Utils.getEnergyState(daySum - energySum));
        updateState(CHANNEL_ENERGY_TODAY, Utils.getEnergyState(daySum));
        updateState(CHANNEL_POWER_ACTUAL, Utils.getPowerState(powerSum));
    }

    @Override
    public void dispose() {
        refreshJob.ifPresent(job -> job.cancel(true));
    }

    synchronized void addPlane(ForecastSolarPlaneHandler sfph) {
        planes.add(sfph);
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
        planes.remove(sfph);
    }

    @Override
    public synchronized List<SolarForecast> getSolarForecasts() {
        List<SolarForecast> l = new ArrayList<SolarForecast>();
        planes.forEach(entry -> {
            l.addAll(entry.getSolarForecasts());
        });
        return l;
    }
}
