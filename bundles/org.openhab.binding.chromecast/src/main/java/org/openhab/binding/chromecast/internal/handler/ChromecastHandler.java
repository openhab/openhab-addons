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
package org.openhab.binding.chromecast.internal.handler;

import java.io.IOException;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.audio.AudioFormat;
import org.eclipse.smarthome.core.audio.AudioHTTPServer;
import org.eclipse.smarthome.core.audio.AudioSink;
import org.eclipse.smarthome.core.audio.AudioStream;
import org.eclipse.smarthome.core.audio.UnsupportedAudioFormatException;
import org.eclipse.smarthome.core.audio.UnsupportedAudioStreamException;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.chromecast.internal.ChromecastAudioSink;
import org.openhab.binding.chromecast.internal.ChromecastCommander;
import org.openhab.binding.chromecast.internal.ChromecastEventReceiver;
import org.openhab.binding.chromecast.internal.ChromecastScheduler;
import org.openhab.binding.chromecast.internal.ChromecastStatusUpdater;
import org.openhab.binding.chromecast.internal.config.ChromecastConfig;
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
 */
@NonNullByDefault
public class ChromecastHandler extends BaseThingHandler implements AudioSink {

    private static final Set<AudioFormat> SUPPORTED_FORMATS = Collections
            .unmodifiableSet(Stream.of(AudioFormat.MP3, AudioFormat.WAV).collect(Collectors.toSet()));
    private static final Set<Class<? extends AudioStream>> SUPPORTED_STREAMS = Collections.singleton(AudioStream.class);

    private final Logger logger = LoggerFactory.getLogger(ChromecastHandler.class);
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
        if (ipAddress == null || ipAddress.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Cannot connect to Chromecast. IP address is not valid or missing.");
            return;
        }

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
            localCoordinator.initialize();
            coordinator = localCoordinator;
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

    private static class Coordinator {
        private final Logger logger = LoggerFactory.getLogger(Coordinator.class);

        private static final long CONNECT_DELAY = 10;

        private final ChromeCast chromeCast;
        private final ChromecastAudioSink audioSink;
        private final ChromecastCommander commander;
        private final ChromecastEventReceiver eventReceiver;
        private final ChromecastStatusUpdater statusUpdater;
        private final ChromecastScheduler scheduler;

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
            chromeCast.registerListener(eventReceiver);
            chromeCast.registerConnectionListener(eventReceiver);

            this.connect();
        }

        void destroy() {
            chromeCast.unregisterConnectionListener(eventReceiver);
            chromeCast.unregisterListener(eventReceiver);

            try {
                scheduler.destroy();
                chromeCast.disconnect();
            } catch (final IOException ex) {
                logger.debug("Disconnect failed: {}", ex.getMessage());
            }
        }

        private void connect() {
            try {
                chromeCast.connect();
                statusUpdater.updateMediaStatus(null);
                statusUpdater.updateStatus(ThingStatus.ONLINE);
            } catch (final Exception e) {
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
