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

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.json.JSONObject;
import org.openhab.binding.solarforecast.internal.Utils;
import org.openhab.binding.solarforecast.internal.actions.SolarForecast;
import org.openhab.binding.solarforecast.internal.actions.SolarForecastActions;
import org.openhab.binding.solarforecast.internal.actions.SolarForecastProvider;
import org.openhab.binding.solarforecast.internal.solcast.SolcastObject.QueryMode;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
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
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SolcastPlaneHandler} is a non active handler instance. It will be triggerer by the bridge.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class SolcastPlaneHandler extends BaseThingHandler implements SolarForecastProvider {
    private static final int MEASURE_INTERVAL_MIN = 15;
    private final Logger logger = LoggerFactory.getLogger(SolcastPlaneHandler.class);
    private final HttpClient httpClient;
    private final ItemRegistry itemRegistry;
    private Optional<SolcastPlaneConfiguration> configuration = Optional.empty();
    private Optional<SolcastBridgeHandler> bridgeHandler = Optional.empty();
    private Optional<Item> powerItem = Optional.empty();
    private Optional<QueryablePersistenceService> persistenceService;
    private SolcastObject forecast = new SolcastObject();
    private ZonedDateTime nextMeasurement;

    public SolcastPlaneHandler(Thing thing, HttpClient hc, Optional<QueryablePersistenceService> qps, ItemRegistry ir) {
        super(thing);
        httpClient = hc;
        persistenceService = qps;
        itemRegistry = ir;
        nextMeasurement = Utils.getNextTimeframe(ZonedDateTime.now(SolcastConstants.zonedId));
        logger.debug("{} Constructor", thing.getLabel());
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(SolarForecastActions.class);
    }

    @Override
    public void initialize() {
        logger.debug("{} initialize", thing.getLabel());
        SolcastPlaneConfiguration c = getConfigAs(SolcastPlaneConfiguration.class);
        configuration = Optional.of(c);

        // initialize Power Item
        if (!EMPTY.equals(c.powerItem)) {
            // power item configured
            Item item = itemRegistry.get(c.powerItem);
            if (item != null) {
                powerItem = Optional.of(item);
            } else {
                logger.info("Item {} not found", c.powerItem);
            }
        } else {
            logger.debug("No Power item configured");
        }

        // connect Bridge & Status
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
        logger.trace("Handle command {} for channel {}", channelUID, command);
        if (command instanceof RefreshType) {
            fetchData();
        }
    }

    /**
     * https://doc.forecast.solar/doku.php?id=api:estimate
     */
    protected SolcastObject fetchData() {
        logger.debug("{} fetch data", thing.getLabel());
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
                    SolcastObject localForecast = new SolcastObject(crEstimate.getContentAsString(), ZonedDateTime
                            .now(SolcastConstants.zonedId).plusMinutes(configuration.get().refreshInterval));
                    logger.trace("{} Fetched data {}", thing.getLabel(), localForecast.toString());

                    // get forecast
                    logger.debug("{} Call {}", thing.getLabel(), forecastUrl);
                    Request forecastRequest = httpClient.newRequest(forecastUrl);
                    forecastRequest.header(HttpHeader.AUTHORIZATION, BEARER + bridgeHandler.get().getApiKey());
                    ContentResponse crForecast = forecastRequest.send();

                    if (crForecast.getStatus() == 200) {
                        localForecast.join(crForecast.getContentAsString());
                        setForecast(localForecast);
                        updateState(CHANNEL_RAW, StringType.valueOf(forecast.getRaw()));
                        logger.trace("{} Fetched data {}", thing.getLabel(), forecast.toString());
                    } else {
                        logger.info("{} Call {} failed {}", thing.getLabel(), forecastUrl, crForecast.getStatus());
                    }
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
        if (ZonedDateTime.now(SolcastConstants.zonedId).isAfter(nextMeasurement)) {
            sendMeasure();
        }
        return forecast;
    }

    /**
     * https://legacy-docs.solcast.com.au/#measurements-rooftop-site
     */
    private void sendMeasure() {
        State updateState = UnDefType.UNDEF;
        if (persistenceService.isPresent() && powerItem.isPresent()) {
            logger.debug("Get item {}", configuration.get().powerItem);
            ZonedDateTime beginPeriodDT = nextMeasurement.minusMinutes(MEASURE_INTERVAL_MIN);
            ZonedDateTime endPeriodDT = nextMeasurement;
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
            double power = total / count;

            // detect unit
            if (AUTODETECT.equals(configuration.get().powerUnit)) {
                State state = powerItem.get().getState();
                if (state instanceof QuantityType<?>) {
                    Unit<?> unitDetected = ((QuantityType<?>) state).getUnit();
                    if (Units.WATT.toString().equals(unitDetected.toString())) {
                        // scale to kW necessary, keep 3 digits after comma
                        power = Math.round(power) / 1000.0;
                    } else if (KILOWATT_UNIT.toString().equals(unitDetected.toString())) {
                        // just round and keep 3 digits after comma
                        power = Math.round(power * 1000.0) / 1000.0;
                    } else {
                        logger.info("No Power unit detected - result is {}", unitDetected.toString());
                        power = UNDEF_DOUBLE;
                    }
                } else {
                    logger.info("No autodetection for State class {} possible", state.getClass());
                    power = UNDEF_DOUBLE;
                }
            } else if (Units.WATT.toString().equals(configuration.get().powerUnit)) {
                // scale to kW necessary, keep 3 digits after comma
                power = Math.round(power) / 1000.0;
            } else if (KILOWATT_UNIT.toString().equals(configuration.get().powerUnit)) {
                // just round and keep 3 digits after comma
                power = Math.round(power * 1000.0) / 1000.0;
            } else {
                logger.info("No Unit conversion possible for {}", configuration.get().powerUnit);
                power = UNDEF_DOUBLE;
            }

            if (power >= 0) {
                logger.debug("Found {} items with average {} power", count, total / count);
                JSONObject measureObject = new JSONObject();
                JSONObject measure = new JSONObject();
                measure.put("period_end", endPeriodDT.format(DateTimeFormatter.ISO_INSTANT));
                measure.put("period", "PT" + MEASURE_INTERVAL_MIN + "M");
                measure.put("total_power", power);
                measureObject.put("measurement", measure);
                logger.debug("Send {}", measureObject.toString());

                String measureUrl = String.format(MEASUREMENT_URL, configuration.get().resourceId);
                Request request = httpClient.POST(measureUrl);
                request.header(HttpHeader.AUTHORIZATION, BEARER + bridgeHandler.get().getApiKey());
                request.content(new StringContentProvider(measureObject.toString()));
                request.header(HttpHeader.CONTENT_TYPE, "application/json");
                try {
                    ContentResponse crMeasure = request.send();
                    if (crMeasure.getStatus() == 200) {
                        logger.debug("{} Call {} finished {}", thing.getLabel(), measureUrl,
                                crMeasure.getContentAsString());
                        updateState = StringType.valueOf(crMeasure.getContentAsString());
                    } else {
                        logger.info("{} Call {} failed {} - {}", thing.getLabel(), measureUrl, crMeasure.getStatus(),
                                crMeasure.getContentAsString());
                    }
                } catch (InterruptedException | TimeoutException | ExecutionException e) {
                    logger.info("{} Call {} failed {}", thing.getLabel(), measureUrl, e.getMessage());
                }
            } else {
                logger.info("Persistence empty");
            }
        }
        updateState(CHANNEL_RAW_TUNING, updateState);
        nextMeasurement = Utils.getNextTimeframe(ZonedDateTime.now(SolcastConstants.zonedId));
    }

    private void updateChannels(SolcastObject f) {
        ZonedDateTime now = ZonedDateTime.now(SolcastConstants.zonedId);
        updateState(CHANNEL_ACTUAL, Utils.getEnergyState(f.getActualValue(now, QueryMode.Estimation)));
        updateState(CHANNEL_ACTUAL_POWER, Utils.getEnergyState(f.getActualPowerValue(now, QueryMode.Estimation)));
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
        logger.debug("{} Forecast set", thing.getLabel());
        forecast = f;
    }

    @Override
    public synchronized List<SolarForecast> getSolarForecasts() {
        return List.of(forecast);
    }
}
