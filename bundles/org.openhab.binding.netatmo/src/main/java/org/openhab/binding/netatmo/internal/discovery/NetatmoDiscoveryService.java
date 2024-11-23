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
package org.openhab.binding.netatmo.internal.discovery;

import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.api.data.ModuleType;
import org.openhab.binding.netatmo.internal.api.dto.NAModule;
import org.openhab.binding.netatmo.internal.config.NAThingConfiguration;
import org.openhab.binding.netatmo.internal.handler.ApiBridgeHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NetatmoDiscoveryService} searches for available Netatmo things
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@Component(scope = ServiceScope.PROTOTYPE, service = NetatmoDiscoveryService.class)
@NonNullByDefault
public class NetatmoDiscoveryService extends AbstractThingHandlerDiscoveryService<ApiBridgeHandler> {
    private static final int DISCOVER_TIMEOUT_SECONDS = 3;
    private final Logger logger = LoggerFactory.getLogger(NetatmoDiscoveryService.class);

    public NetatmoDiscoveryService() {
        super(ApiBridgeHandler.class, ModuleType.AS_SET.stream().filter(mt -> !mt.apiName.isBlank())
                .map(mt -> mt.thingTypeUID).collect(Collectors.toSet()), DISCOVER_TIMEOUT_SECONDS);
    }

    @Override
    public void startScan() {
        thingHandler.identifyAllModulesAndApplyAction(this::createThing);
    }

    private Optional<ThingUID> findThingUID(ModuleType moduleType, String thingId, ThingUID bridgeUID) {
        ThingTypeUID thingTypeUID = moduleType.thingTypeUID;
        return getSupportedThingTypes().stream().filter(supported -> supported.equals(thingTypeUID)).findFirst()
                .map(supported -> new ThingUID(supported, bridgeUID, thingId.replaceAll("[^a-zA-Z0-9_]", "")));
    }

    private Optional<ThingUID> createThing(NAModule module, ThingUID bridgeUID) {
        Optional<ThingUID> moduleUID = findThingUID(module.getType(), module.getId(), bridgeUID);
        if (moduleUID.isPresent()) {
            DiscoveryResultBuilder resultBuilder = DiscoveryResultBuilder.create(moduleUID.get())
                    .withProperty(NAThingConfiguration.ID, module.getId())
                    .withRepresentationProperty(NAThingConfiguration.ID)
                    .withLabel(module.getName() != null ? module.getName() : module.getId()).withBridge(bridgeUID);
            thingDiscovered(resultBuilder.build());
        } else {
            logger.info("Module '{}' is not handled by this version of the binding - it is ignored.", module.getName());
        }
        return moduleUID;
    }
}
