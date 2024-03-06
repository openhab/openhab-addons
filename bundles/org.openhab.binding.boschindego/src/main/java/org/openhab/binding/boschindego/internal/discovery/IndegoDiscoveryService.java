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
package org.openhab.binding.boschindego.internal.discovery;

import static org.openhab.binding.boschindego.internal.BoschIndegoBindingConstants.*;

import java.time.Instant;
import java.util.Collection;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschindego.internal.IndegoTypeDatabase;
import org.openhab.binding.boschindego.internal.dto.response.DevicePropertiesResponse;
import org.openhab.binding.boschindego.internal.exceptions.IndegoException;
import org.openhab.binding.boschindego.internal.handler.BoschAccountHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link IndegoDiscoveryService} is responsible for discovering Indego mowers.
 *
 * @author Jacob Laursen - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = IndegoDiscoveryService.class)
@NonNullByDefault
public class IndegoDiscoveryService extends AbstractThingHandlerDiscoveryService<BoschAccountHandler> {
    private static final int TIMEOUT_SECONDS = 60;

    private final Logger logger = LoggerFactory.getLogger(IndegoDiscoveryService.class);

    public IndegoDiscoveryService() {
        super(BoschAccountHandler.class, Set.of(THING_TYPE_ACCOUNT), TIMEOUT_SECONDS, false);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return Set.of(THING_TYPE_INDEGO);
    }

    @Override
    public void startScan() {
        try {
            Collection<DevicePropertiesResponse> devices = thingHandler.getDevices();

            ThingUID bridgeUID = thingHandler.getThing().getUID();
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
    public void dispose() {
        super.dispose();
        removeOlderResults(Instant.now().toEpochMilli());
    }
}
