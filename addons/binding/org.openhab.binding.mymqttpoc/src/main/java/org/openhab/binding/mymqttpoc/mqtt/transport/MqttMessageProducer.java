package org.openhab.binding.mymqttpoc.mqtt.transport;

/**
 * All message producers which want to register as a message producer to a
 * MqttBrokerConnection should implement this interface.
 *
 * @author Davy Vanherbergen
 * @since 1.3.0
 */
public interface MqttMessageProducer {

    /**
     * Set the sender channel which the message producer should use to publish
     * any message.
     *
     * @param channel
     *            Sender Channel which will be set by the MqttBrokerConnection.
     */
    public void setSenderChannel(MqttSenderChannel channel);
}
