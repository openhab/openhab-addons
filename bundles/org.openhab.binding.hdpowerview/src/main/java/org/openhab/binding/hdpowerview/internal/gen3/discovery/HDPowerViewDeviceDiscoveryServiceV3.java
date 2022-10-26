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
package org.openhab.binding.hdpowerview.internal.gen3.discovery;

import java.util.Collections;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hdpowerview.internal.HDPowerViewBindingConstants;
import org.openhab.binding.hdpowerview.internal.config.HDPowerViewShadeConfiguration;
import org.openhab.binding.hdpowerview.internal.database.ShadeCapabilitiesDatabase;
import org.openhab.binding.hdpowerview.internal.exceptions.HubProcessingException;
import org.openhab.binding.hdpowerview.internal.gen3.dto.Shade3;
import org.openhab.binding.hdpowerview.internal.gen3.handler.HDPowerViewHubHandler3;
import org.openhab.binding.hdpowerview.internal.gen3.webtargets.HDPowerViewWebTargets3;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovers HD PowerView Shades and Repeaters from an existing hub
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class HDPowerViewDeviceDiscoveryServiceV3 extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(HDPowerViewDeviceDiscoveryServiceV3.class);
    private final HDPowerViewHubHandler3 hub;
    private final Runnable scanner;
    private @Nullable ScheduledFuture<?> backgroundFuture;
    private final ShadeCapabilitiesDatabase db = new ShadeCapabilitiesDatabase();

    public HDPowerViewDeviceDiscoveryServiceV3(HDPowerViewHubHandler3 hub) {
        super(Collections.singleton(HDPowerViewBindingConstants.THING_TYPE_SHADE_GEN3), 600, true);
        this.hub = hub;
        this.scanner = createScanner();
    }

    @Override
    protected void startScan() {
        scheduler.execute(scanner);
    }

    @Override
    protected void startBackgroundDiscovery() {
        ScheduledFuture<?> backgroundFuture = this.backgroundFuture;
        if (backgroundFuture != null && !backgroundFuture.isDone()) {
            backgroundFuture.cancel(true);
        }
        this.backgroundFuture = scheduler.scheduleWithFixedDelay(scanner, 0, 60, TimeUnit.SECONDS);
    }

    @Override
    protected void stopBackgroundDiscovery() {
        ScheduledFuture<?> backgroundFuture = this.backgroundFuture;
        if (backgroundFuture != null && !backgroundFuture.isDone()) {
            backgroundFuture.cancel(true);
            this.backgroundFuture = null;
        }
        super.stopBackgroundDiscovery();
    }

    private Runnable createScanner() {
        return () -> {
            HDPowerViewWebTargets3 webTargets = hub.getWebTargets();
            try {
                discoverShades(webTargets);
            } catch (HubProcessingException e) {
                logger.warn("Unexpected exception:{}, message:{}", e.getClass().getSimpleName(), e.getMessage());
            }
            stopScan();
        };
    }

    private void discoverShades(HDPowerViewWebTargets3 webTargets) throws HubProcessingException {
        ThingUID bridgeUid = hub.getThing().getUID();
        for (Shade3 shade : webTargets.getShades()) {
            if (shade.getId() == 0) {
                continue;
            }

            String id = Integer.toString(shade.getId());
            ThingUID thingUID = new ThingUID(HDPowerViewBindingConstants.THING_TYPE_SHADE_GEN3, bridgeUid, id);

            DiscoveryResultBuilder builder = DiscoveryResultBuilder.create(thingUID).withLabel(shade.getName())
                    .withBridge(bridgeUid).withProperty(HDPowerViewShadeConfiguration.ID, id)
                    .withRepresentationProperty(HDPowerViewShadeConfiguration.ID);
            String type = shade.getTypeString();
            if (type != null) {
                builder.withProperty(HDPowerViewBindingConstants.PROPERTY_SHADE_TYPE, type);
            }
            logger.debug("Hub discovered shade '{}'", id);
            thingDiscovered(builder.build());
        }
    }
}
