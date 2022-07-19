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
    private final PointType location;
    private ForecastObject forecast = new ForecastObject();
    private Optional<SolarForecastConfiguration> config = Optional.empty();

    public SolarForecastPlaneHandler(Thing thing, HttpClient hc, PointType loc) {
        super(thing);
        httpClient = hc;
        location = loc;
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
        // TODO Auto-generated method stub
    }

    /**
     * https://doc.forecast.solar/doku.php?id=api:estimate
     */
    ForecastObject fetchData() {
        if (!forecast.isValid()) {
            // https://api.forecast.solar/estimate/:lat/:lon/:dec/:az/:kwp
            String url = BASE_URL + location.getLatitude() + SLASH + location.getLongitude() + SLASH
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
            return new ForecastObject();
        }
        // return old forecast - forecast object will interpolate values
        return forecast;
    }

    private void updateChannels(ForecastObject f) {
        updateState(new ChannelUID(thing.getUID(), SolarForecastBindingConstants.CHANNEL_ACTUAL),
                f.getCurrentValue(LocalDateTime.now()));
        updateState(new ChannelUID(thing.getUID(), SolarForecastBindingConstants.CHANNEL_REMAINING),
                f.getRemainingProduction(LocalDateTime.now()));
        updateState(new ChannelUID(thing.getUID(), SolarForecastBindingConstants.CHANNEL_TODAY), f.getDayTotal());
    }
}
