/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nikohomecontrol.handler;

import static org.openhab.binding.nikohomecontrol.NikoHomeControlBindingConstants.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.nikohomecontrol.internal.discovery.NikoHomeControlDiscoveryService;
import org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlCommunication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link NikoHomeControlBridgeHandler} is the handler for a Niko Home Control IP-interface and connects it to
 * the framework.
 *
 * @author Mark Herwege
 */
public class NikoHomeControlBridgeHandler extends BaseBridgeHandler {

    private Logger logger = LoggerFactory.getLogger(NikoHomeControlBridgeHandler.class);

    private NikoHomeControlCommunication nhcComm;

    private ScheduledFuture<?> refreshTimer;

    private NikoHomeControlDiscoveryService nhcDiscovery;

    public NikoHomeControlBridgeHandler(Bridge nikoHomeControlBridge) {
        super(nikoHomeControlBridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // There is nothing to handle in the bridge handler
    }

    @Override
    public void initialize() {

        logger.debug("Niko Home Control: initializing bridge handler");

        Configuration config = this.getConfig();

        InetAddress addr = null;
        int port = ((Number) config.get(CONFIG_PORT)).intValue();
        InetAddress broadcastAddr = null;

        logger.debug("Niko Home Control: bridge handler port {}", port);

        if (config.get(CONFIG_HOST_NAME) != null) {
            try {
                // If hostname or address was provided in the configuration, try to use this to for bridge and give
                // error
                // when hostname parameter was not valid.
                // No hostname provided is a valid configuration, therefore allow null addr to pass through.
                logger.debug("Niko Home Control: configure bridge with host parameter {}",
                        config.get(CONFIG_HOST_NAME));
                addr = InetAddress.getByName((String) config.get(CONFIG_HOST_NAME));
            } catch (UnknownHostException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                        "Niko Home Control: cannot resolve bridge IP with hostname " + config.get(CONFIG_HOST_NAME));
                return;
            }
        } else if (config.get(CONFIG_BROADCAST_ADDRESS) != null) {
            try {
                logger.debug("Niko Home Control: configure bridge with broadcast parameter {}",
                        config.get(CONFIG_BROADCAST_ADDRESS));
                broadcastAddr = InetAddress.getByName((String) config.get(CONFIG_BROADCAST_ADDRESS));
            } catch (UnknownHostException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                        "Niko Home Control: cannot resolve broadcast address " + config.get(CONFIG_BROADCAST_ADDRESS));
                return;

            }
        } else {
            logger.debug("Niko Home Control: try to auto-discover bridge address");
        }

        createCommunicationObject(addr, port, broadcastAddr);

    }

    /**
     * Create communication object to Niko Home Control IP-interface and start communication.
     * Trigger discovery when communication setup is successful.
     *
     * @param addr IP address of Niko Home Control IP-interface or null
     * @param port
     */
    private void createCommunicationObject(InetAddress addr, int port, InetAddress broadcastAddr) {

        Configuration config = this.getConfig();

        scheduler.submit(new Runnable() {

            @Override
            public void run() {
                try {
                    nhcComm = new NikoHomeControlCommunication(addr, broadcastAddr);

                    nhcComm.startCommunication(port);
                    if (!nhcComm.communicationActive()) {
                        throw new IOException("Niko Home Control: communication socket error");
                    }

                    // Set callback from NikoHomeControlCommunication object to this bridge to be able to take bridge
                    // offline when non-resolvable communication error occurs.
                    setBridgeCallBack();

                    updateProperties();

                    updateStatus(ThingStatus.ONLINE);

                    Integer refreshInterval = ((Number) config.get(CONFIG_REFRESH)).intValue();
                    setupRefreshTimer(refreshInterval);

                    if (nhcDiscovery != null) {
                        nhcDiscovery.discoverDevices();
                    } else {
                        logger.debug("Niko Home Control: cannot discover, discovery service not started");
                    }

                } catch (IOException e) {
                    nhcComm = null;
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                            "Niko Home Control: error starting bridge connection");
                }
            }

        });

    }

    private void setBridgeCallBack() {
        this.nhcComm.setBridgeCallBack(this);
    }

    /**
     * Schedule future communication refresh.
     *
     * @param interval_config Time before refresh in minutes.
     */
    private void setupRefreshTimer(Integer refreshInterval) {

        if (this.refreshTimer != null) {
            this.refreshTimer.cancel(true);
            this.refreshTimer = null;
        }

        if ((refreshInterval == null) || (refreshInterval == 0)) {
            return;
        }

        // This timer will restart the bridge connection periodically
        logger.debug("Niko Home Control: restart bridge connection every {} min", refreshInterval);
        this.refreshTimer = scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {

                logger.debug("Niko Home Control: restart communication at scheduled time");

                try {
                    nhcComm.restartCommunication();
                    if (!nhcComm.communicationActive()) {
                        throw new IOException("Niko Home Control: communication socket error");
                    }

                    updateProperties();

                    updateStatus(ThingStatus.ONLINE);

                } catch (IOException e) {
                    bridgeOffline();
                }
            }
        }, refreshInterval, refreshInterval, TimeUnit.MINUTES);
    }

    /**
     * Take bridge offline when error in communication with Niko Home Control IP-interface. This method can also be
     * called directly from {@link NikoHomeControlCommunication} object.
     */
    public void bridgeOffline() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                "Niko Home Control: error restarting bridge connection");
    }

    /**
     * Put bridge online when error in communication resolved.
     */
    public void bridgeOnline() {
        updateStatus(ThingStatus.ONLINE);
    }

    /**
     * Update bridge properties with properties returned from Niko Home Control Controller, so they can be made visible
     * in PaperUI.
     */
    private void updateProperties() {

        Map<String, String> properties = new HashMap<>();

        properties.put("ipAddress", this.nhcComm.getAddr().getHostAddress());
        properties.put("port", Integer.toString(this.nhcComm.getPort()));
        properties.put("softwareVersion", this.nhcComm.getSystemInfo().getSwVersion());
        properties.put("apiVersion", this.nhcComm.getSystemInfo().getApi());
        properties.put("language", this.nhcComm.getSystemInfo().getLanguage());
        properties.put("currency", this.nhcComm.getSystemInfo().getCurrency());
        properties.put("units", this.nhcComm.getSystemInfo().getUnits());
        properties.put("tzOffset", this.nhcComm.getSystemInfo().getTz());
        properties.put("dstOffset", this.nhcComm.getSystemInfo().getDst());
        properties.put("configDate", this.nhcComm.getSystemInfo().getLastConfig());
        properties.put("energyEraseDate", this.nhcComm.getSystemInfo().getLastEnergyErase());
        properties.put("connectionStartDate", this.nhcComm.getSystemInfo().getTime());

        thing.setProperties(properties);

    }

    @Override
    public void dispose() {
        if (this.nhcComm != null) {
            this.nhcComm.stopCommunication();
        }
        this.nhcComm = null;
    }

    /**
     * Set discovery service handler to be able to start discovery after bridge initialization.
     *
     * @param nhcDiscovery
     */
    public void setNhcDiscovery(NikoHomeControlDiscoveryService nhcDiscovery) {
        this.nhcDiscovery = nhcDiscovery;
    }

    /**
     * Get the Niko Home Control communication object.
     *
     * @return Niko Home Control communication object
     */
    public NikoHomeControlCommunication getCommunication() {
        return this.nhcComm;
    }

}
