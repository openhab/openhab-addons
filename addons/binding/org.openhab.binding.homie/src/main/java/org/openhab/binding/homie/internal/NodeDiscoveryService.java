package org.openhab.binding.homie.internal;

import static org.openhab.binding.homie.HomieBindingConstants.*;

import java.util.Collections;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeDiscoveryService extends AbstractDiscoveryService implements IMqttMessageListener {
    private static Logger logger = LoggerFactory.getLogger(NodeDiscoveryService.class);

    public NodeDiscoveryService(Bridge bridge, MqttConnection connection) {
        super(Collections.singleton(HOMIE_NODE_THING_TYPE), DEVICE_DISCOVERY_TIMEOUT_SECONDS);
        try {
            connection.listenForNodeIds(bridge, this);
        } catch (MqttException e) {
            logger.error("Error listening for node ids", e);
        }
    }

    @Override
    protected void startScan() {

    }

    @Override
    public void messageArrived(String arg0, MqttMessage arg1) throws Exception {

    }

}
