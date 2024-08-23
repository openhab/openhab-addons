/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.homie.internal.homie300;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.MqttChannelTypeProvider;
import org.openhab.binding.mqtt.generic.mapping.AbstractMqttAttributeClass;
import org.openhab.binding.mqtt.generic.tools.ChildMap;
import org.openhab.binding.mqtt.homie.generic.internal.MqttBindingConstants;
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
    private boolean initialized = false;

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
        return attributes.subscribeAndReceive(connection, scheduler, topic, this, timeout)
                // On success, create all properties and tell the handler about this node
                .thenCompose(b -> attributesReceived(connection, scheduler, timeout))
                // No matter if values have been received or not -> the subscriptions have been performed
                .whenComplete((r, e) -> {
                    initialized = true;
                });
    }

    public CompletableFuture<@Nullable Void> attributesReceived(MqttBrokerConnection connection,
            ScheduledExecutorService scheduler, int timeout) {
        callback.nodeAddedOrChanged(this);
        return applyProperties(connection, scheduler, timeout);
    }

    public void nodeRestoredFromConfig() {
        initialized = true;
        attributes.name = nodeID;
    }

    /**
     * Unsubscribe from node attribute and also all property attributes and the property value
     *
     * @return Returns a future that completes as soon as all unsubscriptions have been performed.
     */
    public CompletableFuture<@Nullable Void> stop() {
        return attributes.unsubscribe().thenCompose(b -> CompletableFuture
                .allOf(properties.stream().map(Property::stop).toArray(CompletableFuture[]::new)));
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
        if (!initialized || !allMandatoryFieldsReceived) {
            return;
        }
        // Special case: Not all fields were known before
        if (!attributes.isComplete()) {
            attributesReceived(connection, scheduler, 500);
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
        return new ChannelGroupTypeUID(MqttBindingConstants.BINDING_ID, prefix + "_" + UIDUtils.encode(this.topic));
    }
}
