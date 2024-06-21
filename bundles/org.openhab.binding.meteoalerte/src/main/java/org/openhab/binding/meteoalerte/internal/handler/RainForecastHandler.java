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
package org.openhab.binding.meteoalerte.internal.handler;

import static org.openhab.binding.meteoalerte.internal.MeteoAlerteBindingConstants.*;
import static org.openhab.core.types.TimeSeries.Policy.REPLACE;

import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.HttpMethod;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.meteoalerte.internal.MeteoAlerteException;
import org.openhab.binding.meteoalerte.internal.config.ForecastConfiguration;
import org.openhab.binding.meteoalerte.internal.deserialization.MeteoAlerteDeserializer;
import org.openhab.binding.meteoalerte.internal.dto.RainForecast;
import org.openhab.binding.meteoalerte.internal.dto.RainForecast.Forecast;
import org.openhab.binding.meteoalerte.internal.dto.RainForecast.Properties;
import org.openhab.binding.meteoalerte.internal.dto.RainForecast.RainIntensity;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PointType;
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
public class RainForecastHandler extends BaseThingHandler {
    private static final String URL = "https://rpcache-aa.meteofrance.com/internet2018client/2.0/nowcast/rain?lat=%.4f&lon=%.4f&token=__Wj7dVSTjV9YGu1guveLyDq0g7S7TfTjaHBTPTpO0kj8__";

    private final Logger logger = LoggerFactory.getLogger(RainForecastHandler.class);
    private final MeteoAlerteDeserializer deserializer;

    private Optional<ScheduledFuture<?>> refreshJob = Optional.empty();

    public RainForecastHandler(Thing thing, MeteoAlerteDeserializer deserializer) {
        super(thing);
        this.deserializer = deserializer;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Rain Forecast handler.");
        disposeJob();

        updateStatus(ThingStatus.UNKNOWN);
        refreshJob = Optional.of(scheduler.schedule(this::updateAndPublish, 2, TimeUnit.SECONDS));
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
        ForecastConfiguration config = getConfigAs(ForecastConfiguration.class);
        PointType location = new PointType(config.location);
        String url = URL.formatted(location.getLatitude().doubleValue(), location.getLongitude().doubleValue());

        try {
            String answer = HttpUtil.executeUrl(HttpMethod.GET, url, REQUEST_TIMEOUT_MS);
            logger.trace(answer);
            RainForecast forecast = deserializer.deserialize(RainForecast.class, answer);
            setProperties(forecast.properties);
            updateDate(UPDATE_TIME, forecast.updateTime);
            updateStatus(ThingStatus.ONLINE);
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        } catch (MeteoAlerteException e) {
            logger.warn("Exception deserializing API answer: {}", e.getMessage());
        }

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
        forecast.forEach(prevision -> {
            State state = prevision.rainIntensity() != RainIntensity.UNKNOWN
                    ? new DecimalType(prevision.rainIntensity().ordinal())
                    : UnDefType.UNDEF;
            Instant timestamp = prevision.time().toInstant();
            timeSeries.add(timestamp, state);
        });
        sendTimeSeries(new ChannelUID(getThing().getUID(), INTENSITY), timeSeries);

        ZonedDateTime now = ZonedDateTime.now();
        long until = now.until(forecast.get(0).time(), ChronoUnit.SECONDS);
        logger.debug("Refresh rain intensity forecast in : {}s", until);
        refreshJob = Optional.of(scheduler.schedule(this::updateAndPublish, until, TimeUnit.SECONDS));
    }

    private void updateDate(String channelId, @Nullable ZonedDateTime zonedDateTime) {
        if (isLinked(channelId)) {
            updateState(channelId, zonedDateTime != null ? new DateTimeType(zonedDateTime) : UnDefType.NULL);
        }
    }
}
