/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nikohomecontrol.handler;

import static org.openhab.binding.nikohomecontrol.NikoHomeControlBindingConstants.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.nikohomecontrol.internal.discovery.ThingDiscoveryService;
import org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlCommunication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link NikoHomeControlBridgeHandler} is the handler for a Niko Home Control Gateway and connects it to
 * the framework.
 *
 * @author Mark Herwege
 */
public class NikoHomeControlBridgeHandler extends BaseBridgeHandler {

    private Logger logger = LoggerFactory.getLogger(NikoHomeControlBridgeHandler.class);

    private NikoHomeControlCommunication nhcComm = null;

    private ScheduledFuture<?> refreshTimer;

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
        InetAddress addr = null;
        int port;

        Configuration config = this.getConfig();
        // workaround for int port not being read back as int from configuration, remove trailing .0
        logger.debug("Niko Home Control: bridge handler port {}", config.get(CONFIG_PORT).toString());
        String portString = config.get(CONFIG_PORT).toString();
        int portStringDotPos = portString.indexOf(".");
        if (portStringDotPos > 0) {
            portString = portString.substring(0, portStringDotPos);
        }
        port = Integer.parseInt(portString);
        try {
            if (config.get(CONFIG_HOST_NAME) != null) {
                logger.debug("Niko Home Control: bridge handler host {}", config.get(CONFIG_HOST_NAME).toString());
                addr = InetAddress.getByName(config.get(CONFIG_HOST_NAME).toString());
            }
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Niko Home Control: cannot resolve bridge IP");
            return;
        }
        BigDecimal intervalConfig = (BigDecimal) config.get(CONFIG_REFRESH);
        int refreshInterval;
        if (intervalConfig == null) {
            refreshInterval = 0;
        } else {
            refreshInterval = intervalConfig.intValue();
        }

        InetAddress nhcAddr = addr;

        createCommunicationObject(nhcAddr, port);
        setupRefreshTimer(refreshInterval);

    }

    /**
     * Create communication object to Niko Home Control gateway and start communication.
     * Trigger discovery when communication setup is successful.
     *
     * @param nhcAddr IP address or subnet for gateway
     * @param port
     */
    private void createCommunicationObject(InetAddress nhcAddr, int port) {

        ThingDiscoveryService nhcDiscovery = new ThingDiscoveryService(thing.getUID(), this);

        scheduler.submit(new Runnable() {

            @Override
            public void run() {
                try {
                    for (int i = 0; i < 3; i++) {
                        // try connecting max 3 times
                        nhcComm = new NikoHomeControlCommunication(port, nhcAddr);
                        if (nhcComm.communicationActive()) {
                            break;
                        }
                        nhcComm = null;
                    }
                    if (!nhcComm.communicationActive()) {
                        IOException e = new IOException("Niko Home Control: communication socket not open");
                        throw e;
                    }
                    thing.getConfiguration().put(CONFIG_HOST_NAME, nhcComm.getAddr().getHostAddress());
                    thing.getConfiguration().put(CONFIG_PORT, port);
                    HashMap<String, String> properties = new HashMap<String, String>();
                    properties.put("IP Address", nhcComm.getAddr().getHostAddress());
                    properties.put("Port", Integer.toString(port));
                    properties.put("swversion", nhcComm.getSwversion());
                    properties.put("api", nhcComm.getApi());
                    properties.put("time", nhcComm.getTime());
                    properties.put("language", nhcComm.getLanguage());
                    properties.put("currency", nhcComm.getCurrency());
                    properties.put("units", nhcComm.getUnits());
                    properties.put("DST", nhcComm.getDst());
                    properties.put("TZ", nhcComm.getTz());
                    properties.put("lastenergyerase", nhcComm.getLastenergyerase());
                    properties.put("lastconfig", nhcComm.getLastconfig());
                    thing.setProperties(properties);
                    updateStatus(ThingStatus.ONLINE);

                    nhcDiscovery.start(bundleContext);
                    nhcDiscovery.discoverDevices();

                } catch (IOException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                            "Niko Home Control: error initializing bridge handler");
                }
            }
        });

    }

    /**
     * Schedule future communication refresh.
     *
     * @param interval_config Time before refresh in minutes.
     */
    private void setupRefreshTimer(int refreshInterval) {

        if (refreshTimer != null) {
            refreshTimer.cancel(true);
            refreshTimer = null;
        }

        if (refreshInterval == 0) {
            return;
        }

        // This timer will restart the bridge connection periodically
        logger.debug("Niko Home Control: restart bridge connection every {} min", refreshInterval);
        refreshTimer = scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                logger.debug("Niko Home Control: restart communication at scheduled time");
                updateStatus(ThingStatus.OFFLINE);
                nhcComm.stopCommunication(true);
                thing.setProperty("IP Address", nhcComm.getAddr().getHostAddress());
                updateStatus(ThingStatus.ONLINE);
            }
        }, refreshInterval, refreshInterval, TimeUnit.MINUTES);
    }

    @Override
    public void dispose() {
        nhcComm.stopCommunication(false);
        nhcComm = null;
    }

    /**
     * Get the Niko Home Control communication object.
     *
     * @return Niko Home Control communication object
     */
    public NikoHomeControlCommunication getCommunication() {
        return nhcComm;
    }

}
