/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.eyeonwater.internal.discovery;

import static org.openhab.binding.eyeonwater.internal.EyeOnWaterBindingConstants.THING_TYPE_METER;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.eyeonwater.internal.api.EyeOnWaterClient.EyeOnWaterMeterData;
import org.openhab.binding.eyeonwater.internal.handler.EyeOnWaterBridgeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EyeOnWaterDiscoveryService} discovers physical water meters via the EyeOnWater API.
 *
 * @author Richard Koshak - Initial contribution
 */
@NonNullByDefault
public class EyeOnWaterDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(EyeOnWaterDiscoveryService.class);
    private final EyeOnWaterBridgeHandler bridgeHandler;

    public EyeOnWaterDiscoveryService(EyeOnWaterBridgeHandler bridgeHandler) {
        super(Set.of(THING_TYPE_METER), 15);
        this.bridgeHandler = bridgeHandler;
    }

    @Override
    protected void startScan() {
        logger.debug("Starting EyeOnWater meter discovery scan...");
        try {
            List<EyeOnWaterMeterData> meters = bridgeHandler.discoverMeters();
            for (EyeOnWaterMeterData meter : meters) {
                ThingUID thingUid = new ThingUID(THING_TYPE_METER, bridgeHandler.getThing().getUID(),
                        meter.getMeterId());

                Map<String, Object> properties = new HashMap<>();
                properties.put("meterUuid", meter.getMeterUuid());
                properties.put("meterId", meter.getMeterId());

                DiscoveryResult result = DiscoveryResultBuilder.create(thingUid)
                        .withBridge(bridgeHandler.getThing().getUID())
                        .withLabel("EyeOnWater Meter (" + meter.getMeterId() + ")").withProperties(properties).build();

                thingDiscovered(result);
            }
        } catch (IOException | IllegalStateException e) {
            logger.warn("Failed to run EyeOnWater discovery scan", e);
        } catch (InterruptedException e) {
            logger.debug("Discovery scan interrupted", e);
            Thread.currentThread().interrupt();
        }
    }
}
