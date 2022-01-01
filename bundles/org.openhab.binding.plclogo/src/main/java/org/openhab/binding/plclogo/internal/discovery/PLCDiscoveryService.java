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
package org.openhab.binding.plclogo.internal.discovery;

import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.THING_TYPE_DEVICE;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.net.util.SubnetUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.model.script.actions.Ping;
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

    private static final String LOGO_HOST = "address";
    private static final int LOGO_PORT = 102;

    private static final int CONNECTION_TIMEOUT = 500;
    private static final int DISCOVERY_TIMEOUT = 30;

    private final Runnable scanner = () -> {
        logger.debug("Start scan for LOGO! bridge");

        Enumeration<NetworkInterface> devices = null;
        try {
            devices = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException exception) {
            logger.warn("LOGO! bridge discovering: {}.", exception.toString());
        }

        while ((devices != null) && devices.hasMoreElements()) {
            NetworkInterface device = devices.nextElement();
            try {
                if (!device.isUp() || device.isLoopback()) {
                    continue;
                }
            } catch (SocketException exception) {
                logger.warn("LOGO! bridge discovering: {}.", exception.toString());
                continue;
            }
            for (InterfaceAddress iface : device.getInterfaceAddresses()) {
                InetAddress inetAddress = iface.getAddress();
                if (inetAddress instanceof Inet4Address) {
                    String prefix = String.valueOf(iface.getNetworkPrefixLength());
                    SubnetUtils utilities = new SubnetUtils(inetAddress.getHostAddress() + "/" + prefix);
                    for (String address : utilities.getInfo().getAllAddresses()) {
                        try {
                            if (Ping.checkVitality(address, LOGO_PORT, CONNECTION_TIMEOUT)) {
                                logger.debug("LOGO! device found at: {}.", address);

                                ThingUID thingUID = new ThingUID(THING_TYPE_DEVICE, address.replace('.', '_'));
                                DiscoveryResultBuilder builder = DiscoveryResultBuilder.create(thingUID);
                                builder.withProperty(LOGO_HOST, address).withLabel(String.format("Logo %s", address));
                                builder.withRepresentationProperty(LOGO_HOST);
                                thingDiscovered(builder.build());
                            }
                        } catch (IOException exception) {
                            logger.debug("LOGO! device not found at: {}.", address);
                        }
                    }
                }
            }
        }
    };
    private @Nullable ScheduledFuture<?> scanJob;

    /**
     * Constructor.
     */
    public PLCDiscoveryService() {
        super(Collections.singleton(THING_TYPE_DEVICE), DISCOVERY_TIMEOUT, true);
    }

    @Override
    protected void startBackgroundDiscovery() {
        stopBackgroundDiscovery();
        scanJob = scheduler.scheduleWithFixedDelay(scanner, 0, 60, TimeUnit.SECONDS);
        super.startBackgroundDiscovery();
    }

    @Override
    protected void stopBackgroundDiscovery() {
        ScheduledFuture<?> scanFuture = scanJob;
        if (scanFuture != null) {
            scanFuture.cancel(true);
        }
        scanJob = null;
        super.stopBackgroundDiscovery();
    }

    @Override
    protected void startScan() {
        scheduler.execute(scanner);
    }
}
