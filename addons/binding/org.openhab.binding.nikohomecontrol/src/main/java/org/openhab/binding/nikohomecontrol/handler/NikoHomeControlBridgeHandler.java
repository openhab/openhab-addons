/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nikohomecontrol.handler;

import static org.openhab.binding.nikohomecontrol.NikoHomeControlBindingConstants.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
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
        InetAddress addr = getAddr();
        int port = getPort();

        logger.debug("Niko Home Control: bridge handler host {}, port {}", addr, port);

        if (addr != null) {
            createCommunicationObject(addr, port);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Niko Home Control: cannot resolve bridge IP with hostname " + config.get(CONFIG_HOST_NAME));
        }

    }

    /**
     * Create communication object to Niko Home Control IP-interface and start communication.
     * Trigger discovery when communication setup is successful.
     *
     * @param addr IP address of Niko Home Control IP-interface
     * @param port
     */
    private void createCommunicationObject(InetAddress addr, int port) {
        Configuration config = this.getConfig();

        scheduler.submit(new Runnable() {

            @Override
            public void run() {
                nhcComm = new NikoHomeControlCommunication();

                // Set callback from NikoHomeControlCommunication object to this bridge to be able to take bridge
                // offline when non-resolvable communication error occurs.
                setBridgeCallBack();

                nhcComm.startCommunication();
                if (!nhcComm.communicationActive()) {
                    nhcComm = null;
                    bridgeOffline();
                    return;
                }

                updateProperties();

                updateStatus(ThingStatus.ONLINE);

                Integer refreshInterval = ((Number) config.get(CONFIG_REFRESH)).intValue();
                setupRefreshTimer(refreshInterval);

                if (nhcDiscovery != null) {
                    nhcDiscovery.discoverDevices();
                } else {
                    logger.debug("Niko Home Control: cannot discover actions, discovery service not started");
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

                nhcComm.restartCommunication();
                if (!nhcComm.communicationActive()) {
                    logger.debug("Niko Home Control: communication socket error");
                    bridgeOffline();
                    return;
                }

                updateProperties();

                updateStatus(ThingStatus.ONLINE);
            }
        }, refreshInterval, refreshInterval, TimeUnit.MINUTES);

    }

    /**
     * Take bridge offline when error in communication with Niko Home Control IP-interface. This method can also be
     * called directly from {@link NikoHomeControlCommunication} object.
     */
    public void bridgeOffline() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                "Niko Home Control: error starting bridge connection");
    }

    /**
     * Put bridge online when error in communication resolved.
     */
    public void bridgeOnline() {
        updateProperties();
        updateStatus(ThingStatus.ONLINE);

    }

    /**
     * Update bridge properties with properties returned from Niko Home Control Controller, so they can be made visible
     * in PaperUI.
     */
    private void updateProperties() {
        Map<String, String> properties = new HashMap<>();

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
        if (this.refreshTimer != null) {
            this.refreshTimer.cancel(true);
        }
        this.refreshTimer = null;
        if (this.nhcComm != null) {
            nhcComm.stopCommunication();
        }
        this.nhcComm = null;
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        Configuration configuration = editConfiguration();
        for (Entry<String, Object> configurationParmeter : configurationParameters.entrySet()) {
            configuration.put(configurationParmeter.getKey(), configurationParmeter.getValue());
        }
        updateConfiguration(configuration);

        scheduler.submit(new Runnable() {

            @Override
            public void run() {
                nhcComm.restartCommunication();
                if (!nhcComm.communicationActive()) {
                    bridgeOffline();
                    return;
                }

                updateProperties();

                updateStatus(ThingStatus.ONLINE);

                Integer refreshInterval = ((Number) configuration.get(CONFIG_REFRESH)).intValue();
                setupRefreshTimer(refreshInterval);

            }
        });
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
     * Send a trigger from an alarm received from Niko Home Control.
     *
     * @param Niko Home Control alarm message
     */
    public void triggerAlarm(String alarmText) {
        triggerChannel(CHANNEL_ALARM, alarmText);
        updateStatus(ThingStatus.ONLINE);
    }

    /**
     * Send a trigger from a notice received from Niko Home Control.
     *
     * @param Niko Home Control alarm message
     */
    public void triggerNotice(String alarmText) {
        triggerChannel(CHANNEL_NOTICE, alarmText);
        updateStatus(ThingStatus.ONLINE);
    }

    /**
     * Get the Niko Home Control communication object.
     *
     * @return Niko Home Control communication object
     */
    public NikoHomeControlCommunication getCommunication() {
        return this.nhcComm;
    }

    /**
     * Get the IP-address of the Niko Home Control IP-interface.
     *
     * @return the addr
     */
    public InetAddress getAddr() {
        Configuration config = this.getConfig();
        InetAddress addr = null;
        try {
            addr = InetAddress.getByName((String) config.get(CONFIG_HOST_NAME));
        } catch (UnknownHostException e) {
            logger.debug("Niko Home Control: Cannot resolve hostname {} to IP adress", config.get(CONFIG_HOST_NAME));
        }
        return addr;
    }

    /**
     * Get the listening port of the Niko Home Control IP-interface.
     *
     * @return the port
     */
    public int getPort() {
        Configuration config = this.getConfig();
        return ((Number) config.get(CONFIG_PORT)).intValue();
    }
}
