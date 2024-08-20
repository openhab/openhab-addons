/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.flume.internal.discovery;

import static org.openhab.binding.flume.internal.FlumeBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.flume.internal.handler.FlumeBridgeHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * The {@link FlumeDiscoveryService} implements discovers service for bridge
 *
 * @author Jeff James - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = FlumeDiscoveryService.class, configurationPid = "discovery.flume")
@NonNullByDefault
public class FlumeDiscoveryService extends AbstractThingHandlerDiscoveryService<FlumeBridgeHandler>
        implements ThingHandlerService {
    private static final Set<ThingTypeUID> DISCOVERABLE_THING_TYPES_UIDS = Set.of(THING_TYPE_METER);

    public FlumeDiscoveryService() {
        super(FlumeBridgeHandler.class, DISCOVERABLE_THING_TYPES_UIDS, 0, false);
    }

    @Override
    public void initialize() {
        thingHandler.registerDiscoveryListener(this);
        super.initialize();
    }

    @Override
    public void dispose() {
        super.dispose();
        thingHandler.unregisterDiscoveryListener();
    }

    @Override
    protected synchronized void startScan() {
        thingHandler.refreshDevices(true);
    }

    public void notifyDiscoveryDevice(String id) {
        ThingUID bridgeUID = thingHandler.getThing().getUID();

        ThingUID uid = new ThingUID(THING_TYPE_METER, bridgeUID, id);

        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withBridge(bridgeUID).withProperty(PROPERTY_ID, id)
                .withRepresentationProperty(PROPERTY_ID).withLabel("Flume Meter Device").build();
        thingDiscovered(result);
    }
}
