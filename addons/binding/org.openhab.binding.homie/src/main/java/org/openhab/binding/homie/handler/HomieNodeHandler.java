package org.openhab.binding.homie.handler;

import static org.openhab.binding.homie.HomieBindingConstants.CHANNELPROPERTY_TOPICSUFFIX;
import static org.openhab.binding.homie.internal.conventionv200.HomieConventions.*;

import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
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
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.homie.internal.HomieConfiguration;
import org.openhab.binding.homie.internal.MqttConnection;
import org.openhab.binding.homie.internal.conventionv200.HomieTopic;
import org.openhab.binding.homie.internal.conventionv200.NodePropertiesList;
import org.openhab.binding.homie.internal.conventionv200.NodePropertiesListAnnouncementParser;
import org.openhab.binding.homie.internal.conventionv200.TopicParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HomieNodeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Michael Kolb - Initial Contribution
 *
 */
public class HomieNodeHandler extends BaseThingHandler implements IMqttMessageListener {

    private Logger logger = LoggerFactory.getLogger(HomieNodeHandler.class);
    private final MqttConnection mqttconnection;
    private final TopicParser topicParser;

    public HomieNodeHandler(Thing thing, HomieConfiguration config) {
        super(thing);
        this.mqttconnection = MqttConnection.fromConfiguration(config, this);
        topicParser = new TopicParser(mqttconnection.getBasetopic());
    }

    @Override
    public void dispose() {
        mqttconnection.disconnect();
        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        if (command == RefreshType.REFRESH) {
            mqttconnection.subscribeChannel(channelUID, this);
        }
    }

    @Override
    public void initialize() {
        try {
            mqttconnection.listenForNodeProperties(getBridge(), thing, this);
            updateStatus(ThingStatus.ONLINE);
        } catch (MqttException e) {
            logger.error("Error subscribing for MQTT topics", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error subscribing MQTT" + e.toString());
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        try {
            HomieTopic ht = topicParser.parse(topic);
            String message = mqttMessage.toString();
            if (ht.isNodeProperty()) {
                if (ht.isInternalProperty()) {
                    String prop = ht.getCombinedInternalPropertyName();
                    if (StringUtils.equals(prop, HOMIE_NODE_PROPERTYLIST_ANNOUNCEMENT_TOPIC_SUFFIX)) {
                        NodePropertiesListAnnouncementParser parser = new NodePropertiesListAnnouncementParser();
                        NodePropertiesList propslist = parser.parse(message.toString());
                        propslist.getProperties().stream().forEach(property -> {
                            createChannel(property, Collections.singletonMap(CHANNELPROPERTY_TOPICSUFFIX, property));
                        });
                    } else if (StringUtils.equals(prop, HOMIE_NODE_TYPE_ANNOUNCEMENT_TOPIC_SUFFIX)) {
                        String type = message;
                        logger.debug("Updating node (thing) type " + ht.getNodeId() + " to " + type);
                        Map<String, String> props = new HashMap<>(getThing().getProperties());
                        props.put("type", type);
                        ThingBuilder builder = editThing();
                        Thing newThing = builder.withProperties(getThing().getProperties()).build();
                        updateThing(newThing);
                    }
                } else {
                    String channelId = ht.getNodeId();
                    Channel channel = getThing().getChannel(channelId);
                    if (channel != null) {
                        updateState(channel.getUID(), new StringType(message));
                    }
                }
            }
        } catch (ParseException e) {
            logger.error("Unable to parse topic", e);
        }
    }

    private void createChannel(String channelId, Map<String, String> properties) {
        if (getThing().getChannel(channelId) == null) {
            ChannelUID channelUID = new ChannelUID(getThing().getUID(), channelId);
            Channel channel = ChannelBuilder.create(channelUID, "String").withLabel(channelId)
                    .withKind(ChannelKind.STATE).withProperties(properties).build();
            ThingBuilder builder = editThing();
            builder.withChannel(channel);
            updateThing(builder.build());
            handleCommand(channelUID, RefreshType.REFRESH);
        }
    }

}
