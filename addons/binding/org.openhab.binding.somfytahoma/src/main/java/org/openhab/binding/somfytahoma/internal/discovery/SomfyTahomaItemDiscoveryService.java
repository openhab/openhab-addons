/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 * <p>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.somfytahoma.internal.discovery;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryServiceCallback;
import org.eclipse.smarthome.config.discovery.ExtendedDiscoveryService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.somfytahoma.handler.SomfyTahomaBridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.openhab.binding.somfytahoma.SomfyTahomaBindingConstants.*;

/**
 * The {@link SomfyTahomaItemDiscoveryService} discovers rollershutters and
 * action groups associated with your TahomaLink cloud account.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class SomfyTahomaItemDiscoveryService extends AbstractDiscoveryService implements ExtendedDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(SomfyTahomaItemDiscoveryService.class);
    private SomfyTahomaBridgeHandler bridge = null;
    private DiscoveryServiceCallback discoveryServiceCallback;


    private static final int DISCOVERY_TIMEOUT_SEC = 10;

    public SomfyTahomaItemDiscoveryService(SomfyTahomaBridgeHandler bridgeHandler) {
        super(DISCOVERY_TIMEOUT_SEC);
        logger.debug("Creating discovery service");
        this.bridge = bridgeHandler;
        bridgeHandler.setDiscoveryService(this);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return new HashSet<>(Arrays.asList(
                THING_TYPE_GATEWAY,
                THING_TYPE_ROLLERSHUTTER,
                THING_TYPE_SCREEN,
                THING_TYPE_VENETIANBLIND,
                THING_TYPE_EXTERIORSCREEN,
                THING_TYPE_EXTERIORVENETIANBLIND,
                THING_TYPE_GARAGEDOOR,
                THING_TYPE_ACTIONGROUP,
                THING_TYPE_AWNING,
                THING_TYPE_ONOFF
        ));
    }

    @Override
    public void setDiscoveryServiceCallback(DiscoveryServiceCallback discoveryServiceCallback) {
        this.discoveryServiceCallback = discoveryServiceCallback;
    }

    @Override
    protected void startScan() {
        logger.debug("Starting scanning for items...");
        bridge.setDiscoveryService(this);
        bridge.startDiscovery();
    }

    public void rollerShutterDiscovered(String label, String deviceURL, String oid) {
        deviceDiscovered(label, deviceURL, oid, THING_TYPE_ROLLERSHUTTER);
    }

    public void exteriorScreenDiscovered(String label, String deviceURL, String oid) {
        deviceDiscovered(label, deviceURL, oid, THING_TYPE_EXTERIORSCREEN);
    }

    public void screenDiscovered(String label, String deviceURL, String oid) {
        deviceDiscovered(label, deviceURL, oid, THING_TYPE_SCREEN);
    }

    public void exteriorVenetianBlindDiscovered(String label, String deviceURL, String oid) {
        deviceDiscovered(label, deviceURL, oid, THING_TYPE_EXTERIORVENETIANBLIND);
    }

    public void venetianBlindDiscovered(String label, String deviceURL, String oid) {
        deviceDiscovered(label, deviceURL, oid, THING_TYPE_VENETIANBLIND);
    }

    public void garageDoorDiscovered(String label, String deviceURL, String oid) {
        deviceDiscovered(label, deviceURL, oid, THING_TYPE_GARAGEDOOR);
    }

    public void awningDiscovered(String label, String deviceURL, String oid) {
        deviceDiscovered(label, deviceURL, oid, THING_TYPE_AWNING);
    }

    public void onOffDiscovered(String label, String deviceURL, String oid) {
        deviceDiscovered(label, deviceURL, oid, THING_TYPE_ONOFF);
    }

    public void actionGroupDiscovered(String label, String deviceURL, String oid) {
        deviceDiscovered(label, deviceURL, oid, THING_TYPE_ACTIONGROUP);
    }

    private void deviceDiscovered(String label, String deviceURL, String oid, ThingTypeUID thingTypeUID) {
        Map<String, Object> properties = new HashMap<>(1);
        properties.put("url", deviceURL);

        ThingUID thingUID = new ThingUID(thingTypeUID, bridge.getThing().getUID(), oid);

        if (discoveryServiceCallback.getExistingThing(thingUID) == null) {
            logger.debug("Detected a/an {} - label: {} oid: {}", thingTypeUID.getId(), label, oid);
            thingDiscovered(
                    DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID).withProperties(properties)
                            .withRepresentationProperty("url").withLabel(label)
                            .withBridge(bridge.getThing().getUID()).build());
        }
    }

    public void gatewayDiscovered(String id) {
        Map<String, Object> properties = new HashMap<>(1);
        properties.put("id", id);

        ThingUID thingUID = new ThingUID(THING_TYPE_GATEWAY, bridge.getThing().getUID(), id);

        if (discoveryServiceCallback.getExistingThing(thingUID) == null) {
            logger.debug("Detected a gateway with id: {}", id);
            thingDiscovered(
                    DiscoveryResultBuilder.create(thingUID).withThingType(THING_TYPE_GATEWAY).withProperties(properties)
                            .withRepresentationProperty("id").withLabel("Somfy Tahoma Gateway")
                            .withBridge(bridge.getThing().getUID()).build());
        }
    }

}
