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
package org.openhab.binding.boschindego.internal.discovery;

import static org.openhab.binding.boschindego.internal.BoschIndegoBindingConstants.*;

import java.time.Instant;
import java.util.Collection;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.boschindego.internal.IndegoTypeDatabase;
import org.openhab.binding.boschindego.internal.dto.response.DevicePropertiesResponse;
import org.openhab.binding.boschindego.internal.exceptions.IndegoException;
import org.openhab.binding.boschindego.internal.handler.BoschAccountHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link IndegoDiscoveryService} is responsible for discovering Indego mowers.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class IndegoDiscoveryService extends AbstractDiscoveryService implements ThingHandlerService {

    private static final int TIMEOUT_SECONDS = 60;

    private final Logger logger = LoggerFactory.getLogger(IndegoDiscoveryService.class);

    private @NonNullByDefault({}) BoschAccountHandler accountHandler;

    public IndegoDiscoveryService() {
        super(Set.of(THING_TYPE_ACCOUNT), TIMEOUT_SECONDS, false);
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return accountHandler;
    }

    @Override
    public void setThingHandler(ThingHandler handler) {
        if (handler instanceof BoschAccountHandler accountHandler) {
            this.accountHandler = accountHandler;
        }
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return Set.of(THING_TYPE_INDEGO);
    }

    @Override
    public void startScan() {
        try {
            Collection<DevicePropertiesResponse> devices = accountHandler.getDevices();

            ThingUID bridgeUID = accountHandler.getThing().getUID();
            for (DevicePropertiesResponse device : devices) {
                ThingUID thingUID = new ThingUID(THING_TYPE_INDEGO, bridgeUID, device.serialNumber);
                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                        .withProperty(Thing.PROPERTY_SERIAL_NUMBER, device.serialNumber).withBridge(bridgeUID)
                        .withRepresentationProperty(Thing.PROPERTY_SERIAL_NUMBER)
                        .withLabel(IndegoTypeDatabase.nameFromTypeNumber(device.bareToolNumber)).build();

                thingDiscovered(discoveryResult);
            }
        } catch (IndegoException e) {
            logger.debug("Failed to retrieve serial numbers: {}", e.getMessage());
        }
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }

    @Override
    public void deactivate() {
        removeOlderResults(Instant.now().getEpochSecond());
    }
}
