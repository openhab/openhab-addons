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
package org.openhab.binding.mercedesme.internal.discovery;

import java.util.Map;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mercedesme.internal.Constants;
import org.openhab.binding.mercedesme.internal.handler.AccountHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link MercedesMeDiscoveryService} discovers things for the Adorne hub and Adorne devices.
 * Discovery is only supported if the hub is accessible via default host and port.
 *
 * @author Mark Theiding - Initial Contribution
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, configurationPid = "discovery.mercedesme")
public class MercedesMeDiscoveryService extends AbstractDiscoveryService {

    /**
     * Creates a AdorneDiscoveryService with disabled auto-discovery.
     */
    public MercedesMeDiscoveryService() {
        super(Constants.DISCOVERABLE_DEVICE_TYPE_UIDS, 0, false);
    }

    public void vehicleDiscovered(AccountHandler ac, String vin, Map<String, Object> properties) {
        String vehicleType = properties.get("vehicle").toString();
        ThingTypeUID ttuid = null;
        switch (vehicleType) {
            case Constants.BEV:
                ttuid = Constants.THING_TYPE_BEV;
                break;
            case Constants.COMBUSTION:
                ttuid = Constants.THING_TYPE_COMB;
                break;
            case Constants.HYBRID:
                ttuid = Constants.THING_TYPE_HYBRID;
                break;
            default:
                break;
        }
        if (ttuid != null) {
            thingDiscovered(DiscoveryResultBuilder
                    .create(new ThingUID(ttuid, ac.getThing().getUID(), UUID.randomUUID().toString()))
                    .withBridge(ac.getThing().getUID()).withProperties(properties)
                    .withLabel("Mercedes Benz " + ttuid.getId().toUpperCase()).build());
        }
    }

    @Override
    protected void startScan() {
        // not supported
    }
}
