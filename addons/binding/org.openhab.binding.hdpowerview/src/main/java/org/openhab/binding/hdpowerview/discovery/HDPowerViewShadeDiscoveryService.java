/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hdpowerview.discovery;

import java.io.IOException;
import java.util.Collections;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.hdpowerview.HDPowerViewBindingConstants;
import org.openhab.binding.hdpowerview.config.HDPowerViewShadeConfiguration;
import org.openhab.binding.hdpowerview.handler.HDPowerViewHubHandler;
import org.openhab.binding.hdpowerview.internal.HDPowerViewWebTargets;
import org.openhab.binding.hdpowerview.internal.api.responses.Shades;
import org.openhab.binding.hdpowerview.internal.api.responses.Shades.Shade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovers an HD Power View Shade from an existing hub
 *
 * @author Andy Lintner
 */
public class HDPowerViewShadeDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(HDPowerViewShadeDiscoveryService.class);
    private final HDPowerViewHubHandler hub;

    public HDPowerViewShadeDiscoveryService(HDPowerViewHubHandler hub) {
        super(Collections.singleton(HDPowerViewBindingConstants.THING_TYPE_SHADE), 600, true);
        this.hub = hub;
    }

    @Override
    protected void startScan() {
        HDPowerViewWebTargets targets = hub.getWebTargets();
        Shades shades;
        try {
            shades = targets.getShades();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            stopScan();
            return;
        }
        if (shades != null) {
            for (Shade shade : shades.shadeData) {
                ThingUID thingUID = new ThingUID(HDPowerViewBindingConstants.THING_TYPE_SHADE,
                        Integer.toString(shade.id));
                DiscoveryResult result = DiscoveryResultBuilder.create(thingUID)
                        .withProperty(HDPowerViewShadeConfiguration.ID, shade.id).withLabel(shade.getName())
                        .withBridge(hub.getThing().getUID()).build();
                thingDiscovered(result);
            }
        }
        stopScan();
    }

    @Override
    protected void startBackgroundDiscovery() {
        startScan();
    }

}
