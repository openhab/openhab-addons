/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc2.internal.discovery;

import static org.openhab.binding.ihc2.Ihc2BindingConstants.*;
import static org.openhab.binding.ihc2.internal.ws.Ihc2Client.ConnectionState.CONNECTED;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.ihc2.internal.ws.Ihc2Client;
import org.openhab.binding.ihc2.internal.ws.Ihc2Client.DiscoveryLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Ihc2DiscoveryService} add resource Ids from the IHC project file
 * to the Inbox
 *
 * @author Niels Peter Enemark - Initial contribution
 */

public class Ihc2DiscoveryService extends AbstractDiscoveryService {

    private final ThingUID ihcControllerUID = new ThingUID("ihc2", "ihccontroller", "1");
    private final Logger logger = LoggerFactory.getLogger(Ihc2DiscoveryService.class);

    private static final int DISCOVER_TIMEOUT_SECONDS = 0;

    private final Ihc2Client ihc2Client = Ihc2Client.getInstance();

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_IHCCONTROLLER);

    public Ihc2DiscoveryService() {
        super(SUPPORTED_THING_TYPES, DISCOVER_TIMEOUT_SECONDS, false);
        logger.debug("Ihc2DiscoveryService()");
    }

    /**
     * Called on component activation.
     */
    public void activate() {
        super.activate(null);
        logger.debug("activate Ihc2 discovery");

        if (ihc2Client.getConnectionState() == CONNECTED) {
            logger.debug("activate Ihc2 discovery CONNECTED");
        }
    }

    @Override
    public void deactivate() {
        logger.debug("deactivate Ihc2 discovery");
        super.deactivate();
    }

    @Override
    protected void startScan() {
        logger.debug("Starting Ihc2 discovery scan");

        if (ihc2Client.getConnectionState() == CONNECTED) {
            logger.debug("Starting Ihc2 discovery scan CONNECTED");
        }
        addDiscoveredThings();
        super.stopScan();
    }

    @Override
    public synchronized void stopScan() {
        super.stopScan();
        logger.debug("Stopping Ihc2 discovery scan");
    }

    private void addDiscoveredThings() {
        logger.debug("addDiscoveredThings()");
        if (ihc2Client.getConnectionState() != CONNECTED) {
            return;
        }

        DiscoveryLevel discoveryLevel = ihc2Client.getDiscoveryLevel();

        if (discoveryLevel == DiscoveryLevel.NOTHING) {
            return;
        }

        if (discoveryLevel == DiscoveryLevel.CLEAN) {
            List<ThingTypeUID> typesToRemove = new ArrayList<ThingTypeUID>();
            typesToRemove.add(THING_TYPE_NUMBER);
            typesToRemove.add(THING_TYPE_STRING);
            typesToRemove.add(THING_TYPE_DATETIME);
            typesToRemove.add(THING_TYPE_DIMMER);
            typesToRemove.add(THING_TYPE_SWITCH);
            typesToRemove.add(THING_TYPE_CONTACT);

            removeOlderResults(getTimestampOfLastScan(), typesToRemove, THE_IHC_CONTROLLER_UID);
            return;
        }

        // DiscoveryLevel.LINKED_RESOURCES && DiscoveryLevel.ALL
        List<Ihc2DiscoveredThing> discoveredList = ihc2Client.getDiscoveredThingsList();
        for (Ihc2DiscoveredThing discovered : discoveredList) {

            ThingUID thingUID = new ThingUID(discovered.getThingTypeUID(), discovered.getResourceId());

            String label = discovered.getGroup();
            if (!discovered.getLocation().isEmpty()) {
                label += "->";
                label += discovered.getLocation();
            }
            if (!discovered.getProduct().isEmpty()) {
                label += "->";
                label += discovered.getProduct();
            }
            if (!discovered.getProduct().isEmpty()) {
                label += "->";
                label += discovered.getName();
            }

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(THE_IHC_CONTROLLER_UID)
                    .withLabel(label).withThingType(discovered.getThingTypeUID())
                    .withRepresentationProperty(discovered.getProduct())
                    .withProperty("resourceId", discovered.getResourceId())
                    .withProperty("location", discovered.getGroup()).build(); // Cannot set thing location ??!
            thingDiscovered(discoveryResult);
        }
    }
}
