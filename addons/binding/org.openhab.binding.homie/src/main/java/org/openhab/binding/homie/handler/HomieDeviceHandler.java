/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homie.handler;

import static org.openhab.binding.homie.HomieBindingConstants.*;

import java.text.ParseException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.homie.internal.MqttConnection;
import org.openhab.binding.homie.internal.conventionv200.HomieTopic;
import org.openhab.binding.homie.internal.conventionv200.TopicParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HomieDeviceHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Michael Kolb - Initial contribution
 */
public class HomieDeviceHandler extends BaseBridgeHandler implements IMqttMessageListener {

    private Logger logger = LoggerFactory.getLogger(HomieDeviceHandler.class);
    private final MqttConnection mqttconnection;
    private final TopicParser topicParser;

    /**
     * Constructor
     *
     * @param thing The Bridge that will be handled
     */
    public HomieDeviceHandler(Bridge thing, MqttConnection connection) {
        super(thing);
        this.mqttconnection = connection;
        topicParser = new TopicParser(mqttconnection.getBasetopic());

    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        Channel channel = getThing().getChannel(channelUID.getId());
        if (channel != null) {
            if (command == RefreshType.REFRESH) {
                // reconnect to mqtt to receive the retained messages once more
                mqttconnection.subscribeChannel(channel, this);
            }
        }
    }

    @Override
    public void initialize() {
        try {
            mqttconnection.subscribe(thing, this);
            updateStatus(ThingStatus.ONLINE);
        } catch (MqttException e) {
            logger.error("Error subscribing for MQTT topics", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error subscribing MQTT" + e.toString());
        }
    }

    @Override
    public void dispose() {
        mqttconnection.disconnect();
        super.dispose();
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {

        String message = mqttMessage.toString();
        try {

            HomieTopic ht = topicParser.parse(topic);
            if (ht.isDeviceProperty()) {
                String prop = ht.getCombinedInternalPropertyName();

                getThing().getChannels().forEach(channel -> {
                    String topicSuffix = channel.getProperties().get(CHANNELPROPERTY_TOPICSUFFIX);

                    boolean topicMatchesChannel = StringUtils.equals(topicSuffix, prop);
                    if (topicMatchesChannel) {
                        updateChannelState(channel, message);

                    }

                });

            }

        } catch (ParseException e) {
            logger.info("Topic cannot be handled", e);
        }

    }

    private void updateChannelState(Channel channel, String message) {
        ChannelUID chId = channel.getUID();
        State result = null;

        // Special handling for topics that not only change a channel, but also update a thing state
        String indicatedState = channel.getProperties().get(CHANNELPROPERTY_THINGSTATEINDICATOR);
        if (StringUtils.equals(indicatedState, "online")) {
            boolean isOnline = Boolean.parseBoolean(message);
            updateStatus(isOnline ? ThingStatus.ONLINE : ThingStatus.OFFLINE);
        }

        if (StringUtils.equals(channel.getAcceptedItemType(), "Number")) {
            result = new DecimalType(message);
        } else if (StringUtils.equals(channel.getAcceptedItemType(), "String")) {
            result = new StringType(message);
        } else if (StringUtils.equals(channel.getAcceptedItemType(), "Switch")) {
            boolean value = Boolean.parseBoolean(message);
            result = value ? OnOffType.ON : OnOffType.OFF;
        }

        if (result != null) {
            logger.debug("Updating channel " + channel.getUID() + " with parsed state " + result
                    + " which was parsed out of " + message);
            updateState(chId, result);
        }

    }

}
