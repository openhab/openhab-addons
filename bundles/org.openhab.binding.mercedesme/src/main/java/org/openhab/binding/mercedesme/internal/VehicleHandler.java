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
package org.openhab.binding.mercedesme.internal;

import static org.openhab.binding.mercedesme.internal.Constants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openhab.binding.mercedesme.internal.utils.ChannelStateMap;
import org.openhab.binding.mercedesme.internal.utils.Mapper;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VehicleHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class VehicleHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(VehicleHandler.class);

    private List<String> functions;
    private Optional<ScheduledFuture<?>> refreshJob = Optional.empty();
    private Optional<AccountHandler> accountHandler = Optional.empty();;
    private Optional<VehicleConfiguration> config = Optional.empty();
    private final HttpClient hc;
    private final String uid;
    private Optional<ChannelStateMap> rangeFuel = Optional.empty();
    private Optional<ChannelStateMap> rangeElectric = Optional.empty();

    public VehicleHandler(Thing thing, HttpClientFactory hcf, String uid) {
        super(thing);
        hc = hcf.getCommonHttpClient();
        this.uid = uid;
        functions = new ArrayList<>(List.of(ODO_URL, STATUS_URL, LOCK_URL));
        switch (uid) {
            case COMBUSTION:
                functions.add(FUEL_URL);
                break;
            case HYBRID:
                functions.add(FUEL_URL);
                functions.add(EV_URL);
                break;
            case BEV:
                functions.add(EV_URL);
                break;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        config = Optional.of(getConfigAs(VehicleConfiguration.class));
        updateStatus(ThingStatus.UNKNOWN);
        Bridge bridge = getBridge();
        if (bridge != null) {
            BridgeHandler handler = bridge.getHandler();
            if (handler != null) {
                accountHandler = Optional.of((AccountHandler) handler);
            } else {
                logger.warn("Bridge Handler null");
            }
        } else {
            logger.warn("Bridge null");
        }
        startSchedule(config.get().refreshInterval);
        updateStatus(ThingStatus.ONLINE);
    }

    private void startSchedule(int interval) {
        refreshJob.ifPresentOrElse(job -> {
            if (job.isCancelled()) {
                refreshJob = Optional
                        .of(scheduler.scheduleWithFixedDelay(this::getData, 0, interval, TimeUnit.MINUTES));
            } // else - scheduler is already running!
        }, () -> {
            refreshJob = Optional.of(scheduler.scheduleWithFixedDelay(this::getData, 0, interval, TimeUnit.MINUTES));
        });
    }

    @Override
    public void dispose() {
        refreshJob.ifPresent(job -> job.cancel(true));
    }

    public void getData() {
        if (!accountHandler.isEmpty()) {
            // Mileage for all cars
            String odoUrl = String.format(ODO_URL, config.get().vin);
            call(odoUrl);

            // Electric status for hybrid and electric
            if (uid.equals(BEV) || uid.equals(HYBRID)) {
                String evUrl = String.format(EV_URL, config.get().vin);
                call(evUrl);
            }

            // Fuel for hybrid and combustion
            if (uid.equals(COMBUSTION) || uid.equals(HYBRID)) {
                String evUrl = String.format(FUEL_URL, config.get().vin);
                call(evUrl);
            }

            // Status and Lock for all
            String statusUrl = String.format(STATUS_URL, config.get().vin);
            call(statusUrl);
            String lockUrl = String.format(LOCK_URL, config.get().vin);
            call(lockUrl);

            // Range radius for all types
            updateRadius();
        } else {
            logger.warn("AccountHandler not set");
        }
    }

    private void call(String url) {
        Request req = hc.newRequest(String.format(url, config.get().vin));
        req.header(HttpHeader.AUTHORIZATION, "Bearer " + accountHandler.get().getToken());
        ContentResponse cr;
        try {
            cr = req.send();
            logger.info("Response {} {}", cr.getStatus(), cr.getContentAsString());
            JSONArray ja = new JSONArray(cr.getContentAsString());
            ja.forEach(entry -> {
                JSONObject jo = (JSONObject) entry;
                ChannelStateMap csm = Mapper.getChannelStateMap(jo);
                if (csm != null) {
                    updateChannel(csm);
                    if (csm.getChannel().equals("range-electric")) {
                        rangeElectric = Optional.of(csm);
                    } else if (csm.getChannel().equals("range-fuel")) {
                        rangeFuel = Optional.of(csm);
                    }
                } else {
                    logger.warn("Unable to deliver state for {}", jo);
                }
            });
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.warn("Error getting data {}", e.getMessage());
        }
    }

    private void updateRadius() {
        if (rangeElectric.isPresent()) {
            // update electric radius
            ChannelStateMap radiusElectric = new ChannelStateMap("radius-electric", GROUP_RANGE,
                    guessRangeRadius(rangeElectric.get().getState().as(QuantityType.class)));
            updateChannel(radiusElectric);
            if (rangeFuel.isPresent()) {
                // update fuel & hybrid radius
                ChannelStateMap radiusFuel = new ChannelStateMap("radius-fuel", GROUP_RANGE,
                        guessRangeRadius(rangeFuel.get().getState().as(QuantityType.class)));
                updateChannel(radiusFuel);
                int hybridKm = rangeElectric.get().getState().as(QuantityType.class).intValue()
                        + rangeFuel.get().getState().as(QuantityType.class).intValue();
                ChannelStateMap rangeHybrid = new ChannelStateMap("range-hybrid", GROUP_RANGE,
                        QuantityType.valueOf(hybridKm, KILOMETRE_UNIT));
                updateChannel(rangeHybrid);
                ChannelStateMap radiusHybrid = new ChannelStateMap("radius-hybrid", GROUP_RANGE,
                        guessRangeRadius(rangeHybrid.getState().as(QuantityType.class)));
                updateChannel(radiusHybrid);
            }
        } else if (rangeFuel.isPresent()) {
            // update fuel & hybrid radius
            ChannelStateMap radiusFuel = new ChannelStateMap("radius-fuel", GROUP_RANGE,
                    guessRangeRadius((QuantityType) rangeFuel.get().getState()));
            updateChannel(radiusFuel);
        }
    }

    /**
     * Easy function but there's some measures behind:
     * Guessing the range of the Vehicle on Map. If you can drive x kilometers with your Vehicle it's not feasible to
     * project this x km Radius on Map. The roads to be taken are causing some overhead because they are not a straight
     * line from Location A to B.
     * I've taken some measurements to calculate the overhead factor based on Google Maps
     * Berlin - Dresden: Road Distance: 193 air-line Distance 167 = Factor 87%
     * Kassel - Frankfurt: Road Distance: 199 air-line Distance 143 = Factor 72%
     * After measuring more distances you'll find out that the outcome is between 70% and 90%. So
     *
     * This depends also on the roads of a concrete route but this is only a guess without any Route Navigation behind
     *
     * @param range
     * @return mapping from air-line distance to "real road" distance
     */
    public static State guessRangeRadius(@Nullable QuantityType s) {
        if (s == null) {
            return UnDefType.UNDEF;
        }
        double radius = s.intValue() * 0.8;
        return QuantityType.valueOf(radius, KILOMETRE_UNIT);
    }

    protected void updateChannel(ChannelStateMap csm) {
        updateState(new ChannelUID(thing.getUID(), csm.getGroup(), csm.getChannel()), csm.getState());
    }
}
