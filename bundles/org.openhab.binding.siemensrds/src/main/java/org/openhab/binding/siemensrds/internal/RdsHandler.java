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
 * The {@link RdsHandler} is the OpenHab Handler for 
 * Siemens RDS smart thermostats
 * 
 * @author Andrew Fiddian-Green - Initial contribution
 * 
 */
public class RdsHandler extends BaseThingHandler {

    protected static final Logger LOGGER = 
            LoggerFactory.getLogger(RdsHandler.class);

    private ScheduledFuture<?> lazyPollingScheduler;
    private ScheduledFuture<?> fastPollingScheduler;

    private final AtomicInteger fastPollingCallsToGo = new AtomicInteger();

    private RdsDebouncer debouncer = new RdsDebouncer();
    
    private String plantId = "";
    
    private RdsDataPoints points = null;

    public RdsHandler(Thing thing) { 
        super(thing);
    }


    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) { 
            startFastPollingBurst();
            return;
        }
        
        doHandleCommand(channelUID.getId(), command);
    }

    
    @Override
    public void initialize() {
        String msg; 
                
        msg = "status => unknown..";
        LOGGER.info(msg);
        updateStatus(ThingStatus.UNKNOWN, 
            ThingStatusDetail.CONFIGURATION_PENDING, msg);

        plantId = getThing().getProperties().get(PROP_PLANT_ID);

        if (plantId == null || plantId.isEmpty()) {
            msg = "missing Plant Id, status => offline!";
            LOGGER.error(msg);
            updateStatus(ThingStatus.OFFLINE, 
                ThingStatusDetail.CONFIGURATION_ERROR, msg);
            return;
        } 
        
        RdsCloudHandler cloud = getCloudHandler();

        if (cloud == null) {
            msg = "missing cloud handler, status => offline!";
            LOGGER.error(msg);
            updateStatus(ThingStatus.OFFLINE, 
                ThingStatusDetail.CONFIGURATION_ERROR, msg);
            return;
        }
        
        if (cloud.getThing().getStatus() != ThingStatus.ONLINE) {
            msg = "cloud handler not online, status => offline!";
            LOGGER.error(msg);
            updateStatus(ThingStatus.OFFLINE, 
                ThingStatusDetail.BRIDGE_OFFLINE, msg);
            return;
        }
        
        initializePolling();
    }
    
    
    public void initializePolling() {
        RdsCloudHandler cloud = getCloudHandler();

        if (cloud != null) {
            int pollInterval = cloud.getPollInterval();

            if (pollInterval > 0) {
                LOGGER.info("creating polling timers..");

                // create a "lazy" polling scheduler
                if (lazyPollingScheduler == null || lazyPollingScheduler.isCancelled()) { 
                    lazyPollingScheduler = 
                        scheduler.scheduleWithFixedDelay(this::lazyPollingSchedulerExecute, 
                            pollInterval, pollInterval, TimeUnit.SECONDS);
                }
        
                // create a "fast" polling scheduler
                fastPollingCallsToGo.set(FAST_POLL_CYCLES);
                if (fastPollingScheduler == null || fastPollingScheduler.isCancelled()) { 
                    fastPollingScheduler = 
                        scheduler.scheduleWithFixedDelay(this::fastPollingSchedulerExecute, 
                        FAST_POLL_INTERVAL, FAST_POLL_INTERVAL, TimeUnit.SECONDS);
                }
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
        LOGGER.info("disposing polling timers..");

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
     * private method:
     * initiate a burst of fast polling requests
     */
    public void startFastPollingBurst() {
        fastPollingCallsToGo.set(FAST_POLL_CYCLES);
    }

    
    /*
     * private method:
     * this is the callback used by the lazy polling scheduler..
     * polls for the info for all points 
     */
    private synchronized void lazyPollingSchedulerExecute() {
        doPollNow();
        if (fastPollingCallsToGo.get() > 0) {
            fastPollingCallsToGo.decrementAndGet();
        }
    }   
    
    /*
     * private method:
     * this is the callback used by the fast polling scheduler..
     * checks if a fast polling burst is scheduled, and if so calls 
     * lazyPollingSchedulerExecute  
     */
    private void fastPollingSchedulerExecute() {
        if (fastPollingCallsToGo.get() > 0) {
            lazyPollingSchedulerExecute();
        }
    }


    /*
     * private method:
     * send request to the cloud server for a new list of data point states
     */
    private void doPollNow() {
        String msg;

        RdsCloudHandler cloud = getCloudHandler();

        if (cloud == null) {
            msg = "missing cloud handler, status => offline!";
            LOGGER.error(msg);
            updateStatus(ThingStatus.OFFLINE, 
                ThingStatusDetail.CONFIGURATION_ERROR, msg);
            return;
        } 

        if (cloud.getThing().getStatus() != ThingStatus.ONLINE) {
            msg = "cloud handler offline, status => offline!";
            LOGGER.error(msg);
            updateStatus(ThingStatus.OFFLINE, 
                ThingStatusDetail.BRIDGE_OFFLINE, msg);
            return;
        }

        points = RdsDataPoints.create(cloud.getToken(), plantId);

        if (points == null) {
            if (getThing().getStatus() == ThingStatus.ONLINE) {   
                msg = String.format(
                        "server has no info for %s, status => offline!", 
                        getThing().getLabel());
                LOGGER.error(msg);
                updateStatus(ThingStatus.OFFLINE, 
                    ThingStatusDetail.CONFIGURATION_ERROR, msg);
            }
            return;
        }

        if (!points.isOnline()) {
            if (getThing().getStatus() == ThingStatus.ONLINE) {   
                msg = String.format(
                        "server reports %s offline, status => offline!", 
                        getThing().getLabel());
                LOGGER.error(msg);
                updateStatus(ThingStatus.OFFLINE, 
                    ThingStatusDetail.CONFIGURATION_ERROR, msg);
            }
            return;
        }

        if (getThing().getStatus() != ThingStatus.ONLINE) {   
            msg = String.format(
                    "received info for %s from cloud server, status => online..", 
                    getThing().getLabel());
            LOGGER.info(msg);
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, msg);
        }
        
        for (int i = 0; i < CHAN_MAP.length; i++) {
            if (debouncer.timeExpired(CHAN_MAP[i].channelId)) {
                State state;

                if (CHAN_MAP[i].pType == Ptype.ENUM) {
                    state = points.getEnum(CHAN_MAP[i].objectName);
                } else {
                    state = points.getRaw(CHAN_MAP[i].objectName);
                }

                if (state != null) { 
                    updateState(CHAN_MAP[i].channelId, state);
                }
            }
        }
            
    }
    
    /*
     * private method:
     * sends a new channel value to the cloud server 
     */
    private synchronized void doHandleCommand(String channelId, Command command) {
        RdsCloudHandler cloud = getCloudHandler();
        if (cloud != null && points == null) {
            points = RdsDataPoints.create(cloud.getToken(), plantId);
        }
        
        if (points != null && cloud != null) {
            for (int i = 0; i < CHAN_MAP.length; i++) {
                if (channelId.equals(CHAN_MAP[i].channelId)) {
                    // NOTE: command.format("%s") *should* work on any type :) 
                    points.setValue(cloud.getToken(),
                            CHAN_MAP[i].objectName, command.format("%s"));
                    debouncer.initialize(channelId);
                    break;
                }
            }
        }
    }


    /*
     * private method:
     * returns the cloud handler
     */
    private RdsCloudHandler getCloudHandler () {
        @Nullable
        Bridge b;

        @Nullable
        BridgeHandler h;

        if ((b = getBridge()) != null && (h = b.getHandler()) != null 
                && h instanceof RdsCloudHandler) { 
            return (RdsCloudHandler) h;
        }

        return null;
    }

}
