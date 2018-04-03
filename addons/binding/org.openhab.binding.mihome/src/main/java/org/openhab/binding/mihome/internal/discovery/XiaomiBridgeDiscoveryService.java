/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mihome.internal.discovery;

import static org.openhab.binding.mihome.XiaomiGatewayBindingConstants.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryServiceCallback;
import org.eclipse.smarthome.config.discovery.ExtendedDiscoveryService;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.mihome.handler.XiaomiBridgeHandler;
import org.openhab.binding.mihome.internal.socket.XiaomiDiscoverySocket;
import org.openhab.binding.mihome.internal.socket.XiaomiSocketListener;
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
@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.mihome")
public class XiaomiBridgeDiscoveryService extends AbstractDiscoveryService
        implements XiaomiSocketListener, ExtendedDiscoveryService {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_BRIDGE);
    private static final int DISCOVERY_TIMEOUT_SEC = 10;

    private final Logger logger = LoggerFactory.getLogger(XiaomiBridgeDiscoveryService.class);
    private XiaomiDiscoverySocket socket;
    private DiscoveryServiceCallback discoveryServiceCallback;

    public XiaomiBridgeDiscoveryService() {
        super(SUPPORTED_THING_TYPES, DISCOVERY_TIMEOUT_SEC, false);
    }

    @Override
    public void setDiscoveryServiceCallback(DiscoveryServiceCallback discoveryServiceCallback) {
        this.discoveryServiceCallback = discoveryServiceCallback;
    }

    @Override
    protected void startScan() {
        socket = (socket == null) ? new XiaomiDiscoverySocket() : socket;
        socket.intialize();
        logger.debug("Start scan for bridges");
        socket.registerListener(this);
        discoverGateways();
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        logger.debug("Stop scan");
        removeOlderResults(getTimestampOfLastScan());
        if (socket != null) {
            socket.unregisterListener(this);
        }
    }

    @Override
    public void deactivate() {
        super.deactivate();
        if (socket != null) {
            socket.unregisterListener(this);
        }
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

        properties.put(SERIAL_NUMBER, serialNumber);
        properties.put(HOST, ipAddress);
        properties.put(PORT, port);

        logger.debug("Discovered Xiaomi Gateway - sid: {} ip: {} port: {}", serialNumber, ipAddress, port);

        ThingUID thingUID = new ThingUID(THING_TYPE_BRIDGE, serialNumber);

        Thing existing = discoveryServiceCallback.getExistingThing(thingUID);
        if (existing != null) {
            logger.debug("Bridge {} already exists - asking it for devices", thingUID);
            // "Thing " + thingUID.toString() + " already exists"
            // Ask this bridge for connected devices
            if (existing.getHandler() instanceof XiaomiBridgeHandler) {
                ((XiaomiBridgeHandler) existing.getHandler()).discoverItems();
            }
        } else {
            thingDiscovered(
                    DiscoveryResultBuilder.create(thingUID).withThingType(THING_TYPE_BRIDGE).withProperties(properties)
                            .withLabel("Xiaomi Gateway").withRepresentationProperty(SERIAL_NUMBER).build());
        }
    }
}
