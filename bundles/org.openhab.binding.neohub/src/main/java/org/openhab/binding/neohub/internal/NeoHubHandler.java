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
package org.openhab.binding.neohub.internal;

import static org.openhab.binding.neohub.internal.NeoHubBindingConstants.*;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NeoHubHandler} is the OpenHab Handler for NeoHub devices 
 *
 * @author Andrew Fiddian-Green - Initial contribution (v2.x binding code)
 * @author Sebastian Prehn - Initial contribution (v1.x hub communication)
 * 
*/
public class NeoHubHandler extends BaseBridgeHandler {

    private static final Logger logger = 
            LoggerFactory.getLogger(NeoHubHandler.class);

    private NeoHubConfiguration config;
    private NeoHubSocket socket;

    private ScheduledFuture<?> lazyPollingScheduler;
    private ScheduledFuture<?> fastPollingScheduler;

    private final AtomicInteger fastPollingCallsToGo = new AtomicInteger();


    public NeoHubHandler(Bridge bridge) {
        super(bridge);
    }


    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // future: currently there is nothing to do for a NeoHub
    }


    @Override
    public void initialize() {
        String msg;

        msg = "status => unknown..";
        logger.info(msg);
        updateStatus(ThingStatus.UNKNOWN, 
            ThingStatusDetail.CONFIGURATION_PENDING, msg);

        config = getConfigAs(NeoHubConfiguration.class);

        if (config == null) {
            msg = "missing configuration, status => offline!";
            logger.error(msg);
            updateStatus(ThingStatus.OFFLINE, 
                ThingStatusDetail.CONFIGURATION_PENDING, msg);
            return;
        }

        if (logger.isDebugEnabled()) 
            logger.debug("hostname={}", config.hostName);

        if (config.hostName.isEmpty()) {
            msg = "missing host name, status => offline!";
            logger.error(msg);
            updateStatus(ThingStatus.OFFLINE, 
                ThingStatusDetail.CONFIGURATION_ERROR, msg);
            return;
        }

        if (logger.isDebugEnabled()) 
            logger.debug("port={}", config.portNumber);

        if (config.portNumber <= 0 || config.portNumber > 0xFFFF) {
            msg = "port out of range, status => offline!";
            logger.error(msg);
            updateStatus(ThingStatus.OFFLINE, 
                ThingStatusDetail.CONFIGURATION_ERROR, msg);
            return;
        }

        if (logger.isDebugEnabled()) 
            logger.debug("polling interval={}", config.pollInterval);

        if (config.pollInterval < FAST_POLL_INTERVAL || config.pollInterval > LAZY_POLL_INTERVAL) {
            msg = String.format(
                    "polling interval out of range [%d..%d], status => offline!",
                    FAST_POLL_INTERVAL, LAZY_POLL_INTERVAL);
            logger.error(msg);
            updateStatus(ThingStatus.OFFLINE, 
                ThingStatusDetail.CONFIGURATION_ERROR, msg);
            return;
        }

        socket = new NeoHubSocket(config.hostName, config.portNumber); 

        if (logger.isDebugEnabled()) 
            logger.debug("start background polling..");

        // create a "lazy" polling scheduler
        if (lazyPollingScheduler == null || lazyPollingScheduler.isCancelled()) { 
            lazyPollingScheduler = 
                scheduler.scheduleWithFixedDelay(this::lazyPollingSchedulerExecute, 
                    config.pollInterval, config.pollInterval, TimeUnit.SECONDS);
        }

        // create a "fast" polling scheduler
        fastPollingCallsToGo.set(FAST_POLL_CYCLES);
        if (fastPollingScheduler == null || fastPollingScheduler.isCancelled()) { 
            fastPollingScheduler = 
                scheduler.scheduleWithFixedDelay(this::fastPollingSchedulerExecute, 
                FAST_POLL_INTERVAL, FAST_POLL_INTERVAL, TimeUnit.SECONDS);
        }
        
        // start a fast polling burst to ensure the NeHub is initialized quickly
        startFastPollingBurst();
    }
    
   
    @Override
    public void dispose() {
        if (logger.isDebugEnabled()) 
            logger.debug("stop background polling..");

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
     * device handlers call this to initiate a burst of fast polling requests
     * ( improves response time to users when OpenHAB changes a channel value )
     */
    public void startFastPollingBurst() {
        fastPollingCallsToGo.set(FAST_POLL_CYCLES);
    }

    
    /*
     * device handlers call this method to issue commands to the NeoHub 
     */
    public synchronized NeoHubReturnResult toNeoHubSendChannelValue(
            String commandStr) {
        String msg;
            
        if (socket == null || config == null) {
            msg = "hub not initialized, status => offline!";
            logger.error(msg);
            updateStatus(ThingStatus.OFFLINE, 
                ThingStatusDetail.HANDLER_INITIALIZING_ERROR, msg);
            return NeoHubReturnResult.ERR_INITIALIZATION;
        }
        
        if (logger.isDebugEnabled()) 
            logger.debug("sending command {}", commandStr);
        
        if (socket.sendMessage(commandStr) == null) {
            msg = "communication error, status => offline!";
            logger.error(msg);
            updateStatus(ThingStatus.OFFLINE, 
                ThingStatusDetail.COMMUNICATION_ERROR, msg);
            return NeoHubReturnResult.ERR_COMMUNICATION;
        }

        // start a fast polling burst (to confirm the status change) 
        startFastPollingBurst();
        
        return NeoHubReturnResult.SUCCEEDED;
    }
    

    /* 
     * sends a JSON "INFO" request to the NeoHub 
     * returns a JSON string that contains the full status of all devices
     */
    protected @Nullable NeoHubInfoResponse fromNeoHubFetchPollingResponse() {
        String msg;
        @Nullable String response;
        @Nullable NeoHubInfoResponse result; 
        
        if (socket == null || config == null) {
            msg = "hub not initialized, status => offline!";
            logger.error(msg);
            updateStatus(ThingStatus.OFFLINE, 
                ThingStatusDetail.HANDLER_INITIALIZING_ERROR, msg);
            return null;
        }

        if ((response = socket.sendMessage(CMD_CODE_INFO)) == null) { 
            msg = "communication error, status => offline!";
            logger.error(msg);
            updateStatus(ThingStatus.OFFLINE, 
                ThingStatusDetail.COMMUNICATION_ERROR, msg);
            return null;
        }
        
        try {
            result = NeoHubInfoResponse.createInfoResponse(response);
            if (getThing().getStatus() != ThingStatus.ONLINE) {  
                msg = "info received, status => online..";
                logger.info(msg);
                updateStatus(ThingStatus.ONLINE,
                    ThingStatusDetail.NONE, msg);
            }
            return result;
        } 
        catch (Exception e) {
            msg = String.format(
                    "parsing error, cause = %s, status => offline..", 
                    e.getMessage());
            logger.error(msg);
            updateStatus(ThingStatus.OFFLINE,
                ThingStatusDetail.COMMUNICATION_ERROR, msg);
            return null;
        }
    }
    
    
    /*
     * this is the callback used by the lazy polling scheduler..
     * fetches the info for all devices from the NeoHub, 
     * and passes the results the respective device handlers
     */
    private synchronized void lazyPollingSchedulerExecute() {
        @Nullable
        NeoHubInfoResponse pollResponse = fromNeoHubFetchPollingResponse();

        if (pollResponse != null) { 
            List<Thing> children = getThing().getThings();
                
            // dispatch the infoResponse to each of the hub's owned devices ..
            for (Thing child : children) {
                ThingHandler device = child.getHandler();
                if (device instanceof NeoBaseHandler) {
                    ((NeoBaseHandler) device)
                        .toBaseSendPollResponse(pollResponse);
                }    
           }
        }

        if (fastPollingCallsToGo.get() > 0) {
            fastPollingCallsToGo.decrementAndGet();
        }
    }   
    
    /*
     * this is the callback used by the fast polling scheduler..
     * checks if a fast polling burst is scheduled, and if so calls 
     * lazyPollingSchedulerExecute  
     */
    private void fastPollingSchedulerExecute() {
        if (fastPollingCallsToGo.get() > 0) {
            lazyPollingSchedulerExecute();
        }
    }

}
