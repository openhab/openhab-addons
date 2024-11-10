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
package org.openhab.binding.electroluxappliance.internal.discovery;

import static org.openhab.binding.electroluxappliance.internal.ElectroluxApplianceBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.electroluxappliance.internal.ElectroluxApplianceConfiguration;
import org.openhab.binding.electroluxappliance.internal.handler.ElectroluxApplianceBridgeHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * The {@link ElectroluxApplianceDiscoveryService} searches for available
 * Electrolux Pure A9 discoverable through Electrolux Delta API.
 *
 * @author Jan Gustafsson - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = ElectroluxApplianceDiscoveryService.class)
@NonNullByDefault
public class ElectroluxApplianceDiscoveryService
        extends AbstractThingHandlerDiscoveryService<ElectroluxApplianceBridgeHandler> {
    private static final int SEARCH_TIME = 10;

    public ElectroluxApplianceDiscoveryService() {
        super(ElectroluxApplianceBridgeHandler.class, SUPPORTED_THING_TYPES_UIDS, SEARCH_TIME);
    }

    @Override
    protected void startScan() {
        ThingUID bridgeUID = thingHandler.getThing().getUID();
        thingHandler.getElectroluxApplianceThings().entrySet().stream().forEach(thing -> {
            String applianceType = thing.getValue().getApplianceType();

            if ("PUREA9".equalsIgnoreCase(applianceType)) {
                thingDiscovered(DiscoveryResultBuilder
                        .create(new ThingUID(THING_TYPE_ELECTROLUX_AIR_PURIFIER, bridgeUID, thing.getKey()))
                        .withLabel("Electrolux Pure A9").withBridge(bridgeUID)
                        .withProperty(ElectroluxApplianceConfiguration.SERIAL_NUMBER_LABEL, thing.getKey())
                        .withRepresentationProperty(ElectroluxApplianceConfiguration.SERIAL_NUMBER_LABEL).build());
            } else if ("WM".equalsIgnoreCase(applianceType)) {
                thingDiscovered(DiscoveryResultBuilder
                        .create(new ThingUID(THING_TYPE_ELECTROLUX_WASHING_MACHINE, bridgeUID, thing.getKey()))
                        .withLabel("Electrolux Washing Machine").withBridge(bridgeUID)
                        .withProperty(ElectroluxApplianceConfiguration.SERIAL_NUMBER_LABEL, thing.getKey())
                        .withRepresentationProperty(ElectroluxApplianceConfiguration.SERIAL_NUMBER_LABEL).build());
            }
        });

        stopScan();
    }
}
