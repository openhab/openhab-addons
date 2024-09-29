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
package org.openhab.binding.mqtt.generic;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.utils.FutureCollector;
import org.openhab.binding.mqtt.generic.values.OnOffValue;
import org.openhab.binding.mqtt.generic.values.Value;
import org.openhab.binding.mqtt.handler.AbstractBrokerHandler;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.generic.ChannelTransformation;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.openhab.core.util.UIDUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for MQTT thing handlers. If you are going to implement an MQTT convention, you probably
 * want to inherit from here.
 *
 * <p>
 * This base class will make sure you get a working {@link MqttBrokerConnection}, you will be informed
 * when to start your subscriptions ({@link #start(MqttBrokerConnection)}) and when to free your resources
 * because of a lost connection ({@link AbstractMQTTThingHandler#stop()}).
 *
 * <p>
 * If you inherit from this base class, you must use {@link ChannelState} to (a) keep a cached channel value,
 * (b) to link a MQTT topic value to a channel value ("MQTT state topic") and (c) to have a secondary MQTT topic
 * where any changes to the {@link ChannelState} are send to ("MQTT command topic").
 *
 * <p>
 * You are expected to keep your channel data structure organized in a way, to resolve a {@link ChannelUID} to
 * the corresponding {@link ChannelState} in {@link #getChannelState(ChannelUID)}.
 *
 * <p>
 * To inform the framework of changed values, received via MQTT, a {@link ChannelState} calls a listener callback.
 * While setting up your {@link ChannelState} you would set the callback to your thing handler,
 * because this base class implements {@link ChannelStateUpdateListener}.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractMQTTThingHandler extends BaseThingHandler
        implements ChannelStateUpdateListener, AvailabilityTracker {
    private final Logger logger = LoggerFactory.getLogger(AbstractMQTTThingHandler.class);
    // Timeout for the entire tree parsing and subscription
    private final int subscribeTimeout;

    protected @Nullable MqttBrokerConnection connection;

    private AtomicBoolean messageReceived = new AtomicBoolean(false);
    private Map<String, @Nullable ChannelState> availabilityStates = new ConcurrentHashMap<>();
    private AvailabilityMode availabilityMode = AvailabilityMode.ALL;

    public AbstractMQTTThingHandler(Thing thing, int subscribeTimeout) {
        super(thing);
        this.subscribeTimeout = subscribeTimeout;
    }

    /**
     * Return the channel state for the given channelUID.
     *
     * @param channelUID The channelUID
     * @return A channel state. May be null.
     */
    public abstract @Nullable ChannelState getChannelState(ChannelUID channelUID);

    /**
     * Start the topic discovery and subscribe to all channel state topics on all {@link ChannelState}s.
     * Put the thing ONLINE on success otherwise complete the returned future exceptionally.
     *
     * @param connection A started broker connection
     * @return A future that completes normal on success and exceptionally on any errors.
     */
    protected CompletableFuture<@Nullable Void> start(MqttBrokerConnection connection) {
        return availabilityStates.values().stream().map(cChannel -> {
            final CompletableFuture<@Nullable Void> fut = cChannel == null ? CompletableFuture.completedFuture(null)
                    : cChannel.start(connection, scheduler, 0);
            return fut;
        }).collect(FutureCollector.allOf());
    }

    /**
     * Called when the MQTT connection disappeared.
     * You should clean up all resources that depend on a working connection.
     */
    protected void stop() {
        clearAllAvailabilityTopics();
        resetMessageReceived();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (connection == null) {
            return;
        }

        final @Nullable ChannelState data = getChannelState(channelUID);

        if (data == null) {
            logger.warn("Channel {} not supported!", channelUID);
            return;
        }

        if (command instanceof RefreshType) {
            State state = data.getCache().getChannelState();
            if (state instanceof UnDefType) {
                logger.debug("Channel {} received REFRESH but no value cached, ignoring", channelUID);
            } else {
                updateState(channelUID, state);
            }
            return;
        }

        if (data.isReadOnly()) {
            logger.trace("Channel {} is a read-only channel, ignoring command {}", channelUID, command);
            return;
        }

        final CompletableFuture<Boolean> future = data.publishValue(command);
        future.handle((v, ex) -> {
            if (ex != null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ex.getLocalizedMessage());
                logger.debug("Failed publishing value {} to topic {}: {}", command, data.getCommandTopic(),
                        ex.getMessage());
            } else {
                logger.debug("Successfully published value {} to topic {}", command, data.getCommandTopic());
            }
            return null;
        });
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            stop();
            connection = null;
            return;
        }
        if (bridgeStatusInfo.getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            stop();
            return;
        }

        AbstractBrokerHandler h = getBridgeHandler();
        if (h == null) {
            resetMessageReceived();
            logger.warn("Bridge handler not found!");
            return;
        }

        final MqttBrokerConnection connection;
        try {
            connection = h.getConnectionAsync().get(500, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ignored) {
            resetMessageReceived();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                    "Bridge handler has no valid broker connection!");
            return;
        }
        this.connection = connection;

        // Start up (subscribe to MQTT topics). Limit with a timeout and catch exceptions.
        // We do not set the thing to ONLINE here in the AbstractBase, that is the responsibility of a derived
        // class.
        try {
            start(connection).get(subscribeTimeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ignored) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Did not receive all required topics");
        }
    }

    /**
     * Return the bride handler. The bridge is from the "MQTT" bundle.
     */
    public @Nullable AbstractBrokerHandler getBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            return null;
        }
        return (AbstractBrokerHandler) bridge.getHandler();
    }

    /**
     * Return the bridge status.
     */
    public ThingStatusInfo getBridgeStatus() {
        Bridge b = getBridge();
        if (b != null) {
            return b.getStatusInfo();
        } else {
            return new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, null);
        }
    }

    @Override
    public void initialize() {
        bridgeStatusChanged(getBridgeStatus());
    }

    @Override
    public void handleRemoval() {
        stop();
        super.handleRemoval();
    }

    @Override
    public void dispose() {
        stop();
        try {
            unsubscribeAll().get(500, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.warn("unsubscription on disposal failed for {}: ", thing.getUID(), e);
        }
        connection = null;
        super.dispose();
    }

    /**
     * this method must unsubscribe all topics used by this thing handler
     *
     * @return
     */
    public abstract CompletableFuture<Void> unsubscribeAll();

    @Override
    public void updateChannelState(ChannelUID channelUID, State value) {
        if (messageReceived.compareAndSet(false, true)) {
            calculateAndUpdateThingStatus(true);
        }
        super.updateState(channelUID, value);
    }

    @Override
    public void triggerChannel(ChannelUID channelUID, String event) {
        if (messageReceived.compareAndSet(false, true)) {
            calculateAndUpdateThingStatus(true);
        }
        super.triggerChannel(channelUID, event);
    }

    @Override
    public void postChannelCommand(ChannelUID channelUID, Command command) {
        postCommand(channelUID, command);
    }

    public @Nullable MqttBrokerConnection getConnection() {
        return connection;
    }

    /**
     * This is for tests only to inject a broker connection.
     *
     * @param connection MQTT Broker connection
     */
    public void setConnection(MqttBrokerConnection connection) {
        this.connection = connection;
    }

    @Override
    public void setAvailabilityMode(AvailabilityMode mode) {
        this.availabilityMode = mode;
    }

    @Override
    public void addAvailabilityTopic(String availability_topic, String payload_available,
            String payload_not_available) {
        addAvailabilityTopic(availability_topic, payload_available, payload_not_available, null);
    }

    @Override
    public void addAvailabilityTopic(String availability_topic, String payload_available, String payload_not_available,
            @Nullable ChannelTransformation transformation) {
        availabilityStates.computeIfAbsent(availability_topic, topic -> {
            Value value = new OnOffValue(payload_available, payload_not_available);
            ChannelGroupUID groupUID = new ChannelGroupUID(getThing().getUID(), "availability");
            ChannelUID channelUID = new ChannelUID(groupUID, UIDUtils.encode(topic));
            ChannelState state = new ChannelState(ChannelConfigBuilder.create().withStateTopic(topic).build(),
                    channelUID, value, new ChannelStateUpdateListener() {
                        @Override
                        public void updateChannelState(ChannelUID channelUID, State value) {
                            boolean online = value.equals(OnOffType.ON);
                            calculateAndUpdateThingStatus(online);
                        }

                        @Override
                        public void triggerChannel(ChannelUID channelUID, String eventPayload) {
                        }

                        @Override
                        public void postChannelCommand(ChannelUID channelUID, Command value) {
                        }
                    }, transformation, null);
            MqttBrokerConnection connection = getConnection();
            if (connection != null) {
                state.start(connection, scheduler, 0);
            }

            return state;
        });
    }

    @Override
    public void removeAvailabilityTopic(String availabilityTopic) {
        availabilityStates.computeIfPresent(availabilityTopic, (topic, state) -> {
            if (connection != null && state != null) {
                state.stop();
            }
            return null;
        });
    }

    @Override
    public void clearAllAvailabilityTopics() {
        Set<String> topics = new HashSet<>(availabilityStates.keySet());
        topics.forEach(this::removeAvailabilityTopic);
    }

    @Override
    public void resetMessageReceived() {
        if (messageReceived.compareAndSet(true, false)) {
            calculateAndUpdateThingStatus(false);
        }
    }

    protected void calculateAndUpdateThingStatus(boolean lastValue) {
        final Optional<Boolean> availabilityTopicsSeen;

        if (availabilityStates.isEmpty()) {
            availabilityTopicsSeen = Optional.empty();
        } else {
            availabilityTopicsSeen = switch (availabilityMode) {
                case ALL -> Optional.of(availabilityStates.values().stream().allMatch(
                        c -> c != null && OnOffType.ON.equals(c.getCache().getChannelState().as(OnOffType.class))));
                case ANY -> Optional.of(availabilityStates.values().stream().anyMatch(
                        c -> c != null && OnOffType.ON.equals(c.getCache().getChannelState().as(OnOffType.class))));
                case LATEST -> Optional.of(lastValue);
            };
        }
        updateThingStatus(messageReceived.get(), availabilityTopicsSeen);
    }

    protected abstract void updateThingStatus(boolean messageReceived, Optional<Boolean> availabilityTopicsSeen);
}
