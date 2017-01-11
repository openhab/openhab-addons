package org.openhab.binding.homie.internal;

import static org.openhab.binding.homie.HomieBindingConstants.*;

import java.util.Collections;
import java.util.regex.Matcher;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HomieDiscoveryService extends AbstractDiscoveryService implements IMqttMessageListener {
    private static Logger logger = LoggerFactory.getLogger(HomieDiscoveryService.class);

    public HomieDiscoveryService() {
        super(Collections.singleton(HOMIE_THING_TYPE), DISCOVERY_TIMEOUT_SECONDS, true);
        logger.info("Homie Discovery Service started");
    }

    @Override
    protected void startScan() {
        logger.info("Homie Discovery Service start scan");
        MqttConnection.getInstance().listenForDeviceIds(this);

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        logger.debug("Homie MQTT Message arrived " + message.toString() + " on topic " + topic);
        Matcher idMatcher = HOMIE_ID_REGEX.matcher(topic);

        if (idMatcher.find()) {
            String homieId = idMatcher.group(1);
            logger.info("Homie with id " + homieId + " discovered");
            ThingUID homieThing = new ThingUID(HOMIE_THING_TYPE, homieId);
            DiscoveryResult result = DiscoveryResultBuilder.create(homieThing).withLabel("Homie Thing").build();
            thingDiscovered(result);
        }

    }

}
