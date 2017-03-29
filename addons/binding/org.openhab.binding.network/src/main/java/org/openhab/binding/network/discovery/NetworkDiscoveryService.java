/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.network.discovery;

import static org.openhab.binding.network.NetworkBindingConstants.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.network.service.DiscoveryCallback;
import org.openhab.binding.network.service.NetworkUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NetworkDiscoveryService} is responsible for discovering devices on
 * the current Network. It uses every Network Interface which is connected to a network.
 *
 * @author Marc Mettke - Initial contribution
 */
public class NetworkDiscoveryService extends AbstractDiscoveryService implements DiscoveryCallback {
    private final Logger logger = LoggerFactory.getLogger(NetworkDiscoveryService.class);
    private ExecutorService executorService = null;
    final static int PING_TIMEOUT_IN_MS = 500;
    private int scanningNetworkSize = 0;

    public NetworkDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, 900, false);
    }

    /**
     * Starts the DiscoveryThread for each IP on each interface on the network
     *
     */
    @Override
    protected void startScan() {
        if (executorService != null) {
            stopScan();
        }

        logger.debug("Starting Discovery");
        LinkedHashSet<String> networkIPs = NetworkUtils.getNetworkIPs(NetworkUtils.getInterfaceIPs());
        scanningNetworkSize = networkIPs.size();
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 10);

        for (Iterator<String> it = networkIPs.iterator(); it.hasNext();) {
            final String ip = it.next();
            executorService.execute(new PingRunnable(ip, this));
        }
        stopScan();
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        if (executorService == null) {
            return;
        }

        try {
            executorService.awaitTermination(PING_TIMEOUT_IN_MS * scanningNetworkSize, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
        }
        executorService.shutdown();
        executorService = null;

    }

    /**
     * Submit newly discovered devices. This method is called by the spawned threads in {@link startScan}.
     *
     * @param ip The device IP, received by the
     */
    @Override
    public void newDevice(String ip) {
        logger.info("Found " + ip);

        // uid must not contains dots
        ThingUID uid = new ThingUID(THING_TYPE_DEVICE, ip.replace('.', '_'));

        if (uid != null) {
            Map<String, Object> properties = new HashMap<>(1);
            properties.put(PARAMETER_HOSTNAME, ip);
            DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties)
                    .withLabel("Network Device (" + ip + ")").build();
            thingDiscovered(result);
        }
    }
}
