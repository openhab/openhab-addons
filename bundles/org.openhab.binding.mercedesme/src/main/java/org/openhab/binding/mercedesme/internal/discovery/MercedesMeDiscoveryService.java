/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
 * {@link MercedesMeDiscoveryService} will be notified from Bridge {@link AccountHandler} regarding
 * associated vehicles and provides DiscoveryResults
 *
 * @author Bernd Weymann - Initial Contribution
 * @author Bernd Weymann - Add vin as representation property
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, configurationPid = "discovery.mercedesme")
public class MercedesMeDiscoveryService extends AbstractDiscoveryService {

    public MercedesMeDiscoveryService() {
        super(Constants.DISCOVERABLE_DEVICE_TYPE_UIDS, 0, false);
    }

    public void vehicleDiscovered(AccountHandler ac, String vin, Map<String, Object> properties) {
        Object vehicleTypeObj = properties.get("vehicle");
        String vehicleType = ((vehicleTypeObj == null) ? "unknown" : vehicleTypeObj.toString());
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
            properties.put("vin", vin);
            thingDiscovered(DiscoveryResultBuilder.create(new ThingUID(ttuid, ac.getThing().getUID(), vin))
                    .withBridge(ac.getThing().getUID()).withProperties(properties).withRepresentationProperty("vin")
                    .withLabel("Mercedes Benz " + ttuid.getId().toUpperCase()).build());
        }
    }

    public void vehicleRemove(AccountHandler ac, String vin, String vehicleType) {
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
            thingRemoved(new ThingUID(ttuid, ac.getThing().getUID(), vin));
        }
    }

    @Override
    protected void startScan() {
        // not supported
    }
}
