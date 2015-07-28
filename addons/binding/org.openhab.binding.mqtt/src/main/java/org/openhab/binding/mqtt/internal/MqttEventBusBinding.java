/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mqtt.internal;

import java.util.Dictionary;

//import javax.naming.ConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.events.AbstractItemEventSubscriber;
import org.eclipse.smarthome.core.items.events.ItemCommandEvent;
import org.eclipse.smarthome.core.items.events.ItemEventFactory;
import org.eclipse.smarthome.core.items.events.ItemStateEvent;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.io.transport.mqtt.MqttService;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Binding to broadcast all commands and/or states received on the OpenHab
 * event bus to predefined topics on an MQTT Broker. The binding can also
 * subscribe to state or command topics and publish all of these to the openHAB
 * event bus.
 *
 * Uses EventSubscriber and EventPublisher mechanisms to connect to the Event Bus.
 *
 * To configure Event Bus binding, define the broker name and the subscribing/publishing topics for commands and states
 * in services/mqtt-eventbus.cfg
 * To configure the MQTT broker associated with that broker name there are two ways
 * - Configure a MQTT Bridge with the same name
 * - Configure the MQTT Service itself by modifying services/io-mqtt.cfg (or other config file that refers to
 * org.eclipse.smarthome.mqtt)
 *
 * @author Davy Vanherbergen
 * @author Marcus of Wetware Labs - Ported to OH2
 * @since 1.3.0
 */
public class MqttEventBusBinding extends AbstractItemEventSubscriber
        implements ManagedService, MqttMessageSubscriberListener {

    private EventPublisher eventPublisher;

    private static final Logger logger = LoggerFactory.getLogger(MqttEventBusBinding.class);

    /** MqttService for sending/receiving messages **/
    private MqttService mqttService;

    /** Message producer for sending state messages to MQTT **/
    private MqttMessagePublisher statePublisher;

    /** Message producer for sending command messages to MQTT **/
    private MqttMessagePublisher commandPublisher;

    /** Message consumer for receiving state messages from MQTT **/
    private MqttMessageSubscriber stateSubscriber;

    /** Message consumer for receiving state messages from MQTT **/
    private MqttMessageSubscriber commandSubscriber;

    /**
     * Name of the broker defined in the openhab.cfg to use for sending messages
     * to
     **/
    private String brokerName;

    /***
     * Called by the framework when this EventSubscriber binding is activated
     */
    public void activate() {
        logger.debug("MQTT event bus: Activating binding.");
    }

    /***
     * Called by the framework when this EventSubscriber binding is deactivated
     */
    public void deactivate() {

        if (StringUtils.isBlank(brokerName)) {
            return;
        }

        if (commandPublisher != null) {
            mqttService.unregisterMessageProducer(brokerName, commandPublisher);
            commandPublisher = null;
        }
        if (statePublisher != null) {
            mqttService.unregisterMessageProducer(brokerName, statePublisher);
            statePublisher = null;
        }
        if (commandSubscriber != null) {
            mqttService.unregisterMessageConsumer(brokerName, commandSubscriber);
            commandSubscriber = null;
        }
        if (stateSubscriber != null) {
            mqttService.unregisterMessageConsumer(brokerName, stateSubscriber);
            stateSubscriber = null;
        }
    }

    /**
     * Extract the item name from the topic. This should be the last part of the
     * topic string, as the topics are in the format of
     * /openHab/myItemName/command
     *
     * @param topicDefinition the topic with the wildcard '+' that was used to subscribe
     * @param actualTopic the received topic
     * @return item name or "unknown".
     */
    private String getItemNameFromTopic(String topicDefinition, String actualTopic) {

        String itemName = "error-parsing-name-from-topic";
        if (StringUtils.isEmpty(actualTopic) || actualTopic.indexOf('/') == -1) {
            return itemName;
        }

        String[] definitionParts = topicDefinition.split("/");
        String[] actualParts = actualTopic.split("/");

        for (int i = 0; i < definitionParts.length; i++) {
            if (definitionParts[i].equalsIgnoreCase("+")) {
                itemName = actualParts[i];
                break;
            }
        }
        return itemName;
    }

    /***
     * Received command from Event Bus
     *
     * @param commandEvent command
     */
    @Override
    protected void receiveCommand(ItemCommandEvent commandEvent) {
        if (commandPublisher == null || commandEvent.getItemCommand() == null || !commandPublisher.isActivated()) {
            return;
        }
        commandPublisher.publish(commandPublisher.getTopic(commandEvent.getItemName()),
                commandEvent.getItemCommand().toString().getBytes());

    }

    /***
     * Received state from Event Bus
     *
     * @param stateEvent state
     */
    @Override
    protected void receiveUpdate(ItemStateEvent stateEvent) {
        if (stateEvent.getItemState() == null || statePublisher == null || !statePublisher.isActivated()) {
            return;
        }
        statePublisher.publish(statePublisher.getTopic(stateEvent.getItemName()),
                stateEvent.getItemState().toString().getBytes());
    }

    /***
     * Received command from MQTT Subscriber
     *
     * @param topic received MQTT topic
     * @param command command as MQTT payload
     */
    @Override
    public void mqttCommandReceived(String topic, String command) {
        Command parsedCommand = commandSubscriber.getCommand(command);
        ItemCommandEvent event = ItemEventFactory
                .createCommandEvent(getItemNameFromTopic(commandSubscriber.getTopic(), topic), parsedCommand);
        eventPublisher.post(event);
    }

    /***
     * Received state from MQTT Subscriber
     *
     * @param topic received MQTT topic
     * @param state state as MQTT payload
     */
    @Override
    public void mqttStateReceived(String topic, String state) {
        State parsedState = commandSubscriber.getState(state);
        ItemStateEvent event = ItemEventFactory
                .createStateEvent(getItemNameFromTopic(stateSubscriber.getTopic(), topic), parsedState);
        eventPublisher.post(event);
    }

    /**
     * Setter for Declarative Services. Adds the MqttService instance.
     *
     * @param mqttService Service.
     */
    public void setMqttService(MqttService mqttService) {
        this.mqttService = mqttService;
    }

    /**
     * Unsetter for Declarative Services.
     *
     * @param mqttService MqttService to remove.
     */
    public void unsetMqttService(MqttService mqttService) {
        this.mqttService = null;
    }

    /**
     * Setter for Declarative Services. Adds the EventPublisher instance.
     *
     * @param mqttService Service.
     */
    protected void setEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * Unsetter for Declarative Services.
     *
     * @param eventPublisher EventPublisher to remove.
     */
    protected void unsetEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = null;
    }

    /**
     * Initialize publisher which publishes all openHAB commands to the given
     * MQTT topic.
     *
     * @param topic publishing topic
     */
    private void setupEventBusStatePublisher(String topic) {

        if (StringUtils.isBlank(topic)) {
            logger.trace("MQTT event bus: No topic defined for State Publisher");
            return;
        }

        try {
            logger.debug("MQTT event bus: Setting up State Publisher for topic {}", topic);
            statePublisher = new MqttMessagePublisher(brokerName + ":" + topic + ":state:*:default");
            mqttService.registerMessageProducer(brokerName, statePublisher);

        } catch (Exception e) {
            logger.error("MQTT event bus: Could not create state publisher: {}", e.getMessage());
        }

    }

    /**
     * Initialize subscriber which broadcasts all received command events onto
     * the openHAB event bus.
     *
     * @param topic to subscribe to.
     */
    private void setupEventBusCommandSubscriber(String topic) {

        if (StringUtils.isBlank(topic)) {
            logger.trace("MQTT event bus: No topic defined for Command Subsriber");
            return;
        }

        try {
            topic = StringUtils.replace(topic, "${item}", "+");
            logger.debug("MQTT event bus: Setting up Command Subscriber for topic {}", topic);
            commandSubscriber = new MqttMessageSubscriber(brokerName + ":" + topic + ":command:default", this);
            mqttService.registerMessageConsumer(brokerName, commandSubscriber);

        } catch (Exception e) {
            logger.error("MQTT event bus: Could not create command subscriber: {}", e.getMessage());
        }

    }

    /**
     * Initialize subscriber which broadcasts all received state events onto the
     * openHAB event bus.
     *
     * @param topic to subscribe to.
     */
    private void setupEventBusStateSubscriber(String topic) {

        if (StringUtils.isBlank(topic)) {
            logger.trace("MQTT event bus: No topic defined for State Subsriber");
            return;
        }

        try {
            topic = StringUtils.replace(topic, "${item}", "+");
            logger.debug("MQTT event bus: Setting up State Subscriber for topic {}", topic);
            stateSubscriber = new MqttMessageSubscriber(brokerName + ":" + topic + ":state:default", this);
            mqttService.registerMessageConsumer(brokerName, stateSubscriber);

        } catch (Exception e) {
            logger.error("MQTT event bus: Could not create state subscriber: {}", e.getMessage());
        }

    }

    /**
     * Initialize publisher which publishes all openHAB commands to the given
     * MQTT topic.
     *
     * @param topic publishing topic.
     */
    private void setupEventBusCommandPublisher(String topic) {

        if (StringUtils.isBlank(topic)) {
            logger.trace("MQTT event bus: No topic defined for Command Publisher");
            return;
        }

        try {
            logger.debug("MQTT event bus: Setting up Command Publisher for topic {}", topic);
            commandPublisher = new MqttMessagePublisher(brokerName + ":" + topic + ":command:*:default");
            mqttService.registerMessageProducer(brokerName, commandPublisher);

        } catch (Exception e) {
            logger.error("MQTT event bus: Could not create command publisher: {}", e.getMessage());
        }
    }

    /***
     * Called by ManagedService framework after services/mqtt-eventbus.cfg is updated or initially loaded
     * Sets publishers and subscribers and updates broker name
     */
    @Override
    public void updated(Dictionary<String, ?> properties) throws ConfigurationException {

        // load event bus publish/subscribe configuration from configuration file
        if (properties == null || properties.isEmpty()) {
            logger.trace("No mqtt-eventbus properties configured.");
            return;
        }

        logger.debug("MQTT event bus: Initializing binding");

        // stop existing publishers/subscribers
        deactivate();

        brokerName = (String) properties.get("broker");
        if (StringUtils.isEmpty(brokerName)) {
            logger.debug("MQTT event bus: No broker name configured! Service not started.");
            return;
        }

        setupEventBusStatePublisher((String) properties.get("statePublishTopic"));
        setupEventBusStateSubscriber((String) properties.get("stateSubscribeTopic"));
        setupEventBusCommandPublisher((String) properties.get("commandPublishTopic"));
        setupEventBusCommandSubscriber((String) properties.get("commandSubscribeTopic"));

        logger.debug("MQTT event bus: Binding initialization completed.");
    }

}
