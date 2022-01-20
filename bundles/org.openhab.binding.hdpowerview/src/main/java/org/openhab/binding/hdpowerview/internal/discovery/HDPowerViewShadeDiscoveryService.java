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
package org.openhab.binding.hdpowerview.internal.discovery;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hdpowerview.internal.HDPowerViewBindingConstants;
import org.openhab.binding.hdpowerview.internal.HDPowerViewWebTargets;
import org.openhab.binding.hdpowerview.internal.api.responses.Shades;
import org.openhab.binding.hdpowerview.internal.api.responses.Shades.ShadeData;
import org.openhab.binding.hdpowerview.internal.config.HDPowerViewShadeConfiguration;
import org.openhab.binding.hdpowerview.internal.database.ShadeCapabilitiesDatabase;
import org.openhab.binding.hdpowerview.internal.database.ShadeCapabilitiesDatabase.Capabilities;
import org.openhab.binding.hdpowerview.internal.exceptions.HubException;
import org.openhab.binding.hdpowerview.internal.exceptions.HubMaintenanceException;
import org.openhab.binding.hdpowerview.internal.exceptions.HubProcessingException;
import org.openhab.binding.hdpowerview.internal.handler.HDPowerViewHubHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovers an HD PowerView Shade from an existing hub
 *
 * @author Andy Lintner - Initial contribution
 */
@NonNullByDefault
public class HDPowerViewShadeDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(HDPowerViewShadeDiscoveryService.class);
    private final HDPowerViewHubHandler hub;
    private final Runnable scanner;
    private @Nullable ScheduledFuture<?> backgroundFuture;
    private final ShadeCapabilitiesDatabase db = new ShadeCapabilitiesDatabase();

    public HDPowerViewShadeDiscoveryService(HDPowerViewHubHandler hub) {
        super(Collections.singleton(HDPowerViewBindingConstants.THING_TYPE_SHADE), 600, true);
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
            try {
                HDPowerViewWebTargets webTargets = hub.getWebTargets();
                if (webTargets == null) {
                    throw new HubProcessingException("Web targets not initialized");
                }
                Shades shades = webTargets.getShades();
                if (shades.shadeData != null) {
                    ThingUID bridgeUID = hub.getThing().getUID();
                    List<ShadeData> shadesData = shades.shadeData;
                    if (shadesData != null) {
                        for (ShadeData shadeData : shadesData) {
                            if (shadeData.id != 0) {
                                String id = Integer.toString(shadeData.id);
                                ThingUID thingUID = new ThingUID(HDPowerViewBindingConstants.THING_TYPE_SHADE,
                                        bridgeUID, id);
                                Integer caps = shadeData.capabilities;
                                Capabilities capabilities = db.getCapabilities((caps != null) ? caps.intValue() : -1);

                                DiscoveryResultBuilder builder = DiscoveryResultBuilder.create(thingUID)
                                        .withLabel(shadeData.getName()).withBridge(bridgeUID)
                                        .withProperty(HDPowerViewShadeConfiguration.ID, id)
                                        .withProperty(HDPowerViewBindingConstants.PROPERTY_SHADE_TYPE,
                                                db.getType(shadeData.type).toString())
                                        .withProperty(HDPowerViewBindingConstants.PROPERTY_SHADE_CAPABILITIES,
                                                capabilities.toString())
                                        .withRepresentationProperty(HDPowerViewShadeConfiguration.ID);

                                logger.debug("Hub discovered shade '{}'", id);
                                thingDiscovered(builder.build());
                            }
                        }
                    }
                }
            } catch (HubMaintenanceException e) {
                // exceptions are logged in HDPowerViewWebTargets
            } catch (HubException e) {
                logger.warn("Unexpected error: {}", e.getMessage());
            }
            stopScan();
        };
    }
}
