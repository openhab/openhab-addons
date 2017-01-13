package org.openhab.binding.homie.internal;

import static org.openhab.binding.homie.HomieBindingConstants.*;

import java.text.ParseException;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.homie.HomieBindingConstants;
import org.openhab.binding.homie.internal.conventionv200.HomieTopic;
import org.openhab.binding.homie.internal.conventionv200.TopicParser;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceDiscoveryService extends AbstractDiscoveryService implements IMqttMessageListener {

    private static Logger logger = LoggerFactory.getLogger(DeviceDiscoveryService.class);

    private final TopicParser topicParser;
    private MqttConnection mqttconnection;
    private Map<String, HomieInformationHolder> thingCache = Collections
            .synchronizedMap(new HashMap<String, HomieInformationHolder>());

    public DeviceDiscoveryService() {
        super(Collections.singleton(HOMIE_DEVICE_THING_TYPE), DEVICE_DISCOVERY_TIMEOUT_SECONDS, true);
        logger.info("Homie Discovery Service started");
        mqttconnection = new MqttConnection("DeviceDiscovery");
        topicParser = new TopicParser(HomieBindingConstants.BASETOPIC);
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
        mqttconnection.listenForDeviceIds(this);
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();

        mqttconnection.unsubscribeListenForDeviceIds();
        thingCache.clear();
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        logger.debug("Homie MQTT Message arrived " + message.toString() + " on topic " + topic);

        try {
            HomieTopic topicInfo = topicParser.parse(topic);

            String homieId = topicInfo.getDeviceId();
            HomieInformationHolder homieDeviceInformation = getCacheEntry(homieId);
            homieDeviceInformation.parse(topicInfo, message.toString());
            if (homieDeviceInformation.isInformationComplete()) {
                logger.debug("Data for Homie Device " + homieId + " is complete");
                ThingUID thingId = new ThingUID(HOMIE_DEVICE_THING_TYPE, homieId);
                thingDiscovered(homieDeviceInformation.toDiscoveryResult(thingId));
            }
        } catch (ParseException e) {
            logger.debug("Topic cannot be parsed", e);
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
