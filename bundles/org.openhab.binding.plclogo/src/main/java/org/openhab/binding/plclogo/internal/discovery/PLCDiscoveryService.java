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
package org.openhab.binding.plclogo.internal.discovery;

import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.THING_TYPE_DEVICE;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.model.script.actions.Ping;
import org.openhab.core.net.NetUtil;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PLCDiscoveryService} is responsible for discovering devices on
 * the current Network. It uses every Network Interface which is connected to a network.
 * Based on network binding discovery service.
 *
 * @author Alexander Falkenstern - Initial contribution
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, configurationPid = "discovery.plclogo")
public class PLCDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(PLCDiscoveryService.class);
    private static final Set<ThingTypeUID> THING_TYPES_UIDS = Set.of(THING_TYPE_DEVICE);

    private static final String LOGO_HOST = "address";
    private static final int LOGO_PORT = 102;

    private static final int CONNECTION_TIMEOUT = 500;
    private static final int DISCOVERY_TIMEOUT = 30;

    private final Runnable scanner = () -> {
        logger.debug("Start discovery for LOGO! bridge");
        for (final InetAddress address : NetUtil.getFullRangeOfAddressesToScan()) {
            final NetworkInterface device;
            try {
                device = NetworkInterface.getByInetAddress(address);
                if ((device == null) || !device.isUp() || device.isLoopback()) {
                    continue;
                }
            } catch (SocketException exception) {
                logger.warn("LOGO! bridge discovering: {}.", exception.toString());
                continue;
            }

            final String host = address.getHostAddress();
            try {
                if (Ping.checkVitality(host, LOGO_PORT, CONNECTION_TIMEOUT)) {
                    logger.debug("LOGO! device found at: {}.", host);

                    final ThingUID thingUID = new ThingUID(THING_TYPE_DEVICE, host.replace('.', '_'));
                    final DiscoveryResultBuilder builder = DiscoveryResultBuilder.create(thingUID);
                    builder.withProperty(LOGO_HOST, address).withLabel(String.format("Logo %s", address));
                    builder.withRepresentationProperty(LOGO_HOST);
                    thingDiscovered(builder.build());
                }
            } catch (IOException exception) {
                logger.debug("LOGO! device not found at: {}.", host);
            }
        }
    };
    private @Nullable ScheduledFuture<?> scanJob;

    /**
     * Constructor.
     */
    public PLCDiscoveryService() {
        super(THING_TYPES_UIDS, DISCOVERY_TIMEOUT, true);
    }

    @Override
    protected void startBackgroundDiscovery() {
        stopBackgroundDiscovery();
        scanJob = scheduler.scheduleWithFixedDelay(scanner, 0, 60, TimeUnit.SECONDS);
        super.startBackgroundDiscovery();
    }

    @Override
    protected void stopBackgroundDiscovery() {
        final ScheduledFuture<?> scanJob = this.scanJob;
        if (scanJob != null) {
            scanJob.cancel(true);
        }
        this.scanJob = null;
        super.stopBackgroundDiscovery();
    }

    @Override
    protected void startScan() {
        scheduler.execute(scanner);
    }
}
