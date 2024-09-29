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
package org.openhab.binding.coronastats.internal.discovery;

import static org.openhab.binding.coronastats.internal.CoronaStatsBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CoronaStatsDiscoveryService} create a default world thing.
 *
 * @author Johannes Ott - Initial contribution
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, configurationPid = "discovery.coronastats")
public class CoronaStatsDiscoveryService extends AbstractDiscoveryService {
    private static final ThingUID WORLD_THING_UID = new ThingUID(THING_TYPE_WORLD, STATS);
    private static final int DISCOVER_TIMEOUT_SECONDS = 2;

    private final Logger logger = LoggerFactory.getLogger(CoronaStatsDiscoveryService.class);

    public CoronaStatsDiscoveryService() throws IllegalArgumentException {
        super(SUPPORTED_THING_TYPES_UIDS, DISCOVER_TIMEOUT_SECONDS, true);
    }

    @Override
    protected void startScan() {
        logger.debug("Manual CoronaStats discovery scan.");
        addWorld();
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Background CoronaStats discovery scan.");
        addWorld();
    }

    private void addWorld() {
        // @formatter:off
        DiscoveryResult world = DiscoveryResultBuilder
            .create(WORLD_THING_UID)
            .withLabel(WORLD_LABEL)
            .build();
        // @formatter:on

        thingDiscovered(world);
    }
}
