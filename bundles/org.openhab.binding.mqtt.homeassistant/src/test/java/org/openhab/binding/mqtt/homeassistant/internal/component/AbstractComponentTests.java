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
package org.openhab.binding.mqtt.homeassistant.internal.component;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.openhab.binding.mqtt.generic.MqttChannelTypeProvider;
import org.openhab.binding.mqtt.generic.TransformationServiceProvider;
import org.openhab.binding.mqtt.generic.values.Value;
import org.openhab.binding.mqtt.homeassistant.internal.AbstractHomeAssistantTests;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannel;
import org.openhab.binding.mqtt.homeassistant.internal.HaID;
import org.openhab.binding.mqtt.homeassistant.internal.HandlerConfiguration;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.AbstractChannelConfiguration;
import org.openhab.binding.mqtt.homeassistant.internal.handler.HomeAssistantThingHandler;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 * Abstract class for components tests.
 *
 * @author Anton Kharuzhy - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractComponentTests extends AbstractHomeAssistantTests {
    private static final int SUBSCRIBE_TIMEOUT = 10000;
    private static final int ATTRIBUTE_RECEIVE_TIMEOUT = 2000;

    private @Mock @NonNullByDefault({}) ThingHandlerCallback callbackMock;
    private @NonNullByDefault({}) LatchThingHandler thingHandler;

    @BeforeEach
    public void setupThingHandler() {
        final var config = haThing.getConfiguration();

        config.put(HandlerConfiguration.PROPERTY_BASETOPIC, HandlerConfiguration.DEFAULT_BASETOPIC);
        config.put(HandlerConfiguration.PROPERTY_TOPICS, getConfigTopics());

        // Plumb thing status updates through
        doAnswer(invocation -> {
            ((Thing) invocation.getArgument(0)).setStatusInfo((ThingStatusInfo) invocation.getArgument(1));
            return null;
        }).when(callbackMock).statusUpdated(any(Thing.class), any(ThingStatusInfo.class));

        when(callbackMock.getBridge(eq(BRIDGE_UID))).thenReturn(bridgeThing);

        thingHandler = new LatchThingHandler(haThing, channelTypeProvider, transformationServiceProvider,
                SUBSCRIBE_TIMEOUT, ATTRIBUTE_RECEIVE_TIMEOUT);
        thingHandler.setConnection(bridgeConnection);
        thingHandler.setCallback(callbackMock);
        thingHandler = spy(thingHandler);

        thingHandler.initialize();
    }

    @AfterEach
    public void disposeThingHandler() {
        thingHandler.dispose();
    }

    /**
     * {@link org.openhab.binding.mqtt.homeassistant.internal.DiscoverComponents} will wait a config on specified
     * topics.
     * Topics in config must be without prefix and suffix, they can be converted to full with method
     * {@link #configTopicToMqtt(String)}
     *
     * @return config topics
     */
    protected abstract Set<String> getConfigTopics();

    /**
     * Process payload to discover and configure component. Topic should be added to {@link #getConfigTopics()}
     *
     * @param mqttTopic mqtt topic with configuration
     * @param json configuration payload in Json
     * @return discovered component
     */
    protected AbstractComponent<@NonNull ? extends AbstractChannelConfiguration> discoverComponent(String mqttTopic,
            String json) {
        return discoverComponent(mqttTopic, json.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Process payload to discover and configure component. Topic should be added to {@link #getConfigTopics()}
     *
     * @param mqttTopic mqtt topic with configuration
     * @param jsonPayload configuration payload in Json
     * @return discovered component
     */
    protected AbstractComponent<@NonNull ? extends AbstractChannelConfiguration> discoverComponent(String mqttTopic,
            byte[] jsonPayload) {
        var latch = thingHandler.createWaitForComponentDiscoveredLatch(1);
        assertThat(publishMessage(mqttTopic, jsonPayload), is(true));
        try {
            assert latch.await(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            assertThat(e.getMessage(), false);
        }
        return Objects.requireNonNull(thingHandler.getDiscoveredComponent());
    }

    /**
     * Assert channel topics, label and value class
     *
     * @param component component
     * @param channelId channel
     * @param stateTopic state topic or empty string
     * @param commandTopic command topic or empty string
     * @param label label
     * @param valueClass value class
     */
    protected static void assertChannel(AbstractComponent<@NonNull ? extends AbstractChannelConfiguration> component,
            String channelId, String stateTopic, String commandTopic, String label, Class<? extends Value> valueClass) {
        var stateChannel = Objects.requireNonNull(component.getChannel(channelId));
        assertChannel(stateChannel, stateTopic, commandTopic, label, valueClass);
    }

    /**
     * Assert channel topics, label and value class
     *
     * @param stateChannel channel
     * @param stateTopic state topic or empty string
     * @param commandTopic command topic or empty string
     * @param label label
     * @param valueClass value class
     */
    protected static void assertChannel(ComponentChannel stateChannel, String stateTopic, String commandTopic,
            String label, Class<? extends Value> valueClass) {
        assertThat(stateChannel.getChannel().getLabel(), is(label));
        assertThat(stateChannel.getState().getStateTopic(), is(stateTopic));
        assertThat(stateChannel.getState().getCommandTopic(), is(commandTopic));
        assertThat(stateChannel.getState().getCache(), is(instanceOf(valueClass)));
    }

    /**
     * Assert channel state
     *
     * @param component component
     * @param channelId channel
     * @param state expected state
     */
    @SuppressWarnings("null")
    protected static void assertState(AbstractComponent<@NonNull ? extends AbstractChannelConfiguration> component,
            String channelId, State state) {
        assertThat(component.getChannel(channelId).getState().getCache().getChannelState(), is(state));
    }

    protected void spyOnChannelUpdates(AbstractComponent<@NonNull ? extends AbstractChannelConfiguration> component,
            String channelId) {
        // It's already thingHandler, but not the spy version
        component.getChannel(channelId).getState().setChannelStateUpdateListener(thingHandler);
    }

    /**
     * Assert a channel triggers
     */
    protected void assertTriggered(AbstractComponent<@NonNull ? extends AbstractChannelConfiguration> component,
            String channelId, String trigger) {
        verify(thingHandler).triggerChannel(eq(component.getChannel(channelId).getChannelUID()), eq(trigger));
    }

    /**
     * Assert that given payload was published exact-once on given topic.
     *
     * @param mqttTopic Mqtt topic
     * @param payload payload
     */
    protected void assertPublished(String mqttTopic, String payload) {
        verify(bridgeConnection).publish(eq(mqttTopic), ArgumentMatchers.eq(payload.getBytes(StandardCharsets.UTF_8)),
                anyInt(), anyBoolean());
    }

    /**
     * Assert that given payload was published N times on given topic.
     *
     * @param mqttTopic Mqtt topic
     * @param payload payload
     * @param t payload must be published N times on given topic
     */
    protected void assertPublished(String mqttTopic, String payload, int t) {
        verify(bridgeConnection, times(t)).publish(eq(mqttTopic),
                ArgumentMatchers.eq(payload.getBytes(StandardCharsets.UTF_8)), anyInt(), anyBoolean());
    }

    /**
     * Assert that given payload was not published on given topic.
     *
     * @param mqttTopic Mqtt topic
     * @param payload payload
     */
    protected void assertNotPublished(String mqttTopic, String payload) {
        verify(bridgeConnection, never()).publish(eq(mqttTopic),
                ArgumentMatchers.eq(payload.getBytes(StandardCharsets.UTF_8)), anyInt(), anyBoolean());
    }

    /**
     * Publish payload to all subscribers on specified topic.
     *
     * @param mqttTopic Mqtt topic
     * @param payload payload
     * @return true when at least one subscriber found
     */
    protected boolean publishMessage(String mqttTopic, String payload) {
        return publishMessage(mqttTopic, payload.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Publish payload to all subscribers on specified topic.
     *
     * @param mqttTopic Mqtt topic
     * @param payload payload
     * @return true when at least one subscriber found
     */
    protected boolean publishMessage(String mqttTopic, byte[] payload) {
        final var topicSubscribers = subscriptions.get(mqttTopic);

        if (topicSubscribers != null && !topicSubscribers.isEmpty()) {
            topicSubscribers.forEach(mqttMessageSubscriber -> mqttMessageSubscriber.processMessage(mqttTopic, payload));
            return true;
        }
        return false;
    }

    /**
     * Send command to a thing's channel
     * 
     * @param component component
     * @param channelId channel
     * @param command command to send
     */
    protected void sendCommand(AbstractComponent<@NonNull ? extends AbstractChannelConfiguration> component,
            String channelId, Command command) {
        var channel = Objects.requireNonNull(component.getChannel(channelId));
        thingHandler.handleCommand(channel.getChannelUID(), command);
    }

    protected static class LatchThingHandler extends HomeAssistantThingHandler {
        private @Nullable CountDownLatch latch;
        private @Nullable AbstractComponent<@NonNull ? extends AbstractChannelConfiguration> discoveredComponent;

        public LatchThingHandler(Thing thing, MqttChannelTypeProvider channelTypeProvider,
                TransformationServiceProvider transformationServiceProvider, int subscribeTimeout,
                int attributeReceiveTimeout) {
            super(thing, channelTypeProvider, transformationServiceProvider, subscribeTimeout, attributeReceiveTimeout);
        }

        @Override
        public void componentDiscovered(HaID homeAssistantTopicID, AbstractComponent<@NonNull ?> component) {
            accept(List.of(component));
            discoveredComponent = component;
            if (latch != null) {
                latch.countDown();
            }
        }

        public CountDownLatch createWaitForComponentDiscoveredLatch(int count) {
            final var newLatch = new CountDownLatch(count);
            latch = newLatch;
            return newLatch;
        }

        public @Nullable AbstractComponent<@NonNull ? extends AbstractChannelConfiguration> getDiscoveredComponent() {
            return discoveredComponent;
        }
    }
}
