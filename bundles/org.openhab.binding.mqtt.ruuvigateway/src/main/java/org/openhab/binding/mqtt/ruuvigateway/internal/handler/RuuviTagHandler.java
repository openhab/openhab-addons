/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.ruuvigateway.internal.handler;

import static org.openhab.binding.mqtt.ruuvigateway.internal.RuuviGatewayBindingConstants.*;

import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.AbstractMQTTThingHandler;
import org.openhab.binding.mqtt.generic.ChannelState;
import org.openhab.binding.mqtt.ruuvigateway.internal.RuuviCachedDateTimeState;
import org.openhab.binding.mqtt.ruuvigateway.internal.RuuviCachedNumberState;
import org.openhab.binding.mqtt.ruuvigateway.internal.RuuviCachedStringState;
import org.openhab.binding.mqtt.ruuvigateway.internal.RuuviGatewayBindingConstants;
import org.openhab.binding.mqtt.ruuvigateway.internal.parser.GatewayPayloadParser;
import org.openhab.binding.mqtt.ruuvigateway.internal.parser.GatewayPayloadParser.GatewayPayload;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.io.transport.mqtt.MqttMessageSubscriber;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.UnDefType;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

/**
 * The {@link RuuviTagHandler} is responsible updating RuuviTag Sensor data received from
 * Ruuvi Gateway via MQTT.
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class RuuviTagHandler extends AbstractMQTTThingHandler implements MqttMessageSubscriber {

    // Ruuvitag sends an update every 10 seconds. So we keep a heartbeat to give it some slack
    private int heartbeatTimeoutMillisecs = 60_000;
    // This map is used to initialize channel caches.
    // Key is channel ID.
    // Value is one of the following
    // - null (plain number), uses RuuviCachedNumberState
    // - Unit (QuantityType Number), uses RuuviCachedNumberState with unit
    // - Class object, uses given class object with String constructor

    private static final Map<String, @Nullable Object> unitByChannelUID = new HashMap<>(11);
    static {
        unitByChannelUID.put(CHANNEL_ID_ACCELERATIONX, Units.STANDARD_GRAVITY);
        unitByChannelUID.put(CHANNEL_ID_ACCELERATIONY, Units.STANDARD_GRAVITY);
        unitByChannelUID.put(CHANNEL_ID_ACCELERATIONZ, Units.STANDARD_GRAVITY);
        unitByChannelUID.put(CHANNEL_ID_BATTERY, Units.VOLT);
        unitByChannelUID.put(CHANNEL_ID_DATA_FORMAT, null);
        unitByChannelUID.put(CHANNEL_ID_HUMIDITY, Units.PERCENT);
        unitByChannelUID.put(CHANNEL_ID_MEASUREMENT_SEQUENCE_NUMBER, Units.ONE);
        unitByChannelUID.put(CHANNEL_ID_MOVEMENT_COUNTER, Units.ONE);
        unitByChannelUID.put(CHANNEL_ID_PRESSURE, SIUnits.PASCAL);
        unitByChannelUID.put(CHANNEL_ID_TEMPERATURE, SIUnits.CELSIUS);
        unitByChannelUID.put(CHANNEL_ID_TX_POWER, Units.DECIBEL_MILLIWATTS);
        unitByChannelUID.put(CHANNEL_ID_RSSI, Units.DECIBEL_MILLIWATTS);
        unitByChannelUID.put(CHANNEL_ID_TS, RuuviCachedDateTimeState.class);
        unitByChannelUID.put(CHANNEL_ID_GWTS, RuuviCachedDateTimeState.class);
        unitByChannelUID.put(CHANNEL_ID_GWMAC, RuuviCachedStringState.class);
    }

    private final Logger logger = LoggerFactory.getLogger(RuuviTagHandler.class);
    /**
     * Indicator whether we have received data recently
     */
    private final AtomicBoolean receivedData = new AtomicBoolean();
    private final Map<ChannelUID, ChannelState> channelStateByChannelUID = new HashMap<>();
    private @NonNullByDefault({}) ScheduledFuture<?> heartbeatFuture;

    /**
     * Topic with data for this particular Ruuvi Tag. Set in initialize (when configuration is valid).
     */
    private @NonNullByDefault({}) String topic;

    public RuuviTagHandler(Thing thing, int subscribeTimeout) {
        super(thing, subscribeTimeout);
    }

    @Override
    public void initialize() {
        initializeChannelCaches();
        Configuration configuration = getThing().getConfiguration();
        String topic = (String) configuration.get(RuuviGatewayBindingConstants.CONFIGURATION_PROPERTY_TOPIC);
        if (topic == null || topic.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error.missing-topic");
            return;
        }
        Object timeout = configuration.get(RuuviGatewayBindingConstants.CONFIGURATION_PROPERTY_TIMEOUT);
        if (timeout != null) {
            // Note: only in tests
            heartbeatTimeoutMillisecs = Integer.parseInt(timeout.toString());
            logger.warn("Using overridden timeout: {}", heartbeatTimeoutMillisecs);
        }

        this.topic = topic;
        super.initialize();
    }

    private void initializeChannelCaches() {
        for (Channel channel : thing.getChannels()) {
            ChannelUID channelUID = channel.getUID();
            String channelID = channelUID.getId();
            assert unitByChannelUID.containsKey(channelID); // Invariant as all channels should exist in the static map
            Object cacheHint = unitByChannelUID.get(channelID);
            if (cacheHint == null || cacheHint instanceof Unit<?>) {
                Unit<?> unit = (Unit<?>) cacheHint;
                initNumberStateCache(channelUID, unit);
            } else {
                Class<?> cacheType = (Class<?>) cacheHint;
                initCacheWithClass(channelUID, cacheType);
            }

        }
    }

    private <T extends Quantity<T>> RuuviCachedNumberState<?> initNumberStateCache(ChannelUID channelUID,
            @Nullable Unit<T> unit) {
        final RuuviCachedNumberState<?> cached;
        if (unit == null) {
            cached = new RuuviCachedNumberState<>(channelUID);
            channelStateByChannelUID.put(channelUID, cached);
        } else {
            cached = new RuuviCachedNumberState<>(channelUID, unit);
            channelStateByChannelUID.put(channelUID, cached);
        }
        return cached;
    }

    private ChannelState initCacheWithClass(ChannelUID channelUID, Class<?> clazz) {
        try {
            ChannelState cached = (ChannelState) clazz.getConstructor(ChannelUID.class).newInstance(channelUID);
            Objects.requireNonNull(cached); // to make compiler happy
            channelStateByChannelUID.put(channelUID, cached);
            return cached;
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    protected CompletableFuture<@Nullable Void> start(MqttBrokerConnection connection) {
        if (topic == null) {
            // Initialization has not been completed successfully, return early without changing
            // thing status
            return CompletableFuture.completedFuture(null);
        }

        updateStatus(ThingStatus.UNKNOWN);
        return connection.subscribe(topic, this).handle((subscriptionSuccess, subscriptionException) -> {
            if (subscriptionSuccess) {
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, "@text/online.waiting-initial-data");
                heartbeatFuture = scheduler.scheduleWithFixedDelay(this::heartbeat, heartbeatTimeoutMillisecs,
                        heartbeatTimeoutMillisecs, TimeUnit.MILLISECONDS);
            } else {
                if (subscriptionException == null) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "@text/offline.communication-error.mqtt-subscription-failed");
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "@text/offline.communication-error.mqtt-subscription-failed-details [\""
                                    + subscriptionException.getClass().getSimpleName() + "\", \""
                                    + subscriptionException.getMessage() + "\"]");
                }
            }
            return null;
        });
    }

    @Override
    public CompletableFuture<Void> unsubscribeAll() {
        MqttBrokerConnection localConnection = connection;
        String localTopic = topic;
        if (localConnection != null && localTopic != null) {
            return localConnection.unsubscribe(localTopic, this).thenCompose(unsubscribeSuccessful -> null);
        } else {
            return CompletableFuture.completedFuture(null);
        }
    }

    @Override
    protected void stop() {
        ScheduledFuture<?> localHeartbeatFuture = heartbeatFuture;
        if (localHeartbeatFuture != null) {
            localHeartbeatFuture.cancel(true);
            heartbeatFuture = null;
        }
        channelStateByChannelUID.values().forEach(c -> c.getCache().resetState());
        super.stop();
    }

    @Override
    public void dispose() {
        super.dispose();
        channelStateByChannelUID.clear();
    }

    /**
     * Called regularly. Tries to set receivedData to false. If it was already false and thing is ONLINE,
     * update thing as OFFLINE with COMMUNICATION_ERROR.
     */
    private void heartbeat() {
        synchronized (receivedData) {
            if (!receivedData.getAndSet(false) && getThing().getStatus() == ThingStatus.ONLINE) {
                getThing().getChannels().stream().map(Channel::getUID).filter(this::isLinked)
                        .forEach(c -> updateChannelState(c, UnDefType.UNDEF));
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/offline.communication-error.timeout");
            }
        }
    }

    @Override
    public void processMessage(String topic, byte[] payload) {
        receivedData.set(true);

        final GatewayPayload parsed;
        try {
            parsed = GatewayPayloadParser.parse(payload);
        } catch (JsonSyntaxException | IllegalArgumentException e) {
            // Perhaps thing has been configured with wrong topic. Logging extra details with trace
            // Thing status change will be visible in logs with higher log level
            logger.trace("Received invalid data which could not be parsed to any known Ruuvi Tag data formats ({}): {}",
                    e.getMessage(), new String(payload, StandardCharsets.UTF_8));
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.communication-error.parse-error [\"" + e.getMessage() + "\"]");
            return;
        }
        var ruuvitagData = parsed.measurement;

        boolean atLeastOneRuuviFieldPresent = false;
        for (Channel channel : thing.getChannels()) {
            ChannelUID channelUID = channel.getUID();
            switch (channelUID.getId()) {
                case CHANNEL_ID_ACCELERATIONX:
                    atLeastOneRuuviFieldPresent |= updateStateIfLinked(channelUID, ruuvitagData.getAccelerationX());
                    break;
                case CHANNEL_ID_ACCELERATIONY:
                    atLeastOneRuuviFieldPresent |= updateStateIfLinked(channelUID, ruuvitagData.getAccelerationY());
                    break;
                case CHANNEL_ID_ACCELERATIONZ:
                    atLeastOneRuuviFieldPresent |= updateStateIfLinked(channelUID, ruuvitagData.getAccelerationZ());
                    break;
                case CHANNEL_ID_BATTERY:
                    atLeastOneRuuviFieldPresent |= updateStateIfLinked(channelUID, ruuvitagData.getBatteryVoltage());
                    break;
                case CHANNEL_ID_DATA_FORMAT:
                    atLeastOneRuuviFieldPresent |= updateStateIfLinked(channelUID, ruuvitagData.getDataFormat());
                    break;
                case CHANNEL_ID_HUMIDITY:
                    atLeastOneRuuviFieldPresent |= updateStateIfLinked(channelUID, ruuvitagData.getHumidity());
                    break;
                case CHANNEL_ID_MEASUREMENT_SEQUENCE_NUMBER:
                    atLeastOneRuuviFieldPresent |= updateStateIfLinked(channelUID,
                            ruuvitagData.getMeasurementSequenceNumber());
                    break;
                case CHANNEL_ID_MOVEMENT_COUNTER:
                    atLeastOneRuuviFieldPresent |= updateStateIfLinked(channelUID, ruuvitagData.getMovementCounter());
                    break;
                case CHANNEL_ID_PRESSURE:
                    atLeastOneRuuviFieldPresent |= updateStateIfLinked(channelUID, ruuvitagData.getPressure());
                    break;
                case CHANNEL_ID_TEMPERATURE:
                    atLeastOneRuuviFieldPresent |= updateStateIfLinked(channelUID, ruuvitagData.getTemperature());
                    break;
                case CHANNEL_ID_TX_POWER:
                    atLeastOneRuuviFieldPresent |= updateStateIfLinked(channelUID, ruuvitagData.getTxPower());
                    break;
                //
                // Auxiliary channels, not part of bluetooth advertisement
                //
                case CHANNEL_ID_RSSI:
                    atLeastOneRuuviFieldPresent |= updateStateIfLinked(channelUID, parsed.rssi);
                    break;
                case CHANNEL_ID_TS:
                    atLeastOneRuuviFieldPresent |= updateDateTimeStateIfLinked(channelUID, parsed.ts);
                    break;
                case CHANNEL_ID_GWTS:
                    atLeastOneRuuviFieldPresent |= updateDateTimeStateIfLinked(channelUID, parsed.gwts);
                    break;
                case CHANNEL_ID_GWMAC:
                    atLeastOneRuuviFieldPresent |= updateStringStateIfLinked(channelUID, parsed.gwMac);
                    break;
                default:
                    logger.warn("BUG: We have unhandled channel: {}",
                            thing.getChannels().stream().map(Channel::getUID).collect(Collectors.toList()));
            }
        }
        if (atLeastOneRuuviFieldPresent) {
            String thingStatusDescription = getThing().getStatusInfo().getDescription();
            if (getThing().getStatus() != ThingStatus.ONLINE
                    || (thingStatusDescription != null && !thingStatusDescription.isBlank())) {
                // Update thing as ONLINE and possibly clear the thing detail status
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
            }
        } else {
            if (logger.isTraceEnabled()) {
                logger.trace("Received Ruuvi Tag data but no fields could be parsed: {}", HexUtils.bytesToHex(payload));
            }
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.communication-error.parse-error-no-fields");
        }
    }

    @Override
    public @Nullable ChannelState getChannelState(ChannelUID channelUID) {
        return channelStateByChannelUID.get(channelUID);
    }

    @Override
    protected void updateThingStatus(boolean messageReceived, Optional<Boolean> availabilityTopicsSeen) {
        // Not used here
    }

    /**
     * Update number channel state
     *
     * Update is not done when value is null.
     *
     * @param channelUID channel UID
     * @param value value to update
     * @return whether the value was present
     */
    private boolean updateStateIfLinked(ChannelUID channelUID, @Nullable Number value) {
        RuuviCachedNumberState<?> cache = (RuuviCachedNumberState<?>) channelStateByChannelUID.get(channelUID);
        if (cache == null) {
            // Invariant as channels should be initialized already
            logger.warn("Channel {} not initialized. BUG", channelUID);
            return false;
        }
        if (value == null) {
            return false;
        } else {
            cache.update(value);
            if (isLinked(channelUID)) {
                updateChannelState(channelUID, cache.getCache().getChannelState());
            }
            return true;
        }
    }

    /**
     * Update string channel state
     *
     * Update is not done when value is null.
     *
     * @param channelUID channel UID
     * @param value value to update
     * @return whether the value was present
     */
    private <T extends Quantity<T>> boolean updateStringStateIfLinked(ChannelUID channelUID, Optional<String> value) {
        RuuviCachedStringState cache = (RuuviCachedStringState) channelStateByChannelUID.get(channelUID);
        if (cache == null) {
            // Invariant as channels should be initialized already
            logger.error("Channel {} not initialized. BUG", channelUID);
            return false;
        }
        if (value.isEmpty()) {
            return false;
        } else {
            cache.update(value.get());
            if (isLinked(channelUID)) {
                updateChannelState(channelUID, cache.getCache().getChannelState());
            }
            return true;
        }
    }

    /**
     * Update date time channel state
     *
     * Update is not done when value is null.
     *
     * @param channelUID channel UID
     * @param value value to update
     * @return whether the value was present
     */
    private boolean updateDateTimeStateIfLinked(ChannelUID channelUID, Optional<Instant> value) {
        RuuviCachedDateTimeState cache = (RuuviCachedDateTimeState) channelStateByChannelUID.get(channelUID);
        if (cache == null) {
            // Invariant as channels should be initialized already
            logger.error("Channel {} not initialized. BUG", channelUID);
            return false;
        }
        if (value.isEmpty()) {
            return false;
        } else {
            cache.update(value.get());
            if (isLinked(channelUID)) {
                updateChannelState(channelUID, cache.getCache().getChannelState());
            }
            return true;
        }
    }
}
