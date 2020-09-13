/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import javax.ws.rs.ProcessingException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.hdpowerview.internal.HDPowerViewBindingConstants;
import org.openhab.binding.hdpowerview.internal.HDPowerViewWebTargets;
import org.openhab.binding.hdpowerview.internal.HubMaintenanceException;
import org.openhab.binding.hdpowerview.internal.api.responses.Shades;
import org.openhab.binding.hdpowerview.internal.api.responses.Shades.ShadeData;
import org.openhab.binding.hdpowerview.internal.config.HDPowerViewShadeConfiguration;
import org.openhab.binding.hdpowerview.internal.handler.HDPowerViewHubHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonParseException;

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
                    throw new ProcessingException("Web targets not initialized");
                }
                Shades shades = webTargets.getShades();
                if (shades != null && shades.shadeData != null) {
                    ThingUID bridgeUID = hub.getThing().getUID();
                    List<ShadeData> shadesData = shades.shadeData;
                    if (shadesData != null) {
                        for (ShadeData shadeData : shadesData) {
                            if (shadeData.id != 0) {
                                String id = Integer.toString(shadeData.id);
                                ThingUID thingUID = new ThingUID(HDPowerViewBindingConstants.THING_TYPE_SHADE,
                                        bridgeUID, id);
                                DiscoveryResult result = DiscoveryResultBuilder.create(thingUID)
                                        .withProperty(HDPowerViewShadeConfiguration.ID, id)
                                        .withRepresentationProperty(HDPowerViewShadeConfiguration.ID)
                                        .withLabel(shadeData.getName()).withBridge(bridgeUID).build();
                                logger.debug("Hub discovered shade '{}'", id);
                                thingDiscovered(result);
                            }
                        }
                    }
                }
            } catch (ProcessingException | JsonParseException e) {
                logger.warn("Unexpected error: {}", e.getMessage());
            } catch (HubMaintenanceException e) {
                // exceptions are logged in HDPowerViewWebTargets
            }
            stopScan();
        };
    }
}
