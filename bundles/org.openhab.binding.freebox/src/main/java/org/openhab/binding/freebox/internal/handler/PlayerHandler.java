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
package org.openhab.binding.freebox.internal.handler;

import static org.eclipse.smarthome.core.audio.AudioFormat.*;
import static org.openhab.binding.freebox.internal.FreeboxBindingConstants.*;

import java.io.IOException;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.audio.AudioFormat;
import org.eclipse.smarthome.core.audio.AudioHTTPServer;
import org.eclipse.smarthome.core.audio.AudioSink;
import org.eclipse.smarthome.core.audio.AudioStream;
import org.eclipse.smarthome.core.audio.FixedLengthAudioStream;
import org.eclipse.smarthome.core.audio.URLAudioStream;
import org.eclipse.smarthome.core.audio.UnsupportedAudioFormatException;
import org.eclipse.smarthome.core.audio.UnsupportedAudioStreamException;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.freebox.internal.action.PlayerActions;
import org.openhab.binding.freebox.internal.api.APIRequests;
import org.openhab.binding.freebox.internal.api.FreeboxException;
import org.openhab.binding.freebox.internal.api.model.AirMediaActionData.MediaAction;
import org.openhab.binding.freebox.internal.api.model.AirMediaActionData.MediaType;
import org.openhab.binding.freebox.internal.api.model.AirMediaConfig;
import org.openhab.binding.freebox.internal.api.model.AirMediaReceiver;
import org.openhab.binding.freebox.internal.api.model.LanConfig.NetworkMode;
import org.openhab.binding.freebox.internal.api.model.LanHostName.NameSource;
import org.openhab.binding.freebox.internal.config.PlayerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PlayerHandler} is responsible for handling everything associated to
 * any Freebox Player thing type.
 *
 * @author Gaël L'hopital - Initial contribution
 *         https://github.com/betonniere/freeteuse/
 *         https://github.com/MaximeCheramy/remotefreebox/blob/16e2a42ed7cfcfd1ab303184280564eeace77919/remotefreebox/fbx_descriptor.py
 *         https://dev.freebox.fr/sdk/freebox_player_1.1.4_codes.html
 *         http://192.168.0.98/pub/remote_control?code=78952520&key=1&long=true
 */
@NonNullByDefault
public class PlayerHandler extends HostHandler implements AudioSink {
    private static final int HTTP_CALL_DEFAULT_TIMEOUT_MS = (int) TimeUnit.SECONDS.toMillis(300);
    private static final Set<Class<? extends AudioStream>> SUPPORTED_STREAMS = Collections.singleton(AudioStream.class);
    private static final Set<AudioFormat> BASIC_FORMATS = Collections
            .unmodifiableSet(Stream.of(WAV, OGG).collect(Collectors.toSet()));
    private static final Set<AudioFormat> ALL_MP3_FORMATS = Collections
            .unmodifiableSet(Stream
                    .of(new AudioFormat(CONTAINER_NONE, CODEC_MP3, null, null, 96000, null),
                            new AudioFormat(CONTAINER_NONE, CODEC_MP3, null, null, 112000, null),
                            new AudioFormat(CONTAINER_NONE, CODEC_MP3, null, null, 128000, null),
                            new AudioFormat(CONTAINER_NONE, CODEC_MP3, null, null, 160000, null),
                            new AudioFormat(CONTAINER_NONE, CODEC_MP3, null, null, 192000, null),
                            new AudioFormat(CONTAINER_NONE, CODEC_MP3, null, null, 224000, null),
                            new AudioFormat(CONTAINER_NONE, CODEC_MP3, null, null, 256000, null),
                            new AudioFormat(CONTAINER_NONE, CODEC_MP3, null, null, 320000, null))
                    .collect(Collectors.toSet()));
    private static final Set<String> VALID_REMOTE_KEYS = Collections
            .unmodifiableSet(new HashSet<>(Arrays.asList("red", "green", "blue", "yellow", "power", "list", "tv", "0",
                    "1", "2", "3", "4", "5", "6", "7", "8", "9", "vol_inc", "vol_dec", "mute", "prgm_inc", "prgm_dec",
                    "prev", "bwd", "play", "rec", "fwd", "next", "up", "right", "down", "left", "back", "swap", "info",
                    "epg", "mail", "media", "help", "options", "pip", "ok", "home")));

    private final Logger logger = LoggerFactory.getLogger(PlayerHandler.class);
    private final AudioHTTPServer audioHTTPServer;
    private final @Nullable String callbackUrl;
    private final Set<AudioFormat> SUPPORTED_FORMATS = new HashSet<>();
    private String playerName = "";
    private String baseAddress = "";
    private @NonNullByDefault({}) PlayerConfiguration configuration;

    public PlayerHandler(Thing thing, ZoneId zoneId, AudioHTTPServer audioHTTPServer, @Nullable String callbackUrl) {
        super(thing, zoneId);
        this.audioHTTPServer = audioHTTPServer;
        this.callbackUrl = callbackUrl;
    }

    @Override
    public void initialize() {
        super.initialize();
        playerName = this.editProperties().get(NameSource.UPNP.name());
        configuration = getConfigAs(PlayerConfiguration.class);
    }

    @Override
    protected Map<String, String> discoverAttributes() throws FreeboxException {
        final Map<String, String> properties = super.discoverAttributes();
        List<AirMediaReceiver> devices = getApiManager().execute(new APIRequests.AirMediaReceivers());
        Optional<AirMediaReceiver> matching = devices.stream()
                .filter(device -> properties.get(NameSource.UPNP.name()).equals(device.getName())).findFirst();

        if (matching.isPresent()) {
            properties.put(PROPERTY_AUDIO, Boolean.valueOf(matching.get().isAudioCapable()).toString());
            properties.put(PROPERTY_VIDEO, Boolean.valueOf(matching.get().isVideoCapable()).toString());
            properties.put(PROPERTY_PHOTO, Boolean.valueOf(matching.get().isPhotoCapable()).toString());
            properties.put(PROPERTY_SCREEN, Boolean.valueOf(matching.get().isScreenCapable()).toString());
        }
        return properties;
    }

    @Override
    protected void internalPoll() throws FreeboxException {
        super.internalPoll();
        if (bridgeHandler.getNetworkMode() != NetworkMode.BRIDGE) {
            AirMediaConfig response = bridgeHandler.getApiManager().execute(new APIRequests.GetAirMediaConfig());
            updateChannelOnOff(PLAYER_ACTIONS, AIRMEDIA_STATUS, response.isEnabled());
        } else {
            updateChannelOnOff(PLAYER_ACTIONS, AIRMEDIA_STATUS, false);
        }
        baseAddress = String.format("http://%s/pub/remote_control?code=%s&key=", ipAddress, configuration.remoteCode);
    }

    @Override
    protected boolean internalHandleCommand(ChannelUID channelUID, Command command) throws FreeboxException {
        if (AIRMEDIA_STATUS.equals(channelUID.getIdWithoutGroup()) && (command instanceof OnOffType
                || command instanceof OpenClosedType || command instanceof UpDownType)) {
            boolean enable = command.equals(OnOffType.ON) || command.equals(UpDownType.UP)
                    || command.equals(OpenClosedType.OPEN);
            updateState(new ChannelUID(getThing().getUID(), PLAYER_ACTIONS, AIRMEDIA_STATUS),
                    OnOffType.from(enableAirMedia(enable)));
            return true;
        } else if (KEY_CODE.equals(channelUID.getIdWithoutGroup()) && command instanceof StringType) {
            sendKey(command.toString(), false, 1);
        }

        return super.internalHandleCommand(channelUID, command);
    }

    @Override
    public void process(@Nullable AudioStream audioStream)
            throws UnsupportedAudioFormatException, UnsupportedAudioStreamException {
        if (getThing().getStatus() == ThingStatus.ONLINE) {
            if (audioStream == null) {
                try {
                    getApiManager().execute(new APIRequests.AirMediaAction(playerName, configuration.password,
                            MediaAction.STOP, MediaType.VIDEO));
                } catch (FreeboxException e) {
                    logger.warn("Exception while stopping audio stream playback: {}", e.getMessage());
                }
            } else {
                String url = null;
                if (audioStream instanceof URLAudioStream) {
                    // it is an external URL, we can access it directly
                    URLAudioStream urlAudioStream = (URLAudioStream) audioStream;
                    url = urlAudioStream.getURL();
                } else {
                    // we serve it on our own HTTP server
                    String relativeUrl = "";
                    if (audioStream instanceof FixedLengthAudioStream) {
                        relativeUrl = audioHTTPServer.serve((FixedLengthAudioStream) audioStream, 20);
                    } else {
                        relativeUrl = audioHTTPServer.serve(audioStream);
                    }
                    url = callbackUrl + relativeUrl;
                }
                try {
                    audioStream.close();
                    try {
                        logger.debug("AirPlay audio sink: process url {}", url);
                        getApiManager().execute(new APIRequests.AirMediaAction(playerName, configuration.password,
                                MediaAction.START, MediaType.VIDEO, url));
                    } catch (FreeboxException e) {
                        logger.warn("Audio stream playback failed: {}", e.getMessage());
                    }
                } catch (IOException e) {
                    logger.debug("Exception while closing audioStream", e);
                }
            }
        }

    }

    public boolean enableAirMedia(boolean enable) throws FreeboxException {
        AirMediaConfig config = getApiManager().execute(new APIRequests.SetAirMediaConfig(enable));
        return config.isEnabled();
    }

    @Override
    public Set<AudioFormat> getSupportedFormats() {
        if (SUPPORTED_FORMATS.isEmpty()) {
            SUPPORTED_FORMATS.addAll(BASIC_FORMATS);
            if (configuration.acceptAllMp3) {
                SUPPORTED_FORMATS.add(MP3);
            } else { // Only accept MP3 bitrates >= 96 kbps
                SUPPORTED_FORMATS.addAll(ALL_MP3_FORMATS);
            }
        }
        return SUPPORTED_FORMATS;
    }

    @Override
    public String getId() {
        return getThing().getUID().toString();
    }

    @Override
    public @Nullable String getLabel(@Nullable Locale locale) {
        return getThing().getLabel();
    }

    @Override
    public Set<Class<? extends AudioStream>> getSupportedStreams() {
        return SUPPORTED_STREAMS;
    }

    @Override
    public PercentType getVolume() throws IOException {
        logger.info("getVolume received but AirMedia does not have the capability - returning 100%.");
        return PercentType.HUNDRED;
    }

    @Override
    public void setVolume(PercentType volume) throws IOException {
        logger.info("setVolume received but AirMedia does not have the capability - ignoring it.");
    }

    public void sendKey(String key, boolean longPress, int count) {
        String aKey = key.toLowerCase();
        if (VALID_REMOTE_KEYS.contains(aKey)) {
            StringBuilder urlBuilder = new StringBuilder(baseAddress);
            urlBuilder.append(aKey);
            if (longPress) {
                urlBuilder.append("&long=true");
            }
            if (count > 1) {
                urlBuilder.append(String.format("&repeat=%d", count));
            }
            String url = urlBuilder.toString();
            try {
                HttpUtil.executeUrl("GET", url, null, null, null, HTTP_CALL_DEFAULT_TIMEOUT_MS);
            } catch (IOException e) {
                logger.warn("Error calling Player url {} : {}", url, e);
            }
        } else {
            logger.info("Key '{}' is not a valid key expression", key);
        }

    }

    public void sendMultipleKeys(String keys) {
        String[] keyChain = keys.split(",");
        Arrays.stream(keyChain).forEach(key -> sendKey(key, false, 1));
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singletonList(PlayerActions.class);
    }

}
