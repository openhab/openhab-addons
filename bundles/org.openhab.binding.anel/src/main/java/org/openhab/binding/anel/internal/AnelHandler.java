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
package org.openhab.binding.anel.internal;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.anel.internal.auth.AnelAuthentication;
import org.openhab.binding.anel.internal.auth.AnelAuthentication.AuthMethod;
import org.openhab.binding.anel.internal.state.AnelCommandHandler;
import org.openhab.binding.anel.internal.state.AnelState;
import org.openhab.binding.anel.internal.state.AnelStateUpdater;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AnelHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Patrick Koenemann - Initial contribution
 */
@NonNullByDefault
public class AnelHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(AnelHandler.class);

    private final AnelCommandHandler commandHandler = new AnelCommandHandler();
    private final AnelStateUpdater stateUpdater = new AnelStateUpdater();

    private @Nullable AnelConfiguration config;
    private @Nullable AnelUdpConnector udpConnector;

    /** The most recent state of the Anel device. */
    private @Nullable AnelState state;
    /** Cached authentication information (encrypted, if possible). */
    private @Nullable String authentication;

    private @Nullable ScheduledFuture<?> periodicRefreshTask;

    private int sendingFailures = 0;
    private int updateStateFailures = 0;
    private int refreshRequestWithoutResponse = 0;
    private boolean refreshRequested = false; // avoid multiple simultaneous refresh requests

    public AnelHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        config = getConfigAs(AnelConfiguration.class);

        updateStatus(ThingStatus.UNKNOWN);

        // background initialization
        scheduler.execute(this::initializeConnection);
    }

    private void initializeConnection() {
        final AnelConfiguration config2 = config;
        final String host = config2 == null ? null : config2.hostname;
        if (config2 == null || host == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Cannot initialize thing without configuration: " + config2);
            return;
        }
        try {
            final AnelUdpConnector newUdpConnector = new AnelUdpConnector(host, config2.udpReceivePort,
                    config2.udpSendPort, scheduler);
            udpConnector = newUdpConnector;

            // establish connection and register listener
            newUdpConnector.connect(this::handleStatusUpdate, true);

            // request initial state, 3 attempts
            for (int attempt = 1; attempt <= IAnelConstants.ATTEMPTS_WITH_COMMUNICATION_ERRORS
                    && state == null; attempt++) {
                try {
                    newUdpConnector.send(IAnelConstants.BROADCAST_DISCOVERY_MSG);
                } catch (IOException e) {
                    // network or socket failure, also wait 2 sec and try again
                }

                // answer expected within 50-600ms on a regular network; wait up to 2sec just to make sure
                for (int delay = 0; delay < 10 && state == null; delay++) {
                    Thread.sleep(200); // wait 10 x 200ms = 2sec
                }
            }

            // set thing status (and set unique property)
            final AnelState state2 = state;
            if (state2 != null) {
                updateStatus(ThingStatus.ONLINE);

                final String mac = state2.mac;
                if (mac != null && !mac.isEmpty()) {
                    updateProperty(IAnelConstants.UNIQUE_PROPERTY_NAME, mac);
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Device does not respond (check IP, ports, and network connection): " + config);
            }

            // schedule refresher task to continuously check for device state
            periodicRefreshTask = scheduler.scheduleWithFixedDelay(this::periodicRefresh, //
                    0, IAnelConstants.REFRESH_INTERVAL_SEC, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            // OH shutdown - don't log anything, Framework will call dispose()
        } catch (Exception e) {
            logger.debug("Connection to '{}' failed", config, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, "Connection to '" + config
                    + "' failed unexpectedly with " + e.getClass().getSimpleName() + ": " + e.getMessage());
            dispose();
        }
    }

    private void periodicRefresh() {
        /*
         * it's sufficient to send "wer da?" to the configured ip address.
         * the listener should be able to process the response like any other response.
         */
        final AnelUdpConnector udpConnector2 = udpConnector;
        if (udpConnector2 != null && udpConnector2.isConnected()) {
            /*
             * Check whether or not the device sends a response at all. If not, after some unanswered refresh requests,
             * we should change the thing status to COMM_ERROR. The refresh task should remain active so that the device
             * has a chance to get back online as soon as it responds again.
             */
            if (refreshRequestWithoutResponse > IAnelConstants.UNANSWERED_REFRESH_REQUESTS_TO_SET_THING_OFFLINE
                    && getThing().getStatus() == ThingStatus.ONLINE) {
                final String msg = "Setting thing offline because it did not respond to the last "
                        + IAnelConstants.UNANSWERED_REFRESH_REQUESTS_TO_SET_THING_OFFLINE + " status requests: "
                        + config;
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, msg);
            }

            try {
                refreshRequestWithoutResponse++;

                udpConnector2.send(IAnelConstants.BROADCAST_DISCOVERY_MSG);
                sendingFailures = 0;
            } catch (Exception e) {
                handleSendException(e);
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        final AnelUdpConnector udpConnector2 = udpConnector;
        if (udpConnector2 == null || !udpConnector2.isConnected() || getThing().getStatus() != ThingStatus.ONLINE) {
            // don't log initial refresh commands because they may occur before thing is online
            if (!(command instanceof RefreshType)) {
                logger.debug("Cannot handle command '{}' for channel '{}' because thing ({}) is not connected: {}", //
                        command, channelUID.getId(), getThing().getStatus(), config);
            }
            return;
        }

        String anelCommand = null;
        if (command instanceof RefreshType) {
            final State update = stateUpdater.getChannelUpdate(channelUID.getId(), state);
            if (update != null) {
                updateState(channelUID, update);
            } else if (!refreshRequested) {
                // send broadcast request for refreshing the state; remember it to avoid multiple simultaneous requests
                refreshRequested = true;
                anelCommand = IAnelConstants.BROADCAST_DISCOVERY_MSG;
            } else {
                logger.debug(
                        "Channel {} received command {} which is ignored because another channel already requested the same command",
                        channelUID, command);
            }
        } else if (command instanceof OnOffType) {
            final State lockedState;
            synchronized (this) { // lock needed to update the state if needed
                lockedState = commandHandler.getLockedState(state, channelUID.getId());
                if (lockedState == null) {
                    // command only possible if state is not locked
                    anelCommand = commandHandler.toAnelCommandAndUnsetState(state, channelUID.getId(), command,
                            getAuthentication());
                }
            }

            if (lockedState != null) {
                logger.debug("Channel {} received command {} but it is locked, so the state is reset to {}.",
                        channelUID, command, lockedState);

                updateState(channelUID, lockedState);
            } else if (anelCommand == null) {
                logger.warn(
                        "Channel {} received command {} which is (currently) not supported; please check channel configuration.",
                        channelUID, command);
            }
        } else {
            logger.warn("Channel {} received command {} which is not supported", channelUID, command);
        }

        if (anelCommand != null) {
            logger.debug("Channel {} received command {} which is converted to: {}", channelUID, command, anelCommand);

            try {
                udpConnector2.send(anelCommand);
                sendingFailures = 0;
            } catch (Exception e) {
                handleSendException(e);
            }
        }
    }

    private void handleSendException(Exception e) {
        if (getThing().getStatus() == ThingStatus.ONLINE) {
            if (sendingFailures++ == IAnelConstants.ATTEMPTS_WITH_COMMUNICATION_ERRORS) {
                final String msg = "Setting thing offline because binding failed to send " + sendingFailures
                        + " messages to it: " + config;
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, msg);
            } else if (sendingFailures < IAnelConstants.ATTEMPTS_WITH_COMMUNICATION_ERRORS) {
                logger.warn("Failed to send message to: {}", config, e);
            }
        } // else: ignore exception for offline things
    }

    private void handleStatusUpdate(@Nullable String newStatus) {
        refreshRequestWithoutResponse = 0;
        try {
            if (newStatus != null && newStatus.contains(IAnelConstants.ERROR_CREDENTIALS)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Invalid username or password for " + config);
                return;
            }
            if (newStatus != null && newStatus.contains(IAnelConstants.ERROR_INSUFFICIENT_RIGHTS)) {
                final AnelConfiguration config2 = config;
                if (config2 != null) {
                    logger.warn(
                            "User '{}' on device {} has insufficient rights to change the state of a relay or IO port; you can fix that in the Web-UI, 'Einstellungen / Settings' -> 'User'.",
                            config2.user, config2.hostname);
                }
                return;
            }

            final AnelState recentState, newState;
            synchronized (this) { // to make sure state is fully processed before replacing it
                recentState = state;
                if (newStatus != null && recentState != null && newStatus.equals(recentState.status)
                        && !hasUnsetState(recentState)) {
                    return; // no changes
                }
                newState = AnelState.of(newStatus);

                state = newState; // update most recent state
            }
            final Map<String, State> updates = stateUpdater.getChannelUpdates(recentState, newState);

            if (getThing().getStatus() == ThingStatus.OFFLINE) {
                updateStatus(ThingStatus.ONLINE); // we got a response! set thing online if it wasn't!
            }
            updateStateFailures = 0; // reset error counter, if necessary

            // report all state updates
            if (!updates.isEmpty()) {
                logger.debug("updating channel states: {}", updates);

                updates.forEach(this::updateState);
            }
        } catch (Exception e) {
            if (getThing().getStatus() == ThingStatus.ONLINE) {
                if (updateStateFailures++ == IAnelConstants.ATTEMPTS_WITH_COMMUNICATION_ERRORS) {
                    final String msg = "Setting thing offline because status updated failed " + updateStateFailures
                            + " times in a row for: " + config;
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, msg);
                } else if (updateStateFailures < IAnelConstants.ATTEMPTS_WITH_COMMUNICATION_ERRORS) {
                    logger.warn("Status update failed for: {}", config, e);
                }
            } // else: ignore exception for offline things
        }
    }

    private boolean hasUnsetState(AnelState state) {
        for (int i = 0; i < state.relayState.length; i++) {
            if (state.relayState[i] == null) {
                return true;
            }
        }
        for (int i = 0; i < state.ioState.length; i++) {
            if (state.ioName[i] != null && state.ioState[i] == null) {
                return true;
            }
        }
        return false;
    }

    private String getAuthentication() {
        // create and remember authentication string
        final String currentAuthentication = authentication;
        if (currentAuthentication != null) {
            return currentAuthentication;
        }

        final AnelState currentState = state;
        if (currentState == null) {
            // should never happen because initialization ensures that initial state is received
            throw new IllegalStateException("Cannot send any command to device b/c it did not send any answer yet");
        }

        final AnelConfiguration currentConfig = config;
        if (currentConfig == null) {
            throw new IllegalStateException("Config must not be null!");
        }

        final String newAuthentication = AnelAuthentication.getUserPasswordString(currentConfig.user,
                currentConfig.password, AuthMethod.of(currentState.status));
        authentication = newAuthentication;
        return newAuthentication;
    }

    @Override
    public void dispose() {
        final ScheduledFuture<?> periodicRefreshTask2 = periodicRefreshTask;
        if (periodicRefreshTask2 != null) {
            periodicRefreshTask2.cancel(false);
            periodicRefreshTask = null;
        }
        final AnelUdpConnector connector = udpConnector;
        if (connector != null) {
            udpConnector = null;
            try {
                connector.disconnect();
            } catch (Exception e) {
                logger.debug("Failed to close socket connection for: {}", config, e);
            }
        }
    }
}
