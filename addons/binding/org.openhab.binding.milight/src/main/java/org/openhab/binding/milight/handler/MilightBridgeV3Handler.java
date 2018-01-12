/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.milight.handler;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Map;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.milight.MilightBindingConstants;
import org.openhab.binding.milight.internal.protocol.MilightDiscover;
import org.openhab.binding.milight.internal.protocol.MilightDiscover.DiscoverResult;

/**
 * The {@link MilightBridgeV3Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author David Graeff <david.graeff@web.de>
 */
public class MilightBridgeV3Handler extends AbstractMilightBridgeHandler implements DiscoverResult {
    private MilightDiscover discover;

    public MilightBridgeV3Handler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        super.handleConfigurationUpdate(configurationParameters);

        if (com == null) {
            return;
        }

        BigDecimal refresh_time = (BigDecimal) thing.getConfiguration().get(MilightBindingConstants.CONFIG_REFRESH_SEC);
        if (refresh_time != null && refresh_time.intValue() != refrehIntervalSec) {
            setupRefreshTimer(refresh_time.intValue());
        }
    }

    @Override
    public void thingUpdated(Thing thing) {
        super.thingUpdated(thing);
    }

    @Override
    protected Runnable getKeepAliveRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                discover.sendDiscover(scheduler);
            }
        };
    }

    /**
     * Creates a discovery object and the send queue. The initial IP address may be null
     * or is not matching with the real IP address of the bridge. The discovery class will send
     * a broadcast packet to find the bridge with the respective bridge ID. The response in bridgeDetected()
     * may lead to a recreation of the send queue object.
     *
     * The keep alive timer that is also setup here, will send keep alive packets periodically.
     * If the bridge doesn't respond anymore (e.g. DHCP IP change), the initial session handshake
     * starts all over again.
     */
    @Override
    protected void startConnectAndKeepAlive(InetAddress addr) {
        dispose();

        int port = getPort(MilightBindingConstants.PORT_VER3);
        if (port == 0) {
            return;
        }

        com.setAddress(addr);
        com.setPort(port);
        com.start();

        // We recycle the discovery class and reuse it as a keep alive here.
        try {
            discover = new MilightDiscover(this, 1000, 3);
            discover.setFixedAddr(addr);
            discover.start();
        } catch (SocketException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            return;
        }

        discover.sendDiscover(scheduler);

        BigDecimal refresh_sec = (BigDecimal) thing.getConfiguration().get(MilightBindingConstants.CONFIG_REFRESH_SEC);
        setupRefreshTimer(refresh_sec == null ? 0 : refresh_sec.intValue());
    }

    @Override
    public void dispose() {
        if (discover != null) {
            discover.release();
        }
        super.dispose();
    }

    // A bridge may be connected/paired to white/rgbw/rgb bulbs. Unfortunately the bridge does
    // not know which bulbs are present and the bulbs do not have a bidirectional communication.
    // Therefore we present the user all possible bulbs
    // (4 groups each for white/rgbw and 1 for the obsolete rgb bulb ).
    private void addBulbsToDiscovery() {
        thingDiscoveryService.addDevice(
                new ThingUID(MilightBindingConstants.WHITE_THING_TYPE, this.getThing().getUID(), "0"), "All white");
        thingDiscoveryService.addDevice(
                new ThingUID(MilightBindingConstants.WHITE_THING_TYPE, this.getThing().getUID(), "1"),
                "White (Zone 1)");
        thingDiscoveryService.addDevice(
                new ThingUID(MilightBindingConstants.WHITE_THING_TYPE, this.getThing().getUID(), "2"),
                "White (Zone 2)");
        thingDiscoveryService.addDevice(
                new ThingUID(MilightBindingConstants.WHITE_THING_TYPE, this.getThing().getUID(), "3"),
                "White (Zone 3)");
        thingDiscoveryService.addDevice(
                new ThingUID(MilightBindingConstants.WHITE_THING_TYPE, this.getThing().getUID(), "4"),
                "White (Zone 4)");

        thingDiscoveryService.addDevice(
                new ThingUID(MilightBindingConstants.RGB_THING_TYPE, this.getThing().getUID(), "0"), "All color");
        thingDiscoveryService.addDevice(
                new ThingUID(MilightBindingConstants.RGB_THING_TYPE, this.getThing().getUID(), "1"), "Color (Zone 1)");
        thingDiscoveryService.addDevice(
                new ThingUID(MilightBindingConstants.RGB_THING_TYPE, this.getThing().getUID(), "2"), "Color (Zone 2)");
        thingDiscoveryService.addDevice(
                new ThingUID(MilightBindingConstants.RGB_THING_TYPE, this.getThing().getUID(), "3"), "Color (Zone 3)");
        thingDiscoveryService.addDevice(
                new ThingUID(MilightBindingConstants.RGB_THING_TYPE, this.getThing().getUID(), "4"), "Color (Zone 4)");

        // thingDiscoveryService.addDevice(
        // new ThingUID(MilightBindingConstants.RGB_V2_THING_TYPE, this.getThing().getUID(), "0"),
        // "Color Led (without white channel)");
    }

    @Override
    public void bridgeDetected(InetAddress addr, String id, int version) {
        if (!bridgeid.equals(id)) {
            logger.error("Bridges are not supposed to change their ID");
            dispose();
            return;
        }

        // IP address has changed, reestablish communication
        if (!addr.equals(com.getAddr())) {
            com.setAddress(addr);
            Configuration c = editConfiguration();
            c.put(MilightBindingConstants.CONFIG_HOST_NAME, addr.getHostAddress());
            updateConfiguration(c);
        }

        addBulbsToDiscovery();

        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void noBridgeDetected() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                "Bridge did not respond or the bridge's MAC address does not match with your configuration!");
    }
}
