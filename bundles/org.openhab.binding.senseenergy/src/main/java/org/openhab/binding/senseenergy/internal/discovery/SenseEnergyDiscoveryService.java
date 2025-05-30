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
package org.openhab.binding.senseenergy.internal.discovery;

import static org.openhab.binding.senseenergy.internal.SenseEnergyBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.senseenergy.internal.handler.SenseEnergyBridgeHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * The {@link SenseEnergyDiscoveryService }
 *
 * @author Jeff James - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = SenseEnergyDiscoveryService.class, configurationPid = "discovery.senseenergy")
@NonNullByDefault
public class SenseEnergyDiscoveryService extends AbstractThingHandlerDiscoveryService<SenseEnergyBridgeHandler> {
    private static final Set<ThingTypeUID> DISCOVERABLE_THING_TYPES_UIDS = Set.of(MONITOR_THING_TYPE);

    public SenseEnergyDiscoveryService() {
        super(SenseEnergyBridgeHandler.class, DISCOVERABLE_THING_TYPES_UIDS, 0, false);
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
    protected void startScan() {
        if (thingHandler.getThing().getStatus() == ThingStatus.ONLINE) {
            thingHandler.refreshMonitors();
        }
    }

    public void notifyDiscoveryMonitor(long id) {
        ThingUID bridgeUID = thingHandler.getThing().getUID();

        ThingUID uid = new ThingUID(MONITOR_THING_TYPE, bridgeUID, String.valueOf(id));

        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withBridge(bridgeUID)
                .withProperty(PARAM_MONITOR_ID, id).withRepresentationProperty(PARAM_MONITOR_ID)
                .withLabel("Sense Energy Monitor").build();
        thingDiscovered(result);
    }
}
