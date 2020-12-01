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
package org.openhab.binding.sony.internal;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the base thing handler for all sony things. This base handler provides common services to all sony things
 * like polling, connection retry and status checking.
 *
 * @author Tim Roberts - Initial contribution
 * @param <C> The configuration type for the handler
 */
@NonNullByDefault
public abstract class AbstractThingHandler<C extends AbstractConfig> extends BaseThingHandler {
    /** The logger */
    private final Logger logger = LoggerFactory.getLogger(AbstractThingHandler.class);

    /** The configuration class type */
    private final Class<C> configType;

    /** The refresh state event - will only be created when we are connected. */
    private final AtomicReference<@Nullable Future<?>> refreshState = new AtomicReference<>(null);

    /** The check status event - will only be created when we are connected. */
    private final AtomicReference<@Nullable Future<?>> checkStatus = new AtomicReference<>(null);

    /** The retry connection event - will only be created when we are disconnected. */
    private final AtomicReference<@Nullable Future<?>> retryConnection = new AtomicReference<>(null);

    /** The queue used to cache commands until online */
    private final Queue<CachedCommand> commandQueue = new ConcurrentLinkedQueue<>();

    /**
     * Constructs the handler from the specified {@link Thing}
     *
     * @param thing the non-null thing
     * @param configType the non-null configuration type
     */
    public AbstractThingHandler(final Thing thing, final Class<C> configType) {
        super(thing);

        Objects.requireNonNull(thing, "thing cannot be null");
        Objects.requireNonNull(configType, "configType cannot be null");

        this.configType = configType;
    }

    /**
     * Called when the thing handler should attempt a connection. Note that this method is reentrant. The implementation
     * of this method MUST call {@link #updateStatus(ThingStatus, ThingStatusDetail, String)} prior to exiting (either
     * with ONLINE or OFFLINE) to allow this abstract base to process the results of the connection attempt properly.
     */
    protected abstract void connect();

    /**
     * Called when the thing handler should attempt to refresh state. Note that this method is reentrant.
     * 
     * @param initial true if this is the initial refresh state after going online, false otherwise
     */
    protected abstract void refreshState(boolean initial);

    /**
     * Returns the configuration cast to the specific type
     * 
     * @return a non-null configuration
     */
    protected C getSonyConfig() {
        return getConfigAs(configType);
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "Initializing ...");
        SonyUtil.cancel(retryConnection.getAndSet(this.scheduler.submit(this::doConnect)));
    }

    /**
     * Helper method to start a connection attempt
     */
    private void doConnect() {
        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "Initializing ...");
        connect();
    }

    /**
     * Helper method to determine if we should auto reconnect on a command
     * 
     * @return true if auto reconnect is supported, false otherwise
     */
    private boolean isAutoReconnect() {
        return getSonyConfig().getRetryPolling() <= 0;
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        Objects.requireNonNull(channelUID, "channelUID cannot be null");
        Objects.requireNonNull(command, "command cannot be null");

        final ThingStatus status = getThing().getStatus();
        final boolean autoReconnect = isAutoReconnect();
        if (status == ThingStatus.OFFLINE && autoReconnect) {
            logger.debug("AutoReconnect on - scheduling reconnect and caching: {} {}", channelUID, command);
            commandQueue.add(new CachedCommand(channelUID, command));
            scheduleReconnect();
        } else if (status == ThingStatus.UNKNOWN && autoReconnect) {
            logger.debug("AutoReconnect on - waiting for reconnect and caching: {} {}", channelUID, command);
            commandQueue.add(new CachedCommand(channelUID, command));
        } else if (status == ThingStatus.OFFLINE) {
            logger.debug("Thing OFFLINE - ignoring command: {} {}", channelUID, command);
        } else {
            doHandleCommand(channelUID, command);
        }
    }

    /**
     * This will execute any cached commands
     */
    private void doCachedCommands() {
        logger.debug("Executing any cached commands");
        final long now = System.currentTimeMillis();
        while (true) {
            final CachedCommand cmd = commandQueue.poll();
            if (cmd == null) {
                return;
            }

            if (now - cmd.timestamp > SonyBindingConstants.THING_CACHECOMMAND_TIMEOUTMS) {
                logger.debug("Command expired waiting on a connect: {} {}", cmd.channelUID, cmd.command);
            } else {
                doHandleCommand(cmd.channelUID, cmd.command);
            }
        }
    }

    /**
     * Internal method to handle a command
     * 
     * @param channelUID a non-null channel UID
     * @param command a non-null command
     */
    private void doHandleCommand(final ChannelUID channelUID, final Command command) {
        Objects.requireNonNull(channelUID, "channelUID cannot be null");
        Objects.requireNonNull(command, "command cannot be null");

        if (command instanceof RefreshType) {
            handleRefreshCommand(channelUID);
        } else {
            handleSetCommand(channelUID, command);
        }
    }

    /**
     * Method that handles the {@link RefreshType} command specifically.
     *
     * @param channelUID a non-null channel UID
     */
    protected abstract void handleRefreshCommand(final ChannelUID channelUID);

    /**
     * Method that handles a non {@link RefreshType} command.
     *
     * @param channelUID a non-null channel UID
     * @param command a non-null refresh command
     */
    protected abstract void handleSetCommand(final ChannelUID channelUID, final Command command);

    @Override
    protected void updateStatus(final ThingStatus status, final ThingStatusDetail statusDetail,
            final @Nullable String description) {
        super.updateStatus(status, statusDetail, description);

        if (status == ThingStatus.ONLINE) {
            schedulePolling();
            scheduleCheckStatus();
            doCachedCommands();
        } else if (status == ThingStatus.UNKNOWN) {
            // probably in the process of reconnecting - ignore
            logger.trace("Ignoring thing status of UNKNOWN");
        } else {
            SonyUtil.cancel(refreshState.getAndSet(null));
            SonyUtil.cancel(checkStatus.getAndSet(null));

            // don't bother reconnecting - won't fix a configuration error
            if (statusDetail != ThingStatusDetail.CONFIGURATION_ERROR) {
                scheduleReconnect();
            }
        }
    }

    /**
     * Starts the polling process. The polling process will refresh the state of the sony device if the refresh time (in
     * seconds) is greater than 0. This process will continue until cancelled.
     */
    private void schedulePolling() {
        final C config = getSonyConfig();
        final Integer refresh = config.getRefresh();

        if (refresh != null && refresh > 0) {
            logger.debug("Starting state polling every {} seconds", refresh);
            SonyUtil.cancel(refreshState.getAndSet(
                    this.scheduler.scheduleWithFixedDelay(new RefreshState(), refresh, refresh, TimeUnit.SECONDS)));
        } else {
            logger.debug("Refresh not a positive number - polling has been disabled");
        }
    }

    /**
     * Tries to reconnect to the sony device. The results of the connection should call
     * {@link #updateStatus(ThingStatus, ThingStatusDetail, String)} and if set to OFFLINE, this method will be called
     * to schedule another connection attempt
     *
     * There is one warning here - if the retryPolling is set lower than how long
     * it takes to connect, you can get in an infinite loop of the connect getting cancelled for the next retry.
     */
    private void scheduleReconnect() {
        final C config = getSonyConfig();
        final Integer retryPolling = config.getRetryPolling();

        if (retryPolling != null && retryPolling > 0) {
            SonyUtil.cancel(retryConnection.getAndSet(this.scheduler.schedule(() -> {
                if (!SonyUtil.isInterrupted() && !isRemoved()) {
                    doConnect();
                }
            }, retryPolling, TimeUnit.SECONDS)));
        } else {
            logger.debug("Retry connection has been disabled via configuration setting");
        }
    }

    /**
     * Schedules a check status attempt by simply getting the configuration and calling
     * {@link #scheduleCheckStatus(Integer, String, Integer)}
     */
    private void scheduleCheckStatus() {
        final C config = getSonyConfig();
        URL url;

        try {
            url = getCheckStatusUrl();
        } catch (final MalformedURLException e) {
            logger.debug("Cannot check status - URL is malformed: {}", e.getMessage());
            return;
        }

        final String ipAddress = url.getHost();

        final int urlPort = url.getPort();
        final int port = urlPort == -1 ? url.getDefaultPort() : urlPort;

        scheduleCheckStatus(config.getCheckStatusPolling(), ipAddress, port);
    }

    /**
     * Gets the URL to check the status for
     * 
     * @return a non-null URL containing an ipaddress and possibly port
     * @throws MalformedURLException if the url is malformed
     */
    protected URL getCheckStatusUrl() throws MalformedURLException {
        final C config = getSonyConfig();
        return config.getDeviceUrl();
    }

    /**
     * Schedules the check status for the given interval and IP Address/port. If the status is successful, another check
     * status is schedule after checkStatusInterval seconds. If the connection was unsuccessful, the state is updated to
     * OFFLINE (which will trigger a connection attempt)
     *
     * If any of the parameters are null (or checkStatusInterval is <= 0), no check status will be scheduled
     *
     * @param checkStatusInterval a possibly null checkStatus interval
     * @param ipAddress a possibly null, possibly empty IP address to check
     * @param port a possibly null port to check
     */
    private void scheduleCheckStatus(final @Nullable Integer checkStatusInterval, final @Nullable String ipAddress,
            final @Nullable Integer port) {
        if (StringUtils.isNotBlank(ipAddress) && port != null && checkStatusInterval != null
                && checkStatusInterval > 0) {
            SonyUtil.cancel(checkStatus.getAndSet(scheduler.schedule(() -> {
                try {
                    if (!SonyUtil.isInterrupted() && !isRemoved()) {
                        try (Socket soc = new Socket()) {
                            soc.connect(new InetSocketAddress(ipAddress, port), 5000);
                        }
                        logger.debug("Checking connectivity to {}:{} - successful", ipAddress, port);
                        scheduleCheckStatus(checkStatusInterval, ipAddress, port);
                    }
                } catch (final IOException ex) {
                    logger.debug("Checking connectivity to {}:{} - unsuccessful - going offline: {}", ipAddress, port,
                            ex.getMessage(), ex);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Could not connect to " + ipAddress + ":" + port);
                }
            }, checkStatusInterval, TimeUnit.SECONDS)));
        }
    }

    /**
     * Helper method to determine if the thing is being removed (or is removed)
     *
     * @return true if removed, false otherwise
     */
    private boolean isRemoved() {
        final ThingStatus status = getThing().getStatus();
        return status == ThingStatus.REMOVED || status == ThingStatus.REMOVING;
    }

    @Override
    public void dispose() {
        super.dispose();
        SonyUtil.cancel(refreshState.getAndSet(null));
        SonyUtil.cancel(retryConnection.getAndSet(null));
        SonyUtil.cancel(checkStatus.getAndSet(null));
    }

    /**
     * This is a helper class to track a command that has been cached until the thing goes online. The channelUID,
     * command and a timestamp will be recorded.
     */
    @NonNullByDefault
    private class CachedCommand {
        /** When the cached command was created */
        private final long timestamp = System.currentTimeMillis();

        /** The channel UID */
        private final ChannelUID channelUID;

        /** The command */
        private final Command command;

        /**
         * Creates the cached command
         * 
         * @param channelUID a non-null channel UID
         * @param command a non-null command
         */
        public CachedCommand(ChannelUID channelUID, Command command) {
            this.channelUID = channelUID;
            this.command = command;
        }
    }

    /**
     * This helper class is used to manage refreshing of the state
     */
    private class RefreshState implements Runnable {

        // boolean indicating if the refresh is the first refresh after going online
        private boolean initial = true;

        @Override
        public void run() {
            // throw exceptions to prevent future tasks under these circumstances
            if (isRemoved()) {
                throw new IllegalStateException("Thing has been removed - ending state polling");
            }
            if (SonyUtil.isInterrupted()) {
                throw new IllegalStateException("State polling has been cancelled");
            }

            // catch the various runtime exceptions that may occur here (the biggest being ProcessingException)
            // and handle it.
            try {
                if (thing.getStatus() == ThingStatus.ONLINE) {
                    refreshState(initial);
                    initial = false;
                } else {
                    initial = true;
                }
            } catch (final Exception ex) {
                if (StringUtils.contains(ex.getMessage(), "Connection refused")) {
                    logger.debug("Connection refused - device is probably turned off");
                } else {
                    logger.debug("Uncaught exception (refreshstate) : {}", ex.getMessage(), ex);
                }
            }
        }
    }
}
