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
package org.openhab.binding.zwavejs.internal.handler;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;

import javax.naming.CommunicationException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.zwavejs.internal.BindingConstants;
import org.openhab.binding.zwavejs.internal.action.ZwaveJSActions;
import org.openhab.binding.zwavejs.internal.api.ZWaveJSClient;
import org.openhab.binding.zwavejs.internal.api.dto.Node;
import org.openhab.binding.zwavejs.internal.api.dto.State;
import org.openhab.binding.zwavejs.internal.api.dto.Status;
import org.openhab.binding.zwavejs.internal.api.dto.commands.BaseCommand;
import org.openhab.binding.zwavejs.internal.api.dto.commands.ControllerExclusionCommand;
import org.openhab.binding.zwavejs.internal.api.dto.commands.ControllerInclusionCommand;
import org.openhab.binding.zwavejs.internal.api.dto.commands.MulticastSetValueCommand;
import org.openhab.binding.zwavejs.internal.api.dto.commands.ServerListeningCommand;
import org.openhab.binding.zwavejs.internal.api.dto.messages.BaseMessage;
import org.openhab.binding.zwavejs.internal.api.dto.messages.EventMessage;
import org.openhab.binding.zwavejs.internal.api.dto.messages.ResultMessage;
import org.openhab.binding.zwavejs.internal.api.dto.messages.VersionMessage;
import org.openhab.binding.zwavejs.internal.config.ZwaveJSBridgeConfiguration;
import org.openhab.binding.zwavejs.internal.discovery.NodeDiscoveryService;
import org.openhab.core.io.net.http.WebSocketFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class ZwaveJSBridgeHandler extends BaseBridgeHandler implements ZwaveEventListener {

    private final Logger logger = LoggerFactory.getLogger(ZwaveJSBridgeHandler.class);
    private final Map<Integer, ZwaveNodeListener> nodeListeners = new ConcurrentHashMap<>();
    private final Map<Integer, Node> lastNodeStates = new ConcurrentHashMap<>();

    protected ScheduledExecutorService executorService = scheduler;
    private @Nullable NodeDiscoveryService discoveryService;
    private ZWaveJSClient client;

    public ZwaveJSBridgeHandler(Bridge bridge, WebSocketFactory wsFactory) {
        super(bridge);
        this.client = new ZWaveJSClient(wsFactory.getCommonWebSocketClient());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // The bridge does not support any commands
    }

    @Override
    public void initialize() {
        ZwaveJSBridgeConfiguration config = getConfigAs(ZwaveJSBridgeConfiguration.class);

        if (!config.isValid()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error.hostname-or-port");
            return;
        }

        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(() -> {
            startClient(config);
        });
    }

    protected void startClient(ZwaveJSBridgeConfiguration config) {
        try {
            client.setBufferSize(config.maxMessageSize);
            client.start("ws://" + config.hostname + ":" + config.port);
            client.addEventListener(this);
            // the thing is set to online when the response/events are received
        } catch (CommunicationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (InterruptedException e) {
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    @Override
    public void onEvent(BaseMessage message) {
        if (message instanceof VersionMessage event) {
            Map<String, String> properties = new HashMap<>();
            properties.put(BindingConstants.PROPERTY_DRIVER_VERSION, event.driverVersion);
            properties.put(BindingConstants.PROPERTY_SERVER_VERSION, event.serverVersion);
            properties.put(BindingConstants.PROPERTY_SCHEMA_MIN, String.valueOf(event.minSchemaVersion));
            properties.put(BindingConstants.PROPERTY_SCHEMA_MAX, String.valueOf(event.maxSchemaVersion));
            properties.put(BindingConstants.PROPERTY_HOME_ID, String.valueOf(event.homeId));
            this.getThing().setProperties(properties);
        } else if (message instanceof ResultMessage result) {
            if (result.result == null || result.result.state == null) {
                return;
            }
            procesStateUpdate(result.result.state);
            updateStatus(ThingStatus.ONLINE);
        } else if (message instanceof EventMessage result) {
            if ("value updated".equals(result.event.event)) {
                final ZwaveNodeListener nodeListener = nodeListeners.get(result.event.nodeId);
                if (nodeListener != null) {
                    nodeListener.onNodeStateChanged(result.event);
                }
            } else if ("value notification".equals(result.event.event)) {
                final ZwaveNodeListener nodeListener = nodeListeners.get(result.event.nodeId);
                if (nodeListener != null) {
                    nodeListener.onNodeStateChanged(result.event);
                }
            } else if ("alive".equals(result.event.event)) {
                final ZwaveNodeListener nodeListener = nodeListeners.get(result.event.nodeId);
                if (nodeListener != null) {
                    nodeListener.onNodeAlive(result.event);
                }
            } else if ("dead".equals(result.event.event)) {
                final ZwaveNodeListener nodeListener = nodeListeners.get(result.event.nodeId);
                if (nodeListener != null) {
                    nodeListener.onNodeDead(result.event);
                }
            }
        }
    }

    private void procesStateUpdate(State state) {
        logger.debug("Processing state update with {} nodes", state.nodes.size());

        Map<Integer, Node> lastNodeStatesCopy = new HashMap<>(lastNodeStates);
        final NodeDiscoveryService discovery = discoveryService;
        for (Node node : state.nodes) {
            logger.debug("Node {}. Processing with label: {}", node.nodeId, node.label);

            final int nodeId = node.nodeId;

            final @Nullable ZwaveNodeListener nodeListener = nodeListeners.get(nodeId);
            if (nodeListener == null) {
                if (Status.DEAD.equals(node.status)) {
                    logger.warn("Node {}. Ignored due to state: {}", nodeId, node.status);
                    continue;
                }
                logger.trace("Node {}. No listener, pass to discovery", nodeId);

                if (discovery != null) {
                    discovery.addNodeDiscovery(node);
                }

                lastNodeStates.put(nodeId, node);
            } else {
                lastNodeStates.put(nodeId, node);
            }
            lastNodeStatesCopy.remove(nodeId);
        }

        // Check for removed nodes
        lastNodeStatesCopy.forEach((nodeId, node) -> {
            logger.trace("Node {}. Removed state is missing update", nodeId);
            lastNodeStates.remove(nodeId);

            final ZwaveNodeListener nodeListener = nodeListeners.get(nodeId);
            if (nodeListener != null) {
                nodeListener.onNodeRemoved();
            }

            if (discovery != null) {
                discovery.removeNodeDiscovery(nodeId);
            }
        });
    }

    /*
     * Initiates a full refresh of all data from the remote service.
     *
     */
    public void getFullState() {
        if (getThing().getStatus().equals(ThingStatus.ONLINE)) {
            client.sendCommand(new ServerListeningCommand());
        }
    }

    public void sendCommand(BaseCommand command) {
        if (getThing().getStatus().equals(ThingStatus.ONLINE)) {
            client.sendCommand(command);
        }
    }

    public @Nullable Node requestNodeDetails(int nodeId) {
        Node node = lastNodeStates.get(nodeId);
        logger.debug("Node {}. Details requested, provided: {}", nodeId, node != null);
        return node;
    }

    @Override
    public boolean registerNodeListener(ZwaveNodeListener nodeListener) {
        final Integer id = nodeListener.getId();
        if (!nodeListeners.containsKey(id)) {
            logger.debug("Node {}. Registering listener", id);
            nodeListeners.put(id, nodeListener);
        }
        return true;
    }

    @Override
    public boolean unregisterNodeListener(ZwaveNodeListener nodeListener) {
        logger.debug("Node {}. Unregistering listener", nodeListener.getId());
        return nodeListeners.remove(nodeListener.getId()) != null;
    }

    public boolean registerDiscoveryListener(NodeDiscoveryService listener) {
        logger.debug("Registering Z-Wave discovery listener");
        if (discoveryService == null) {
            discoveryService = listener;
            // getFullState.forEach(listener::adNodeDiscovery);
            getFullState();
            return true;
        }

        return false;
    }

    public boolean unregisterDiscoveryListener() {
        logger.debug("Unregistering Z-Wave discovery listener");
        if (discoveryService != null) {
            discoveryService = null;
            return true;
        }

        return false;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(NodeDiscoveryService.class, ZwaveJSActions.class);
    }

    @Override
    public void dispose() {
        client.stop();
        super.dispose();
    }

    @Override
    public void onConnectionError(String message) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, message);
    }

    public void startInclusion() {
        sendCommand(new ControllerInclusionCommand(false));
    }

    public void stopInclusion() {
        sendCommand(new ControllerInclusionCommand(true));
    }

    public void startExclusion() {
        sendCommand(new ControllerExclusionCommand(false));
    }

    public void stopExclusion() {
        sendCommand(new ControllerExclusionCommand(true));
    }

    public void sendMulticastCommand(String nodeIDs, Integer commandClass, Integer endpoint, String property,
            String value) {
        sendCommand(new MulticastSetValueCommand(parseNodeIDs(nodeIDs), commandClass, endpoint, property,
                convertValueType(value)));
    }

    private static int[] parseNodeIDs(String nodeIDs) {
        return Arrays.stream(nodeIDs.split(",")).map(String::trim).mapToInt(Integer::parseInt).toArray();
    }

    private static Object convertValueType(String value) {
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return value;
        }
    }
}
