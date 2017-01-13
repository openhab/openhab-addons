package org.openhab.binding.homie.internal;

import static org.openhab.binding.homie.HomieBindingConstants.*;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MqttConnection {

    private class CallbackHandler implements MqttCallback {

        @Override
        public void messageArrived(String arg0, MqttMessage arg1) throws Exception {

        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken arg0) {

        }

        @Override
        public void connectionLost(Throwable arg0) {
            logger.error("MQTT Connection lost", arg0);
            try {
                client.connect();
            } catch (MqttException e) {
                logger.error("MQTT Reconnect failed", e);
            }

        }

    }

    private static Logger logger = LoggerFactory.getLogger(MqttConnection.class);

    private static MqttConnection instance = null;

    private final String brokerURL;
    private final String basetopic;
    private MqttClient client;
    private final String listenDeviceTopic;

    public String getBasetopic() {
        return basetopic;
    }

    public static MqttConnection getInstance() {
        if (instance == null) {
            instance = new MqttConnection();

        }
        return instance;
    }

    private MqttConnection() {
        this.brokerURL = BROKER_URL;
        this.basetopic = BASETOPIC;
        listenDeviceTopic = String.format("%s/#", basetopic);
        connect();
    }

    private void connect() {
        try {
            logger.debug("Homie MQTT Connection start");
            client = new MqttClient(brokerURL, MQTT_CLIENTID, new MemoryPersistence());
            client.connect();
            client.setCallback(new CallbackHandler());
            logger.debug("Homie MQTT Connection connected");
        } catch (MqttException e) {
            logger.error("MQTT Connect failed", e);
        }
    }

    public void subscribe(Thing thing, IMqttMessageListener messageListener) throws MqttException {
        String topic = String.format("%s/%s/#", basetopic, thing.getUID().getId());
        client.subscribe(topic, messageListener);
    }

    public void listenForDeviceIds(IMqttMessageListener messageListener) {

        logger.debug("Listening for devices on topic " + listenDeviceTopic);
        try {
            client.subscribe(listenDeviceTopic, messageListener);
        } catch (MqttException e) {
            logger.error("Failed to subscribe to topic " + listenDeviceTopic, e);
        }
    }

    public void listenForNodeIds(Bridge bridge, IMqttMessageListener messageListener) throws MqttException {
        subscribe(bridge, messageListener);
    }

    public void unsubscribeListenForDeviceIds() {
        try {
            client.unsubscribe(listenDeviceTopic);
        } catch (MqttException e) {
            logger.error("Failed to unsubscribe from topic " + listenDeviceTopic, e);
        }

    }

}
