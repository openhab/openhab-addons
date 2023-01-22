/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.mihome.internal.discovery;

import static org.openhab.binding.mihome.internal.XiaomiGatewayBindingConstants.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mihome.internal.socket.XiaomiDiscoverySocket;
import org.openhab.binding.mihome.internal.socket.XiaomiSocketListener;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * Discovery service for the Xiaomi bridge.
 *
 * @author Patrick Boos - Initial contribution
 * @author Kuba Wolanin - logger fixes
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, configurationPid = "discovery.mihome")
public class XiaomiBridgeDiscoveryService extends AbstractDiscoveryService implements XiaomiSocketListener {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_BRIDGE);
    private static final int DISCOVERY_TIMEOUT_SEC = 30;

    private final Logger logger = LoggerFactory.getLogger(XiaomiBridgeDiscoveryService.class);
    private final XiaomiDiscoverySocket socket = new XiaomiDiscoverySocket("discovery");

    public XiaomiBridgeDiscoveryService() {
        super(SUPPORTED_THING_TYPES, DISCOVERY_TIMEOUT_SEC, false);
    }

    @Override
    protected void startScan() {
        socket.initialize();
        logger.debug("Start scan for bridges");
        socket.registerListener(this);
        discoverGateways();
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        logger.debug("Stop scan");
        removeOlderResults(getTimestampOfLastScan());
        socket.unregisterListener(this);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        socket.unregisterListener(this);
    }

    @Override
    public void onDataReceived(JsonObject data) {
        logger.debug("Received message {}", data);
        if (data.get("cmd").getAsString().equals("iam")) {
            getGatewayInfo(data);
        }
    }

    @Override
    public int getScanTimeout() {
        return DISCOVERY_TIMEOUT_SEC;
    }

    private void discoverGateways() {
        socket.sendMessage("{\"cmd\":\"whois\"}");
    }

    private void getGatewayInfo(JsonObject jobject) {
        Map<String, Object> properties = new HashMap<>(4);
        String serialNumber = jobject.get("sid").getAsString();
        String ipAddress = jobject.get("ip").getAsString();
        int port = jobject.get("port").getAsInt();

        // It is reported that the gateway is sometimes providing the serial number without the leading 0
        // This is a workaround for a bug in the gateway
        if (serialNumber.length() == 11) {
            serialNumber = "0" + serialNumber;
        }

        properties.put(SERIAL_NUMBER, serialNumber);
        properties.put(HOST, ipAddress);
        properties.put(PORT, port);

        logger.debug("Discovered Xiaomi Gateway - sid: {} ip: {} port: {}", serialNumber, ipAddress, port);

        ThingUID thingUID = new ThingUID(THING_TYPE_BRIDGE, serialNumber);

        thingDiscovered(
                DiscoveryResultBuilder.create(thingUID).withThingType(THING_TYPE_BRIDGE).withProperties(properties)
                        .withLabel("Xiaomi Gateway").withRepresentationProperty(SERIAL_NUMBER).build());
    }
}
