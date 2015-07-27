package org.openhab.binding.mqtt.handler;

import static org.openhab.binding.mqtt.MqttBindingConstants.*;

import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.io.transport.mqtt.MqttService;
import org.openhab.binding.mqtt.internal.MqttMessagePublisher;
import org.openhab.binding.mqtt.internal.MqttMessageSubscriber;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MqttBridgeHandler} is responsible for handling connection to MQTT service
 *
 * @author Marcus of Wetware Labs - Initial contribution
 */
public class MqttBridgeHandler extends BaseBridgeHandler {

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_BRIDGE);

    private Logger logger = LoggerFactory.getLogger(MqttBridgeHandler.class);

    private List<MqttBridgeListener> mqttBridgeListeners = new CopyOnWriteArrayList<>();

    private String broker;

    /** MqttService for sending/receiving messages **/
    private MqttService mqttService;

    public MqttBridgeHandler(Bridge mqttBridge) {
        super(mqttBridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // TODO Auto-generated method stub

    }

    String getBroker() {
        return broker;
    }

    public void registerMessageConsumer(MqttMessageSubscriber subscriber) {
        mqttService.registerMessageConsumer(broker, subscriber);
    }

    public void registerMessageProducer(MqttMessagePublisher publisher) {
        mqttService.registerMessageProducer(broker, publisher);
    }

    public void unRegisterMessageConsumer(MqttMessageSubscriber subscriber) {
        mqttService.unregisterMessageConsumer(broker, subscriber);
    }

    public void unRegisterMessageProducer(MqttMessagePublisher publisher) {
        mqttService.unregisterMessageProducer(broker, publisher);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing MQTT bridge handler.");

        // final String broker = this.getThing().getBridgeUID().segments[2];
        broker = this.getThing().getUID().getId();
        // broker = (String) getConfig().get(BROKER);

        try {
            // get a reference to org.eclipse.smarthome.io.transport.mqtt service
            ServiceReference<MqttService> mqttServiceReference = bundleContext.getServiceReference(MqttService.class);
            mqttService = bundleContext.getService(mqttServiceReference);
            try {
                // get reference to ConfigurationAdmin and update the configuration of io.transport.mqtt service (PID is
                // actually org.eclipse.smarthome.mqtt)
                ServiceReference<ConfigurationAdmin> configurationAdminReference = bundleContext
                        .getServiceReference(ConfigurationAdmin.class);
                if (configurationAdminReference != null) {
                    ConfigurationAdmin confAdmin = bundleContext.getService(configurationAdminReference);

                    Configuration mqttServiceConf = confAdmin.getConfiguration(MQTT_SERVICE_PID);
                    Dictionary<String, Object> properties = mqttServiceConf.getProperties();
                    if (properties == null) {
                        // confAdmin.createFactoryConfiguration(MQTT_SERVICE_PID);
                        // properties = mqttServiceConf.getProperties();
                        properties = new Hashtable<String, Object>();
                        properties.put("service.pid", MQTT_SERVICE_PID); // CHECK! initialize the PID. Is this
                                                                         // necessary?
                    }

                    if (getConfig().get(URL) != null) {
                        properties.put(broker + "." + URL, getConfig().get(URL));
                    }
                    if (getConfig().get(USER) != null) {
                        properties.put(broker + "." + USER, getConfig().get(USER));
                    }
                    if (getConfig().get(PWD) != null) {
                        properties.put(broker + "." + PWD, getConfig().get(PWD));
                    }
                    if (getConfig().get(CLIENTID) != null) {
                        properties.put(broker + "." + CLIENTID, getConfig().get(CLIENTID));
                    }
                    // mqttServiceConf.update(properties); // FIXME! Updating properties like this via Configuration
                    // class does not notify the mqttservice!
                    mqttService.updated(properties); // CHECK! Is this safe to do? Properties set this way are not
                                                     // propagated to ConfigurationAdmin..
                    updateStatus(ThingStatus.ONLINE);
                }
            } catch (Exception e) {
                logger.error("Failed to set MQTT broker properties");
            }
        } catch (Exception e) {
            logger.error("Failed to get MQTT service!");
        }
    }

    @Override
    public void dispose() {
        logger.debug("Handler disposed.");
        updateStatus(ThingStatus.REMOVED);
    }

    private synchronized void onUpdate() {
    }

    public boolean registerMqttBridgeListener(MqttBridgeListener mqttBridgeListener) {
        if (mqttBridgeListener == null) {
            throw new NullPointerException("It's not allowed to pass a null mqttBridgeListener.");
        }
        boolean result = mqttBridgeListeners.add(mqttBridgeListener);
        if (result) {
            onUpdate();
        }
        return result;
    }

}
