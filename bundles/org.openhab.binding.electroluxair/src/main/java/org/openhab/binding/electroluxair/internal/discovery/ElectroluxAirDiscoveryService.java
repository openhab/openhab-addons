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
package org.openhab.binding.electroluxair.internal.discovery;

import static org.openhab.binding.electroluxair.internal.ElectroluxAirBindingConstants.*;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.electroluxair.internal.ElectroluxAirConfiguration;
import org.openhab.binding.electroluxair.internal.handler.ElectroluxAirBridgeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;

/**
 * The {@link ElectroluxAirDiscoveryService} searches for available
 * Electrolux Pure A9 discoverable through Electrolux Delta API.
 *
 * @author Jan Gustafsson - Initial contribution
 */
@NonNullByDefault
public class ElectroluxAirDiscoveryService extends AbstractDiscoveryService
        implements ThingHandlerService, DiscoveryService {
    private static final int SEARCH_TIME = 2;
    private @Nullable ElectroluxAirBridgeHandler handler;

    public ElectroluxAirDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, SEARCH_TIME);
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof ElectroluxAirBridgeHandler bridgeHandler) {
            this.handler = bridgeHandler;
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
        ElectroluxAirBridgeHandler bridgeHandler = this.handler;
        if (bridgeHandler != null) {
            ThingUID bridgeUID = bridgeHandler.getThing().getUID();
            bridgeHandler.getElectroluxAirThings().entrySet().stream().forEach(thing -> {
                thingDiscovered(DiscoveryResultBuilder
                        .create(new ThingUID(THING_TYPE_ELECTROLUX_PURE_A9, bridgeUID, thing.getKey()))
                        .withLabel("Electrolux Pure A9").withBridge(bridgeUID)
                        .withProperty(ElectroluxAirConfiguration.DEVICE_ID_LABEL, thing.getKey())
                        .withRepresentationProperty(ElectroluxAirConfiguration.DEVICE_ID_LABEL).build());
            });
        }
        stopScan();
    }
}
