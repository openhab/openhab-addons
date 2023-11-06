/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.fronius.internal.handler;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.fronius.internal.FroniusBridgeConfiguration;
import org.openhab.binding.fronius.internal.FroniusCommunicationException;
import org.openhab.binding.fronius.internal.FroniusHttpUtil;
import org.openhab.binding.fronius.internal.api.BaseFroniusResponse;
import org.openhab.binding.fronius.internal.api.HeadStatus;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Basic Handler class for all Fronius services.
 *
 * @author Gerrit Beine - Initial contribution
 * @author Thomas Rokohl - Refactoring to merge the concepts
 * @author Thomas Kordelle - Added inverter power, battery state of charge and PV solar yield
 * @author Jimmy Tanagra - Implement connection retry
 *         Convert ValueUnit to QuantityType
 *         Support NULL value
 */
public abstract class FroniusBaseThingHandler extends BaseThingHandler {

    private static final int API_TIMEOUT = 5000;
    private final Logger logger = LoggerFactory.getLogger(FroniusBaseThingHandler.class);
    private final String serviceDescription;
    private FroniusBridgeHandler bridgeHandler;
    private final Gson gson;

    public FroniusBaseThingHandler(Thing thing) {
        super(thing);
        gson = new Gson();
        serviceDescription = getDescription();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateChannel(channelUID.getId());
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing {} Service", serviceDescription);
        // this is important so FroniusBridgeHandler::childHandlerInitialized gets called
        Bridge bridge = getBridge();
        if (bridge == null || bridge.getHandler() == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
        } else if (bridge.getStatus() == ThingStatus.ONLINE) {
            updateStatus(ThingStatus.UNKNOWN);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    /**
     * Update all Channels
     */
    protected void updateChannels() {
        for (Channel channel : getThing().getChannels()) {
            updateChannel(channel.getUID().getId());
        }
    }

    /**
     * Update the channel from the last data
     *
     * @param channelId the id identifying the channel to be updated
     */
    protected void updateChannel(String channelId) {
        if (!isLinked(channelId)) {
            return;
        }

        State state = getValue(channelId);
        if (state == null) {
            state = UnDefType.NULL;
        }

        if (logger.isTraceEnabled()) {
            logger.trace("Update channel {} with state {} ({})", channelId, state.toString(),
                    state.getClass().getSimpleName());
        }
        updateState(channelId, state);
    }

    /**
     * return an internal description for logging
     *
     * @return the description of the thing
     */
    protected abstract String getDescription();

    /**
     * get the "new" associated value for a channelId
     *
     * @param channelId the id identifying the channel
     * @return the "new" associated value
     */
    protected abstract State getValue(String channelId);

    /**
     * Called by the bridge to fetch data and update channels
     *
     * @param bridgeConfiguration the connected bridge configuration
     */
    public void refresh(FroniusBridgeConfiguration bridgeConfiguration) {
        try {
            handleRefresh(bridgeConfiguration);
            if (getThing().getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (FroniusCommunicationException | RuntimeException e) {
            logger.debug("Exception caught in refresh() for {}", getThing().getUID().getId(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    /**
     * This method should be overridden to do whatever a thing must do to update its channels
     * this function is called from the bridge in a given interval
     *
     * @param bridgeConfiguration the connected bridge configuration
     */
    protected abstract void handleRefresh(FroniusBridgeConfiguration bridgeConfiguration)
            throws FroniusCommunicationException;

    /**
     *
     * @param type response class type
     * @param url to request
     * @return the object representation of the json response
     */
    protected @NonNull <T extends BaseFroniusResponse> T collectDataFromUrl(Class<T> type, String url)
            throws FroniusCommunicationException {
        try {
            int attempts = 1;
            while (true) {
                logger.trace("Fetching URL = {}", url);
                String response = FroniusHttpUtil.executeUrl(url, API_TIMEOUT);
                logger.trace("aqiResponse = {}", response);

                T result = gson.fromJson(response, type);
                if (result == null) {
                    throw new FroniusCommunicationException("Empty json result");
                }

                HeadStatus status = result.getHead().getStatus();
                if (status.getCode() == 0) {
                    return result;
                }

                // Sometimes Fronius would return a HTTP status 200 with a proper JSON data
                // with Reason: Transfer timeout.
                //
                // "Status" : {
                // "Code" : 8,
                // "Reason" : "Transfer timeout.",
                // "UserMessage" : ""
                // },
                logger.debug("Error from Fronius attempt #{}: {} - {}", attempts, status.getCode(), status.getReason());
                if (attempts >= 3) {
                    throw new FroniusCommunicationException(status.getReason());
                }
                Thread.sleep(500 * attempts);
                attempts++;
            }
        } catch (JsonSyntaxException | NumberFormatException e) {
            logger.debug("Received Invalid JSON Data", e);
            throw new FroniusCommunicationException("Invalid JSON data received", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FroniusCommunicationException("Data collection interrupted", e);
        }
    }
}
