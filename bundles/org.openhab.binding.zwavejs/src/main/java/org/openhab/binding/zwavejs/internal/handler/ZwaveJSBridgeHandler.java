/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import static org.openhab.binding.zwavejs.internal.BindingConstants.VIRTUAL_COMMAND_CLASS_NOTIFICATION;
import static org.openhab.binding.zwavejs.internal.BindingConstants.VIRTUAL_NOTIFICATION_PROPERTY;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.zwavejs.internal.BindingConstants;
import org.openhab.binding.zwavejs.internal.action.ZwaveJSActions;
import org.openhab.binding.zwavejs.internal.api.ZWaveJSClient;
import org.openhab.binding.zwavejs.internal.api.dto.Args;
import org.openhab.binding.zwavejs.internal.api.dto.Event;
import org.openhab.binding.zwavejs.internal.api.dto.Metadata;
import org.openhab.binding.zwavejs.internal.api.dto.Node;
import org.openhab.binding.zwavejs.internal.api.dto.State;
import org.openhab.binding.zwavejs.internal.api.dto.Status;
import org.openhab.binding.zwavejs.internal.api.dto.Value;
import org.openhab.binding.zwavejs.internal.api.dto.commands.BaseCommand;
import org.openhab.binding.zwavejs.internal.api.dto.commands.ControllerExclusionCommand;
import org.openhab.binding.zwavejs.internal.api.dto.commands.ControllerInclusionCommand;
import org.openhab.binding.zwavejs.internal.api.dto.commands.MulticastSetValueCommand;
import org.openhab.binding.zwavejs.internal.api.dto.commands.ServerListeningCommand;
import org.openhab.binding.zwavejs.internal.api.dto.messages.BaseMessage;
import org.openhab.binding.zwavejs.internal.api.dto.messages.EventMessage;
import org.openhab.binding.zwavejs.internal.api.dto.messages.ResultMessage;
import org.openhab.binding.zwavejs.internal.api.dto.messages.VersionMessage;
import org.openhab.binding.zwavejs.internal.api.exception.CommunicationException;
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

import com.google.gson.Gson;

/**
 * The {@link ZwaveJSBridgeHandler} is responsible for handling communication between the
 * {@link ZwaveJSNodeHandler} 's and the {@link ZWaveJSClient} This handler also manages node discovery
 * and provides controller-level operations like inclusion and exclusion.
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class ZwaveJSBridgeHandler extends BaseBridgeHandler implements ZwaveEventListener {

    private final Logger logger = LoggerFactory.getLogger(ZwaveJSBridgeHandler.class);
    private final Map<Integer, ZwaveNodeListener> nodeListeners = new ConcurrentHashMap<>();
    private final Map<Integer, Node> lastNodeStates = new ConcurrentHashMap<>();
    private final Map<Integer, Map<ValueId, PendingValue>> pendingNodeValues = new ConcurrentHashMap<>();

    protected ScheduledExecutorService executorService = scheduler;
    private @Nullable NodeDiscoveryService discoveryService;
    private @Nullable ScheduledFuture<?> initialConnection;
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

        initialConnection = scheduler.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                startClient(config);
            }
        }, 0, 120, TimeUnit.SECONDS);
    }

    protected void startClient(ZwaveJSBridgeConfiguration config) {
        try {
            client.setBufferSize(config.maxMessageSize);
            client.start("ws://" + config.hostname + ":" + config.port);
            client.addEventListener(this);
            // the thing is set to online when the response/events are received
            stopInitialConnectionJob();
        } catch (CommunicationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void stopInitialConnectionJob() {
        ScheduledFuture<?> initialConnection = this.initialConnection;
        if (initialConnection != null) {
            initialConnection.cancel(false);
            this.initialConnection = null;
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
            return;
        }

        if (message instanceof ResultMessage result) {
            if (result.messageId != null && result.messageId.startsWith("getvalue|")) {
                Event event = createEventFromMessageId(result.messageId,
                        result.result != null ? result.result.value : null);
                if (event == null) {
                    return;
                }

                ZwaveNodeListener nodeListener = nodeListeners.get(event.nodeId);
                if (nodeListener != null) {
                    nodeListener.onNodeStateChanged(event);
                }
                return;
            }
            if (result.result == null || result.result.state == null) {
                logger.debug("ResultMessage missing result or state, ignoring.");
                return;
            }
            procesStateUpdate(result.result.state);
            updateStatus(ThingStatus.ONLINE);
            return;
        }

        ZwaveNodeListener nodeListener;

        if (message instanceof EventMessage eventMsg && eventMsg.event != null) {
            String eventType = eventMsg.event.event;
            nodeListener = nodeListeners.get(eventMsg.event.nodeId);
            switch (eventType) {
                case "notification":
                    if (nodeListener != null) {
                        Event event = normalizeNotificationEvent(eventMsg.event);
                        nodeListener.onNodeStateChanged(event);
                    }
                    break;
                case "value updated":
                    if (nodeListener != null) {
                        nodeListener.onNodeStateChanged(eventMsg.event);
                    }
                    break;
                case "metadata updated":
                case "value added":
                case "value removed":
                    processNodeDefinitionEvent(eventMsg.event);
                    break;
                case "value notification":
                    if (nodeListener != null) {
                        nodeListener.onNodeStateChanged(normalizeValueNotification(eventMsg.event));
                    }
                    break;
                case "alive":
                    if (nodeListener != null) {
                        nodeListener.onNodeAlive(eventMsg.event);
                    }
                    break;
                case "dead":
                    if (nodeListener != null) {
                        nodeListener.onNodeDead(eventMsg.event);
                    }
                    break;
                case "node removed":
                    lastNodeStates.remove(eventMsg.event.nodeId);
                    pendingNodeValues.remove(eventMsg.event.nodeId);
                    if (nodeListener != null) {
                        nodeListener.onNodeRemoved(eventMsg.event);
                    }
                    break;
                case "node added":
                    Node addedNode = eventMsg.event.node;
                    if (addedNode != null) {
                        lastNodeStates.put(addedNode.nodeId, addedNode);
                        final NodeDiscoveryService discovery = discoveryService;
                        if (addedNode.ready && discovery != null) {
                            discovery.addNodeDiscovery(addedNode);
                        } else if (!addedNode.ready) {
                            logger.trace("Node {}. Deferring discovery until the node is ready", addedNode.nodeId);
                        }
                    }
                    break;
                case "ready":
                    Node readyNode = eventMsg.event.nodeState;
                    if (readyNode != null) {
                        pendingNodeValues.remove(readyNode.nodeId);
                        lastNodeStates.put(readyNode.nodeId, readyNode);
                        if (!readyNode.ready) {
                            logger.trace("Node {}. Ignoring ready event with an unready node state", readyNode.nodeId);
                            break;
                        }
                        ZwaveNodeListener readyNodeListener = nodeListeners.get(readyNode.nodeId);
                        if (readyNodeListener != null) {
                            readyNodeListener.onNodeReady(readyNode);
                        } else {
                            final NodeDiscoveryService discovery = discoveryService;
                            if (discovery != null) {
                                discovery.addNodeDiscovery(readyNode);
                            }
                        }
                    }
                    break;
                case "statistics updated":
                    if (nodeListener != null && eventMsg.event.statistics != null) {
                        nodeListener.onStatisticsUpdated(eventMsg.event.statistics);
                    }
                    break;
                default:
                    logger.trace("Unhandled event type: {}", eventType);
            }
        }
    }

    private void processNodeDefinitionEvent(Event event) {
        Args args = event.args;
        Node node = lastNodeStates.get(event.nodeId);
        if (args == null || node == null || !node.ready || node.values == null) {
            logger.trace("Node {}. Ignoring {} without a complete cached node", event.nodeId, event.event);
            return;
        }

        ValueId valueId = ValueId.from(args);
        boolean definitionChanged = false;
        synchronized (node) {
            List<Value> values = new ArrayList<>(node.values);
            int valueIndex = findValue(values, valueId);
            switch (event.event) {
                case "metadata updated":
                    Metadata metadata = args.metadata;
                    if (metadata == null) {
                        logger.trace("Node {}. Ignoring metadata update without metadata", event.nodeId);
                        return;
                    }
                    if (valueIndex >= 0) {
                        Value previous = values.get(valueIndex);
                        Value updated = copyValue(previous);
                        updateValueIdentityFromArgs(updated, args);
                        updated.metadata = metadata;
                        values.set(valueIndex, updated);
                        definitionChanged = !hasSameGeneratedDefinition(previous, updated);
                    } else {
                        PendingValue pending = pendingValue(event.nodeId, valueId, args);
                        pending.value.metadata = metadata;
                        definitionChanged = addPendingValueIfComplete(event.nodeId, valueId, values);
                    }
                    break;
                case "value added":
                    definitionChanged = processValueAdded(event.nodeId, valueId, args, values, valueIndex);
                    break;
                case "value removed":
                    removePendingValue(event.nodeId, valueId);
                    if (valueIndex >= 0) {
                        values.remove(valueIndex);
                        definitionChanged = true;
                    }
                    break;
                default:
                    return;
            }
            node.values = List.copyOf(values);
        }

        if (!definitionChanged) {
            logger.trace("Node {}. {} did not change the generated definition", event.nodeId, event.event);
            return;
        }

        ZwaveNodeListener listener = nodeListeners.get(event.nodeId);
        if (listener != null) {
            listener.onNodeDefinitionChanged(node);
        }
    }

    private boolean processValueAdded(int nodeId, ValueId valueId, Args args, List<Value> values, int valueIndex) {
        if (valueIndex >= 0) {
            Value previous = values.get(valueIndex);
            Value updated = copyValue(previous);
            updateValueFromArgs(updated, args);
            PendingValue pending = removePendingValue(nodeId, valueId);
            if (updated.metadata == null && pending != null) {
                updated.metadata = pending.value.metadata;
            }
            values.set(valueIndex, updated);
            return !hasSameGeneratedDefinition(previous, updated);
        }

        PendingValue pending = pendingValue(nodeId, valueId, args);
        updateValueFromArgs(pending.value, args);
        pending.valueAdded = true;
        return addPendingValueIfComplete(nodeId, valueId, values);
    }

    private PendingValue pendingValue(int nodeId, ValueId valueId, Args args) {
        Map<ValueId, PendingValue> pendingValues = Objects
                .requireNonNull(pendingNodeValues.computeIfAbsent(nodeId, ignored -> new HashMap<>()));
        return Objects.requireNonNull(pendingValues.computeIfAbsent(valueId, ignored -> {
            Value value = new Value();
            updateValueFromArgs(value, args);
            return new PendingValue(value);
        }));
    }

    private boolean addPendingValueIfComplete(int nodeId, ValueId valueId, List<Value> values) {
        PendingValue pending = pendingNodeValues.getOrDefault(nodeId, Map.of()).get(valueId);
        if (pending == null || !pending.valueAdded || pending.value.metadata == null) {
            return false;
        }
        values.add(pending.value);
        removePendingValue(nodeId, valueId);
        return true;
    }

    private @Nullable PendingValue removePendingValue(int nodeId, ValueId valueId) {
        Map<ValueId, PendingValue> pendingValues = pendingNodeValues.get(nodeId);
        if (pendingValues == null) {
            return null;
        }
        PendingValue value = pendingValues.remove(valueId);
        if (pendingValues.isEmpty()) {
            pendingNodeValues.remove(nodeId);
        }
        return value;
    }

    private static int findValue(List<Value> values, ValueId valueId) {
        for (int i = 0; i < values.size(); i++) {
            if (valueId.equals(ValueId.from(values.get(i)))) {
                return i;
            }
        }
        return -1;
    }

    private static boolean hasSameGeneratedDefinition(Value first, Value second) {
        Metadata firstMetadata = first.metadata;
        Metadata secondMetadata = second.metadata;
        return first.endpoint == second.endpoint && first.commandClass == second.commandClass
                && Objects.equals(first.commandClassName, second.commandClassName)
                && Objects.equals(first.property, second.property)
                && Objects.equals(first.propertyName, second.propertyName)
                && Objects.equals(first.propertyKey, second.propertyKey) && firstMetadata != null
                && secondMetadata != null && firstMetadata.hasSameGeneratedDefinition(secondMetadata)
                && Objects.equals(valueShape(first.value), valueShape(second.value));
    }

    private static @Nullable Object valueShape(@Nullable Object value) {
        if (value instanceof Map<?, ?> map) {
            Object nestedValue = map.get("value");
            Object nestedType = nestedValue == null ? null : valueShape(nestedValue);
            return List.of(normalizeIdPart(map.get("unit")), nestedType == null ? "" : nestedType,
                    map.containsKey("red"), map.containsKey("green"), map.containsKey("blue"),
                    map.containsKey("warmWhite"), map.containsKey("coldWhite"));
        }
        if (value instanceof Double || value instanceof Float) {
            return Double.class;
        }
        if (value instanceof Number) {
            return Number.class;
        }
        return value == null ? null : value.getClass();
    }

    private static Value copyValue(Value source) {
        Value copy = new Value();
        copy.endpoint = source.endpoint;
        copy.commandClass = source.commandClass;
        copy.commandClassName = source.commandClassName;
        copy.property = source.property;
        copy.propertyName = source.propertyName;
        copy.ccVersion = source.ccVersion;
        copy.metadata = source.metadata;
        copy.value = source.value;
        copy.propertyKey = source.propertyKey;
        copy.propertyKeyName = source.propertyKeyName;
        return copy;
    }

    private static void updateValueFromArgs(Value value, Args args) {
        updateValueIdentityFromArgs(value, args);
        value.value = args.newValue;
        if (args.metadata != null) {
            value.metadata = args.metadata;
        }
    }

    private static void updateValueIdentityFromArgs(Value value, Args args) {
        value.endpoint = args.endpoint;
        value.commandClass = args.commandClass;
        value.commandClassName = args.commandClassName;
        value.property = args.property;
        value.propertyName = args.propertyName;
        value.propertyKey = args.propertyKey;
        value.propertyKeyName = args.propertyKeyName;
    }

    private record ValueId(int endpoint, int commandClass, String property, String propertyKey) {
        private static ValueId from(Args args) {
            return new ValueId(args.endpoint, args.commandClass, normalizeIdPart(args.property),
                    normalizeIdPart(args.propertyKey));
        }

        private static ValueId from(Value value) {
            return new ValueId(value.endpoint, value.commandClass, normalizeIdPart(value.property),
                    normalizeIdPart(value.propertyKey));
        }
    }

    private static String normalizeIdPart(@Nullable Object value) {
        return value != null ? value.toString() : "";
    }

    private static class PendingValue {
        private final Value value;
        private boolean valueAdded;

        private PendingValue(Value value) {
            this.value = value;
        }
    }

    public static Event normalizeNotificationEvent(Event event) {
        Event normalizedEvent = new Event();
        normalizedEvent.event = event.event;
        normalizedEvent.args = new Args();
        normalizedEvent.args.commandClass = event.ccId;
        normalizedEvent.args.commandClassName = VIRTUAL_COMMAND_CLASS_NOTIFICATION;
        normalizedEvent.args.propertyName = VIRTUAL_NOTIFICATION_PROPERTY;
        normalizedEvent.args.endpoint = event.endpointIndex;
        normalizedEvent.args.newValue = new Gson().toJson(event.args);
        normalizedEvent.nodeId = event.nodeId;
        return normalizedEvent;
    }

    /**
     * "value notification" events report stateless CC values (e.g. Central Scene or Scene Activation).
     * Unlike "value updated" events they carry the datum in {@code args.value}; copy it to
     * {@code args.newValue} so downstream handling is identical for both event types.
     *
     * @param event the incoming value notification event
     * @return the same event with {@code args.newValue} populated
     */
    static Event normalizeValueNotification(Event event) {
        if (event.args != null && event.args.newValue == null) {
            event.args.newValue = event.args.value;
        }
        return event;
    }

    private @Nullable Event createEventFromMessageId(String messageId, @Nullable Object value) {
        // Example messageId: getvalue|0|51|Color Switch|2|currentColor|44|2466
        String[] parts = messageId.split("\\|");
        if (parts.length < 7) {
            logger.warn("Invalid messageId format: {}", messageId);
            return null;
        }
        Event event = new Event();
        event.args = new Args();
        event.args.newValue = value;
        try {
            event.args.endpoint = Integer.parseInt(parts[1]);
            event.args.commandClass = Integer.parseInt(parts[2]);
            event.args.commandClassName = parts[3];
            if (!"null".equals(parts[4]) && !parts[4].isBlank()) {
                event.args.propertyKey = parts[4];
            }
            if (!"null".equals(parts[5]) && !parts[5].isBlank()) {
                event.args.propertyName = parts[5];
            }
            event.nodeId = Integer.parseInt(parts[6]);
        } catch (NumberFormatException e) {
            logger.warn("Error parsing messageId '{}': {}", messageId, e.getMessage());
            return null;
        }
        return event;
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
                if (Status.DEAD == node.status) {
                    logger.warn("Node {}. Ignored due to state: {}", nodeId, node.status);
                    continue;
                }
                if (!node.ready) {
                    logger.trace("Node {}. Deferring discovery until the node is ready", nodeId);
                    lastNodeStates.put(nodeId, node);
                    lastNodeStatesCopy.remove(nodeId);
                    continue;
                }
                logger.trace("Node {}. No listener, pass to discovery", nodeId);

                if (discovery != null) {
                    discovery.addNodeDiscovery(node);
                }
            }
            lastNodeStates.put(nodeId, node);
            pendingNodeValues.remove(nodeId);
            lastNodeStatesCopy.remove(nodeId);
        }

        // Check for removed nodes
        lastNodeStatesCopy.forEach((nodeId, node) -> {
            logger.trace("Node {}. Removed state is missing update", nodeId);
            lastNodeStates.remove(nodeId);
            pendingNodeValues.remove(nodeId);

            final ZwaveNodeListener nodeListener = nodeListeners.get(nodeId);
            if (nodeListener != null) {
                Event event = new Event();
                event.nodeId = nodeId;
                event.event = "node removed";
                nodeListener.onNodeRemoved(event);
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
    public void registerNodeListener(ZwaveNodeListener nodeListener) {
        final Integer id = nodeListener.getId();
        if (nodeListeners.put(id, nodeListener) != null) {
            logger.debug("Node {}. Registering listener", id);
        }
    }

    @Override
    public boolean unregisterNodeListener(ZwaveNodeListener nodeListener) {
        logger.debug("Node {}. Unregistering listener", nodeListener.getId());
        return nodeListeners.remove(nodeListener.getId()) != null;
    }

    @Override
    public boolean registerDiscoveryListener(NodeDiscoveryService listener) {
        logger.debug("Registering Z-Wave discovery listener");
        if (discoveryService == null) {
            discoveryService = listener;
            getFullState();
            return true;
        }

        return false;
    }

    @Override
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
        stopInitialConnectionJob();
        pendingNodeValues.clear();
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

    private int[] parseNodeIDs(String nodeIDs) {
        return Arrays.stream(nodeIDs.split(",")).map(String::trim).filter(s -> !s.isEmpty()).mapToInt(s -> {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                logger.warn("Invalid node ID '{}' - skipping", s);
                return -1; // Use -1 as invalid marker
            }
        }).filter(id -> id > 0) // Filter out invalid IDs (-1 and 0)
                .toArray();
    }

    /**
     * Converts a string value to a Boolean, Double or String, in that order of precedence.
     * Package-private (rather than private) so it can be unit tested directly.
     */
    static Object convertValueType(String value) {
        String trimmed = value.trim();

        if ("true".equalsIgnoreCase(trimmed) || "false".equalsIgnoreCase(trimmed)) {
            return Boolean.parseBoolean(trimmed);
        }

        try {
            return Double.parseDouble(trimmed);
        } catch (NumberFormatException e) {
            return trimmed;
        }
    }
}
