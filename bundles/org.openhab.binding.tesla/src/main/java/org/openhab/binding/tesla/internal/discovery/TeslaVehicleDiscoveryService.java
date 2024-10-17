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
package org.openhab.binding.tesla.internal.discovery;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tesla.internal.TeslaBindingConstants;
import org.openhab.binding.tesla.internal.TeslaHandlerFactory;
import org.openhab.binding.tesla.internal.handler.TeslaAccountHandler;
import org.openhab.binding.tesla.internal.handler.VehicleListener;
import org.openhab.binding.tesla.internal.protocol.dto.Vehicle;
import org.openhab.binding.tesla.internal.protocol.dto.VehicleConfig;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This service is used by {@link TeslaAccountHandler} instances in order to
 * automatically provide vehicle information from the account.
 *
 * @author Kai Kreuzer - Initial contribution
 *
 */
@NonNullByDefault
@Component(scope = ServiceScope.PROTOTYPE, service = TeslaVehicleDiscoveryService.class)
public class TeslaVehicleDiscoveryService extends AbstractThingHandlerDiscoveryService<TeslaAccountHandler>
        implements VehicleListener {
    private final Logger logger = LoggerFactory.getLogger(TeslaVehicleDiscoveryService.class);

    public TeslaVehicleDiscoveryService() throws IllegalArgumentException {
        super(TeslaAccountHandler.class, TeslaHandlerFactory.SUPPORTED_THING_TYPES_UIDS, 10, true);
    }

    @Override
    public void initialize() {
        thingHandler.addVehicleListener(this);
        super.initialize();
    }

    @Override
    protected void startScan() {
        thingHandler.scanForVehicles();
    }

    @Override
    public void dispose() {
        super.dispose();
        thingHandler.removeVehicleListener(this);
    }

    @Override
    public void vehicleFound(Vehicle vehicle, @Nullable VehicleConfig vehicleConfig) {
        ThingTypeUID type = vehicleConfig == null ? TeslaBindingConstants.THING_TYPE_VEHICLE
                : vehicleConfig.identifyModel();
        if (type != null) {
            logger.debug("Found a {} vehicle", type.getId());
            ThingUID thingUID = new ThingUID(type, thingHandler.getThing().getUID(), vehicle.vin);
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withLabel(vehicle.displayName)
                    .withBridge(thingHandler.getThing().getUID()).withProperty(TeslaBindingConstants.VIN, vehicle.vin)
                    .build();
            thingDiscovered(discoveryResult);
        }
    }
}
