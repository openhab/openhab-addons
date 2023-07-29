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
package org.openhab.binding.generacmobilelink.internal.discovery;

import static org.openhab.binding.generacmobilelink.internal.GeneracMobileLinkBindingConstants.THING_TYPE_GENERATOR;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.generacmobilelink.internal.GeneracMobileLinkBindingConstants;
import org.openhab.binding.generacmobilelink.internal.dto.GeneratorStatusDTO;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;

/**
 * The {@link GeneracMobileLinkDiscoveryService} is responsible for discovering generator things
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class GeneracMobileLinkDiscoveryService extends AbstractDiscoveryService {
    private static final Set<ThingTypeUID> SUPPORTED_DISCOVERY_THING_TYPES_UIDS = Set.of(THING_TYPE_GENERATOR);

    public GeneracMobileLinkDiscoveryService() {
        super(SUPPORTED_DISCOVERY_THING_TYPES_UIDS, 0);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_DISCOVERY_THING_TYPES_UIDS;
    }

    @Override
    public void startScan() {
    }

    @Override
    public boolean isBackgroundDiscoveryEnabled() {
        return false;
    }

    public void generatorDiscovered(GeneratorStatusDTO generator, ThingUID bridgeUID) {
        DiscoveryResult result = DiscoveryResultBuilder
                .create(new ThingUID(GeneracMobileLinkBindingConstants.THING_TYPE_GENERATOR, bridgeUID,
                        String.valueOf(generator.gensetID)))
                .withLabel("MobileLink Generator " + generator.generatorName)
                .withProperty("generatorId", String.valueOf(generator.gensetID))
                .withRepresentationProperty("generatorId").withBridge(bridgeUID).build();
        thingDiscovered(result);
    }
}
