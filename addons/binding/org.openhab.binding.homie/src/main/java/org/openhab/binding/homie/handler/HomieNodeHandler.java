package org.openhab.binding.homie.handler;

import static org.openhab.binding.homie.internal.conventionv200.HomieConventions.*;

import java.text.ParseException;

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
import org.openhab.binding.homie.internal.MqttConnection;
import org.openhab.binding.homie.internal.conventionv200.HomieTopic;
import org.openhab.binding.homie.internal.conventionv200.NodePropertiesList;
import org.openhab.binding.homie.internal.conventionv200.NodePropertiesListAnnouncementParser;
import org.openhab.binding.homie.internal.conventionv200.TopicParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HomieNodeHandler extends BaseThingHandler implements IMqttMessageListener {

    private Logger logger = LoggerFactory.getLogger(HomieNodeHandler.class);
    private final MqttConnection mqttconnection;
    private final TopicParser topicParser;

    public HomieNodeHandler(Thing thing) {
        super(thing);
        this.mqttconnection = new MqttConnection("NodeHandler#" + thing.getUID().getAsString());
        topicParser = new TopicParser(mqttconnection.getBasetopic());
    }

    @Override
    public void handleRemoval() {
        // TODO Auto-generated method stub
        super.handleRemoval();
    }

    @Override
    public void dispose() {
        mqttconnection.disconnect();
        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

    }

    @Override
    public void initialize() {
        try {
            mqttconnection.subscribe(getBridge(), thing, this);
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
                        propslist.getProperties().stream().forEach(item -> {
                            updateChannel(item, propslist.isPropertySettable(item));
                        });
                    } else if (StringUtils.equals(prop, HOMIE_NODE_PROPERTYTYPE_ANNOUNCEMENT_TOPIC_SUFFIX)) {
                        updatePropertyType(ht.getNodeId(), message);
                    }
                } else {
                    String channelId = ht.getNodeId();
                    Channel channel = getThing().getChannel(channelId);
                    updateState(channel.getUID(), new StringType(message));
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
            logger.error("Unable to parse topic", e);
        }
    }

    private void updatePropertyType(String nodeId, String type) {
        logger.debug("Updating node (thing) type" + nodeId + " to " + type);

    }

    private void updateChannel(String channelId, boolean readonly) {
        if (getThing().getChannel(channelId) == null) {

            ChannelUID channelUID = new ChannelUID(getThing().getUID(), channelId);
            Channel channel = ChannelBuilder.create(channelUID, "Everything").withLabel(channelId)
                    .withKind(ChannelKind.STATE).build();
            ThingBuilder builder = editThing();
            builder.withChannel(channel);
            updateThing(builder.build());
        }
    }

}
