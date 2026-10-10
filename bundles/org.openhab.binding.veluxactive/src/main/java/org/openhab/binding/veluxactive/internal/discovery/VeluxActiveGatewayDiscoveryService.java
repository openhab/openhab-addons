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
package org.openhab.binding.veluxactive.internal.discovery;

import static org.openhab.binding.veluxactive.internal.VeluxActiveBindingConstants.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.veluxactive.internal.api.ModuleInfo;
import org.openhab.binding.veluxactive.internal.api.ModuleType;
import org.openhab.binding.veluxactive.internal.handler.VeluxActiveGatewayHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * Discovers modules belonging to one gateway.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
@Component(scope = ServiceScope.PROTOTYPE, service = VeluxActiveGatewayDiscoveryService.class)
public class VeluxActiveGatewayDiscoveryService
        extends AbstractThingHandlerDiscoveryService<VeluxActiveGatewayHandler> {
    private long generation;

    public VeluxActiveGatewayDiscoveryService() {
        super(VeluxActiveGatewayHandler.class,
                Set.of(THING_TYPE_INDOOR_SENSOR, THING_TYPE_DEPARTURE_SWITCH, THING_TYPE_OPENING), 60, false);
    }

    @Override
    public void startScan() {
        long scan;
        synchronized (this) {
            scan = ++generation;
        }
        thingHandler.discover(modules -> publish(scan, modules));
    }

    private synchronized void publish(long scan, List<ModuleInfo> modules) {
        if (scan != generation) {
            return;
        }
        ThingUID bridgeUID = thingHandler.getThing().getUID();
        for (ModuleInfo module : modules) {
            @Nullable
            ThingTypeUID thingType = switch (module.type()) {
                case GATEWAY -> null;
                case INDOOR_SENSOR -> THING_TYPE_INDOOR_SENSOR;
                case DEPARTURE_SWITCH -> THING_TYPE_DEPARTURE_SWITCH;
                case OPENING -> THING_TYPE_OPENING;
            };
            if (thingType == null) {
                continue;
            }
            String identity = module.homeId() + "/" + module.moduleId();
            String id = UUID.nameUUIDFromBytes(identity.getBytes(StandardCharsets.UTF_8)).toString();
            String roomName = module.roomName();
            String label = module.type() == ModuleType.OPENING && roomName != null && !roomName.isBlank()
                    ? module.name() + " (" + roomName + ")"
                    : module.name();
            thingDiscovered(DiscoveryResultBuilder.create(new ThingUID(thingType, bridgeUID, id)).withBridge(bridgeUID)
                    .withLabel(label).withProperty("moduleId", module.moduleId()).withRepresentationProperty("moduleId")
                    .build());
        }
    }

    @Override
    protected synchronized void stopScan() {
        generation++;
        super.stopScan();
    }

    @Override
    public synchronized void dispose() {
        generation++;
        super.dispose();
    }
}
