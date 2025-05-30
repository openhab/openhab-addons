/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hdpowerview.internal.config.HDPowerViewHubConfiguration;
import org.openhab.binding.hdpowerview.internal.exceptions.HubException;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
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
    private final HDPowerviewPropertyGetter propertyGetter;

    @Activate
    public HDPowerViewHubDiscoveryService(@Reference HDPowerviewPropertyGetter propertyGetter) {
        super(Set.of(THING_TYPE_HUB), 60, true);
        scanner = createScanner();
        this.propertyGetter = propertyGetter;
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
                        String serial = propertyGetter.getSerialNumberApiV1(host);
                        ThingUID thingUID = new ThingUID(THING_TYPE_HUB, serial);
                        String label = String.format("@text/%s [\"%s\", \"%s\"]", LABEL_KEY_HUB, "1", host);
                        DiscoveryResult hub = DiscoveryResultBuilder.create(thingUID)
                                .withProperty(HDPowerViewHubConfiguration.HOST, host)
                                .withProperty(Thing.PROPERTY_SERIAL_NUMBER, serial)
                                .withRepresentationProperty(Thing.PROPERTY_SERIAL_NUMBER).withLabel(label).build();
                        logger.debug("NetBios discovered Gen 1 hub '{}' on host '{}'", thingUID, host);
                        thingDiscovered(hub);
                    }
                } catch (HubException e) {
                    logger.debug("Error discovering hub", e);
                } catch (UnknownHostException e) {
                    // Nothing to do here - the host couldn't be found, likely because it doesn't exist
                }
            }
        };
    }
}
