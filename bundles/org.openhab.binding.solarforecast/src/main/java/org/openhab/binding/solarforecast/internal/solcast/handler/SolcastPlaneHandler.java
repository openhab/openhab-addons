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
package org.openhab.binding.solarforecast.internal.solcast.handler;

import static org.openhab.binding.solarforecast.internal.SolarForecastBindingConstants.*;
import static org.openhab.binding.solarforecast.internal.solcast.SolcastConstants.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.json.JSONObject;
import org.openhab.binding.solarforecast.internal.SolarForecastException;
import org.openhab.binding.solarforecast.internal.actions.SolarForecast;
import org.openhab.binding.solarforecast.internal.actions.SolarForecastActions;
import org.openhab.binding.solarforecast.internal.actions.SolarForecastProvider;
import org.openhab.binding.solarforecast.internal.solcast.SolcastCache;
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

    private final Logger logger = LoggerFactory.getLogger(SolcastPlaneHandler.class);
    private final AtomicBoolean dirty = new AtomicBoolean(false);
    private final Storage<String> storage;
    private final HttpClient httpClient;
    private final String identifier;
    private SolcastPlaneConfiguration configuration = new SolcastPlaneConfiguration();
    private Instant lastCounterReset = Utils.now();
    private SolcastCache cache;
    private JSONObject counterJson;
    private SolcastObject forecast;
    private @Nullable SolcastBridgeHandler bridgeHandler;

    public SolcastPlaneHandler(Thing thing, HttpClient hc, Storage<String> storage) {
        super(thing);
        httpClient = hc;
        this.storage = storage;
        counterJson = getNewCounter();
        identifier = thing.getUID().getAsString();
        forecast = new SolcastObject(identifier);
        cache = new SolcastCache(identifier, storage);
    }

    @Override
    public void initialize() {
        configuration = getConfigAs(SolcastPlaneConfiguration.class);
        if (!isConfigurationValid()) {
            return;
        }

        // connect Bridge & Status
        Bridge bridge = getBridge();
        if (bridge != null) {
            BridgeHandler handler = bridge.getHandler();
            if (handler != null) {
                if (handler instanceof SolcastBridgeHandler solcastBridgeHandler) {
                    bridgeHandler = solcastBridgeHandler;
                    restoreCounter();
                    restoreForecast();
                    solcastBridgeHandler.addPlane(this);
                } else {
                    configErrorStatus("@text/solarforecast.plane.status.wrong-handler [\"" + handler + "\"]");
                }
            } else {
                configErrorStatus("@text/solarforecast.plane.status.bridge-handler-not-found");
            }
        } else {
            configErrorStatus("@text/solarforecast.plane.status.bridge-missing");
        }
    }

    private boolean isConfigurationValid() {
        if (configuration.resourceId.isBlank()) {
            configErrorStatus("@text/solarforecast.site.status.location-missing");
            return false;
        }
        return true;
    }

    private void restoreForecast() {
        String expirationString = storage.get(identifier + EXPIRATION_APPENDIX);
        String creationString = storage.get(identifier + CREATION_APPENDIX);
        if (expirationString != null && creationString != null) {
            updateForecast(new SolcastObject(identifier, cache.getForecast(), Instant.parse(expirationString),
                    Instant.parse(creationString)));
        } else {
            logger.trace("{} No stored forecast found", identifier);
            // immediate expiration if refresh interval is not 0
            Instant expiration = (configuration.refreshInterval == 0) ? Instant.MAX : Instant.MIN;
            updateForecast(new SolcastObject(identifier, cache.getForecast(), expiration, Instant.MIN));
        }
    }

    protected void configErrorStatus(String message) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, message);
    }

    @Override
    public void dispose() {
        super.dispose();
        bridge().removePlane(this);
    }

    @Override
    public void handleRemoval() {
        storage.remove(identifier + CALL_COUNT_APPENDIX);
        storage.remove(identifier + CALL_COUNT_DATE_APPENDIX);
        storage.remove(identifier + FORECAST_APPENDIX);
        storage.remove(identifier + CREATION_APPENDIX);
        storage.remove(identifier + EXPIRATION_APPENDIX);
        super.handleRemoval();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            if (CHANNEL_API_COUNT.equals(channelUID.getIdWithoutGroup())) {
                checkCounterReset();
                updateState(CHANNEL_API_COUNT, StringType.valueOf(counterJson.toString()));
            } else {
                String group = channelUID.getGroupId();
                if (group == null) {
                    return;
                }
                String channel = channelUID.getIdWithoutGroup();
                var mode = switch (group) {
                    case GROUP_OPTIMISTIC -> QueryMode.Optimistic;
                    case GROUP_PESSIMISTIC -> QueryMode.Pessimistic;
                    default -> QueryMode.Average;
                };
                switch (channel) {
                    case CHANNEL_ENERGY_ESTIMATE ->
                        sendTimeSeries(group + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_ENERGY_ESTIMATE,
                                getForecast().getEnergyTimeSeries(mode));
                    case CHANNEL_POWER_ESTIMATE ->
                        sendTimeSeries(group + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_POWER_ESTIMATE,
                                getForecast().getPowerTimeSeries(mode));
                    default -> updateChannels();
                }
            }
        }
    }

    public void updateData() {
        // check count, maybe fetching will increase counter
        checkCounterReset();
        SolcastObject localForecast = getForecast();
        // 1) fetch new data if expired
        if (localForecast.isExpired()) {
            fetchData();
        }
        try {
            // 2) Update channels with current data
            updateChannels();
            // 3) Update timeseries if dirty flag is set by fetchData before
            if (dirty.get()) {
                updateTimeseries();
            }
            updateStatus(ThingStatus.ONLINE);
        } catch (SolarForecastException sfe) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                    "@text/solarforecast.plane.status.exception [\"" + sfe.getMessage() + "\"]");
        }
    }

    private void fetchData() {
        SolcastObject forecastObject = getForecast();
        if (forecastObject.isExpired()) {
            logger.debug("{} Forecast expired -> get new forecast", identifier);
            try {
                if (!cache.isFilled() || !configuration.guessActuals) {
                    logger.debug("{} Cache not used {} or not filled {}", identifier, !configuration.guessActuals,
                            cache.toString());
                    fetchData(CURRENT_ESTIMATE_URL);
                    if (!cache.isFilled()) {
                        logger.debug("{} Cache still not filled after fetch {}", identifier, cache.toString());
                    }
                }
                fetchData(FORECAST_URL);
                Instant expiration = getExpirationTime();
                SolcastObject newForewcast = new SolcastObject(identifier, cache.getForecast(), expiration);
                storage.put(identifier + CREATION_APPENDIX, Utils.now().toString());
                storage.put(identifier + EXPIRATION_APPENDIX, expiration.toString());
                updateForecast(newForewcast);
            } catch (ExecutionException | TimeoutException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            } catch (InterruptedException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
    }

    private void fetchData(String urlPattern) throws InterruptedException, TimeoutException, ExecutionException {
        String fetchUrl = String.format(urlPattern, configuration.resourceId);
        logger.trace("{} fetch {}", identifier, fetchUrl);
        Request fetchRequest = httpClient.newRequest(fetchUrl);
        fetchRequest.header(HttpHeader.AUTHORIZATION, BEARER + bridge().getApiKey());
        ContentResponse response = fetchRequest.send();
        int callStatus = response.getStatus();
        count(callStatus);

        if (callStatus == HttpStatus.OK_200) {
            JSONObject actualJson = new JSONObject(response.getContentAsString());
            cache.update(actualJson);
        } else {
            apiCallFailure(fetchUrl, response.getStatus());
        }
    }

    private Instant getExpirationTime() {
        return (configuration.refreshInterval == 0) ? Instant.MAX
                : Utils.now().plus(configuration.refreshInterval, ChronoUnit.MINUTES).truncatedTo(ChronoUnit.MINUTES);
    }

    /**
     * ### Counter functionality ####
     */

    private void restoreCounter() {
        String counterString = storage.get(identifier + CALL_COUNT_APPENDIX);
        counterJson = getNewCounter();
        if (counterString != null) {
            try {
                counterJson = new JSONObject(counterString);
            } catch (Exception e) {
                logger.debug("{} Could not restore counter: {}", identifier, e.getMessage());
            }
        }
        String lastResetString = storage.get(identifier + CALL_COUNT_DATE_APPENDIX);
        if (lastResetString != null) {
            lastCounterReset = Instant.parse(lastResetString);
        }
        // immediately check counter if it's still valid
        checkCounterReset();
    }

    private void count(int status) {
        // check first regarding day switch before increasing
        checkCounterReset();
        switch (status) {
            case 200 -> counterJson.put(HTTP_OK, counterJson.getInt(HTTP_OK) + 1);
            case 429 -> counterJson.put(HTTP_TOO_MANY_REQUESTS, counterJson.getInt(HTTP_TOO_MANY_REQUESTS) + 1);
            default -> counterJson.put(HTTP_OTHER, counterJson.getInt(HTTP_OTHER) + 1);
        }
        storeCounter();
    }

    /**
     * Solcast API counter is reseted daily at 00:00 ZTC
     */
    private void checkCounterReset() {
        Instant now = Utils.now();
        if (lastCounterReset.atZone(ZoneId.of("UTC")).getDayOfMonth() != now.atZone(ZoneId.of("UTC")).getDayOfMonth()) {
            counterJson = getNewCounter();
            storeCounter();
            lastCounterReset = now;
        }
    }

    private void storeCounter() {
        storage.put(identifier + CALL_COUNT_DATE_APPENDIX, lastCounterReset.toString());
        storage.put(identifier + CALL_COUNT_APPENDIX, counterJson.toString());
    }

    private JSONObject getNewCounter() {
        return new JSONObject("{\"200\":0,\"429\":0,\"other\":0}");
    }

    public JSONObject getCounter() {
        return counterJson;
    }

    private void apiCallFailure(String url, int status) {
        updateState(GROUP_UPDATE + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_API_COUNT,
                StringType.valueOf(counterJson.toString()));
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                "@text/solarforecast.plane.status.http-status [\"" + status + "\"]");
    }

    /**
     * Update channels frequently with new data
     *
     * @param Forecast object
     */
    protected void updateChannels() throws SolarForecastException {
        SolcastObject localForecast = getForecast();
        ZonedDateTime now = ZonedDateTime.now(Utils.getClock());
        updateState(GROUP_UPDATE + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_API_COUNT,
                StringType.valueOf(counterJson.toString()));
        Instant creationInstant = localForecast.getCreationInstant();
        if (creationInstant != Instant.MIN && creationInstant != Instant.MAX) {
            updateState(GROUP_UPDATE + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_LATEST_UPDATE,
                    new DateTimeType(creationInstant));
        }
        MODES.forEach(mode -> {
            double energyDay = localForecast.getDayTotal(now.toLocalDate(), mode);
            double energyProduced = localForecast.getActualEnergyValue(now, mode);
            updateState(mode + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_ENERGY_ACTUAL,
                    Utils.getEnergyState(energyProduced));
            updateState(mode + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_ENERGY_REMAIN,
                    Utils.getEnergyState(energyDay - energyProduced));
            updateState(mode + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_ENERGY_TODAY,
                    Utils.getEnergyState(energyDay));
            updateState(mode + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_POWER_ACTUAL,
                    Utils.getPowerState(localForecast.getActualPowerValue(now, QueryMode.Average)));
        });
    }

    /**
     * Update time series only if new forecast object is created
     *
     * @param Forecast object
     */
    protected void updateTimeseries() {
        SolcastObject localForecast = getForecast();
        MODES.forEach(mode -> {
            sendTimeSeries(mode + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_POWER_ESTIMATE,
                    localForecast.getPowerTimeSeries(mode));
            sendTimeSeries(mode + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_ENERGY_ESTIMATE,
                    localForecast.getEnergyTimeSeries(mode));
        });
    }

    /**
     * Set the new forecast data.
     *
     * @param newForecast set as actual forecast data
     */
    protected void updateForecast(SolcastObject newForecast) {
        synchronized (this) {
            forecast = newForecast;
            dirty.set(true);
        }
    }

    public boolean isTimeseriesUpdateNeeded() {
        return dirty.getAndSet(false);
    }

    /**
     * Get the current forecast data reference in a thread-safe manner.
     *
     * @return the current shared {@link SolcastObject} reference
     */
    public SolcastObject getForecast() {
        synchronized (this) {
            SolcastObject localForecast = forecast;
            return localForecast;
        }
    }

    private SolcastBridgeHandler bridge() {
        SolcastBridgeHandler localBridgeHandler = bridgeHandler;
        if (localBridgeHandler != null) {
            return localBridgeHandler;
        } else {
            throw new IllegalStateException("Bridge handler not initialized");
        }
    }

    /**
     * ### Action functionality
     */

    @Override
    public List<SolarForecast> getSolarForecasts() {
        return List.of(getForecast());
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(SolarForecastActions.class);
    }
}
