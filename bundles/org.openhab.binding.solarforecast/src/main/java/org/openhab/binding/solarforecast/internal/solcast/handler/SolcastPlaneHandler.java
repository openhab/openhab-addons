/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
import java.time.ZoneId;
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
import org.json.JSONArray;
import org.json.JSONObject;
import org.openhab.binding.solarforecast.internal.actions.SolarForecast;
import org.openhab.binding.solarforecast.internal.actions.SolarForecastActions;
import org.openhab.binding.solarforecast.internal.actions.SolarForecastProvider;
import org.openhab.binding.solarforecast.internal.solcast.SolcastObject;
import org.openhab.binding.solarforecast.internal.solcast.SolcastObject.QueryMode;
import org.openhab.binding.solarforecast.internal.solcast.config.SolcastPlaneConfiguration;
import org.openhab.binding.solarforecast.internal.utils.Utils;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.storage.Storage;
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
 * The {@link SolcastPlaneHandler} is a non active handler instance. It will be
 * triggered by the bridge.
 *
 * @author Bernd Weymann - Initial contribution
 */

@NonNullByDefault
public class SolcastPlaneHandler extends BaseThingHandler implements SolarForecastProvider {
    public static final String CALL_COUNT_APPENDIX = "-count";
    public static final String CALL_COUNT_DATE_APPENDIX = "-count-date";

    protected Optional<SolcastObject> currentForecastOptional = Optional.empty();

    private final Logger logger = LoggerFactory.getLogger(SolcastPlaneHandler.class);
    private final HttpClient httpClient;
    private SolcastPlaneConfiguration configuration = new SolcastPlaneConfiguration();
    private Optional<SolcastBridgeHandler> bridgeHandler = Optional.empty();
    private Storage<String> storage;
    private Instant lastReset = Utils.now();
    private JSONObject counterJson;

    public SolcastPlaneHandler(Thing thing, HttpClient hc, Storage<String> storage) {
        super(thing);
        httpClient = hc;
        this.storage = storage;
        counterJson = getNewCounter();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(SolarForecastActions.class);
    }

    @Override
    public void initialize() {
        configuration = getConfigAs(SolcastPlaneConfiguration.class);

        String counterString = storage.get(thing.getUID() + CALL_COUNT_APPENDIX);
        if (counterString != null) {
            try {
                counterJson = new JSONObject(counterString);
            } catch (Exception e) {
                counterJson = getNewCounter();
                int persistenceCount = Integer.valueOf(counterString);
                counterJson.put("200", persistenceCount);
            }
        } else {
            counterJson = getNewCounter();
        }
        String lastResetString = storage.get(thing.getUID() + CALL_COUNT_DATE_APPENDIX);
        if (lastResetString != null) {
            lastReset = Instant.parse(lastResetString);
        }
        // immediately check counter if it's still valid
        checkCount();

        // connect Bridge & Status
        Bridge bridge = getBridge();
        if (bridge != null) {
            BridgeHandler handler = bridge.getHandler();
            if (handler != null) {
                if (handler instanceof SolcastBridgeHandler sbh) {
                    bridgeHandler = Optional.of(sbh);
                    Instant expiration = (configuration.refreshInterval == 0) ? Instant.MAX
                            : Utils.now().minusSeconds(1);
                    SolcastObject forecast = new SolcastObject(thing.getUID().getAsString(), null, expiration, sbh,
                            storage);
                    currentForecastOptional = Optional.of(forecast);
                    sbh.addPlane(this);
                    // in case of successful forecast restore from persistence update time series and get ONLINE
                    // immediately
                    if (!forecast.isExpired()) {
                        setForecast(forecast);
                        updateStatus(ThingStatus.ONLINE);
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
        bridgeHandler.ifPresent(bridge -> bridge.removePlane(this));
        storage.put(thing.getUID() + CALL_COUNT_DATE_APPENDIX, lastReset.toString());
        storage.put(thing.getUID() + CALL_COUNT_APPENDIX, counterJson.toString());
    }

    @Override
    public void handleRemoval() {
        storage.remove(thing.getUID() + CALL_COUNT_APPENDIX);
        storage.remove(thing.getUID() + CALL_COUNT_DATE_APPENDIX);
        storage.remove(thing.getUID() + SolcastObject.FORECAST_APPENDIX);
        storage.remove(thing.getUID() + SolcastObject.CREATION_APPENDIX);
        storage.remove(thing.getUID() + SolcastObject.EXPIRATION_APPENDIX);
        super.handleRemoval();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            checkCount();
            if (CHANNEL_API_COUNT.equals(channelUID.getIdWithoutGroup())) {
                updateState(CHANNEL_API_COUNT, StringType.valueOf(counterJson.toString()));
            } else {
                currentForecastOptional.ifPresent(forecastObject -> {
                    String group = channelUID.getGroupId();
                    if (group == null) {
                        group = EMPTY;
                    }
                    String channel = channelUID.getIdWithoutGroup();
                    QueryMode mode = QueryMode.Average;
                    switch (group) {
                        case GROUP_AVERAGE:
                            mode = QueryMode.Average;
                            break;
                        case GROUP_OPTIMISTIC:
                            mode = QueryMode.Optimistic;
                            break;
                        case GROUP_PESSIMISTIC:
                            mode = QueryMode.Pessimistic;
                            break;
                        case GROUP_RAW:
                            currentForecastOptional.ifPresent(f -> {
                                updateState(GROUP_RAW + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_JSON,
                                        StringType.valueOf(f.getRaw().toString()));
                            });
                    }
                    switch (channel) {
                        case CHANNEL_ENERGY_ESTIMATE:
                            sendTimeSeries(group + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_ENERGY_ESTIMATE,
                                    forecastObject.getEnergyTimeSeries(mode));
                            break;
                        case CHANNEL_POWER_ESTIMATE:
                            sendTimeSeries(group + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_POWER_ESTIMATE,
                                    forecastObject.getPowerTimeSeries(mode));
                            break;
                        default:
                            updateChannels(forecastObject);
                    }
                });
            }
        }
    }

    protected synchronized SolcastObject fetchData() {
        // check count, maybe fetching will increase counter
        checkCount();
        bridgeHandler.ifPresent(bridge -> {
            currentForecastOptional.ifPresent(forecastObject -> {
                if (forecastObject.isExpired()) {
                    logger.trace("{} Forecast expired -> get new forecast", thing.getUID());
                    JSONArray actuals = null;
                    // Step 1 - try to get actual values from current forecast object
                    if (forecastObject.getForecastBegin() != Instant.MAX
                            && forecastObject.getForecastEnd() != Instant.MIN && configuration.guessActuals) {
                        // get todays values and if they are complete use them as actual values
                        actuals = getTodaysValues(forecastObject.getRaw());
                        int valuesToday = actuals.length();
                        if (valuesToday < 48) {
                            // we didn't get all actual values, so we can't use this forecast
                            actuals = null;
                            logger.trace("{} Forecast valid but not for whole day. Only found {} values for today",
                                    thing.getUID(), valuesToday);
                        } else {
                            logger.trace("{} Guessing with {} forecasts as new actuals", thing.getUID(),
                                    actuals.length());
                        }
                    }
                    try {
                        // Step 2 - if step 1 didn't succeed request needs to be placed
                        if (actuals == null) {
                            String currentEstimateUrl = String.format(CURRENT_ESTIMATE_URL, configuration.resourceId);
                            logger.trace("{} We have no actual values - need to fetch", thing.getUID());
                            Request estimateRequest = httpClient.newRequest(currentEstimateUrl);
                            estimateRequest.header(HttpHeader.AUTHORIZATION, BEARER + bridge.getApiKey());
                            ContentResponse crEstimate = estimateRequest.send();
                            int callStatus = crEstimate.getStatus();
                            count(callStatus);
                            if (callStatus == 200) {
                                JSONObject actualJson = new JSONObject(crEstimate.getContentAsString());
                                actuals = actualJson.getJSONArray(KEY_ACTUALS);
                            } else {
                                apiCallFailure(currentEstimateUrl, crEstimate.getStatus());
                                return;
                            }
                        }
                        // Step 3 - request forecast values and
                        String forecastUrl = String.format(FORECAST_URL, configuration.resourceId);
                        Request forecastRequest = httpClient.newRequest(forecastUrl);
                        forecastRequest.header(HttpHeader.AUTHORIZATION, BEARER + bridge.getApiKey());
                        ContentResponse crForecast = forecastRequest.send();
                        int callStatus = crForecast.getStatus();
                        count(callStatus);

                        if (callStatus == 200) {
                            JSONObject forecastJson = new JSONObject(crForecast.getContentAsString());
                            JSONArray forecast = mergeArrays(actuals, forecastJson.getJSONArray(KEY_FORECAST));
                            Instant expiration = (configuration.refreshInterval == 0) ? Instant.MAX
                                    : Utils.now().plus(configuration.refreshInterval, ChronoUnit.MINUTES);
                            SolcastObject localForecast = new SolcastObject(thing.getUID().getAsString(), forecast,
                                    expiration, bridge, storage);
                            setForecast(localForecast);
                            updateState(GROUP_RAW + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_JSON,
                                    StringType.valueOf(forecast.toString()));
                            updateStatus(ThingStatus.ONLINE);
                        } else {
                            apiCallFailure(forecastUrl, crForecast.getStatus());
                        }
                    } catch (ExecutionException | TimeoutException e) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                    } catch (InterruptedException e) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                        Thread.currentThread().interrupt();
                    }
                } else {
                    updateChannels(forecastObject);
                    updateStatus(ThingStatus.ONLINE);
                }
            });
        });
        return currentForecastOptional.get();
    }

    private void count(int status) {
        // check first regarding day switch before increasing
        switch (status) {
            case 200:
                int new200Count = counterJson.getInt("200") + 1;
                counterJson.put("200", new200Count);
                break;
            case 429:
                int new429Count = counterJson.getInt("429") + 1;
                counterJson.put("429", new429Count);
                break;
            default:
                int newOtherCount = counterJson.getInt("other") + 1;
                counterJson.put("other", newOtherCount);
                break;
        }
        checkCount();
    }

    private void checkCount() {
        Instant now = Utils.now();
        if (lastReset.atZone(ZoneId.of("UTC")).getDayOfMonth() != now.atZone(ZoneId.of("UTC")).getDayOfMonth()) {
            counterJson = getNewCounter();
            lastReset = now;
        }
    }

    private JSONObject getNewCounter() {
        return new JSONObject("{\"200\":0,\"429\":0,\"other\":0}");
    }

    public JSONObject getCounter() {
        return counterJson;
    }

    /**
     * Get todays forecast values according to configured time zone
     *
     * @param wholeForecast
     * @return JSONArray
     */
    protected static JSONArray getTodaysValues(JSONArray wholeForecast) {
        JSONArray todaysValuesArray = new JSONArray();
        LocalDate today = ZonedDateTime.now(Utils.getClock()).toLocalDate();
        wholeForecast.forEach(entry -> {
            JSONObject forecastJson = (JSONObject) entry;
            String periodEnd = forecastJson.getString(KEY_PERIOD_END);
            ZonedDateTime periodEndZdt = Utils.getZdtFromUTC(periodEnd);
            if (periodEndZdt != null) {
                if (periodEndZdt.toLocalDate().equals(today)) {
                    todaysValuesArray.put(entry);
                }
            }
        });
        return todaysValuesArray;
    }

    /**
     * Merge JSON arrays and avoid double value entries of PERIOD_END.
     * 1) take all forecast values
     * 2) only add actual values missing in the forecast
     *
     * @param actuals
     * @param forecast
     * @return combined array
     */
    protected static JSONArray mergeArrays(JSONArray actuals, JSONArray forecast) {
        JSONArray uniqueForecast = (new JSONArray()).putAll(forecast);
        for (int i = 0; i < actuals.length(); i++) {
            JSONObject actualValue = actuals.getJSONObject(i);
            String actualPeriod = actualValue.getString(KEY_PERIOD_END);
            boolean found = false;
            for (int j = 0; j < forecast.length(); j++) {
                JSONObject forecastValue = forecast.getJSONObject(j);
                String forecastPeriod = forecastValue.getString(KEY_PERIOD_END);
                if (forecastPeriod.equals(actualPeriod)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                uniqueForecast.put(actualValue);
            }
        }
        return uniqueForecast;
    }

    private void apiCallFailure(String url, int status) {
        logger.debug("{} Call {} failed {}", thing.getLabel(), url, status);
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                "@text/solarforecast.plane.status.http-status [\"" + status + "\"]");
    }

    /**
     * Update channels frequently with new data
     *
     * @param Forecast object
     */
    protected void updateChannels(SolcastObject f) {
        if (bridgeHandler.isEmpty()) {
            return;
        }
        ZonedDateTime now = ZonedDateTime.now(Utils.getClock());
        MODES.forEach(mode -> {
            double energyDay = f.getDayTotal(now.toLocalDate(), mode);
            double energyProduced = f.getActualEnergyValue(now, mode);
            String group = switch (mode) {
                case Average -> GROUP_AVERAGE;
                case Optimistic -> GROUP_OPTIMISTIC;
                case Pessimistic -> GROUP_PESSIMISTIC;
                case Error -> throw new IllegalStateException("mode " + mode + " not expected");
            };
            updateState(group + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_ENERGY_ACTUAL,
                    Utils.getEnergyState(energyProduced));
            updateState(group + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_ENERGY_REMAIN,
                    Utils.getEnergyState(energyDay - energyProduced));
            updateState(group + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_ENERGY_TODAY,
                    Utils.getEnergyState(energyDay));
            updateState(group + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_POWER_ACTUAL,
                    Utils.getPowerState(f.getActualPowerValue(now, QueryMode.Average)));
        });
        updateState(GROUP_UPDATE + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_API_COUNT,
                StringType.valueOf(counterJson.toString()));
        ZonedDateTime creation = Utils.getZdtFromUTC(f.getCreationInstant());
        updateState(GROUP_UPDATE + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_LATEST_UPDATE,
                new DateTimeType(creation));
    }

    /**
     * Update time series only if new forecast object is created
     *
     * @param Forecast object
     */
    protected synchronized void setForecast(SolcastObject f) {
        currentForecastOptional = Optional.of(f);
        sendTimeSeries(GROUP_AVERAGE + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_POWER_ESTIMATE,
                f.getPowerTimeSeries(QueryMode.Average));
        sendTimeSeries(GROUP_AVERAGE + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_ENERGY_ESTIMATE,
                f.getEnergyTimeSeries(QueryMode.Average));
        sendTimeSeries(GROUP_OPTIMISTIC + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_POWER_ESTIMATE,
                f.getPowerTimeSeries(QueryMode.Optimistic));
        sendTimeSeries(GROUP_OPTIMISTIC + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_ENERGY_ESTIMATE,
                f.getEnergyTimeSeries(QueryMode.Optimistic));
        sendTimeSeries(GROUP_PESSIMISTIC + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_POWER_ESTIMATE,
                f.getPowerTimeSeries(QueryMode.Pessimistic));
        sendTimeSeries(GROUP_PESSIMISTIC + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_ENERGY_ESTIMATE,
                f.getEnergyTimeSeries(QueryMode.Pessimistic));
        bridgeHandler.ifPresent(h -> {
            h.forecastUpdate();
        });
    }

    @Override
    public synchronized List<SolarForecast> getSolarForecasts() {
        return List.of(currentForecastOptional.get());
    }
}
