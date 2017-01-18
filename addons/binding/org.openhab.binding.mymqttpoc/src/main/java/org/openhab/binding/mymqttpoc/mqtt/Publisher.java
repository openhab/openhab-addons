package org.openhab.binding.mymqttpoc.mqtt;

import org.openhab.binding.mymqttpoc.mqtt.transport.MqttMessageProducer;
import org.openhab.binding.mymqttpoc.mqtt.transport.MqttSenderChannel;

public class Publisher implements MqttSenderChannel, MqttMessageProducer {

    @Override
    public void setSenderChannel(MqttSenderChannel channel) {
        // TODO Auto-generated method stub

    }

    @Override
    public void publish(String topic, byte[] message) throws Exception {
        // TODO Auto-generated method stub

    }

}
