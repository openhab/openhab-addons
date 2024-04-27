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
package org.openhab.binding.saicismart.internal;

import static org.openhab.binding.saicismart.internal.SAICiSMARTBindingConstants.THING_TYPE_VEHICLE;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;

import net.heberling.ismart.asn1.v1_1.entity.VinInfo;

/**
 *
 * @author Markus Heberling - Initial contribution
 */
@NonNullByDefault
public class VehicleDiscovery extends AbstractDiscoveryService implements DiscoveryService, ThingHandlerService {

    private @Nullable SAICiSMARTBridgeHandler handler;
    private static final String PROPERTY_VIN = "vin";

    public VehicleDiscovery() throws IllegalArgumentException {
        super(Set.of(THING_TYPE_VEHICLE), 0);
    }

    @Override
    protected void startScan() {
        Collection<VinInfo> vinList = handler.getVinList();
        for (VinInfo vinInfo : vinList) {
            ThingTypeUID type = THING_TYPE_VEHICLE;
            ThingUID thingUID = new ThingUID(type, handler.getThing().getUID(), vinInfo.getVin());
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                    .withLabel(new String(vinInfo.getBrandName()) + " " + new String(vinInfo.getModelName()))
                    .withBridge(handler.getThing().getUID()).withProperty(PROPERTY_VIN, vinInfo.getVin())
                    .withRepresentationProperty(PROPERTY_VIN).build();
            thingDiscovered(discoveryResult);
        }
    }

    @Override
    public void setThingHandler(ThingHandler handler) {
        this.handler = (SAICiSMARTBridgeHandler) handler;
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
}
