/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.resol.internal.discovery;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.resol.handler.ResolBridgeHandler;
import org.openhab.binding.resol.internal.ResolBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ResolDiscoveryService} class handles the discovery of things.
 * with broadcasting and put it to inbox, if found.
 *
 *
 * @author Raphael Mack - Initial contribution
 */
public class ResolDiscoveryService extends AbstractDiscoveryService {

    private Logger logger = LoggerFactory.getLogger(ResolDiscoveryService.class);

    private ResolBridgeHandler resolBridgeHandler;

    public ResolDiscoveryService(ResolBridgeHandler resolBridgeHandler) throws IllegalArgumentException {
        super(ResolBindingConstants.SUPPORTED_THING_TYPES_UIDS, 10, false);
        this.resolBridgeHandler = resolBridgeHandler;
    }

    private void addThing(ThingUID bridgeUID, String thingType, String type, String name) {
        logger.trace("Adding new Resol thing: {}", type);
        ThingUID thingUID = null;
        switch (thingType) {
            case ResolBindingConstants.THING_ID_DEVICE:
                thingUID = new ThingUID(ResolBindingConstants.THING_TYPE_UID_DEVICE, bridgeUID, type);
                break;
        }

        if (thingUID != null) {
            logger.trace("Adding new Discovery thingType: {} bridgeType: {}", thingUID.getThingTypeId(),
                    bridgeUID.getThingTypeId());

            Map<String, Object> properties = new HashMap<>(1);
            properties.put("type", type);

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                    .withProperties(properties).withLabel(name).build();
            logger.trace("call register: {} label: {}", discoveryResult.getBindingId(), discoveryResult.getLabel());
            thingDiscovered(discoveryResult);
        } else {
            logger.debug("Discovered Thing is unsupported: type '{}'", type);
        }

    }

    public void addResolThing(String thingType, String thingID, String name) {

        addThing(resolBridgeHandler.getThing().getUID(), thingType, thingID, name);

    }

    public void activate() {
        resolBridgeHandler.registerDiscoveryService(this);
    }

    @Override
    public void deactivate() {
        resolBridgeHandler.unregisterDiscoveryService();
    }

    @Override
    protected void startScan() {
        // Scan will be done by bridge
    }

}
