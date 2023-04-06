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
import org.openhab.binding.boschindego.internal.exceptions.IndegoException;
import org.openhab.binding.boschindego.internal.handler.BoschAccountHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link IndegoDiscoveryService} is responsible for discovering Indego mowers.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class IndegoDiscoveryService extends AbstractDiscoveryService {

    private static final int TIMEOUT_SECONDS = 60;

    private final Logger logger = LoggerFactory.getLogger(IndegoDiscoveryService.class);
    private final BoschAccountHandler accountHandler;

    public IndegoDiscoveryService(BoschAccountHandler accountHandler) {
        super(Set.of(THING_TYPE_ACCOUNT), TIMEOUT_SECONDS, false);
        this.accountHandler = accountHandler;
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return Set.of(THING_TYPE_INDEGO);
    }

    @Override
    public void startScan() {
        try {
            Collection<String> serialNumbers = accountHandler.getSerialNumbers();

            ThingUID bridgeUID = accountHandler.getThing().getUID();
            for (String serialNumber : serialNumbers) {
                ThingUID thingUID = new ThingUID(THING_TYPE_INDEGO, bridgeUID, serialNumber);
                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                        .withProperty(Thing.PROPERTY_SERIAL_NUMBER, serialNumber).withBridge(bridgeUID)
                        .withRepresentationProperty(Thing.PROPERTY_SERIAL_NUMBER)
                        .withLabel("Indego (" + serialNumber + ")").build();

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
