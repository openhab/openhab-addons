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
import org.openhab.core.library.types.DecimalType;
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
 * triggerer by the bridge.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class SolcastPlaneHandler extends BaseThingHandler implements SolarForecastProvider {
    private final Logger logger = LoggerFactory.getLogger(SolcastPlaneHandler.class);
    private final HttpClient httpClient;
    private SolcastPlaneConfiguration configuration = new SolcastPlaneConfiguration();
    private Optional<SolcastBridgeHandler> bridgeHandler = Optional.empty();
    private Storage<String> storage;
    private Instant lastReset = Instant.now();
    private int apiRequestCounter = 0;
    protected Optional<SolcastObject> forecastOptional = Optional.empty();

    public SolcastPlaneHandler(Thing thing, HttpClient hc, Storage<String> storage) {
        super(thing);
        httpClient = hc;
        this.storage = storage;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(SolarForecastActions.class);
    }

    @Override
    public void initialize() {
        configuration = getConfigAs(SolcastPlaneConfiguration.class);
        // connect Bridge & Status
        Bridge bridge = getBridge();
        if (bridge != null) {
            BridgeHandler handler = bridge.getHandler();
            if (handler != null) {
                if (handler instanceof SolcastBridgeHandler sbh) {
                    bridgeHandler = Optional.of(sbh);
                    Instant expiration = (configuration.refreshInterval == 0) ? Instant.MAX
                            : Instant.now(Utils.getClock()).minusSeconds(1);
                    SolcastObject forecast = new SolcastObject(thing.getUID().getAsString(), expiration, sbh, storage);
                    forecastOptional = Optional.of(forecast);
                    sbh.addPlane(this);
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
    }

    @Override
    public void handleRemoval() {
        super.handleRemoval();
        storage.remove(thing.getUID() + SolcastObject.FORECAST_APPENDIX);
        storage.remove(thing.getUID() + SolcastObject.FORECAST_APPENDIX);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            forecastOptional.ifPresent(forecastObject -> {
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
                        forecastOptional.ifPresent(f -> {
                            updateState(GROUP_RAW + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_JSON,
                                    StringType.valueOf(f.getRaw()));
                        });
                }
                switch (channel) {
                    case CHANNEL_ENERGY_ESTIMATE:
                        sendTimeSeries(CHANNEL_ENERGY_ESTIMATE, forecastObject.getEnergyTimeSeries(mode));
                        break;
                    case CHANNEL_POWER_ESTIMATE:
                        sendTimeSeries(CHANNEL_POWER_ESTIMATE, forecastObject.getPowerTimeSeries(mode));
                        break;
                    default:
                        updateChannels(forecastObject);
                }
            });
        }
    }

    protected synchronized SolcastObject fetchData() {
        bridgeHandler.ifPresent(bridge -> {
            forecastOptional.ifPresent(forecastObject -> {
                if (forecastObject.isExpired()) {
                    logger.trace("[REDUCE] Forecast expired -> get new forecast");
                    String forecastUrl = String.format(FORECAST_URL, configuration.resourceId);
                    String currentEstimateUrl = String.format(CURRENT_ESTIMATE_URL, configuration.resourceId);
                    try {
                        JSONObject forecast = null;
                        if (forecastObject.getForecastBegin() != Instant.MAX
                                && forecastObject.getForecastEnd() != Instant.MIN) {
                            // we found a forecast with valid data
                            forecast = getTodaysValues(forecastObject.getRaw());
                            int valuesToday = forecast.getJSONArray(KEY_ACTUALS).length();
                            if (valuesToday != 48) {
                                // sorry, we didn't get all actuals, so we can't use this forecast
                                forecast = null;
                                logger.trace(
                                        "[REDUCE] Forecast valid but not for whole day. Only found {} values for today",
                                        valuesToday);
                            } else {
                                logger.trace("[REDUCE] Guessing with forecast as new actuals {}", forecast.toString());
                            }
                        }
                        if (forecast == null) {
                            logger.trace("[REDUCE] We have no actual values - need to fetch");
                            Request estimateRequest = httpClient.newRequest(currentEstimateUrl);
                            estimateRequest.header(HttpHeader.AUTHORIZATION, BEARER + bridge.getApiKey());
                            increaseCount();
                            ContentResponse crEstimate = estimateRequest.send();
                            int callStatus = crEstimate.getStatus();
                            if (callStatus == 200) {
                                forecast = new JSONObject(crEstimate.getContentAsString());
                            } else {
                                apiCallFailure(currentEstimateUrl, crEstimate.getStatus());
                                logger.trace("[REDUCE] Actuals call failed with {} - return", callStatus);
                                return;
                            }
                        }
                        if (forecast != null) {
                            logger.trace("[REDUCE] Actuals call successful - continue with fetching forecast");
                            Request forecastRequest = httpClient.newRequest(forecastUrl);
                            forecastRequest.header(HttpHeader.AUTHORIZATION, BEARER + bridge.getApiKey());
                            increaseCount();
                            ContentResponse crForecast = forecastRequest.send();

                            if (crForecast.getStatus() == 200) {
                                logger.trace("[REDUCE] Forecast call successful");
                                JSONObject forecastJson = new JSONObject(crForecast.getContentAsString());
                                forecast.put(KEY_FORECAST, forecastJson.getJSONArray(KEY_FORECAST));
                                Instant expiration = (configuration.refreshInterval == 0) ? Instant.MAX
                                        : Instant.now(Utils.getClock()).plus(configuration.refreshInterval,
                                                ChronoUnit.MINUTES);
                                SolcastObject localForecast = new SolcastObject(thing.getUID().getAsString(), forecast,
                                        expiration, bridge, storage);
                                setForecast(localForecast);
                                updateState(GROUP_RAW + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_JSON,
                                        StringType.valueOf(forecastOptional.get().getRaw()));
                                updateStatus(ThingStatus.ONLINE);
                            } else {
                                apiCallFailure(forecastUrl, crForecast.getStatus());
                            }
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
        return forecastOptional.get();
    }

    private void increaseCount() {
        apiRequestCounter++;
    }

    private void checkCount() {
        Instant now = Instant.now();
        if (lastReset.atZone(ZoneId.of("UTC")).getDayOfMonth() != now.atZone(ZoneId.of("UTC")).getDayOfMonth()) {
            logger.trace("New day {}, reset counter", now);
            apiRequestCounter = 0;
            lastReset = now;
        }
    }

    /**
     * Store all values from today in JSONObject
     */
    protected static JSONObject getTodaysValues(String raw) {
        JSONObject forecast = new JSONObject(raw);
        JSONArray actualJsonArray = new JSONArray();
        if (forecast.has(KEY_ACTUALS)) {
            actualJsonArray = forecast.getJSONArray(KEY_ACTUALS);
        }
        JSONArray forecastJsonArray = new JSONArray();
        if (forecast.has(KEY_FORECAST)) {
            forecastJsonArray = forecast.getJSONArray(KEY_FORECAST);
        }
        for (int i = 0; i < forecastJsonArray.length(); i++) {
            actualJsonArray.put(forecastJsonArray.getJSONObject(i));
        }
        JSONArray todaysValuesArray = new JSONArray();
        LocalDate today = ZonedDateTime.now(Utils.getClock()).toLocalDate();
        actualJsonArray.forEach(entry -> {
            JSONObject forecastJson = (JSONObject) entry;
            String periodEnd = forecastJson.getString(KEY_PERIOD_END);
            ZonedDateTime periodEndZdt = Utils.getZdtFromUTC(periodEnd);
            if (periodEndZdt != null) {
                if (periodEndZdt.toLocalDate().equals(today)) {
                    todaysValuesArray.put(entry);
                }
            }
        });
        JSONObject ret = new JSONObject();
        ret.put(KEY_ACTUALS, todaysValuesArray);
        return ret;
    }

    private void apiCallFailure(String url, int status) {
        logger.debug("{} Call {} failed {}", thing.getLabel(), url, status);
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                "@text/solarforecast.plane.status.http-status [\"" + status + "\"]");
    }

    protected void updateChannels(SolcastObject f) {
        if (bridgeHandler.isEmpty()) {
            return;
        }
        ZonedDateTime now = ZonedDateTime.now(Utils.getClock());
        List<QueryMode> modes = List.of(QueryMode.Average, QueryMode.Pessimistic, QueryMode.Optimistic);
        modes.forEach(mode -> {
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
            checkCount();
            updateState(GROUP_RAW + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_COUNT,
                    new DecimalType(apiRequestCounter));
        });
    }

    protected synchronized void setForecast(SolcastObject f) {
        forecastOptional = Optional.of(f);
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
        return List.of(forecastOptional.get());
    }
}
