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
package org.openhab.binding.matter.internal.handler;

import static org.openhab.binding.matter.internal.MatterBindingConstants.*;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.actions.MatterControllerActions;
import org.openhab.binding.matter.internal.client.MatterClientListener;
import org.openhab.binding.matter.internal.client.MatterWebsocketService;
import org.openhab.binding.matter.internal.client.dto.Node;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.client.dto.ws.BridgeEventMessage;
import org.openhab.binding.matter.internal.client.dto.ws.EventTriggeredMessage;
import org.openhab.binding.matter.internal.client.dto.ws.NodeDataMessage;
import org.openhab.binding.matter.internal.client.dto.ws.NodeStateMessage;
import org.openhab.binding.matter.internal.config.ControllerConfiguration;
import org.openhab.binding.matter.internal.controller.MatterControllerClient;
import org.openhab.binding.matter.internal.discovery.MatterDiscoveryHandler;
import org.openhab.binding.matter.internal.discovery.MatterDiscoveryService;
import org.openhab.binding.matter.internal.util.TranslationService;
import org.openhab.core.OpenHAB;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ControllerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 * 
 * A Controller is a Matter client that is used to discover and link devices to
 * the Matter network, as well as to send commands to devices and receive events
 * from them.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class ControllerHandler extends BaseBridgeHandler implements MatterClientListener, MatterDiscoveryHandler {

    private final Logger logger = LoggerFactory.getLogger(ControllerHandler.class);
    private static final int CONNECTION_TIMEOUT_MS = 180000; // 3 minutes
    private final MatterWebsocketService websocketService;
    // Set of nodes we are waiting to connect to
    private Set<BigInteger> outstandingNodeRequests = Collections.synchronizedSet(new HashSet<>());
    // Set of nodes we need to try reconnecting to
    private Set<BigInteger> disconnectedNodes = Collections.synchronizedSet(new HashSet<>());
    // Nodes that we have linked to a handler
    private Map<BigInteger, NodeHandler> linkedNodes = Collections.synchronizedMap(new HashMap<>());
    private @Nullable MatterDiscoveryService discoveryService;
    private @Nullable ScheduledFuture<?> reconnectFuture;
    private MatterControllerClient client;
    private boolean ready = false;
    private final TranslationService translationService;

    public ControllerHandler(Bridge bridge, MatterWebsocketService websocketService,
            TranslationService translationService) {
        super(bridge);
        client = new MatterControllerClient();
        client.addListener(this);
        this.websocketService = websocketService;
        this.translationService = translationService;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(MatterDiscoveryService.class, MatterControllerActions.class);
    }

    @Override
    public void initialize() {
        logger.debug("initialize");
        connect();
    }

    @Override
    public void dispose() {
        logger.debug("dispose");
        ready = false;
        client.removeListener(this);
        cancelReconnect();
        outstandingNodeRequests.clear();
        disconnectedNodes.clear();
        linkedNodes.clear();
        client.disconnect();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        super.childHandlerInitialized(childHandler, childThing);
        logger.debug("childHandlerInitialized ready {} {}", ready, childHandler);
        if (childHandler instanceof NodeHandler handler) {
            BigInteger nodeId = handler.getNodeId();
            linkedNodes.put(nodeId, handler);
            updateNode(nodeId);
        }
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        super.childHandlerDisposed(childHandler, childThing);
        logger.debug("childHandlerDisposed {}", childHandler);
        if (!ready) {
            return;
        }
        if (childHandler instanceof NodeHandler handler) {
            removeNode(handler.getNodeId());
            try {
                client.disconnectNode(handler.getNodeId()).get();
            } catch (InterruptedException | ExecutionException e) {
                logger.debug("Could not disconnect node {}", handler.getNodeId(), e);
            }

        }
    }

    @Override
    public void setDiscoveryService(@Nullable MatterDiscoveryService service) {
        logger.debug("setDiscoveryService");
        this.discoveryService = service;
    }

    public @Nullable MatterDiscoveryService getDiscoveryService() {
        return discoveryService;
    }

    @Override
    public CompletableFuture<Void> startScan(@Nullable String code) {
        if (!client.isConnected()) {
            logger.debug("not connected");
            return CompletableFuture.completedFuture(null);
        }
        if (code != null) {
            return client.pairNode(code).thenCompose(nodeId -> updateNode(nodeId));
        } else {
            // If no code, just sync unknown nodes
            return syncUnknownNodes();
        }
    }

    @Override
    public LocaleProvider getLocaleProvider() {
        return translationService.getLocaleProvider();
    }

    @Override
    public TranslationProvider getTranslationProvider() {
        return translationService.getTranslationProvider();
    }

    @Override
    public void onEvent(NodeStateMessage message) {
        logger.debug("Node onEvent: node {} is {}", message.nodeId, message.state);
        switch (message.state) {
            case CONNECTED:
                updateEndpointStatuses(message.nodeId, ThingStatus.UNKNOWN, ThingStatusDetail.NONE,
                        translationService.getTranslation(THING_STATUS_DETAIL_CONTROLLER_WAITING_FOR_DATA));
                client.requestAllNodeData(message.nodeId);
                break;
            case STRUCTURECHANGED:
                updateNode(message.nodeId);
                break;
            case DECOMMISSIONED:
                updateEndpointStatuses(message.nodeId, ThingStatus.OFFLINE, ThingStatusDetail.GONE,
                        "Node " + message.state);
                removeNode(message.nodeId);
                break;
            case DISCONNECTED:
                if (linkedNodes.containsKey(message.nodeId)) {
                    disconnectedNodes.add(message.nodeId);
                }
                // fall through
            case RECONNECTING:
            case WAITINGFORDEVICEDISCOVERY:
                updateEndpointStatuses(message.nodeId, ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Node " + message.state);
                break;
            default:
        }
    }

    @Override
    public void onEvent(AttributeChangedMessage message) {
        NodeHandler handler = linkedNodes.get(message.path.nodeId);
        if (handler == null) {
            logger.debug("No handler found for node {}", message.path.nodeId);
            return;
        }
        handler.onEvent(message);
    }

    @Override
    public void onEvent(EventTriggeredMessage message) {
        NodeHandler handler = linkedNodes.get(message.path.nodeId);
        if (handler == null) {
            logger.debug("No handler found for node {}", message.path.nodeId);
            return;
        }
        handler.onEvent(message);
    }

    @Override
    public void onEvent(NodeDataMessage message) {
        logger.debug("NodeDataMessage onEvent: node {} is {}", message.node.id, message.node);
        updateNode(message.node);
    }

    @Override
    public void onEvent(BridgeEventMessage message) {
    }

    @Override
    public void onConnect() {
        logger.debug("Websocket connected");
    }

    @Override
    public void onDisconnect(String reason) {
        logger.debug("websocket disconnected");
        setOffline(reason);
    }

    @Override
    public void onReady() {
        logger.debug("websocket ready");
        ready = true;
        updateStatus(ThingStatus.ONLINE);
        cancelReconnect();
        linkedNodes.keySet().forEach(nodeId -> updateNode(nodeId));
    }

    public String getTranslation(String key, Object... args) {
        return translationService.getTranslation(key, args);
    }

    public MatterControllerClient getClient() {
        return client;
    }

    public Map<BigInteger, NodeHandler> getLinkedNodes() {
        return linkedNodes;
    }

    protected void removeNode(BigInteger nodeId) {
        logger.debug("removing node {}", nodeId);
        disconnectedNodes.remove(nodeId);
        outstandingNodeRequests.remove(nodeId);
        linkedNodes.remove(nodeId);
    }

    /**
     * Update the node with the given id
     * If the node is not currently linked, it will be added to the discovery service
     * 
     * @param id
     * @return
     */
    protected CompletableFuture<Void> updateNode(BigInteger id) {
        logger.debug("updateNode BEGIN {}", id);

        // If we are already waiting to get this node, return a completed future
        synchronized (this) {
            // If we are already waiting to get this node, return a completed future
            if (!ready || outstandingNodeRequests.contains(id)) {
                return CompletableFuture.completedFuture(null);
            }
            outstandingNodeRequests.add(id);
        }

        return client.initializeNode(id, CONNECTION_TIMEOUT_MS).thenAccept((Void) -> {
            disconnectedNodes.remove(id);
            client.requestAllNodeData(id);
            logger.debug("updateNode END {}", id);
        }).exceptionally(e -> {
            logger.debug("Could not update node {}", id, e);
            disconnectedNodes.add(id);
            String message = e.getMessage();
            updateEndpointStatuses(id, ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    message != null ? message : "");
            throw new CompletionException(e);
        }).whenComplete((node, e) -> outstandingNodeRequests.remove(id));
    }

    protected CompletableFuture<Void> updateEndpoint(BigInteger nodeId, Integer endpointId) {
        return client.requestEndpointData(nodeId, endpointId);
    }

    /**
     * Update the endpoints (devices) for a node
     * 
     * @param node
     */
    private synchronized void updateNode(Node node) {
        NodeHandler handler = linkedNodes.get(node.id);
        disconnectedNodes.remove(node.id);
        if (handler != null) {
            handler.updateNode(node);
        } else {
            discoverChildNode(node);
        }
    }

    private void connect() {
        logger.debug("connect");
        if (client.isConnected()) {
            logger.debug("Client already connected");
            return;
        }
        String folderName = OpenHAB.getUserDataFolder() + File.separator + "matter";
        File folder = new File(folderName);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String storagePath = folder.getAbsolutePath();
        String controllerName = "controller-" + getThing().getUID().getId();

        logger.debug("matter config: {}", storagePath);
        final ControllerConfiguration config = getConfigAs(ControllerConfiguration.class);
        client.connect(websocketService, new BigInteger(config.nodeId), controllerName, storagePath);
    }

    /**
     * Discover all unknown nodes from the controller
     * 
     * @return
     */
    private CompletableFuture<Void> syncUnknownNodes() {
        logger.debug("syncUnknownNodes starting");
        if (!ready) {
            return CompletableFuture.completedFuture(null);
        }
        return client.getCommissionedNodeIds().thenCompose(nodeIds -> {
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (BigInteger id : nodeIds) {
                // ignore nodes that are already linked and have no bridged endpoints
                NodeHandler handler = linkedNodes.get(id);
                if (handler != null) {
                    if (!handler.hasBridgedEndpoints()) {
                        continue;
                    }
                }
                // updateNode will add the node to the discovery service, if it fails, we will add it to the discovery
                // service manually (orphaned node)
                futures.add(updateNode(id).exceptionally(e -> {
                    logger.debug("Failed to sync node {}: {}", id, e.getMessage());
                    MatterDiscoveryService discoveryService = this.discoveryService;
                    if (discoveryService != null) {
                        ThingUID bridgeUID = getThing().getUID();
                        ThingUID thingUID = new ThingUID(THING_TYPE_NODE, bridgeUID, id.toString());
                        discoveryService.discoverUnknownNodeDevice(thingUID, bridgeUID, id);
                    }
                    return Void.TYPE.cast(null); // ugly but it works
                }));
            }
            // Return a Future that completes when all updateNode futures are complete
            return futures.isEmpty() ? CompletableFuture.completedFuture(null)
                    : CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        }).exceptionally(e -> {
            logger.debug("Error getting commissioned nodes from controller", e);
            // Convert the exception into a failed future instead of swallowing it
            throw new CompletionException(e);
        }).whenComplete((v, e) -> logger.debug("refresh done {}", e != null ? " with error: " + e.getMessage() : ""));
    }

    private synchronized void reconnect() {
        logger.debug("reconnect!");
        cancelReconnect();
        this.reconnectFuture = scheduler.schedule(this::connect, 30, TimeUnit.SECONDS);
    }

    private synchronized void cancelReconnect() {
        ScheduledFuture<?> reconnectFuture = this.reconnectFuture;
        if (reconnectFuture != null) {
            reconnectFuture.cancel(true);
        }
        this.reconnectFuture = null;
    }

    private void updateEndpointStatuses(BigInteger nodeId, ThingStatus status, ThingStatusDetail detail,
            String details) {
        for (Thing thing : getThing().getThings()) {
            ThingHandler handler = thing.getHandler();
            if (handler instanceof NodeHandler endpointHandler) {
                if (nodeId.equals(endpointHandler.getNodeId())) {
                    endpointHandler.setEndpointStatus(status, detail, details);
                }
            }
        }
    }

    private void setOffline(@Nullable String message) {
        logger.debug("setOffline {}", message);
        client.disconnect();
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, message);
        reconnect();
    }

    private void discoverChildNode(Node node) {
        logger.debug("discoverChildNode {}", node.id);

        MatterDiscoveryService discoveryService = this.discoveryService;
        if (discoveryService != null) {
            ThingUID bridgeUID = getThing().getUID();
            ThingUID thingUID = new ThingUID(THING_TYPE_NODE, bridgeUID, node.id.toString());
            discoveryService.discoverNodeDevice(thingUID, bridgeUID, node);
        }
    }
}
