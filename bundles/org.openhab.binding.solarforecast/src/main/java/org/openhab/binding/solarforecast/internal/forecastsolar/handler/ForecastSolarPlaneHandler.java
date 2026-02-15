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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.solarforecast.internal.SolarForecastBindingConstants;
import org.openhab.binding.solarforecast.internal.SolarForecastException;
import org.openhab.binding.solarforecast.internal.actions.SolarForecast;
import org.openhab.binding.solarforecast.internal.actions.SolarForecastActions;
import org.openhab.binding.solarforecast.internal.actions.SolarForecastProvider;
import org.openhab.binding.solarforecast.internal.forecastsolar.ForecastSolarObject;
import org.openhab.binding.solarforecast.internal.forecastsolar.config.ForecastSolarPlaneConfiguration;
import org.openhab.binding.solarforecast.internal.solcast.SolcastObject.QueryMode;
import org.openhab.binding.solarforecast.internal.utils.Utils;
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
    private final AtomicBoolean dirty = new AtomicBoolean(false);
    private final HttpClient httpClient;
    private ForecastSolarObject forecast;

    protected ForecastSolarPlaneConfiguration configuration = new ForecastSolarPlaneConfiguration();
    protected String identifier;
    protected @Nullable ForecastSolarBridgeHandler bridgeHandler;

    public ForecastSolarPlaneHandler(Thing thing, HttpClient hc) {
        super(thing);
        httpClient = hc;
        String label = thing.getLabel();
        identifier = (label == null) ? thing.getUID().getAsString() : label;
        forecast = new ForecastSolarObject(identifier);
    }

    /**
     * #####################
     * Handler functionality
     * #####################
     */

    @Override
    public void initialize() {
        configuration = getConfigAs(ForecastSolarPlaneConfiguration.class);
        if (!isConfigurationValid()) {
            return;
        }
        Bridge bridge = getBridge();
        if (bridge != null) {
            BridgeHandler handler = bridge.getHandler();
            if (handler != null) {
                if (handler instanceof ForecastSolarBridgeHandler fsbh) {
                    bridgeHandler = fsbh;
                    updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE,
                            "@text/solarforecast.plane.status.await-feedback");
                    bridge().addPlane(this);
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

    private boolean isConfigurationValid() {
        // Validate configuration
        if (configuration.declination < 0 || configuration.declination > 90) {
            configErrorStatus("Declination must be between 0 and 90.");
            return false;
        }
        if (configuration.azimuth < -180 || configuration.azimuth > 180) {
            configErrorStatus("Azimuth must be between -180 and 180.");
            return false;
        }
        if (configuration.kwp <= 0) {
            configErrorStatus("Installed kWp must be positive.");
            return false;
        }
        if (configuration.refreshInterval < 0) {
            configErrorStatus("Refresh interval must be non-negative.");
            return false;
        }
        return true;
    }

    protected void configErrorStatus(String message) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, message);
    }

    @Override
    public void dispose() {
        bridge().removePlane(this);
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
                bridge().getSequentialScheduler().execute(this::updateData);
            }
        }
    }

    /**
     * #####################
     * Forecast functionality
     * #####################
     */

    /**
     * This is the main function called by the bridge to update current data and refresh if expired
     * Only called from sequential bridge scheduler thread!
     * 1) Fetch new data if expired
     * 2) Update channels with current data
     * 3) Update timeseries if fetchData delivered new forecast
     * 4) Update thing status to ONLINE
     */
    public void updateData() {
        ForecastSolarObject localForecast = getForecast();
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

    /**
     * Fetch new forecast data from the forecast.solar API if the current data is expired.
     * https://doc.forecast.solar/doku.php?id=api:estimate
     */
    private void fetchData() {
        ForecastSolarObject localForecast = getForecast();
        if (!localForecast.isExpired()) {
            return;
        }
        String url = buildUrl();
        logger.trace("Call {}", Utils.redactUrlForLog(url));
        try {
            ContentResponse cr = httpClient.newRequest(url).timeout(10, TimeUnit.SECONDS).send();
            int responseStatus = cr.getStatus();
            handleResponse(responseStatus, cr.getContentAsString());
        } catch (ExecutionException | TimeoutException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (InterruptedException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    private void handleResponse(int responseStatus, String forecastContent) {
        if (responseStatus == HttpStatus.OK_200) {
            try {
                ForecastSolarObject newForecast = new ForecastSolarObject(identifier, forecastContent,
                        Instant.now(Utils.getClock()).plus(configuration.refreshInterval, ChronoUnit.MINUTES));
                updateForecast(newForecast);
                updateStatus(ThingStatus.ONLINE);
            } catch (SolarForecastException fse) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                        "@text/solarforecast.plane.status.json-status [\"" + fse.getMessage() + "\"]");
            }
        } else if (responseStatus == HttpStatus.TOO_MANY_REQUESTS_429) {
            // special handling for 429 response: https://doc.forecast.solar/facing429
            // bridge shall "calm down" until at least one hour is expired
            bridge().calmDown();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/solarforecast.plane.status.http-status [\"" + responseStatus + "\"]");
        } else {
            logger.trace("Call failed with status {}. Response: {}", responseStatus, forecastContent);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/solarforecast.plane.status.http-status [\"" + responseStatus + "\"]");
        }
    }

    /**
     * Update the channels with the current forecast data.
     */
    private void updateChannels() {
        ForecastSolarObject localForecast = getForecast();
        ZonedDateTime now = ZonedDateTime.now(Utils.getClock());
        double energyDay = localForecast.getDayTotal(now.toLocalDate());
        double energyProduced = localForecast.getActualEnergyValue(now);
        // energyDay (separate field in JSON) and energyProduced (sum of actual values) can differ slightly due to
        // rounding)
        // 2026-01-05 22:03:55.147 [TRACE] [r.handler.ForecastSolarPlaneHandler] - Actual 1.0039800206714415 Day
        // 1.0039800206714413 Diff -2.220446049250313E-16
        // avoid negative remaining energy due to rounding issues
        double remainingEnergy = Math.max(0, (energyDay - energyProduced));
        updateState(CHANNEL_ENERGY_ACTUAL, Utils.getEnergyState(energyProduced));
        updateState(CHANNEL_ENERGY_REMAIN, Utils.getEnergyState(remainingEnergy));
        updateState(CHANNEL_ENERGY_TODAY, Utils.getEnergyState(energyDay));
        updateState(CHANNEL_POWER_ACTUAL, Utils.getPowerState(localForecast.getActualPowerValue(now)));
    }

    /**
     * Update timeseries with the current forecast data.
     */
    private void updateTimeseries() {
        ForecastSolarObject localForecast = getForecast();
        sendTimeSeries(CHANNEL_POWER_ESTIMATE, localForecast.getPowerTimeSeries(QueryMode.Average));
        sendTimeSeries(CHANNEL_ENERGY_ESTIMATE, localForecast.getEnergyTimeSeries(QueryMode.Average));
    }

    /**
     * Set the new forecast data.
     *
     * @param newForecast set as actual forecast data
     */
    protected void updateForecast(ForecastSolarObject newForecast) {
        synchronized (this) {
            forecast = newForecast;
            dirty.set(true);
        }
    }

    /**
     * Get the current forecast data reference in a thread-safe manner.
     *
     * @return the current shared {@link ForecastSolarObject} reference
     */
    public ForecastSolarObject getForecast() {
        synchronized (this) {
            ForecastSolarObject localForecast = forecast;
            return localForecast;
        }
    }

    /**
     * Used by bridge to check if timeseries needs to be updated.
     * Check if timeseries update is needed and reset the flag.
     *
     * @return true if timeseries update is needed
     */
    public boolean isTimeseriesUpdateNeeded() {
        return dirty.getAndSet(false);
    }

    /**
     * #####################
     * URL functionality
     * #####################
     */

    /**
     * Build the forecast.solar API URL with mandatory and optional parameters.
     *
     * @return complete URL for the forecast request
     */
    protected String buildUrl() {
        // create URL with mandatory parameters using StringBuilder
        StringBuilder url = new StringBuilder();
        url.append(bridge().getBaseUrl()).append(configuration.declination).append(SLASH).append(configuration.azimuth)
                .append(SLASH).append(configuration.kwp).append("?damping=").append(configuration.dampAM).append(",")
                .append(configuration.dampPM);
        // add parameters calculated by queryParameters() including subclasses
        Map<String, String> parameters = new HashMap<>();
        queryParameters(parameters);
        for (Entry<String, String> entry : parameters.entrySet()) {
            url.append("&").append(entry.getKey()).append("=").append(entry.getValue());
        }
        return url.toString();
    }

    /**
     * Query parameters for the forecast request. Base forecast solar parameter is "full=1" mandatory for all requests
     * and horizon if configured.
     *
     * @return Map with parameter key
     */
    protected void queryParameters(Map<String, String> parameters) {
        bridge().queryParameters(parameters);
        parameters.put("full", "1"); // full forecast data including hours without sun
        if (!SolarForecastBindingConstants.EMPTY.equals(configuration.horizon)) {
            parameters.put("horizon", configuration.horizon); // horizon if configured
        }
    }

    /**
     * #####################
     * Helper functionality
     * #####################
     */

    private ForecastSolarBridgeHandler bridge() {
        ForecastSolarBridgeHandler localBridgeHandler = bridgeHandler;
        if (localBridgeHandler != null) {
            return localBridgeHandler;
        } else {
            throw new IllegalStateException("Bridge handler not initialized");
        }
    }

    /**
     * #####################
     * Actions functionality
     * #####################
     */

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(SolarForecastActions.class);
    }

    @Override
    public List<SolarForecast> getSolarForecasts() {
        return List.of(getForecast());
    }
}
