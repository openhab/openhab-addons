/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homie.internal;

import static org.openhab.binding.homie.HomieBindingConstants.*;
import static org.openhab.binding.homie.internal.conventionv200.HomieConventions.NAME_TOPIC_SUFFIX;

import java.text.ParseException;
import java.util.Collections;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.homie.internal.conventionv200.HomieTopic;
import org.openhab.binding.homie.internal.conventionv200.TopicParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceDiscoveryService extends AbstractDiscoveryService implements IMqttMessageListener {

    private static Logger logger = LoggerFactory.getLogger(DeviceDiscoveryService.class);

    private final TopicParser topicParser;
    private MqttConnection mqttconnection;

    public DeviceDiscoveryService(HomieConfiguration config) {
        super(Collections.singleton(HOMIE_DEVICE_THING_TYPE), DEVICE_DISCOVERY_TIMEOUT_SECONDS, true);
        logger.info("Homie Discovery Service started");
        mqttconnection = MqttConnection.fromConfiguration(config, this);
        topicParser = new TopicParser(config.getBaseTopic());
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
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        String message = mqttMessage.toString();

        try {
            HomieTopic ht = topicParser.parse(topic);
            if (ht.isDeviceProperty() && StringUtils.equals(ht.getCombinedInternalPropertyName(), NAME_TOPIC_SUFFIX)) {
                ThingUID thingId = new ThingUID(HOMIE_DEVICE_THING_TYPE, ht.getDeviceId());
                DiscoveryResult dr = DiscoveryResultBuilder.create(thingId).withLabel(message).build();
                thingDiscovered(dr);
            }

        } catch (ParseException e) {
            logger.debug("Topic cannot be parsed", e);
        }

    }

}
