/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.internal.action;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.handler.AbstractBrokerHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the automation engine action handler service for the publishMQTT action.
 *
 * @author David Graeff - Initial contribution
 */
@ThingActionsScope(name = "mqtt")
@NonNullByDefault
public class MQTTActions implements ThingActions {
    private final Logger logger = LoggerFactory.getLogger(MQTTActions.class);
    private @Nullable AbstractBrokerHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = (AbstractBrokerHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "@text/actionLabel", description = "@text/actionDesc")
    public void publishMQTT(
            @ActionInput(name = "topic", label = "@text/actionInputTopicLabel", description = "@text/actionInputTopicDesc") @Nullable String topic,
            @ActionInput(name = "value", label = "@text/actionInputValueLabel", description = "@text/actionInputValueDesc") @Nullable String value) {
        publishMQTT(topic, value, null);
    }

    @RuleAction(label = "@text/actionLabel", description = "@text/actionDesc")
    public void publishMQTT(
            @ActionInput(name = "topic", label = "@text/actionInputTopicLabel", description = "@text/actionInputTopicDesc") @Nullable final String topic,
            @ActionInput(name = "value", label = "@text/actionInputValueLabel", description = "@text/actionInputValueDesc") @Nullable final String value,
            @ActionInput(name = "retain", label = "@text/actionInputRetainlabel", description = "@text/actionInputRetainDesc") @Nullable final Boolean retain) {
        if (value == null) {
            logger.debug("skipping MQTT publishing to topic '{}' due to null value.", topic);
            return;
        }
        publishMQTT(topic, value.getBytes(), retain);
    }

    @RuleAction(label = "@text/actionLabel", description = "@text/actionDesc")
    public void publishMQTT(
            @ActionInput(name = "topic", label = "@text/actionInputTopicLabel", description = "@text/actionInputTopicDesc") @Nullable final String topic,
            @ActionInput(name = "value", label = "@text/actionInputValueLabel", description = "@text/actionInputValueDesc") final byte[] value) {
        publishMQTT(topic, value, null);
    }

    @RuleAction(label = "@text/actionLabel", description = "@text/actionDesc")
    public void publishMQTT(
            @ActionInput(name = "topic", label = "@text/actionInputTopicLabel", description = "@text/actionInputTopicDesc") @Nullable final String topic,
            @ActionInput(name = "value", label = "@text/actionInputValueLabel", description = "@text/actionInputValueDesc") final byte[] value,
            @ActionInput(name = "retain", label = "@text/actionInputRetainlabel", description = "@text/actionInputRetainDesc") @Nullable final Boolean retain) {
        AbstractBrokerHandler brokerHandler = handler;
        if (brokerHandler == null) {
            logger.warn("MQTT Action service ThingHandler is null!");
            return;
        }
        MqttBrokerConnection connection = brokerHandler.getConnection();
        if (connection == null) {
            logger.warn("MQTT Action service ThingHandler connection is null!");
            return;
        }
        if (topic == null) {
            logger.debug("skipping MQTT publishing of value '{}' as topic is null.", value);
            return;
        }

        connection.publish(topic, value, connection.getQos(), retain != null && retain.booleanValue()).thenRun(() -> {
            logger.debug("MQTT publish to {} performed", topic);
        }).exceptionally(e -> {
            logger.warn("MQTT publish to {} failed!", topic);
            return null;
        });
    }

    public static void publishMQTT(ThingActions actions, @Nullable String topic, @Nullable String value) {
        publishMQTT(actions, topic, value, null);
    }

    public static void publishMQTT(ThingActions actions, @Nullable String topic, @Nullable String value,
            @Nullable Boolean retain) {
        ((MQTTActions) actions).publishMQTT(topic, value, retain);
    }

    public static void publishMQTT(ThingActions actions, @Nullable String topic, byte[] value) {
        publishMQTT(actions, topic, value, null);
    }

    public static void publishMQTT(ThingActions actions, @Nullable String topic, byte[] value,
            @Nullable Boolean retain) {
        ((MQTTActions) actions).publishMQTT(topic, value, retain);
    }
}
