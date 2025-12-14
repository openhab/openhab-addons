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
package org.openhab.binding.solarforecast.internal.forecastsolar.handler;

import static org.openhab.binding.solarforecast.internal.SolarForecastBindingConstants.*;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.openhab.binding.solarforecast.internal.SolarForecastBindingConstants;
import org.openhab.binding.solarforecast.internal.SolarForecastException;
import org.openhab.binding.solarforecast.internal.actions.SolarForecast;
import org.openhab.binding.solarforecast.internal.actions.SolarForecastActions;
import org.openhab.binding.solarforecast.internal.actions.SolarForecastProvider;
import org.openhab.binding.solarforecast.internal.forecastsolar.ForecastSolarObject;
import org.openhab.binding.solarforecast.internal.forecastsolar.config.ForecastSolarPlaneConfiguration;
import org.openhab.binding.solarforecast.internal.solcast.SolcastObject.QueryMode;
import org.openhab.binding.solarforecast.internal.utils.Utils;
import org.openhab.core.common.ThreadPoolManager;
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
 * The {@link ForecastSolarPlaneHandler} is triggered by bridge to fetch solar forecast data if expired.
 *
 * @author Bernd Weymann - Initial contribution
 * @author Bernd Weymann - Used as base class for {@link AdjustableForecastSolarPlaneHandler} and
 *         {@link SmartForecastSolarPlaneHandler}
 */
@NonNullByDefault
public class ForecastSolarPlaneHandler extends BaseThingHandler implements SolarForecastProvider {

    private final Logger logger = LoggerFactory.getLogger(ForecastSolarPlaneHandler.class);
    private final HttpClient httpClient;
    private boolean dirtyFlag = false;
    private ForecastSolarObject forecast;

    protected ForecastSolarPlaneConfiguration configuration = new ForecastSolarPlaneConfiguration();
    protected @Nullable ScheduledFuture<?> futureSchedule;
    protected @Nullable ForecastSolarBridgeHandler bridgeHandler;
    protected ScheduledExecutorService refresher = ThreadPoolManager
            .getPoolBasedSequentialScheduledExecutorService(BINDING_ID, null);

    public ForecastSolarPlaneHandler(Thing thing, HttpClient hc) {
        super(thing);
        httpClient = hc;
        forecast = new ForecastSolarObject(thing.getUID().getAsString());
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(SolarForecastActions.class);
    }

    @Override
    public void initialize() {
        configuration = getConfigAs(ForecastSolarPlaneConfiguration.class);
        Bridge bridge = getBridge();
        if (bridge != null) {
            BridgeHandler handler = bridge.getHandler();
            if (handler != null) {
                if (handler instanceof ForecastSolarBridgeHandler fsbh) {
                    bridgeHandler = fsbh;
                    updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE,
                            "@text/solarforecast.plane.status.await-feedback");
                    refresher.execute(this::doInitialize);
                } else {
                    configErrorStatus("@text/solarforecast.plane.status.wrong-handler" + " [\"" + handler + "\"]");
                }
            } else {
                configErrorStatus("@text/solarforecast.plane.status.bridge-handler-not-found");
            }
        } else {
            configErrorStatus("@text/solarforecast.plane.status.bridge-missing");
        }
    }

    private void doInitialize() {
        fetchData();
        ForecastSolarObject localForecast = getForecast();
        if (!localForecast.isEmpty()) {
            bridge().addPlane(this);
        } else {
            futureSchedule = refresher.schedule(this::doInitialize, 1, TimeUnit.MINUTES);
        }
    }

    protected void configErrorStatus(String message) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, message);
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> localFutureSchedule = futureSchedule;
        if (localFutureSchedule != null) {
            localFutureSchedule.cancel(true);
            futureSchedule = null;
        }
        ForecastSolarBridgeHandler localBridgeHandler = bridgeHandler;
        if (localBridgeHandler != null) {
            localBridgeHandler.removePlane(this);
            localBridgeHandler = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            ForecastSolarObject localForecast = getForecast();
            if (CHANNEL_POWER_ESTIMATE.equals(channelUID.getIdWithoutGroup())) {
                sendTimeSeries(CHANNEL_POWER_ESTIMATE, localForecast.getPowerTimeSeries(QueryMode.Average));
            } else if (CHANNEL_ENERGY_ESTIMATE.equals(channelUID.getIdWithoutGroup())) {
                sendTimeSeries(CHANNEL_ENERGY_ESTIMATE, localForecast.getEnergyTimeSeries(QueryMode.Average));
            } else {
                fetchData();
            }
        }
    }

    /**
     *
     * @return
     */
    ForecastSolarObject getData() {
        ForecastSolarObject localForecast = getForecast();
        if (localForecast.isExpired()) {
            // asynchronous fetch of new data
            refresher.execute(this::fetchData);
        }
        // else use available forecast
        updateStatus(ThingStatus.ONLINE);
        updateChannels();

        if (dirtyFlag) {
            // reset dirty flag after first readout of new data
            dirtyFlag = false;
            bridge().requestForecastUpdate();
        }
        return localForecast;
    }

    /**
     * https://doc.forecast.solar/doku.php?id=api:estimate
     */
    private void fetchData() {
        ForecastSolarObject localForecast = getForecast();
        if (!localForecast.isExpired()) {
            return;
        }
        String url = buildUrl();
        logger.trace("Call {}", url);
        try {
            ContentResponse cr = httpClient.GET(url);
            int responseStatus = cr.getStatus();
            if (responseStatus == 200) {
                try {
                    localForecast = new ForecastSolarObject(thing.getUID().getAsString(), cr.getContentAsString(),
                            Instant.now(Utils.getClock()).plus(configuration.refreshInterval, ChronoUnit.MINUTES));
                    updateStatus(ThingStatus.ONLINE);
                    setForecast(localForecast);
                } catch (SolarForecastException fse) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                            "@text/solarforecast.plane.status.json-status [\"" + fse.getMessage() + "\"]");
                }
            } else if (responseStatus == 429) {
                // special handling for 429 response: https://doc.forecast.solar/facing429
                // bridge shall "calm down" until at least one hour is expired
                bridge().calmDown();
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/solarforecast.plane.status.http-status [\"" + cr.getStatus() + "\"]");
            } else {
                logger.trace("Call {} failed with status {}. Response: {}", url, cr.getStatus(),
                        cr.getContentAsString());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/solarforecast.plane.status.http-status [\"" + cr.getStatus() + "\"]");
            }
        } catch (ExecutionException | TimeoutException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (InterruptedException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    protected String buildUrl() {
        // create URL with mandatory parameters
        String url = bridge().getBaseUrl() + configuration.declination + SLASH + configuration.azimuth + SLASH
                + configuration.kwp + "?damping=" + configuration.dampAM + "," + configuration.dampPM;
        // add parameters calculated by queryParameters() including subclasses
        for (Entry<String, String> entry : queryParameters().entrySet()) {
            url += "&" + entry.getKey() + "=" + entry.getValue();
        }
        return url;
    }

    /**
     * Query parameters for the forecast request. Base forecast solar parameter is "full=1" mandatory for all requests
     * and horizon if configured.
     *
     * @return Map with parameter key
     */
    protected Map<String, String> queryParameters() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("full", "1"); // full forecast data including hours without sun
        if (!SolarForecastBindingConstants.EMPTY.equals(configuration.horizon)) {
            parameters.put("horizon", configuration.horizon); // horizon if configured
        }
        return bridge().queryParameters(parameters);
    }

    protected void updateChannels() {
        ForecastSolarObject localForecast = getForecast();
        ZonedDateTime now = ZonedDateTime.now(Utils.getClock());
        double energyDay = localForecast.getDayTotal(now.toLocalDate());
        double energyProduced = localForecast.getActualEnergyValue(now);
        updateState(CHANNEL_ENERGY_ACTUAL, Utils.getEnergyState(energyProduced));
        updateState(CHANNEL_ENERGY_REMAIN, Utils.getEnergyState(energyDay - energyProduced));
        updateState(CHANNEL_ENERGY_TODAY, Utils.getEnergyState(energyDay));
        updateState(CHANNEL_POWER_ACTUAL, Utils.getPowerState(localForecast.getActualPowerValue(now)));
    }

    protected void setForecast(ForecastSolarObject newForecast) {
        synchronized (this) {
            forecast = newForecast;
            dirtyFlag = true;
        }
        ForecastSolarObject localForecast = getForecast();
        sendTimeSeries(CHANNEL_POWER_ESTIMATE, localForecast.getPowerTimeSeries(QueryMode.Average));
        sendTimeSeries(CHANNEL_ENERGY_ESTIMATE, localForecast.getEnergyTimeSeries(QueryMode.Average));
    }

    protected ForecastSolarObject getForecast() {
        synchronized (this) {
            ForecastSolarObject localForecast = forecast;
            return localForecast;
        }
    }

    @Override
    public List<SolarForecast> getSolarForecasts() {
        return List.of(getForecast());
    }

    private ForecastSolarBridgeHandler bridge() {
        ForecastSolarBridgeHandler localBridgeHandler = bridgeHandler;
        if (localBridgeHandler != null) {
            return localBridgeHandler;
        } else {
            throw new IllegalStateException("Bridge handler not initialized");
        }
    }
}
