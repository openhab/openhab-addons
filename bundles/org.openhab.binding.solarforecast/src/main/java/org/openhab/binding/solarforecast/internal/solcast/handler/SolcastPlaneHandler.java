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
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
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
import org.openhab.binding.solarforecast.internal.solcast.SolcastCounter;
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
    private SolcastCounter counter;
    private SolcastObject forecast;
    private SolcastCache cache;

    private @Nullable SolcastBridgeHandler bridgeHandler;

    public SolcastPlaneHandler(Thing thing, HttpClient hc, Storage<String> storage) {
        super(thing);
        httpClient = hc;
        this.storage = storage;
        identifier = thing.getUID().getAsString();
        forecast = new SolcastObject(identifier);
        cache = new SolcastCache(identifier, storage);
        counter = new SolcastCounter(identifier, storage);
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
            String channel = channelUID.getIdWithoutGroup();
            String group = channelUID.getGroupId();
            if (group != null) {
                bridge().getScheduler().execute(() -> doRefresh(group, channel));
            }
        }
    }

    private void doRefresh(String group, String channel) {
        switch (group) {
            case GROUP_UPDATE -> updateSupervisorChannels();
            case GROUP_OPTIMISTIC, GROUP_PESSIMISTIC, GROUP_AVERAGE -> {
                QueryMode mode = QueryMode.valueOf(group.toUpperCase(Locale.ENGLISH));
                switch (channel) {
                    case CHANNEL_ENERGY_ACTUAL, CHANNEL_ENERGY_REMAIN, CHANNEL_ENERGY_TODAY, CHANNEL_POWER_ACTUAL ->
                        updateForecastChannels(mode);
                    case CHANNEL_POWER_ESTIMATE, CHANNEL_ENERGY_ESTIMATE -> updateTimeseries();
                }
            }
            default -> {
                logger.trace("{} Unknown group {} for refresh command", identifier, group);
            }
        }
    }

    public void updateData() {
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
        counter.count(callStatus);

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

    public JSONObject getCounter() {
        return counter.get();
    }

    private void apiCallFailure(String url, int status) {
        updateState(GROUP_UPDATE + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_API_COUNT,
                StringType.valueOf(getCounter().toString()));
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                "@text/solarforecast.plane.status.http-status [\"" + status + "\"]");
    }

    /**
     * Update channels frequently with new data
     *
     * @param Forecast object
     */
    protected void updateChannels() throws SolarForecastException {
        updateSupervisorChannels();
        MODES.forEach(mode -> {
            updateForecastChannels(mode);
        });
    }

    private void updateSupervisorChannels() {
        updateState(GROUP_UPDATE + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_API_COUNT,
                StringType.valueOf(getCounter().toString()));
        SolcastObject localForecast = getForecast();
        Instant creationInstant = localForecast.getCreationInstant();
        if (creationInstant != Instant.MIN && creationInstant != Instant.MAX) {
            updateState(GROUP_UPDATE + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_LATEST_UPDATE,
                    new DateTimeType(creationInstant));
        }
    }

    private void updateForecastChannels(QueryMode mode) {
        ZonedDateTime now = ZonedDateTime.now(Utils.getClock());
        SolcastObject localForecast = getForecast();
        double energyDay = localForecast.getDayTotal(now.toLocalDate(), mode);
        double energyProduced = localForecast.getActualEnergyValue(now, mode);
        updateState(mode + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_ENERGY_ACTUAL,
                Utils.getEnergyState(energyProduced));
        updateState(mode + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_ENERGY_REMAIN,
                Utils.getEnergyState(energyDay - energyProduced));
        updateState(mode + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_ENERGY_TODAY, Utils.getEnergyState(energyDay));
        updateState(mode + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_POWER_ACTUAL,
                Utils.getPowerState(localForecast.getActualPowerValue(now, QueryMode.AVERAGE)));
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
