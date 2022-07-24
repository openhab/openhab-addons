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
package org.openhab.binding.solarforecast.internal.forecastsolar;

import static org.openhab.binding.solarforecast.internal.SolarForecastBindingConstants.*;
import static org.openhab.binding.solarforecast.internal.forecastsolar.ForecastSolarConstants.BASE_URL;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.openhab.binding.solarforecast.internal.solcast.SolcastObject;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
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
public class ForecastSolarPlaneHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(ForecastSolarPlaneHandler.class);
    private final HttpClient httpClient;

    private Optional<ForecastSolarConfiguration> configuration = Optional.empty();
    private Optional<ForecastSolarBridgeHandler> bridgeHandler = Optional.empty();
    private Optional<PointType> location = Optional.empty();
    private Optional<String> apiKey = Optional.empty();
    private ForecastSolarObject forecast = new ForecastSolarObject();

    public ForecastSolarPlaneHandler(Thing thing, HttpClient hc) {
        super(thing);
        httpClient = hc;
    }

    @Override
    public void initialize() {
        ForecastSolarConfiguration c = getConfigAs(ForecastSolarConfiguration.class);
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
        logger.info("Handle command {} for channel {}", channelUID, command);
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
                            + configuration.get().kwp;
                } else {
                    // use paid API
                    // https://api.forecast.solar/:apikey/estimate/:lat/:lon/:dec/:az/:kwp
                    url = BASE_URL + apiKey.get() + "/estimate/" + location.get().getLatitude() + SLASH
                            + location.get().getLongitude() + SLASH + configuration.get().declination + SLASH
                            + configuration.get().azimuth + SLASH + configuration.get().kwp;
                }
                logger.info("{} Call {}", thing.getLabel(), url);
                try {
                    ContentResponse cr = httpClient.GET(url);
                    if (cr.getStatus() == 200) {
                        forecast = new ForecastSolarObject(cr.getContentAsString(), LocalDateTime.now(),
                                LocalDateTime.now().plusMinutes(configuration.get().refreshInterval));
                        logger.debug("{} Fetched data {}", thing.getLabel(), forecast.toString());
                        updateChannels(forecast);
                        updateState(CHANNEL_RAW, StringType.valueOf(cr.getContentAsString()));
                    } else {
                        logger.info("{} Call {} failed {}", thing.getLabel(), url, cr.getStatus());
                    }
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    logger.info("{} Call {} failed {}", thing.getLabel(), url, e.getMessage());
                }
            } else {
                logger.debug("{} use available forecast {}", thing.getLabel(), forecast);
            }
            updateChannels(forecast);
        } else {
            logger.info("{} Location not present", thing.getLabel());
        }
        return forecast;
    }

    private void updateChannels(ForecastSolarObject f) {
        updateState(CHANNEL_ACTUAL, SolcastObject.getStateObject(f.getActualValue(LocalDateTime.now())));
        updateState(CHANNEL_REMAINING, SolcastObject.getStateObject(f.getRemainingProduction(LocalDateTime.now())));
        updateState(CHANNEL_TODAY, SolcastObject.getStateObject(f.getDayTotal(LocalDateTime.now(), 0)));
        updateState(CHANNEL_TOMORROW, SolcastObject.getStateObject(f.getDayTotal(LocalDateTime.now(), 1)));
        updateState(CHANNEL_DAY2, SolcastObject.getStateObject(f.getDayTotal(LocalDateTime.now(), 2)));
        updateState(CHANNEL_DAY3, SolcastObject.getStateObject(f.getDayTotal(LocalDateTime.now(), 3)));
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

    /**
     * Used by SinglePlaneHandler to submit config data
     *
     * @param c
     */
    protected void setConfig(ForecastSolarConfiguration c) {
        logger.info("Config {}", c);
        configuration = Optional.of(c);
        location = Optional.of(PointType.valueOf(c.location));
        if (!EMPTY.equals(c.apiKey)) {
            apiKey = Optional.of(c.apiKey);
        }
    }
}
