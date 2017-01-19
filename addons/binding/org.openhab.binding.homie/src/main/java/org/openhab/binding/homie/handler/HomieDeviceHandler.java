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
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelKind;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.homie.HomieChannelTypeProvider;
import org.openhab.binding.homie.internal.MqttConnection;
import org.openhab.binding.homie.internal.conventionv200.HomieTopic;
import org.openhab.binding.homie.internal.conventionv200.NodePropertiesList;
import org.openhab.binding.homie.internal.conventionv200.NodePropertiesListAnnouncementParser;
import org.openhab.binding.homie.internal.conventionv200.TopicParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HomieDeviceHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Michael Kolb - Initial contribution
 */
public class HomieDeviceHandler extends BaseThingHandler implements IMqttMessageListener {

    private Logger logger = LoggerFactory.getLogger(HomieDeviceHandler.class);
    private final MqttConnection mqttconnection;
    private final TopicParser topicParser;
    private final HomieChannelTypeProvider provider;
    private final NodePropertiesListAnnouncementParser parser = new NodePropertiesListAnnouncementParser();

    /**
     * Constructor
     *
     * @param thing The Bridge that will be handled
     * @param provider
     */
    public HomieDeviceHandler(Thing thing, MqttConnection connection, HomieChannelTypeProvider provider) {
        super(thing);
        this.mqttconnection = connection;
        this.provider = provider;
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
        logger.debug("Message for topic '" + topic + "' arrived");
        String message = mqttMessage.toString();
        try {
            HomieTopic ht = topicParser.parse(topic);
            String prop = ht.getCombinedNodePropertyName();
            if (ht.isNodeTypeAnnouncement()) {
                updateChannelType(ht, message);
            } else if (ht.isNodePropertyAnnouncement()) {
                NodePropertiesList properties = parser.parse(message);
                createChannel(ht, properties);
            } else {
                updateChannelState(prop, message);
            }

        } catch (ParseException e) {
            logger.info("Topic cannot be handled", e);
        }

    }

    private void updateChannelType(HomieTopic ht, String message) {
        String channelId = ht.getNodeId();
        logger.debug("Updating type of channel with ID " + channelId);
    }

    private void createChannel(HomieTopic ht, NodePropertiesList properties) {
        String channelId = ht.getNodeId();
        if (getThing().getChannel(channelId) == null) {
            logger.debug("Creating channel with ID " + channelId);

            ChannelTypeUID channelTypeUID = new ChannelTypeUID(BINDING_ID, "ct-" + channelId);
            provider.addChannelType(channelTypeUID, true);

            Map<String, String> channelProperties = new HashMap<>();
            channelProperties.put(CHANNELPROPERTY_TOPICSUFFIX, ht.getNodeId() + "/#");

            ChannelUID channelUID = new ChannelUID(getThing().getUID(), channelId);
            Channel channel = ChannelBuilder.create(channelUID, "String").withLabel(channelId)
                    .withKind(ChannelKind.STATE).withType(channelTypeUID).withProperties(channelProperties).build();
            ThingBuilder builder = editThing();
            builder.withChannel(channel);
            updateThing(builder.build());
            handleCommand(channelUID, RefreshType.REFRESH);
        } else {
            logger.debug("Channel with ID " + channelId + " already exists");
        }
    }

    private void updateChannelState(String topicSuffix, String message) {
        boolean processedAtLeastOnce = false;
        for (Channel channel : getThing().getChannels()) {
            String chanTopSuffix = channel.getProperties().get(CHANNELPROPERTY_TOPICSUFFIX);
            boolean topicMatchesChannel = StringUtils.equals(chanTopSuffix, topicSuffix);
            if (topicMatchesChannel) {
                processedAtLeastOnce = true;
                ChannelUID chId = channel.getUID();

                // Special handling for topics that not only change a channel, but also update a thing state
                String indicatedState = channel.getProperties().get(CHANNELPROPERTY_THINGSTATEINDICATOR);
                if (StringUtils.equals(indicatedState, "online")) {
                    boolean isOnline = Boolean.parseBoolean(message);
                    updateStatus(isOnline ? ThingStatus.ONLINE : ThingStatus.OFFLINE);
                }

                State result = castToState(channel, message);
                if (result != null) {
                    logger.debug("Updating channel " + channel.getUID() + " with parsed state " + result
                            + " which was parsed out of " + message);
                    updateState(chId, result);
                }
            }
        }

        if (!processedAtLeastOnce) {
            logger.debug("Topic '" + topicSuffix + "' with message '" + message + "' was not processed");
        }

    }

    private State castToState(Channel channel, String message) {
        if (StringUtils.equals(channel.getAcceptedItemType(), "Number")) {
            return new DecimalType(message);
        } else if (StringUtils.equals(channel.getAcceptedItemType(), "String")) {
            return new StringType(message);
        } else if (StringUtils.equals(channel.getAcceptedItemType(), "Switch")) {
            boolean value = Boolean.parseBoolean(message);
            return value ? OnOffType.ON : OnOffType.OFF;
        }
        return null;
    }

}
