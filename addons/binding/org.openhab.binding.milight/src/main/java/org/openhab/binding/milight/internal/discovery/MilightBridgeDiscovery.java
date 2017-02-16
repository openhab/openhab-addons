/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.milight.internal.discovery;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.milight.MilightBindingConstants;
import org.openhab.binding.milight.internal.MilightHandlerFactory;
import org.openhab.binding.milight.internal.protocol.MilightDiscover;
import org.openhab.binding.milight.internal.protocol.MilightDiscover.DiscoverResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MilightHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author David Graeff - Initial contribution
 */
public class MilightBridgeDiscovery extends AbstractDiscoveryService implements DiscoverResult {
    private ScheduledFuture<?> backgroundFuture;
    private final InetAddress broadcast;
    private Logger logger = LoggerFactory.getLogger(MilightBridgeDiscovery.class);
    private MilightDiscover receiveThread;

    class DetectTask extends TimerTask {
        @Override
        public void run() {
            receiveThread.sendDiscover(scheduler);
        }
    }

    private void startDiscoveryService() {
        if (receiveThread == null) {
            try {
                receiveThread = new MilightDiscover(broadcast, this, 50, 2000 / 50);
            } catch (SocketException e) {
                logger.error("Opening a socket for the milight discovery service failed. " + e.getLocalizedMessage());
                return;
            }
            receiveThread.start();
        }
    }

    public MilightBridgeDiscovery() throws IllegalArgumentException, UnknownHostException {
        super(MilightBindingConstants.BRIDGE_THING_TYPES_UIDS, 2, true);
        byte[] addr = { (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff };
        broadcast = InetAddress.getByAddress(addr);
        startDiscoveryService();
    }

    @Override
    protected void startBackgroundDiscovery() {
        if (backgroundFuture != null) {
            return;
        }

        startDiscoveryService();

        backgroundFuture = scheduler.scheduleAtFixedRate(new DetectTask(), 50, 60000 * 30, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void stopBackgroundDiscovery() {
        stopScan();
        if (backgroundFuture != null) {
            backgroundFuture.cancel(false);
            backgroundFuture = null;
        }
        if (receiveThread != null) {
            receiveThread.stopReceiving();
        }
        receiveThread = null;
    }

    @Override
    public void bridgeDetected(InetAddress addr, String id) {
        logger.debug("Milight bridge found " + addr.getHostName() + " " + id);
        ThingUID thingUID = new ThingUID(MilightBindingConstants.BINDING_ID, "bridge", id);
        String label = "Milight Bridge " + id;
        Map<String, Object> properties = new TreeMap<>();
        properties.put((String) MilightBindingConstants.CONFIG_ID, id);
        properties.put((String) MilightBindingConstants.CONFIG_HOST_NAME, addr.getHostAddress());
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withLabel(label)
                .withProperties(properties).build();
        thingDiscovered(discoveryResult);
    }

    @Override
    protected void startScan() {
        startDiscoveryService();
        receiveThread.sendDiscover(scheduler);
    }

    @Override
    protected synchronized void stopScan() {
        if (receiveThread != null) {
            receiveThread.stopResend();
        }
        super.stopScan();
    }

    @Override
    public void noBridgeDetected() {

    }
}

