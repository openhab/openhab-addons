/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import static org.openhab.binding.fronius.internal.FroniusBindingConstants.API_TIMEOUT;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.fronius.internal.FroniusBridgeConfiguration;
import org.openhab.binding.fronius.internal.api.FroniusCommunicationException;
import org.openhab.binding.fronius.internal.api.FroniusHttpUtil;
import org.openhab.binding.fronius.internal.api.dto.BaseFroniusResponse;
import org.openhab.binding.fronius.internal.api.dto.Head;
import org.openhab.binding.fronius.internal.api.dto.HeadStatus;
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
@NonNullByDefault
public abstract class FroniusBaseThingHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(FroniusBaseThingHandler.class);
    private final String serviceDescription;
    private final Gson gson;

    public FroniusBaseThingHandler(Thing thing) {
        super(thing);
        serviceDescription = getDescription();
        gson = new Gson();
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
     * Called by the bridge to handle a bridge configuration update.
     *
     * @param configurationParameters map of changed bridge configuration parameters
     */
    protected void handleBridgeConfigurationUpdate(Map<String, Object> configurationParameters) {
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
    protected abstract @Nullable State getValue(String channelId);

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
    protected <T extends BaseFroniusResponse> T collectDataFromUrl(Class<T> type, String url)
            throws FroniusCommunicationException {
        try {
            int attempts = 1;
            while (true) {
                logger.trace("Fetching URL = {}", url);
                String response = FroniusHttpUtil.executeUrl(HttpMethod.GET, url, API_TIMEOUT);
                logger.trace("aqiResponse = {}", response);

                @Nullable
                T result = gson.fromJson(response, type);
                if (result == null) {
                    throw new FroniusCommunicationException("Empty json result");
                }

                Head head = result.getHead();
                if (head == null) {
                    throw new FroniusCommunicationException("Empty head in json result");
                }
                HeadStatus status = head.getStatus();
                if (status != null && status.getCode() == 0) {
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
                int code = status != null ? status.getCode() : 255;
                String reason = status != null ? status.getReason() : "undefined runtime error";
                logger.debug("Error from Fronius attempt #{}: {} - {}", attempts, code, reason);
                if (attempts >= 3) {
                    throw new FroniusCommunicationException(reason);
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
