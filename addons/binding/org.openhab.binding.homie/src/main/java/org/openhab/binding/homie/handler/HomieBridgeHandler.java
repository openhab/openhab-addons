/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homie.handler;

import static org.openhab.binding.homie.HomieBindingConstants.*;
import static org.openhab.binding.homie.internal.conventionv200.HomieConventions.*;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.homie.internal.MqttConnection;
import org.openhab.binding.homie.internal.conventionv200.HomieTopic;
import org.openhab.binding.homie.internal.conventionv200.NodePropertiesList;
import org.openhab.binding.homie.internal.conventionv200.NodePropertiesListAnnouncementParser;
import org.openhab.binding.homie.internal.conventionv200.TopicParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HomieBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Michael Kolb - Initial contribution
 */
public class HomieBridgeHandler extends BaseBridgeHandler implements IMqttMessageListener {

    private Logger logger = LoggerFactory.getLogger(HomieBridgeHandler.class);
    private final MqttConnection mqttconnection;
    private final TopicParser topicParser;
    private final List<String> subThings = Collections.synchronizedList(new LinkedList<String>());

    public HomieBridgeHandler(Bridge thing, MqttConnection mqttconnection) {
        super(thing);
        this.mqttconnection = mqttconnection;
        topicParser = new TopicParser(mqttconnection.getBasetopic());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // if(channelUID.getId().equals(CHANNEL_1)) {
        // // TODO: handle command
        //
        // // Note: if communication with thing fails for some reason,
        // // indicate that by setting the status with detail information
        // // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
        // // "Could not control device at IP address x.x.x.x");
        // }
    }

    @Override
    public void initialize() {
        try {
            mqttconnection.subscribe(thing, this);
        } catch (MqttException e) {
            logger.error("Error subscribing for MQTT topics", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error subscribing MQTT" + e.toString());
        }
        // TODO: Initialize the thing. If done set status to ONLINE to indicate proper working.
        // Long running initialization should be done asynchronously in background.
        updateStatus(ThingStatus.ONLINE);

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work
        // as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        String message = mqttMessage.toString();
        try {
            HomieTopic ht = topicParser.parse(topic);
            if (ht.isDeviceProperty()) {
                String prop = ht.getCombinedInternalPropertyName();
                if (StringUtils.equals(prop, STATS_UPTIME_TOPIC_SUFFIX)) {
                    ChannelUID channel = new ChannelUID(getThing().getUID(), CHANNEL_STATS_UPTIME);
                    updateState(channel, new DecimalType(message.toString()));
                } else if (StringUtils.equals(prop, ONLINE_TOPIC_SUFFIX)) {
                    boolean isOnline = StringUtils.equalsIgnoreCase(message.toString(), "true");
                    updateStatus(isOnline ? ThingStatus.ONLINE : ThingStatus.OFFLINE);
                    ChannelUID channel = new ChannelUID(getThing().getUID(), CHANNEL_ONLINE);
                    updateState(channel, isOnline ? OnOffType.ON : OnOffType.OFF);
                } else if (StringUtils.equals(prop, NAME_TOPIC_SUFFIX)) {
                    ChannelUID channel = new ChannelUID(getThing().getUID(), CHANNEL_NAME);
                    updateState(channel, new StringType(message.toString()));
                } else if (StringUtils.equals(prop, LOCALIP_TOPIC_SUFFIX)) {
                    ChannelUID channel = new ChannelUID(getThing().getUID(), CHANNEL_LOCALIP);
                    updateState(channel, new StringType(message.toString()));
                } else if (StringUtils.equals(prop, MAC_TOPIC_SUFFIX)) {
                    ChannelUID channel = new ChannelUID(getThing().getUID(), CHANNEL_MAC);
                    updateState(channel, new StringType(message.toString()));
                } else if (StringUtils.equals(prop, STATS_SIGNAL_TOPIC_SUFFIX)) {
                    ChannelUID channel = new ChannelUID(getThing().getUID(), CHANNEL_STATS_SIGNAL);
                    updateState(channel, new StringType(message.toString()));
                } else if (StringUtils.equals(prop, STATS_INTERVAL_TOPIC_SUFFIX)) {
                    ChannelUID channel = new ChannelUID(getThing().getUID(), CHANNEL_STATS_INTERVAL);
                    updateState(channel, new StringType(message.toString()));
                } else if (StringUtils.equals(prop, FIRMWARE_NAME_TOPIC_SUFFIX)) {
                    ChannelUID channel = new ChannelUID(getThing().getUID(), CHANNEL_FIRMWARE_NAME);
                    updateState(channel, new StringType(message.toString()));
                } else if (StringUtils.equals(prop, FIRMWARE_VERSION_TOPIC_SUFFIX)) {
                    ChannelUID channel = new ChannelUID(getThing().getUID(), CHANNEL_FIRMWARE_VERSION);
                    updateState(channel, new StringType(message.toString()));
                } else if (StringUtils.equals(prop, FIRMWARE_CHECKSUM_TOPIC_SUFFIX)) {
                    ChannelUID channel = new ChannelUID(getThing().getUID(), CHANNEL_FIRMWARE_CHECKSUM);
                    updateState(channel, new StringType(message.toString()));
                } else if (StringUtils.equals(prop, IMPLEMENTATION_TOPIC_SUFFIX)) {
                    ChannelUID channel = new ChannelUID(getThing().getUID(), CHANNEL_IMPLEMENTATION);
                    updateState(channel, new StringType(message.toString()));
                }
            } else if (ht.isNodeProperty()) {
                String nodeId = ht.getNodeId();
                Thing thing = addOrGetNode(nodeId);
                String prop = ht.getCombinedInternalPropertyName();

                if (StringUtils.equals(prop, HOMIE_NODE_PROPERTYLIST_ANNOUNCEMENT_TOPIC_SUFFIX)) {
                    NodePropertiesListAnnouncementParser parser = new NodePropertiesListAnnouncementParser();
                    NodePropertiesList propslist = parser.parse(message.toString());
                    addOrGetChannels(propslist);
                } else if (StringUtils.equals(prop, HOMIE_NODE_PROPERTYTYPE_ANNOUNCEMENT_TOPIC_SUFFIX)) {
                    updatePropertyType(ht.getNodeId(), message);
                }

                // Use this to add channels dynamically
                /*
                 * ThingBuilder thingBuilder = editThing();
                 *
                 * thingBuilder.withChannels(newChannels);
                 * updateThing(thingBuilder.build());
                 */
            }
        } catch (ParseException e) {
            logger.error("Topic cannot be handled", e);
        }

    }

    private void updatePropertyType(String nodeId, String type) {
        logger.debug("Updating node (thing) type" + nodeId + " to " + type);

    }

    private void addOrGetChannels(NodePropertiesList list) {
        logger.debug("Add new properties (channels)" + Arrays.toString(list.getProperties().toArray()));

    }

    private Thing addOrGetNode(String nodeId) {
        logger.debug("Add new node (thing) " + nodeId);
        return getThing();
    }
}
