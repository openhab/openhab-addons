/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.neeo.internal.discovery;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.neeo.NeeoConstants;
import org.openhab.binding.neeo.internal.MdnsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link AbstractDiscoveryService} will simply join the NEEO binding's discovery process to force a
 * MDNS query to the brains. The query response is then handled by openHAB's MDNS client (which eventually goes to the
 * {@link NeeoBrainDiscovery}).
 *
 * @author Tim Roberts - Initial contribution
 */
public class NeeoMdnsDiscovery extends AbstractDiscoveryService {
    /** The logger */
    private final Logger logger = LoggerFactory.getLogger(NeeoMdnsDiscovery.class);

    /**
     * The thing types we discover. We simply create a dummy thingtype of the correct binding to allow this 'discovery'
     * process to join in the NEEO discovery process. The ID portion is never used.
     */
    private static final Set<ThingTypeUID> DISCOVERABLE_THING_TYPES_UIDS = Collections
            .singleton(new ThingTypeUID(NeeoConstants.BINDING_ID, "mdns"));

    public NeeoMdnsDiscovery() {
        super(DISCOVERABLE_THING_TYPES_UIDS, 10);
    }

    @Override
    protected void startScan() {
        try {
            MdnsHelper.sendQuery();
        } catch (IOException e) {
            logger.debug("Exception sending MDNS query: {}", e.getMessage(), e);
        }
    }
}
