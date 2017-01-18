package org.openhab.binding.mymqttpoc.mqtt.transport;

/**
 * Callback interface for sending a message to the MqttBrokerConnection.
 *
 * @author Davy Vanherbergen
 * @since 1.3.0
 */
public interface MqttSenderChannel {

    /**
     * Send a message to the MQTT broker.
     *
     * @param topic
     *            Topic to publish the message to.
     * @param message
     *            message payload.
     * @throws Exception
     *             if an error occurs during sending.
     */
    public void publish(String topic, byte[] message) throws Exception;

}