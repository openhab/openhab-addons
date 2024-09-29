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
package org.openhab.binding.qolsysiq.internal.discovery;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.qolsysiq.internal.QolsysIQBindingConstants;
import org.openhab.binding.qolsysiq.internal.handler.QolsysIQChildDiscoveryHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple discovery service that can be used by Partition and Zone Handlers
 *
 * @author Dan Cunningham - Initial contribution
 *
 */
@NonNullByDefault
public class QolsysIQChildDiscoveryService extends AbstractThingHandlerDiscoveryService<QolsysIQChildDiscoveryHandler> {
    private final Logger logger = LoggerFactory.getLogger(QolsysIQChildDiscoveryService.class);

    private static final Set<ThingTypeUID> SUPPORTED_DISCOVERY_THING_TYPES_UIDS = Set
            .of(QolsysIQBindingConstants.THING_TYPE_PARTITION, QolsysIQBindingConstants.THING_TYPE_ZONE);

    public QolsysIQChildDiscoveryService() throws IllegalArgumentException {
        super(QolsysIQChildDiscoveryHandler.class, SUPPORTED_DISCOVERY_THING_TYPES_UIDS, 5, false);
    }

    @Override
    public void initialize() {
        thingHandler.setDiscoveryService(this);
        super.initialize();
    }

    @Override
    protected void startScan() {
        thingHandler.startDiscovery();
    }

    public void discoverQolsysIQChildThing(ThingUID thingUID, ThingUID bridgeUID, Integer id, String label) {
        logger.trace("discoverQolsysIQChildThing: {} {} {} {}", thingUID, bridgeUID, id, label);
        DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withLabel(label).withProperty("id", id)
                .withRepresentationProperty("id").withBridge(bridgeUID).build();
        thingDiscovered(result);
    }
}
