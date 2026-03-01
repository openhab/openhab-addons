/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.energyforecast.internal.handler;

import static org.openhab.binding.energyforecast.internal.EnergyForecastBindingConstants.*;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.energyforecast.internal.config.EnergyForecastConfiguration;
import org.openhab.binding.energyforecast.internal.dto.PriceInfo;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.TimeSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link EnergyForecastHandler} handles config, thing status and channel updates
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class EnergyForecastHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(EnergyForecastHandler.class);
    private final HttpClient httpClient;
    private final TimeZoneProvider tzp;
    private final StorageService storageService;

    private @Nullable ScheduledFuture<?> refreshJob;
    private @Nullable PriceInfo prices;

    private EnergyForecastConfiguration config = new EnergyForecastConfiguration();

    public EnergyForecastHandler(Thing thing, HttpClient client, StorageService storageService, TimeZoneProvider tzp) {
        super(thing);
        this.httpClient = client;
        this.storageService = storageService;
        this.tzp = tzp;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updatePriceChannels();
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(EnergyForecastConfiguration.class);
        if (checkConfig()) {
            prices = new PriceInfo(config, storageService.getStorage(thing.getUID().getAsString()), tzp);
            updateStatus(ThingStatus.UNKNOWN);
            refreshJob = scheduler.scheduleWithFixedDelay(this::refreshData, 0, config.refreshInterval,
                    TimeUnit.MINUTES);
        }
    }

    private boolean checkConfig() {
        if (config.token.isBlank()) {
            configError("@text/thing-status.energyforecast.token-empty");
            return false;
        }
        if (config.zone.isBlank()) {
            configError("@text/thing-status.energyforecast.zone-empty");
            return false;
        }
        return true;
    }

    private void configError(String reason) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, reason);
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> localRefreshJob = refreshJob;
        if (localRefreshJob != null) {
            localRefreshJob.cancel(true);
        }
    }

    @Override
    public void handleRemoval() {
        getPriceInfo().handleRemoval();
        super.handleRemoval();
    }

    private void refreshData() {
        if (!config.token.isEmpty()) {
            if (fetchEnergyForecastPrices()) {
                updatePriceChannels();
                getPriceInfo().consolidate();
            }
        }
    }

    private void updatePriceChannels() {
        Map<String, TimeSeries> seriesMap = getPriceInfo().getTimeSeries();

        // price group
        sendSeriesToChannel(CHANNEL_GROUP_PRICE, CHANNEL_PRICE_SERIES, seriesMap.get(CHANNEL_PRICE_SERIES));
        sendSeriesToChannel(CHANNEL_GROUP_PRICE, CHANNEL_PRICE_ORIGIN, seriesMap.get(CHANNEL_PRICE_ORIGIN));

        // metric group
        sendSeriesToChannel(CHANNEL_GROUP_METRIC, CHANNEL_METRIC_FORECAST, seriesMap.get(CHANNEL_METRIC_FORECAST));
        sendSeriesToChannel(CHANNEL_GROUP_METRIC, CHANNEL_METRIC_FORECAST_ERROR,
                seriesMap.get(CHANNEL_METRIC_FORECAST_ERROR));
        sendSeriesToChannel(CHANNEL_GROUP_METRIC, CHANNEL_METRIC_PERCENT_ERROR,
                seriesMap.get(CHANNEL_METRIC_PERCENT_ERROR));

        sendValueToChannel(CHANNEL_GROUP_METRIC, CHANNEL_METRIC_MAE, seriesMap.get(CHANNEL_METRIC_MAE));
        sendValueToChannel(CHANNEL_GROUP_METRIC, CHANNEL_METRIC_MAPE, seriesMap.get(CHANNEL_METRIC_MAPE));
    }

    private void sendSeriesToChannel(String group, String channel, @Nullable TimeSeries series) {
        if (series != null) {
            if (CHANNEL_METRIC_PERCENT_ERROR.equals(channel)) {
                TimeSeries limitSeries = new TimeSeries(TimeSeries.Policy.REPLACE);
                series.getStates().forEach(entry -> limitSeries.add(entry.timestamp(), applyLimit(entry.state())));
                sendTimeSeries(group + ChannelUID.CHANNEL_GROUP_SEPARATOR + channel, limitSeries);
            } else {
                sendTimeSeries(group + ChannelUID.CHANNEL_GROUP_SEPARATOR + channel, series);
            }
        } else {
            logger.warn("No data available for channel {} in group {}", channel, group);
        }
    }

    private void sendValueToChannel(String group, String channel, @Nullable TimeSeries series) {
        if (series != null) {
            Optional<TimeSeries.Entry> value = series.getStates().findFirst();
            value.ifPresent(entry -> {
                updateState(group + ChannelUID.CHANNEL_GROUP_SEPARATOR + channel, applyLimit(entry.state()));
            });
        }
    }

    private State applyLimit(State state) {
        if (config.errorLimit != 0) {
            if (state instanceof QuantityType quantity) {
                if (Units.PERCENT.equals(quantity.getUnit())) {
                    double originValue = quantity.doubleValue();
                    if (originValue > 0) {
                        return QuantityType.valueOf(Math.min(config.errorLimit, quantity.doubleValue()) + " %");
                    } else {
                        return QuantityType.valueOf(Math.max(-config.errorLimit, quantity.doubleValue()) + " %");
                    }
                }
            }
        }
        return state;
    }

    private boolean fetchEnergyForecastPrices() {
        try {
            Request forecastRequest = httpClient.newRequest(ENERGY_FORECAST_URL).timeout(10, TimeUnit.SECONDS);
            forecastRequest.param("fixed_cost_cent", "0");
            forecastRequest.param("vat", "0");
            forecastRequest.param("resolution", "PT15M".equals(config.resolution) ? "QUARTER_HOURLY" : "HOURLY ");
            forecastRequest.param("market_zone", config.zone);
            forecastRequest.param("token", config.token);

            ContentResponse response = forecastRequest.send();
            int status = response.getStatus();
            logger.info("Energy price forecast response: {}", status);
            if (status == HttpStatus.OK_200) {
                getPriceInfo().newPriceSeries(response.getContentAsString());
                updateStatus(ThingStatus.ONLINE);
                return true;
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, response.toString());
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
        return false;
    }

    private PriceInfo getPriceInfo() {
        PriceInfo localPrices = prices;
        if (localPrices == null) {
            throw new IllegalStateException("PriceInfo is not initialized");
        }
        return localPrices;
    }
}
