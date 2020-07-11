/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.vwweconnect.internal.discovery;

import static org.openhab.binding.vwweconnect.internal.VWWeConnectBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
import org.openhab.binding.vwweconnect.internal.VWWeConnectSession;
import org.openhab.binding.vwweconnect.internal.handler.VWWeConnectBridgeHandler;
import org.openhab.binding.vwweconnect.internal.model.BaseVehicle;
import org.openhab.binding.vwweconnect.internal.model.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The discovery service, notified by a listener on the VWWeConnectSession.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VWWeConnectDiscoveryService extends AbstractDiscoveryService
        implements DiscoveryService, ThingHandlerService {

    private static final int SEARCH_TIME_SECONDS = 60;
    private final Logger logger = LoggerFactory.getLogger(VWWeConnectDiscoveryService.class);
    private @Nullable VWWeConnectBridgeHandler vwWeConnectBridgeHandler;
    private @NonNullByDefault({}) ThingUID bridgeUID;

    public VWWeConnectDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, SEARCH_TIME_SECONDS);
    }

    @Override
    public void startScan() {
        removeOlderResults(getTimestampOfLastScan());
        logger.debug("VWWeConnectDiscoveryService:startScan");

        if (vwWeConnectBridgeHandler != null) {
            VWWeConnectSession session = vwWeConnectBridgeHandler.getSession();
            if (session != null) {
                HashMap<String, BaseVehicle> vwWeConnectThings = session.getVWWeConnectThings();
                for (Map.Entry<String, BaseVehicle> entry : vwWeConnectThings.entrySet()) {
                    BaseVehicle thing = entry.getValue();
                    if (thing != null) {
                        logger.info("Thing: {}", thing);
                        onThingAddedInternal(thing);
                    }
                }
            }
        }
    }

    private void onThingAddedInternal(BaseVehicle thing) {
        logger.debug("VWWeConnectDiscoveryService:OnThingAddedInternal");
        if (thing instanceof Vehicle) {
            Vehicle theVehicle = (Vehicle) thing;
            String vin = theVehicle.getCompleteVehicleJson().getVin();
            if (vin != null) {
                ThingUID thingUID = new ThingUID(VEHICLE_THING_TYPE, vin);
                String label = theVehicle.getCompleteVehicleJson().getName();
                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withLabel(label)
                        .withBridge(bridgeUID).withProperty(VIN, vin).withRepresentationProperty(vin).build();
                logger.debug("Discovered thing: thinguid: {}, bridge {}, label {}", thingUID.toString(), bridgeUID,
                        label);
                thingDiscovered(discoveryResult);
            } else {
                logger.debug("VIN is null for thing '{}'", thing);
            }
        } else {
            logger.debug("Discovered unsupported thing of type '{}'", thing.getClass());
        }
    }

    @Override
    public void activate() {
        Map<String, @Nullable Object> properties = new HashMap<>();
        properties.put(DiscoveryService.CONFIG_PROPERTY_BACKGROUND_DISCOVERY, Boolean.TRUE);
        super.activate(properties);
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof VWWeConnectBridgeHandler) {
            vwWeConnectBridgeHandler = (VWWeConnectBridgeHandler) handler;
            bridgeUID = vwWeConnectBridgeHandler.getThing().getUID();
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return vwWeConnectBridgeHandler;
    }
}
