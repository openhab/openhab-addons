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
package org.openhab.binding.dmx.internal.handler;

import static org.openhab.binding.dmx.internal.DmxBindingConstants.THING_TYPE_LIB485_BRIDGE;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dmx.internal.DmxBridgeHandler;
import org.openhab.binding.dmx.internal.config.Lib485BridgeHandlerConfiguration;
import org.openhab.binding.dmx.internal.dmxoverethernet.IpNode;
import org.openhab.binding.dmx.internal.multiverse.Universe;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Lib485BridgeHandler} is responsible for communication with
 * an Lib485 instance
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class Lib485BridgeHandler extends DmxBridgeHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_LIB485_BRIDGE);
    public static final int MIN_UNIVERSE_ID = 0;
    public static final int MAX_UNIVERSE_ID = 0;
    public static final int DEFAULT_PORT = 9020;

    private final Logger logger = LoggerFactory.getLogger(Lib485BridgeHandler.class);
    private final Map<IpNode, @Nullable Socket> receiverNodes = new HashMap<>();

    public Lib485BridgeHandler(Bridge lib485Bridge) {
        super(lib485Bridge);
    }

    @Override
    protected void openConnection() {
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            for (IpNode receiverNode : receiverNodes.keySet()) {
                Socket socket = receiverNodes.get(receiverNode);
                if (socket == null) {
                    try {
                        socket = new Socket(receiverNode.getAddressString(), receiverNode.getPort());
                    } catch (IOException e) {
                        logger.debug("Could not connect to {} in {}: {}", receiverNode, this.thing.getUID(),
                                e.getMessage());
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "could not connect to " + receiverNode.toString());
                        return;
                    }
                }

                if (socket.isConnected()) {
                    receiverNodes.put(receiverNode, socket);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                    receiverNodes.put(receiverNode, null);
                    return;
                }
            }
            updateStatus(ThingStatus.ONLINE);
        }
    }

    @Override
    protected void closeConnection() {
        for (IpNode receiverNode : receiverNodes.keySet()) {
            Socket socket = receiverNodes.get(receiverNode);
            if ((socket != null) && (!socket.isClosed())) {
                try {
                    socket.close();
                } catch (IOException e) {
                    logger.warn("Could not close socket {} in {}: {}", receiverNode, this.thing.getUID(),
                            e.getMessage());
                }
            }
            receiverNodes.put(receiverNode, null);
        }
    }

    @Override
    protected void sendDmxData() {
        if (getThing().getStatus() == ThingStatus.ONLINE) {
            long now = System.currentTimeMillis();
            universe.calculateBuffer(now);
            for (IpNode receiverNode : receiverNodes.keySet()) {
                Socket socket = receiverNodes.get(receiverNode);
                if (socket != null && socket.isConnected()) {
                    try {
                        socket.getOutputStream().write(universe.getBuffer());
                    } catch (IOException e) {
                        logger.debug("Could not send to {} in {}: {}", receiverNode, this.thing.getUID(),
                                e.getMessage());
                        closeConnection(ThingStatusDetail.COMMUNICATION_ERROR, "could not send DMX data");
                        return;
                    }
                } else {
                    closeConnection(ThingStatusDetail.NONE, "reconnect");
                    return;
                }
            }
        } else {
            openConnection();
        }
    }

    @Override
    protected void updateConfiguration() {
        Lib485BridgeHandlerConfiguration configuration = getConfig().as(Lib485BridgeHandlerConfiguration.class);

        universe = new Universe(MIN_UNIVERSE_ID);

        receiverNodes.clear();
        if (configuration.address.isEmpty()) {
            receiverNodes.put(new IpNode("localhost:9020"), null);
            logger.debug("sending to {} for {}", receiverNodes, this.thing.getUID());
        } else {
            try {
                for (IpNode receiverNode : IpNode.fromString(configuration.address, DEFAULT_PORT)) {
                    receiverNodes.put(receiverNode, null);
                    logger.debug("sending to {} for {}", receiverNode, this.thing.getUID());
                }
            } catch (IllegalArgumentException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
                return;
            }
        }
        super.updateConfiguration();

        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE);

        logger.debug("updated configuration for Lib485 bridge {}", this.thing.getUID());
    }

    @Override
    public void initialize() {
        logger.debug("initializing Lib485 bridge {}", this.thing.getUID());

        updateConfiguration();
    }
}
