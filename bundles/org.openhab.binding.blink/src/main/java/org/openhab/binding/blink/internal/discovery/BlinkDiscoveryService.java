/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.blink.internal.discovery;

import static org.openhab.binding.blink.internal.BlinkBindingConstants.*;

import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.blink.internal.dto.BlinkHomescreen;
import org.openhab.binding.blink.internal.handler.AccountHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BlinkDiscoveryService} performs auto-discovery of camera and network things for an account bridge.
 * Background Discovery is enabled by default.
 *
 * @author Matthias Oesterheld - Initial contribution
 */
@NonNullByDefault
public class BlinkDiscoveryService extends AbstractDiscoveryService implements ThingHandlerService {

    static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_CAMERA, THING_TYPE_NETWORK);
    private final Logger logger = LoggerFactory.getLogger(BlinkDiscoveryService.class);

    @Nullable
    AccountHandler accountHandler;
    @Nullable
    ScheduledFuture<?> discoveryJob;

    public BlinkDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, 15, true);
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Starting Blink background discovery");
        if (discoveryJob == null || discoveryJob.isCancelled()) {
            discoveryJob = scheduler.scheduleWithFixedDelay(this::discover, 0, 30, TimeUnit.MINUTES);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        if (discoveryJob != null) {
            discoveryJob.cancel(true);
            discoveryJob = null;
        }
    }

    @Override
    protected void startScan() {
        discover();
    }

    /**
     * actual discovery of things based on homescreen api results is done here.
     */
    void discover() {
        if (accountHandler == null) {
            logger.debug("Blink background discovery cancelled without accountHandler.");
            return;
        }
        if (accountHandler.getThing().getStatus() != ThingStatus.ONLINE) {
            logger.debug("Not starting discovery for things which ar not online.");
            return;
        }
        BlinkHomescreen homescreen = accountHandler.getDevices(false);
        if (homescreen == null || homescreen.cameras == null || homescreen.networks == null)
            return;
        ThingUID bridgeUID = accountHandler.getThing().getUID();
        logger.debug("Blink background discovery running for {}", bridgeUID.getAsString());
        homescreen.cameras.forEach(camera -> {
            ThingUID uid = new ThingUID(THING_TYPE_CAMERA, bridgeUID, Long.toString(camera.id));
            DiscoveryResultBuilder dr = DiscoveryResultBuilder.create(uid).withLabel(camera.name).withBridge(bridgeUID)
                    .withProperty(PROPERTY_CAMERA_ID, camera.id).withProperty(PROPERTY_NETWORK_ID, camera.network_id)
                    .withRepresentationProperty(PROPERTY_CAMERA_ID);
            thingDiscovered(dr.build());
        });
        homescreen.networks.forEach(network -> {
            ThingUID uid = new ThingUID(THING_TYPE_NETWORK, bridgeUID, Long.toString(network.id));
            DiscoveryResultBuilder dr = DiscoveryResultBuilder.create(uid).withLabel(network.name).withBridge(bridgeUID)
                    .withProperty(PROPERTY_NETWORK_ID, network.id).withRepresentationProperty(PROPERTY_NETWORK_ID);
            thingDiscovered(dr.build());
        });
    }

    @Override
    protected void thingDiscovered(DiscoveryResult discoveryResult) {
        super.thingDiscovered(discoveryResult);
    }

    @Override
    public void setThingHandler(@NonNullByDefault({}) ThingHandler handler) {
        if (handler instanceof AccountHandler) {
            accountHandler = (AccountHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return accountHandler;
    }

    @Override
    public void activate() {
        super.activate(null);
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }
}
