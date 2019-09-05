/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.mqtt.internal.discovery;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.eclipse.smarthome.io.transport.mqtt.MqttService;
import org.eclipse.smarthome.io.transport.mqtt.MqttServiceObserver;
import org.openhab.binding.mqtt.MqttBindingConstants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MqttServiceDiscoveryService} is responsible for discovering connections on
 * the MqttService shared connection pool.
 *
 * @author David Graeff - Initial contribution
 */
@Component(immediate = true, service = DiscoveryService.class, configurationPid = "discovery.systemmqttbroker")
public class MqttServiceDiscoveryService extends AbstractDiscoveryService implements MqttServiceObserver {
    private final Logger logger = LoggerFactory.getLogger(MqttServiceDiscoveryService.class);
    MqttService mqttService;

    public MqttServiceDiscoveryService() {
        super(Stream.of(MqttBindingConstants.BRIDGE_TYPE_SYSTEMBROKER, MqttBindingConstants.BRIDGE_TYPE_BROKER)
                .collect(Collectors.toSet()), 0, true);
    }

    @Override
    @Activate
    protected void activate(Map<String, Object> config) {
        super.activate(config);
    }

    @Override
    @Deactivate
    protected void deactivate() {
        super.deactivate();
    }

    @Reference
    public void setMqttService(MqttService service) {
        mqttService = service;
    }

    public void unsetMqttService(MqttService service) {
        mqttService = null;
    }

    @Override
    protected void startScan() {
        mqttService.addBrokersListener(this);
        mqttService.getAllBrokerConnections().forEach((brokerId, broker) -> brokerAdded(brokerId, broker));
        stopScan();
    }

    @Override
    protected void startBackgroundDiscovery() {
        if (mqttService == null) {
            return;
        }
        mqttService.addBrokersListener(this);
        mqttService.getAllBrokerConnections().forEach((brokerId, broker) -> brokerAdded(brokerId, broker));
    }

    @Override
    protected void stopBackgroundDiscovery() {
        if (mqttService == null) {
            return;
        }
        mqttService.removeBrokersListener(this);
    }

    @Override
    public void brokerAdded(String brokerId, MqttBrokerConnection broker) {
        logger.trace("Found broker connection {}", brokerId);

        Map<String, Object> properties = new HashMap<>();
        properties.put("host", broker.getHost());
        properties.put("port", broker.getPort());
        properties.put("brokerid", brokerId);
        ThingUID thingUID;
        thingUID = new ThingUID(MqttBindingConstants.BRIDGE_TYPE_SYSTEMBROKER, brokerId);
        thingDiscovered(DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                .withRepresentationProperty("brokerid").withLabel("MQTT Broker").build());
    }

    @Override
    public void brokerRemoved(String brokerId, MqttBrokerConnection broker) {
        ThingUID thingUID;
        thingUID = new ThingUID(MqttBindingConstants.BRIDGE_TYPE_SYSTEMBROKER, brokerId);
        thingRemoved(thingUID);
    }
}
