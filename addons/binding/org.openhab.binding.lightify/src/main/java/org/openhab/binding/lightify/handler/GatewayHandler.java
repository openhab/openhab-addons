/*
 * Copyright (c) 2014-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lightify.handler;

import com.noctarius.lightify.LightifyLink;
import com.noctarius.lightify.StatusListener;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Set;

import static org.openhab.binding.lightify.LightifyConstants.PROPERTY_ADDRESS;
import static org.openhab.binding.lightify.LightifyConstants.THING_TYPE_LIGHTIFY_GATEWAY;

/**
 * The {@link org.eclipse.smarthome.core.thing.binding.BridgeHandler} implementation to handle commands
 * and status of the OSRAM Lightify gateway device. This handler uses a native implementation of the
 * Lightify proprietary protocol to communicate with the Lightify gateway, therefore no internet connection
 * is necessary to utilize the OSRAM public API.
 *
 * @author Christoph Engelbert (@noctarius2k) - Initial contribution
 */
public class GatewayHandler
        extends BaseBridgeHandler
        implements StatusListener {

    /**
     * Supported {@link ThingTypeUID}s for this handler
     */
    public static final Set<ThingTypeUID> SUPPORTED_TYPES = Collections.singleton(THING_TYPE_LIGHTIFY_GATEWAY);

    private final Logger logger = LoggerFactory.getLogger(LightifyLink.class);

    private String address;
    private LightifyLink lightifyLink;

    public GatewayHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.OFFLINE);
        address = getConfig().get(PROPERTY_ADDRESS).toString();
        lightifyLink = new LightifyLink(address, this);

        // Build internal device / zone lookup
        lightifyLink.performSearch(null);
    }

    @Override
    public void handleUpdate(ChannelUID channelUID, State newState) {
    }

    @Override
    public void dispose() {
        if (lightifyLink != null) {
            lightifyLink.disconnect();
        }
    }

    /**
     * Provides access to the {@link LightifyLink} used to communicate with the Lightify gateway
     *
     * @return the current communication link
     */
    public LightifyLink getLightifyLink() {
        return lightifyLink;
    }

    @Override
    public void onConnect() {
        logger.info("Connecting to lightify gateway: {}:4000", address);
    }

    @Override
    public void onConnectionFailed() {
        logger.info("Reconnection failed, retrying...");
    }

    @Override
    public void onConnectionEstablished() {
        logger.info("Connection established...");
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void onConnectionLost() {
        if (isInitialized()) {
            logger.info("Connection lost...");
            updateStatus(ThingStatus.OFFLINE);
        }
    }
}
