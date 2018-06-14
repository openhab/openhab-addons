/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.qivicon.internal;

import static org.openhab.binding.qivicon.internal.QiviconBindingConstants.SUPPORTED_THING_TYPES_UIDS;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.osgi.service.component.annotations.Component;

@NonNullByDefault
@Component(service = DiscoveryService.class, immediate = true)
public class QiviconDiscovery extends AbstractDiscoveryService {
    public QiviconDiscovery() {
        super(SUPPORTED_THING_TYPES_UIDS, 10, false);
    }

    @Override
    protected void startScan() {
        // TODO Auto-generated method stub

        // DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID)
        // .withLabel(deviceName).withProperties(device.getBulbInfo())
        // .withProperty(PARAMETER_NETWORK_ADDRESS, device.getNetworkAddress()).build();

        // thingDiscovered(discoveryResult);
    }
}
