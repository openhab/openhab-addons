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
package org.openhab.binding.solarforecast.internal.forecastsolar.handler;

import static org.openhab.binding.solarforecast.internal.SolarForecastBindingConstants.*;

import java.time.Instant;
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
import org.openhab.binding.solarforecast.internal.SolarForecastBindingConstants;
import org.openhab.binding.solarforecast.internal.SolarForecastException;
import org.openhab.binding.solarforecast.internal.actions.SolarForecast;
import org.openhab.binding.solarforecast.internal.actions.SolarForecastActions;
import org.openhab.binding.solarforecast.internal.actions.SolarForecastProvider;
import org.openhab.binding.solarforecast.internal.forecastsolar.ForecastSolarObject;
import org.openhab.binding.solarforecast.internal.forecastsolar.config.ForecastSolarPlaneConfiguration;
import org.openhab.binding.solarforecast.internal.solcast.SolcastObject.QueryMode;
import org.openhab.binding.solarforecast.internal.utils.Utils;
import org.openhab.core.library.types.PointType;
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
 * The {@link ForecastSolarPlaneHandler} is a non active handler instance. It will be triggered by the bridge.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class ForecastSolarPlaneHandler extends BaseThingHandler implements SolarForecastProvider {
    public static final String BASE_URL = "https://api.forecast.solar/";

    private final Logger logger = LoggerFactory.getLogger(ForecastSolarPlaneHandler.class);
    private final HttpClient httpClient;

    private Optional<ForecastSolarPlaneConfiguration> configuration = Optional.empty();
    private Optional<ForecastSolarBridgeHandler> bridgeHandler = Optional.empty();
    private Optional<PointType> location = Optional.empty();
    private Optional<String> apiKey = Optional.empty();
    private ForecastSolarObject forecast;

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
        ForecastSolarPlaneConfiguration c = getConfigAs(ForecastSolarPlaneConfiguration.class);
        configuration = Optional.of(c);
        Bridge bridge = getBridge();
        if (bridge != null) {
            BridgeHandler handler = bridge.getHandler();
            if (handler != null) {
                if (handler instanceof ForecastSolarBridgeHandler fsbh) {
                    bridgeHandler = Optional.of(fsbh);
                    updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE,
                            "@text/solarforecast.plane.status.await-feedback");
                    fsbh.addPlane(this);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "@text/solarforecast.plane.status.wrong-handler" + " [\"" + handler + "\"]");
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
            if (CHANNEL_POWER_ESTIMATE.equals(channelUID.getIdWithoutGroup())) {
                sendTimeSeries(CHANNEL_POWER_ESTIMATE, forecast.getPowerTimeSeries(QueryMode.Average));
            } else if (CHANNEL_ENERGY_ESTIMATE.equals(channelUID.getIdWithoutGroup())) {
                sendTimeSeries(CHANNEL_ENERGY_ESTIMATE, forecast.getEnergyTimeSeries(QueryMode.Average));
            } else if (CHANNEL_JSON.equals(channelUID.getIdWithoutGroup())) {
                updateState(CHANNEL_JSON, StringType.valueOf(forecast.getRaw()));
            } else {
                fetchData();
            }
        }
    }

    /**
     * https://doc.forecast.solar/doku.php?id=api:estimate
     */
    protected ForecastSolarObject fetchData() {
        if (location.isPresent()) {
            if (forecast.isExpired()) {
                String url = getBaseUrl() + "estimate/" + location.get().getLatitude() + SLASH
                        + location.get().getLongitude() + SLASH + configuration.get().declination + SLASH
                        + configuration.get().azimuth + SLASH + configuration.get().kwp + "?damping="
                        + configuration.get().dampAM + "," + configuration.get().dampPM;
                if (!SolarForecastBindingConstants.EMPTY.equals(configuration.get().horizon)) {
                    url += "&horizon=" + configuration.get().horizon;
                }
                try {
                    ContentResponse cr = httpClient.GET(url);
                    int responseStatus = cr.getStatus();
                    if (responseStatus == 200) {
                        try {
                            ForecastSolarObject localForecast = new ForecastSolarObject(thing.getUID().getAsString(),
                                    cr.getContentAsString(), Instant.now(Utils.getClock())
                                            .plus(configuration.get().refreshInterval, ChronoUnit.MINUTES));
                            updateStatus(ThingStatus.ONLINE);
                            updateState(CHANNEL_JSON, StringType.valueOf(cr.getContentAsString()));
                            setForecast(localForecast);
                        } catch (SolarForecastException fse) {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                                    "@text/solarforecast.plane.status.json-status [\"" + fse.getMessage() + "\"]");
                        }
                    } else if (responseStatus == 429) {
                        // special handling for 429 response: https://doc.forecast.solar/facing429
                        // bridge shall "calm down" until at least one hour is expired
                        if (bridgeHandler.isPresent()) {
                            bridgeHandler.get().calmDown();
                        }
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
            } else {
                // else use available forecast
                updateChannels(forecast);
            }
        } else {
            logger.warn("{} Location not present", thing.getLabel());
        }
        return forecast;
    }

    private void updateChannels(ForecastSolarObject f) {
        ZonedDateTime now = ZonedDateTime.now(Utils.getClock());
        double energyDay = f.getDayTotal(now.toLocalDate());
        double energyProduced = f.getActualEnergyValue(now);
        updateState(CHANNEL_ENERGY_ACTUAL, Utils.getEnergyState(energyProduced));
        updateState(CHANNEL_ENERGY_REMAIN, Utils.getEnergyState(energyDay - energyProduced));
        updateState(CHANNEL_ENERGY_TODAY, Utils.getEnergyState(energyDay));
        updateState(CHANNEL_POWER_ACTUAL, Utils.getPowerState(f.getActualPowerValue(now)));
    }

    /**
     * Used by Bridge to set location directly
     *
     * @param loc
     */
    void setLocation(PointType loc) {
        location = Optional.of(loc);
    }

    void setApiKey(String key) {
        apiKey = Optional.of(key);
    }

    String getBaseUrl() {
        String url = BASE_URL;
        if (apiKey.isPresent()) {
            url += apiKey.get() + SLASH;
        }
        return url;
    }

    protected synchronized void setForecast(ForecastSolarObject f) {
        forecast = f;
        sendTimeSeries(CHANNEL_POWER_ESTIMATE, forecast.getPowerTimeSeries(QueryMode.Average));
        sendTimeSeries(CHANNEL_ENERGY_ESTIMATE, forecast.getEnergyTimeSeries(QueryMode.Average));
        bridgeHandler.ifPresent(h -> {
            h.forecastUpdate();
        });
    }

    @Override
    public synchronized List<SolarForecast> getSolarForecasts() {
        return List.of(forecast);
    }
}
