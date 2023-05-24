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
package org.openhab.binding.hdpowerview.internal.discovery;

import static org.openhab.binding.hdpowerview.internal.HDPowerViewBindingConstants.*;

import java.net.UnknownHostException;
import java.util.Collections;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hdpowerview.internal.config.HDPowerViewHubConfiguration;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jcifs.netbios.NbtAddress;

/**
 * Discovers HD PowerView hubs by means of NetBios
 *
 * @author Andy Lintner - Initial contribution
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, configurationPid = "discovery.hdpowerview")
public class HDPowerViewHubDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(HDPowerViewHubDiscoveryService.class);

    private final Runnable scanner;
    private @Nullable ScheduledFuture<?> backgroundFuture;

    public HDPowerViewHubDiscoveryService() {
        super(Collections.singleton(THING_TYPE_HUB), 60, true);
        scanner = createScanner();
    }

    @Override
    protected void startScan() {
        scheduler.execute(scanner);
    }

    @Override
    protected void startBackgroundDiscovery() {
        ScheduledFuture<?> backgroundFuture = this.backgroundFuture;
        if (backgroundFuture != null && !backgroundFuture.isDone()) {
            backgroundFuture.cancel(true);
        }
        this.backgroundFuture = scheduler.scheduleWithFixedDelay(scanner, 0, 60, TimeUnit.SECONDS);
    }

    @Override
    protected void stopBackgroundDiscovery() {
        ScheduledFuture<?> backgroundFuture = this.backgroundFuture;
        if (backgroundFuture != null && !backgroundFuture.isDone()) {
            backgroundFuture.cancel(true);
            this.backgroundFuture = null;
        }
        super.stopBackgroundDiscovery();
    }

    private Runnable createScanner() {
        return () -> {
            for (String netBiosName : NETBIOS_NAMES) {
                try {
                    NbtAddress address = NbtAddress.getByName(netBiosName);
                    if (address != null) {
                        String host = address.getInetAddress().getHostAddress();
                        ThingUID thingUID = new ThingUID(THING_TYPE_HUB, host.replace('.', '_'));
                        DiscoveryResult hub = DiscoveryResultBuilder.create(thingUID)
                                .withProperty(HDPowerViewHubConfiguration.HOST, host)
                                .withRepresentationProperty(HDPowerViewHubConfiguration.HOST)
                                .withLabel("PowerView Hub (" + host + ")").build();
                        logger.debug("NetBios discovered hub on host '{}'", host);
                        thingDiscovered(hub);
                    }
                } catch (UnknownHostException e) {
                    // Nothing to do here - the host couldn't be found, likely because it doesn't
                    // exist
                }
            }
        };
    }
}
