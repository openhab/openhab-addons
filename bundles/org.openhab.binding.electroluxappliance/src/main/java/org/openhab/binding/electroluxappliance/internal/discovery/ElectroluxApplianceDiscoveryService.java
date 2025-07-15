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
package org.openhab.binding.electroluxappliance.internal.discovery;

import static org.openhab.binding.electroluxappliance.internal.ElectroluxApplianceBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.electroluxappliance.internal.ElectroluxApplianceConfiguration;
import org.openhab.binding.electroluxappliance.internal.handler.ElectroluxApplianceBridgeHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
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
            final String deviceType = thing.getValue().getApplianceInfo().getApplianceInfo().getDeviceType();

            switch (deviceType) {
                case "PORTABLE_AIR_CONDITIONER":
                    handleThingDiscovered("Electrolux Air Conditioner", bridgeUID,
                            THING_TYPE_ELECTROLUX_PORTABLE_AIR_CONDITIONER, thing.getKey());
                    break;
                default:
                    final String applianceType = thing.getValue().getApplianceType();
                    // These two have not been modified as they are not updated to use DTO data validation for commands,
                    // and I cannot confirm the deviceTypes reported by them.
                    // Hence they fall-back to the traditional discovery methods here.
                    if ("PUREA9".equalsIgnoreCase(applianceType)) {
                        handleThingDiscovered("Electrolux Pure A9", bridgeUID, THING_TYPE_ELECTROLUX_AIR_PURIFIER,
                                thing.getKey());
                    } else if ("WM".equalsIgnoreCase(applianceType)) {
                        handleThingDiscovered("Electrolux Washing Machine", bridgeUID,
                                THING_TYPE_ELECTROLUX_WASHING_MACHINE, thing.getKey());
                    }
            }
        });

        stopScan();
    }

    private void handleThingDiscovered(final String label, final ThingUID bridgeUID, final ThingTypeUID thingTypeUID,
            final String serialNo) {
        thingDiscovered(DiscoveryResultBuilder.create(new ThingUID(thingTypeUID, bridgeUID, serialNo)).withLabel(label)
                .withBridge(bridgeUID).withProperty(ElectroluxApplianceConfiguration.SERIAL_NUMBER_LABEL, serialNo)
                .withRepresentationProperty(ElectroluxApplianceConfiguration.SERIAL_NUMBER_LABEL).build());
    }
}
