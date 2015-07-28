/**
 *
 */
package org.openhab.binding.mqtt.internal;

/**
 * Callback for received MQTT messages for the clients of MqttMessageSubscriber
 *
 * @author Marcus of Wetware Labs
 *
 */
public interface MqttMessageSubscriberListener {

    /***
     * Received a MQTT message with a topic defined as Command
     * 
     * @param topic
     * @param command
     */
    public abstract void mqttCommandReceived(String topic, String command);

    /***
     * Received a MQTT message with a topic defined as State
     * 
     * @param topic
     * @param command
     */
    public abstract void mqttStateReceived(String topic, String state);
}
