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
package org.openhab.binding.solarforecast.internal.solcast;

import static org.openhab.binding.solarforecast.internal.SolarForecastBindingConstants.*;
import static org.openhab.binding.solarforecast.internal.solcast.SolcastConstants.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SolcastPlaneHandler} is a non active handler instance. It will be triggerer by the bridge.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class SolcastPlaneHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(SolcastPlaneHandler.class);
    private final HttpClient httpClient;

    private Optional<SolcastConfiguration> configuration = Optional.empty();
    private Optional<SolcastBridgeHandler> bridgeHandler = Optional.empty();
    private SolcastObject forecast = new SolcastObject();

    public SolcastPlaneHandler(Thing thing, HttpClient hc) {
        super(thing);
        httpClient = hc;
    }

    @Override
    public void initialize() {
        SolcastConfiguration c = getConfigAs(SolcastConfiguration.class);
        configuration = Optional.of(c);
        Bridge bridge = getBridge();
        if (bridge != null) {
            BridgeHandler handler = bridge.getHandler();
            if (handler != null) {
                if (handler instanceof SolcastBridgeHandler) {
                    bridgeHandler = Optional.of((SolcastBridgeHandler) handler);
                    bridgeHandler.get().addPlane(this);
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                            "Wrong Handler " + handler);
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED, "BridgeHandler not found");
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED, "Bridge not set");
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (bridgeHandler.isPresent()) {
            bridgeHandler.get().removePlane(this);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.info("Handle command {} for channel {}", channelUID, command);
        if (command instanceof RefreshType) {
            fetchData();
        }
    }

    /**
     * https://doc.forecast.solar/doku.php?id=api:estimate
     */
    protected SolcastObject fetchData() {
        if (!forecast.isValid()) {
            String forecastUrl = String.format(FORECAST_URL, configuration.get().resourceId);
            String currentEstimateUrl = String.format(CURRENT_ESTIMATE_URL, configuration.get().resourceId);
            logger.info("{} Call {}", thing.getLabel(), currentEstimateUrl);
            try {
                // get actual estimate
                Request estimateRequest = httpClient.newRequest(currentEstimateUrl);
                estimateRequest.header(HttpHeader.AUTHORIZATION, BEARER + bridgeHandler.get().getApiKey());
                ContentResponse crEstimate = estimateRequest.send();
                if (crEstimate.getStatus() == 200) {
                    forecast = new SolcastObject(crEstimate.getContentAsString(),
                            LocalDateTime.now().plusMinutes(configuration.get().refreshInterval));
                    logger.trace("{} Fetched data {}", thing.getLabel(), forecast.toString());

                    // get forecast
                    logger.info("{} Call {}", thing.getLabel(), forecastUrl);
                    Request forecastRequest = httpClient.newRequest(forecastUrl);
                    forecastRequest.header(HttpHeader.AUTHORIZATION, BEARER + bridgeHandler.get().getApiKey());
                    ContentResponse crForecast = forecastRequest.send();

                    if (crForecast.getStatus() == 200) {
                        forecast.join(crForecast.getContentAsString());
                        logger.trace("{} Fetched data {}", thing.getLabel(), forecast.toString());
                        updateChannels(forecast);
                        updateState(CHANNEL_RAW, StringType.valueOf(forecast.getRaw()));
                    } else {
                        logger.info("{} Call {} failed {}", thing.getLabel(), forecastUrl, crForecast.getStatus());
                    }
                    updateChannels(forecast);
                    updateState(CHANNEL_RAW, StringType.valueOf(forecast.getRaw()));
                } else {
                    logger.info("{} Call {} failed {}", thing.getLabel(), currentEstimateUrl, crEstimate.getStatus());
                }
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                logger.info("{} Call {} failed {}", thing.getLabel(), currentEstimateUrl, e.getMessage());
            }
        } else {
            logger.debug("{} use available forecast {}", thing.getLabel(), forecast);
        }
        updateChannels(forecast);
        return forecast;
    }

    private void updateChannels(SolcastObject f) {
        updateState(CHANNEL_ACTUAL, SolcastObject.getStateObject(f.getActualValue(LocalDateTime.now())));
        updateState(CHANNEL_REMAINING, SolcastObject.getStateObject(f.getRemainingProduction(LocalDateTime.now())));
        updateState(CHANNEL_TODAY, SolcastObject.getStateObject(f.getDayTotal(LocalDateTime.now(), 0)));
        updateState(CHANNEL_TOMORROW, SolcastObject.getStateObject(f.getDayTotal(LocalDateTime.now(), 1)));
        updateState(CHANNEL_TOMORROW_HIGH,
                SolcastObject.getStateObject(f.getOptimisticDayTotal(LocalDateTime.now(), 1)));
        updateState(CHANNEL_TOMORROW_LOW,
                SolcastObject.getStateObject(f.getPessimisticDayTotal(LocalDateTime.now(), 1)));
        updateState(CHANNEL_DAY2, SolcastObject.getStateObject(f.getDayTotal(LocalDateTime.now(), 2)));
        updateState(CHANNEL_DAY2_HIGH, SolcastObject.getStateObject(f.getOptimisticDayTotal(LocalDateTime.now(), 2)));
        updateState(CHANNEL_DAY2_LOW, SolcastObject.getStateObject(f.getPessimisticDayTotal(LocalDateTime.now(), 2)));
        updateState(CHANNEL_DAY3, SolcastObject.getStateObject(f.getDayTotal(LocalDateTime.now(), 3)));
        updateState(CHANNEL_DAY3_HIGH, SolcastObject.getStateObject(f.getOptimisticDayTotal(LocalDateTime.now(), 3)));
        updateState(CHANNEL_DAY3_LOW, SolcastObject.getStateObject(f.getPessimisticDayTotal(LocalDateTime.now(), 3)));
        updateState(CHANNEL_DAY4, SolcastObject.getStateObject(f.getDayTotal(LocalDateTime.now(), 4)));
        updateState(CHANNEL_DAY4_HIGH, SolcastObject.getStateObject(f.getOptimisticDayTotal(LocalDateTime.now(), 4)));
        updateState(CHANNEL_DAY4_LOW, SolcastObject.getStateObject(f.getPessimisticDayTotal(LocalDateTime.now(), 4)));
        updateState(CHANNEL_DAY5, SolcastObject.getStateObject(f.getDayTotal(LocalDateTime.now(), 5)));
        updateState(CHANNEL_DAY5_HIGH, SolcastObject.getStateObject(f.getOptimisticDayTotal(LocalDateTime.now(), 5)));
        updateState(CHANNEL_DAY5_LOW, SolcastObject.getStateObject(f.getPessimisticDayTotal(LocalDateTime.now(), 5)));
        updateState(CHANNEL_DAY6, SolcastObject.getStateObject(f.getDayTotal(LocalDateTime.now(), 6)));
        updateState(CHANNEL_DAY6_HIGH, SolcastObject.getStateObject(f.getOptimisticDayTotal(LocalDateTime.now(), 6)));
        updateState(CHANNEL_DAY6_LOW, SolcastObject.getStateObject(f.getPessimisticDayTotal(LocalDateTime.now(), 6)));
        updateState(CHANNEL_RAW, StringType.valueOf(forecast.getRaw()));
    }

    public void setConfig(SolcastConfiguration config) {
        configuration = Optional.of(config);
    }
}
