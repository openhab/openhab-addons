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
package org.openhab.binding.automower.internal.discovery;

import static org.openhab.binding.automower.internal.AutomowerBindingConstants.THING_TYPE_AUTOMOWER;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.automower.internal.AutomowerBindingConstants;
import org.openhab.binding.automower.internal.bridge.AutomowerBridgeHandler;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.Mower;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.MowerListResult;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;

/**
 * The {@link AutomowerDiscoveryService} is responsible for discovering new mowers available for the
 * configured app key.
 *
 * @author Markus Pfleger - Initial contribution
 */
@NonNullByDefault
public class AutomowerDiscoveryService extends AbstractDiscoveryService {

    private final AutomowerBridgeHandler bridgeHandler;

    public AutomowerDiscoveryService(AutomowerBridgeHandler bridgeHandler) {
        super(Collections.singleton(THING_TYPE_AUTOMOWER), 10, false);
        this.bridgeHandler = bridgeHandler;
    }

    @Override
    protected void startScan() {
        Optional<MowerListResult> registeredMowers = bridgeHandler.getAutomowers();

        registeredMowers.ifPresent(mowers -> {
            for (Mower mower : mowers.getData()) {
                ThingUID bridgeUID = bridgeHandler.getThing().getUID();
                ThingTypeUID thingTypeUID = THING_TYPE_AUTOMOWER;
                ThingUID mowerThingUid = new ThingUID(THING_TYPE_AUTOMOWER, bridgeUID, mower.getId());

                Map<String, Object> properties = new HashMap<>();
                properties.put(AutomowerBindingConstants.AUTOMOWER_ID, mower.getId());
                properties.put(AutomowerBindingConstants.AUTOMOWER_SERIAL_NUMBER,
                        mower.getAttributes().getSystem().getSerialNumber());
                properties.put(AutomowerBindingConstants.AUTOMOWER_MODEL, mower.getAttributes().getSystem().getModel());
                properties.put(AutomowerBindingConstants.AUTOMOWER_NAME, mower.getAttributes().getSystem().getName());

                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(mowerThingUid)
                        .withThingType(thingTypeUID).withProperties(properties).withBridge(bridgeUID)
                        .withRepresentationProperty(AutomowerBindingConstants.AUTOMOWER_ID)
                        .withLabel(mower.getAttributes().getSystem().getName() + " (Automower "
                                + mower.getAttributes().getSystem().getModel() + ")")
                        .build();

                thingDiscovered(discoveryResult);
            }
        });
    }
}
