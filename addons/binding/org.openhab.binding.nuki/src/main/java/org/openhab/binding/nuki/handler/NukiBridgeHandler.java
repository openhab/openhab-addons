/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nuki.handler;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.nuki.internal.dataexchange.BridgeInfoResponse;
import org.openhab.binding.nuki.internal.dataexchange.NukiHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NukiBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Markus Katter - Initial contribution
 */
public class NukiBridgeHandler extends BaseBridgeHandler {

    private final static Logger logger = LoggerFactory.getLogger(NukiBridgeHandler.class);
    private final static int RETRYJOB_INTERVAL = 60;

    private NukiHttpClient nukiHttpClient;
    private ScheduledFuture<?> retryJob;

    public NukiBridgeHandler(Bridge bridge) {
        super(bridge);
        logger.trace("Instantiating NukiBridgeHandler({})", bridge);
    }

    public NukiHttpClient getNukiHttpClient() {
        return nukiHttpClient;
    }

    @Override
    public void initialize() {
        logger.debug("NukiBridgeHandler:initialize()");
        scheduler.execute(() -> initializeHandler());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("NukiBridgeHandler:handleCommand({}, {})", channelUID, command);
    }

    @Override
    public void dispose() {
        logger.debug("NukiBridgeHandler:dispose()");
        nukiHttpClient.stopClient();
        nukiHttpClient = null;
        cancelRetryJob();
    }

    private void initializeHandler() {
        logger.debug("NukiBridgeHandler:initializeHandler()");
        if (nukiHttpClient == null) {
            nukiHttpClient = new NukiHttpClient(getConfig());
        }
        BridgeInfoResponse bridgeInfoResponse = nukiHttpClient.getBridgeInfo();
        if (bridgeInfoResponse.getStatus() == HttpStatus.OK_200) {
            updateStatus(ThingStatus.ONLINE);
            cancelRetryJob();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, bridgeInfoResponse.getMessage());
            scheduleRetryJob();
        }
    }

    private void scheduleRetryJob() {
        logger.trace("NukiBridgeHandler:scheduleRetryJob():Scheduling retryJob in {}secs for Bridge.",
                RETRYJOB_INTERVAL);
        if (retryJob != null && !retryJob.isDone()) {
            logger.trace("NukiBridgeHandler:scheduleRetryJob():Already scheduled for Bridge.");
            return;
        }
        retryJob = null;
        retryJob = scheduler.schedule(() -> {
            initialize();
        }, RETRYJOB_INTERVAL, TimeUnit.SECONDS);
    }

    private void cancelRetryJob() {
        logger.trace("NukiBridgeHandler:cancelRetryJob():Canceling retryJob for Bridge.");
        if (retryJob != null) {
            retryJob.cancel(true);
            retryJob = null;
        }
    }

}
