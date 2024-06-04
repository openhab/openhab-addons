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
package org.openhab.binding.mqtt.generic.mapping;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.io.transport.mqtt.MqttException;
import org.openhab.core.io.transport.mqtt.MqttMessageSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Use this class to subscribe to a given MQTT topic via a {@link MqttMessageSubscriber}
 * and convert received values to the type of the given field and notify the user of the changed value.
 *
 * Used by {@link AbstractMqttAttributeClass}.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class SubscribeFieldToMQTTtopic implements MqttMessageSubscriber {
    private final Logger logger = LoggerFactory.getLogger(SubscribeFieldToMQTTtopic.class);
    protected CompletableFuture<@Nullable Void> future = new CompletableFuture<>();
    public final Field field;
    public final FieldChanged changeConsumer;
    public final String topic;
    private final ScheduledExecutorService scheduler;
    private @Nullable ScheduledFuture<?> scheduledFuture;
    private final boolean mandatory;
    private boolean receivedValue = false;

    /**
     * Implement this interface to be notified of an updated field.
     */
    public interface FieldChanged {
        void fieldChanged(Field field, Object value);
    }

    /**
     * Create a {@link SubscribeFieldToMQTTtopic}.
     *
     * @param scheduler A scheduler to realize subscription timeouts.
     * @param field The destination field.
     * @param fieldChangeListener A listener for field changes. This is only called if the received value
     *            could successfully be converted to the field type.
     * @param topic The MQTT topic.
     * @param mandatory True of this field is a mandatory one. A timeout will cause a future to complete exceptionally.
     */
    public SubscribeFieldToMQTTtopic(ScheduledExecutorService scheduler, Field field, FieldChanged fieldChangeListener,
            String topic, boolean mandatory) {
        this.scheduler = scheduler;
        this.field = field;
        this.changeConsumer = fieldChangeListener;
        this.topic = topic;
        this.mandatory = mandatory;
    }

    static Object numberConvert(Object value, Class<?> type) throws IllegalArgumentException, NumberFormatException {
        Object result = value;
        // Handle the conversion case of BigDecimal to Float,Double,Long,Integer and the respective
        // primitive types
        String typeName = type.getSimpleName();
        if (value instanceof BigDecimal bdValue && !type.equals(BigDecimal.class)) {
            if (type.equals(Float.class) || "float".equals(typeName)) {
                result = bdValue.floatValue();
            } else if (type.equals(Double.class) || "double".equals(typeName)) {
                result = bdValue.doubleValue();
            } else if (type.equals(Long.class) || "long".equals(typeName)) {
                result = bdValue.longValue();
            } else if (type.equals(Integer.class) || "int".equals(typeName)) {
                result = bdValue.intValue();
            }
        } else
        // Handle the conversion case of String to Float,Double,Long,Integer,BigDecimal and the respective
        // primitive types
        if (value instanceof String bdValue && !type.equals(String.class)) {
            if (type.equals(Float.class) || "float".equals(typeName)) {
                result = Float.valueOf(bdValue);
            } else if (type.equals(Double.class) || "double".equals(typeName)) {
                result = Double.valueOf(bdValue);
            } else if (type.equals(Long.class) || "long".equals(typeName)) {
                result = Long.valueOf(bdValue);
            } else if (type.equals(BigDecimal.class)) {
                result = new BigDecimal(bdValue);
            } else if (type.equals(Integer.class) || "int".equals(typeName)) {
                result = Integer.valueOf(bdValue);
            } else if (type.equals(Boolean.class) || "boolean".equals(typeName)) {
                result = Boolean.valueOf(bdValue);
            } else if (type.isEnum()) {
                @SuppressWarnings({ "rawtypes", "unchecked" })
                final Class<? extends Enum> enumType = (Class<? extends Enum>) type;
                @SuppressWarnings("unchecked")
                Enum<?> enumValue = Enum.valueOf(enumType, value.toString());
                result = enumValue;
            }
        }
        return result;
    }

    /**
     * Callback by the {@link MqttBrokerConnection} if a matching topic received a new value.
     * Because routing is already done by aforementioned class, the topic parameter is not checked again.
     *
     * @param topic The MQTT topic. Not used.
     * @param payload The MQTT payload.
     */
    @SuppressWarnings({ "null", "unused" })
    @Override
    public void processMessage(String topic, byte[] payload) {
        final ScheduledFuture<?> scheduledFuture = this.scheduledFuture;
        if (scheduledFuture != null) { // Cancel timeout
            scheduledFuture.cancel(false);
            this.scheduledFuture = null;
        }

        if (payload.length == 0) {
            logger.debug("NULL payload on topic: {}", topic);
            return;
        }

        String valueStr = new String(payload, StandardCharsets.UTF_8);
        String originalValueStr = valueStr;

        // Check if there is a manipulation annotation attached to the field
        try {
            final MQTTvalueTransform transform = field.getAnnotation(MQTTvalueTransform.class);
            Object value;
            if (transform != null) {
                // Add a prefix/suffix to the value
                valueStr = transform.prefix() + valueStr + transform.suffix();
                // Split the value if the field is an array. Convert numbers/enums if necessary.
                value = field.getType().isArray() ? valueStr.split(transform.splitCharacter())
                        : numberConvert(valueStr, field.getType());
            } else if (field.getType().isArray()) {
                throw new IllegalArgumentException("No split character defined!");
            } else {
                // Convert numbers/enums if necessary
                value = numberConvert(valueStr, field.getType());
            }
            receivedValue = true;
            changeConsumer.fieldChanged(field, value);
            future.complete(null);
        } catch (IllegalArgumentException e) {
            if (mandatory) {
                future.completeExceptionally(e);
            } else {
                logger.warn("Unable to interpret {} from topic {}", originalValueStr, topic);
                future.complete(null);
            }
        }
    }

    void timeoutReached() {
        if (mandatory) {
            future.completeExceptionally(new Exception("Did not receive mandatory topic value: " + topic));
        } else {
            future.complete(null);
        }
    }

    /**
     * Subscribe to the MQTT topic. A {@link SubscribeFieldToMQTTtopic} cannot be stopped.
     * You need to manually unsubscribe from the {@link #topic} before disposing.
     *
     * @param connection An MQTT connection.
     * @param timeout Timeout in milliseconds. The returned future completes after this time even if no message has
     *            been received for the MQTT topic.
     * @return Returns a future that completes if either a value is received for the topic or a timeout happens.
     * @throws MqttException If an MQTT IO exception happens this exception is thrown.
     */
    public CompletableFuture<@Nullable Void> subscribeAndReceive(MqttBrokerConnection connection, int timeout) {
        connection.subscribe(topic, this).exceptionally(e -> {
            logger.debug("Failed to subscribe to topic {}", topic, e);
            final ScheduledFuture<?> scheduledFuture = this.scheduledFuture;
            if (scheduledFuture != null) { // Cancel timeout
                scheduledFuture.cancel(false);
                this.scheduledFuture = null;
            }
            future.complete(null);
            return false;
        }).thenRun(() -> {
            if (!future.isDone()) {
                this.scheduledFuture = scheduler.schedule(this::timeoutReached, timeout, TimeUnit.MILLISECONDS);
            }
        });
        return future;
    }

    /**
     * Return true if the corresponding field has received a value at least once.
     */
    public boolean hasReceivedValue() {
        return receivedValue;
    }

    /**
     * Return true if the corresponding field is mandatory.
     */
    public boolean isMandatory() {
        return mandatory;
    }
}
