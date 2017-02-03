/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.chromecast.handler;

import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.audio.AudioFormat;
import org.eclipse.smarthome.core.audio.AudioHTTPServer;
import org.eclipse.smarthome.core.audio.AudioSink;
import org.eclipse.smarthome.core.audio.AudioStream;
import org.eclipse.smarthome.core.audio.FixedLengthAudioStream;
import org.eclipse.smarthome.core.audio.URLAudioStream;
import org.eclipse.smarthome.core.audio.UnsupportedAudioFormatException;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.chromecast.ChromecastBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import su.litvak.chromecast.api.v2.Application;
import su.litvak.chromecast.api.v2.ChromeCast;
import su.litvak.chromecast.api.v2.ChromeCastSpontaneousEvent;
import su.litvak.chromecast.api.v2.ChromeCastSpontaneousEventListener;
import su.litvak.chromecast.api.v2.MediaStatus;
import su.litvak.chromecast.api.v2.Status;
import su.litvak.chromecast.api.v2.Volume;

/**
 * The {@link ChromecastHandler} is responsible for handling commands, which are
 * sent to one of the channels. It furthermore implements {@link AudioSink} support.
 *
 * @author Markus Rathgeb - Original author
 * @author Kai Kreuzer - Initial contribution as openHAB add-on
 * @author Daniel Walters - Online status fix, handle playuri channel and refactor play media code
 *
 */
public class ChromecastHandler extends BaseThingHandler implements ChromeCastSpontaneousEventListener, AudioSink {

    private static final String MEDIA_PLAYER = "CC1AD845";

    private static AudioFormat mp3 = new AudioFormat(AudioFormat.CONTAINER_NONE, AudioFormat.CODEC_MP3, null, null,
            null, null);
    private static AudioFormat wav = new AudioFormat(AudioFormat.CONTAINER_WAVE, AudioFormat.CODEC_PCM_SIGNED, null,
            null, null, null);
    private static HashSet<AudioFormat> supportedFormats = new HashSet<>();

    static {
        supportedFormats.add(wav);
        supportedFormats.add(mp3);
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private AudioHTTPServer audioHTTPServer;
    private ChromeCast chromecast;
    private ScheduledFuture<?> futureConnect;
    private PercentType volume;
    private String callbackUrl;

    /**
     * Constructor.
     *
     * @param thing the thing the handler should be created for
     * @param audioHTTPServer server for hosting audio streams
     * @param callbackUrl url to be used to tell the Chromecast which host to call for audio urls
     */
    public ChromecastHandler(final Thing thing, AudioHTTPServer audioHTTPServer, String callbackUrl) {
        super(thing);
        this.audioHTTPServer = audioHTTPServer;
        this.callbackUrl = callbackUrl;
    }

    private void createChromecast(final String address) {
        try {
            chromecast = new ChromeCast(address);
            chromecast.registerListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void destroyChromecast() {
        chromecast.unregisterListener(this);
        try {
            chromecast.disconnect();
        } catch (final IOException ex) {
            logger.debug("Disconnect failed.", ex);
        }
        chromecast = null;
    }

    private void scheduleConnect(final boolean immediate) {
        final long delay = immediate ? 0 : 10;
        if (futureConnect != null) {
            futureConnect.cancel(true);
            futureConnect = null;
        }
        futureConnect = scheduler.schedule(() -> {
            try {
                chromecast.connect();
                // assume device is online as we no longer get notified
                updateStatus(ThingStatus.ONLINE);
            } catch (final Exception e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
                scheduleConnect(false);
            }
        }, delay, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        if (chromecast != null) {
            destroyChromecast();
        }
    }

    @Override
    public void initialize() {
        final Object obj = getConfig().get(ChromecastBindingConstants.HOST);
        if (!(obj instanceof String)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Cannot connect to Chromecast. IP address is invalid.");
            return;
        }
        final String host = (String) obj;
        if (StringUtils.isBlank(host)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Cannot connect to Chromecast. IP address is not set.");
            return;
        }

        if (chromecast != null && !chromecast.getAddress().equals(host)) {
            destroyChromecast();
        }
        if (chromecast == null) {
            createChromecast(host);
        }

        scheduleConnect(true);
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        if (chromecast == null) {
            return;
        }

        switch (channelUID.getId()) {
            case ChromecastBindingConstants.CHANNEL_CONTROL:
                handleControl(command);
                break;
            case ChromecastBindingConstants.CHANNEL_VOLUME:
                handleVolume(command);
                break;
            case ChromecastBindingConstants.CHANNEL_PLAY_URI:
                handlePlayUri(command);
                break;
            default:
                logger.debug("Received command {} for unknown channel: {}", command, channelUID);
                break;
        }
    }

    private void handlePlayUri(Command command) {
        if (command instanceof StringType) {
            playMedia(null, null, command.toString(), null);
        }
    }

    private void handleControl(final Command command) {
        try {
            if (command instanceof PlayPauseType) {
                final PlayPauseType playPause = (PlayPauseType) command;
                if (playPause == PlayPauseType.PLAY) {
                    chromecast.play();
                } else {
                    chromecast.pause();
                }
            }
            updateStatus(ThingStatus.ONLINE);
        } catch (final IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void handleVolume(final Command command) {
        if (command instanceof PercentType) {
            final PercentType num = (PercentType) command;
            try {
                chromecast.setVolume(num.floatValue() / 100);
            } catch (final IOException ex) {
                logger.debug("Set volume failed.", ex);
            }
        }
    }

    private void handleCcStatus(final Status status) {
        logger.debug("STATUS {}", status);
        handleCcVolume(status.volume);
    }

    private void handleCcMediaStatus(final MediaStatus mediaStatus) {
        logger.debug("MEDIA_STATUS {}", mediaStatus);
        switch (mediaStatus.playerState) {
            case IDLE:
            case PAUSED:
                updateState(new ChannelUID(getThing().getUID(), ChromecastBindingConstants.CHANNEL_CONTROL),
                        PlayPauseType.PAUSE);
                break;
            case BUFFERING:
            case PLAYING:
                updateState(new ChannelUID(getThing().getUID(), ChromecastBindingConstants.CHANNEL_CONTROL),
                        PlayPauseType.PLAY);
                break;
            default:
                break;
        }
    }

    private void handleCcVolume(final Volume volume) {
        PercentType value = new PercentType((int) (volume.level * 100));
        updateState(new ChannelUID(getThing().getUID(), ChromecastBindingConstants.CHANNEL_VOLUME), value);
        this.volume = value;
    }

    @Override
    public void spontaneousEventReceived(final ChromeCastSpontaneousEvent event) {
        switch (event.getType()) {
            case MEDIA_STATUS:
                final MediaStatus mediaStatus = event.getData(MediaStatus.class);
                handleCcMediaStatus(mediaStatus);
                break;
            case STATUS:
                final Status status = event.getData(Status.class);
                handleCcStatus(status);
                break;
            case UNKNOWN:
                logger.warn("Received an 'UNKNOWN' event (class={})", event.getType().getDataClass());
                break;
            default:
                logger.debug("Unhandled event type: {}", event.getData());
                break;

        }
    }

    @Override
    public String getId() {
        return getThing().getUID().toString();
    }

    @Override
    public String getLabel(Locale locale) {
        return getThing().getLabel();
    }

    @Override
    public void process(AudioStream audioStream) throws UnsupportedAudioFormatException {
        String url = null;
        if (audioStream instanceof URLAudioStream) {
            // it is an external URL, the speaker can access it itself and play it.
            URLAudioStream urlAudioStream = (URLAudioStream) audioStream;
            url = urlAudioStream.getURL();
        } else {
            if (callbackUrl != null) {
                // we serve it on our own HTTP server
                String relativeUrl;
                if (audioStream instanceof FixedLengthAudioStream) {
                    relativeUrl = audioHTTPServer.serve((FixedLengthAudioStream) audioStream, 10);
                } else {
                    relativeUrl = audioHTTPServer.serve(audioStream);
                }
                url = callbackUrl + relativeUrl;
            } else {
                logger.warn("We do not have any callback url, so Chromecast cannot play the audio stream!");
                return;
            }
        }
        String mimeType = audioStream.getFormat().getCodec() == AudioFormat.CODEC_MP3 ? "audio/mpeg" : "audio/wav";
        playMedia("Notification", null, url, mimeType);
    }

    private void playMedia(String title, String imgUrl, String url, String mimeType) {
        try {
            if (chromecast.isAppAvailable(MEDIA_PLAYER)) {
                if (!chromecast.isAppRunning(MEDIA_PLAYER)) {
                    final Application app = chromecast.launchApp(MEDIA_PLAYER);
                    logger.debug("Application launched: {}", app);
                }
                if (url != null) {
                    chromecast.load(title, imgUrl, url, mimeType);
                }
            } else {
                logger.error("Missing media player app - cannot process media.");
            }
        } catch (final IOException e) {
            logger.debug("Failed playing media: {}", e.getMessage());
        }
    }

    @Override
    public Set<AudioFormat> getSupportedFormats() {
        return supportedFormats;
    }

    @Override
    public PercentType getVolume() throws IOException {
        return volume;
    }

    @Override
    public void setVolume(PercentType volume) throws IOException {
        handleVolume(volume);
    }

}
