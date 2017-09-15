/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.icloud.discovery;

import static org.openhab.binding.icloud.iCloudBindingConstants.*;

import java.util.ArrayList;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.icloud.handler.iCloudBridgeHandler;
import org.openhab.binding.icloud.internal.json.iCloud.Content;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class iCloudDeviceDiscovery extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(iCloudBridgeHandler.class);
    private static final int timeout = 10;
    private ThingUID bridgeUID;

    public iCloudDeviceDiscovery(iCloudBridgeHandler iCloudBridgeHandler) {
        super(SUPPORTED_THING_TYPES_UIDS, timeout);
        this.bridgeUID = iCloudBridgeHandler.getThing().getUID();
    }

    public void discover(ArrayList<Content> content) {
        if (content != null) {
            for (int i = 0; i < content.toArray().length; i++) {
                String deviceTypeName = content.get(i).getDeviceDisplayName();
                String deviceOwnerName = content.get(i).getName();

                String thingLabel = deviceOwnerName + " (" + deviceTypeName + ")";

                logger.debug("iCloud device discovery for [" + content.get(i).getDeviceDisplayName() + "]");
                ThingUID uid = new ThingUID(THING_TYPE_ICLOUDDEVICE, bridgeUID, Integer.toString(i));
                DiscoveryResult result = DiscoveryResultBuilder.create(uid).withBridge(bridgeUID).withLabel(thingLabel)
                        .build();

                thingDiscovered(result);
            }
        }
    }

    @Override
    protected void startScan() {

    }
}
