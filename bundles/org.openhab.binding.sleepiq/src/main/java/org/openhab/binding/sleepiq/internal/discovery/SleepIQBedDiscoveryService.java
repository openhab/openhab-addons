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
package org.openhab.binding.sleepiq.internal.discovery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.sleepiq.internal.SleepIQBindingConstants;
import org.openhab.binding.sleepiq.internal.api.dto.Bed;
import org.openhab.binding.sleepiq.internal.config.SleepIQBedConfiguration;
import org.openhab.binding.sleepiq.internal.handler.SleepIQCloudHandler;
import org.openhab.binding.sleepiq.internal.handler.SleepIQDualBedHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SleepIQBedDiscoveryService} is responsible for processing the
 * results of devices found through the SleepIQ cloud service.
 *
 * @author Gregory Moyer - Initial contribution
 */
@NonNullByDefault
public class SleepIQBedDiscoveryService extends AbstractDiscoveryService {
    private static final int TIMEOUT = 60;

    private final Logger logger = LoggerFactory.getLogger(SleepIQBedDiscoveryService.class);

    private final SleepIQCloudHandler cloudHandler;

    /**
     * Discovers beds in the SleepIQ cloud service account.
     *
     * @param cloudHandler the cloud service handler (bridge)
     */
    public SleepIQBedDiscoveryService(final SleepIQCloudHandler cloudHandler) {
        super(SleepIQDualBedHandler.SUPPORTED_THING_TYPE_UIDS, TIMEOUT, true);
        this.cloudHandler = cloudHandler;
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Starting background discovery for new beds");
        startScan();
    }

    @Override
    protected void startScan() {
        logger.debug("Starting scan for new beds");
        List<Bed> beds = cloudHandler.getBeds();
        if (beds != null) {
            for (Bed bed : beds) {
                // only dual chamber beds are supported currently
                if (!bed.isDualSleep()) {
                    logger.info("Found a bed that is not dual chamber - currently unsupported");
                    continue;
                }

                ThingUID bridgeUID = cloudHandler.getThing().getUID();
                ThingUID thingUID = new ThingUID(SleepIQBindingConstants.THING_TYPE_DUAL_BED, bridgeUID,
                        bed.getMacAddress());

                // thing already exists
                if (cloudHandler.getThing().getThing(thingUID) != null) {
                    continue;
                }

                logger.debug("New bed found with MAC address {}", bed.getMacAddress());
                @SuppressWarnings({ "unchecked", "rawtypes" })
                Map<String, Object> properties = (Map) cloudHandler.updateProperties(bed, new HashMap<>());
                properties.put(SleepIQBedConfiguration.BED_ID, bed.getBedId());

                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                        .withBridge(bridgeUID).withLabel(bed.getName() + " - " + bed.getModel()).build();
                thingDiscovered(discoveryResult);
            }
        }
        stopScan();
    }
}
