/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.verisure.internal.discovery;

import static org.openhab.binding.verisure.VerisureBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.verisure.handler.VerisureBridgeHandler;
import org.openhab.binding.verisure.handler.VerisureObjectHandler;
import org.openhab.binding.verisure.internal.VerisureAlarmJSON;
import org.openhab.binding.verisure.internal.VerisureDoorWindowsJSON;
import org.openhab.binding.verisure.internal.VerisureObjectJSON;
import org.openhab.binding.verisure.internal.VerisureUserTrackingJSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * The discovery service, notified by a listener on the VerisureSession.
 * 
 * @author jarle hjortland
 *
 */
public class VerisureObjectDiscoveryService extends AbstractDiscoveryService {
    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets
            .union(VerisureBridgeHandler.SUPPORTED_THING_TYPES, VerisureObjectHandler.SUPPORTED_THING_TYPES);

    private Logger logger = LoggerFactory.getLogger(VerisureObjectDiscoveryService.class);

    private final static int SEARCH_TIME = 60;

    private VerisureBridgeHandler verisureBridgeHandler;

    public VerisureObjectDiscoveryService(VerisureBridgeHandler bridgeHandler) throws IllegalArgumentException {
        // super(SEARCH_TIME);
        super(SUPPORTED_THING_TYPES, SEARCH_TIME);

        this.verisureBridgeHandler = bridgeHandler;

    }

    @Override
    public void startScan() {
        removeOlderResults(getTimestampOfLastScan());
        logger.debug("VerisureObjectDiscoveryService:startScan");

        HashMap<String, VerisureObjectJSON> verisureObjects = verisureBridgeHandler.getSession().getVerisureObjects();

        for (Map.Entry<String, VerisureObjectJSON> entry : verisureObjects.entrySet()) {
            onObjectAddedInternal(entry.getValue());
        }
    }

    private void onObjectAddedInternal(VerisureObjectJSON value) {
        logger.debug("VerisureObjectDiscoveryService:OnObjectAddedInternal");
        ThingUID thingUID = getThingUID(value);
        if (thingUID != null) {
            ThingUID bridgeUID = verisureBridgeHandler.getThing().getUID();
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                    .withLabel(value.getDescription()).build();

            logger.debug("thinguid: {}, bridge {}, label {}", thingUID.toString(), bridgeUID, value.getId());
            thingDiscovered(discoveryResult);
        } else {
            logger.debug("discovered unsupported light of type '{}' with id {}", value.getId(), value.getId());
        }

    }

    public void activate() {
    }

    private ThingUID getThingUID(VerisureObjectJSON voj) {
        ThingUID bridgeUID = verisureBridgeHandler.getThing().getUID();

        ThingUID tuid = null;
        if (voj instanceof VerisureAlarmJSON) {
            tuid = new ThingUID(THING_TYPE_LOCK, bridgeUID, voj.getId().replaceAll("[^a-zA-Z0-9_]", "_"));
        } else if (voj instanceof VerisureUserTrackingJSON) {
            tuid = new ThingUID(THING_TYPE_USERPRESENCE, bridgeUID, voj.getId().replaceAll("[^a-zA-Z0-9_]", "_"));
        } else if (voj instanceof VerisureDoorWindowsJSON) {
            tuid = new ThingUID(THING_TYPE_DOORWINDOW, bridgeUID, voj.getId().replaceAll("[^a-zA-Z0-9_]", "_"));
        } else {
            tuid = new ThingUID(THING_TYPE_CLIMATESENSOR, bridgeUID, voj.getId().replaceAll("[^a-zA-Z0-9_]", "_"));
        }

        return tuid;
    }

}
