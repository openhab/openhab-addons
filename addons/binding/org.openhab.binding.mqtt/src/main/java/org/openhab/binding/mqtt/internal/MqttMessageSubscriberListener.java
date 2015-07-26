/**
 *
 */
package org.openhab.binding.mqtt.internal;

/**
 * Callback for received MQTT messages
 * 
 * @author Marcus of Wetware Labs
 *
 */
public interface MqttMessageSubscriberListener {
    public abstract void mqttCommandReceived(String topic, String command);

    public abstract void mqttStateReceived(String topic, String state);
}
