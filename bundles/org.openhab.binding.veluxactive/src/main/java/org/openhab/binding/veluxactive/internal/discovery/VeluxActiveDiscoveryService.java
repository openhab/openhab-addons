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
import org.openhab.binding.veluxactive.internal.api.ModuleInfo;
import org.openhab.binding.veluxactive.internal.api.ModuleType;
import org.openhab.binding.veluxactive.internal.handler.VeluxActiveAccountHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * Discovers gateway Bridges belonging to an account.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
@Component(scope = ServiceScope.PROTOTYPE, service = VeluxActiveDiscoveryService.class)
public class VeluxActiveDiscoveryService extends AbstractThingHandlerDiscoveryService<VeluxActiveAccountHandler> {
    private long generation;

    public VeluxActiveDiscoveryService() {
        super(VeluxActiveAccountHandler.class, Set.of(THING_TYPE_GATEWAY), 60, false);
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
            String identity = module.homeId() + "/" + module.moduleId();
            String id = UUID.nameUUIDFromBytes(identity.getBytes(StandardCharsets.UTF_8)).toString();
            if (module.type() != ModuleType.GATEWAY) {
                continue;
            }
            thingDiscovered(DiscoveryResultBuilder.create(new ThingUID(THING_TYPE_GATEWAY, bridgeUID, id))
                    .withBridge(bridgeUID).withLabel(module.name()).withProperty("moduleId", module.moduleId())
                    .withRepresentationProperty("moduleId").build());
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
