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

import java.util.HashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.roborock.internal.RoborockAccountHandler;
import org.openhab.binding.roborock.internal.api.Home;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The RoborockVacuumDiscoveryService is responsible for auto detecting a vacuum
 * cleaner in the Roborock ecosystem.
 *
 * @author Paul Smedley - Initial contribution
 */

@Component(scope = ServiceScope.PROTOTYPE, service = RoborockVacuumDiscoveryService.class)
@NonNullByDefault
public class RoborockVacuumDiscoveryService extends AbstractThingHandlerDiscoveryService<RoborockAccountHandler> {

    private Logger logger = LoggerFactory.getLogger(RoborockVacuumDiscoveryService.class);
    private @NonNullByDefault({}) ThingUID bridgeUid;

    private final Gson gson = new Gson();

    public RoborockVacuumDiscoveryService() {
        super(RoborockAccountHandler.class, SUPPORTED_THING_TYPES_UIDS, 5, false);
    }

    @Nullable
    protected Home getHomeDetail() {
        return thingHandler.getHomeDetail();
    }

    @Override
    public void initialize() {
        bridgeUid = thingHandler.getThing().getUID();
        super.initialize();
    }

    private void discover() {
        // Home homeDetail = getHomeDetail();
        HashMap<String, Object> properties = new HashMap<>();
        /*
         * JsonArray jsonArrayVacuumList = JsonParser.parseString(responseVehicleList).getAsJsonArray();
         * VehicleList vehicleList = new VehicleList();
         * for (int i = 0; i < jsonArrayVehicleList.size(); i++) {
         * vehicleList = gson.fromJson(jsonArrayVehicleList.get(i), VehicleList.class);
         * if (vehicleList == null) {
         * return;
         * }
         * properties.put("publicID", vehicleList.publicId);
         * ThingUID uid = new ThingUID(TESLASCOPE_VEHICLE, bridgeUid, vehicleList.publicId);
         * thingDiscovered(DiscoveryResultBuilder.create(uid).withBridge(bridgeUid).withProperties(properties)
         * .withRepresentationProperty("publicID").withLabel("Teslascope - " + vehicleList.name).build());
         * }
         */
    }

    @Override
    protected void startScan() {
        logger.debug("Starting device discovery");
        discover();
    }
}
