/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

package org.openhab.binding.qbus.internal;

import static org.openhab.binding.qbus.internal.QbusBindingConstants.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.qbus.internal.protocol.QbusCommunication;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link QbusBridgeHandler} is the handler for a Qbus controller
 *
 * @author Koen Schockaert - Initial Contribution
 */
public class QbusBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(QbusBridgeHandler.class);

    private QbusCommunication qbusComm;

    private ScheduledFuture<?> refreshTimer;

    public QbusBridgeHandler(Bridge Bridge) {
        super(Bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // There is nothing to handle in the bridge handler
    }

    @Override
    public void initialize() {
        logger.debug("QBUS: initializing bridge handler");

        Configuration config = this.getConfig();
        InetAddress addr = getAddr();
        int port = getPort();

        logger.debug("Qbus: bridge handler host {}, port {}", addr, port);

        if (addr != null) {
            createCommunicationObject(addr, port);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Qbus: cannot resolve bridge IP with hostname " + config.get(CONFIG_HOST_NAME));
        }
    }

    /**
     * Create communication object to Qbus server and start communication.
     *
     * @param addr : IP address of Qbus server
     * @param port : Communication port of QbusServer
     * @param sn : Serial number of Controller
     */
    private void createCommunicationObject(InetAddress addr, int port) {
        Configuration config = this.getConfig();
        scheduler.submit(() -> {
            qbusComm = new QbusCommunication();

            // Set callback from Qbus object to this bridge to be able to take bridge
            // offline when non-resolvable communication error occurs.
            setBridgeCallBack();

            qbusComm.startCommunication();
            if (!qbusComm.communicationActive()) {
                qbusComm = null;
                bridgeOffline();
                return;
            }

            updateStatus(ThingStatus.ONLINE);

            Integer refreshInterval = ((Number) config.get(CONFIG_REFRESH)).intValue();
            setupRefreshTimer(refreshInterval);

        });
    }

    private void setBridgeCallBack() {
        this.qbusComm.setBridgeCallBack(this);
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
        logger.debug("Qbus: Checking for Client communication every {} min", refreshInterval);
        this.refreshTimer = scheduler.scheduleWithFixedDelay(() -> {
            logger.debug("Qbus: check communication after timerinterval");

            if (!qbusComm.communicationActive()) {
                logger.debug("Qbus: Restarting communication");
                qbusComm.restartCommunication();

                if (!qbusComm.communicationActive()) {
                    qbusComm = null;
                    bridgeOffline();
                    updateStatus(ThingStatus.OFFLINE);
                    return;
                }

                // updateStatus(ThingStatus.ONLINE);
                updateStatus(ThingStatus.ONLINE);
            } else {
                logger.debug("Qbus: Communication still active");
            }

        }, refreshInterval, refreshInterval, TimeUnit.MINUTES);
    }

    /**
     * Take bridge offline when error in communication with Qbus server. This method can also be
     * called directly from {@link QbusCommunication} object.
     */
    public void bridgeOffline() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                "Qbus: error starting bridge connection");
    }

    /**
     * Put bridge online when error in communication resolved.
     */
    public void bridgeOnline() {
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public boolean isInitialized() {
        return true;
    }

    @Override
    public void dispose() {
        if (this.refreshTimer != null) {
            this.refreshTimer.cancel(true);
        }
        this.refreshTimer = null;
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        Configuration configuration = editConfiguration();
        for (Entry<String, Object> configurationParmeter : configurationParameters.entrySet()) {
            configuration.put(configurationParmeter.getKey(), configurationParmeter.getValue());
        }
        updateConfiguration(configuration);

        scheduler.submit(() -> {

            updateStatus(ThingStatus.ONLINE);

            Integer refreshInterval = ((Number) configuration.get(CONFIG_REFRESH)).intValue();
            setupRefreshTimer(refreshInterval);
        });
    }

    /**
     * Get the Qbus communication object.
     *
     * @return Qbus communication object
     */
    public QbusCommunication getCommunication() {
        return this.qbusComm;
    }

    /**
     * Get the IP-address of the Qbus server.
     *
     * @return the addr
     */
    public InetAddress getAddr() {
        Configuration config = this.getConfig();
        InetAddress addr = null;
        try {
            addr = InetAddress.getByName((String) config.get(CONFIG_HOST_NAME));
        } catch (UnknownHostException e) {
            logger.debug("Qbus: Cannot resolve hostname {} to IP adress", config.get(CONFIG_HOST_NAME));
        }
        return addr;
    }

    /**
     * Get the listening port of the Qbus server.
     *
     * @return the port
     */
    public int getPort() {
        Configuration config = this.getConfig();
        return ((Number) config.get(CONFIG_PORT)).intValue();
    }

    /**
     * Get the serial nr of the Qbus server.
     *
     * @return the sn
     */
    public String getSn() {
        Configuration config = this.getConfig();
        return ((String) config.get(CONFIG_SN));
    }
}
