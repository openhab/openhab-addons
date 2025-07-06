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
package org.openhab.binding.teslascope.internal.discovery;

import static org.openhab.binding.teslascope.internal.TeslascopeBindingConstants.*;

import java.util.HashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.teslascope.internal.TeslascopeAccountHandler;
import org.openhab.binding.teslascope.internal.api.VehicleList;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

/**
 * The TeslascopeVehicleDiscoveryService is responsible for auto detecting a Tesla
 * vehicle on the Teslascope service.
 *
 * @author paul@smedley.id.au - Initial contribution
 */

@Component(scope = ServiceScope.PROTOTYPE, service = TeslascopeVehicleDiscoveryService.class)
@NonNullByDefault
public class TeslascopeVehicleDiscoveryService extends AbstractThingHandlerDiscoveryService<TeslascopeAccountHandler> {

    private Logger logger = LoggerFactory.getLogger(TeslascopeVehicleDiscoveryService.class);

    private final Gson gson = new Gson();

    public TeslascopeVehicleDiscoveryService() {
        super(TeslascopeAccountHandler.class, SUPPORTED_THING_TYPES_UIDS, 5, false);
    }

    protected String getVehicleList() {
        return thingHandler.getVehicleList();
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    private void discover() {
        ThingUID bridgeUID = thingHandler.getThing().getUID();
        String responseVehicleList = getVehicleList();
        HashMap<String, Object> properties = new HashMap<>();
        JsonArray jsonArrayVehicleList = JsonParser.parseString(responseVehicleList).getAsJsonArray();
        VehicleList vehicleList = new VehicleList();
        for (int i = 0; i < jsonArrayVehicleList.size(); i++) {
            vehicleList = gson.fromJson(jsonArrayVehicleList.get(i), VehicleList.class);
            if (vehicleList == null) {
                return;
            }
            properties.put(CONFIG_PUBLICID, vehicleList.publicId);
            ThingUID uid = new ThingUID(TESLASCOPE_VEHICLE, bridgeUID, vehicleList.publicId);
            thingDiscovered(DiscoveryResultBuilder.create(uid).withBridge(bridgeUID).withProperties(properties)
                    .withRepresentationProperty(CONFIG_PUBLICID).withLabel("Teslascope - " + vehicleList.name).build());
        }
    }

    @Override
    protected void startScan() {
        logger.debug("Starting device discovery");
        discover();
    }
}
