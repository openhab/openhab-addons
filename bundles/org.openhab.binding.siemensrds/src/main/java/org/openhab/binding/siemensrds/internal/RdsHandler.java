/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.siemensrds.internal;

import org.openhab.binding.siemensrds.internal.RdsDebouncer;

import static org.openhab.binding.siemensrds.internal.RdsBindingConstants.*;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.BridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RdsHandler} is the OpenHab Handler for Siemens RDS smart
 * thermostats
 * 
 * @author Andrew Fiddian-Green - Initial contribution
 * 
 */
public class RdsHandler extends BaseThingHandler {

    protected final Logger logger = LoggerFactory.getLogger(RdsHandler.class);

    private ScheduledFuture<?> lazyPollingScheduler;
    private ScheduledFuture<?> fastPollingScheduler;

    private final AtomicInteger fastPollingCallsToGo = new AtomicInteger();

    private RdsDebouncer debouncer = new RdsDebouncer();

    private RdsConfiguration config = null;

    private RdsDataPoints points = null;

    public RdsHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command != RefreshType.REFRESH) {
            doHandleCommand(channelUID.getId(), command);
        }
        startFastPollingBurst();
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_PENDING, "status => unknown..");

        config = getConfigAs(RdsConfiguration.class);

        if (config == null || config.plantId == null || config.plantId.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "missing Plant Id, status => offline!");
            return;
        }

        RdsCloudHandler cloud = getCloudHandler();

        if (cloud == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "missing cloud handler, status => offline!");
            return;
        }

        if (cloud.getThing().getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                    "cloud handler not online, status => offline!");
            return;
        }

        initializePolling();
    }

    public void initializePolling() {
        RdsCloudHandler cloud = getCloudHandler();

        if (cloud != null) {
            int pollInterval = cloud.getPollInterval();

            if (pollInterval > 0) {
                logger.info("creating polling timers..");

                // create a "lazy" polling scheduler
                if (lazyPollingScheduler == null || lazyPollingScheduler.isCancelled()) {
                    lazyPollingScheduler = scheduler.scheduleWithFixedDelay(this::lazyPollingSchedulerExecute,
                            pollInterval, pollInterval, TimeUnit.SECONDS);
                }

                // create a "fast" polling scheduler
                fastPollingCallsToGo.set(FAST_POLL_CYCLES);
                if (fastPollingScheduler == null || fastPollingScheduler.isCancelled()) {
                    fastPollingScheduler = scheduler.scheduleWithFixedDelay(this::fastPollingSchedulerExecute,
                            FAST_POLL_INTERVAL, FAST_POLL_INTERVAL, TimeUnit.SECONDS);
                }

                startFastPollingBurst();
            }
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            if (fastPollingScheduler == null) {
                initializePolling();
            }
        }
    }

    @Override
    public void dispose() {
        logger.info("disposing polling timers..");

        // clean up the lazy polling scheduler
        if (lazyPollingScheduler != null && !lazyPollingScheduler.isCancelled()) {
            lazyPollingScheduler.cancel(true);
            lazyPollingScheduler = null;
        }

        // clean up the fast polling scheduler
        if (fastPollingScheduler != null && !fastPollingScheduler.isCancelled()) {
            fastPollingScheduler.cancel(true);
            fastPollingScheduler = null;
        }
    }

    /*
     * private method: initiate a burst of fast polling requests
     */
    public void startFastPollingBurst() {
        fastPollingCallsToGo.set(FAST_POLL_CYCLES);
    }

    /*
     * private method: this is the callback used by the lazy polling scheduler..
     * polls for the info for all points
     */
    private synchronized void lazyPollingSchedulerExecute() {
        doPollNow();
        if (fastPollingCallsToGo.get() > 0) {
            fastPollingCallsToGo.decrementAndGet();
        }
    }

    /*
     * private method: this is the callback used by the fast polling scheduler..
     * checks if a fast polling burst is scheduled, and if so calls
     * lazyPollingSchedulerExecute
     */
    private void fastPollingSchedulerExecute() {
        if (fastPollingCallsToGo.get() > 0) {
            lazyPollingSchedulerExecute();
        }
    }

    /*
     * private method: send request to the cloud server for a new list of data point
     * states
     */
    private void doPollNow() {
        RdsCloudHandler cloud = getCloudHandler();

        if (cloud == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "missing cloud handler, status => offline!");
            return;
        }

        if (cloud.getThing().getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                    "cloud handler offline, status => offline!");
            return;
        }

        String apiKey = cloud.getApiKey();
        String token = cloud.getToken();

        if (points == null || (!points.refresh(apiKey, token))) {
            points = RdsDataPoints.create(apiKey, token, config.plantId);
        }

        if (points == null) {
            if (getThing().getStatus() == ThingStatus.ONLINE) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        String.format("server has no info for %s, status => offline!", getThing().getLabel()));
            }
            return;
        }

        if (!points.isOnline()) {
            if (getThing().getStatus() == ThingStatus.ONLINE) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        String.format("server reports %s offline, status => offline!", getThing().getLabel()));
            }
            return;
        }

        if (getThing().getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE,
                    String.format("received info for %s from cloud server, status => online..", getThing().getLabel()));
        }

        for (ChannelMap chan : CHAN_MAP) {
            if (debouncer.timeExpired(chan.channelId)) {
                State state = null;

                switch (chan.channelId) {
                case CHA_ROOM_TEMP:
                case CHA_ROOM_HUMIDITY:
                case CHA_OUTSIDE_TEMP:
                case CHA_TARGET_TEMP: {
                    state = points.getRaw(chan.hierarchyName);
                    break;
                }
                case CHA_ROOM_AIR_QUALITY:
                case CHA_ENERGY_SAVINGS_LEVEL: {
                    state = points.getEnum(chan.hierarchyName);
                    break;
                }
                case CHA_OUTPUT_STATE: {
                    state = points.getEnum(chan.hierarchyName);
                    /*
                     * convert the state text "Neither" to the more easy to understand word "Off"
                     */
                    if (state.toString().equals(STATE_NEITHER)) {
                        state = new StringType(STATE_OFF);
                    }
                    break;
                }
                case CHA_STAT_AUTO_MODE: {
                    state = OnOffType.from(points.getPresPrio(chan.hierarchyName) > 13
                            || points.asInt(HIE_STAT_OCC_MODE_PRESENT) == 2);
                    break;
                }
                case CHA_STAT_OCC_MODE_PRESENT: {
                    state = OnOffType.from(points.asInt(chan.hierarchyName) == 3);
                    break;
                }
                case CHA_DHW_AUTO_MODE: {
                    state = OnOffType.from(points.getPresPrio(chan.hierarchyName) > 13);
                    break;
                }
                case CHA_DHW_OUTPUT_STATE: {
                    state = OnOffType.from(points.asInt(chan.hierarchyName) == 2);
                    break;
                }
                }

                if (state != null) {
                    updateState(chan.channelId, state);
                }
            }
        }
    }

    /*
     * private method: sends a new channel value to the cloud server
     */
    private synchronized void doHandleCommand(String channelId, Command command) {
        RdsCloudHandler cloud = getCloudHandler();
        if (cloud != null && points == null) {
            points = RdsDataPoints.create(cloud.getApiKey(), cloud.getToken(), config.plantId);
        }

        if (points != null && cloud != null) {
            for (ChannelMap chan : CHAN_MAP) {
                if (channelId.equals(chan.channelId)) {
                    switch (chan.channelId) {
                    case CHA_TARGET_TEMP: {
                        points.setValue(cloud.getApiKey(), cloud.getToken(), chan.hierarchyName, command.format("%s"));
                        debouncer.initialize(channelId);
                        break;
                    }
                    case CHA_STAT_AUTO_MODE: {
                        /*
                         * this command is particularly funky.. use Green Leaf = 5 to set to Auto, and
                         * use Comfort Button = 1 to set to Manual
                         */
                        if (command == OnOffType.ON) {
                            points.setValue(cloud.getApiKey(), cloud.getToken(), HIE_ENERGY_SAVINGS_LEVEL, "5");
                        } else {
                            points.setValue(cloud.getApiKey(), cloud.getToken(), HIE_STAT_CMF_BTN, "1");
                        }
                        debouncer.initialize(channelId);
                        break;
                    }
                    case CHA_STAT_OCC_MODE_PRESENT: {
                        points.setValue(cloud.getApiKey(), cloud.getToken(), chan.hierarchyName,
                                command == OnOffType.OFF ? "2" : "3");
                        debouncer.initialize(channelId);
                        break;
                    }
                    case CHA_DHW_AUTO_MODE: {
                        if (command == OnOffType.ON) {
                            points.setValue(cloud.getApiKey(), cloud.getToken(), chan.hierarchyName, "0");
                        } else {
                            points.setValue(cloud.getApiKey(), cloud.getToken(), chan.hierarchyName,
                                    String.valueOf(points.asInt(chan.hierarchyName)));
                        }
                        debouncer.initialize(channelId);
                        break;
                    }
                    case CHA_DHW_OUTPUT_STATE: {
                        points.setValue(cloud.getApiKey(), cloud.getToken(), chan.hierarchyName,
                                command == OnOffType.OFF ? "1" : "2");
                        debouncer.initialize(channelId);
                        break;
                    }
                    case CHA_ROOM_TEMP:
                    case CHA_ROOM_HUMIDITY:
                    case CHA_OUTSIDE_TEMP:
                    case CHA_ROOM_AIR_QUALITY:
                    case CHA_OUTPUT_STATE: {
                        logger.debug("error: unexpected command to channel {}", chan.channelId);
                        break;
                    }
                    }
                    break;
                }
            }
        }
    }

    /*
     * private method: returns the cloud handler
     */
    private RdsCloudHandler getCloudHandler() {
        @Nullable Bridge b;

        @Nullable BridgeHandler h;

        if ((b = getBridge()) != null && (h = b.getHandler()) != null && h instanceof RdsCloudHandler) {
            return (RdsCloudHandler) h;
        }

        return null;
    }

}
