/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.milight.internal.handler;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AbstractBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractBridgeHandler extends BaseBridgeHandler {
    protected final Logger logger = LoggerFactory.getLogger(AbstractBridgeHandler.class);
    protected volatile boolean preventReinit = false;
    protected BridgeHandlerConfig config = new BridgeHandlerConfig();
    protected @Nullable InetAddress address;
    @NonNullByDefault({})
    protected DatagramSocket socket;
    protected int port;

    public AbstractBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // There is nothing to handle in the bridge handler
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        if (preventReinit) {
            return;
        }
        super.handleConfigurationUpdate(configurationParameters);
    }

    /**
     * Creates a connection and other supportive objects.
     *
     * @param addr
     */
    protected abstract void startConnectAndKeepAlive();

    /**
     * You need a CONFIG_HOST_NAME and CONFIG_ID for a milight bridge handler to initialize correctly.
     * The ID is a unique 12 character long ASCII based on the bridge MAC address (for example ACCF23A20164)
     * and is send as response for a discovery message.
     */
    @Override
    public void initialize() {
        config = getConfigAs(BridgeHandlerConfig.class);

        try {
            address = InetAddress.getByName(config.host);
        } catch (UnknownHostException ignored) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No address known!");
            return;
        }

        port = config.port;

        // Version 1/2 do not support response messages / detection. We therefore directly call bridgeDetected(addr).
        if (config.bridgeid.length() != 12) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No bridgeID of length 12!");
            return;
        }

        startConnectAndKeepAlive();
    }
}
