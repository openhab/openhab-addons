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
package org.openhab.binding.solarforecast.internal.solcast.handler;

import static org.openhab.binding.solarforecast.internal.SolarForecastBindingConstants.*;
import static org.openhab.binding.solarforecast.internal.solcast.SolcastConstants.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.openhab.binding.solarforecast.internal.actions.SolarForecast;
import org.openhab.binding.solarforecast.internal.actions.SolarForecastActions;
import org.openhab.binding.solarforecast.internal.actions.SolarForecastProvider;
import org.openhab.binding.solarforecast.internal.solcast.SolcastObject;
import org.openhab.binding.solarforecast.internal.solcast.SolcastObject.QueryMode;
import org.openhab.binding.solarforecast.internal.solcast.config.SolcastPlaneConfiguration;
import org.openhab.binding.solarforecast.internal.utils.Utils;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
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
public class SolcastPlaneHandler extends BaseThingHandler implements SolarForecastProvider {
    private final Logger logger = LoggerFactory.getLogger(SolcastPlaneHandler.class);
    private final HttpClient httpClient;
    private Optional<SolcastPlaneConfiguration> configuration = Optional.empty();
    private Optional<SolcastBridgeHandler> bridgeHandler = Optional.empty();
    private Optional<SolcastObject> forecast = Optional.empty();

    public SolcastPlaneHandler(Thing thing, HttpClient hc) {
        super(thing);
        httpClient = hc;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(SolarForecastActions.class);
    }

    @Override
    public void initialize() {
        SolcastPlaneConfiguration c = getConfigAs(SolcastPlaneConfiguration.class);
        configuration = Optional.of(c);

        // connect Bridge & Status
        Bridge bridge = getBridge();
        if (bridge != null) {
            BridgeHandler handler = bridge.getHandler();
            if (handler != null) {
                if (handler instanceof SolcastBridgeHandler sbh) {
                    bridgeHandler = Optional.of(sbh);
                    bridgeHandler.get().addPlane(this);
                    forecast = Optional.of(new SolcastObject(bridgeHandler.get()));
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "@text/solarforecast.plane.status.wrong-handler [\"" + handler + "\"]");
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/solarforecast.plane.status.bridge-handler-not-found");
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/solarforecast.plane.status.bridge-missing");
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
        if (command instanceof RefreshType) {
            fetchData();
        }
    }

    protected SolcastObject fetchData() {
        if (!forecast.get().isValid()) {
            String forecastUrl = String.format(FORECAST_URL, configuration.get().resourceId);
            String currentEstimateUrl = String.format(CURRENT_ESTIMATE_URL, configuration.get().resourceId);
            try {
                // get actual estimate
                Request estimateRequest = httpClient.newRequest(currentEstimateUrl);
                estimateRequest.header(HttpHeader.AUTHORIZATION, BEARER + bridgeHandler.get().getApiKey());
                ContentResponse crEstimate = estimateRequest.send();
                if (crEstimate.getStatus() == 200) {
                    SolcastObject localForecast = new SolcastObject(crEstimate.getContentAsString(),
                            Instant.now().plus(configuration.get().refreshInterval, ChronoUnit.MINUTES),
                            bridgeHandler.get());

                    // get forecast
                    Request forecastRequest = httpClient.newRequest(forecastUrl);
                    forecastRequest.header(HttpHeader.AUTHORIZATION, BEARER + bridgeHandler.get().getApiKey());
                    ContentResponse crForecast = forecastRequest.send();

                    if (crForecast.getStatus() == 200) {
                        localForecast.join(crForecast.getContentAsString());
                        setForecast(localForecast);
                        updateState(CHANNEL_RAW, StringType.valueOf(forecast.get().getRaw()));
                        updateStatus(ThingStatus.ONLINE);
                    } else {
                        logger.debug("{} Call {} failed {}", thing.getLabel(), forecastUrl, crForecast.getStatus());
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "@text/solarforecast.plane.status.http-status [\"" + crForecast.getStatus() + "\"]");
                    }
                } else {
                    logger.debug("{} Call {} failed {}", thing.getLabel(), currentEstimateUrl, crEstimate.getStatus());
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "@text/solarforecast.plane.status.http-status [\"" + crEstimate.getStatus() + "\"]");
                }
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                logger.debug("{} Call {} failed {}", thing.getLabel(), currentEstimateUrl, e.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        } // else use available forecast
        updateChannels(forecast.get());
        return forecast.get();
    }

    private void updateChannels(SolcastObject f) {
        ZonedDateTime now = ZonedDateTime.now(bridgeHandler.get().getTimeZone());
        updateState(CHANNEL_ACTUAL, Utils.getEnergyState(f.getActualValue(now, QueryMode.Estimation)));
        updateState(CHANNEL_ACTUAL_POWER, Utils.getPowerState(f.getActualPowerValue(now, QueryMode.Estimation)));
        updateState(CHANNEL_REMAINING, Utils.getEnergyState(f.getRemainingProduction(now, QueryMode.Estimation)));
        LocalDate nowDate = now.toLocalDate();
        updateState(CHANNEL_TODAY, Utils.getEnergyState(f.getDayTotal(nowDate, QueryMode.Estimation)));
        updateState(CHANNEL_DAY1, Utils.getEnergyState(f.getDayTotal(nowDate.plusDays(1), QueryMode.Estimation)));
        updateState(CHANNEL_DAY1_HIGH, Utils.getEnergyState(f.getDayTotal(nowDate.plusDays(1), QueryMode.Optimistic)));
        updateState(CHANNEL_DAY1_LOW, Utils.getEnergyState(f.getDayTotal(nowDate.plusDays(1), QueryMode.Pessimistic)));
        updateState(CHANNEL_DAY2, Utils.getEnergyState(f.getDayTotal(nowDate.plusDays(2), QueryMode.Estimation)));
        updateState(CHANNEL_DAY2_HIGH, Utils.getEnergyState(f.getDayTotal(nowDate.plusDays(2), QueryMode.Optimistic)));
        updateState(CHANNEL_DAY2_LOW, Utils.getEnergyState(f.getDayTotal(nowDate.plusDays(2), QueryMode.Pessimistic)));
        updateState(CHANNEL_DAY3, Utils.getEnergyState(f.getDayTotal(nowDate.plusDays(3), QueryMode.Estimation)));
        updateState(CHANNEL_DAY3_HIGH, Utils.getEnergyState(f.getDayTotal(nowDate.plusDays(3), QueryMode.Optimistic)));
        updateState(CHANNEL_DAY3_LOW, Utils.getEnergyState(f.getDayTotal(nowDate.plusDays(3), QueryMode.Pessimistic)));
        updateState(CHANNEL_DAY4, Utils.getEnergyState(f.getDayTotal(nowDate.plusDays(4), QueryMode.Estimation)));
        updateState(CHANNEL_DAY4_HIGH, Utils.getEnergyState(f.getDayTotal(nowDate.plusDays(4), QueryMode.Optimistic)));
        updateState(CHANNEL_DAY4_LOW, Utils.getEnergyState(f.getDayTotal(nowDate.plusDays(4), QueryMode.Pessimistic)));
        updateState(CHANNEL_DAY5, Utils.getEnergyState(f.getDayTotal(nowDate.plusDays(5), QueryMode.Estimation)));
        updateState(CHANNEL_DAY5_HIGH, Utils.getEnergyState(f.getDayTotal(nowDate.plusDays(5), QueryMode.Optimistic)));
        updateState(CHANNEL_DAY5_LOW, Utils.getEnergyState(f.getDayTotal(nowDate.plusDays(5), QueryMode.Pessimistic)));
        updateState(CHANNEL_DAY6, Utils.getEnergyState(f.getDayTotal(nowDate.plusDays(6), QueryMode.Estimation)));
        updateState(CHANNEL_DAY6_HIGH, Utils.getEnergyState(f.getDayTotal(nowDate.plusDays(6), QueryMode.Optimistic)));
        updateState(CHANNEL_DAY6_LOW, Utils.getEnergyState(f.getDayTotal(nowDate.plusDays(6), QueryMode.Pessimistic)));
    }

    private synchronized void setForecast(SolcastObject f) {
        forecast = Optional.of(f);
    }

    @Override
    public synchronized List<SolarForecast> getSolarForecasts() {
        return List.of(forecast.get());
    }
}
