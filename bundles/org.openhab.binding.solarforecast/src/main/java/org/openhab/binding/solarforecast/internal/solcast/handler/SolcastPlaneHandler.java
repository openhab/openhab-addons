/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
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
import org.openhab.binding.solarforecast.internal.actions.SolarForecast;
import org.openhab.binding.solarforecast.internal.actions.SolarForecastActions;
import org.openhab.binding.solarforecast.internal.actions.SolarForecastProvider;
import org.openhab.binding.solarforecast.internal.solcast.SolcastObject;
import org.openhab.binding.solarforecast.internal.solcast.SolcastObject.QueryMode;
import org.openhab.binding.solarforecast.internal.solcast.config.SolcastPlaneConfiguration;
import org.openhab.binding.solarforecast.internal.utils.Utils;
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
    private Optional<SolcastObject> forecast = Optional.empty();
    private Optional<Instant> nextMeasurement = Optional.empty();

    public SolcastPlaneHandler(Thing thing, HttpClient hc, Optional<QueryablePersistenceService> qps, ItemRegistry ir) {
        super(thing);
        httpClient = hc;
        persistenceService = qps;
        itemRegistry = ir;
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
                    nextMeasurement = Optional.of(Utils.getNextTimeframe(Instant.now(), bridgeHandler.get()));
                    // initialize Power Item
                    if (!EMPTY.equals(c.powerItem)) {
                        // power item configured
                        Item item = itemRegistry.get(c.powerItem);
                        if (item != null) {
                            powerItem = Optional.of(item);
                            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE,
                                    "@text/solarforecast.plane.status.await-feedback");
                        } else {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                                    "@text/solarforecast.plane.status.power-item [\"" + c.powerItem + "\"]");
                        }
                    } else {
                        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE,
                                "@text/solarforecast.plane.status.await-feedback");
                    }
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
        if (Instant.now().isAfter(nextMeasurement.get())) {
            sendMeasure();
        }
        return forecast.get();
    }

    /**
     * https://legacy-docs.solcast.com.au/#measurements-rooftop-site
     */
    private void sendMeasure() {
        State updateState = UnDefType.UNDEF;
        if (persistenceService.isPresent() && powerItem.isPresent()) {
            ZonedDateTime beginPeriodDT = nextMeasurement.get().atZone(bridgeHandler.get().getTimeZone())
                    .minusMinutes(MEASURE_INTERVAL_MIN);
            ZonedDateTime endPeriodDT = nextMeasurement.get().atZone(bridgeHandler.get().getTimeZone());
            FilterCriteria fc = new FilterCriteria();
            fc.setBeginDate(beginPeriodDT);
            fc.setEndDate(endPeriodDT);
            fc.setItemName(configuration.get().powerItem);
            Iterable<HistoricItem> historicItems = persistenceService.get().query(fc);
            int count = 0;
            double total = 0;
            for (HistoricItem historicItem : historicItems) {
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
                        logger.trace("No valid Power unit detected {}", unitDetected.toString());
                        power = UNDEF_DOUBLE;
                    }
                } else {
                    logger.trace("No autodetection for State class {} possible", state.getClass());
                    power = UNDEF_DOUBLE;
                }
            } else if (Units.WATT.toString().equals(configuration.get().powerUnit)) {
                // scale to kW necessary, keep 3 digits after comma
                power = Math.round(power) / 1000.0;
            } else if (KILOWATT_UNIT.toString().equals(configuration.get().powerUnit)) {
                // just round and keep 3 digits after comma
                power = Math.round(power * 1000.0) / 1000.0;
            } else {
                logger.trace("No Unit conversion possible for {}", configuration.get().powerUnit);
                power = UNDEF_DOUBLE;
            }

            if (power >= 0) {
                JSONObject measureObject = new JSONObject();
                JSONObject measure = new JSONObject();
                measure.put("period_end", endPeriodDT.format(DateTimeFormatter.ISO_INSTANT));
                measure.put("period", "PT" + MEASURE_INTERVAL_MIN + "M");
                measure.put("total_power", power);
                measureObject.put("measurement", measure);
                logger.trace("Send {}", measureObject.toString());

                String measureUrl = String.format(MEASUREMENT_URL, configuration.get().resourceId);
                Request request = httpClient.POST(measureUrl);
                request.header(HttpHeader.AUTHORIZATION, BEARER + bridgeHandler.get().getApiKey());
                request.content(new StringContentProvider(measureObject.toString()));
                request.header(HttpHeader.CONTENT_TYPE, "application/json");
                try {
                    ContentResponse crMeasure = request.send();
                    if (crMeasure.getStatus() == 200) {
                        updateState = StringType.valueOf(crMeasure.getContentAsString());
                        updateStatus(ThingStatus.ONLINE);
                    } else {
                        logger.debug("{} Call {} failed {} - {}", thing.getLabel(), measureUrl, crMeasure.getStatus(),
                                crMeasure.getContentAsString());
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                    }
                } catch (InterruptedException | TimeoutException | ExecutionException e) {
                    logger.debug("{} Call {} failed {}", thing.getLabel(), measureUrl, e.getMessage());
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                }
            } else {
                logger.debug("Persistence for {} empty", powerItem);
            }
        }
        updateState(CHANNEL_RAW_TUNING, updateState);
        nextMeasurement = Optional.of(Utils.getNextTimeframe(Instant.now(), bridgeHandler.get()));
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
