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
package org.openhab.binding.mqtt.homeassistant.internal;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.AvailabilityTracker;
import org.openhab.binding.mqtt.generic.ChannelStateUpdateListener;
import org.openhab.binding.mqtt.generic.TransformationServiceProvider;
import org.openhab.binding.mqtt.generic.utils.FutureCollector;
import org.openhab.binding.mqtt.homeassistant.internal.component.AbstractComponent;
import org.openhab.binding.mqtt.homeassistant.internal.component.ComponentFactory;
import org.openhab.binding.mqtt.homeassistant.internal.exception.ConfigurationException;
import org.openhab.binding.mqtt.homeassistant.internal.exception.UnsupportedComponentException;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.io.transport.mqtt.MqttMessageSubscriber;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * Responsible for subscribing to the HomeAssistant MQTT components wildcard topic, either
 * in a time limited discovery mode or as a background discovery.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class DiscoverComponents implements MqttMessageSubscriber {
    private final Logger logger = LoggerFactory.getLogger(DiscoverComponents.class);
    private final ThingUID thingUID;
    private final ScheduledExecutorService scheduler;
    private final ChannelStateUpdateListener updateListener;
    private final AvailabilityTracker tracker;
    private final TransformationServiceProvider transformationServiceProvider;

    protected final CompletableFuture<@Nullable Void> discoverFinishedFuture = new CompletableFuture<>();
    private final Gson gson;

    private @Nullable ScheduledFuture<?> stopDiscoveryFuture;
    private WeakReference<@Nullable MqttBrokerConnection> connectionRef = new WeakReference<>(null);
    protected @Nullable ComponentDiscovered discoveredListener;
    private int discoverTime;
    private Set<String> topics = new HashSet<>();

    /**
     * Implement this to get notified of new components
     */
    public static interface ComponentDiscovered {
        void componentDiscovered(HaID homeAssistantTopicID, AbstractComponent<?> component);
    }

    /**
     * Create a new discovery object.
     *
     * @param thingUID The Thing UID to perform the discovery for.
     * @param scheduler A scheduler for timeouts
     * @param channelStateUpdateListener Channel update listener. Usually the handler.
     */
    public DiscoverComponents(ThingUID thingUID, ScheduledExecutorService scheduler,
            ChannelStateUpdateListener channelStateUpdateListener, AvailabilityTracker tracker, Gson gson,
            TransformationServiceProvider transformationServiceProvider) {
        this.thingUID = thingUID;
        this.scheduler = scheduler;
        this.updateListener = channelStateUpdateListener;
        this.gson = gson;
        this.tracker = tracker;
        this.transformationServiceProvider = transformationServiceProvider;
    }

    @Override
    public void processMessage(String topic, byte[] payload) {
        if (!topic.endsWith("/config")) {
            return;
        }

        HaID haID = new HaID(topic);
        String config = new String(payload);
        AbstractComponent<?> component = null;

        if (config.length() > 0) {
            try {
                component = ComponentFactory.createComponent(thingUID, haID, config, updateListener, tracker, scheduler,
                        gson, transformationServiceProvider);
                component.setConfigSeen();

                logger.trace("Found HomeAssistant component {}", haID);

                if (discoveredListener != null) {
                    discoveredListener.componentDiscovered(haID, component);
                }
            } catch (UnsupportedComponentException e) {
                logger.warn("HomeAssistant discover error: thing {} component type is unsupported: {}", haID.objectID,
                        haID.component);
            } catch (ConfigurationException e) {
                logger.warn("HomeAssistant discover error: invalid configuration of thing {} component {}: {}",
                        haID.objectID, haID.component, e.getMessage());
            } catch (Exception e) {
                logger.warn("HomeAssistant discover error: {}", e.getMessage());
            }
        } else {
            logger.warn("Configuration of HomeAssistant thing {} is empty", haID.objectID);
        }
    }

    /**
     * Start a components discovery.
     *
     * <p>
     * We need to consider the case that the remote client is using node IDs
     * and also the case that no node IDs are used.
     * </p>
     *
     * @param connection A MQTT broker connection
     * @param discoverTime The time in milliseconds for the discovery to run. Can be 0 to disable the
     *            timeout.
     *            You need to call {@link #stopDiscovery()} at some
     *            point in that case.
     * @param topicDescriptions Contains the object-id (=device id) and potentially a node-id as well.
     * @param componentsDiscoveredListener Listener for results
     * @return A future that completes normally after the given time in milliseconds or exceptionally on any error.
     *         Completes immediately if the timeout is disabled.
     */
    public CompletableFuture<@Nullable Void> startDiscovery(MqttBrokerConnection connection, int discoverTime,
            Set<HaID> topicDescriptions, ComponentDiscovered componentsDiscoveredListener) {
        this.topics = topicDescriptions.stream().map(id -> id.getTopic("config")).collect(Collectors.toSet());
        this.discoverTime = discoverTime;
        this.discoveredListener = componentsDiscoveredListener;
        this.connectionRef = new WeakReference<>(connection);

        // Subscribe to the wildcard topic and start receive MQTT retained topics
        this.topics.stream().map(t -> connection.subscribe(t, this)).collect(FutureCollector.allOf())
                .thenRun(this::subscribeSuccess).exceptionally(this::subscribeFail);

        return discoverFinishedFuture;
    }

    private void subscribeSuccess() {
        final MqttBrokerConnection connection = connectionRef.get();
        // Set up a scheduled future that will stop the discovery after the given time
        if (connection != null && discoverTime > 0) {
            this.stopDiscoveryFuture = scheduler.schedule(() -> {
                this.stopDiscoveryFuture = null;
                this.topics.stream().forEach(t -> connection.unsubscribe(t, this));
                this.discoveredListener = null;
                discoverFinishedFuture.complete(null);
            }, discoverTime, TimeUnit.MILLISECONDS);
        } else {
            // No timeout -> complete immediately
            discoverFinishedFuture.complete(null);
        }
    }

    private @Nullable Void subscribeFail(Throwable e) {
        final ScheduledFuture<?> scheduledFuture = this.stopDiscoveryFuture;
        if (scheduledFuture != null) { // Cancel timeout
            scheduledFuture.cancel(false);
            this.stopDiscoveryFuture = null;
        }
        this.discoveredListener = null;
        final MqttBrokerConnection connection = connectionRef.get();
        if (connection != null) {
            this.topics.stream().forEach(t -> connection.unsubscribe(t, this));
            connectionRef.clear();
        }
        discoverFinishedFuture.completeExceptionally(e);
        return null;
    }

    /**
     * Stops an ongoing discovery or do nothing if no discovery is running.
     */
    public void stopDiscovery() {
        subscribeFail(new Throwable("Stopped"));
    }
}
