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
package org.openhab.binding.mqtt.homie.internal.homie300;

import java.math.BigDecimal;
import java.math.MathContext;
import java.net.URI;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.DefaultSystemChannelTypeProvider;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.binding.mqtt.generic.ChannelConfigBuilder;
import org.openhab.binding.mqtt.generic.ChannelState;
import org.openhab.binding.mqtt.generic.mapping.AbstractMqttAttributeClass;
import org.openhab.binding.mqtt.generic.mapping.AbstractMqttAttributeClass.AttributeChanged;
import org.openhab.binding.mqtt.generic.values.ColorValue;
import org.openhab.binding.mqtt.generic.values.NumberValue;
import org.openhab.binding.mqtt.generic.values.OnOffValue;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.binding.mqtt.generic.values.Value;
import org.openhab.binding.mqtt.homie.generic.internal.MqttBindingConstants;
import org.openhab.binding.mqtt.homie.internal.homie300.PropertyAttributes.DataTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A homie Property (which translates into an ESH channel).
 *
 * @author David Graeff - Initial contribution
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
    // ESH
    public final ChannelUID channelUID;
    public final ChannelTypeUID channelTypeUID;
    private ChannelType type;
    private Channel channel;
    private final String topic;
    private final DeviceCallback callback;
    protected boolean initialized = false;

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
        channelUID = new ChannelUID(node.uid(), propertyID);
        channelTypeUID = new ChannelTypeUID(MqttBindingConstants.BINDING_ID, this.topic.replace('/', '_'));
        type = ChannelTypeBuilder.trigger(channelTypeUID, "dummy").build(); // Dummy value
        channel = ChannelBuilder.create(channelUID, "dummy").build();// Dummy value
    }

    /**
     * Subscribe to property attributes. This will not subscribe
     * to the property value though. Call {@link Device#startChannels(MqttBrokerConnection)} to do that.
     *
     * @return Returns a future that completes as soon as all attribute values have been received or requests have timed
     *         out.
     */
    public CompletableFuture<@Nullable Void> subscribe(MqttBrokerConnection connection,
            ScheduledExecutorService scheduler, int timeout) {
        return attributes.subscribeAndReceive(connection, scheduler, topic, this, timeout)
                // On success, create the channel and tell the handler about this property
                .thenRun(this::attributesReceived)
                // No matter if values have been received or not -> the subscriptions have been performed
                .whenComplete((r, e) -> {
                    initialized = true;
                });
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
        createChannelFromAttribute();
        callback.propertyAddedOrChanged(this);
    }

    /**
     * Creates the ChannelType of the Homie property.
     *
     * @param attributes Attributes of the property.
     * @param channelState ChannelState of the property.
     *
     * @return Returns the ChannelType to be used to build the Channel.
     */
    private ChannelType createChannelType(PropertyAttributes attributes, ChannelState channelState) {
        if (attributes.retained) {
            return ChannelTypeBuilder.state(channelTypeUID, attributes.name, channelState.getItemType())
                    .withConfigDescriptionURI(URI.create(MqttBindingConstants.CONFIG_HOMIE_CHANNEL))
                    .withStateDescription(
                            channelState.getCache().createStateDescription(attributes.unit, !attributes.settable))
                    .build();
        } else {
            if (attributes.datatype.equals(DataTypeEnum.enum_)) {
                if (attributes.format.contains("PRESSED") && attributes.format.contains("RELEASED")) {
                    return DefaultSystemChannelTypeProvider.SYSTEM_RAWBUTTON;
                } else if (attributes.format.contains("SHORT_PRESSED") && attributes.format.contains("LONG_PRESSED")
                        && attributes.format.contains("DOUBLE_PRESSED")) {
                    return DefaultSystemChannelTypeProvider.SYSTEM_BUTTON;
                } else if (attributes.format.contains("DIR1_PRESSED") && attributes.format.contains("DIR1_RELEASED")
                        && attributes.format.contains("DIR2_PRESSED") && attributes.format.contains("DIR2_RELEASED")) {
                    return DefaultSystemChannelTypeProvider.SYSTEM_RAWROCKER;
                }
            }
            return ChannelTypeBuilder.trigger(channelTypeUID, attributes.name)
                    .withConfigDescriptionURI(URI.create(MqttBindingConstants.CONFIG_HOMIE_CHANNEL)).build();
        }
    }

    public void createChannelFromAttribute() {
        final String commandTopic = topic + "/set";
        final String stateTopic = topic;

        Value value;
        Boolean isDecimal = null;

        if (attributes.name == "") {
            attributes.name = propertyID;
        }

        switch (attributes.datatype) {
            case boolean_:
                value = new OnOffValue("true", "false");
                break;
            case color_:
                value = new ColorValue(attributes.format.contains("rgb"), null, null, 100);
                break;
            case enum_:
                String enumValues[] = attributes.format.split(",");
                value = new TextValue(enumValues);
                break;
            case float_:
            case integer_:
                isDecimal = attributes.datatype == DataTypeEnum.float_;
                String s[] = attributes.format.split("\\:");
                BigDecimal min = s.length == 2 ? convertFromString(s[0]) : null;
                BigDecimal max = s.length == 2 ? convertFromString(s[1]) : null;
                BigDecimal step = (min != null && max != null)
                        ? max.subtract(min).divide(new BigDecimal(100.0), new MathContext(isDecimal ? 2 : 0))
                        : null;
                if (step != null && !isDecimal && step.intValue() <= 0) {
                    step = new BigDecimal(1);
                }

                value = new NumberValue(min, max, step);
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
            b = b.withCommandTopic(commandTopic);
        }

        final ChannelState channelState = new ChannelState(b.build(), channelUID, value, callback);
        this.channelState = channelState;

        final ChannelType type = createChannelType(attributes, channelState);
        this.type = type;

        this.channel = ChannelBuilder.create(channelUID, type.getItemType()).withType(type.getUID())
                .withKind(type.getKind()).withLabel(attributes.name)
                .withConfiguration(new Configuration(attributes.asMap())).build();
    }

    /**
     * Unsubscribe from all property attributes and the property value.
     *
     * @return Returns a future that completes as soon as all unsubscriptions have been performed.
     */
    public CompletableFuture<@Nullable Void> stop() {
        final ChannelState channelState = this.channelState;
        if (channelState != null) {
            return channelState.stop().thenCompose(b -> attributes.unsubscribe());
        }
        return attributes.unsubscribe();
    }

    /**
     * @return Returns the channelState. You should have called
     *         {@link Property#subscribe(AbstractMqttAttributeClass, int)}
     *         and waited for the future to complete before calling this Getter.
     */
    public @Nullable ChannelState getChannelState() {
        return channelState;
    }

    /**
     * Subscribes to the state topic on the given connection and informs about updates on the given listener.
     *
     * @param connection A broker connection
     * @param scheduler A scheduler to realize the timeout
     * @param timeout A timeout in milliseconds. Can be 0 to disable the timeout and let the future return earlier.
     * @param channelStateUpdateListener An update listener
     * @return A future that completes with true if the subscribing worked and false and/or exceptionally otherwise.
     */
    public CompletableFuture<@Nullable Void> startChannel(MqttBrokerConnection connection,
            ScheduledExecutorService scheduler, int timeout) {
        final ChannelState channelState = this.channelState;
        if (channelState == null) {
            CompletableFuture<@Nullable Void> f = new CompletableFuture<>();
            f.completeExceptionally(new IllegalStateException("Attributes not yet received!"));
            return f;
        }
        // Make sure we set the callback again which might have been nulled during an stop
        channelState.setChannelStateUpdateListener(this.callback);
        return channelState.start(connection, scheduler, timeout);
    }

    /**
     * @return Returns the channel type of this property.
     *         The type is a dummy only if {@link #channelState} has not been set yet.
     */
    public ChannelType getType() {
        return type;
    }

    /**
     * @return Returns the channel of this property.
     *         The channel is a dummy only if {@link #channelState} has not been set yet.
     */
    public Channel getChannel() {
        return channel;
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
        if (!initialized || !allMandatoryFieldsReceived) {
            return;
        }
        attributesReceived();
    }

    /**
     * Creates a list of retained topics related to the property
     * @return Returns a list of relative topics
     */
    public ArrayList<String> getRetainedTopics() {
        ArrayList<String> topics = new ArrayList<String>();

        topics.addAll(Stream.of(this.attributes.getClass().getDeclaredFields()).map(
            f -> {return String.format("%s/$%s", this.propertyID, f.getName());}).collect(Collectors.toList()));

        // All exceptions can be ignored because the 'retained' attribute of the PropertyAttributes class
        // is public, is a boolean variable and has a default value (true)
        try {
            if(attributes.getClass().getDeclaredField("retained").getBoolean(attributes))
                topics.add(this.propertyID);
        } catch (NoSuchFieldException ignored) {
        } catch (SecurityException ignored) {
        } catch (IllegalArgumentException ignored) {
        } catch (IllegalAccessException ignored) {
        }

        return topics;
    }
}
