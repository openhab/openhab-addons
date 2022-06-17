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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.types.Command;
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

    public VehicleHandler(Thing thing, HttpClientFactory hcf, String uid) {
        super(thing);
        hc = hcf.getCommonHttpClient();
        functions = new ArrayList<>(List.of(Constants.ODO_URL, Constants.STATUS_URL, Constants.LOCK_URL));
        switch (uid) {
            case Constants.COMBUSTION:
                functions.add(Constants.FUEL_URL);
                break;
            case Constants.HYBRID:
                functions.add(Constants.FUEL_URL);
                functions.add(Constants.EV_URL);
                break;
            case Constants.BEV:
                functions.add(Constants.EV_URL);
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
                logger.debug("Bridge Handler null");
            }
        } else {
            logger.debug("Bridge null");
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
            Iterator<String> iter = functions.iterator();
            while (iter.hasNext()) {
                String url = iter.next();
                Request req = hc.newRequest(String.format(url, config.get().vin));
                req.header(HttpHeader.AUTHORIZATION, "Bearer " + accountHandler.get().getToken());
                ContentResponse cr;
                try {
                    cr = req.send();
                    logger.info("Response {} {}", cr.getStatus(), cr.getContentAsString());
                } catch (InterruptedException | TimeoutException | ExecutionException e) {
                    logger.warn("Error getting data {}", e.getMessage());
                }
            }

        } else {
            logger.warn("AccountHandler not set");
        }
    }
}
