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
package org.openhab.binding.milight.internal.handler;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
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
    protected @NonNullByDefault({}) DatagramSocket socket;
    protected int bridgeOffset;

    public AbstractBridgeHandler(Bridge bridge, int bridgeOffset) {
        super(bridge);
        this.bridgeOffset = bridgeOffset;
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

        if (!config.host.isEmpty()) {
            try {
                address = InetAddress.getByName(config.host);
            } catch (UnknownHostException ignored) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Address set, but is invalid!");
                return;
            }
        }

        startConnectAndKeepAlive();
    }
}
