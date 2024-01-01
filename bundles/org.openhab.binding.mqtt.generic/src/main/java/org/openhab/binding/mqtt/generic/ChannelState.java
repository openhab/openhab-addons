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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.binding.mqtt.generic.values.Value;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.io.transport.mqtt.MqttMessageSubscriber;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.Type;
import org.openhab.core.types.TypeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This object consists of a {@link Value}, which is updated on the respective MQTT topic change.
 * Updates to the value are propagated via the {@link ChannelStateUpdateListener}.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class ChannelState implements MqttMessageSubscriber {
    private final Logger logger = LoggerFactory.getLogger(ChannelState.class);

    // Immutable channel configuration
    protected final boolean readOnly;
    protected final ChannelUID channelUID;
    protected final ChannelConfig config;

    /** Channel value **/
    protected final Value cachedValue;

    // Runtime variables
    private @Nullable MqttBrokerConnection connection;
    protected final List<ChannelStateTransformation> transformationsIn = new ArrayList<>();
    protected final List<ChannelStateTransformation> transformationsOut = new ArrayList<>();
    private @Nullable ChannelStateUpdateListener channelStateUpdateListener;
    protected boolean hasSubscribed = false;
    private @Nullable ScheduledFuture<?> scheduledFuture;
    private CompletableFuture<@Nullable Void> future = CompletableFuture.completedFuture(null);
    private final Object futureLock = new Object();

    /**
     * Creates a new channel state.
     *
     * @param config The channel configuration
     * @param channelUID The channelUID is used for the {@link ChannelStateUpdateListener} to notify about value changes
     * @param cachedValue MQTT only notifies us once about a value, during the subscribe. The channel state therefore
     *            needs a cache for the current value.
     * @param channelStateUpdateListener A channel state update listener
     */
    public ChannelState(ChannelConfig config, ChannelUID channelUID, Value cachedValue,
            @Nullable ChannelStateUpdateListener channelStateUpdateListener) {
        this.config = config;
        this.channelStateUpdateListener = channelStateUpdateListener;
        this.channelUID = channelUID;
        this.cachedValue = cachedValue;
        this.readOnly = config.commandTopic.isBlank();
    }

    public boolean isReadOnly() {
        return this.readOnly;
    }

    /**
     * Add a transformation that is applied for each received MQTT topic value.
     * The transformations are executed in order.
     *
     * @param transformation A transformation
     */
    public void addTransformation(ChannelStateTransformation transformation) {
        transformationsIn.add(transformation);
    }

    public void addTransformation(String transformation, TransformationServiceProvider transformationServiceProvider) {
        parseTransformation(transformation, transformationServiceProvider).forEach(t -> addTransformation(t));
    }

    /**
     * Add a transformation that is applied for each value to be published.
     * The transformations are executed in order.
     *
     * @param transformation A transformation
     */
    public void addTransformationOut(ChannelStateTransformation transformation) {
        transformationsOut.add(transformation);
    }

    public void addTransformationOut(String transformation,
            TransformationServiceProvider transformationServiceProvider) {
        parseTransformation(transformation, transformationServiceProvider).forEach(t -> addTransformationOut(t));
    }

    public static Stream<ChannelStateTransformation> parseTransformation(String transformation,
            TransformationServiceProvider transformationServiceProvider) {
        String[] transformations = transformation.split("∩");
        return Stream.of(transformations).filter(t -> !t.isBlank())
                .map(t -> new ChannelStateTransformation(t, transformationServiceProvider));
    }

    /**
     * Clear transformations
     */
    public void clearTransformations() {
        transformationsIn.clear();
        transformationsOut.clear();
    }

    /**
     * Returns the cached value state object of this message subscriber.
     * <p>
     * MQTT only notifies us once about a value, during the subscribe.
     * The channel state therefore needs a cache for the current value.
     * If MQTT has not yet published a value, the cache might still be in UNDEF state.
     * </p>
     */
    public Value getCache() {
        return cachedValue;
    }

    /**
     * Return the channelUID
     */
    public ChannelUID channelUID() {
        return channelUID;
    }

    /**
     * Incoming message from the MqttBrokerConnection
     *
     * @param topic The topic. Is the same as the field stateTopic.
     * @param payload The byte payload. Must be UTF8 encoded text or binary data.
     */
    @Override
    public void processMessage(String topic, byte[] payload) {
        final ChannelStateUpdateListener channelStateUpdateListener = this.channelStateUpdateListener;
        if (channelStateUpdateListener == null) {
            logger.warn("MQTT message received for topic {}, but MessageSubscriber object hasn't been started!", topic);
            return;
        }

        if (cachedValue.isBinary()) {
            cachedValue.update(payload);
            channelStateUpdateListener.updateChannelState(channelUID, cachedValue.getChannelState());
            receivedOrTimeout();
            return;
        }

        // String value: Apply transformations
        String strValue = new String(payload, StandardCharsets.UTF_8);
        for (ChannelStateTransformation t : transformationsIn) {
            String transformedValue = t.processValue(strValue);
            if (transformedValue != null) {
                strValue = transformedValue;
            } else {
                logger.debug("Transformation '{}' returned null on '{}', discarding message", strValue, t.serviceName);
                receivedOrTimeout();
                return;
            }
        }

        // Is trigger?: Special handling
        if (config.trigger) {
            channelStateUpdateListener.triggerChannel(channelUID, strValue);
            receivedOrTimeout();
            return;
        }

        Command command = TypeParser.parseCommand(cachedValue.getSupportedCommandTypes(), strValue);
        if (command == null) {
            logger.warn("Incoming payload '{}' on '{}' not supported by type '{}'", strValue, topic,
                    cachedValue.getClass().getSimpleName());
            receivedOrTimeout();
            return;
        }

        Type parsedType;
        // Map the string to a command, update the cached value and post the command to the framework
        try {
            parsedType = cachedValue.parseMessage(command);
        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.warn("Command '{}' from channel '{}' not supported by type '{}': {}", strValue, channelUID,
                    cachedValue.getClass().getSimpleName(), e.getMessage());
            receivedOrTimeout();
            return;
        }

        if (parsedType instanceof State parsedState) {
            cachedValue.update(parsedState);
        } else {
            // things that are only Commands _must_ be posted as a command (like STOP)
            channelStateUpdateListener.postChannelCommand(channelUID, (Command) parsedType);
            receivedOrTimeout();
            return;
        }

        State newState = cachedValue.getChannelState();
        // If the user explicitly wants a command sent, not an update, do that. But
        // we have to check that the state is even possible to send as a command
        // (i.e. not UNDEF)
        if (config.postCommand && newState instanceof Command newCommand) {
            channelStateUpdateListener.postChannelCommand(channelUID, newCommand);
        } else {
            channelStateUpdateListener.updateChannelState(channelUID, newState);
        }
        receivedOrTimeout();
    }

    /**
     * Returns the state topic. Might be an empty string if this is a stateless channel (TRIGGER kind channel).
     */
    public String getStateTopic() {
        return config.stateTopic;
    }

    /**
     * Return the command topic. Might be an empty string, if this is a read-only channel.
     */
    public String getCommandTopic() {
        return config.commandTopic;
    }

    /**
     * Returns the channelType ID which also happens to be an item-type
     */
    public String getItemType() {
        return cachedValue.getItemType();
    }

    /**
     * Returns true if this is a stateful channel.
     */
    public boolean isStateful() {
        return config.retained;
    }

    /**
     * Removes the subscription to the state topic and resets the channelStateUpdateListener.
     *
     * @return A future that completes with true if unsubscribing from the state topic succeeded.
     *         It completes with false if no connection is established and completes exceptionally otherwise.
     */
    public CompletableFuture<@Nullable Void> stop() {
        final MqttBrokerConnection connection = this.connection;
        if (connection != null && !config.stateTopic.isBlank()) {
            return connection.unsubscribe(config.stateTopic, this).thenRun(this::internalStop);
        } else {
            internalStop();
            return CompletableFuture.completedFuture(null);
        }
    }

    private void internalStop() {
        logger.debug("Unsubscribed channel {} from topic: {}", this.channelUID, config.stateTopic);
        this.connection = null;
        this.channelStateUpdateListener = null;
        hasSubscribed = false;
        cachedValue.resetState();
    }

    private void receivedOrTimeout() {
        final ScheduledFuture<?> scheduledFuture = this.scheduledFuture;
        if (scheduledFuture != null) { // Cancel timeout
            scheduledFuture.cancel(false);
            this.scheduledFuture = null;
        }
        future.complete(null);
    }

    private @Nullable Void subscribeFail(Throwable e) {
        final ScheduledFuture<?> scheduledFuture = this.scheduledFuture;
        if (scheduledFuture != null) { // Cancel timeout
            scheduledFuture.cancel(false);
            this.scheduledFuture = null;
        }
        future.completeExceptionally(e);
        return null;
    }

    /**
     * Subscribes to the state topic on the given connection and informs about updates on the given listener.
     *
     * @param connection A broker connection
     * @param scheduler A scheduler to realize the timeout
     * @param timeout A timeout in milliseconds. Can be 0 to disable the timeout and let the future return earlier.
     * @return A future that completes with true if the subscribing worked, with false if the stateTopic is not set
     *         and exceptionally otherwise.
     */
    public CompletableFuture<@Nullable Void> start(MqttBrokerConnection connection, ScheduledExecutorService scheduler,
            int timeout) {
        synchronized (futureLock) {
            // if the connection is still the same, the subscription is still present, otherwise we need to renew
            if ((hasSubscribed || !future.isDone()) && connection.equals(this.connection)) {
                return future;
            }
            hasSubscribed = false;

            this.connection = connection;

            if (config.stateTopic.isBlank()) {
                return CompletableFuture.completedFuture(null);
            }

            this.future = new CompletableFuture<>();
        }
        connection.subscribe(config.stateTopic, this).thenRun(() -> {
            hasSubscribed = true;
            logger.debug("Subscribed channel {} to topic: {}", this.channelUID, config.stateTopic);
            if (timeout > 0 && !future.isDone()) {
                this.scheduledFuture = scheduler.schedule(this::receivedOrTimeout, timeout, TimeUnit.MILLISECONDS);
            } else {
                receivedOrTimeout();
            }
        }).exceptionally(this::subscribeFail);
        return future;
    }

    /**
     * Return true if this channel has subscribed to its MQTT topics.
     * You need to call {@link #start(MqttBrokerConnection, ScheduledExecutorService, int)} and
     * have a stateTopic set, to subscribe this channel.
     */
    public boolean hasSubscribed() {
        return this.hasSubscribed;
    }

    /**
     * Publishes a value on MQTT. A command topic needs to be set in the configuration.
     *
     * @param command The command to send
     * @return A future that completes with true if the publishing worked and false if it is a readonly topic
     *         and exceptionally otherwise.
     */
    public CompletableFuture<Boolean> publishValue(Command command) {
        final MqttBrokerConnection connection = this.connection;

        if (connection == null) {
            CompletableFuture<Boolean> f = new CompletableFuture<>();
            f.completeExceptionally(new IllegalStateException(
                    "The connection object has not been set. start() should have been called!"));
            return f;
        }

        Command mqttCommandValue = cachedValue.parseCommand(command);
        Value mqttFormatter = cachedValue;

        if (readOnly) {
            logger.debug(
                    "You have tried to publish {} to the mqtt topic '{}' that was marked read-only. You can't 'set' anything on a sensor state topic for example.",
                    mqttCommandValue, config.commandTopic);
            return CompletableFuture.completedFuture(false);
        }

        // Outgoing transformations
        for (ChannelStateTransformation t : transformationsOut) {
            String commandString = mqttFormatter.getMQTTpublishValue(mqttCommandValue, null);
            String transformedValue = t.processValue(commandString);
            if (transformedValue != null) {
                mqttFormatter = new TextValue();
                mqttCommandValue = new StringType(transformedValue);
            } else {
                logger.debug("Transformation '{}' returned null on '{}', discarding message", mqttCommandValue,
                        t.serviceName);
                return CompletableFuture.completedFuture(false);
            }
        }

        String commandString;

        // Formatter: Applied before the channel state value is published to the MQTT broker.
        if (config.formatBeforePublish.length() > 0) {
            try {
                commandString = mqttFormatter.getMQTTpublishValue(mqttCommandValue, config.formatBeforePublish);
            } catch (IllegalFormatException e) {
                logger.debug("Format pattern incorrect for {}", channelUID, e);
                commandString = mqttFormatter.getMQTTpublishValue(mqttCommandValue, null);
            }
        } else {
            commandString = mqttFormatter.getMQTTpublishValue(mqttCommandValue, null);
        }

        int qos = (config.qos != null) ? config.qos : connection.getQos();

        return connection.publish(config.commandTopic, commandString.getBytes(), qos, config.retained);
    }

    /**
     * @return The channelStateUpdateListener
     */
    public @Nullable ChannelStateUpdateListener getChannelStateUpdateListener() {
        return channelStateUpdateListener;
    }

    /**
     * @param channelStateUpdateListener The channelStateUpdateListener to set
     */
    public void setChannelStateUpdateListener(ChannelStateUpdateListener channelStateUpdateListener) {
        this.channelStateUpdateListener = channelStateUpdateListener;
    }

    public @Nullable MqttBrokerConnection getConnection() {
        return connection;
    }

    /**
     * This is for tests only to inject a broker connection. Use
     * {@link #start(MqttBrokerConnection, ScheduledExecutorService, int)} instead.
     *
     * @param connection MQTT Broker connection
     */
    public void setConnection(MqttBrokerConnection connection) {
        this.connection = connection;
    }
}
