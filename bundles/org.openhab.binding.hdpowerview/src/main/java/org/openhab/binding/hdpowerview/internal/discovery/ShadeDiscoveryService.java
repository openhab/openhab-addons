/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hdpowerview.internal.GatewayWebTargets;
import org.openhab.binding.hdpowerview.internal.HDPowerViewBindingConstants;
import org.openhab.binding.hdpowerview.internal.config.HDPowerViewShadeConfiguration;
import org.openhab.binding.hdpowerview.internal.dto.gen3.Shade;
import org.openhab.binding.hdpowerview.internal.exceptions.HubProcessingException;
import org.openhab.binding.hdpowerview.internal.handler.GatewayBridgeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovers shades in an HD PowerView Generation 3 Gateway.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class ShadeDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(ShadeDiscoveryService.class);
    private final GatewayBridgeHandler hub;
    private final Runnable scanner;
    private @Nullable ScheduledFuture<?> backgroundFuture;

    public ShadeDiscoveryService(GatewayBridgeHandler hub) {
        super(Collections.singleton(HDPowerViewBindingConstants.THING_TYPE_SHADE3), 60, true);
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
                GatewayWebTargets webTargets = hub.getWebTargets();
                discoverShades(webTargets);
            } catch (HubProcessingException e) {
                logger.warn("Unexpected exception:{}, message:{}", e.getClass().getSimpleName(), e.getMessage());
            } catch (IllegalStateException e) {
                // ignore
            }
            stopScan();
        };
    }

    private void discoverShades(GatewayWebTargets webTargets) throws HubProcessingException {
        ThingUID bridgeUid = hub.getThing().getUID();
        for (Shade shade : webTargets.getShades()) {
            if (shade.getId() == 0) {
                continue;
            }

            String id = Integer.toString(shade.getId());
            ThingUID thingUID = new ThingUID(HDPowerViewBindingConstants.THING_TYPE_SHADE3, bridgeUid, id);

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
