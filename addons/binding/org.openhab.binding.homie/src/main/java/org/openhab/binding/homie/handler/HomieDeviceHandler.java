/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homie.handler;

import static org.openhab.binding.homie.HomieBindingConstants.*;
import static org.openhab.binding.homie.internal.conventionv200.HomieConventions.*;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelKind;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.homie.HomieChannelTypeProvider;
import org.openhab.binding.homie.internal.HomieConfiguration;
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

    private abstract class NodeHandler implements IMqttMessageListener {
        private final String nodeId;
        private final String channelId;
        private final ChannelUID channelUID;
        private final MqttConnection connection;
        private final String channelCategory;
        private final TopicParser topicParser;
        private NodePropertiesList properties;

        public NodeHandler(String nodeId, HomieConfiguration configString, String channelCategory) {
            this.nodeId = nodeId;
            this.channelCategory = channelCategory;
            this.channelId = nodeId;
            this.channelUID = new ChannelUID(getThing().getUID(), channelId);
            this.connection = MqttConnection.fromConfiguration(config, this);
            this.topicParser = new TopicParser(config.getBaseTopic());

        }

        protected NodePropertiesList getProperties() {
            return properties;
        }

        public synchronized void init() throws MqttException {
            connection.listenForNode(getThing().getUID().getId(), nodeId, this);
        }

        protected void sendCommand(String property, Command command) {
            try {
                connection.send(getThing().getUID().getId(), getNodeId(), property, command);
            } catch (MqttException e) {
                logger.error("Error sending message", e);
            }
        }

        protected final void handleCommand(Command command) {
            if (command == RefreshType.REFRESH) {
                try {
                    connection.listenForNode(getThing().getUID().getId(), nodeId, this);
                } catch (MqttException e) {
                    logger.error("Error handling command", e);
                }
            } else {
                onHandleCommand(command);
            }
        }

        protected abstract void onHandleCommand(Command command);

        public String getNodeId() {
            return nodeId;
        }

        public String getCategory() {
            return channelCategory;
        }

        public ChannelUID getChannelUID() {
            return channelUID;
        }

        public Channel getChannel() {
            return getThing().getChannel(channelId);
        }

        @Override
        public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
            String msg = mqttMessage.toString();
            HomieTopic ht = topicParser.parse(topic);
            onMqttMessage(ht, msg);
        }

        protected abstract void onMqttMessage(HomieTopic topic, String message);

        public void setProperties(NodePropertiesList properties) {
            this.properties = properties;
        }

    }

    private class ESHNodeHandler extends NodeHandler {

        private String unit;
        private BigDecimal min;
        private BigDecimal max;
        private BigDecimal step;
        private String itemType = "String";
        private boolean isReadonly;
        private String description = "";

        public ESHNodeHandler(String nodeId, HomieConfiguration config, String category) throws MqttException {
            super(nodeId, config, category);
            description = "Homie Node '" + getNodeId() + "'";
        }

        @Override
        public synchronized void init() throws MqttException {
            isReadonly = getProperties().isPropertySettable(ESH_VALUE_TOPIC);
            super.init();

        }

        private void updateChannelType() {
            ChannelTypeUID channelTypeUID = typeProvider.createChannelTypeBySettings(unit, min, max, step, itemType,
                    isReadonly, getCategory());
            Channel currentChannel = getChannel();

            // @formatter:off
                Channel newChannel = ChannelBuilder.create(getChannelUID(), itemType)
                        .withDescription(description)
                        .withKind(ChannelKind.STATE)
                        .withLabel(getNodeId())
                        .withType(channelTypeUID).build();
                // @formatter:on

            Thing newThing;
            if (currentChannel != null) {
                newThing = editThing().withoutChannel(getChannelUID()).withChannel(newChannel).build();
            } else {
                newThing = editThing().withChannel(newChannel).build();
            }
            updateThing(newThing);
        }

        @Override
        public void onHandleCommand(Command command) {
            if (command == RefreshType.REFRESH) {

            } else {
                if (isReadonly) {
                    logger.warn("Node '{}' received command '{}' but is readonly", getNodeId(), command);
                } else {
                    sendCommand(ESH_VALUE_TOPIC, command);
                    handleCommand(RefreshType.REFRESH);
                }
            }
        }

        @Override
        protected void onMqttMessage(HomieTopic topic, String message) {
            if (topic.isESHNodeUnit()) {
                this.unit = message;
                updateChannelType();
            } else if (topic.isESHMin()) {
                this.min = new BigDecimal(message);
                updateChannelType();
            } else if (topic.isESHMax()) {
                this.max = new BigDecimal(message);
                updateChannelType();
            } else if (topic.isESHStep()) {
                this.step = new BigDecimal(message);
                updateChannelType();
            } else if (topic.isESHItemType()) {
                this.itemType = message;
                updateChannelType();
            } else if (topic.isESHDescription()) {
                this.description = message;
                updateChannelType();
            } else if (topic.isESHValue()) {
                State state = transformToState(message);
                updateState(getChannelUID(), state);
            }

        }

        private State transformToState(String message) {
            State result = null;
            if (StringUtils.equals(itemType, "Switch")) {
                boolean on = StringUtils.equalsAnyIgnoreCase(message, "1", "ON", "TRUE");
                result = on ? OnOffType.ON : OnOffType.OFF;
            } else if (StringUtils.equals(itemType, "Number")) {
                result = new DecimalType(message);
            } else if (StringUtils.equals(itemType, "Rollershutter")) {
                boolean up = StringUtils.equalsAnyIgnoreCase(message, "1", "ON", "TRUE", "UP");
                result = up ? UpDownType.UP : UpDownType.DOWN;
            } else if (StringUtils.equals(itemType, "Color")) {
                result = new HSBType(message);
            } else if (StringUtils.equals(itemType, "Contact")) {
                boolean open = StringUtils.equalsAnyIgnoreCase(message, "1", "ON", "TRUE", "OPEN");
                result = open ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
            } else if (StringUtils.equals(itemType, "Dimmer")) {
                result = new PercentType(message);
            } else if (StringUtils.equals(itemType, "Player")) {
                boolean play = StringUtils.equalsAnyIgnoreCase(message, "1", "ON", "TRUE", "PLAY");
                result = play ? PlayPauseType.PLAY : PlayPauseType.PAUSE;
            } else if (StringUtils.equals(itemType, "String")) {
                result = new StringType(message);
            } else {
                logger.warn("Cannot transform message '{}' to a ESH state, using itemtype 'String' instead");
                result = new StringType(message);
            }
            return result;
        }

    }

    private class DefaultNodeHandler extends NodeHandler {

        public DefaultNodeHandler(String nodeId, HomieConfiguration config) throws MqttException {
            super(nodeId, config, "");

        }

        @Override
        protected void onMqttMessage(HomieTopic topic, String message) {
            logger.warn("Handling non ESH Homie Nodes is not implemented yet");

        }

        @Override
        protected void onHandleCommand(Command command) {
            logger.warn("Handling non ESH Homie Nodes is not implemented yet");

        }

    }

    private Map<String, NodeHandler> nodeHandlers = new HashMap<>();

    private Logger logger = LoggerFactory.getLogger(HomieDeviceHandler.class);
    private final MqttConnection mqttconnection;
    private final TopicParser topicParser;
    private final HomieChannelTypeProvider typeProvider;
    private final NodePropertiesListAnnouncementParser parser = new NodePropertiesListAnnouncementParser();

    private final HomieConfiguration config;

    /**
     * Constructor
     *
     * @param thing The Bridge that will be handled
     * @param provider
     */
    public HomieDeviceHandler(Thing thing, HomieChannelTypeProvider provider, HomieConfiguration config) {
        super(thing);
        this.config = config;
        this.mqttconnection = MqttConnection.fromConfiguration(config, this);
        this.typeProvider = provider;
        topicParser = new TopicParser(mqttconnection.getBasetopic());

    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        Channel channel = getThing().getChannel(channelUID.getId());
        if (channel != null) {
            boolean isDeviceChannel = channel.getProperties().containsKey(CHANNELPROPERTY_TOPICSUFFIX);

            if (isDeviceChannel) {
                // reconnect to mqtt to receive the retained messages once more
                mqttconnection.subscribeChannel(channel, this);
            } else {
                NodeHandler handler = this.nodeHandlers.get(channelUID.getId());
                if (handler != null) {
                    handler.handleCommand(command);
                }
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
        logger.debug("Inbound Message. Topic '{}' Message '{}'", topic, mqttMessage);
        String message = mqttMessage.toString();
        try {
            HomieTopic ht = topicParser.parse(topic);

            if (ht.isNodeTypeAnnouncement()) {
                String nodeId = ht.getNodeId();
                NodeHandler nodeHandler = null;
                if (StringUtils.startsWith(message, ESH_TYPE_PREFIX)) {
                    String category = StringUtils.remove(message, ESH_TYPE_PREFIX);
                    nodeHandler = new ESHNodeHandler(nodeId, config, category);
                    logger.debug("Processing node type announcement for {}. Set node type to ESH", nodeId);
                } else {
                    logger.warn("Node {} does not support additional ESH convention", nodeId);
                    nodeHandler = new DefaultNodeHandler(nodeId, config);
                }
                nodeHandlers.put(nodeId, nodeHandler);
            } else if (ht.isNodePropertyAnnouncement()) {
                String nodeId = ht.getNodeId();
                NodeHandler handler = nodeHandlers.get(nodeId);
                if (handler != null) {
                    logger.debug("Processing property list announcement for node {}", nodeId);
                    NodePropertiesList properties = parser.parse(message);
                    handler.setProperties(properties);
                    handler.init();
                }
            } else if (ht.isInternalDeviceProperty()) {
                updateInternalChannelState(ht.getCombinedInternalPropertyName(), message);
            }

        } catch (ParseException e) {
            logger.info("Topic cannot be handled", e);
        }

    }

    private void updateInternalChannelState(String topicSuffix, String message) {
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
            logger.warn("Topic '" + topicSuffix + "' with message '" + message + "' was not processed");
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
