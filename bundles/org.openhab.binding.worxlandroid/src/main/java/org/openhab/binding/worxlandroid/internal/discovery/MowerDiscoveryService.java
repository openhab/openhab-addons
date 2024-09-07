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
package org.openhab.binding.worxlandroid.internal.discovery;

import static org.openhab.binding.worxlandroid.internal.WorxLandroidBindingConstants.THING_TYPE_MOWER;

import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.worxlandroid.internal.WorxLandroidBindingConstants;
import org.openhab.binding.worxlandroid.internal.api.WebApiException;
import org.openhab.binding.worxlandroid.internal.api.dto.ProductItemStatus;
import org.openhab.binding.worxlandroid.internal.config.MowerConfiguration;
import org.openhab.binding.worxlandroid.internal.handler.WorxLandroidBridgeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MowerDiscoveryService} is a service for discovering your mowers through Worx Landroid API
 *
 * @author Nils - Initial contribution
 * @author Gaël L'hopital - Added representation property and serialNumber configuration element
 */
@NonNullByDefault
public class MowerDiscoveryService extends AbstractDiscoveryService {
    /**
     * Maximum time to search for devices in seconds.
     */
    private static final int SEARCH_TIME_SEC = 20;

    private final Logger logger = LoggerFactory.getLogger(MowerDiscoveryService.class);
    private final WorxLandroidBridgeHandler bridgeHandler;

    public MowerDiscoveryService(WorxLandroidBridgeHandler bridgeHandler) {
        super(WorxLandroidBindingConstants.SUPPORTED_THING_TYPES, SEARCH_TIME_SEC);
        this.bridgeHandler = bridgeHandler;
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return WorxLandroidBindingConstants.SUPPORTED_THING_TYPES;
    }

    @Override
    protected void startScan() {
        try {
            List<ProductItemStatus> productItemsStatusResponse = bridgeHandler.retrieveAllDevices();
            productItemsStatusResponse.forEach(mower -> {

                DiscoveryResult discoveryResult = DiscoveryResultBuilder
                        .create(new ThingUID(THING_TYPE_MOWER, bridgeHandler.getThing().getUID(), mower.id))
                        .withRepresentationProperty(MowerConfiguration.SERIAL_NUMBER).withLabel(mower.name)
                        .withProperty(MowerConfiguration.SERIAL_NUMBER, mower.serialNumber)
                        .withBridge(bridgeHandler.getThing().getUID()).build();

                thingDiscovered(discoveryResult);
                logger.debug("Discovered a mower thing with ID '{}'", mower.serialNumber);
            });
        } catch (WebApiException exception) {
            logger.warn("Error in WebApiException : {}", exception.getMessage());
        }
    }

    @Override
    protected void startBackgroundDiscovery() {
        startScan();
    }
}
