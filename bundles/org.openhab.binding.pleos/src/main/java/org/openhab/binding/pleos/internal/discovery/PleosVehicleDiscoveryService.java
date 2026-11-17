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
package org.openhab.binding.pleos.internal.discovery;

import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.pleos.internal.PleosBindingConstants;
import org.openhab.binding.pleos.internal.api.PleosApiClient;
import org.openhab.binding.pleos.internal.api.PleosApiException;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.VehiclesResponse;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.VehiclesResponse.VehicleInfo;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.VehiclesResponse.VehiclesData;
import org.openhab.binding.pleos.internal.handler.PleosAccountHandler;
import org.openhab.binding.pleos.internal.model.VehicleType;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PleosVehicleDiscoveryService} discovers vehicles associated with a Pleos account.
 *
 * @author Stephan Sandmann - Initial contribution
 */
@NonNullByDefault
@Component(scope = ServiceScope.PROTOTYPE, service = ThingHandlerService.class)
public class PleosVehicleDiscoveryService extends AbstractThingHandlerDiscoveryService<PleosAccountHandler> {

    private final Logger logger = LoggerFactory.getLogger(PleosVehicleDiscoveryService.class);
    private static final int DISCOVERY_TIMEOUT_SECONDS = 30;

    public PleosVehicleDiscoveryService() {
        super(PleosAccountHandler.class, Set.of(PleosBindingConstants.THING_TYPE_VEHICLE), DISCOVERY_TIMEOUT_SECONDS,
                true);
    }

    @Override
    protected void startScan() {
        logger.debug("Initiate Pleo’s vehicle discovery via account: {}", thingHandler.getThing().getUID());

        PleosApiClient apiClient = thingHandler.getApiClient();
        if (apiClient == null) {
            logger.debug("Discovery aborted: API client in AccountHandler not yet ready.");
            return;
        }

        try {
            VehiclesResponse response = apiClient.getVehicles();
            VehiclesData data = response.data();
            List<VehicleInfo> vehicles = data != null ? data.vehicles() : null;

            if (vehicles == null || vehicles.isEmpty()) {
                logger.debug("No shared vehicles found in the Pleos account.");
                return;
            }

            ThingUID bridgeUID = thingHandler.getThing().getUID();

            for (VehicleInfo vehicle : vehicles) {
                String vin = vehicle.vin();
                if (vin == null || vin.isBlank()) {
                    continue;
                }

                VehicleType vehicleType = VehicleType.fromApiValue(vehicle.type());
                String label = buildVehicleLabel(vin, vehicle.sellname(), vehicle.nickname());
                ThingUID vehicleUID = new ThingUID(PleosBindingConstants.THING_TYPE_VEHICLE, bridgeUID, vin);

                DiscoveryResultBuilder resultBuilder = DiscoveryResultBuilder.create(vehicleUID).withBridge(bridgeUID)
                        .withLabel(label).withProperty("vin", vin).withProperty("vehicleType", vehicleType.name())
                        .withRepresentationProperty("vin");

                String sellname = vehicle.sellname();
                if (sellname != null && !sellname.isBlank()) {
                    resultBuilder.withProperty("modelName", sellname.trim());
                }
                String modelCode = vehicle.name();
                if (modelCode != null && !modelCode.isBlank()) {
                    resultBuilder.withProperty("modelCode", modelCode.trim());
                }

                thingDiscovered(resultBuilder.build());
            }
        } catch (PleosApiException e) {
            logger.warn("Error during Pleos vehicle scan: {}", e.getMessage());
        }
    }

    private String buildVehicleLabel(String vin, @org.eclipse.jdt.annotation.Nullable String sellname,
            @org.eclipse.jdt.annotation.Nullable String nickname) {
        String baseName = (sellname != null && !sellname.isBlank()) ? sellname.trim() : "";
        String nick = (nickname != null && !nickname.isBlank()) ? nickname.trim() : "";

        if (!baseName.isEmpty() && !nick.isEmpty()) {
            return baseName + " (" + nick + ")";
        } else if (!baseName.isEmpty()) {
            return baseName;
        } else if (!nick.isEmpty()) {
            return nick;
        }
        return "Pleos vehicle " + vin;
    }
}
