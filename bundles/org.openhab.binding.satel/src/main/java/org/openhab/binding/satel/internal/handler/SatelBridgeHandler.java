/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.satel.internal.handler;

import java.nio.charset.Charset;
import java.time.ZoneId;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.satel.internal.command.NewStatesCommand;
import org.openhab.binding.satel.internal.command.SatelCommand;
import org.openhab.binding.satel.internal.config.SatelBridgeConfig;
import org.openhab.binding.satel.internal.event.ConnectionStatusEvent;
import org.openhab.binding.satel.internal.event.SatelEventListener;
import org.openhab.binding.satel.internal.protocol.SatelModule;
import org.openhab.binding.satel.internal.types.IntegraType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ConfigStatusBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SatelBridgeHandler} is base class for all bridge handlers.
 *
 * @author Krzysztof Goworek - Initial contribution
 */
@NonNullByDefault
public abstract class SatelBridgeHandler extends ConfigStatusBridgeHandler implements SatelEventListener {

    private final Logger logger = LoggerFactory.getLogger(SatelBridgeHandler.class);

    private SatelBridgeConfig config = new SatelBridgeConfig();
    private @Nullable SatelModule satelModule;
    private @Nullable ScheduledFuture<?> pollingJob;
    private String userCodeOverride = "";
    private final ZoneId integraZone = ZoneId.systemDefault();

    public SatelBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void incomingEvent(ConnectionStatusEvent event) {
        final SatelModule satelModule = this.satelModule;
        if (satelModule != null) {
            // update bridge status and get new states from the system
            if (event.isConnected()) {
                updateStatus(ThingStatus.ONLINE);
                satelModule.sendCommand(new NewStatesCommand(satelModule.hasExtPayloadSupport()));
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, event.getReason());
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // the bridge does not support any command at the moment
        logger.debug("New command for {}: {}", channelUID, command);
    }

    protected void initialize(final SatelModule satelModule) {
        logger.debug("Initializing bridge handler");

        final SatelBridgeConfig config = getConfigAs(SatelBridgeConfig.class);
        this.config = config;
        this.satelModule = satelModule;
        satelModule.addEventListener(this);
        satelModule.open();
        logger.debug("Satel module opened");

        final ScheduledFuture<?> pollingJob = this.pollingJob;
        if (pollingJob == null || pollingJob.isCancelled()) {
            Runnable pollingCommand = () -> {
                if (!satelModule.isInitialized()) {
                    logger.debug("Module not initialized yet, skipping refresh");
                    return;
                }

                // get list of states that have changed
                logger.trace("Sending 'get new states' command");
                satelModule.sendCommand(new NewStatesCommand(satelModule.hasExtPayloadSupport()));
            };
            this.pollingJob = scheduler.scheduleWithFixedDelay(pollingCommand, 0, config.getRefresh(),
                    TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing bridge handler.");

        final ScheduledFuture<?> pollingJob = this.pollingJob;
        if (pollingJob != null && !pollingJob.isCancelled()) {
            pollingJob.cancel(true);
            this.pollingJob = null;
        }

        final SatelModule satelModule = this.satelModule;
        if (satelModule != null) {
            satelModule.close();
            this.satelModule = null;
            logger.debug("Satel module closed.");
        }
    }

    /**
     * Adds given listener to list of event receivers.
     *
     * @param listener listener object to add
     */
    public void addEventListener(SatelEventListener listener) {
        final SatelModule satelModule = this.satelModule;
        if (satelModule != null) {
            satelModule.addEventListener(listener);
        }
    }

    /**
     * Removes given listener from list of event receivers.
     *
     * @param listener listener object to remove
     */
    public void removeEventListener(SatelEventListener listener) {
        final SatelModule satelModule = this.satelModule;
        if (satelModule != null) {
            satelModule.removeEventListener(listener);
        }
    }

    @Override
    public boolean isInitialized() {
        final SatelModule satelModule = this.satelModule;
        return satelModule != null && satelModule.isInitialized();
    }

    /**
     * @return type of Integra system
     * @see IntegraType
     */
    public IntegraType getIntegraType() {
        final SatelModule satelModule = this.satelModule;
        return satelModule != null ? satelModule.getIntegraType() : IntegraType.UNKNOWN;
    }

    /**
     * @return current user code, either from the configuration or set later using {@link #setUserCode(String)}
     */
    public String getUserCode() {
        if (!userCodeOverride.isEmpty()) {
            return userCodeOverride;
        } else {
            return config.getUserCode();
        }
    }

    /**
     * @param userCode new use code that overrides the one in the configuration
     */
    public void setUserCode(String userCode) {
        this.userCodeOverride = userCode;
    }

    /**
     * @return encoding for texts
     */
    public Charset getEncoding() {
        try {
            return config.getEncoding();
        } catch (Exception e) {
            logger.info("Invalid or unsupported encoding configured for {}", getThing().getUID());
            return Charset.defaultCharset();
        }
    }

    /**
     * @return zone for Integra date and time values
     */
    public ZoneId getZoneId() {
        return integraZone;
    }

    /**
     * Sends given command to communication module.
     *
     * @param command a command to send
     * @param async if <code>false</code> method waits for the response
     * @return <code>true</code> if send succeeded
     */
    public boolean sendCommand(SatelCommand command, boolean async) {
        final SatelModule satelModule = this.satelModule;
        if (satelModule == null) {
            return false;
        }
        if (async) {
            return satelModule.sendCommand(command);
        } else if (!satelModule.sendCommand(command, true)) {
            return false;
        }

        boolean interrupted = false;
        while (!interrupted) {
            // wait for command state change
            try {
                synchronized (command) {
                    command.wait(satelModule.getTimeout());
                }
            } catch (InterruptedException e) {
                // ignore, we will leave the loop on next interruption state check
                interrupted = true;
            }
            // check current state
            switch (command.getState()) {
                case SUCCEEDED:
                    return true;
                case FAILED:
                    return false;
                default:
                    // wait for next change unless interrupted
            }
        }
        return false;
    }
}
