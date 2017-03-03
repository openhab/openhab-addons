/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
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
import java.net.UnknownHostException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.milight.MilightBindingConstants;
import org.openhab.binding.milight.internal.discovery.ThingDiscoveryService;
import org.openhab.binding.milight.internal.protocol.MilightCommunication;
import org.openhab.binding.milight.internal.protocol.MilightDiscover;
import org.openhab.binding.milight.internal.protocol.MilightDiscover.DiscoverResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MilightBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author David Graeff - Initial contribution
 */
public class MilightBridgeHandler extends BaseBridgeHandler implements DiscoverResult {
    private Logger logger = LoggerFactory.getLogger(MilightBridgeHandler.class);
    private MilightDiscover discover;
    private MilightCommunication com;
    private ScheduledFuture<?> discoverTimer;
    private int refrehInterval;
    private String bridgeid;
    private ThingDiscoveryService thingDiscoveryService;

    public MilightBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // There is nothing to handle in the bridge handler
    }

    @Override
    public void thingUpdated(Thing thing) {
        this.thing = thing;
        if (com == null) {
            return;
        }

        boolean reconnect = false;

        // Create a new communication object if the user changed the IP configuration.
        Object host_config_obj = thing.getConfiguration().get(MilightBindingConstants.CONFIG_HOST_NAME);
        String host_config = ((host_config_obj instanceof String) ? (String) host_config_obj
                : (host_config_obj instanceof InetAddress) ? ((InetAddress) host_config_obj).getHostAddress() : null);
        if (host_config != null && !host_config.equals(com.getAddr().getHostAddress())) {
            reconnect = true;
        }

        // Create a new communication object if the user changed the bridge ID configuration.
        String id_config = (String) thing.getConfiguration().get(MilightBindingConstants.CONFIG_ID);
        if (id_config != null && !id_config.equals(com.getBridgeId())) {
            reconnect = true;
        }

        // Create a new communication object if the user changed the port configuration.
        BigDecimal port_config = (BigDecimal) thing.getConfiguration().get(MilightBindingConstants.CONFIG_PORT);
        if (port_config != null && port_config.intValue() > 0 && port_config.intValue() <= 65000
                && port_config.intValue() != com.getPort()) {
            reconnect = true;
        }

        if (reconnect) {
            createCommunicationObject();
        }

        BigDecimal interval_config = (BigDecimal) thing.getConfiguration().get(MilightBindingConstants.CONFIG_REFRESH);
        if (interval_config != null && interval_config.intValue() != refrehInterval) {
            setupRefreshTimer();
        }
    }

    /**
     * You need a CONFIG_HOST_NAME and CONFIG_ID for a milight bridge handler to initialize correctly.
     * The ID is a unique 12 character long ASCII based on the bridge MAC address (for example ACCF23A20164)
     * and is send as response for a discovery message.
     */
    @Override
    public void initialize() {
        thingDiscoveryService = new ThingDiscoveryService(thing.getUID(), this);
        thingDiscoveryService.start(bundleContext);

        createCommunicationObject();
        setupRefreshTimer();
    }

    private void createCommunicationObject() {
        Object host_config_obj = thing.getConfiguration().get(MilightBindingConstants.CONFIG_HOST_NAME);
        String host_config = ((host_config_obj instanceof String) ? (String) host_config_obj
                : (host_config_obj instanceof InetAddress) ? ((InetAddress) host_config_obj).getHostAddress() : null);

        InetAddress addr = null;

        try {
            addr = InetAddress.getByName(host_config);
        } catch (UnknownHostException ignored) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No address known!");
            return;
        }

        bridgeid = (String) thing.getConfiguration().get(MilightBindingConstants.CONFIG_ID);

        // Version 1/2 do not support response messages / detection. We therefore directly call bridgeDetected(addr).
        if (bridgeid == null || bridgeid.length() != 12) {
            bridgeid = null; // for the case length() != 12
            logger.warn("BridgeID not known. Version 2 fallback behaviour activated, no periodical refresh available!");
            bridgeDetected(addr, "");
            return;
        }

        // The MilightDiscover class is used here for periodically ping the device and update the state.
        if (discover != null) {
            discover.stopReceiving();
        }
        try {
            discover = new MilightDiscover(addr, this, 1000, 3);
            discover.start();
        } catch (SocketException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            return;
        }

        discover.sendDiscover(scheduler);
    }

    /**
     * Sets up the periodically refresh via the scheduler. If the user set CONFIG_REFRESH to 0, no refresh will be
     * done.
     */
    private void setupRefreshTimer() {
        // Version 1/2 do not support response messages / detection.
        if (bridgeid == null) {
            return;
        }

        if (discoverTimer != null) {
            discoverTimer.cancel(true);
        }

        BigDecimal interval_config = (BigDecimal) thing.getConfiguration().get(MilightBindingConstants.CONFIG_REFRESH);
        if (interval_config == null || interval_config.intValue() == 0) {
            refrehInterval = 0;
            return;
        }

        refrehInterval = interval_config.intValue();

        // This timer will do the state update periodically.
        discoverTimer = scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                discover.sendDiscover(scheduler);
            }
        }, refrehInterval, refrehInterval, TimeUnit.MINUTES);
    }

    @Override
    public void dispose() {
        if (discover != null) {
            discover.stopReceiving();
        }
    }

    /**
     * @return Return the protocol communication object. This may be null
     *         if the bridge is offline.
     */
    public MilightCommunication getCommunication() {
        return com;
    }

    @Override
    public void bridgeDetected(InetAddress addr, String id) {
        BigDecimal port_config = (BigDecimal) thing.getConfiguration().get(MilightBindingConstants.CONFIG_PORT);
        if (port_config == null || port_config.intValue() < 0 || port_config.intValue() > 65000) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No valid port set!");
            return;
        }

        try {
            com = new MilightCommunication(addr, port_config.intValue(), id);
        } catch (SocketException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getLocalizedMessage());
            return;
        }

        // A bridge may be connected/paired to white/rgbw/rgb bulbs. Unfortunately the bridge does
        // not know which bulbs are present and the bulbs do not have a bidirectional communication.
        // Therefore we present the user all possible bulbs
        // (4 groups each for white/rgbw and 1 for the obsolete rgb bulb ).
        thingDiscoveryService.addDevice(
                new ThingUID(MilightBindingConstants.WHITE_THING_TYPE, this.getThing().getUID(), "0"),
                "White group all");
        thingDiscoveryService.addDevice(
                new ThingUID(MilightBindingConstants.WHITE_THING_TYPE, this.getThing().getUID(), "1"), "White group 1");
        thingDiscoveryService.addDevice(
                new ThingUID(MilightBindingConstants.WHITE_THING_TYPE, this.getThing().getUID(), "2"), "White group 2");
        thingDiscoveryService.addDevice(
                new ThingUID(MilightBindingConstants.WHITE_THING_TYPE, this.getThing().getUID(), "3"), "White group 3");
        thingDiscoveryService.addDevice(
                new ThingUID(MilightBindingConstants.WHITE_THING_TYPE, this.getThing().getUID(), "4"), "White group 4");

        thingDiscoveryService.addDevice(
                new ThingUID(MilightBindingConstants.RGB_THING_TYPE, this.getThing().getUID(), "5"),
                "Color Leds (old, without white)");

        thingDiscoveryService.addDevice(
                new ThingUID(MilightBindingConstants.RGB_THING_TYPE, this.getThing().getUID(), "6"), "Color group all");
        thingDiscoveryService.addDevice(
                new ThingUID(MilightBindingConstants.RGB_THING_TYPE, this.getThing().getUID(), "7"), "Color group 1");
        thingDiscoveryService.addDevice(
                new ThingUID(MilightBindingConstants.RGB_THING_TYPE, this.getThing().getUID(), "8"), "Color group 2");
        thingDiscoveryService.addDevice(
                new ThingUID(MilightBindingConstants.RGB_THING_TYPE, this.getThing().getUID(), "9"), "Color group 3");
        thingDiscoveryService.addDevice(
                new ThingUID(MilightBindingConstants.RGB_THING_TYPE, this.getThing().getUID(), "10"), "Color group 4");

        updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, bridgeid != null ? "" : "V2 compatibility mode");
    }

    @Override
    public void noBridgeDetected() {
        updateStatus(ThingStatus.OFFLINE);
    }
}
