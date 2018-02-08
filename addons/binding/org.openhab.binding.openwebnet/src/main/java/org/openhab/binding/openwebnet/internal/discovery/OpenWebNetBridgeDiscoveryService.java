/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.openwebnet.internal.discovery;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.openwebnet.OpenWebNetBindingConstants;
import org.openhab.binding.openwebnet.handler.OpenWebNetBridgeHandler;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.NRSerialPort;

/**
 *
 * @author Antoine Laydier
 *
 */
@Component(immediate = true, service = DiscoveryService.class, configurationPid = "discovery.openwebnetbridge")
@NonNullByDefault
public class OpenWebNetBridgeDiscoveryService extends AbstractDiscoveryService {

    // Needed for Maven
    @SuppressWarnings("null")
    private Logger logger = LoggerFactory.getLogger(OpenWebNetBridgeDiscoveryService.class);

    private static final int TIMEOUT = 10;

    // Needed for Maven
    @SuppressWarnings("null")
    private final @NonNull ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();;

    public OpenWebNetBridgeDiscoveryService() throws IllegalArgumentException {
        super(OpenWebNetBridgeHandler.SUPPORTED_THING_TYPES, TIMEOUT, true);
    }

    @Override
    protected void startScan() {
        logger.debug("startScan called");
        executor.schedule(this::scan, 0, TimeUnit.SECONDS);
    }

    // Needed by Maven
    @SuppressWarnings("null")
    private synchronized void scan() {
        logger.debug("Scanning started...");

        for (String port : NRSerialPort.getAvailableSerialPorts()) {
            logger.debug("Scanning port {} ", port);
            hubDiscovered(port);
            if (Thread.interrupted()) {
                logger.debug("Scan interrupted");
                return;
            }
        }
        logger.debug("Scan completed.");
        stopScan();
    }

    private void hubDiscovered(String systemName) {
        int pos = systemName.lastIndexOf("/");
        String name = systemName.substring(pos + 1, systemName.length());
        logger.debug("Adding Bridge \"{}\" at port {}", name, systemName);
        Map<@NonNull String, @NonNull Object> properties = new HashMap<>(1);
        properties.put(OpenWebNetBindingConstants.SERIAL_PORT, systemName);

        ThingUID uid = new ThingUID(OpenWebNetBindingConstants.THING_TYPE_BRIDGE, name);
        thingDiscovered(DiscoveryResultBuilder.create(uid).withProperties(properties)
                .withLabel("ZigBee OpenWebNet Bridge @ " + systemName).build());
    }
}
