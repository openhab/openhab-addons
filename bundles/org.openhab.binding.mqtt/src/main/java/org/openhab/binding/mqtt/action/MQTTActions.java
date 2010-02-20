/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.action;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.binding.ThingActions;
import org.eclipse.smarthome.core.thing.binding.ThingActionsScope;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.binding.mqtt.handler.AbstractBrokerHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the automation engine action handler service for the publishMQTT action.
 * <p>
 * <b>Note:</b>The static method <b>invokeMethodOf</b> handles the case where
 * the test <i>actions instanceof MQTTActions</i> fails. This test can fail
 * due to an issue in openHAB core v2.5.0 where the {@link MQTTActions} class
 * can be loaded by a different classloader than the <i>actions</i> instance.
 *
 * @author David Graeff - Initial contribution
 */
@ThingActionsScope(name = "mqtt")
@NonNullByDefault
public class MQTTActions implements ThingActions, IMQTTActions {
    private final Logger logger = LoggerFactory.getLogger(MQTTActions.class);
    private @Nullable AbstractBrokerHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = (AbstractBrokerHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return this.handler;
    }

    @RuleAction(label = "@text/actionLabel", description = "@text/actionDesc")
    public void publishMQTT(
            @ActionInput(name = "topic", label = "@text/actionInputTopicLabel", description = "@text/actionInputTopicDesc") @Nullable String topic,
            @ActionInput(name = "value", label = "@text/actionInputValueLabel", description = "@text/actionInputValueDesc") @Nullable String value) {
        publishMQTT(topic, value, null);
    }

    @Override
    @RuleAction(label = "@text/actionLabel", description = "@text/actionDesc")
    public void publishMQTT(
            @ActionInput(name = "topic", label = "@text/actionInputTopicLabel", description = "@text/actionInputTopicDesc") @Nullable String topic,
            @ActionInput(name = "value", label = "@text/actionInputValueLabel", description = "@text/actionInputValueDesc") @Nullable String value,
            @ActionInput(name = "retain", label = "@text/actionInputRetainlabel", description = "@text/actionInputRetainDesc") @Nullable Boolean retain) {
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
        if (value == null) {
            logger.debug("skipping MQTT publishing to topic '{}' due to null value.", topic);
            return;
        }
        if (topic == null) {
            logger.debug("skipping MQTT publishing of value '{}' as topic is null.", value);
            return;
        }
        if (retain == null) {
            retain = connection.isRetain();
        }
        connection.publish(topic, value.getBytes(), connection.getQos(), retain).thenRun(() -> {
            logger.debug("MQTT publish to {} performed", topic);
        }).exceptionally(e -> {
            logger.warn("MQTT publish to {} failed!", topic);
            return null;
        });
    }

    public static void publishMQTT(@Nullable ThingActions actions, @Nullable String topic, @Nullable String value) {
        publishMQTT(actions, topic, value, null);
    }

    public static void publishMQTT(@Nullable ThingActions actions, @Nullable String topic, @Nullable String value,
            @Nullable Boolean retain) {
        invokeMethodOf(actions).publishMQTT(topic, value, retain);
    }

    private static IMQTTActions invokeMethodOf(@Nullable ThingActions actions) {
        if (actions == null) {
            throw new IllegalArgumentException("actions cannot be null");
        }
        if (actions.getClass().getName().equals(MQTTActions.class.getName())) {
            if (actions instanceof IMQTTActions) {
                return (IMQTTActions) actions;
            } else {
                return (IMQTTActions) Proxy.newProxyInstance(IMQTTActions.class.getClassLoader(),
                        new Class[] { IMQTTActions.class }, (Object proxy, Method method, Object[] args) -> {
                            Method m = actions.getClass().getDeclaredMethod(method.getName(),
                                    method.getParameterTypes());
                            return m.invoke(actions, args);
                        });
            }
        }
        throw new IllegalArgumentException("Actions is not an instance of MQTTActions");
    }
}
