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
package org.openhab.binding.meteofrance.internal.handler;

import static org.openhab.binding.meteofrance.internal.MeteoFranceBindingConstants.*;
import static org.openhab.core.types.TimeSeries.Policy.REPLACE;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.meteofrance.internal.config.ForecastConfiguration;
import org.openhab.binding.meteofrance.internal.dto.RainForecast;
import org.openhab.binding.meteofrance.internal.dto.RainForecast.Forecast;
import org.openhab.binding.meteofrance.internal.dto.RainForecast.Properties;
import org.openhab.binding.meteofrance.internal.dto.RainForecast.RainIntensity;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.TimeSeries;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RainForecastHandler} is responsible for updating channels
 * and querying the API
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class RainForecastHandler extends BaseThingHandler implements MeteoFranceChildHandler {
    private final Logger logger = LoggerFactory.getLogger(RainForecastHandler.class);
    private final ChannelUID intensityChannelUID;
    private final ZoneId systemZoneId;

    private Optional<ScheduledFuture<?>> refreshJob = Optional.empty();
    private Optional<PointType> location = Optional.empty();

    public RainForecastHandler(Thing thing, ZoneId zoneId) {
        super(thing);
        this.intensityChannelUID = new ChannelUID(getThing().getUID(), INTENSITY);
        this.systemZoneId = zoneId;
    }

    @Override
    public @Nullable Bridge getBridge() {
        return super.getBridge();
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Rain Forecast handler.");
        disposeJob();

        updateStatus(ThingStatus.UNKNOWN);

        ForecastConfiguration config = getConfigAs(ForecastConfiguration.class);

        try {
            this.location = Optional.of(new PointType(config.location));
        } catch (IllegalArgumentException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Incorrect 'location' value.");
            return;
        }

        this.refreshJob = Optional.of(scheduler.schedule(this::updateAndPublish, 2, TimeUnit.SECONDS));
    }

    private void disposeJob() {
        refreshJob.ifPresent(job -> job.cancel(true));
        refreshJob = Optional.empty();
    }

    @Override
    public void dispose() {
        logger.debug("Disposing the Rain Forecast handler.");
        disposeJob();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateAndPublish();
        }
    }

    private void updateAndPublish() {
        getBridgeHandler().ifPresentOrElse(handler -> {
            location.ifPresent(loc -> {
                RainForecast forecast = handler.getRainForecast(loc);
                if (forecast != null) {
                    updateStatus(ThingStatus.ONLINE);
                    setProperties(forecast.properties);
                    updateDate(UPDATE_TIME, forecast.updateTime);
                }
            });
        }, () -> logger.warn("No viable bridge"));
    }

    private void setProperties(@Nullable Properties forecastProps) {
        if (forecastProps == null) {
            return;
        }
        Map<String, String> properties = editProperties();
        properties.put("altitude", "%d m".formatted(forecastProps.altitude));
        properties.put("name", forecastProps.name);
        properties.put("country", forecastProps.country);
        properties.put("department", forecastProps.frenchDepartment);
        properties.put("timezone", forecastProps.timezone);
        properties.put("comfidence", "%d".formatted(forecastProps.confidence));
        this.updateProperties(properties);

        setForecast(forecastProps.forecast);
    }

    private void setForecast(List<Forecast> forecast) {
        TimeSeries timeSeries = new TimeSeries(REPLACE);
        ZonedDateTime now = ZonedDateTime.now();

        State currentState = null;
        long untilNextRun = 0;
        for (Forecast prevision : forecast) {
            State state = prevision.rainIntensity() != RainIntensity.UNKNOWN
                    ? new DecimalType(prevision.rainIntensity().ordinal())
                    : UnDefType.UNDEF;
            if (currentState == null) {
                currentState = state;
                if (prevision.time().isAfter(now)) {
                    untilNextRun = now.until(prevision.time(), ChronoUnit.SECONDS);
                }
            }
            timeSeries.add(prevision.time().toInstant(), state);
        }
        updateState(intensityChannelUID, currentState == null ? UnDefType.UNDEF : currentState);
        sendTimeSeries(intensityChannelUID, timeSeries);

        untilNextRun = untilNextRun != 0 ? untilNextRun : 300;

        logger.debug("Refresh rain intensity forecast in: {}s", untilNextRun);
        refreshJob = Optional.of(scheduler.schedule(this::updateAndPublish, untilNextRun, TimeUnit.SECONDS));
    }

    private void updateDate(String channelId, @Nullable ZonedDateTime zonedDateTime) {
        if (isLinked(channelId)) {
            updateState(channelId,
                    zonedDateTime != null ? new DateTimeType(zonedDateTime.withZoneSameInstant(systemZoneId))
                            : UnDefType.NULL);
        }
    }
}
