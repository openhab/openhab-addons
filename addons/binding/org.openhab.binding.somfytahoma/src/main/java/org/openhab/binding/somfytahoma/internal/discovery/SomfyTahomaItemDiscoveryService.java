/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
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
        return new HashSet<>(Arrays.asList(THING_TYPE_ROLLERSHUTTER, THING_TYPE_ACTIONGROUP, THING_TYPE_AWNING, THING_TYPE_ONOFF));
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

    public void rollershutterDiscovered(String label, String deviceURL, String oid) {
        Map<String, Object> properties = new HashMap<>(1);
        properties.put("url", deviceURL);

        ThingUID thingUID = new ThingUID(THING_TYPE_ROLLERSHUTTER, bridge.getThing().getUID(), oid);

        if (discoveryServiceCallback.getExistingThing(thingUID) == null) {
            logger.debug("Detected a rollershutter - label: {} oid: {}", label, oid);
            thingDiscovered(
                    DiscoveryResultBuilder.create(thingUID).withThingType(THING_TYPE_ROLLERSHUTTER).withProperties(properties)
                            .withRepresentationProperty("url").withLabel(label)
                            .withBridge(bridge.getThing().getUID()).build());
        }
    }

    public void awningDiscovered(String label, String deviceURL, String oid) {
        Map<String, Object> properties = new HashMap<>(1);
        properties.put("url", deviceURL);

        ThingUID thingUID = new ThingUID(THING_TYPE_AWNING, bridge.getThing().getUID(), oid);

        if (discoveryServiceCallback.getExistingThing(thingUID) == null) {
            logger.debug("Detected an awning - label: {} oid: {}", label, oid);
            thingDiscovered(
                    DiscoveryResultBuilder.create(thingUID).withThingType(THING_TYPE_AWNING).withProperties(properties)
                            .withRepresentationProperty("url").withLabel(label)
                            .withBridge(bridge.getThing().getUID()).build());
        }
    }

    public void onOffDiscovered(String label, String deviceURL, String oid) {
        Map<String, Object> properties = new HashMap<>(1);
        properties.put("url", deviceURL);

        ThingUID thingUID = new ThingUID(THING_TYPE_ONOFF, bridge.getThing().getUID(), oid);

        if (discoveryServiceCallback.getExistingThing(thingUID) == null) {
            logger.debug("Detected an on/off switch - label: {} oid: {}", label, oid);
            thingDiscovered(
                    DiscoveryResultBuilder.create(thingUID).withThingType(THING_TYPE_ONOFF).withProperties(properties)
                            .withRepresentationProperty("url").withLabel(label)
                            .withBridge(bridge.getThing().getUID()).build());
        }
    }

    public void actionGroupDiscovered(String label, String deviceURL, String oid) {
        Map<String, Object> properties = new HashMap<>(1);
        properties.put("url", deviceURL);

        ThingUID thingUID = new ThingUID(THING_TYPE_ACTIONGROUP, bridge.getThing().getUID(), oid);

        if (discoveryServiceCallback.getExistingThing(thingUID) == null) {
            logger.debug("Detected an action group - label: {} oid: {}", label, oid);
            thingDiscovered(
                    DiscoveryResultBuilder.create(thingUID).withThingType(THING_TYPE_ACTIONGROUP).withProperties(properties)
                            .withRepresentationProperty("url").withLabel(label)
                            .withBridge(bridge.getThing().getUID()).build());
        }
    }

}
