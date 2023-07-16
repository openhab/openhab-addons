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

package org.openhab.binding.connectedcar.internal.discovery;

import static org.openhab.binding.connectedcar.internal.BindingConstants.*;
import static org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.*;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.VehicleDetails;
import org.openhab.binding.connectedcar.internal.api.ApiException;
import org.openhab.binding.connectedcar.internal.handler.AccountHandler;
import org.openhab.binding.connectedcar.internal.handler.AccountListener;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Device discovery creates a thing in the inbox for each vehicle
 * found in the data received from {@link AccountHandler}.
 *
 * @author Markus Michels - Initial contribution
 * @author Thomas Knaller - Maintainer
 * @author Dr. Yves Kreis - Maintainer
 */
@NonNullByDefault
public class ConnectedCarDiscoveryService extends AbstractDiscoveryService implements AccountListener {
    private final Logger logger = LoggerFactory.getLogger(ConnectedCarDiscoveryService.class);
    private final AccountHandler accountHandler;
    private ThingUID bridgeUID;
    private static final int TIMEOUT = 10;

    public ConnectedCarDiscoveryService(AccountHandler bridgeHandler, Bundle bundle) {
        super(SUPPORTED_THING_TYPES_UIDS, TIMEOUT);
        this.accountHandler = bridgeHandler;
        this.bridgeUID = bridgeHandler.getThing().getUID();
    }

    /**
     * Called by Account Handler for each vehicle found under the account, creates the corresponding vehicle thing
     */
    @Override
    public void informationUpdate(@Nullable List<VehicleDetails> vehicleList) {
        if (vehicleList == null) {
            return;
        }
        for (VehicleDetails vehicle : vehicleList) {
            logger.debug("{} Thing with id {} discovered", vehicle.brand, vehicle.vin);
            Map<String, Object> properties = new TreeMap<String, Object>();
            ThingTypeUID tuid;
            switch (vehicle.brand) {
                case API_BRAND_VWID:
                    tuid = THING_TYPE_IDVEHICLE;
                    break;
                case "ŠKODA":
                case API_BRAND_SKODA_E:
                    tuid = THING_TYPE_SKODAEVEHICLE;
                    break;
                case API_BRAND_FORD:
                    tuid = THING_TYPE_FORDVEHICLE;
                    break;
                case API_BRAND_WECHARGE:
                    tuid = THING_TYPE_WCWALLBOX;
                    break;
                default:
                    tuid = THING_TYPE_CNVEHICLE;
            }
            ThingUID uid = new ThingUID(tuid, bridgeUID, vehicle.getId());
            properties.put(PROPERTY_VIN, vehicle.vin);
            properties.put(PROPERTY_MODEL, vehicle.model);
            properties.put(PROPERTY_COLOR, vehicle.color);
            properties.put(PROPERTY_ENGINE, vehicle.engine);
            properties.put(PROPERTY_TRANS, vehicle.transmission);
            logger.debug("{}: Adding discovered thing with id {}", vehicle.getId(), uid.toString());
            DiscoveryResult result = DiscoveryResultBuilder.create(uid).withBridge(bridgeUID).withProperties(properties)
                    .withRepresentationProperty(PROPERTY_VIN).withLabel(vehicle.model).build();
            thingDiscovered(result);

        }
    }

    @Override
    protected void startScan() {
        try {
            accountHandler.initializeThing("Discovery:startScan");
        } catch (ApiException e) {
            logger.debug("Discovery failed: {}", e.getMessage());
        }
    }

    public void activate() {
        accountHandler.registerListener(this);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        accountHandler.unregisterListener(this);
    }

    @Override
    public void stateChanged(ThingStatus status, ThingStatusDetail detail, String message) {
    }
}
