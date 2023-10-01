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
package org.openhab.binding.mqtt.generic.mapping;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * MQTT does not directly support key-value configuration maps. We do need those for device discovery and thing
 * configuration though.<br>
 * Different conventions came up with different solutions, and one is to have "attribute classes".
 * </p>
 *
 * <p>
 * An attribute class is a java class that extends {@link AbstractMqttAttributeClass} and
 * contains annotated fields where each field corresponds to a MQTT topic.
 * To automatically subscribe to all fields, a call to
 * {@link #subscribeAndReceive(MqttBrokerConnection, ScheduledExecutorService, String, AttributeChanged, int)} is
 * required.
 * Unsubscribe with a call to {@link #unsubscribe()}.
 * </p>
 *
 * <p>
 * The Homie 3.x convention uses attribute classes for Devices, Nodes and Properties configuration.
 * </p>
 *
 * <p>
 * The given object, called bean in this context, can consist of all basic java types boolean, int, double, long,
 * String, respective object wrappers like Integer, Double, Long, the BigDecimal type and Enum types. Enums need to be
 * declared within the bean class though. Arrays like String[] are supported as well, but require an annotation because
 * the separator needs to be known.
 * </p>
 * A topic prefix can be defined for the entire class or for a single field. A field annotation overwrites a class
 * annotation.
 *
 * An example:
 *
 * <pre>
 * &#64;TopicPrefix("$")
 * class MyAttributes extends AbstractMqttAttributeClass {
 *    public String testString;
 *    public @MapToField(splitCharacter=",") String[] multipleStrings;
 *
 *    public int anInt = 2;
 *
 *    public enum AnEnum {
 *      Value1,
 *      Value2
 *    };
 *    public AnEnum anEnum = AnEnum.Value1;
 *
 *    public BigDecimal aDecimalValue
 *
 *    &#64;Override
 *    public Object getFieldsOf() {
 *        return this;
 *    }
 * };
 * </pre>
 *
 * You would use this class in this way:
 *
 * <pre>
 * MyAttributes bean = new MyAttributes();
 * bean.subscribe(connection, new ScheduledExecutorService(), "mqtt/topic/bean", null, 500)
 *         .thenRun(() -> System.out.println("subscribed"));
 * </pre>
 *
 * The above attribute class would end up with subscriptions to "mqtt/topic/bean/$testString",
 * "mqtt/topic/bean/$multipleStrings", "mqtt/topic/bean/$anInt" and so on. It is assumed that all MQTT messages are
 * UTF-8 strings.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractMqttAttributeClass implements SubscribeFieldToMQTTtopic.FieldChanged {
    private final Logger logger = LoggerFactory.getLogger(AbstractMqttAttributeClass.class);
    protected transient List<SubscribeFieldToMQTTtopic> subscriptions = new ArrayList<>();
    public transient WeakReference<@Nullable MqttBrokerConnection> connection = new WeakReference<>(null);
    protected transient WeakReference<@Nullable ScheduledExecutorService> scheduler = new WeakReference<>(null);
    private final String prefix;
    private transient String basetopic = "";
    protected transient AttributeChanged attributeChangedListener = (b, c, d, e, f) -> {
    };
    private transient boolean complete = false;

    /**
     * Implement this interface to be notified of an updated field.
     */
    public interface AttributeChanged {
        /**
         * An attribute has changed
         *
         * @param name The name of the field
         * @param value The new value
         * @param connection The broker connection
         * @param scheduler The scheduler that was used for timeouts
         * @param allMandatoryFieldsReceived True if now all mandatory fields have values
         */
        void attributeChanged(String name, Object value, MqttBrokerConnection connection,
                ScheduledExecutorService scheduler, boolean allMandatoryFieldsReceived);
    }

    @SuppressWarnings("null")
    protected AbstractMqttAttributeClass() {
        TopicPrefix topicUsesPrefix = getFieldsOf().getClass().getAnnotation(TopicPrefix.class);
        prefix = (topicUsesPrefix != null) ? topicUsesPrefix.value() : "";
    }

    /**
     * Unsubscribe from all topics of the managed object.
     *
     * @return Returns a future that completes as soon as all unsubscriptions have been performed.
     */
    public CompletableFuture<@Nullable Void> unsubscribe() {
        final MqttBrokerConnection connection = this.connection.get();
        if (connection == null) {
            subscriptions.clear();
            return CompletableFuture.completedFuture(null);
        }

        final CompletableFuture<?>[] futures = subscriptions.stream().map(m -> connection.unsubscribe(m.topic, m))
                .toArray(CompletableFuture[]::new);
        subscriptions.clear();
        return CompletableFuture.allOf(futures);
    }

    /**
     * Subscribe to all subtopics on a MQTT broker connection base topic that match field names of s java object.
     * The fields will be kept in sync with their respective topics. Optionally, you can register update-observers for
     * specific fields.
     *
     * @param connection A MQTT broker connection.
     * @param scheduler A scheduler for timeouts.
     * @param basetopic The base topic. Given a base topic of "base/topic", a field "test" would be registered as
     *            "base/topic/test".
     * @param attributeChangedListener Field change listener
     * @param timeout Timeout per subscription in milliseconds. The returned future completes after this time
     *            even if no
     *            message has been received for a single MQTT topic.
     * @return Returns a future that completes as soon as values for all subscriptions have been received or have timed
     *         out.
     */
    public CompletableFuture<@Nullable Void> subscribeAndReceive(MqttBrokerConnection connection,
            ScheduledExecutorService scheduler, String basetopic, @Nullable AttributeChanged attributeChangedListener,
            int timeout) {
        // We first need to unsubscribe old subscriptions if any
        final CompletableFuture<@Nullable Void> startFuture;
        if (!subscriptions.isEmpty()) {
            startFuture = unsubscribe();
        } else {
            startFuture = CompletableFuture.completedFuture(null);
        }

        this.connection = new WeakReference<>(connection);
        this.scheduler = new WeakReference<>(scheduler);
        this.basetopic = basetopic;
        if (attributeChangedListener != null) {
            this.attributeChangedListener = attributeChangedListener;
        } else {
            this.attributeChangedListener = (b, c, d, e, f) -> {
            };
        }

        subscriptions = getAllFields(getFieldsOf().getClass()).stream().filter(AbstractMqttAttributeClass::filterField)
                .map(this::mapFieldToSubscriber).collect(Collectors.toList());

        final CompletableFuture<?>[] futures = subscriptions.stream()
                .map(m -> m.subscribeAndReceive(connection, timeout)).toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(startFuture, CompletableFuture.allOf(futures));
    }

    /**
     * Return fields of the given class as well as all super classes.
     *
     * @param clazz The class
     * @return A list of Field objects
     */
    protected static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();

        Class<?> currentClass = clazz;
        while (currentClass != null) {
            fields.addAll(Arrays.asList(currentClass.getDeclaredFields()));
            currentClass = currentClass.getSuperclass();
        }

        return fields;
    }

    /**
     * Return true if the given field is not final and not transient
     */
    protected static boolean filterField(Field field) {
        return !Modifier.isFinal(field.getModifiers()) && !Modifier.isTransient(field.getModifiers())
                && !Modifier.isStatic(field.getModifiers());
    }

    /**
     * Maps the given field to a newly created {@link SubscribeFieldToMQTTtopic}.
     * Requires the scheduler of this class to be set.
     *
     * @param field A field
     * @return A newly created {@link SubscribeFieldToMQTTtopic}.
     */
    protected SubscribeFieldToMQTTtopic mapFieldToSubscriber(Field field) {
        final ScheduledExecutorService scheduler = this.scheduler.get();
        if (scheduler == null) {
            throw new IllegalStateException("No scheduler set!");
        }

        MandatoryField mandatoryField = field.getAnnotation(MandatoryField.class);
        @SuppressWarnings("null")
        boolean mandatory = mandatoryField != null;

        TopicPrefix topicUsesPrefix = field.getAnnotation(TopicPrefix.class);
        @SuppressWarnings("null")
        String localPrefix = (topicUsesPrefix != null) ? topicUsesPrefix.value() : prefix;

        final String topic = basetopic + "/" + localPrefix + field.getName();

        return createSubscriber(scheduler, field, topic, mandatory);
    }

    /**
     * Creates a field subscriber for the given field on the given object
     *
     * @param scheduler A scheduler for the timeout functionality
     * @param field The field
     * @param topic The full topic to subscribe to
     * @param mandatory True of this field is a mandatory one. A timeout will cause a future to complete exceptionally.
     * @return Returns a MQTT message subscriber for a single class field
     */
    public SubscribeFieldToMQTTtopic createSubscriber(ScheduledExecutorService scheduler, Field field, String topic,
            boolean mandatory) {
        return new SubscribeFieldToMQTTtopic(scheduler, field, this, topic, mandatory);
    }

    /**
     * Return true if this attribute class has received a value for each mandatory field.
     * In contrast to the parameter "allMandatoryFieldsReceived" of
     * {@link AttributeChanged#attributeChanged(String, Object, MqttBrokerConnection, ScheduledExecutorService, boolean)}
     * this flag will only be updated after that call.
     *
     * <p>
     * You can use this behaviour to compare if a changed field was the last one to complete
     * this attribute class. E.g.:
     * </p>
     *
     * <pre>
     * void attributeChanged(..., boolean allMandatoryFieldsReceived) {
     *   if (allMandatoryFieldsReceived && !attributes.isComplete()) {
     *      // The attribute class is now complete but wasn't before...
     *   }
     * }
     * </pre>
     */
    public boolean isComplete() {
        return complete;
    }

    /**
     * One of the observed MQTT topics got a new value. Apply this to the given field now
     * and propagate the changed value event.
     */
    @Override
    public void fieldChanged(Field field, Object value) {
        // This object holds only a weak reference to connection and scheduler.
        // Attribute classes should perform an unsubscribe when a connection is lost.
        // We fail the future exceptionally here if that didn't happen so that everyone knows.
        final MqttBrokerConnection connection = this.connection.get();
        final ScheduledExecutorService scheduler = this.scheduler.get();
        if (connection == null || scheduler == null) {
            logger.warn("No connection or scheduler set!");
            return;
        }
        // Set field. It is not a reason to fail the future exceptionally if a field could not be set.
        // But at least issue a warning to the log.
        try {
            field.set(getFieldsOf(), value);
            final boolean newComplete = !subscriptions.stream().anyMatch(s -> s.isMandatory() && !s.hasReceivedValue());
            attributeChangedListener.attributeChanged(field.getName(), value, connection, scheduler, newComplete);
            complete = newComplete;
        } catch (IllegalArgumentException | IllegalAccessException e) {
            logger.warn("Could not assign value {} to field {}", value, field, e);
        }
    }

    /**
     * Implement this method in your field class and return "this".
     */
    public abstract Object getFieldsOf();
}
