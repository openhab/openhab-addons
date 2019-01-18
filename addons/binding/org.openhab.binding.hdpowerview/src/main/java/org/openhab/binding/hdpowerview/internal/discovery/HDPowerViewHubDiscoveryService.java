/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import java.net.UnknownHostException;
import java.util.Collections;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.hdpowerview.internal.HDPowerViewBindingConstants;
import org.openhab.binding.hdpowerview.internal.config.HDPowerViewHubConfiguration;
import org.osgi.service.component.annotations.Component;

import jcifs.netbios.NbtAddress;

/**
 * Discovers the HD Power View HUB by searching for a host advertised with the NetBIOS name PDBU-Hub3.0
 *
 * @author Andy Lintner - Initial contribution
 */
@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.hdpowerview")
public class HDPowerViewHubDiscoveryService extends AbstractDiscoveryService {

    private final Runnable scanner;
    private ScheduledFuture<?> backgroundFuture;

    public HDPowerViewHubDiscoveryService() {
        super(Collections.singleton(HDPowerViewBindingConstants.THING_TYPE_HUB), 600, true);
        scanner = createScanner();
    }

    @Override
    protected void startScan() {
        scheduler.execute(scanner);
    }

    @Override
    protected void startBackgroundDiscovery() {
        if (backgroundFuture != null && !backgroundFuture.isDone()) {
            backgroundFuture.cancel(true);
            backgroundFuture = null;
        }
        backgroundFuture = scheduler.scheduleWithFixedDelay(scanner, 0, 60, TimeUnit.SECONDS);
    }

    @Override
    protected void stopBackgroundDiscovery() {
        if (backgroundFuture != null && !backgroundFuture.isDone()) {
            backgroundFuture.cancel(true);
            backgroundFuture = null;
        }
        super.stopBackgroundDiscovery();
    }

    private Runnable createScanner() {
        return () -> {
            try {
                NbtAddress address = NbtAddress.getByName(HDPowerViewBindingConstants.NETBIOS_NAME);
                if (address != null) {
                    String host = address.getInetAddress().getHostAddress();
                    ThingUID thingUID = new ThingUID(HDPowerViewBindingConstants.THING_TYPE_HUB,
                            host.replace('.', '_'));
                    DiscoveryResult result = DiscoveryResultBuilder.create(thingUID)
                            .withProperty(HDPowerViewHubConfiguration.HOST, host)
                            .withLabel("PowerView Hub (" + host + ")").build();
                    thingDiscovered(result);
                }
            } catch (UnknownHostException e) {
                // Nothing to do here - the host couldn't be found, likely because it doesn't exist
            }
        };
    }

}
