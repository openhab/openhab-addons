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
package org.openhab.binding.mqtt.homie.internal.handler;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.AbstractMQTTThingHandler;
import org.openhab.binding.mqtt.generic.ChannelState;
import org.openhab.binding.mqtt.generic.MqttChannelTypeProvider;
import org.openhab.binding.mqtt.generic.tools.DelayedBatchProcessing;
import org.openhab.binding.mqtt.homie.generic.internal.MqttBindingConstants;
import org.openhab.binding.mqtt.homie.internal.homie300.Device;
import org.openhab.binding.mqtt.homie.internal.homie300.DeviceAttributes;
import org.openhab.binding.mqtt.homie.internal.homie300.DeviceAttributes.ReadyState;
import org.openhab.binding.mqtt.homie.internal.homie300.DeviceCallback;
import org.openhab.binding.mqtt.homie.internal.homie300.HandlerConfiguration;
import org.openhab.binding.mqtt.homie.internal.homie300.Node;
import org.openhab.binding.mqtt.homie.internal.homie300.Property;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles MQTT topics that follow the Homie MQTT convention. The convention specifies a MQTT topic layout
 * and defines Devices, Nodes and Properties, corresponding to Things, Channel Groups and Channels respectively.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class HomieThingHandler extends AbstractMQTTThingHandler implements DeviceCallback, Consumer<List<Object>> {
    private final Logger logger = LoggerFactory.getLogger(HomieThingHandler.class);
    protected Device device;
    protected final MqttChannelTypeProvider channelTypeProvider;
    /** The timeout per attribute field subscription */
    protected final int attributeReceiveTimeout;
    protected final int subscribeTimeout;
    protected final int deviceTimeout;
    protected HandlerConfiguration config = new HandlerConfiguration();
    protected DelayedBatchProcessing<Object> delayedProcessing;
    private @Nullable ScheduledFuture<?> heartBeatTimer;

    /**
     * Create a new thing handler for homie discovered things. A channel type provider and a topic value receive timeout
     * must be provided.
     *
     * @param thing The thing of this handler
     * @param channelTypeProvider A channel type provider
     * @param deviceTimeout Timeout for the entire device subscription. In milliseconds.
     * @param subscribeTimeout Timeout for an entire attribute class subscription and receive. In milliseconds.
     *            Even a slow remote device will publish a full node or property within 100ms.
     * @param attributeReceiveTimeout The timeout per attribute field subscription. In milliseconds.
     *            One attribute subscription and receiving should not take longer than 50ms.
     */
    public HomieThingHandler(Thing thing, MqttChannelTypeProvider channelTypeProvider, int deviceTimeout,
            int subscribeTimeout, int attributeReceiveTimeout) {
        super(thing, deviceTimeout);
        this.channelTypeProvider = channelTypeProvider;
        this.deviceTimeout = deviceTimeout;
        this.subscribeTimeout = subscribeTimeout;
        this.attributeReceiveTimeout = attributeReceiveTimeout;
        this.delayedProcessing = new DelayedBatchProcessing<>(subscribeTimeout, this, scheduler);
        this.device = new Device(this.thing.getUID(), this, new DeviceAttributes());
    }

    /**
     * Overwrite the {@link Device} and {@link DelayedBatchProcessing} object.
     * Those are set in the constructor already, but require to be replaced for tests.
     *
     * @param device The device object
     * @param delayedProcessing The delayed processing object
     */
    protected void setInternalObjects(Device device, DelayedBatchProcessing<Object> delayedProcessing) {
        this.device = device;
        this.delayedProcessing = delayedProcessing;
    }

    @Override
    public void initialize() {
        config = getConfigAs(HandlerConfiguration.class);
        logger.debug("About to initialize Homie device {}", config.deviceid);
        if (config.deviceid.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Object ID unknown");
            return;
        }
        device.initialize(config.basetopic, config.deviceid, thing.getChannels());
        super.initialize();
    }

    @Override
    public void handleRemoval() {
        this.stop();
        if (config.removetopics) {
            this.removeRetainedTopics();
        }
        super.handleRemoval();
    }

    @Override
    protected CompletableFuture<@Nullable Void> start(MqttBrokerConnection connection) {
        logger.debug("About to start Homie device {}", config.deviceid);
        if (connection.getQos() != 1) {
            // QoS 1 is required.
            logger.warn(
                    "Homie devices require QoS 1 but Qos 0/2 is configured. Using override. Please check the configuration");
            connection.setQos(1);
        }
        return device.subscribe(connection, scheduler, attributeReceiveTimeout)
                .thenCompose((Void v) -> device.startChannels(connection, scheduler, attributeReceiveTimeout, this))
                .thenRun(() -> {
                    logger.debug("Homie device {} fully attached (start)", config.deviceid);
                });
    }

    @Override
    protected void stop() {
        logger.debug("About to stop Homie device {}", config.deviceid);
        final ScheduledFuture<?> heartBeatTimer = this.heartBeatTimer;
        if (heartBeatTimer != null) {
            heartBeatTimer.cancel(false);
            this.heartBeatTimer = null;
        }
        delayedProcessing.join();
        device.stop();
        super.stop();
    }

    @Override
    public CompletableFuture<Void> unsubscribeAll() {
        // already unsubscribed everything by calling stop()
        return CompletableFuture.allOf();
    }

    @Override
    public @Nullable ChannelState getChannelState(ChannelUID channelUID) {
        Property property = device.getProperty(channelUID);
        return property != null ? property.getChannelState() : null;
    }

    @Override
    public void readyStateChanged(ReadyState state) {
        switch (state) {
            case alert:
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_ERROR);
                break;
            case disconnected:
                updateStatus(ThingStatus.OFFLINE);
                break;
            case init:
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_PENDING);
                break;
            case lost:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.GONE, "Device did not send heartbeat in time");
                break;
            case ready:
                updateStatus(ThingStatus.ONLINE);
                break;
            case sleeping:
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.DUTY_CYCLE);
                break;
            case unknown:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.GONE, "Device did not publish a ready state");
                break;
            default:
                break;
        }
    }

    @Override
    public void nodeRemoved(Node node) {
        channelTypeProvider.removeChannelGroupType(node.channelGroupTypeUID);
        delayedProcessing.accept(node);
    }

    @Override
    public void propertyRemoved(Property property) {
        channelTypeProvider.removeChannelType(property.channelTypeUID);
        delayedProcessing.accept(property);
    }

    @Override
    public void nodeAddedOrChanged(Node node) {
        channelTypeProvider.setChannelGroupType(node.channelGroupTypeUID, node.type());
        delayedProcessing.accept(node);
    }

    @Override
    public void propertyAddedOrChanged(Property property) {
        channelTypeProvider.setChannelType(property.channelTypeUID, property.getType());
        delayedProcessing.accept(property);
    }

    /**
     * Callback of {@link DelayedBatchProcessing}.
     * Add all newly discovered nodes and properties to the Thing and start subscribe to each channel state topic.
     */
    @Override
    public void accept(@Nullable List<Object> t) {
        if (!device.isInitialized()) {
            return;
        }
        List<Channel> channels = device.nodes().stream().flatMap(n -> n.properties.stream()).map(Property::getChannel)
                .collect(Collectors.toList());
        updateThing(editThing().withChannels(channels).build());
        updateProperty(MqttBindingConstants.HOMIE_PROPERTY_VERSION, device.attributes.homie);
        final MqttBrokerConnection connection = this.connection;
        if (connection != null) {
            device.startChannels(connection, scheduler, attributeReceiveTimeout, this).thenRun(() -> {
                logger.debug("Homie device {} fully attached (accept)", config.deviceid);
            });
        }
    }

    /**
     * Removes all retained topics related to the device
     */
    private void removeRetainedTopics() {
        MqttBrokerConnection connection = this.connection;
        if (connection == null) {
            logger.warn("couldn't remove retained topics for {} because connection is null", thing.getUID());
            return;
        }
        device.getRetainedTopics().stream().map(d -> String.format("%s/%s", config.basetopic, d))
                .collect(Collectors.toList()).forEach(t -> connection.publish(t, new byte[0], 1, true));
    }

    @Override
    protected void updateThingStatus(boolean messageReceived, Optional<Boolean> availabilityTopicsSeen) {
        // not used here
    }
}
