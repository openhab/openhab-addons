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
package org.openhab.binding.nikohomecontrol.internal.handler;

import static org.openhab.binding.nikohomecontrol.internal.NikoHomeControlBindingConstants.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nikohomecontrol.internal.discovery.NikoHomeControlDiscoveryService;
import org.openhab.binding.nikohomecontrol.internal.protocol.NhcControllerEvent;
import org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlCommunication;
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
 * {@link NikoHomeControlBridgeHandler} is an abstract class representing a handler to all different interfaces to the
 * Niko Home Control System. {@link NikoHomeControlBridgeHandler1} or {@link NikoHomeControlBridgeHandler2} should be
 * used for the respective
 * version of Niko Home Control.
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
public abstract class NikoHomeControlBridgeHandler extends BaseBridgeHandler implements NhcControllerEvent {

    private final Logger logger = LoggerFactory.getLogger(NikoHomeControlBridgeHandler.class);

    protected @NonNullByDefault({}) NikoHomeControlBridgeConfig config;

    protected @Nullable NikoHomeControlCommunication nhcComm;

    private volatile @Nullable ScheduledFuture<?> refreshTimer;

    protected volatile @Nullable NikoHomeControlDiscoveryService nhcDiscovery;

    public NikoHomeControlBridgeHandler(Bridge nikoHomeControlBridge) {
        super(nikoHomeControlBridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // There is nothing to handle in the bridge handler
    }

    /**
     * Create communication object to Niko Home Control IP-interface and start communication.
     * Trigger discovery when communication setup is successful.
     */
    protected void startCommunication() {
        NikoHomeControlCommunication comm = nhcComm;
        if (comm == null) {
            bridgeOffline();
            return;
        }

        updateStatus(ThingStatus.UNKNOWN);

        scheduler.submit(() -> {
            comm.startCommunication();
            if (!comm.communicationActive()) {
                bridgeOffline();
                return;
            }

            updateProperties();

            updateStatus(ThingStatus.ONLINE);

            int refreshInterval = config.refresh;
            setupRefreshTimer(refreshInterval);

            NikoHomeControlDiscoveryService discovery = nhcDiscovery;
            if (discovery != null) {
                discovery.discoverDevices();
            } else {
                logger.debug("cannot discover devices, discovery service not started");
            }
        });
    }

    /**
     * Schedule future communication refresh.
     *
     * @param interval_config Time before refresh in minutes.
     */
    private void setupRefreshTimer(int refreshInterval) {
        ScheduledFuture<?> timer = refreshTimer;
        if (timer != null) {
            timer.cancel(true);
            refreshTimer = null;
        }

        if (refreshInterval == 0) {
            return;
        }

        // This timer will restart the bridge connection periodically
        logger.debug("restart bridge connection every {} min", refreshInterval);
        refreshTimer = scheduler.scheduleWithFixedDelay(() -> {
            logger.debug("restart communication at scheduled time");

            NikoHomeControlCommunication comm = nhcComm;
            if (comm != null) {
                comm.restartCommunication();
                if (!comm.communicationActive()) {
                    bridgeOffline();
                    return;
                }

                updateProperties();

                updateStatus(ThingStatus.ONLINE);
            }
        }, refreshInterval, refreshInterval, TimeUnit.MINUTES);
    }

    /**
     * Take bridge offline when error in communication with Niko Home Control IP-interface.
     */
    protected void bridgeOffline() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                "@text/offline.communication-error");
    }

    /**
     * Put bridge online when error in communication resolved.
     */
    public void bridgeOnline() {
        updateProperties();
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void controllerOffline(String message) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, message);
    }

    @Override
    public void controllerOnline() {
        bridgeOnline();

        int refreshInterval = config.refresh;
        if (refreshTimer == null) {
            setupRefreshTimer(refreshInterval);
        }
    }

    /**
     * Update bridge properties with properties returned from Niko Home Control Controller.
     */
    protected abstract void updateProperties();

    @Override
    public void dispose() {
        ScheduledFuture<?> timer = refreshTimer;
        if (timer != null) {
            timer.cancel(true);
        }
        refreshTimer = null;

        NikoHomeControlCommunication comm = nhcComm;
        if (comm != null) {
            comm.stopCommunication();
        }
        nhcComm = null;
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        NikoHomeControlCommunication comm = nhcComm;
        // if the communication had not been started yet, just dispose and initialize again
        if (comm == null) {
            super.handleConfigurationUpdate(configurationParameters);
            return;
        }

        Configuration configuration = editConfiguration();
        for (Entry<String, Object> configurationParmeter : configurationParameters.entrySet()) {
            configuration.put(configurationParmeter.getKey(), configurationParmeter.getValue());
        }
        updateConfiguration(configuration);

        setConfig();

        scheduler.submit(() -> {
            comm.restartCommunication();
            if (!comm.communicationActive()) {
                bridgeOffline();
                return;
            }

            updateProperties();

            updateStatus(ThingStatus.ONLINE);

            int refreshInterval = config.refresh;
            setupRefreshTimer(refreshInterval);
        });
    }

    /**
     * Set discovery service handler to be able to start discovery after bridge initialization.
     *
     * @param nhcDiscovery
     */
    public void setNhcDiscovery(@Nullable NikoHomeControlDiscoveryService nhcDiscovery) {
        this.nhcDiscovery = nhcDiscovery;
    }

    @Override
    public void alarmEvent(String alarmText) {
        logger.debug("triggering alarm channel with {}", alarmText);
        triggerChannel(CHANNEL_ALARM, alarmText);
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void noticeEvent(String alarmText) {
        logger.debug("triggering notice channel with {}", alarmText);
        triggerChannel(CHANNEL_NOTICE, alarmText);
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void updatePropertiesEvent() {
        updateProperties();
    }

    /**
     * Get the Niko Home Control communication object.
     *
     * @return Niko Home Control communication object
     */
    public @Nullable NikoHomeControlCommunication getCommunication() {
        return nhcComm;
    }

    @Override
    public @Nullable InetAddress getAddr() {
        InetAddress addr = null;
        try {
            addr = InetAddress.getByName(config.addr);
        } catch (UnknownHostException e) {
            logger.debug("Cannot resolve hostname {} to IP adress", config.addr);
        }
        return addr;
    }

    @Override
    public int getPort() {
        return config.port;
    }

    protected synchronized void setConfig() {
        config = getConfig().as(NikoHomeControlBridgeConfig.class);
    }
}
