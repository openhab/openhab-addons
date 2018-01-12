/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.chromecast.handler;

import org.apache.commons.lang.StringUtils;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import su.litvak.chromecast.api.v2.ChromeCast;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.openhab.binding.chromecast.ChromecastBindingConstants.HOST;
import static org.openhab.binding.chromecast.ChromecastBindingConstants.PORT;
import static org.openhab.binding.chromecast.ChromecastBindingConstants.REFRESH_RATE_SECONDS;

/**
 * The {@link ChromecastHandler} is responsible for handling commands, which are
 * sent to one of the channels. It furthermore implements {@link AudioSink} support.
 *
 * @author Markus Rathgeb - Original author
 * @author Kai Kreuzer - Initial contribution as openHAB add-on
 * @author Daniel Walters - Online status fix, handle playuri channel and refactor play media code
 * @author Jason Holmes - Media Status. Refactor the monolith into separate classes.
 */
public class ChromecastHandler extends BaseThingHandler implements AudioSink {
    private static final HashSet<AudioFormat> SUPPORTED_FORMATS = new HashSet<>();
    private static final HashSet<Class<? extends AudioStream>> SUPPORTED_STREAMS = new HashSet<>();

    static {
        SUPPORTED_FORMATS.add(AudioFormat.MP3);
        SUPPORTED_FORMATS.add(AudioFormat.WAV);

        SUPPORTED_STREAMS.add(AudioStream.class);
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final AudioHTTPServer audioHTTPServer;
    private final String callbackUrl;

    /**
     * The actual implementation. A new one is created each time #initalize is called.
     */
    private Coordinator coordinator;

    /**
     * Constructor.
     *
     * @param thing           the thing the coordinator should be created for
     * @param audioHTTPServer server for hosting audio streams
     * @param callbackUrl     url to be used to tell the Chromecast which host to call for audio urls
     */
    public ChromecastHandler(final Thing thing, AudioHTTPServer audioHTTPServer, String callbackUrl) {
        super(thing);
        this.audioHTTPServer = audioHTTPServer;
        this.callbackUrl = callbackUrl;
    }

    @Override
    public void initialize() {
        final Object ipAddress = getConfig().get(HOST);
        if (!(ipAddress instanceof String)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Cannot connect to Chromecast. IP address is invalid.");
            return;
        }

        final String host = (String) ipAddress;
        if (StringUtils.isBlank(host)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Cannot connect to Chromecast. IP address is not set.");
            return;
        }

        final Object portNumber = getConfig().get(PORT);
        logger.debug("portNumber Type is {}", portNumber.getClass().getTypeName());
        final int port;
        if (portNumber instanceof BigDecimal) {
            port = ((BigDecimal) portNumber).intValue();
        } else if (portNumber instanceof Integer) {
            port = (Integer) portNumber;
        } else {
            port = 8009;
        }

        final Integer refreshRate;
        final Object rawRefreshRate = getConfig().get(REFRESH_RATE_SECONDS);
        if (rawRefreshRate instanceof BigDecimal) {
            refreshRate = ((BigDecimal) rawRefreshRate).intValue();
        } else if (rawRefreshRate instanceof Integer) {
            refreshRate = (Integer) rawRefreshRate;
        } else {
            refreshRate = 10;
        }

        if (coordinator != null && (!coordinator.chromeCast.getAddress().equals(host) || (coordinator.chromeCast.getPort() != port))) {
            coordinator.destroy();
            coordinator = null;
        }

        if (coordinator == null) {
            ChromeCast chromecast = new ChromeCast(host, port);
            coordinator = new Coordinator(this, thing, chromecast, refreshRate, audioHTTPServer, callbackUrl);
            coordinator.initialize();
        }
    }

    @Override
    public void dispose() {
        if (coordinator != null) {
            coordinator.destroy();
            coordinator = null;
        }
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        if (coordinator != null) {
            coordinator.commander.handleCommand(channelUID, command);
        } else {
            logger.info("Cannot handle command. No coordinator was initialized");
        }
    }

    @Override  // Just exposing this for ChromecastStatusUpdater.
    public void updateState(String channelID, State state) {
        super.updateState(channelID, state);
    }

    @Override // Just exposing this for ChromecastStatusUpdater.
    public void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, String description) {
        super.updateStatus(status, statusDetail, description);
    }

    @Override // Just exposing this for ChromecastStatusUpdater.
    public boolean isLinked(String channelId) {
        return super.isLinked(channelId);
    }

    @Override
    public String getId() {
        return thing.getUID().toString();
    }

    @Override
    public String getLabel(Locale locale) {
        return thing.getLabel();
    }

    @Override
    public Set<AudioFormat> getSupportedFormats() {
        return SUPPORTED_FORMATS;
    }

    @Override public Set<Class<? extends AudioStream>> getSupportedStreams() {
        return SUPPORTED_STREAMS;
    }

    @Override
    public void process(AudioStream audioStream)
        throws UnsupportedAudioFormatException, UnsupportedAudioStreamException {
        if (coordinator != null) {
            coordinator.audioSink.process(audioStream);
        } else {
            logger.info("Cannot process audioStream. No coordinator has been initialized.");
        }
    }

    @Override
    public PercentType getVolume() throws IOException {
        if (coordinator != null) {
            return coordinator.statusUpdater.getVolume();
        } else {
            logger.info("Cannot get volume. No coordinator has been initialized.");
            return PercentType.ZERO;
        }
    }

    @Override
    public void setVolume(PercentType percentType) throws IOException {
        if (coordinator != null) {
            coordinator.commander.handleVolume(percentType);
        } else {
            logger.info("Cannot set volume. No coordinator has been initialized.");
        }
    }

    private static class Coordinator {
        private static final int CONNECT_DELAY = 10;
        private final Logger logger = LoggerFactory.getLogger(Coordinator.class);

        private final ChromeCast chromeCast;
        private final ChromecastAudioSink audioSink;
        private final ChromecastCommander commander;
        private final ChromecastEventReceiver eventReceiver;
        private final ChromecastStatusUpdater statusUpdater;
        private final ChromecastScheduler scheduler;

        private final Runnable connectRunnable = this::connect;
        private final Runnable refreshRunnable = new Runnable() {
            @Override
            public void run() {
                commander.handleRefresh();
            }
        };

        private Coordinator(ChromecastHandler handler, Thing thing, ChromeCast chromeCast, int refreshRate, AudioHTTPServer audioHttpServer, String callbackURL) {
            this.chromeCast = chromeCast;

            this.scheduler = new ChromecastScheduler(handler.scheduler, CONNECT_DELAY, connectRunnable, refreshRate, refreshRunnable);
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
                statusUpdater.updateStatus(ThingStatus.ONLINE);
            } catch (final Exception e) {
                statusUpdater.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
                scheduler.scheduleConnect();
            }
        }
    }
}
