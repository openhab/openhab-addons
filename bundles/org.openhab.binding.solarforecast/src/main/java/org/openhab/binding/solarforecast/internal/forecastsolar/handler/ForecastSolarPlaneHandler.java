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
package org.openhab.binding.solarforecast.internal.forecastsolar.handler;

import static org.openhab.binding.solarforecast.internal.SolarForecastBindingConstants.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.openhab.binding.solarforecast.internal.SolarForecastBindingConstants;
import org.openhab.binding.solarforecast.internal.actions.SolarForecast;
import org.openhab.binding.solarforecast.internal.actions.SolarForecastActions;
import org.openhab.binding.solarforecast.internal.actions.SolarForecastProvider;
import org.openhab.binding.solarforecast.internal.forecastsolar.ForecastSolarObject;
import org.openhab.binding.solarforecast.internal.forecastsolar.config.ForecastSolarPlaneConfiguration;
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
 * The {@link ForecastSolarPlaneHandler} is a non active handler instance. It will be triggerer by the bridge.
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
    private ForecastSolarObject forecast = new ForecastSolarObject();
    private int failureCounter = 0;

    public ForecastSolarPlaneHandler(Thing thing, HttpClient hc) {
        super(thing);
        httpClient = hc;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(SolarForecastActions.class);
    }

    @Override
    public void initialize() {
        ForecastSolarPlaneConfiguration c = getConfigAs(ForecastSolarPlaneConfiguration.class);
        configuration = Optional.of(c);
        Bridge bridge = getBridge();
        if (bridge != null) {
            BridgeHandler handler = bridge.getHandler();
            if (handler != null) {
                if (handler instanceof ForecastSolarBridgeHandler) {
                    bridgeHandler = Optional.of((ForecastSolarBridgeHandler) handler);
                    bridgeHandler.get().addPlane(this);
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                            "@text/solarforecast.site.status.wrong-handler" + " [\"" + handler + "\"]");
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                        "@text/solarforecast.site.status.bridge-handler-not-found");
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                    "@text/solarforecast.site.status.bridge-missing");
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

    /**
     * https://doc.forecast.solar/doku.php?id=api:estimate
     */
    protected ForecastSolarObject fetchData() {
        if (location.isPresent()) {
            if (!forecast.isValid()) {
                String url;
                if (apiKey.isEmpty()) {
                    // use public API
                    // https://api.forecast.solar/estimate/:lat/:lon/:dec/:az/:kwp
                    url = BASE_URL + "estimate/" + location.get().getLatitude() + SLASH + location.get().getLongitude()
                            + SLASH + configuration.get().declination + SLASH + configuration.get().azimuth + SLASH
                            + configuration.get().kwp + "?damping=" + configuration.get().dampAM + ","
                            + configuration.get().dampPM;
                } else {
                    // use paid API
                    // https://api.forecast.solar/:apikey/estimate/:lat/:lon/:dec/:az/:kwp
                    url = BASE_URL + apiKey.get() + "/estimate/" + location.get().getLatitude() + SLASH
                            + location.get().getLongitude() + SLASH + configuration.get().declination + SLASH
                            + configuration.get().azimuth + SLASH + configuration.get().kwp + "?damping="
                            + configuration.get().dampAM + "," + configuration.get().dampPM;
                }
                if (!SolarForecastBindingConstants.EMPTY.equals(configuration.get().horizon)) {
                    url += "?horizon=" + configuration.get().horizon;
                }
                try {
                    ContentResponse cr = httpClient.GET(url);
                    if (cr.getStatus() == 200) {
                        ForecastSolarObject localForecast = new ForecastSolarObject(cr.getContentAsString(),
                                Instant.now().plus(configuration.get().refreshInterval, ChronoUnit.MINUTES));
                        setForecast(localForecast);
                        failureCounter = 0;
                        updateState(CHANNEL_RAW, StringType.valueOf(cr.getContentAsString()));
                    } else {
                        logger.info("{} Call {} failed {}", thing.getLabel(), url, cr.getStatus());
                        failureCounter++;
                    }
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    logger.info("{} Call {} failed {}", thing.getLabel(), url, e.getMessage());
                }
            } // else use available forecast
            updateChannels(forecast);
        } else {
            logger.info("{} Location not present", thing.getLabel());
        }
        return forecast;
    }

    private void updateChannels(ForecastSolarObject f) {
        LocalDateTime now = LocalDateTime.now();
        updateState(CHANNEL_ACTUAL, Utils.getEnergyState(f.getActualValue(now)));
        updateState(CHANNEL_ACTUAL_POWER, Utils.getPowerState(f.getActualPowerValue(now)));
        updateState(CHANNEL_REMAINING, Utils.getEnergyState(f.getRemainingProduction(now)));
        updateState(CHANNEL_TODAY, Utils.getEnergyState(f.getDayTotal(now.toLocalDate())));
        updateState(CHANNEL_DAY1, Utils.getEnergyState(f.getDayTotal(now.plusDays(1).toLocalDate())));
        updateState(CHANNEL_DAY2, Utils.getEnergyState(f.getDayTotal(now.plusDays(2).toLocalDate())));
        updateState(CHANNEL_DAY3, Utils.getEnergyState(f.getDayTotal(now.plusDays(3).toLocalDate())));
    }

    /**
     * Used by Bridge to set location directly
     *
     * @param loc
     */
    void setLocation(PointType loc) {
        location = Optional.of(loc);
    }

    /**
     * Used by Bridge to set location directly
     *
     * @param loc
     */
    void setApiKey(String key) {
        apiKey = Optional.of(key);
    }

    private synchronized void setForecast(ForecastSolarObject f) {
        forecast = f;
    }

    @Override
    public synchronized List<SolarForecast> getSolarForecasts() {
        return List.of(forecast);
    }
}
