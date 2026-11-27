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

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homie.internal.HomieBindingConstants;
import org.openhab.binding.homie.internal.homie300.PropertyAttributes.DataTypeEnum;
import org.openhab.binding.mqtt.generic.ChannelConfigBuilder;
import org.openhab.binding.mqtt.generic.ChannelState;
import org.openhab.binding.mqtt.generic.mapping.AbstractMqttAttributeClass.AttributeChanged;
import org.openhab.binding.mqtt.generic.mapping.ColorMode;
import org.openhab.binding.mqtt.generic.values.ColorValue;
import org.openhab.binding.mqtt.generic.values.DateTimeValue;
import org.openhab.binding.mqtt.generic.values.NumberValue;
import org.openhab.binding.mqtt.generic.values.OnOffValue;
import org.openhab.binding.mqtt.generic.values.PercentageValue;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.binding.mqtt.generic.values.Value;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.CommonTriggerEvents;
import org.openhab.core.thing.DefaultSystemChannelTypeProvider;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.AutoUpdatePolicy;
import org.openhab.core.thing.type.ChannelDefinition;
import org.openhab.core.thing.type.ChannelDefinitionBuilder;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeBuilder;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.CommandDescription;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.util.UnitUtils;
import org.openhab.core.util.UIDUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A homie Property (which translates into a channel).
 *
 * @author David Graeff - Initial contribution
 * @author Cody Cutrer - Guard metadata and channel subscriptions across lifecycle changes
 */
@NonNullByDefault
public class Property implements AttributeChanged {
    private final Logger logger = LoggerFactory.getLogger(Property.class);
    // Homie data
    public final PropertyAttributes attributes;
    public final Node parentNode;
    public final String propertyID;
    // Runtime state
    protected @Nullable ChannelState channelState;
    public final ChannelUID channelUID;
    public ChannelTypeUID channelTypeUID;
    private @Nullable ChannelType channelType = null;
    private @Nullable ChannelDefinition channelDefinition = null;
    private @Nullable StateDescription stateDescription = null;
    private @Nullable CommandDescription commandDescription = null;
    private final String topic;
    private final DeviceCallback callback;
    protected boolean initialized = false;
    private final AtomicReference<SubscriptionContext> subscription = new AtomicReference<>(new SubscriptionContext());
    private final Object lifecycleLock = new Object();
    private volatile boolean removed;
    private CompletableFuture<@Nullable Void> channelOperations = CompletableFuture.completedFuture(null);

    /**
     * Creates a Homie Property.
     *
     * @param topic The base topic for this property (e.g. "homie/device/node")
     * @param node The parent Homie Node.
     * @param propertyID The unique property ID (among all properties on this Node).
     */
    public Property(String topic, Node node, String propertyID, DeviceCallback callback,
            PropertyAttributes attributes) {
        this.callback = callback;
        this.attributes = attributes;
        this.topic = topic + "/" + propertyID;
        this.parentNode = node;
        this.propertyID = propertyID;
        channelUID = new ChannelUID(node.uid(), UIDUtils.encode(propertyID));
        channelTypeUID = new ChannelTypeUID(HomieBindingConstants.BINDING_ID,
                HomieBindingConstants.CHANNEL_TYPE_STRING);
    }

    /**
     * Subscribe to property attributes. This will not subscribe
     * to the property value though. Call
     * {@link Device#startChannels(MqttBrokerConnection, ScheduledExecutorService, int, HomieThingHandler)}
     * to do that.
     *
     * @return Returns a future that completes as soon as all attribute values have been received or requests have timed
     *         out.
     */
    public CompletableFuture<@Nullable Void> subscribe(MqttBrokerConnection connection,
            ScheduledExecutorService scheduler, int timeout) {
        SubscriptionContext context = newSubscriptionContext(null);
        if (context == null) {
            return CompletableFuture.failedFuture(new CancellationException("Property removed"));
        }
        return context
                .run(() -> attributes.subscribeAndReceive(connection, scheduler, topic,
                        (name, value, broker, executor,
                                complete) -> context.run(() -> initialized && complete ? updateAttributes(context)
                                        : CompletableFuture.completedFuture(null)),
                        timeout))
                .thenCompose(v -> context.run(() -> updateAttributes(context))).handle((v, error) -> context.run(() -> {
                    initialized = true;
                    return error == null ? CompletableFuture.<@Nullable Void> completedFuture(null)
                            : CompletableFuture.<@Nullable Void> failedFuture(error);
                })).thenCompose(future -> future);
    }

    private @Nullable SubscriptionContext newSubscriptionContext(@Nullable SubscriptionContext expected) {
        CompletableFuture<@Nullable Void> predecessor = new CompletableFuture<>();
        SubscriptionContext context = new SubscriptionContext(predecessor);
        // Reserve the reset before publishing the context, so concurrent value starts wait for it.
        context.run(() -> {
            initialized = false;
            // A failed wire unsubscribe can leave the cached state marked subscribed even though
            // the connection removed its consumer. Retry its local stop before reusing it.
            final ChannelState cached = channelState;
            channelOperations = cached == null ? CompletableFuture.completedFuture(null) : cached.stop();
            return CompletableFuture.completedFuture(null);
        });
        final SubscriptionContext previous;
        synchronized (lifecycleLock) {
            if (removed || parentNode.isRemoved()) {
                return null;
            }
            if (expected == null) {
                previous = subscription.getAndSet(context);
            } else if (subscription.compareAndSet(expected, context)) {
                previous = expected;
            } else {
                // The concurrent owner may already be stopping. Do not revive it by retrying here;
                // the device tolerates child supersession while still propagating device shutdown.
                return subscription.get();
            }
        }
        previous.stop(this::unsubscribe).whenComplete((v, error) -> {
            if (error == null) {
                predecessor.complete(null);
            } else {
                predecessor.completeExceptionally(error);
            }
        });
        return context;
    }

    private CompletableFuture<@Nullable Void> updateAttributes(SubscriptionContext context) {
        return queueChannelOperation(context, () -> {
            final ChannelState previous = channelState;
            CompletableFuture<@Nullable Void> stopped = previous == null ? CompletableFuture.completedFuture(null)
                    : previous.stop().exceptionallyCompose(error -> {
                        // MQTT removes the local consumer before its wire unsubscribe completes.
                        // Retry the local stop to clear ChannelState's subscribed flag after failure.
                        logger.debug("Value unsubscribe failed; resetting cached channel state", error);
                        return previous.stop();
                    });
            return stopped.thenCompose(v -> context.run(() -> {
                initialized = true;
                attributesReceived();
                return CompletableFuture.completedFuture(null);
            }));
        });
    }

    // Called from context.run(), which serializes updates to this chain. Await channel replacement
    // and value subscription completion so asynchronous unsubscription cannot race a new value listener.
    private CompletableFuture<@Nullable Void> queueChannelOperation(SubscriptionContext context,
            Supplier<CompletableFuture<@Nullable Void>> action) {
        channelOperations = channelOperations.handle((v, error) -> null).thenCompose(v -> context.run(action));
        return channelOperations;
    }

    private @Nullable BigDecimal convertFromString(String value) {
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException ignore) {
            logger.debug("Cannot convert {} to a number", value);
            return null;
        }
    }

    /**
     * As soon as subscribing succeeded and corresponding MQTT values have been received, the ChannelType and
     * ChannelState are determined.
     */
    public void attributesReceived() {
        createChannelTypeFromAttributes();
        callback.propertyAddedOrChanged(this);
    }

    private void createChannelTypeFromAttributes() {
        final String commandTopic = topic + "/set";
        final String stateTopic = topic;

        Value value;
        Boolean isDecimal = null;

        if (attributes.name.isEmpty()) {
            attributes.name = propertyID;
        }

        Unit<?> unit = UnitUtils.parseUnit(attributes.unit);
        String dimension = null;
        if (unit != null) {
            dimension = UnitUtils.getDimensionName(unit);
        }

        switch (attributes.datatype) {
            case boolean_:
                value = new OnOffValue("true", "false");
                break;
            case color_:
                if ("hsv".equals(attributes.format)) {
                    value = new ColorValue(ColorMode.HSB, null, null, 100);
                } else if ("rgb".equals(attributes.format)) {
                    value = new ColorValue(ColorMode.RGB, null, null, 100);
                } else {
                    logger.warn("Non supported color format: '{}'. Only 'hsv' and 'rgb' are supported",
                            attributes.format);
                    value = new TextValue();
                }
                break;
            case enum_:
                String[] enumValues = attributes.format.split(",");
                value = new TextValue(enumValues);
                break;
            case float_:
            case integer_:
                isDecimal = attributes.datatype == DataTypeEnum.float_;
                String[] s = attributes.format.split("\\:");
                BigDecimal min = s.length == 2 ? convertFromString(s[0]) : null;
                BigDecimal max = s.length == 2 ? convertFromString(s[1]) : null;
                BigDecimal step = (min != null && max != null)
                        ? max.subtract(min).divide(new BigDecimal(100.0), new MathContext(isDecimal ? 2 : 0))
                        : null;
                if (step != null && !isDecimal && step.intValue() <= 0) {
                    step = new BigDecimal(1);
                }
                if (attributes.unit.contains("%") && attributes.settable) {
                    value = new PercentageValue(min, max, step, null, null, null);
                } else {
                    value = new NumberValue(min, max, step, unit);
                }
                break;
            case datetime_:
                value = new DateTimeValue();
                break;
            case string_:
            case unknown:
            default:
                value = new TextValue();
                break;
        }

        ChannelConfigBuilder b = ChannelConfigBuilder.create().makeTrigger(!attributes.retained)
                .withStateTopic(stateTopic);

        if (isDecimal != null && !isDecimal) {
            b = b.withFormatter("%d"); // Apply formatter to only publish integers
        }

        if (attributes.settable) {
            b = b.withCommandTopic(commandTopic).withRetain(false);
        }

        final ChannelState channelState = new ChannelState(b.build(), channelUID, value, callback);
        this.channelState = channelState;

        Map<String, String> channelProperties = new HashMap<>();

        if (attributes.settable) {
            channelProperties.put(HomieBindingConstants.CHANNEL_PROPERTY_SETTABLE,
                    Boolean.toString(attributes.settable));
        }
        if (!attributes.retained) {
            channelProperties.put(HomieBindingConstants.CHANNEL_PROPERTY_RETAINED,
                    Boolean.toString(attributes.retained));
        }

        if (!attributes.format.isEmpty()) {
            channelProperties.put(HomieBindingConstants.CHANNEL_PROPERTY_FORMAT, attributes.format);
        }

        this.channelType = null;
        if (!attributes.retained && !attributes.settable) {
            channelProperties.put(HomieBindingConstants.CHANNEL_PROPERTY_DATATYPE, attributes.datatype.toString());
            if (attributes.datatype.equals(DataTypeEnum.enum_)) {
                if (attributes.format.contains(CommonTriggerEvents.PRESSED)
                        && attributes.format.contains(CommonTriggerEvents.RELEASED)) {
                    this.channelTypeUID = DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_RAWBUTTON;
                } else if (attributes.format.contains(CommonTriggerEvents.SHORT_PRESSED)
                        && attributes.format.contains(CommonTriggerEvents.LONG_PRESSED)
                        && attributes.format.contains(CommonTriggerEvents.DOUBLE_PRESSED)) {
                    this.channelTypeUID = DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_BUTTON;
                } else if (attributes.format.contains(CommonTriggerEvents.DIR1_PRESSED)
                        && attributes.format.contains(CommonTriggerEvents.DIR1_RELEASED)
                        && attributes.format.contains(CommonTriggerEvents.DIR2_PRESSED)
                        && attributes.format.contains(CommonTriggerEvents.DIR2_RELEASED)) {
                    this.channelTypeUID = DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_RAWROCKER;
                } else {
                    this.channelTypeUID = DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_TRIGGER;
                }
            } else {
                this.channelTypeUID = DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_TRIGGER;
            }
        } else {
            if (!attributes.unit.isEmpty()) {
                channelProperties.put(HomieBindingConstants.CHANNEL_PROPERTY_UNIT, attributes.unit);
            }

            String channelTypeId;

            if (attributes.datatype.equals(DataTypeEnum.unknown)) {
                channelTypeId = HomieBindingConstants.CHANNEL_TYPE_STRING;
            } else if (dimension != null) {
                channelTypeId = "number-" + UIDUtils.encode(unit.toString().toLowerCase());
                channelProperties.put(HomieBindingConstants.CHANNEL_PROPERTY_DATATYPE, attributes.datatype.toString());
            } else {
                channelTypeId = attributes.datatype.toString();
                channelTypeId = channelTypeId.substring(0, channelTypeId.length() - 1);
            }
            this.channelTypeUID = new ChannelTypeUID(HomieBindingConstants.BINDING_ID, channelTypeId);
            if (dimension != null) {
                this.channelType = ChannelTypeBuilder
                        .state(channelTypeUID, dimension + " Value (" + unit.toString() + ")", "Number:" + dimension)
                        .withUnitHint(unit.toString()).build();
            }

            if (attributes.retained) {
                this.commandDescription = null;
                this.stateDescription = channelState.getCache().createStateDescription(!attributes.settable).build()
                        .toStateDescription();
            } else if (attributes.settable) {
                this.commandDescription = channelState.getCache().createCommandDescription().build();
                this.stateDescription = null;
            } else {
                this.commandDescription = null;
                this.stateDescription = null;
            }
        }

        var builder = new ChannelDefinitionBuilder(UIDUtils.encode(propertyID), channelTypeUID)
                .withLabel(attributes.name).withProperties(channelProperties);

        if (attributes.settable && !attributes.retained) {
            builder.withAutoUpdatePolicy(AutoUpdatePolicy.VETO);
        }

        this.channelDefinition = builder.build();
    }

    /**
     * Unsubscribe from all property attributes and the property value.
     *
     * @return Returns a future that completes as soon as all unsubscriptions have been performed.
     */
    public CompletableFuture<@Nullable Void> stop() {
        final SubscriptionContext context;
        synchronized (lifecycleLock) {
            context = subscription.get();
        }
        return context.stop(this::unsubscribe);
    }

    // Schema removal is terminal for this instance; ordinary stop still permits cached-value recovery.
    void remove() {
        synchronized (lifecycleLock) {
            removed = true;
        }
        stop();
    }

    private CompletableFuture<@Nullable Void> unsubscribe() {
        initialized = false;
        CompletableFuture<@Nullable Void> metadataStopped = attributes.unsubscribe();
        // A repeated MQTT unsubscribe may finish before an earlier ChannelState.stop(). Drain that
        // operation first so its delayed internalStop cannot clear a cached state reused on restart.
        CompletableFuture<@Nullable Void> valueStopped = channelOperations.handle((v, error) -> null).thenCompose(v -> {
            final ChannelState channelState = this.channelState;
            return channelState == null ? CompletableFuture.completedFuture(null) : channelState.stop();
        });
        return CompletableFuture.allOf(metadataStopped, valueStopped);
    }

    public ChannelUID getChannelUID() {
        return channelUID;
    }

    /**
     * @return Returns the channelState.
     * 
     *         You should have called
     *         {@link Property#subscribe(MqttBrokerConnection, ScheduledExecutorService, int)}
     *         and waited for the future to complete before calling this Getter.
     */
    public @Nullable ChannelState getChannelState() {
        return channelState;
    }

    /**
     * @return Returns the channelType, if a dynamic one is necessary.
     *
     *         You should have called
     *         {@link Property#subscribe(AbstractMqttAttributeClass, int)}
     *         and waited for the future to complete before calling this Getter.
     */
    public @Nullable ChannelType getChannelType() {
        return channelType;
    }

    /**
     * @return Returns the ChannelDefinition.
     * 
     *         You should have called
     *         {@link Property#subscribe(AbstractMqttAttributeClass, int)}
     *         and waited for the future to complete before calling this Getter.
     */
    public @Nullable ChannelDefinition getChannelDefinition() {
        return channelDefinition;
    }

    /**
     * @return Returns the StateDescription.
     *
     *         You should have called
     *         {@link Property#subscribe(AbstractMqttAttributeClass, int)}
     *         and waited for the future to complete before calling this Getter.
     */
    public @Nullable StateDescription getStateDescription() {
        return stateDescription;
    }

    /**
     * @return Returns the CommandDescription.
     *
     *         You should have called
     *         {@link Property#subscribe(AbstractMqttAttributeClass, int)}
     *         and waited for the future to complete before calling this Getter.
     */
    public @Nullable CommandDescription getCommandDescription() {
        return commandDescription;
    }

    /**
     * Subscribes to the state topic on the given connection and informs about updates on the given listener.
     *
     * @param connection A broker connection
     * @param scheduler A scheduler to realize the timeout
     * @param timeout A timeout in milliseconds. Can be 0 to disable the timeout and let the future return earlier.
     * @return A future that completes with true if the subscribing worked and false and/or exceptionally otherwise.
     */
    public CompletableFuture<@Nullable Void> startChannel(MqttBrokerConnection connection,
            ScheduledExecutorService scheduler, int timeout) {
        if (removed || parentNode.isRemoved()) {
            return CompletableFuture.failedFuture(new CancellationException("Property removed"));
        }
        SubscriptionContext current = subscription.get();
        // Cached channels remain usable when their parent node's metadata could not be refreshed.
        SubscriptionContext context = current.isActive() ? current : newSubscriptionContext(current);
        if (context == null) {
            return CompletableFuture.failedFuture(new CancellationException("Property removed"));
        }
        return context.run(() -> queueChannelOperation(context, () -> {
            final ChannelState channelState = this.channelState;
            if (channelState == null) {
                return CompletableFuture.failedFuture(new IllegalStateException("Attributes not yet received!"));
            }
            // A stop clears the callback on the previous ChannelState.
            channelState.setChannelStateUpdateListener(this.callback);
            return channelState.start(connection, scheduler, timeout);
        })).thenCompose(v -> context.run(() -> CompletableFuture.completedFuture(null)));
    }

    /**
     * @return Create a channel for this property.
     */
    public Channel getChannel(ChannelTypeRegistry channelTypeRegistry) {
        ChannelType channelType = channelTypeRegistry.getChannelType(channelTypeUID);

        return ChannelBuilder.create(channelUID, channelType.getItemType()).withType(channelTypeUID)
                .withKind(channelType.getKind()).withLabel(Objects.requireNonNull(channelDefinition.getLabel()))
                .withProperties(channelDefinition.getProperties())
                .withAutoUpdatePolicy(channelDefinition.getAutoUpdatePolicy()).build();
    }

    @Override
    public String toString() {
        return channelUID.toString();
    }

    /**
     * Because the remote device could change any of the property attributes in-between,
     * whenever that happens, we re-create the channel, channel-type and channelState.
     */
    @Override
    public void attributeChanged(String name, Object value, MqttBrokerConnection connection,
            ScheduledExecutorService scheduler, boolean allMandatoryFieldsReceived) {
        SubscriptionContext context = subscription.get();
        context.run(() -> initialized && allMandatoryFieldsReceived ? updateAttributes(context)
                : CompletableFuture.completedFuture(null));
    }

    /**
     * Creates a list of retained topics related to the property
     *
     * @return Returns a list of relative topics
     */
    public List<String> getRetainedTopics() {
        List<String> topics = new ArrayList<>();

        topics.addAll(Stream.of(this.attributes.getClass().getDeclaredFields())
                .map(f -> String.format("%s/$%s", this.propertyID, f.getName())).collect(Collectors.toList()));

        // All exceptions can be ignored because the 'retained' attribute of the PropertyAttributes class
        // is public, is a boolean variable and has a default value (true)
        try {
            if (attributes.getClass().getDeclaredField("retained").getBoolean(attributes)) {
                topics.add(this.propertyID);
            }
        } catch (NoSuchFieldException ignored) {
        } catch (SecurityException ignored) {
        } catch (IllegalArgumentException ignored) {
        } catch (IllegalAccessException ignored) {
        }

        return topics;
    }
}
