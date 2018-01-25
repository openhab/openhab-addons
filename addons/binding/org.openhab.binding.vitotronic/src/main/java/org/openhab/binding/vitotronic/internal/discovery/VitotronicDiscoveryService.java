/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.vitotronic.internal.discovery;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.vitotronic.VitotronicBindingConstants;
import org.openhab.binding.vitotronic.handler.VitotronicBridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VitotronicBridgeDiscovery} class handles the discovery of things.
 * with broadcasting and put it to inbox, if found.
 *
 *
 * @author Stefan Andres - Initial contribution
 */
public class VitotronicDiscoveryService extends AbstractDiscoveryService {

    private Logger logger = LoggerFactory.getLogger(VitotronicDiscoveryService.class);

    private VitotronicBridgeHandler vitotronicBridgeHandler;

    public VitotronicDiscoveryService(VitotronicBridgeHandler vitotronicBridgeHandler) throws IllegalArgumentException {
        super(VitotronicBindingConstants.SUPPORTED_THING_TYPES_UIDS, 10, false);
        this.vitotronicBridgeHandler = vitotronicBridgeHandler;

    }

    private void addThing(ThingUID bridgeUID, String thingType, String thingID) {
        logger.trace("Adding new Vitotronic thing: {}", thingID);
        ThingUID thingUID = null;
        switch (thingType) {
            case VitotronicBindingConstants.THING_ID_HEATING:
                thingUID = new ThingUID(VitotronicBindingConstants.THING_TYPE_UID_HEATING, bridgeUID, thingID);
                break;
            case VitotronicBindingConstants.THING_ID_PELLETBURNER:
                thingUID = new ThingUID(VitotronicBindingConstants.THING_TYPE_UID_PELLETBURNER, bridgeUID, thingID);
                break;
            case VitotronicBindingConstants.THING_ID_STORAGETANK:
                thingUID = new ThingUID(VitotronicBindingConstants.THING_TYPE_UID_STORAGETANK, bridgeUID, thingID);
                break;
            case VitotronicBindingConstants.THING_ID_CIRCUIT:
                thingUID = new ThingUID(VitotronicBindingConstants.THING_TYPE_UID_CIRCUIT, bridgeUID, thingID);
                break;
            case VitotronicBindingConstants.THING_ID_SOLAR:
                thingUID = new ThingUID(VitotronicBindingConstants.THING_TYPE_UID_SOLAR, bridgeUID, thingID);
                break;
        }

        if (thingUID != null) {
            logger.trace("Adding new Discovery thingType: {} bridgeType: {}", thingUID.getThingTypeId(),
                    bridgeUID.getThingTypeId());

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                    .withLabel(thingID).build();
            logger.trace("call register: {} label: {}", discoveryResult.getBindingId(), discoveryResult.getLabel());
            thingDiscovered(discoveryResult);
        } else {
            logger.debug("Discovered Thing is unsupported: type '{}'", thingID);
        }

    }

    public void addVitotronicThing(String thingType, String thingID) {

        addThing(vitotronicBridgeHandler.getThing().getUID(), thingType, thingID);

    }

    public void activate() {
        vitotronicBridgeHandler.registerDiscoveryService(this);
    }

    @Override
    public void deactivate() {
        vitotronicBridgeHandler.unregisterDiscoveryService();
    }

    @Override
    protected void startScan() {
        // Scan will be done by bridge
    }

}
