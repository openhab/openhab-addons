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
package org.openhab.binding.matter.internal.client;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.api.WebSocketPolicy;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.matter.internal.client.dto.Endpoint;
import org.openhab.binding.matter.internal.client.dto.Node;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.BaseCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.BaseCluster.OctetString;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.ClusterRegistry;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.client.dto.ws.BridgeEventAttributeChanged;
import org.openhab.binding.matter.internal.client.dto.ws.BridgeEventMessage;
import org.openhab.binding.matter.internal.client.dto.ws.BridgeEventTriggered;
import org.openhab.binding.matter.internal.client.dto.ws.Event;
import org.openhab.binding.matter.internal.client.dto.ws.EventTriggeredMessage;
import org.openhab.binding.matter.internal.client.dto.ws.Message;
import org.openhab.binding.matter.internal.client.dto.ws.NodeDataMessage;
import org.openhab.binding.matter.internal.client.dto.ws.NodeStateMessage;
import org.openhab.binding.matter.internal.client.dto.ws.Path;
import org.openhab.binding.matter.internal.client.dto.ws.Request;
import org.openhab.binding.matter.internal.client.dto.ws.Response;
import org.openhab.binding.matter.internal.client.dto.ws.ResponseType;
import org.openhab.binding.matter.internal.client.dto.ws.TriggerEvent;
import org.openhab.core.common.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

/**
 * A client for the Matter WebSocket API for communicating with a Matter controller
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class MatterWebsocketClient implements WebSocketListener, MatterWebsocketService.NodeProcessListener {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private static final int BUFFER_SIZE = 1048576 * 2; // 2 Mb
    private static final int REQUEST_TIMEOUT_SECONDS = 60 * 3; // 3 minutes

    private final ScheduledExecutorService scheduler = ThreadPoolManager
            .getScheduledPool("matter.MatterWebsocketClient");

    protected final Gson gson = new GsonBuilder().registerTypeAdapter(Node.class, new NodeDeserializer())
            .registerTypeAdapter(BigInteger.class, new BigIntegerSerializer())
            .registerTypeHierarchyAdapter(BaseCluster.MatterEnum.class, new MatterEnumDeserializer())
            .registerTypeAdapter(AttributeChangedMessage.class, new AttributeChangedMessageDeserializer())
            .registerTypeAdapter(EventTriggeredMessage.class, new EventTriggeredMessageDeserializer())
            .registerTypeAdapter(OctetString.class, new OctetStringDeserializer())
            .registerTypeAdapter(OctetString.class, new OctetStringSerializer()).create();

    protected final WebSocketClient client = new WebSocketClient();
    protected final ConcurrentHashMap<String, CompletableFuture<JsonElement>> pendingRequests = new ConcurrentHashMap<>();
    protected final CopyOnWriteArrayList<MatterClientListener> clientListeners = new CopyOnWriteArrayList<>();

    @Nullable
    private Session session;
    @Nullable
    Map<String, String> connectionParameters;

    @Nullable
    private MatterWebsocketService wss;

    /**
     * Connect to a local Matter controller running on this host in openHAB, primarily use case
     *
     * @param nodeId
     * @param storagePath
     * @param controllerName
     * @throws Exception
     */
    public void connectWhenReady(MatterWebsocketService wss, Map<String, String> connectionParameters) {
        this.connectionParameters = connectionParameters;
        this.wss = wss;
        wss.addProcessListener(this);
    }

    /**
     * Disconnect from the controller
     */
    public void disconnect() {
        Session session = this.session;
        try {
            pendingRequests.forEach((id, future) -> {
                if (!future.isDone()) {
                    future.completeExceptionally(new Exception("Client disconnected"));
                }
            });
            pendingRequests.clear();

            if (session != null && session.isOpen()) {
                session.disconnect();
                session.close();
                session = null;
            }
        } catch (IOException e) {
            logger.debug("Error trying to disconnect", e);
        } finally {
            try {
                client.stop();
            } catch (Exception e) {
                logger.debug("Error closing Web Socket", e);
            }
            MatterWebsocketService wss = this.wss;
            if (wss != null) {
                wss.removeProcessListener(this);
            }
        }
    }

    /**
     * Add a listener to the client
     * 
     * @param listener
     */
    public void addListener(MatterClientListener listener) {
        clientListeners.add(listener);
    }

    /**
     * Remove a listener from the client
     * 
     * @param listener
     */
    public void removeListener(MatterClientListener listener) {
        clientListeners.remove(listener);
    }

    /**
     * Check if the client is connected to the controller
     * 
     * @return
     */
    public boolean isConnected() {
        Session session = this.session;
        return session != null && session.isOpen();
    }

    /**
     * Send a generic command to the controller
     * 
     * @param namespace
     * @param functionName
     * @param objects
     * @return
     */
    public CompletableFuture<String> genericCommand(String namespace, String functionName,
            @Nullable Object... objects) {
        CompletableFuture<JsonElement> future = sendMessage(namespace, functionName, objects);
        return future.thenApply(obj -> obj.toString());
    }

    @Override
    public void onNodeExit(int exitCode) {
    }

    @Override
    public void onNodeReady(int port) {
        logger.debug("onNodeReady port {}", port);
        if (isConnected()) {
            logger.debug("Already connected, aborting!");
            return;
        }
        try {
            connectWebsocket("localhost", port);
        } catch (Exception e) {
            disconnect();
            logger.debug("Could not connect", e);
            for (MatterClientListener listener : clientListeners) {
                String msg = e.getLocalizedMessage();
                listener.onDisconnect(msg != null ? msg : "Exception connecting");
            }
        }
    }

    @Override
    public void onWebSocketConnect(@Nullable Session session) {
        if (session != null) {
            final WebSocketPolicy currentPolicy = session.getPolicy();
            currentPolicy.setInputBufferSize(BUFFER_SIZE);
            currentPolicy.setMaxTextMessageSize(BUFFER_SIZE);
            currentPolicy.setMaxBinaryMessageSize(BUFFER_SIZE);
            this.session = session;
            for (MatterClientListener listener : clientListeners) {
                listener.onConnect();
            }
        }
    }

    @Override
    public void onWebSocketText(@Nullable String msg) {
        logger.debug("onWebSocketText {}", msg);
        scheduler.submit(() -> {
            Message message = gson.fromJson(msg, Message.class);
            if (message == null) {
                logger.debug("invalid Message");
                return;
            }
            if ("response".equals(message.type)) {
                Response response = gson.fromJson(message.message, Response.class);
                if (response == null) {
                    logger.debug("invalid response Message");
                    return;
                }
                CompletableFuture<JsonElement> future = pendingRequests.remove(response.id);
                if (future == null) {
                    logger.debug("no future for response id {}, type {} , did the request timeout?", response.id,
                            response.type);
                    return;
                }
                logger.debug("result type: {} ", response.type);
                if (response.type != ResponseType.RESULT_SUCCESS) {
                    future.completeExceptionally(
                            new MatterRequestException(response.error, MatterErrorCode.fromErrorId(response.errorId)));
                } else {
                    future.complete(response.result);
                }
            } else if ("event".equals(message.type)) {
                Event event = gson.fromJson(message.message, Event.class);
                if (event == null) {
                    logger.debug("invalid Event");
                    return;
                }
                switch (event.type) {
                    case "attributeChanged":
                        logger.debug("attributeChanged message {}", event.data);
                        AttributeChangedMessage changedMessage = gson.fromJson(event.data,
                                AttributeChangedMessage.class);
                        if (changedMessage == null) {
                            logger.debug("invalid AttributeChangedMessage");
                            return;
                        }
                        for (MatterClientListener listener : clientListeners) {
                            try {
                                listener.onEvent(changedMessage);
                            } catch (Exception e) {
                                logger.debug("Error notifying listener", e);
                            }
                        }
                        break;
                    case "eventTriggered":
                        logger.debug("eventTriggered message {}", event.data);
                        EventTriggeredMessage triggeredMessage = gson.fromJson(event.data, EventTriggeredMessage.class);
                        if (triggeredMessage == null) {
                            logger.debug("invalid EventTriggeredMessage");
                            return;
                        }
                        for (MatterClientListener listener : clientListeners) {
                            try {
                                listener.onEvent(triggeredMessage);
                            } catch (Exception e) {
                                logger.debug("Error notifying listener", e);
                            }
                        }
                        break;
                    case "nodeStateInformation":
                        logger.debug("nodeStateInformation message {}", event.data);
                        NodeStateMessage nodeStateMessage = gson.fromJson(event.data, NodeStateMessage.class);
                        if (nodeStateMessage == null) {
                            logger.debug("invalid NodeStateMessage");
                            return;
                        }
                        for (MatterClientListener listener : clientListeners) {
                            try {
                                listener.onEvent(nodeStateMessage);
                            } catch (Exception e) {
                                logger.debug("Error notifying listener", e);
                            }
                        }
                        break;
                    case "nodeData":
                        logger.debug("nodeData message {}", event.data);
                        Node node = gson.fromJson(event.data, Node.class);
                        if (node == null) {
                            logger.debug("invalid nodeData");
                            return;
                        }
                        for (MatterClientListener listener : clientListeners) {
                            try {
                                listener.onEvent(new NodeDataMessage(node));
                            } catch (Exception e) {
                                logger.debug("Error notifying listener", e);
                            }
                        }
                        break;
                    case "bridgeEvent":
                        logger.debug("bridgeEvent message {}", event.data);
                        BridgeEventMessage bridgeEventMessage = gson.fromJson(event.data, BridgeEventMessage.class);

                        if (bridgeEventMessage == null) {
                            logger.debug("invalid bridgeEvent");
                            return;
                        }

                        switch (bridgeEventMessage.type) {
                            case "attributeChanged":
                                bridgeEventMessage = gson.fromJson(event.data, BridgeEventAttributeChanged.class);
                                break;
                            case "eventTriggered":
                                bridgeEventMessage = gson.fromJson(event.data, BridgeEventTriggered.class);
                                break;
                        }

                        if (bridgeEventMessage == null) {
                            logger.debug("invalid bridgeEvent subtype");
                            return;
                        }

                        for (MatterClientListener listener : clientListeners) {
                            try {
                                listener.onEvent(bridgeEventMessage);
                            } catch (Exception e) {
                                logger.debug("Error notifying listener", e);
                            }
                        }
                        break;
                    case "ready":
                        for (MatterClientListener listener : clientListeners) {
                            listener.onReady();
                        }
                        break;
                    default:
                        break;
                }
            }
        });
    }

    @Override
    public void onWebSocketClose(int statusCode, @Nullable String reason) {
        logger.debug("onWebSocketClose {} {}", statusCode, reason);
        for (MatterClientListener listener : clientListeners) {
            listener.onDisconnect(reason != null ? reason : "Code " + statusCode);
        }
    }

    @Override
    public void onWebSocketError(@Nullable Throwable cause) {
        logger.debug("onWebSocketError", cause);
    }

    @Override
    public void onWebSocketBinary(byte @Nullable [] payload, int offset, int len) {
        logger.debug("onWebSocketBinary data, not supported");
    }

    protected CompletableFuture<JsonElement> sendMessage(String namespace, String functionName,
            @Nullable Object args[]) {
        return sendMessage(namespace, functionName, args, REQUEST_TIMEOUT_SECONDS);
    }

    protected CompletableFuture<JsonElement> sendMessage(String namespace, String functionName, @Nullable Object args[],
            int timeoutSeconds) {
        if (timeoutSeconds <= 0) {
            timeoutSeconds = REQUEST_TIMEOUT_SECONDS;
        }
        CompletableFuture<JsonElement> responseFuture = new CompletableFuture<>();

        Session session = this.session;
        if (session == null) {
            logger.debug("Could not send {} {} : no valid session", namespace, functionName);
            return responseFuture;
        }
        String requestId = UUID.randomUUID().toString();
        pendingRequests.put(requestId, responseFuture);
        Request message = new Request(requestId, namespace, functionName, args);
        String jsonMessage = gson.toJson(message);
        logger.debug("sendMessage: {}", jsonMessage);
        session.getRemote().sendStringByFuture(jsonMessage);

        // timeout handling
        scheduler.schedule(() -> {
            CompletableFuture<JsonElement> future = pendingRequests.remove(requestId);
            if (future != null && !future.isDone()) {
                future.completeExceptionally(new TimeoutException(String.format(
                        "Request %s:%s timed out after %d seconds", namespace, functionName, REQUEST_TIMEOUT_SECONDS)));
            }
        }, timeoutSeconds, TimeUnit.SECONDS);

        return responseFuture;
    }

    private void connectWebsocket(String host, int port) throws Exception {
        String dest = "ws://" + host + ":" + port;
        Map<String, String> connectionParameters = this.connectionParameters;
        if (connectionParameters != null) {
            dest += "?" + connectionParameters.entrySet().stream()
                    .map((Map.Entry<String, String> entry) -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8)
                            + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                    .collect(Collectors.joining("&"));
        }

        logger.debug("Connecting {}", dest);
        WebSocketClient client = new WebSocketClient();
        client.setMaxIdleTimeout(Long.MAX_VALUE);
        client.start();
        URI uri = new URI(dest);
        client.connect(this, uri, new ClientUpgradeRequest()).get();
    }

    @NonNullByDefault({})
    class NodeDeserializer implements JsonDeserializer<Node> {
        @Override
        public @Nullable Node deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject jsonObjectNode = json.getAsJsonObject();
            Node node = new Node();
            node.id = jsonObjectNode.get("id").getAsBigInteger();

            // Deserialize root endpoint
            JsonObject rootEndpointJson = jsonObjectNode.getAsJsonObject("rootEndpoint");
            Endpoint rootEndpoint = deserializeEndpoint(rootEndpointJson, context);
            node.rootEndpoint = rootEndpoint;

            return node;
        }

        private Endpoint deserializeEndpoint(JsonObject endpointJson, JsonDeserializationContext context) {
            Endpoint endpoint = new Endpoint();
            endpoint.number = endpointJson.get("number").getAsInt();
            endpoint.clusters = new HashMap<>();
            logger.trace("deserializeEndpoint {}", endpoint.number);

            // Deserialize clusters
            JsonObject clustersJson = endpointJson.getAsJsonObject("clusters");
            Set<Map.Entry<String, JsonElement>> clusterEntries = clustersJson.entrySet();
            for (Map.Entry<String, JsonElement> clusterEntry : clusterEntries) {
                String clusterName = clusterEntry.getKey();
                JsonElement clusterElement = clusterEntry.getValue();
                logger.trace("Cluster {}", clusterEntry);
                try {
                    Class<?> clazz = Class.forName(BaseCluster.class.getPackageName() + "." + clusterName + "Cluster");
                    if (BaseCluster.class.isAssignableFrom(clazz)) {
                        BaseCluster cluster = context.deserialize(clusterElement, clazz);
                        deserializeFields(cluster, clusterElement, clazz, context);
                        endpoint.clusters.put(clusterName, cluster);
                        logger.trace("deserializeEndpoint adding cluster {} to endpoint {}", clusterName,
                                endpoint.number);
                    }
                } catch (ClassNotFoundException e) {
                    logger.debug("Cluster not found: {}", clusterName);
                } catch (JsonSyntaxException | IllegalArgumentException | SecurityException
                        | IllegalAccessException e) {
                    logger.debug("Exception for cluster {}", clusterName, e);
                }
            }

            // Deserialize child endpoints
            endpoint.children = new ArrayList<>();
            JsonArray childrenJson = endpointJson.getAsJsonArray("children");
            if (childrenJson != null) {
                for (JsonElement childElement : childrenJson) {
                    JsonObject childJson = childElement.getAsJsonObject();
                    Endpoint childEndpoint = deserializeEndpoint(childJson, context);
                    endpoint.children.add(childEndpoint);
                }
            }

            return endpoint;
        }

        private void deserializeFields(Object instance, JsonElement jsonElement, Class<?> clazz,
                JsonDeserializationContext context) throws IllegalAccessException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                String fieldName = entry.getKey();
                JsonElement element = entry.getValue();

                try {
                    Field field = getField(clazz, fieldName);
                    field.setAccessible(true);

                    if (List.class.isAssignableFrom(field.getType())) {
                        // Handle lists generically
                        Type fieldType = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                        List<?> list = context.deserialize(element,
                                TypeToken.getParameterized(List.class, fieldType).getType());
                        field.set(instance, list);
                    } else {
                        // Handle normal fields
                        Object fieldValue = context.deserialize(element, field.getType());
                        field.set(instance, fieldValue);
                    }
                } catch (NoSuchFieldException e) {
                    logger.trace("Skipping field {}", fieldName);
                }
            }
        }

        private Field getField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                Class<?> superClass = clazz.getSuperclass();
                if (superClass == null) {
                    throw e;
                } else {
                    return getField(superClass, fieldName);
                }
            }
        }
    }

    @NonNullByDefault({})
    class AttributeChangedMessageDeserializer implements JsonDeserializer<AttributeChangedMessage> {
        @Override
        public @Nullable AttributeChangedMessage deserialize(JsonElement json, Type typeOfT,
                JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();

            Path path = context.deserialize(jsonObject.get("path"), Path.class);
            Long version = jsonObject.get("version").getAsLong();

            JsonElement valueElement = jsonObject.get("value");
            Object value = null;

            // Use ClusterRegistry to find the cluster class
            Class<? extends BaseCluster> clusterClass = ClusterRegistry.CLUSTER_IDS.get(path.clusterId);
            if (clusterClass != null) {
                try {
                    // Use reflection to find the field type
                    Field field = getField(clusterClass, path.attributeName);
                    if (field != null) {
                        value = context.deserialize(valueElement, field.getType());
                    }
                } catch (NoSuchFieldException e) {
                    logger.debug("Field not found for attribute: {}", path.attributeName, e);
                }
            }

            if (value == null) {
                // Fallback to primitive types if no specific class is found
                if (valueElement.isJsonPrimitive()) {
                    JsonPrimitive primitive = valueElement.getAsJsonPrimitive();
                    if (primitive.isNumber()) {
                        value = primitive.getAsNumber();
                    } else if (primitive.isString()) {
                        value = primitive.getAsString();
                    } else if (primitive.isBoolean()) {
                        value = primitive.getAsBoolean();
                    }
                } else if (valueElement.isJsonArray()) {
                    value = context.deserialize(valueElement.getAsJsonArray(), List.class);
                } else {
                    value = valueElement.toString();
                }
            }

            return new AttributeChangedMessage(path, version, value);
        }

        private Field getField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                Class<?> superClass = clazz.getSuperclass();
                if (superClass == null) {
                    throw e;
                } else {
                    return getField(superClass, fieldName);
                }
            }
        }
    }

    /**
     * Biginteger types have to be represented as strings in JSON
     */
    @NonNullByDefault({})
    class BigIntegerSerializer implements JsonSerializer<BigInteger> {
        @Override
        public JsonElement serialize(BigInteger src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }
    }

    @NonNullByDefault({})
    class MatterEnumDeserializer implements JsonDeserializer<BaseCluster.MatterEnum> {
        @Override
        public BaseCluster.MatterEnum deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            int value = json.getAsInt();
            Class<?> rawType = (Class<?>) typeOfT;

            if (BaseCluster.MatterEnum.class.isAssignableFrom(rawType) && rawType.isEnum()) {
                @SuppressWarnings("unchecked")
                Class<? extends BaseCluster.MatterEnum> enumType = (Class<? extends BaseCluster.MatterEnum>) rawType;
                return BaseCluster.MatterEnum.fromValue(enumType, value);
            }

            throw new JsonParseException("Unable to deserialize " + typeOfT);
        }
    }

    @NonNullByDefault({})
    class EventTriggeredMessageDeserializer implements JsonDeserializer<EventTriggeredMessage> {
        @Override
        public EventTriggeredMessage deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            Path path = context.deserialize(jsonObject.get("path"), Path.class);
            JsonArray eventsArray = jsonObject.getAsJsonArray("events");

            TriggerEvent[] events = new TriggerEvent[eventsArray.size()];
            Class<? extends BaseCluster> clusterClass = ClusterRegistry.CLUSTER_IDS.get(path.clusterId);

            String eventName = path.eventName;
            String className = Character.toUpperCase(eventName.charAt(0)) + eventName.substring(1);
            for (int i = 0; i < eventsArray.size(); i++) {
                JsonObject eventObject = eventsArray.get(i).getAsJsonObject();
                TriggerEvent event = context.deserialize(eventObject, TriggerEvent.class);
                if (clusterClass != null) {
                    try {
                        Class<?> eventClass = Class.forName(clusterClass.getName() + "$" + className);
                        event.data = context.deserialize(eventObject.get("data"), eventClass);
                    } catch (ClassNotFoundException e) {
                        logger.debug("Event class not found for event: {}", path.eventName, e);
                    }
                }
                events[i] = event;
            }
            return new EventTriggeredMessage(path, events);
        }
    }

    @NonNullByDefault({})
    class OctetStringDeserializer implements JsonDeserializer<OctetString> {
        @Override
        public OctetString deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return new OctetString(json.getAsString());
        }
    }

    @NonNullByDefault({})
    class OctetStringSerializer implements JsonSerializer<OctetString> {
        @Override
        public JsonElement serialize(OctetString src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }
    }

    /**
     * Get the Gson instance for use in tests
     */
    public Gson getGson() {
        return gson;
    }
}
