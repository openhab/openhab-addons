/**
        updateState(CHANNEL_ENERGY_REMAIN, Utils.getEnergyState(daySum - energySum));
        updateState(CHANNEL_ENERGY_TODAY, Utils.getEnergyState(daySum));
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
package org.openhab.binding.solarforecast.internal.solcast.handler;

import static org.openhab.binding.solarforecast.internal.SolarForecastBindingConstants.*;

import java.time.DateTimeException;
import java.time.ZoneId;
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
import org.openhab.binding.solarforecast.internal.solcast.SolcastObject;
import org.openhab.binding.solarforecast.internal.solcast.SolcastObject.QueryMode;
import org.openhab.binding.solarforecast.internal.solcast.config.SolcastBridgeConfiguration;
import org.openhab.binding.solarforecast.internal.utils.Utils;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SolcastBridgeHandler} is a non active handler instance. It will be triggered by the bridge.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class SolcastBridgeHandler extends BaseBridgeHandler implements SolarForecastProvider, TimeZoneProvider {
    private final Logger logger = LoggerFactory.getLogger(SolcastBridgeHandler.class);

    private List<SolcastPlaneHandler> parts = new ArrayList<SolcastPlaneHandler>();
    private Optional<SolcastBridgeConfiguration> configuration = Optional.empty();
    private Optional<ScheduledFuture<?>> refreshJob = Optional.empty();
    private ZoneId timeZone;

    public SolcastBridgeHandler(Bridge bridge, TimeZoneProvider tzp) {
        super(bridge);
        timeZone = tzp.getTimeZone();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(SolarForecastActions.class);
    }

    @Override
    public void initialize() {
        SolcastBridgeConfiguration config = getConfigAs(SolcastBridgeConfiguration.class);
        configuration = Optional.of(config);
        if (!EMPTY.equals(config.apiKey)) {
            if (!AUTODETECT.equals(configuration.get().timeZone)) {
                try {
                    timeZone = ZoneId.of(configuration.get().timeZone);
                    updateStatus(ThingStatus.ONLINE);
                    refreshJob = Optional.of(scheduler.scheduleWithFixedDelay(this::getData, 0, REFRESH_ACTUAL_INTERVAL,
                            TimeUnit.MINUTES));
                } catch (DateTimeException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "@text/solarforecast.site.status.timezone" + " [\"" + configuration.get().timeZone + "\"]");
                }
            } else {
                updateStatus(ThingStatus.ONLINE);
                refreshJob = Optional.of(
                        scheduler.scheduleWithFixedDelay(this::getData, 0, REFRESH_ACTUAL_INTERVAL, TimeUnit.MINUTES));
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/solarforecast.site.status.api-key-missing");
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            getData();
        }
    }

    @Override
    public void dispose() {
        refreshJob.ifPresent(job -> job.cancel(true));
    }

    /**
     * Get data for all planes. Protect parts map from being modified during update
     */
    private synchronized void getData() {
        if (parts.isEmpty()) {
            logger.debug("No PV plane defined yet");
            return;
        }
        ZonedDateTime now = ZonedDateTime.now(getTimeZone());
        double energySum = 0;
        double powerSum = 0;
        double daySum = 0;
        for (Iterator<SolcastPlaneHandler> iterator = parts.iterator(); iterator.hasNext();) {
            SolcastPlaneHandler sfph = iterator.next();
            SolcastObject fo = sfph.fetchData();
            energySum += fo.getActualEnergyValue(now, QueryMode.Estimation);
            powerSum += fo.getActualPowerValue(now, QueryMode.Estimation);
            daySum += fo.getDayTotal(now.toLocalDate(), QueryMode.Estimation);
        }
        updateState(CHANNEL_ENERGY_ACTUAL, Utils.getEnergyState(energySum));
        updateState(CHANNEL_ENERGY_REMAIN, Utils.getEnergyState(daySum - energySum));
        updateState(CHANNEL_ENERGY_TODAY, Utils.getEnergyState(daySum));
        updateState(CHANNEL_POWER_ACTUAL, Utils.getPowerState(powerSum));
    }

    synchronized void addPlane(SolcastPlaneHandler sph) {
        parts.add(sph);
    }

    synchronized void removePlane(SolcastPlaneHandler sph) {
        parts.remove(sph);
    }

    String getApiKey() {
        if (configuration.isPresent()) {
            return configuration.get().apiKey;
        }
        return EMPTY;
    }

    @Override
    public synchronized List<SolarForecast> getSolarForecasts() {
        List<SolarForecast> l = new ArrayList<SolarForecast>();
        parts.forEach(entry -> {
            l.addAll(entry.getSolarForecasts());
        });
        return l;
    }

    @Override
    public ZoneId getTimeZone() {
        return timeZone;
    }
}
