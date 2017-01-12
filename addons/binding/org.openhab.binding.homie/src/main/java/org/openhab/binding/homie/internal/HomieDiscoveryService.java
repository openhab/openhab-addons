package org.openhab.binding.homie.internal;

import static org.openhab.binding.homie.HomieBindingConstants.*;

import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HomieDiscoveryService extends AbstractDiscoveryService implements IMqttMessageListener {
    private static Logger logger = LoggerFactory.getLogger(HomieDiscoveryService.class);

    private MqttConnection mqttconnection;
    private Map<String, HomieInformationHolder> thingCache = Collections
            .synchronizedMap(new HashMap<String, HomieInformationHolder>());

    public HomieDiscoveryService() {
        super(Collections.singleton(HOMIE_THING_TYPE), DISCOVERY_TIMEOUT_SECONDS, true);
        logger.info("Homie Discovery Service started");
        mqttconnection = MqttConnection.getInstance();
    }

    protected void activate(ComponentContext componentContext) {
        Dictionary<String, Object> configProperties = componentContext.getProperties();
        String brokerURL = (String) configProperties.get("mqttbrokerurl");
        String basetopic = (String) configProperties.get("basetopic");
        if (StringUtils.isNotBlank(brokerURL) && StringUtils.isNotBlank(basetopic)) {
            // mqttconnection = new MqttConnection(brokerURL, basetopic);
        }
    }

    @Override
    protected void startScan() {
        logger.info("Homie Discovery Service start scan");
        thingCache.clear();
        mqttconnection.listenForDeviceIds(this);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        logger.debug("Homie MQTT Message arrived " + message.toString() + " on topic " + topic);
        Matcher idMatcher = HOMIE_ID_REGEX.matcher(topic);

        if (idMatcher.find()) {
            String homieId = idMatcher.group(1);
            HomieInformationHolder homieDeviceInformation = getCacheEntry(homieId);
            homieDeviceInformation.parse(topic, message.toString());
            if (homieDeviceInformation.isInformationComplete()) {
                logger.debug("Data for Homie Device " + homieId + " is complete");
                ThingUID thingId = new ThingUID(HOMIE_THING_TYPE, homieId);
                thingDiscovered(homieDeviceInformation.toDiscoveryResult(thingId));

            }

        }

    }

    private HomieInformationHolder getCacheEntry(String homieId) {
        if (!thingCache.containsKey(homieId)) {
            thingCache.put(homieId, new HomieInformationHolder());
            logger.info("Homie with id " + homieId + " discovered");
        }
        return thingCache.get(homieId);
    }

}
