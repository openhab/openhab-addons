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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.ring.handler.AccountHandler;
import org.openhab.binding.ring.internal.RingDeviceRegistry;
import org.openhab.binding.ring.internal.data.Chime;
import org.openhab.binding.ring.internal.data.Doorbell;
import org.openhab.binding.ring.internal.data.RingDevice;
import org.openhab.binding.ring.internal.data.RingDeviceTO;
import org.openhab.binding.ring.internal.data.Stickupcam;
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
    public RingDiscoveryService() {
        super(AccountHandler.class, SUPPORTED_THING_TYPES_UIDS, 5, true);
    }

    @Override
    protected void startScan() {
        ThingHandler thingHandler = getThingHandler();
        if (thingHandler instanceof AccountHandler accountHandler) {
            RingDeviceRegistry registry = accountHandler.getDeviceRegistry();
            ThingUID bridgeUID = accountHandler.getThing().getUID();
            for (RingDevice device : registry.getRingDevices(RingDeviceRegistry.Status.ADDED)) {
                RingDeviceTO deviceTO = device.getDeviceStatus();
                ThingTypeUID thingTypeUID = switch (device) {
                    case Chime chime -> THING_TYPE_CHIME;
                    case Doorbell doorbell -> THING_TYPE_DOORBELL;
                    case Stickupcam stickupcam -> THING_TYPE_STICKUPCAM;
                    default -> THING_TYPE_OTHERDEVICE;
                };

                DiscoveryResult result = DiscoveryResultBuilder
                        .create(new ThingUID(thingTypeUID, bridgeUID, deviceTO.id)).withLabel(deviceTO.description)
                        .withBridge(bridgeUID).build();

                thingDiscovered(result);
                registry.setStatus(deviceTO.id, RingDeviceRegistry.Status.DISCOVERED);
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
        removeOlderResults(Instant.now().toEpochMilli());
    }
}
