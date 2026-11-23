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
package org.openhab.binding.homie.internal.homie300;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homie.internal.HomieBindingConstants;
import org.openhab.binding.homie.internal.handler.HomieThingHandler;
import org.openhab.binding.mqtt.generic.mapping.AbstractMqttAttributeClass;
import org.openhab.binding.mqtt.generic.tools.ChildMap;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.util.UIDUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Homie 3.x Device. This is also the base class to subscribe to and parse a homie MQTT topic tree.
 * First use {@link #subscribe(MqttBrokerConnection, ScheduledExecutorService, int)}
 * to subscribe to the device/nodes/properties tree.
 * If everything has been received and parsed, call
 * {@link #startChannels(MqttBrokerConnection, ScheduledExecutorService, int, HomieThingHandler)}
 * to also subscribe to the property values. Usage:
 *
 * <pre>
 * Device device(thingUID, callback);
 * device.subscribe(topicMapper,timeout).thenRun(()-> {
 *   System.out.println("All attributes received. Device tree ready");
 *   device.startChannels(connection, handler);
 * });
 * </pre>
 *
 * @author David Graeff - Initial contribution
 * @author Cody Cutrer - Restore metadata subscriptions for existing channels
 */
@NonNullByDefault
public class Device implements AbstractMqttAttributeClass.AttributeChanged {
    private final Logger logger = LoggerFactory.getLogger(Device.class);
    // The device attributes, statistics and nodes of this device
    public final DeviceAttributes attributes;
    public volatile ChildMap<Node> nodes;

    // The corresponding ThingUID and callback of this device object
    public final ThingUID thingUID;
    public ThingTypeUID thingTypeUID = HomieBindingConstants.HOMIE_DEVICE_THING;
    private final DeviceCallback callback;

    // Unique identifier and topic
    private String topic = "";
    public String deviceID = "";
    private volatile @Nullable SubscriptionContext initializedSubscription;
    private boolean metadataReady = false;
    private final AtomicReference<DeviceSubscription> subscription;

    /**
     * Creates a Homie Device structure. It consists of device attributes, device statistics and nodes.
     *
     * @param thingUID The thing UID
     * @param callback A callback, used to notify about new/removed nodes/properties and more.
     * @param attributes The device attributes object
     */
    public Device(ThingUID thingUID, DeviceCallback callback, DeviceAttributes attributes) {
        this(thingUID, callback, attributes, new ChildMap<>());
    }

    /**
     * Creates a Homie Device structure. It consists of device attributes, device statistics and nodes.
     *
     * @param thingUID The thing UID
     * @param callback A callback, used to notify about new/removed nodes/properties and more.
     * @param attributes The device attributes object
     * @param nodes The nodes map
     */
    public Device(ThingUID thingUID, DeviceCallback callback, DeviceAttributes attributes, ChildMap<Node> nodes) {
        this.thingUID = thingUID;
        this.callback = callback;
        this.attributes = attributes;
        this.nodes = nodes;
        this.subscription = new AtomicReference<>(new DeviceSubscription(new SubscriptionContext(), nodes));
    }

    /**
     * Subscribe to all device attributes and device statistics. Parse the nodes
     * and subscribe to all node attributes. Parse node properties. This will not subscribe
     * to properties though. If subscribing to all necessary topics worked {@link #isInitialized()} will return true.
     *
     * Call {@link #startChannels(MqttBrokerConnection, ScheduledExecutorService, int, HomieThingHandler)} subsequently.
     *
     * @param connection A broker connection
     * @param scheduler A scheduler to realize the timeout
     * @param timeout A timeout in milliseconds
     * @return A future that is complete as soon as all attributes, nodes and properties have been requested and have
     *         been subscribed to.
     */
    public CompletableFuture<@Nullable Void> subscribe(MqttBrokerConnection connection,
            ScheduledExecutorService scheduler, int timeout) {
        if (topic.isEmpty()) {
            throw new IllegalStateException("You must call initialize()!");
        }

        CompletableFuture<@Nullable Void> predecessor = new CompletableFuture<>();
        SubscriptionContext context = new SubscriptionContext(predecessor);
        DeviceSubscription current = new DeviceSubscription(context, nodes);
        DeviceSubscription previous = subscription.getAndSet(current);
        previous.context().stop(() -> unsubscribe(previous.nodes())).whenComplete((v, error) -> {
            if (error == null) {
                predecessor.complete(null);
            } else {
                predecessor.completeExceptionally(error);
            }
        });
        return context.run(() -> {
            metadataReady = false;
            return attributes.subscribeAndReceive(connection, scheduler, topic,
                    (name, value, broker, executor, complete) -> context.run(() -> {
                        attributeChanged(current, name, value, broker, executor, complete);
                        return CompletableFuture.completedFuture(null);
                    }), timeout);
        }).thenCompose(v -> context.run(() -> attributesReceived(current, connection, scheduler, timeout)))
                .handle((v, error) -> context.run(() -> {
                    metadataReady = true;
                    initializedSubscription = context;
                    return error == null ? CompletableFuture.<@Nullable Void> completedFuture(null)
                            : CompletableFuture.<@Nullable Void> failedFuture(error);
                })).thenCompose(future -> future);
    }

    public CompletableFuture<@Nullable Void> attributesReceived(MqttBrokerConnection connection,
            ScheduledExecutorService scheduler, int timeout) {
        DeviceSubscription current = subscription.get();
        return current.context().run(() -> attributesReceived(current, connection, scheduler, timeout));
    }

    private CompletableFuture<@Nullable Void> attributesReceived(DeviceSubscription current,
            MqttBrokerConnection connection, ScheduledExecutorService scheduler, int timeout) {
        SubscriptionContext context = current.context();
        ChildMap<Node> nodes = current.nodes();
        // Parent metadata is ready before child metadata. Continue processing schema and state updates
        // while the child subscriptions are pending.
        metadataReady = true;
        callback.readyStateChanged(attributes.state);
        // ChildMap.apply only subscribes newly discovered children. Existing children also need
        // metadata subscriptions after restoration or a reconnect.
        List<Node> existing = nodes.stream().toList();
        return applyNodes(nodes, connection, scheduler, timeout).thenCompose(v -> context.run(
                () -> CompletableFuture.allOf(existing.stream().filter(child -> child.equals(nodes.get(child.nodeID)))
                        .map(child -> child.subscribe(connection, scheduler, timeout))
                        .toArray(CompletableFuture[]::new)).exceptionally(error -> {
                            logger.debug("Could not subscribe to restored nodes", error);
                            return null;
                        })));
    }

    /**
     * Subscribe to all property state topics. The handler will receive an update call for each
     * received value. Therefore the thing channels should have been created before.
     *
     * @param connection A broker connection
     * @param scheduler A scheduler to realize the timeout
     * @param timeout A timeout in milliseconds. Can be 0 to disable the timeout and let the future return earlier.
     * @param handler The Homie handler, that receives property (channel) updates.
     * @return A future that is complete as soon as all properties have subscribed to their state topics.
     */
    public CompletableFuture<@Nullable Void> startChannels(MqttBrokerConnection connection,
            ScheduledExecutorService scheduler, int timeout, HomieThingHandler handler) {
        DeviceSubscription current = subscription.get();
        return current.context().run(() -> {
            if (!isInitialized() || deviceID.isEmpty()) {
                CompletableFuture<@Nullable Void> c = new CompletableFuture<>();
                c.completeExceptionally(new Exception("Homie Device Tree not inialized yet."));
                return c;
            }

            return CompletableFuture.allOf(current.nodes().stream().flatMap(node -> node.properties.stream())
                    .map(p -> p.startChannel(connection, scheduler, timeout)).toArray(CompletableFuture[]::new));
        });
    }

    /**
     * Get a homie property (which translates to a channel).
     *
     * @param channelUID The group ID corresponds to the Homie Node, the channel ID (without group ID) corresponds to
     *            the Nodes Property.
     * @return A Homie Property, addressed by the given ChannelUID
     */
    @SuppressWarnings({ "null", "unused" })
    public @Nullable Property getProperty(ChannelUID channelUID) {
        final String groupId = channelUID.getGroupId();
        if (groupId == null) {
            return null;
        }
        Node node = nodes.get(UIDUtils.decode(groupId));
        if (node == null) {
            return null;
        }
        return node.properties.get(UIDUtils.decode(channelUID.getIdWithoutGroup()));
    }

    /**
     * Unsubscribe from everything.
     */
    public CompletableFuture<@Nullable Void> stop() {
        DeviceSubscription current = subscription.get();
        return current.context().stop(() -> unsubscribe(current.nodes()));
    }

    private CompletableFuture<@Nullable Void> unsubscribe(ChildMap<Node> nodes) {
        return CompletableFuture.allOf(
                CompletableFuture.allOf(nodes.stream().map(Node::stop).toArray(CompletableFuture[]::new)),
                attributes.unsubscribe());
    }

    /**
     * Return all homie nodes on this device
     */
    public ChildMap<Node> nodes() {
        return nodes;
    }

    /**
     * @return Return true if this device is initialized
     */
    public boolean isInitialized() {
        SubscriptionContext context = subscription.get().context();
        return context.equals(initializedSubscription) && context.isActive();
    }

    /**
     * Restore Nodes and Properties from Thing channels after handler initalization.
     *
     * @param channels
     */
    @SuppressWarnings({ "null", "unused" })
    public void initialize(String baseTopic, String deviceID, List<Channel> channels) {
        // Deferred cleanup retains the outgoing map through its subscription context.
        if (!this.deviceID.isEmpty()) {
            CompletableFuture<@Nullable Void> predecessor = stop();
            nodes = new ChildMap<>();
            subscription.set(new DeviceSubscription(new SubscriptionContext(predecessor), nodes));
        }
        this.topic = baseTopic + "/" + deviceID;
        this.deviceID = deviceID;
        this.thingTypeUID = new ThingTypeUID(HomieBindingConstants.BINDING_ID,
                HomieBindingConstants.HOMIE_DEVICE_THING.getId() + "_" + UIDUtils.encode(topic));

        nodes.clear();
        for (Channel channel : channels) {
            final String channelGroupId = channel.getUID().getGroupId();
            if (channelGroupId == null) {
                continue;
            }
            final String nodeID = UIDUtils.decode(channelGroupId);
            final String propertyID = UIDUtils.decode(channel.getUID().getIdWithoutGroup());
            Node node = nodes.get(nodeID);
            if (node == null) {
                node = createNode(nodeID);
                node.nodeRestoredFromConfig();
                nodes.put(nodeID, node);
            }
            // Restores the property's attributes object via the channel's config and properties.
            // (config is only for backwards compatibility before properties were used)
            var channelConfig = channel.getConfiguration();
            PropertyAttributes attributes = channelConfig.as(PropertyAttributes.class);

            var channelProperties = channel.getProperties();
            String channelId = channel.getChannelTypeUID().getId();

            String datatype = channelProperties.get(HomieBindingConstants.CHANNEL_PROPERTY_DATATYPE);
            if (datatype != null) {
                attributes.datatype = PropertyAttributes.DataTypeEnum.valueOf(datatype);
            } else if (channel.getChannelTypeUID().getBindingId().equals(HomieBindingConstants.BINDING_ID)) {
                attributes.datatype = PropertyAttributes.DataTypeEnum.valueOf(channelId + "_");
            }
            String label = channel.getLabel();
            if (label != null) {
                attributes.name = label;
            }
            String settable = channelProperties.get(HomieBindingConstants.CHANNEL_PROPERTY_SETTABLE);
            if (settable != null) {
                attributes.settable = Boolean.valueOf(settable);
            }
            String retained = channelProperties.get(HomieBindingConstants.CHANNEL_PROPERTY_RETAINED);
            if (retained != null) {
                attributes.retained = Boolean.valueOf(retained);
            }
            String unit = channelProperties.get(HomieBindingConstants.CHANNEL_PROPERTY_UNIT);
            if (unit != null) {
                attributes.unit = unit;
            }
            String format = channelProperties.get(HomieBindingConstants.CHANNEL_PROPERTY_FORMAT);
            if (format != null) {
                attributes.format = format;
            }

            Property property = node.createProperty(propertyID, attributes);
            property.attributesReceived();

            node.properties.put(propertyID, property);
        }
    }

    /**
     * Creates a new Homie Node, a child of this Homie Device.
     *
     * <p>
     * Implementation detail: Cannot be used for mocking or spying within tests.
     * </p>
     *
     * @param nodeID The node ID
     * @return A child node
     */
    public Node createNode(String nodeID) {
        return new Node(topic, nodeID, thingUID, callback, new NodeAttributes());
    }

    /**
     * Creates a new Homie Node, a child of this Homie Device.
     *
     * @param nodeID The node ID
     * @param attributes The node attributes object
     * @return A child node
     */
    public Node createNode(String nodeID, NodeAttributes attributes) {
        return new Node(topic, nodeID, thingUID, callback, attributes);
    }

    /**
     * <p>
     * The nodes of a device are determined by the device attribute "$nodes". If that attribute changes,
     * {@link #attributeChanged(String, Object, MqttBrokerConnection, ScheduledExecutorService, boolean)} is
     * called. The {@link #nodes} map will be synchronized and this method will be called for every removed node.
     * </p>
     *
     * <p>
     * This method will stop the node and will notify about the removed node all removed properties.
     * </p>
     *
     * @param node The removed node.
     */
    protected void notifyNodeRemoved(Node node) {
        node.stop();
        node.properties.stream().forEach(property -> node.notifyPropertyRemoved(property));
        callback.nodeRemoved(node);
    }

    private CompletableFuture<@Nullable Void> applyNodes(ChildMap<Node> nodes, MqttBrokerConnection connection,
            ScheduledExecutorService scheduler, int timeout) {
        return nodes.apply(Objects.requireNonNull(attributes.nodes),
                node -> node.subscribe(connection, scheduler, timeout), this::createNode, this::notifyNodeRemoved)
                .exceptionally(e -> {
                    logger.warn("Could not subscribe", e);
                    return null;
                });
    }

    @Override
    public void attributeChanged(String name, Object value, MqttBrokerConnection connection,
            ScheduledExecutorService scheduler, boolean allMandatoryFieldsReceived) {
        DeviceSubscription current = subscription.get();
        current.context().run(() -> {
            attributeChanged(current, name, value, connection, scheduler, allMandatoryFieldsReceived);
            return CompletableFuture.completedFuture(null);
        });
    }

    private void attributeChanged(DeviceSubscription current, String name, Object value,
            MqttBrokerConnection connection, ScheduledExecutorService scheduler, boolean allMandatoryFieldsReceived) {
        if (!metadataReady || !allMandatoryFieldsReceived) {
            return;
        }
        // Special case: Not all fields were known before
        if (!attributes.isComplete()) {
            attributesReceived(current, connection, scheduler, 500);
        } else {
            switch (name) {
                case "state": {
                    callback.readyStateChanged(attributes.state);
                    return;
                }
                case "nodes": {
                    applyNodes(current.nodes(), connection, scheduler, 500);
                    return;
                }
            }
        }
    }

    /**
     * Creates a list of retained topics related to the device
     *
     * @return Returns a list of relative topics
     */
    public List<String> getRetainedTopics() {
        List<String> topics = new ArrayList<>(Stream.of(this.attributes.getClass().getDeclaredFields())
                .map(f -> String.format("%s/$%s", this.deviceID, f.getName())).collect(Collectors.toList()));

        this.nodes.stream().map(n -> n.getRetainedTopics().stream().map(a -> String.format("%s/%s", this.deviceID, a))
                .collect(Collectors.toList())).collect(Collectors.toList()).forEach(topics::addAll);

        return topics;
    }

    private record DeviceSubscription(SubscriptionContext context, ChildMap<Node> nodes) {
    }
}
