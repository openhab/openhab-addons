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
package org.openhab.binding.ddwrt.internal;

import java.util.Set;
import java.util.concurrent.ScheduledFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// @Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.ddwrt")
/**
 * The {@link ddwrtConfiguration} class is the discovery service for detecting things in DD-WRT network.
 *
 * @author Lee Ballard - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = ddwrtDiscoveryService.class)
@NonNullByDefault
public class ddwrtDiscoveryService extends AbstractThingHandlerDiscoveryService<ddwrtNetworkHandler> {

    private static final int DISCOVERY_TIMEOUT_SECONDS = 10;
    public static final ThingTypeUID THING_TYPE_DEVICE = new ThingTypeUID("ddwrt", "device");

    private final Logger logger = LoggerFactory.getLogger(ddwrtDiscoveryService.class);

    @Nullable
    private ScheduledFuture<?> scanFuture;

    public ddwrtDiscoveryService() {
        super(ddwrtNetworkHandler.class, Set.of(THING_TYPE_DEVICE), DISCOVERY_TIMEOUT_SECONDS);
    }

    @Override
    protected void startScan() {
        logger.debug("Starting DD-WRT discovery scan");
        return;
    }
}
