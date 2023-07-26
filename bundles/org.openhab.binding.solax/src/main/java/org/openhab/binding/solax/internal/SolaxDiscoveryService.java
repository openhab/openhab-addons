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
package org.openhab.binding.solax.internal;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.solax.internal.model.InverterData;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SolaxDiscoveryService} is used for the discovery implementation.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public class SolaxDiscoveryService extends AbstractDiscoveryService implements ThingHandlerService {

    private final Logger logger = LoggerFactory.getLogger(SolaxDiscoveryService.class);

    private static final int TIMEOUT = 10;

    private @Nullable SolaxBridgeHandler bridgeHandler;

    public SolaxDiscoveryService() throws IllegalArgumentException {
        super(SolaxBindingConstants.SUPPORTED_THING_TYPES_UIDS, TIMEOUT);
    }

    @SuppressWarnings("null")
    @Override
    protected void startScan() {
        if (bridgeHandler == null) {
            logger.warn("Unable to retrieve the registered Solax bridge");
            return;
        }

        try {
            InverterData data = bridgeHandler.scanForInverter();
            if (data == null) {
                logger.warn("Unable to retrieve inverter data from bridge...");
                return;
            }

            String serialNumber = data.getWifiSerial();
            if (serialNumber != null) {
                ThingUID thingUID = new ThingUID(SolaxBindingConstants.THING_TYPE_INVERTER,
                        bridgeHandler.getThing().getUID(), serialNumber);

                DiscoveryResult result = DiscoveryResultBuilder.create(thingUID)
                        .withProperty(SolaxBindingConstants.SERIAL_NUMBER, serialNumber)
                        .withProperty(SolaxBindingConstants.INVERTER_TYPE, data.getInverterType().name())
                        .withRepresentationProperty(SolaxBindingConstants.SERIAL_NUMBER)
                        .withBridge(bridgeHandler.getThing().getUID()).withLabel(data.getInverterType().name()).build();
                thingDiscovered(result);
            } else {
                logger.warn(
                        "Unable to retrieve the serial number of the inverter's wifi module. Cannot continue with discovery...");
            }
        } catch (IOException e) {
            logger.warn("Scan for inverter ended with an exception.", e);
        }
    }

    @Override
    public void setThingHandler(ThingHandler handler) {
        if (handler instanceof SolaxBridgeHandler bridgeHandler) {
            this.bridgeHandler = bridgeHandler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return bridgeHandler;
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }
}
