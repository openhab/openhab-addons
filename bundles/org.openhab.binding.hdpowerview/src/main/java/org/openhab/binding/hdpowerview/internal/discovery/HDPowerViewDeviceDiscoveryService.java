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
import org.openhab.binding.hdpowerview.internal.api.responses.RepeaterData;
import org.openhab.binding.hdpowerview.internal.api.responses.Shades;
import org.openhab.binding.hdpowerview.internal.api.responses.Shades.ShadeData;
import org.openhab.binding.hdpowerview.internal.config.HDPowerViewRepeaterConfiguration;
import org.openhab.binding.hdpowerview.internal.config.HDPowerViewShadeConfiguration;
import org.openhab.binding.hdpowerview.internal.database.ShadeCapabilitiesDatabase;
import org.openhab.binding.hdpowerview.internal.database.ShadeCapabilitiesDatabase.Capabilities;
import org.openhab.binding.hdpowerview.internal.exceptions.HubException;
import org.openhab.binding.hdpowerview.internal.exceptions.HubInvalidResponseException;
import org.openhab.binding.hdpowerview.internal.exceptions.HubMaintenanceException;
import org.openhab.binding.hdpowerview.internal.exceptions.HubProcessingException;
import org.openhab.binding.hdpowerview.internal.handler.HDPowerViewHubHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovers HD PowerView Shades and Repeaters from an existing hub
 *
 * @author Andy Lintner - Initial contribution
 * @author Jacob Laursen - Add Repeater discovery
 */
@NonNullByDefault
public class HDPowerViewDeviceDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(HDPowerViewDeviceDiscoveryService.class);
    private final HDPowerViewHubHandler hub;
    private final Runnable scanner;
    private @Nullable ScheduledFuture<?> backgroundFuture;
    private final ShadeCapabilitiesDatabase db = new ShadeCapabilitiesDatabase();

    public HDPowerViewDeviceDiscoveryService(HDPowerViewHubHandler hub) {
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
                discoverShades(webTargets);
                discoverRepeaters(webTargets);
            } catch (HubMaintenanceException e) {
                // exceptions are logged in HDPowerViewWebTargets
            } catch (HubException e) {
                logger.warn("Unexpected error: {}", e.getMessage());
            }
            stopScan();
        };
    }

    private void discoverShades(HDPowerViewWebTargets webTargets)
            throws HubInvalidResponseException, HubProcessingException, HubMaintenanceException {
        Shades shades = webTargets.getShades();
        List<ShadeData> shadesData = shades.shadeData;
        if (shadesData == null) {
            return;
        }
        ThingUID bridgeUid = hub.getThing().getUID();
        for (ShadeData shadeData : shadesData) {
            if (shadeData.id == 0) {
                continue;
            }
            String id = Integer.toString(shadeData.id);
            ThingUID thingUID = new ThingUID(HDPowerViewBindingConstants.THING_TYPE_SHADE, bridgeUid, id);
            Capabilities capabilities = db.getCapabilities(shadeData.capabilities);

            DiscoveryResultBuilder builder = DiscoveryResultBuilder.create(thingUID).withLabel(shadeData.getName())
                    .withBridge(bridgeUid).withProperty(HDPowerViewShadeConfiguration.ID, id)
                    .withProperty(HDPowerViewBindingConstants.PROPERTY_SHADE_TYPE,
                            db.getType(shadeData.type).toString())
                    .withProperty(HDPowerViewBindingConstants.PROPERTY_SHADE_CAPABILITIES, capabilities.toString())
                    .withRepresentationProperty(HDPowerViewShadeConfiguration.ID);

            logger.debug("Hub discovered shade '{}'", id);
            thingDiscovered(builder.build());
        }
    }

    private void discoverRepeaters(HDPowerViewWebTargets webTargets)
            throws HubInvalidResponseException, HubProcessingException, HubMaintenanceException {
        List<RepeaterData> repeaters = webTargets.getRepeaters().repeaterData;
        if (repeaters == null) {
            return;
        }
        ThingUID bridgeUid = hub.getThing().getUID();
        for (RepeaterData repeaterData : repeaters) {
            if (repeaterData.id == 0) {
                continue;
            }
            String id = Integer.toString(repeaterData.id);
            ThingUID thingUid = new ThingUID(HDPowerViewBindingConstants.THING_TYPE_REPEATER, bridgeUid, id);

            DiscoveryResultBuilder builder = DiscoveryResultBuilder.create(thingUid).withLabel(repeaterData.getName())
                    .withBridge(bridgeUid).withProperty(HDPowerViewRepeaterConfiguration.ID, id)
                    .withRepresentationProperty(HDPowerViewRepeaterConfiguration.ID);

            logger.debug("Hub discovered repeater '{}'", id);
            thingDiscovered(builder.build());
        }
    }
}
