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
package org.openhab.binding.volvooncall.internal.discovery;

import static org.openhab.binding.volvooncall.internal.VolvoOnCallBindingConstants.*;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.volvooncall.internal.VolvoOnCallException;
import org.openhab.binding.volvooncall.internal.api.VocHttpApi;
import org.openhab.binding.volvooncall.internal.config.VehicleConfiguration;
import org.openhab.binding.volvooncall.internal.dto.AccountVehicleRelation;
import org.openhab.binding.volvooncall.internal.dto.Attributes;
import org.openhab.binding.volvooncall.internal.dto.CustomerAccounts;
import org.openhab.binding.volvooncall.internal.dto.Vehicles;
import org.openhab.binding.volvooncall.internal.handler.VolvoOnCallBridgeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VolvoVehicleDiscoveryService} searches for available
 * cars discoverable through VocAPI
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class VolvoVehicleDiscoveryService extends AbstractDiscoveryService implements ThingHandlerService {
    private static final int SEARCH_TIME = 2;
    private final Logger logger = LoggerFactory.getLogger(VolvoVehicleDiscoveryService.class);
    private @Nullable VolvoOnCallBridgeHandler handler;

    public VolvoVehicleDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, SEARCH_TIME);
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof VolvoOnCallBridgeHandler volvoOnCallBridgeHandler) {
            this.handler = volvoOnCallBridgeHandler;
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
        VolvoOnCallBridgeHandler bridgeHandler = this.handler;
        if (bridgeHandler != null) {
            ThingUID bridgeUID = bridgeHandler.getThing().getUID();
            VocHttpApi api = bridgeHandler.getApi();
            if (api != null) {
                try {
                    CustomerAccounts account = api.getURL("customeraccounts/", CustomerAccounts.class);
                    account.accountVehicleRelationsURL.forEach(relationURL -> {
                        try {
                            AccountVehicleRelation accountVehicle = api.getURL(relationURL,
                                    AccountVehicleRelation.class);
                            logger.debug("Found vehicle : {}", accountVehicle.vehicleId);

                            Vehicles vehicle = api.getURL(accountVehicle.vehicleURL, Vehicles.class);
                            Attributes attributes = api.getURL(Attributes.class, vehicle.vehicleId);

                            thingDiscovered(DiscoveryResultBuilder
                                    .create(new ThingUID(VEHICLE_THING_TYPE, bridgeUID, accountVehicle.vehicleId))
                                    .withLabel(attributes.vehicleType + " " + attributes.registrationNumber)
                                    .withBridge(bridgeUID).withProperty(VehicleConfiguration.VIN, attributes.vin)
                                    .withRepresentationProperty(VehicleConfiguration.VIN).build());

                        } catch (VolvoOnCallException e) {
                            logger.warn("Error while getting vehicle informations : {}", e.getMessage());
                        }
                    });
                } catch (VolvoOnCallException e) {
                    logger.warn("Error while discovering vehicle: {}", e.getMessage());
                }
            }
        }
        stopScan();
    }
}
