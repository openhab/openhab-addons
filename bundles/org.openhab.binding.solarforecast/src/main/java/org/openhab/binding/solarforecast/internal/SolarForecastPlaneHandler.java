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
package org.openhab.binding.solarforecast.internal;

import static org.openhab.binding.solarforecast.internal.SolarForecastBindingConstants.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
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
 * The {@link SolarForecastPlaneHandler} is a non active handler instance. It will be triggerer by the bridge.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class SolarForecastPlaneHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SolarForecastPlaneHandler.class);
    private final HttpClient httpClient;
    private ForecastObject forecast = new ForecastObject();
    private Optional<SolarForecastConfiguration> config = Optional.empty();
    private Optional<PointType> location = Optional.empty();

    public SolarForecastPlaneHandler(Thing thing, HttpClient hc) {
        super(thing);
        httpClient = hc;
    }

    @Override
    public void initialize() {
        SolarForecastConfiguration c = getConfigAs(SolarForecastConfiguration.class);
        config = Optional.of(c);
        Bridge bridge = getBridge();
        if (bridge != null) {
            BridgeHandler handler = bridge.getHandler();
            if (handler != null) {
                if (handler instanceof SolarForecastBridgeHandler) {
                    ((SolarForecastBridgeHandler) handler).addPlane(this);
                    location = Optional.of(((SolarForecastBridgeHandler) handler).getLocation());
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
        if (thing instanceof SolarForecastBridgeHandler) {
            ((SolarForecastBridgeHandler) thing).removePlane(this);
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
    ForecastObject fetchData() {
        if (!forecast.isValid() && location.isPresent()) {
            // https://api.forecast.solar/estimate/:lat/:lon/:dec/:az/:kwp
            String url = BASE_URL + location.get().getLatitude() + SLASH + location.get().getLongitude() + SLASH
                    + config.get().declination + SLASH + config.get().azimuth + SLASH + config.get().kwp;
            logger.info("Call {}", url);
            try {
                ContentResponse cr = httpClient.GET(url);
                if (cr.getStatus() == 200) {
                    forecast = new ForecastObject(cr.getContentAsString(), LocalDateTime.now());
                    logger.info("Fetched data {}", forecast.toString());
                    updateChannels(forecast);
                    updateState(new ChannelUID(thing.getUID(), CHANNEL_RAW),
                            StringType.valueOf(cr.getContentAsString()));
                }
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                logger.info("Call {} failed {}", url, e.getMessage());
            }
        }
        updateChannels(forecast);
        return forecast;
    }

    private void updateChannels(ForecastObject f) {
        updateState(new ChannelUID(thing.getUID(), SolarForecastBindingConstants.CHANNEL_ACTUAL),
                ForecastObject.getStateObject(f.getActualValue(LocalDateTime.now())));
        updateState(new ChannelUID(thing.getUID(), SolarForecastBindingConstants.CHANNEL_REMAINING),
                ForecastObject.getStateObject(f.getRemainingProduction(LocalDateTime.now())));
        updateState(new ChannelUID(thing.getUID(), SolarForecastBindingConstants.CHANNEL_TODAY),
                ForecastObject.getStateObject(f.getDayTotal(LocalDateTime.now(), 0)));
        updateState(new ChannelUID(thing.getUID(), SolarForecastBindingConstants.CHANNEL_TOMORROW),
                ForecastObject.getStateObject(f.getDayTotal(LocalDateTime.now(), 1)));
    }

    protected void setLocation(PointType loc) {
        location = Optional.of(loc);
    }
}
