/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nuki.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.nuki.NukiBindingConstants;
import org.openhab.binding.nuki.internal.dataexchange.BridgeCallbackAddResponse;
import org.openhab.binding.nuki.internal.dataexchange.BridgeCallbackListResponse;
import org.openhab.binding.nuki.internal.dataexchange.BridgeCallbackRemoveResponse;
import org.openhab.binding.nuki.internal.dataexchange.BridgeInfoResponse;
import org.openhab.binding.nuki.internal.dataexchange.NukiApiServlet;
import org.openhab.binding.nuki.internal.dataexchange.NukiHttpClient;
import org.openhab.binding.nuki.internal.dto.BridgeApiCallbackListCallbackDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NukiBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Markus Katter - Initial contribution
 */
public class NukiBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(NukiBridgeHandler.class);
    private static final int RETRYJOB_INTERVAL = 60;

    private HttpClient httpClient;
    private NukiHttpClient nukiHttpClient;
    private NukiApiServlet nukiApiServlet;
    private String openHabIpPort;
    private ScheduledFuture<?> retryJob;
    String bridgeId;
    String bridgeIp;

    public NukiBridgeHandler(Bridge bridge, HttpClient httpClient, NukiApiServlet nukiApiServlet,
            String openHabIpPort) {
        super(bridge);
        logger.debug("Instantiating NukiBridgeHandler({}, {}, {}, {})", bridge, httpClient, nukiApiServlet,
                openHabIpPort);
        this.httpClient = httpClient;
        this.nukiApiServlet = nukiApiServlet;
        this.openHabIpPort = openHabIpPort;
        this.bridgeIp = (String) getConfig().get(NukiBindingConstants.CONFIG_IP);
    }

    public NukiHttpClient getNukiHttpClient() {
        return nukiHttpClient;
    }

    @Override
    public void initialize() {
        logger.debug("NukiBridgeHandler:initialize()");
        updateStatus(ThingStatus.UNKNOWN);
        scheduler.execute(() -> initializeHandler());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("NukiBridgeHandler:handleCommand({}, {})", channelUID, command);
    }

    @Override
    public void dispose() {
        logger.debug("NukiBridgeHandler:dispose()");
        nukiHttpClient = null;
        nukiApiServlet.deactivate();
        cancelRetryJob();
    }

    private synchronized void initializeHandler() {
        logger.debug("NukiBridgeHandler:initializeHandler()");
        if (nukiHttpClient == null) {
            nukiHttpClient = new NukiHttpClient(httpClient, getConfig());
        }
        BridgeInfoResponse bridgeInfoResponse = nukiHttpClient.getBridgeInfo();
        if (bridgeInfoResponse.getStatus() == HttpStatus.OK_200) {
            bridgeId = bridgeIdToHex(bridgeInfoResponse.getHardwareId());
            boolean manageCallbacks = (Boolean) getConfig().get(NukiBindingConstants.CONFIG_MANAGECB);
            if (manageCallbacks) {
                manageNukiBridgeCallbacks(bridgeId);
            }
            nukiApiServlet.activate(this, bridgeId);
            updateStatus(ThingStatus.ONLINE);
            cancelRetryJob();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, bridgeInfoResponse.getMessage());
            scheduleRetryJob();
        }
    }

    private void scheduleRetryJob() {
        logger.trace("NukiBridgeHandler:scheduleRetryJob():Scheduling retryJob in {}secs for Bridge[{}].",
                RETRYJOB_INTERVAL, (bridgeId == null) ? bridgeIp : bridgeId);
        if (retryJob != null && !retryJob.isDone()) {
            logger.trace("NukiBridgeHandler:scheduleRetryJob():Already scheduled for Bridge[{}].",
                    (bridgeId == null) ? bridgeIp : bridgeId);
            return;
        }
        retryJob = null;
        retryJob = scheduler.schedule(() -> {
            initialize();
        }, RETRYJOB_INTERVAL, TimeUnit.SECONDS);
    }

    private void cancelRetryJob() {
        logger.trace("NukiBridgeHandler:cancelRetryJob():Canceling retryJob for Bridge[{}].", bridgeId);
        if (retryJob != null) {
            retryJob.cancel(true);
            retryJob = null;
        }
    }

    private String bridgeIdToHex(int bridgeId) {
        String bridgeIdHex = String.format("%08X", bridgeId);
        logger.trace("bridgeIdToHex({}):bridgeIdHex[{}]", bridgeId, bridgeIdHex);
        return bridgeIdHex;
    }

    private String createCallbackUrl(String nukiId) {
        ArrayList<String> parameters = new ArrayList<>();
        parameters.add(openHabIpPort);
        parameters.add(nukiId);
        String callbackUrl = String.format(NukiBindingConstants.CALLBACK_URL, parameters.toArray());
        logger.trace("createCallbackUrl({}):callbackUrl[{}]", nukiId, callbackUrl);
        return callbackUrl;
    }

    private void manageNukiBridgeCallbacks(String bridgeId) {
        logger.debug("manageNukiBridgeCallbacks({})", bridgeId);
        BridgeCallbackListResponse bridgeCallbackListResponse = nukiHttpClient.getBridgeCallbackList();
        List<BridgeApiCallbackListCallbackDto> callbacks = bridgeCallbackListResponse.getCallbacks();
        String callbackUrl = createCallbackUrl(bridgeId);
        boolean callbackExists = false;
        int callbackCount = callbacks.size();
        for (BridgeApiCallbackListCallbackDto callback : callbacks) {
            if (callback.getUrl().equals(callbackUrl)) {
                logger.debug("callbackUrl[{}] already existing on Nuki Bridge[{}].", callbackUrl, bridgeId);
                callbackExists = true;
                continue;
            }
            if (callback.getUrl().contains(NukiBindingConstants.CALLBACK_ENDPOINT + bridgeId)) {
                logger.debug("Partial callbackUrl[{}] found on Nuki Bridge[{}] - Removing it!", callback.getUrl(),
                        bridgeId);
                BridgeCallbackRemoveResponse bridgeCallbackRemoveResponse = nukiHttpClient
                        .getBridgeCallbackRemove(callback.getId());
                if (bridgeCallbackRemoveResponse.getStatus() == HttpStatus.OK_200) {
                    logger.debug("Successfully removed!");
                    callbackCount--;
                }
            }
        }
        if (!callbackExists) {
            if (callbackCount == 3) {
                logger.debug("Already 3 callback URLs existing on Nuki Bridge[{}] - Removing ID 0!", bridgeId);
                BridgeCallbackRemoveResponse bridgeCallbackRemoveResponse = nukiHttpClient.getBridgeCallbackRemove(0);
                if (bridgeCallbackRemoveResponse.getStatus() == HttpStatus.OK_200) {
                    logger.debug("Successfully removed!");
                    callbackCount--;
                }
            }
            logger.debug("Adding callbackUrl[{}] to Nuki Bridge[{}]!", callbackUrl, bridgeId);
            BridgeCallbackAddResponse bridgeCallbackAddResponse = nukiHttpClient.getBridgeCallbackAdd(callbackUrl);
            if (bridgeCallbackAddResponse.getStatus() == HttpStatus.OK_200) {
                logger.debug("Successfully added!", callbackUrl, bridgeId);
                callbackExists = true;
            }
        }
    }

}
