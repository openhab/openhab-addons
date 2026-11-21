/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.tibberpulselocal.internal.discovery;

import static org.openhab.binding.tibberpulselocal.internal.TibberPulseLocalBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tibberpulselocal.internal.handler.BridgeHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * Offers a {@code meter} thing for each Pulse device id that is seen on the stream of a bridge. The device id is only
 * known from the stream, so this is how a user learns it. The service never opens a connection: it listens to the
 * stream the {@link BridgeHandler} already owns.
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
@Component(scope = ServiceScope.PROTOTYPE, service = MeterDiscoveryService.class)
public class MeterDiscoveryService extends AbstractThingHandlerDiscoveryService<BridgeHandler> {

    private static final int SCAN_TIMEOUT_SECONDS = 5;

    private final BridgeHandler.DeviceSeenListener listener = this::discover;

    public MeterDiscoveryService() {
        super(BridgeHandler.class, Set.of(THING_TYPE_METER), SCAN_TIMEOUT_SECONDS, true);
    }

    /** A scan has no stream of its own to wait for: it offers the devices the stream has shown so far. */
    @Override
    protected void startScan() {
        thingHandler.getSeenDeviceIds().forEach(this::discover);
    }

    @Override
    protected void startBackgroundDiscovery() {
        thingHandler.addDeviceSeenListener(listener);
    }

    @Override
    protected void stopBackgroundDiscovery() {
        thingHandler.removeDeviceSeenListener(listener);
    }

    private void discover(String deviceId) {
        ThingUID bridgeUid = thingHandler.getThing().getUID();
        DiscoveryResult result = DiscoveryResultBuilder.create(new ThingUID(THING_TYPE_METER, bridgeUid, deviceId))
                .withBridge(bridgeUid).withProperty(CONFIG_DEVICE_ID, deviceId)
                .withRepresentationProperty(CONFIG_DEVICE_ID).withLabel("Tibber Pulse " + deviceId).build();
        thingDiscovered(result);
    }
}
