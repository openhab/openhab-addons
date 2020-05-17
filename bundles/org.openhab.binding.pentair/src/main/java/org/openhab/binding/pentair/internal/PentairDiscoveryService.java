/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.pentair.internal;

import static org.openhab.binding.pentair.internal.PentairBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.pentair.internal.handler.PentairBaseBridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PentairrDiscoveryService} handles discovery of devices as they are identified by the bridge handler.
 * Requests from the framework to startScan() are ignored, since no active scanning is possible. (Leveraged from
 * AlarmDecoder)
 *
 * @author Jeff James - Initial contribution
 */
@NonNullByDefault
public class PentairDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(PentairDiscoveryService.class);

    private PentairBaseBridgeHandler bridgeHandler;
    // private final Set<String> discoveredZoneSet = new HashSet<>();
    // private final Set<Integer> discoveredRFZoneSet = new HashSet<>();

    public PentairDiscoveryService(PentairBaseBridgeHandler bridgeHandler) throws IllegalArgumentException {
        super(DISCOVERABLE_DEVICE_TYPE_UIDS, 0, false);
        this.bridgeHandler = bridgeHandler;
    }

    @Override
    protected void startScan() {
        // Ignore start scan requests
    }

    public void notifyDiscoveredController(int id) {
        ThingUID bridgeUID = bridgeHandler.getThing().getUID();
        ThingUID uid = new ThingUID(CONTROLLER_THING_TYPE, bridgeUID, "controller");

        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withBridge(bridgeUID).withProperty(PARAMETER_ID, id)
                .build();
        thingDiscovered(result);
        logger.debug("Discovered Controller {}", uid);
    }

    public void notifyDiscoverdIntelliflo(int id) {
        int pumpid = (id & 0x04) + 1;
        ThingUID bridgeUID = bridgeHandler.getThing().getUID();
        ThingUID uid = new ThingUID(INTELLIFLO_THING_TYPE, bridgeUID, "pump" + pumpid);

        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withBridge(bridgeUID).withProperty(PARAMETER_ID, id)
                .build();
        thingDiscovered(result);
        logger.debug("Discovered Pump {}", uid);
    }

    public void notifyDiscoveredIntellichlor(int id) {
        ThingUID bridgeUID = bridgeHandler.getThing().getUID();
        ThingUID uid = new ThingUID(INTELLICHLOR_THING_TYPE, bridgeUID, "intellichlor");

        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withBridge(bridgeUID).withProperty(PARAMETER_ID, id)
                .build();
        thingDiscovered(result);
        logger.debug("Discovered Intellichlor {}", uid);
    }
}
