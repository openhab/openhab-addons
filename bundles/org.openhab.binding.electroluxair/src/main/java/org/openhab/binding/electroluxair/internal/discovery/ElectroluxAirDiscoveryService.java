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
package org.openhab.binding.electroluxair.internal.discovery;

import static org.openhab.binding.electroluxair.internal.ElectroluxAirBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.electroluxair.internal.ElectroluxAirConfiguration;
import org.openhab.binding.electroluxair.internal.handler.ElectroluxAirBridgeHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * The {@link ElectroluxAirDiscoveryService} searches for available
 * Electrolux Pure A9 discoverable through Electrolux Delta API.
 *
 * @author Jan Gustafsson - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = ElectroluxAirDiscoveryService.class)
@NonNullByDefault
public class ElectroluxAirDiscoveryService extends AbstractThingHandlerDiscoveryService<ElectroluxAirBridgeHandler> {
    private static final int SEARCH_TIME = 2;

    public ElectroluxAirDiscoveryService() {
        super(ElectroluxAirBridgeHandler.class, SUPPORTED_THING_TYPES_UIDS, SEARCH_TIME);
    }

    @Override
    protected void startScan() {
        ThingUID bridgeUID = thingHandler.getThing().getUID();
        thingHandler.getElectroluxAirThings().entrySet().stream().forEach(thing -> {
            thingDiscovered(DiscoveryResultBuilder
                    .create(new ThingUID(THING_TYPE_ELECTROLUX_PURE_A9, bridgeUID, thing.getKey()))
                    .withLabel("Electrolux Pure A9").withBridge(bridgeUID)
                    .withProperty(ElectroluxAirConfiguration.DEVICE_ID_LABEL, thing.getKey())
                    .withRepresentationProperty(ElectroluxAirConfiguration.DEVICE_ID_LABEL).build());
        });

        stopScan();
    }
}
