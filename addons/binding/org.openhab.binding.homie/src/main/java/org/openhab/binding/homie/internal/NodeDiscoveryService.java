package org.openhab.binding.homie.internal;

import static org.openhab.binding.homie.HomieBindingConstants.*;

import java.text.ParseException;
import java.util.Collections;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.homie.handler.HomieDeviceHandler;
import org.openhab.binding.homie.internal.conventionv200.HomieTopic;
import org.openhab.binding.homie.internal.conventionv200.TopicParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeDiscoveryService extends AbstractDiscoveryService implements IMqttMessageListener {
    private static Logger logger = LoggerFactory.getLogger(NodeDiscoveryService.class);
    private final MqttConnection connection;
    private final HomieDeviceHandler bridgeHandler;
    private final TopicParser parser;

    public NodeDiscoveryService(HomieDeviceHandler bridgeHandler, HomieConfiguration config) {
        super(Collections.singleton(HOMIE_NODE_THING_TYPE), DEVICE_DISCOVERY_TIMEOUT_SECONDS);
        this.connection = MqttConnection.fromConfiguration(config, this);
        this.bridgeHandler = bridgeHandler;
        this.parser = new TopicParser(connection.getBasetopic());
    }

    @Override
    protected void startScan() {
        try {
            connection.listenForNodeIds(bridgeHandler.getThing(), this);
        } catch (MqttException e) {
            logger.error("Error listening for node ids", e);
        }
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();

        connection.unsubscribeListenForNodeIds();

    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        try {
            HomieTopic ht = parser.parse(topic);
            if (StringUtils.isNotBlank(ht.getNodeId())) {
                ThingUID thingId = new ThingUID(HOMIE_NODE_THING_TYPE, bridgeHandler.getThing().getUID(),
                        ht.getNodeId());
                DiscoveryResult result = DiscoveryResultBuilder.create(thingId).withLabel(ht.getNodeId())
                        .withBridge(bridgeHandler.getThing().getUID()).build();
                thingDiscovered(result);
            }
        } catch (ParseException e) {
            logger.debug("Unable to parse topic " + topic);
        }
    }

}
