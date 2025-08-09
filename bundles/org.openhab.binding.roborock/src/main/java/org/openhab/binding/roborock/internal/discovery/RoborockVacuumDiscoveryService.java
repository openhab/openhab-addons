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
package org.openhab.binding.roborock.internal.discovery;

import static org.openhab.binding.roborock.internal.RoborockBindingConstants.*;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.roborock.internal.RoborockAccountHandler;
import org.openhab.binding.roborock.internal.api.Home;
import org.openhab.binding.roborock.internal.api.HomeData;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The RoborockVacuumDiscoveryService is responsible for auto detecting a vacuum
 * cleaner in the Roborock ecosystem.
 *
 * @author Paul Smedley - Initial contribution
 */

@Component(scope = ServiceScope.PROTOTYPE, service = RoborockVacuumDiscoveryService.class)
@NonNullByDefault
public class RoborockVacuumDiscoveryService extends AbstractThingHandlerDiscoveryService<RoborockAccountHandler> {

    private final Logger logger = LoggerFactory.getLogger(RoborockVacuumDiscoveryService.class);

    public RoborockVacuumDiscoveryService() {
        super(RoborockAccountHandler.class, SUPPORTED_THING_TYPES_UIDS, 5, false);
    }

    @Nullable
    protected Home getHomeDetail() {
        return thingHandler.getHomeDetail();
    }

    @Nullable
    protected HomeData getHomeData(String rrHomeID) {
        return thingHandler.getHomeData(rrHomeID);
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    private void discover() {
        ThingUID bridgeUID = thingHandler.getThing().getUID();
        Home home;
        home = getHomeDetail();
        if (home != null) {
            HomeData homeData;
            homeData = getHomeData(Integer.toString(home.data.rrHomeId));

            if (homeData != null) {
                for (int i = 0; i < homeData.result.devices.length; i++) {
                    Map<String, Object> properties = Map.of("sn",
                            (homeData.result.devices[i].sn != null) ? homeData.result.devices[i].sn : "N/A");
                    ThingUID uid = new ThingUID(ROBOROCK_VACUUM, bridgeUID, homeData.result.devices[i].duid);
                    thingDiscovered(DiscoveryResultBuilder.create(uid).withBridge(bridgeUID).withProperties(properties)
                            .withLabel(homeData.result.devices[i].name).build());
                }
            }
        }
    }

    @Override
    protected void startScan() {
        logger.debug("Starting device discovery");
        discover();
    }
}
