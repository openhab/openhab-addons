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

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.solarforecast.internal.actions.SolarForecast;
import org.openhab.binding.solarforecast.internal.actions.SolarForecastActions;
import org.openhab.binding.solarforecast.internal.actions.SolarForecastProvider;
import org.openhab.binding.solarforecast.internal.forecastsolar.ForecastSolarObject;
import org.openhab.binding.solarforecast.internal.forecastsolar.config.ForecastSolarBridgeConfiguration;
import org.openhab.binding.solarforecast.internal.solcast.SolcastObject.QueryMode;
import org.openhab.binding.solarforecast.internal.utils.Utils;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.TimeSeries;
import org.openhab.core.types.TimeSeries.Policy;

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

    public void forecastUpdate() {
        if (planes.isEmpty()) {
            return;
        }
        TreeMap<Instant, QuantityType<?>> combinedPowerForecast = new TreeMap<Instant, QuantityType<?>>();
        TreeMap<Instant, QuantityType<?>> combinedEnergyForecast = new TreeMap<Instant, QuantityType<?>>();
        List<SolarForecast> forecastObjects = new ArrayList<SolarForecast>();
        for (Iterator<ForecastSolarPlaneHandler> iterator = planes.iterator(); iterator.hasNext();) {
            ForecastSolarPlaneHandler sfph = iterator.next();
            forecastObjects.addAll(sfph.getSolarForecasts());
        }
        forecastObjects.forEach(fc -> {
            TimeSeries powerTS = fc.getPowerTimeSeries(QueryMode.Estimation);
            powerTS.getStates().forEach(entry -> {
                Utils.addState(combinedPowerForecast, entry);
            });
            TimeSeries energyTS = fc.getEnergyTimeSeries(QueryMode.Estimation);
            energyTS.getStates().forEach(entry -> {
                Utils.addState(combinedEnergyForecast, entry);
            });
        });

        TimeSeries powerSeries = new TimeSeries(Policy.REPLACE);
        combinedPowerForecast.forEach((timestamp, state) -> {
            powerSeries.add(timestamp, state);
        });
        sendTimeSeries(CHANNEL_POWER_ESTIMATE, powerSeries);

        TimeSeries energySeries = new TimeSeries(Policy.REPLACE);
        combinedEnergyForecast.forEach((timestamp, state) -> {
            energySeries.add(timestamp, state);
        });
        sendTimeSeries(CHANNEL_ENERGY_ESTIMATE, energySeries);
    }

    @Override
    public void dispose() {
        refreshJob.ifPresent(job -> job.cancel(true));
    }

    public synchronized void addPlane(ForecastSolarPlaneHandler sfph) {
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

    public synchronized void removePlane(ForecastSolarPlaneHandler sfph) {
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
