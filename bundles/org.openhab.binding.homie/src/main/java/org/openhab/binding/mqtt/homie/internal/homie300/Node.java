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
import java.util.Collection;
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
import org.openhab.binding.mqtt.generic.MqttChannelTypeProvider;
import org.openhab.binding.mqtt.generic.mapping.AbstractMqttAttributeClass;
import org.openhab.binding.mqtt.generic.tools.ChildMap;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.type.ChannelDefinition;
import org.openhab.core.thing.type.ChannelGroupDefinition;
import org.openhab.core.thing.type.ChannelGroupType;
import org.openhab.core.thing.type.ChannelGroupTypeBuilder;
import org.openhab.core.thing.type.ChannelGroupTypeUID;
import org.openhab.core.util.UIDUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Homie 3.x Node.
 *
 * A Homie Node contains Homie Properties ({@link Property}) but can also have attributes ({@link NodeAttributes}).
 * It corresponds to a ChannelGroup.
 *
 * @author David Graeff - Initial contribution
 * @author Cody Cutrer - Restore metadata subscriptions for existing channels
 */
@NonNullByDefault
public class Node implements AbstractMqttAttributeClass.AttributeChanged {
    private final Logger logger = LoggerFactory.getLogger(Node.class);
    // Homie
    public final String nodeID;
    public final NodeAttributes attributes;
    public ChildMap<Property> properties;
    // Runtime
    public final DeviceCallback callback;
    protected final ChannelGroupUID channelGroupUID;
    private final String topic;
    private boolean metadataReady = false;
    private final AtomicReference<SubscriptionContext> subscription = new AtomicReference<>(new SubscriptionContext());

    /**
     * Creates a Homie Node.
     *
     * @param topic The base topic for this node (e.g. "homie/device")
     * @param nodeID The node ID
     * @param thingUID The Thing UID, used to determine the ChannelGroupUID.
     * @param callback The callback for the handler.
     */
    public Node(String topic, String nodeID, ThingUID thingUID, DeviceCallback callback, NodeAttributes attributes) {
        this.attributes = attributes;
        this.topic = topic + "/" + nodeID;
        this.nodeID = nodeID;
        this.callback = callback;
        channelGroupUID = new ChannelGroupUID(thingUID, UIDUtils.encode(nodeID));
        properties = new ChildMap<>();
    }

    /**
     * Parse node properties. This will not subscribe to properties though. Call
     * {@link Device#startChannels(MqttBrokerConnection, ScheduledExecutorService, int, HomieThingHandler)}
     * as soon as the returned future has completed.
     */
    public CompletableFuture<@Nullable Void> subscribe(MqttBrokerConnection connection,
            ScheduledExecutorService scheduler, int timeout) {
        CompletableFuture<@Nullable Void> predecessor = new CompletableFuture<>();
        SubscriptionContext context = new SubscriptionContext(predecessor);
        SubscriptionContext previous = subscription.getAndSet(context);
        previous.stop(this::unsubscribe).whenComplete((v, error) -> {
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
                        attributeChanged(context, name, value, broker, executor, complete);
                        return CompletableFuture.completedFuture(null);
                    }), timeout);
        }).thenCompose(v -> context.run(() -> attributesReceived(context, connection, scheduler, timeout)))
                .thenCompose(v -> context.run(() -> CompletableFuture.completedFuture(null)));
    }

    public CompletableFuture<@Nullable Void> attributesReceived(MqttBrokerConnection connection,
            ScheduledExecutorService scheduler, int timeout) {
        SubscriptionContext context = subscription.get();
        return context.run(() -> attributesReceived(context, connection, scheduler, timeout));
    }

    private CompletableFuture<@Nullable Void> attributesReceived(SubscriptionContext context,
            MqttBrokerConnection connection, ScheduledExecutorService scheduler, int timeout) {
        // Parent metadata is ready before child metadata. Continue processing schema and state updates
        // while the child subscriptions are pending.
        metadataReady = true;
        callback.nodeAddedOrChanged(this);
        // ChildMap.apply only subscribes newly discovered children. Existing children also need
        // metadata subscriptions after restoration or a reconnect.
        List<Property> existing = properties.stream().toList();
        return applyProperties(connection, scheduler, timeout).thenCompose(v -> context.run(() -> CompletableFuture
                .allOf(existing.stream().filter(child -> child.equals(properties.get(child.propertyID)))
                        .map(child -> child.subscribe(connection, scheduler, timeout))
                        .toArray(CompletableFuture[]::new))));
    }

    public void nodeRestoredFromConfig() {
        attributes.name = nodeID;
    }

    /**
     * Unsubscribe from node attribute and also all property attributes and the property value
     *
     * @return Returns a future that completes as soon as all unsubscriptions have been performed.
     */
    public CompletableFuture<@Nullable Void> stop() {
        return subscription.get().stop(this::unsubscribe);
    }

    private CompletableFuture<@Nullable Void> unsubscribe() {
        return CompletableFuture.allOf(
                CompletableFuture.allOf(properties.stream().map(Property::stop).toArray(CompletableFuture[]::new)),
                attributes.unsubscribe());
    }

    /**
     * Return the channel group type for this Node.
     */
    public ChannelGroupType type(String prefix, MqttChannelTypeProvider channelTypeProvider) {
        final List<ChannelDefinition> channelDefinitions = properties.stream(propertyOrder(prefix, channelTypeProvider))
                .map(p -> Objects.requireNonNull(p.getChannelDefinition())).toList();
        return ChannelGroupTypeBuilder.instance(getChannelGroupTypeUID(prefix), attributes.name)
                .withChannelDefinitions(channelDefinitions).build();
    }

    public ChannelGroupDefinition getChannelGroupDefinition(String prefix) {
        return new ChannelGroupDefinition(channelGroupUID.getId(), getChannelGroupTypeUID(prefix), attributes.name,
                null);
    }

    /**
     * Return the channel group UID.
     */
    public ChannelGroupUID uid() {
        return channelGroupUID;
    }

    /**
     * Create a Homie Property for this Node.
     *
     * @param propertyID The property ID
     * @return A Homie Property
     */
    public Property createProperty(String propertyID) {
        return new Property(topic, this, propertyID, callback, new PropertyAttributes());
    }

    /**
     * Create a Homie Property for this Node.
     *
     * @param propertyID The property ID
     * @param attributes The node attributes object
     * @return A Homie Property
     */
    public Property createProperty(String propertyID, PropertyAttributes attributes) {
        return new Property(topic, this, propertyID, callback, attributes);
    }

    /**
     * <p>
     * The properties of a node are determined by the node attribute "$properties". If that attribute changes,
     * {@link #attributeChanged(String, Object, MqttBrokerConnection, ScheduledExecutorServic, boolean)} is
     * called. The {@link #properties} map will be synchronized and this method will be called for every removed
     * property.
     * </p>
     *
     * <p>
     * This method will stop the property and will notify about the removed property.
     * </p>
     *
     * @param property The removed property.
     */
    protected void notifyPropertyRemoved(Property property) {
        property.stop();
        callback.propertyRemoved(property);
    }

    protected CompletableFuture<@Nullable Void> applyProperties(MqttBrokerConnection connection,
            ScheduledExecutorService scheduler, int timeout) {
        return properties.apply(Objects.requireNonNull(attributes.properties),
                prop -> prop.subscribe(connection, scheduler, timeout), this::createProperty,
                this::notifyPropertyRemoved).exceptionally(e -> {
                    logger.warn("Could not subscribe", e);
                    return null;
                });
    }

    @Override
    public void attributeChanged(String name, Object value, MqttBrokerConnection connection,
            ScheduledExecutorService scheduler, boolean allMandatoryFieldsReceived) {
        SubscriptionContext context = subscription.get();
        context.run(() -> {
            attributeChanged(context, name, value, connection, scheduler, allMandatoryFieldsReceived);
            return CompletableFuture.completedFuture(null);
        });
    }

    private void attributeChanged(SubscriptionContext context, String name, Object value,
            MqttBrokerConnection connection, ScheduledExecutorService scheduler, boolean allMandatoryFieldsReceived) {
        if (!metadataReady || !allMandatoryFieldsReceived) {
            return;
        }
        // Special case: Not all fields were known before
        if (!attributes.isComplete()) {
            attributesReceived(context, connection, scheduler, 500);
        } else if ("properties".equals(name)) {
            applyProperties(connection, scheduler, 500);
        }
        callback.nodeAddedOrChanged(this);
    }

    @Override
    public String toString() {
        return channelGroupUID.toString();
    }

    /**
     * Creates a list of retained topics related to the node
     *
     * @return Returns a list of relative topics
     */
    public List<String> getRetainedTopics() {
        List<String> topics = new ArrayList<>(Stream.of(this.attributes.getClass().getDeclaredFields())
                .map(f -> String.format("%s/$%s", this.nodeID, f.getName())).collect(Collectors.toList()));

        this.properties.stream().map(p -> p.getRetainedTopics().stream()
                .map(a -> String.format("%s/%s", this.nodeID, a)).collect(Collectors.toList()))
                .collect(Collectors.toList()).forEach(topics::addAll);

        return topics;
    }

    public Collection<String> propertyOrder(String prefix, MqttChannelTypeProvider channelTypeProvider) {
        String[] properties = attributes.properties;
        if (properties != null) {
            return Stream.of(properties).toList();
        }
        ChannelGroupType channelGroupType = channelTypeProvider.getChannelGroupType(getChannelGroupTypeUID(prefix),
                null);
        if (channelGroupType != null) {
            return channelGroupType.getChannelDefinitions().stream().map(ChannelDefinition::getId).toList();
        }
        return this.properties.keySet();
    }

    private ChannelGroupTypeUID getChannelGroupTypeUID(String prefix) {
        return new ChannelGroupTypeUID(HomieBindingConstants.BINDING_ID, prefix + "_" + UIDUtils.encode(this.topic));
    }
}
