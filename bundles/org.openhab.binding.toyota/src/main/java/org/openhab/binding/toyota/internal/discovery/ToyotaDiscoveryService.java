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
package org.openhab.binding.toyota.internal.discovery;

import static org.openhab.binding.toyota.internal.ToyotaBindingConstants.*;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.toyota.internal.config.VehicleConfiguration;
import org.openhab.binding.toyota.internal.dto.Vehicle;
import org.openhab.binding.toyota.internal.handler.MyTBridgeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;

/**
 * The {@link ToyotaDiscoveryService} searches for available
 * cars discoverable through VocAPI
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ToyotaDiscoveryService extends AbstractDiscoveryService implements ThingHandlerService {
    private static final int SEARCH_TIME = 2;
    private @Nullable MyTBridgeHandler handler;

    public ToyotaDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, SEARCH_TIME);
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof MyTBridgeHandler myTHandler) {
            this.handler = myTHandler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @Override
    public void activate(@Nullable Map<String, Object> configProperties) {
        super.activate(configProperties);
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    protected void startScan() {
        if (handler instanceof MyTBridgeHandler myTHandler && myTHandler.getThing().getStatus() == ThingStatus.ONLINE) {
            ThingUID bridgeUID = myTHandler.getThing().getUID();
            List<Vehicle> vehicles = myTHandler.getVehicles();
            vehicles.forEach(vehicle -> {
                thingDiscovered(
                        DiscoveryResultBuilder.create(new ThingUID(VEHICLE_THING_TYPE, bridgeUID, vehicle.vehicleId))
                                .withLabel(vehicle.modelName + " " + vehicle.licensePlate).withBridge(bridgeUID)
                                .withProperty(VehicleConfiguration.VIN, vehicle.vin)
                                .withRepresentationProperty(VehicleConfiguration.VIN).build());
            });
        }
        stopScan();
    }
}
