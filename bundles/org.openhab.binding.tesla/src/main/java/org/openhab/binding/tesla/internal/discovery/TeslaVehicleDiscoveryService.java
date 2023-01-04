/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.util.Map;

import org.openhab.binding.tesla.internal.TeslaBindingConstants;
import org.openhab.binding.tesla.internal.TeslaHandlerFactory;
import org.openhab.binding.tesla.internal.handler.TeslaAccountHandler;
import org.openhab.binding.tesla.internal.handler.VehicleListener;
import org.openhab.binding.tesla.internal.protocol.Vehicle;
import org.openhab.binding.tesla.internal.protocol.VehicleConfig;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This service is used by {@link TeslaAccountHandler} instances in order to
 * automatically provide vehicle information from the account.
 *
 * @author Kai Kreuzer - Initial contribution
 *
 */
@Component(service = ThingHandlerService.class)
public class TeslaVehicleDiscoveryService extends AbstractDiscoveryService
        implements DiscoveryService, VehicleListener, ThingHandlerService {
    private final Logger logger = LoggerFactory.getLogger(TeslaVehicleDiscoveryService.class);

    public TeslaVehicleDiscoveryService() throws IllegalArgumentException {
        super(TeslaHandlerFactory.SUPPORTED_THING_TYPES_UIDS, 10, true);
    }

    private TeslaAccountHandler handler;

    @Override
    public void setThingHandler(ThingHandler handler) {
        this.handler = (TeslaAccountHandler) handler;
        this.handler.addVehicleListener(this);
    }

    @Override
    public ThingHandler getThingHandler() {
        return handler;
    }

    @Override
    protected void startScan() {
        handler.scanForVehicles();
    }

    @Override
    public void activate(Map<String, Object> configProperties) {
        super.activate(configProperties);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        if (handler != null) {
            handler.removeVehicleListener(this);
        }
    }

    @Override
    public void vehicleFound(Vehicle vehicle, VehicleConfig vehicleConfig) {
        ThingTypeUID type = vehicleConfig == null ? TeslaBindingConstants.THING_TYPE_VEHICLE
                : vehicleConfig.identifyModel();
        if (type != null) {
            logger.debug("Found a {} vehicle", type.getId());
            ThingUID thingUID = new ThingUID(type, handler.getThing().getUID(), vehicle.vin);
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withLabel(vehicle.display_name)
                    .withBridge(handler.getThing().getUID()).withProperty(TeslaBindingConstants.VIN, vehicle.vin)
                    .build();
            thingDiscovered(discoveryResult);
        }
    }
}
