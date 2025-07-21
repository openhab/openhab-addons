/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.ring.internal.discovery;

import static org.openhab.binding.ring.RingBindingConstants.*;

import java.time.Instant;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ring.internal.api.RingDeviceTO;
import org.openhab.binding.ring.internal.device.Chime;
import org.openhab.binding.ring.internal.device.Doorbell;
import org.openhab.binding.ring.internal.device.RingDevice;
import org.openhab.binding.ring.internal.device.Stickupcam;
import org.openhab.binding.ring.internal.handler.AccountHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * The RingDiscoveryService is responsible for auto detecting a Ring
 * device in the local network.
 *
 * @author Wim Vissers - Initial contribution
 * @author Chris Milbert - Stickupcam contribution
 * @author Ben Rosenblum - Updated for OH4 / New Maintainer
 * @author Jan N. Klug - Refactored to ThingHandlerService
 */

@Component(scope = ServiceScope.PROTOTYPE, service = RingDiscoveryService.class)
@NonNullByDefault
public class RingDiscoveryService extends AbstractThingHandlerDiscoveryService<AccountHandler> {
    private @Nullable ScheduledFuture<?> discoveryJob;

    public RingDiscoveryService() {
        super(AccountHandler.class, SUPPORTED_THING_TYPES_UIDS, 5);
    }

    @Override
    protected void startScan() {
        ThingHandler thingHandler = getThingHandler();
        if (thingHandler instanceof AccountHandler accountHandler) {
            ThingUID bridgeUID = accountHandler.getThing().getUID();
            for (RingDevice device : accountHandler.getAllDevices()) {
                RingDeviceTO deviceTO = device.getDeviceStatus();
                ThingTypeUID thingTypeUID = switch (device) {
                    case Chime chime -> THING_TYPE_CHIME;
                    case Doorbell doorbell -> THING_TYPE_DOORBELL;
                    case Stickupcam stickupcam -> THING_TYPE_STICKUPCAM;
                    default -> THING_TYPE_OTHERDEVICE;
                };

                Configuration configuration = new Configuration();
                configuration.put(THING_CONFIG_ID, deviceTO.id);
                configuration.put(THING_PROPERTY_KIND, deviceTO.kind);
                configuration.put(THING_PROPERTY_DESCRIPTION, deviceTO.description);
                configuration.put(THING_PROPERTY_DEVICE_ID, deviceTO.deviceId);
                configuration.put(THING_PROPERTY_OWNER_ID, deviceTO.owner.id);

                DiscoveryResult result = DiscoveryResultBuilder
                        .create(new ThingUID(thingTypeUID, bridgeUID, deviceTO.id))
                        .withProperties(configuration.getProperties()).withLabel(deviceTO.description)
                        .withRepresentationProperty(THING_CONFIG_ID).withBridge(bridgeUID).build();

                thingDiscovered(result);
            }
        }
    }

    @Override
    protected synchronized void stopScan() {
        removeOlderResults(getTimestampOfLastScan());
        super.stopScan();
    }

    @Override
    public void dispose() {
        super.dispose();
        removeOlderResults(Instant.now());
    }

    @Override
    public void startBackgroundDiscovery() {
        ScheduledFuture<?> job = this.discoveryJob;
        if (job == null || job.isCancelled()) {
            discoveryJob = scheduler.scheduleWithFixedDelay(this::startScan, 1, 5, TimeUnit.MINUTES);
        }
    }

    @Override
    public void stopBackgroundDiscovery() {
        ScheduledFuture<?> job = this.discoveryJob;
        if (job != null) {
            job.cancel(true);
            discoveryJob = null;
        }
    }
}
