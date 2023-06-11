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
package org.openhab.binding.chromecast.internal.handler;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.chromecast.internal.ChromecastAudioSink;
import org.openhab.binding.chromecast.internal.ChromecastCommander;
import org.openhab.binding.chromecast.internal.ChromecastEventReceiver;
import org.openhab.binding.chromecast.internal.ChromecastScheduler;
import org.openhab.binding.chromecast.internal.ChromecastStatusUpdater;
import org.openhab.binding.chromecast.internal.action.ChromecastActions;
import org.openhab.binding.chromecast.internal.config.ChromecastConfig;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioHTTPServer;
import org.openhab.core.audio.AudioSink;
import org.openhab.core.audio.AudioStream;
import org.openhab.core.audio.UnsupportedAudioFormatException;
import org.openhab.core.audio.UnsupportedAudioStreamException;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import su.litvak.chromecast.api.v2.ChromeCast;

/**
 * The {@link ChromecastHandler} is responsible for handling commands, which are sent to one of the channels. It
 * furthermore implements {@link AudioSink} support.
 *
 * @author Markus Rathgeb, Kai Kreuzer - Initial contribution
 * @author Daniel Walters - Online status fix, handle playuri channel and refactor play media code
 * @author Jason Holmes - Media Status. Refactor the monolith into separate classes.
 * @author Scott Hanson - Added Actions.
 */
@NonNullByDefault
public class ChromecastHandler extends BaseThingHandler implements AudioSink {
    private final Logger logger = LoggerFactory.getLogger(ChromecastHandler.class);

    private static final Set<AudioFormat> SUPPORTED_FORMATS = Set.of(AudioFormat.MP3, AudioFormat.WAV);
    private static final Set<Class<? extends AudioStream>> SUPPORTED_STREAMS = Set.of(AudioStream.class);

    private final AudioHTTPServer audioHTTPServer;
    private final @Nullable String callbackUrl;

    /**
     * The actual implementation. A new one is created each time #initialize is called.
     */
    private @Nullable Coordinator coordinator;

    /**
     * Constructor.
     *
     * @param thing the thing the coordinator should be created for
     * @param audioHTTPServer server for hosting audio streams
     * @param callbackUrl url to be used to tell the Chromecast which host to call for audio urls
     */
    public ChromecastHandler(final Thing thing, AudioHTTPServer audioHTTPServer, @Nullable String callbackUrl) {
        super(thing);
        this.audioHTTPServer = audioHTTPServer;
        this.callbackUrl = callbackUrl;
    }

    @Override
    public void initialize() {
        ChromecastConfig config = getConfigAs(ChromecastConfig.class);

        final String ipAddress = config.ipAddress;
        if (ipAddress == null || ipAddress.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Cannot connect to Chromecast. IP address is not valid or missing.");
            return;
        }

        updateStatus(ThingStatus.UNKNOWN);

        Coordinator localCoordinator = coordinator;
        if (localCoordinator != null && (!localCoordinator.chromeCast.getAddress().equals(ipAddress)
                || (localCoordinator.chromeCast.getPort() != config.port))) {
            localCoordinator.destroy();
            localCoordinator = coordinator = null;
        }

        if (localCoordinator == null) {
            ChromeCast chromecast = new ChromeCast(ipAddress, config.port);
            localCoordinator = new Coordinator(this, thing, chromecast, config.refreshRate, audioHTTPServer,
                    callbackUrl);
            coordinator = localCoordinator;

            scheduler.submit(() -> {
                Coordinator c = coordinator;
                if (c != null) {
                    c.initialize();
                }
            });
        }
    }

    @Override
    public void dispose() {
        Coordinator localCoordinator = coordinator;
        if (localCoordinator != null) {
            localCoordinator.destroy();
            coordinator = null;
        }
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        Coordinator localCoordinator = coordinator;
        if (localCoordinator != null) {
            localCoordinator.commander.handleCommand(channelUID, command);
        } else {
            logger.debug("Cannot handle command. No coordinator has been initialized");
        }
    }

    @Override // Just exposing this for ChromecastStatusUpdater.
    public void updateState(String channelId, State state) {
        super.updateState(channelId, state);
    }

    @Override // Just exposing this for ChromecastStatusUpdater.
    public void updateState(ChannelUID channelUID, State state) {
        super.updateState(channelUID, state);
    }

    @Override // Just exposing this for ChromecastStatusUpdater.
    public void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
        super.updateStatus(status, statusDetail, description);
    }

    @Override // Just exposing this for ChromecastStatusUpdater.
    public boolean isLinked(String channelId) {
        return super.isLinked(channelId);
    }

    @Override // Just exposing this for ChromecastStatusUpdater.
    public boolean isLinked(ChannelUID channelUID) {
        return super.isLinked(channelUID);
    }

    @Override
    public String getId() {
        return thing.getUID().toString();
    }

    @Override
    public @Nullable String getLabel(@Nullable Locale locale) {
        return thing.getLabel();
    }

    @Override
    public Set<AudioFormat> getSupportedFormats() {
        return SUPPORTED_FORMATS;
    }

    @Override
    public Set<Class<? extends AudioStream>> getSupportedStreams() {
        return SUPPORTED_STREAMS;
    }

    @Override
    public void process(@Nullable AudioStream audioStream)
            throws UnsupportedAudioFormatException, UnsupportedAudioStreamException {
        Coordinator localCoordinator = coordinator;
        if (localCoordinator != null) {
            localCoordinator.audioSink.process(audioStream);
        } else {
            logger.debug("Cannot process audioStream. No coordinator has been initialized.");
        }
    }

    @Override
    public PercentType getVolume() throws IOException {
        Coordinator localCoordinator = coordinator;
        if (localCoordinator != null) {
            return localCoordinator.statusUpdater.getVolume();
        } else {
            throw new IOException("Cannot get volume. No coordinator has been initialized.");
        }
    }

    @Override
    public void setVolume(PercentType percentType) throws IOException {
        Coordinator localCoordinator = coordinator;
        if (localCoordinator != null) {
            localCoordinator.commander.handleVolume(percentType);
        } else {
            throw new IOException("Cannot set volume. No coordinator has been initialized.");
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(ChromecastActions.class);
    }

    public boolean playURL(String url, @Nullable String mediaType) {
        Coordinator localCoordinator = coordinator;
        if (localCoordinator != null) {
            localCoordinator.commander.playMedia(null, url, mediaType);
            return true;
        }
        return false;
    }

    private static class Coordinator {
        private final Logger logger = LoggerFactory.getLogger(Coordinator.class);

        private static final long CONNECT_DELAY = 10;

        private final ChromeCast chromeCast;
        private final ChromecastAudioSink audioSink;
        private final ChromecastCommander commander;
        private final ChromecastEventReceiver eventReceiver;
        private final ChromecastStatusUpdater statusUpdater;
        private final ChromecastScheduler scheduler;

        /**
         * used internally to represent the connection state
         */
        private enum ConnectionState {
            UNKNOWN,
            CONNECTING,
            CONNECTED,
            DISCONNECTING,
            DISCONNECTED
        }

        private ConnectionState connectionState = ConnectionState.UNKNOWN;

        private Coordinator(ChromecastHandler handler, Thing thing, ChromeCast chromeCast, long refreshRate,
                AudioHTTPServer audioHttpServer, @Nullable String callbackURL) {
            this.chromeCast = chromeCast;

            this.scheduler = new ChromecastScheduler(handler.scheduler, CONNECT_DELAY, this::connect, refreshRate,
                    this::refresh);
            this.statusUpdater = new ChromecastStatusUpdater(thing, handler);

            this.commander = new ChromecastCommander(chromeCast, scheduler, statusUpdater);
            this.eventReceiver = new ChromecastEventReceiver(scheduler, statusUpdater);
            this.audioSink = new ChromecastAudioSink(commander, audioHttpServer, callbackURL);
        }

        void initialize() {
            if (connectionState == ConnectionState.CONNECTED) {
                logger.debug("Already connected");
                return;
            } else if (connectionState == ConnectionState.CONNECTING) {
                logger.debug("Already connecting");
                return;
            } else if (connectionState == ConnectionState.DISCONNECTING) {
                logger.warn("Trying to re-connect while still disconnecting");
                return;
            }
            connectionState = ConnectionState.CONNECTING;

            chromeCast.registerListener(eventReceiver);
            chromeCast.registerConnectionListener(eventReceiver);

            connect();
        }

        void destroy() {
            connectionState = ConnectionState.DISCONNECTING;

            chromeCast.unregisterConnectionListener(eventReceiver);
            chromeCast.unregisterListener(eventReceiver);

            scheduler.destroy();

            try {
                chromeCast.disconnect();

                connectionState = ConnectionState.DISCONNECTED;
            } catch (final IOException e) {
                logger.debug("Disconnect failed: {}", e.getMessage());
                connectionState = ConnectionState.UNKNOWN;
            }
        }

        private void connect() {
            try {
                chromeCast.connect();

                statusUpdater.updateMediaStatus(null);
                statusUpdater.updateStatus(ThingStatus.ONLINE);

                connectionState = ConnectionState.CONNECTED;
            } catch (final IOException | GeneralSecurityException e) {
                logger.debug("Connect failed, trying to reconnect: {}", e.getMessage());
                statusUpdater.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                        e.getMessage());
                scheduler.scheduleConnect();
            }
        }

        private void refresh() {
            commander.handleRefresh();
        }
    }
}
