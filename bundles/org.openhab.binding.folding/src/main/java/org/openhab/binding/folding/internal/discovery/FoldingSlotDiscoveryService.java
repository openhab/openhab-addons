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
package org.openhab.binding.folding.internal.discovery;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.folding.internal.FoldingBindingConstants;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;

/**
 * Discovery service implementation.
 *
 * The Client handler has to be configured manually, but once connected,
 * it will publish discovered slots to this service. This service converts
 * the internal representation to discovery results.
 *
 * @author Marius Bjoernstad - Initial contribution
 */
@Component(service = DiscoveryService.class, configurationPid = "discovery.folding")
public class FoldingSlotDiscoveryService extends AbstractDiscoveryService {

    public FoldingSlotDiscoveryService() {
        super(Collections.singleton(FoldingBindingConstants.THING_TYPE_SLOT), 10, true);
        FoldingDiscoveryProxy.getInstance().setService(this);
    }

    @Override
    protected void startScan() {
    }

    protected String getLabel(String host, String description) {
        if (description == null) {
            description = "slot";
        }
        int endOfLabel = description.indexOf(' ');
        if (endOfLabel > 0) {
            description = description.substring(0, endOfLabel);
        }
        endOfLabel = description.indexOf(':');
        if (endOfLabel > 0) {
            description = description.substring(0, endOfLabel);
        }
        return description + " @ " + host;
    }

    public void newSlot(ThingUID bridgeUID, String host, String id, String description) {
        if (isBackgroundDiscoveryEnabled() && id != null) {
            Map<String, Object> properties = new HashMap<>(1);
            properties.put(FoldingBindingConstants.PARAM_SLOT_ID, id);
            ThingUID thingUID = new ThingUID(FoldingBindingConstants.THING_TYPE_SLOT, bridgeUID, id);
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withBridge(bridgeUID).withLabel(getLabel(host, description)).build();
            thingDiscovered(discoveryResult);
        }
    }
}
