/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.jellyfin.internal.handler;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.jellyfin.internal.events.SessionEventBus;
import org.openhab.binding.jellyfin.internal.events.SessionEventListener;
import org.openhab.binding.jellyfin.internal.thirdparty.gen.current.model.SessionInfoDto;
import org.openhab.binding.jellyfin.internal.util.client.ClientStateUpdater;
import org.openhab.binding.jellyfin.internal.util.command.ClientCommandRouter;
import org.openhab.binding.jellyfin.internal.util.extrapolation.PlaybackExtrapolator;
import org.openhab.binding.jellyfin.internal.util.timeout.SessionTimeoutMonitor;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ClientHandler} is responsible for managing Jellyfin client devices.
 * It receives session updates from the parent ServerHandler bridge via event bus
 * and handles commands sent to client channels (media controls, playback position, etc.).
 *
 * <p>
 * Key responsibilities:
 * <ul>
 * <li>Maintain bridge connection to ServerHandler</li>
 * <li>Subscribe to session events from the event bus</li>
 * <li>Update channels based on session state (synchronized)</li>
 * <li>Delegate commands to {@link ClientCommandRouter}</li>
 * <li>Delegate position extrapolation to {@link PlaybackExtrapolator}</li>
 * <li>Monitor session timeouts via {@link SessionTimeoutMonitor}</li>
 * </ul>
 *
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
public class ClientHandler extends BaseThingHandler implements SessionEventListener {

    private final Logger logger = LoggerFactory.getLogger(ClientHandler.class);

    /** Lock object for synchronizing access to {@link #currentSession}. */
    private final Object sessionLock = new Object();

    /** Session timeout threshold in milliseconds (60 seconds). */
    private static final long SESSION_TIMEOUT_MS = 60_000;

    /** Session timeout monitor – created once, started in {@link #initialize()}. */
    private final SessionTimeoutMonitor timeoutMonitor = new SessionTimeoutMonitor(SESSION_TIMEOUT_MS);

    /** The device ID extracted from the ThingUID, used to subscribe to the event bus. */
    @Nullable
    private String deviceId;

    /**
     * The current session information for this client.
     * Access is guarded by {@link #sessionLock}.
     */
    @Nullable
    private SessionInfoDto currentSession;

    /** Per-second position extrapolation between server session updates. */
    @Nullable
    private PlaybackExtrapolator extrapolator;

    /** Command router – created (and disposed) alongside the handler lifecycle. */
    @Nullable
    private ClientCommandRouter commandRouter;

    /**
     * Constructor for the client handler.
     *
     * @param thing The thing instance for this client device
     */
    public ClientHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing ClientHandler for thing {}", thing.getUID());

        String id = (String) thing.getConfiguration().get("serialNumber");
        if (id == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Missing required configuration: serialNumber");
            return;
        }
        deviceId = id;

        // Validate bridge connection
        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No bridge configured for client");
            return;
        }

        // Verify bridge is a ServerHandler
        ServerHandler serverHandler = getServerHandler();
        if (serverHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Bridge is not a Jellyfin server");
            return;
        }

        // Check bridge online status
        if (bridge.getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Server bridge is not online");
            return;
        }

        // Subscribe to event bus for session updates
        SessionEventBus eventBus = serverHandler.getSessionEventBus();
        eventBus.subscribe(id, this);
        logger.debug("ClientHandler subscribed to event bus for device ID: {}", id);

        // Create playback extrapolator (owns its own single-thread scheduler)
        extrapolator = new PlaybackExtrapolator(id, this::isLinked,
                (channelId, state) -> updateState(channel(channelId), state), timeoutMonitor::recordActivity);

        // Create command router using the framework scheduler for delayed browse
        commandRouter = new ClientCommandRouter(serverHandler, this::getCurrentSession, scheduler);

        // Start session timeout monitor
        timeoutMonitor.start(scheduler, id, () -> {
            synchronized (sessionLock) {
                return currentSession != null;
            }
        }, this::onSessionTimeout);
        logger.debug("Session timeout monitor started for device: {}", id);

        // Client status will be determined by session updates
        updateClientState();
        logger.debug("ClientHandler initialized for thing {}", thing.getUID());
    }

    @Override
    public void dispose() {
        logger.debug("Disposing ClientHandler for thing {}", thing.getUID());

        // Stop timeout monitor
        timeoutMonitor.stop();

        // Unsubscribe from event bus
        String localDeviceId = deviceId;
        if (localDeviceId != null) {
            ServerHandler serverHandler = getServerHandler();
            if (serverHandler != null) {
                SessionEventBus eventBus = serverHandler.getSessionEventBus();
                eventBus.unsubscribe(localDeviceId, this);
                logger.debug("ClientHandler unsubscribed from event bus for device: {}", localDeviceId);
            }
        }

        // Dispose command router (cancels any delayed commands)
        ClientCommandRouter router = commandRouter;
        if (router != null) {
            router.dispose();
            commandRouter = null;
        }

        // Dispose extrapolator (stops task and shuts down scheduler)
        PlaybackExtrapolator extrap = extrapolator;
        if (extrap != null) {
            extrap.dispose();
            extrapolator = null;
        }

        synchronized (sessionLock) {
            currentSession = null;
        }

        deviceId = null;

        super.dispose();
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("Bridge status changed to {} for client {}", bridgeStatusInfo.getStatus(), thing.getUID());

        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            updateClientState();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Server bridge is offline");
            clearChannelStates();
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received command {} for channel {}", command, channelUID);

        // Reject commands if thing is not ONLINE
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            logger.info("Cannot send {} - client {} is {}", command, deviceId, getThing().getStatus());
            return;
        }

        if (command instanceof RefreshType) {
            SessionInfoDto session;
            synchronized (sessionLock) {
                session = currentSession;
            }
            updateStateFromSession(session);
            return;
        }

        ClientCommandRouter router = commandRouter;
        if (router == null) {
            logger.warn("Cannot handle command - handler not yet initialized");
            return;
        }

        try {
            router.route(channelUID, command);
        } catch (Exception e) {
            logger.warn("Error handling command {} for channel {}: {}", command, channelUID, e.getMessage(), e);
        }
    }

    /**
     * Receives session update notifications from the event bus.
     *
     * <p>
     * Updates the current session, refreshes the timeout monitor, re-evaluates client
     * status, publishes channel states, and (re-)starts position extrapolation.
     *
     * @param session the updated session, or {@code null} if the session ended
     */
    @Override
    public void onSessionUpdate(@Nullable SessionInfoDto session) {
        try {
            logger.trace("Received session update for device: {}", deviceId);

            synchronized (sessionLock) {
                currentSession = session;
            }
            timeoutMonitor.recordActivity();

            updateClientState();
            updateStateFromSession(session);

            // Stop the previous extrapolation tick and restart from the fresh server position.
            PlaybackExtrapolator extrap = extrapolator;
            if (extrap != null) {
                extrap.stop();
                extrap.start(session);
            }
        } catch (Exception e) {
            logger.warn("Error processing session update for device {}: {}", deviceId, e.getMessage());
            logger.debug("Session update exception", e);
        }
    }

    /**
     * Updates the client status based on bridge availability, session presence, and timeout.
     *
     * <p>
     * Priority:
     * <ol>
     * <li>Bridge must be ONLINE.</li>
     * <li>A session must exist.</li>
     * <li>The session must not have timed out.</li>
     * </ol>
     */
    private void updateClientState() {
        Bridge bridge = getBridge();
        if (bridge == null || bridge.getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                    "Server bridge is not available or offline");
            return;
        }

        synchronized (sessionLock) {
            if (currentSession == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Device not connected to server");
            } else if (timeoutMonitor.isTimedOut()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "No session update received (timeout)");
            } else {
                updateStatus(ThingStatus.ONLINE);
            }
        }
    }

    /**
     * Called by the {@link SessionTimeoutMonitor} when no session update has arrived within
     * the timeout period. Clears the session and brings the client offline.
     */
    private void onSessionTimeout() {
        logger.info("[SESSION] Clearing session for device {} due to timeout", deviceId);
        synchronized (sessionLock) {
            currentSession = null;
        }
        timeoutMonitor.resetActivity();
        updateClientState();
        clearChannelStates();

        PlaybackExtrapolator extrap = extrapolator;
        if (extrap != null) {
            extrap.stop();
        }
    }

    /**
     * Publishes channel states derived from the given session snapshot.
     *
     * @param session the session to derive states from, or {@code null} to clear all channels
     */
    public synchronized void updateStateFromSession(@Nullable SessionInfoDto session) {
        // Skip state updates if thing is not ONLINE
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            logger.trace("Skipping state update - thing is {} for device {}", getThing().getStatus(), deviceId);
            return;
        }

        if (session == null) {
            logger.debug("Clearing client state for device {} - session is null", deviceId);
        } else {
            var playingItem = session.getNowPlayingItem();
            var playState = session.getPlayState();
            if (playingItem != null) {
                logger.debug("Updating client state from session: {} - playing '{}' (paused={})", session.getId(),
                        playingItem.getName(), playState != null ? playState.getIsPaused() : "n/a");
            } else {
                logger.debug("Updating client state from session: {} - no NowPlayingItem (positionTicks={})",
                        session.getId(), playState != null ? playState.getPositionTicks() : "n/a");
            }
        }

        Map<String, State> states = ClientStateUpdater.calculateChannelStates(session);
        states.forEach((channelId, state) -> {
            if (isLinked(channelId)) {
                updateState(channel(channelId), state);
            }
        });

        // If playback is paused or nothing is playing, stop extrapolation immediately.
        if (session != null) {
            var playState = session.getPlayState();
            boolean isPaused = playState != null && Boolean.TRUE.equals(playState.getIsPaused());
            boolean notPlaying = session.getNowPlayingItem() == null;
            if (isPaused || notPlaying) {
                PlaybackExtrapolator extrap = extrapolator;
                if (extrap != null) {
                    extrap.stop();
                }
            }
        }
    }

    /**
     * Returns the current session information for this client.
     *
     * @return the current session, or {@code null} if no session is active
     */
    @Nullable
    public SessionInfoDto getCurrentSession() {
        synchronized (sessionLock) {
            return currentSession;
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private ChannelUID channel(String channelId) {
        return new ChannelUID(thing.getUID(), channelId);
    }

    private void clearChannelStates() {
        final String[] channels = { "playing-item-id", "playing-item-name", "playing-item-series-name",
                "playing-item-season-name", "playing-item-season", "playing-item-episode", "playing-item-genres",
                "playing-item-type", "playing-item-total-seconds", "media-control", "playing-item-percentage",
                "playing-item-second" };
        for (String ch : channels) {
            updateState(new ChannelUID(thing.getUID(), ch), UnDefType.NULL);
        }
    }

    @Nullable
    private ServerHandler getServerHandler() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            return null;
        }
        if (bridge.getHandler() instanceof ServerHandler serverHandler) {
            return serverHandler;
        }
        return null;
    }
}
