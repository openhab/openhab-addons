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
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.json.JSONObject;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.HistoricItem;
import org.openhab.core.persistence.QueryablePersistenceService;
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
    private static final int MEASURE_INTERVAL_MIN = 15;
    private static final int MEASURE_OFFSET_MIN = 5;
    private final Logger logger = LoggerFactory.getLogger(SolcastPlaneHandler.class);
    private final HttpClient httpClient;
    private Optional<SolcastPlaneConfiguration> configuration = Optional.empty();
    private Optional<SolcastBridgeHandler> bridgeHandler = Optional.empty();
    private SolcastObject forecast = new SolcastObject();
    private Optional<QueryablePersistenceService> persistenceService;
    private ZonedDateTime nextMeasurement = ZonedDateTime.now();

    public SolcastPlaneHandler(Thing thing, HttpClient hc, Optional<QueryablePersistenceService> qps) {
        super(thing);
        httpClient = hc;
        persistenceService = qps;
    }

    @Override
    public void initialize() {
        SolcastPlaneConfiguration c = getConfigAs(SolcastPlaneConfiguration.class);
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
            logger.debug("{} Call {}", thing.getLabel(), currentEstimateUrl);
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
                    logger.debug("{} Call {}", thing.getLabel(), forecastUrl);
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
        if (ZonedDateTime.now().isAfter(nextMeasurement)) {
            nextMeasurement = ZonedDateTime.now().plusMinutes(MEASURE_INTERVAL_MIN);
            sendMeasure();
        }
        return forecast;
    }

    /**
     * https://legacy-docs.solcast.com.au/#measurements-rooftop-site
     */
    private void sendMeasure() {
        if (persistenceService.isPresent() && !EMPTY.equals(configuration.get().powerItem)) {
            logger.info("Get item {}", configuration.get().powerItem);
            ZonedDateTime now = ZonedDateTime.now();
            ZonedDateTime beginPeriodDT = now.minusMinutes(MEASURE_INTERVAL_MIN + MEASURE_OFFSET_MIN);
            ZonedDateTime endPeriodDT = now.minusMinutes(MEASURE_OFFSET_MIN);
            FilterCriteria fc = new FilterCriteria();
            fc.setBeginDate(beginPeriodDT);
            fc.setEndDate(endPeriodDT);
            fc.setItemName(configuration.get().powerItem);
            Iterable<HistoricItem> historicItems = persistenceService.get().query(fc);
            int count = 0;
            double total = 0;
            for (HistoricItem historicItem : historicItems) {
                // logger.info("Found {} item with average {} power", historicItem.getTimestamp(),
                // historicItem.getState().toFullString());
                DecimalType dt = historicItem.getState().as(DecimalType.class);
                if (dt != null) {
                    total += dt.doubleValue();
                }
                count++;
            }
            double power = Math.round(total * 1000.0 / count) / 1000.0;
            if (power > 0.001) {
                logger.info("Found {} items with average {} power", count, total / count);
                JSONObject measureObject = new JSONObject();
                JSONObject measure = new JSONObject();
                measure.put("period_end", endPeriodDT.format(DateTimeFormatter.ISO_INSTANT));
                measure.put("period", "PT" + MEASURE_INTERVAL_MIN + "M");
                measure.put("total_power", power);
                measureObject.put("measurement", measure);
                logger.info("Send {}", measureObject.toString());

                String measureUrl = String.format(MEASUREMENT_URL, configuration.get().resourceId);
                Request request = httpClient.POST(measureUrl);
                request.header(HttpHeader.AUTHORIZATION, BEARER + bridgeHandler.get().getApiKey());
                request.header(HttpHeader.CONTENT_TYPE, "application/json");
                request.content(new StringContentProvider(measureObject.toString()), "application/json");
                try {
                    ContentResponse crMeasure = request.send();
                    if (crMeasure.getStatus() == 200) {
                        logger.info("{} Call {} finished {}", thing.getLabel(), measureUrl,
                                crMeasure.getContentAsString());
                    } else {
                        logger.info("{} Call {} failed {} - {}", thing.getLabel(), measureUrl, crMeasure.getStatus(),
                                crMeasure.getContentAsString());
                    }
                } catch (InterruptedException | TimeoutException | ExecutionException e) {
                    logger.info("{} Call {} failed {}", thing.getLabel(), measureUrl, e.getMessage());
                }
            }
        } else {
            logger.info("Persistence empty");
        }
    }

    private void updateChannels(SolcastObject f) {
        updateState(CHANNEL_ACTUAL, SolcastObject.getStateObject(f.getActualValue(LocalDateTime.now())));
        updateState(CHANNEL_REMAINING, SolcastObject.getStateObject(f.getRemainingProduction(LocalDateTime.now())));
        updateState(CHANNEL_TODAY, SolcastObject.getStateObject(f.getDayTotal(LocalDateTime.now(), 0)));
        updateState(CHANNEL_DAY1, SolcastObject.getStateObject(f.getDayTotal(LocalDateTime.now(), 1)));
        updateState(CHANNEL_DAY1_HIGH, SolcastObject.getStateObject(f.getOptimisticDayTotal(LocalDateTime.now(), 1)));
        updateState(CHANNEL_DAY1_LOW, SolcastObject.getStateObject(f.getPessimisticDayTotal(LocalDateTime.now(), 1)));
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
}
