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
package org.openhab.binding.fronius.internal.handler;

import java.io.IOException;
import java.math.BigDecimal;

import org.openhab.binding.fronius.internal.FroniusBridgeConfiguration;
import org.openhab.binding.fronius.internal.api.BaseFroniusResponse;
import org.openhab.binding.fronius.internal.api.ValueUnit;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
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
        if (getFroniusBridgeHandler() == null) {
            logger.debug("Initializing {} Service is only supported within a bridge", serviceDescription);
            updateStatus(ThingStatus.OFFLINE);
            return;
        }
        logger.debug("Initializing {} Service", serviceDescription);
        getFroniusBridgeHandler().registerService(this);
    }

    private synchronized FroniusBridgeHandler getFroniusBridgeHandler() {
        if (this.bridgeHandler == null) {
            Bridge bridge = getBridge();
            if (bridge == null) {
                return null;
            }
            ThingHandler handler = bridge.getHandler();
            if (handler instanceof FroniusBridgeHandler) {
                this.bridgeHandler = (FroniusBridgeHandler) handler;
            } else {
                return null;
            }
        }
        return this.bridgeHandler;
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

        Object value = getValue(channelId);
        if (value == null) {
            logger.debug("Value retrieved for channel '{}' was null. Can't update.", channelId);
            return;
        }

        State state = null;
        if (value instanceof BigDecimal) {
            state = new DecimalType((BigDecimal) value);
        } else if (value instanceof Integer) {
            state = new DecimalType(BigDecimal.valueOf(((Integer) value).longValue()));
        } else if (value instanceof Double) {
            state = new DecimalType((double) value);
        } else if (value instanceof ValueUnit) {
            state = new DecimalType(((ValueUnit) value).getValue());
        } else if (value instanceof String) {
            state = new StringType((String) value);
        } else if (value instanceof QuantityType) {
            state = (QuantityType) value;
        } else {
            logger.warn("Update channel {}: Unsupported value type {}", channelId, value.getClass().getSimpleName());
        }
        logger.debug("Update channel {} with state {} ({})", channelId, (state == null) ? "null" : state.toString(),
                value.getClass().getSimpleName());

        // Update the channel
        if (state != null) {
            updateState(channelId, state);
        }
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
    protected abstract Object getValue(String channelId);

    /**
     * do whatever a thing must do to update the values
     * this function is called from the bridge in a given interval
     *
     * @param bridgeConfiguration the connected bridge configuration
     */
    public abstract void refresh(FroniusBridgeConfiguration bridgeConfiguration);

    /**
     *
     * @param type response class type
     * @param url to request
     * @return the object representation of the json response
     */
    protected <T extends BaseFroniusResponse> T collectDataFormUrl(Class<T> type, String url) {
        T result = null;
        boolean resultOk = false;
        String errorMsg = null;

        try {
            logger.debug("URL = {}", url);
            String response = HttpUtil.executeUrl("GET", url, API_TIMEOUT);

            if (response != null) {
                logger.debug("aqiResponse = {}", response);
                result = gson.fromJson(response, type);
            }

            if (result == null) {
                errorMsg = "no data returned";
            } else {
                if (result.getHead().getStatus().getCode() == 0) {
                    resultOk = true;
                } else {
                    errorMsg = result.getHead().getStatus().getReason();
                }
            }
            if (!resultOk) {
                logger.debug("Error in fronius response: {}", errorMsg);
            }
        } catch (JsonSyntaxException | NumberFormatException e) {
            errorMsg = "Invalid JSON data received";
            logger.debug("Error running fronius request: {}", e.getMessage());
        } catch (IOException | IllegalStateException e) {
            errorMsg = e.getMessage();
            logger.debug("Error running fronius request: {}", errorMsg);
        }

        // Update the thing status
        if (resultOk) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, errorMsg);
        }
        return resultOk ? result : null;
    }
}
