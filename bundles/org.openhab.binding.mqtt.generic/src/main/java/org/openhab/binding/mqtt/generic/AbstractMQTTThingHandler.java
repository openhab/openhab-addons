/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.binding.mqtt.handler.AbstractBrokerHandler;
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
public abstract class AbstractMQTTThingHandler extends BaseThingHandler implements ChannelStateUpdateListener {
    private final Logger logger = LoggerFactory.getLogger(AbstractMQTTThingHandler.class);
    // Timeout for the entire tree parsing and subscription
    private final int subscribeTimeout;

    protected @Nullable MqttBrokerConnection connection;

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
    abstract public @Nullable ChannelState getChannelState(ChannelUID channelUID);

    /**
     * Start the topic discovery and subscribe to all channel state topics on all {@link ChannelState}s.
     * Put the thing ONLINE on success otherwise complete the returned future exceptionally.
     *
     * @param connection A started broker connection
     * @return A future that completes normal on success and exceptionally on any errors.
     */
    abstract protected CompletableFuture<@Nullable Void> start(MqttBrokerConnection connection);

    /**
     * Called when the MQTT connection disappeared.
     * You should clean up all resources that depend on a working connection.
     */
    protected void stop() {
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (connection == null) {
            return;
        }

        final @Nullable ChannelState data = getChannelState(channelUID);

        if (data == null) {
            logger.warn("Channel {} not supported", channelUID.getId());
            if (command instanceof RefreshType) {
                updateState(channelUID.getId(), UnDefType.UNDEF);
            }
            return;
        }

        if (command instanceof RefreshType || data.isReadOnly()) {
            updateState(channelUID.getId(), data.getCache().getChannelState());
            return;
        }

        final CompletableFuture<Boolean> future = data.publishValue(command);
        future.exceptionally(e -> {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
            return false;
        }).thenRun(() -> logger.debug("Successfully published value {} to topic {}", command, data.getCommandTopic()));
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
            logger.warn("Bridge handler not found!");
            return;
        }

        final MqttBrokerConnection connection;
        try {
            connection = h.getConnectionAsync().get(500, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ignored) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                    "Bridge handler has no valid broker connection!");
            return;
        }
        this.connection = connection;

        // Start up (subscribe to MQTT topics). Limit with a timeout and catch exceptions.
        // We do not set the thing to ONLINE here in the AbstractBase, that is the responsibility of a derived
        // class.
        try {
            start(connection).thenApply(e -> true).exceptionally(e -> {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
                return null;
            }).get(subscribeTimeout, TimeUnit.MILLISECONDS);
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
            logger.warn("unsubcription on disposal failed for {}: ", thing.getUID(), e);
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
        super.updateState(channelUID, value);
    }

    @Override
    public void triggerChannel(ChannelUID channelUID, String event) {
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
}
