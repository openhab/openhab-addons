/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.satel.handler;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.ConfigStatusBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.satel.internal.command.NewStatesCommand;
import org.openhab.binding.satel.internal.command.SatelCommand;
import org.openhab.binding.satel.internal.config.SatelBridgeConfig;
import org.openhab.binding.satel.internal.event.ConnectionStatusEvent;
import org.openhab.binding.satel.internal.event.SatelEvent;
import org.openhab.binding.satel.internal.event.SatelEventListener;
import org.openhab.binding.satel.internal.protocol.SatelModule;
import org.openhab.binding.satel.internal.types.IntegraType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SatelBridgeHandler} is base class for all bridge handlers.
 *
 * @author Krzysztof Goworek - Initial contribution
 */
public abstract class SatelBridgeHandler extends ConfigStatusBridgeHandler implements SatelEventListener {

    private final Logger logger = LoggerFactory.getLogger(SatelBridgeHandler.class);

    private SatelBridgeConfig config;
    private SatelModule satelModule;
    private ScheduledFuture<?> pollingJob;
    private String userCodeOverride;

    public SatelBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void incomingEvent(SatelEvent event) {
        if (event instanceof ConnectionStatusEvent) {
            ConnectionStatusEvent statusEvent = (ConnectionStatusEvent) event;
            // update bridge status and get new states from the system
            if (statusEvent.isConnected()) {
                updateStatus(ThingStatus.ONLINE);
                satelModule.sendCommand(new NewStatesCommand(satelModule.getIntegraType().hasExtPayload()));
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                        statusEvent.getReason());
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // the bridge does not support any command at the moment
        logger.debug("New command for {}: {}", channelUID, command.toFullString());
    }

    protected void initialize(final SatelModule satelModule) {
        logger.debug("Initializing bridge handler");

        this.config = getConfigAs(SatelBridgeConfig.class);
        this.satelModule = satelModule;
        this.satelModule.addEventListener(this);
        this.satelModule.open();
        logger.debug("Satel module opened");

        if (satelModule != null) {
            if (pollingJob == null || pollingJob.isCancelled()) {
                Runnable pollingCommand = () -> {
                    if (!satelModule.isInitialized()) {
                        logger.debug("Module not initialized yet, skipping refresh");
                        return;
                    }

                    // get list of states that have changed
                    logger.trace("Sending 'get new states' command");
                    satelModule.sendCommand(new NewStatesCommand(satelModule.getIntegraType().hasExtPayload()));
                };
                pollingJob = scheduler.scheduleWithFixedDelay(pollingCommand, 0, config.getRefresh(),
                        TimeUnit.MILLISECONDS);
            }
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing bridge handler.");

        if (pollingJob != null && !pollingJob.isCancelled()) {
            pollingJob.cancel(true);
            pollingJob = null;
        }
        if (satelModule != null) {
            satelModule.close();
            satelModule = null;
            logger.debug("Satel module closed.");
        }
    }

    /**
     * Adds given listener to list of event receivers.
     *
     * @param listener listener object to add
     */
    public void addEventListener(SatelEventListener listener) {
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
        if (satelModule != null) {
            satelModule.removeEventListener(listener);
        }
    }

    @Override
    public boolean isInitialized() {
        return satelModule != null && satelModule.isInitialized();
    }

    /**
     * @return type of Integra system
     * @see IntegraType
     */
    public IntegraType getIntegraType() {
        if (satelModule != null) {
            return satelModule.getIntegraType();
        }
        return null;
    }

    /**
     * @return current user code, either from the configuration or set later using {@link #setUserCode(String)}
     */
    public String getUserCode() {
        if (StringUtils.isNotEmpty(userCodeOverride)) {
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
    public String getEncoding() {
        return config.getEncoding();
    }

    /**
     * Sends given command to communication module.
     *
     * @param command a command to send
     * @param async if <code>true</code> method waits for the response
     * @return <code>true</code> if send succeeded
     */
    public boolean sendCommand(SatelCommand command, boolean async) {
        if (this.satelModule == null) {
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
                    command.wait(this.satelModule.getTimeout());
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
