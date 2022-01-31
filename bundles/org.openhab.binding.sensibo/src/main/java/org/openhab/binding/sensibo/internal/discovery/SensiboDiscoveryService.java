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
package org.openhab.binding.sensibo.internal.discovery;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.sensibo.internal.SensiboBindingConstants;
import org.openhab.binding.sensibo.internal.handler.SensiboAccountHandler;
import org.openhab.binding.sensibo.internal.model.SensiboModel;
import org.openhab.binding.sensibo.internal.model.SensiboSky;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Arne Seime - Initial contribution
 */
@NonNullByDefault
public class SensiboDiscoveryService extends AbstractDiscoveryService {
    public static final Set<ThingTypeUID> DISCOVERABLE_THING_TYPES_UIDS = Collections
            .singleton(SensiboBindingConstants.THING_TYPE_SENSIBOSKY);
    private static final long REFRESH_INTERVAL_MINUTES = 60;
    private final Logger logger = LoggerFactory.getLogger(SensiboDiscoveryService.class);
    private final SensiboAccountHandler accountHandler;
    private Optional<ScheduledFuture<?>> discoveryJob = Optional.empty();

    public SensiboDiscoveryService(final SensiboAccountHandler accountHandler) {
        super(DISCOVERABLE_THING_TYPES_UIDS, 10);
        this.accountHandler = accountHandler;
    }

    @Override
    protected void startBackgroundDiscovery() {
        discoveryJob = Optional
                .of(scheduler.scheduleWithFixedDelay(this::startScan, 0, REFRESH_INTERVAL_MINUTES, TimeUnit.MINUTES));
    }

    @Override
    protected void startScan() {
        logger.debug("Start scan for Sensibo devices.");
        synchronized (this) {
            removeOlderResults(getTimestampOfLastScan(), null, accountHandler.getThing().getUID());
            final ThingUID accountUID = accountHandler.getThing().getUID();
            accountHandler.updateModelFromServerAndUpdateThingStatus();
            final SensiboModel model = accountHandler.getModel();
            for (final SensiboSky pod : model.getPods()) {
                final ThingUID podUID = new ThingUID(SensiboBindingConstants.THING_TYPE_SENSIBOSKY, accountUID,
                        String.valueOf(pod.getMacAddress()));
                Map<String, String> properties = pod.getThingProperties();

                // DiscoveryResult result uses Map<String,Object> as properties while ThingBuilder uses
                // Map<String,String>
                Map<String, Object> stringObjectProperties = new HashMap<>();
                stringObjectProperties.putAll(properties);

                final DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(podUID).withBridge(accountUID)
                        .withLabel(pod.getProductName()).withRepresentationProperty(Thing.PROPERTY_MAC_ADDRESS)
                        .withProperties(stringObjectProperties).build();
                thingDiscovered(discoveryResult);
            }
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        stopScan();
        discoveryJob.ifPresent(job -> {
            if (!job.isCancelled()) {
                job.cancel(true);
            }
            discoveryJob = Optional.empty();
        });
    }

    @Override
    protected void stopScan() {
        logger.debug("Stop scan for Sensibo devices.");
        super.stopScan();
    }
}
