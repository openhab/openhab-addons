/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.vitotronic.internal.discovery;

import static org.openhab.binding.vitotronic.internal.VitotronicBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.vitotronic.internal.VitotronicBindingConstants;
import org.openhab.binding.vitotronic.internal.handler.VitotronicBridgeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VitotronicBridgeDiscovery} class handles the discovery of things.
 * with broadcasting and put it to inbox, if found.
 *
 *
 * @author Stefan Andres - Initial contribution
 */
@NonNullByDefault
public class VitotronicDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(VitotronicDiscoveryService.class);

    private VitotronicBridgeHandler vitotronicBridgeHandler;

    public VitotronicDiscoveryService(VitotronicBridgeHandler vitotronicBridgeHandler) throws IllegalArgumentException {
        super(VitotronicBindingConstants.SUPPORTED_THING_TYPES_UIDS, 10, false);
        this.vitotronicBridgeHandler = vitotronicBridgeHandler;
    }

    private void addThing(ThingUID bridgeUID, String thingType, String thingID) {
        logger.trace("Adding new Vitotronic thing: {}", thingID);
        ThingUID thingUID = null;
        switch (thingType) {
            case THING_ID_HEATING:
                thingUID = new ThingUID(THING_TYPE_UID_HEATING, bridgeUID, thingID);
                break;
            case THING_ID_GASBURNER:
                thingUID = new ThingUID(THING_TYPE_UID_GASBURNER, bridgeUID, thingID);
                break;
            case THING_ID_PELLETBURNER:
                thingUID = new ThingUID(THING_TYPE_UID_PELLETBURNER, bridgeUID, thingID);
                break;
            case THING_ID_STORAGETANK:
                thingUID = new ThingUID(THING_TYPE_UID_STORAGETANK, bridgeUID, thingID);
                break;
            case THING_ID_CIRCUIT:
                thingUID = new ThingUID(THING_TYPE_UID_CIRCUIT, bridgeUID, thingID);
                break;
            case THING_ID_SOLAR:
                thingUID = new ThingUID(THING_TYPE_UID_SOLAR, bridgeUID, thingID);
                break;
        }

        if (thingUID != null) {
            logger.trace("Adding new Discovery thingType: {} bridgeType: {}", thingUID, bridgeUID);
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
