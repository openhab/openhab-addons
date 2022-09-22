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
package org.openhab.binding.arcam.internal.connection;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.arcam.internal.ArcamBindingConstants;
import org.openhab.binding.arcam.internal.ArcamNowPlaying;
import org.openhab.binding.arcam.internal.ArcamState;
import org.openhab.binding.arcam.internal.ArcamUtil;
import org.openhab.binding.arcam.internal.ArcamZone;
import org.openhab.binding.arcam.internal.devices.ArcamDevice;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ArcamConnection} class manages the socket connection with the device.
 * It will trigger state changes when new messages are received and writes commands to the socket.
 *
 * @author Joep Admiraal - Initial contribution
 */
@NonNullByDefault
public class ArcamConnection implements ArcamSocketListener {
    private final Logger logger = LoggerFactory.getLogger(ArcamConnection.class);

    private ArcamState state;
    private ArcamDevice device;
    private ArcamSocket socket;
    private ArcamConnectionListener connectionListener;
    private ArcamCommandInTransit commandInTransit = new ArcamCommandInTransit();

    @Nullable
    private ArcamCommandCode nowPlayingInTransit;

    public ArcamConnection(ArcamState state, ScheduledExecutorService scheduler,
            ArcamConnectionListener connectionListener, ArcamDevice device, String thingUID) {
        this.state = state;
        this.device = device;
        byte[] heartbeatCommand = device.getHeartbeatCommand();
        this.socket = new ArcamSocket(thingUID, scheduler, heartbeatCommand, this);
        this.connectionListener = connectionListener;
    }

    public void connect(String hostname) throws UnknownHostException, IOException {
        socket.connect(hostname);
    }

    public void dispose() {
        socket.dispose();
    }

    public void reboot() {
        byte[] data = device.getRebootCommand();
        ArcamCommandCode commandCode = ArcamCommandCode.REBOOT;

        logger.debug("Sending reboot array: {}", ArcamUtil.bytesToHex(data));
        sendCommand(data, commandCode);
    }

    public void requestState(String channel) {
        ArcamCommandCode commandCode = ArcamCommandCode.getFromChannel(channel);
        if (commandCode == null) {
            logger.debug("commandCode not found, channel: {}", channel);
            return;
        }

        byte[] data = device.getStateCommandByte(commandCode);
        if (data.length == 0) {
            return;
        }

        sendCommand(data, commandCode);
    }

    public void setBalance(int balance, ArcamZone zone) {
        byte[] data = device.getBalanceCommand(balance, zone);
        ArcamCommandCode commandCode = zone == ArcamZone.MASTER ? ArcamCommandCode.MASTER_BALANCE
                : ArcamCommandCode.ZONE2_BALANCE;

        logger.debug("Sending balance byte: {}, array: {}, zone: {}", balance, ArcamUtil.bytesToHex(data), zone);
        sendCommand(data, commandCode);
    }

    public void setDacFilter(String dacFilter) {
        byte[] data = device.getDacFilterCommand(dacFilter);
        ArcamCommandCode commandCode = ArcamCommandCode.DAC_FILTER;

        logger.debug("Sending dacFilter byte: {}, array: {}", data[4], ArcamUtil.bytesToHex(data));
        sendCommand(data, commandCode);
    }

    public void setDisplayBrightness(String displayBrightness) {
        byte[] data = device.getDisplayBrightnessCommand(displayBrightness);
        ArcamCommandCode commandCode = ArcamCommandCode.DISPLAY_BRIGHTNESS;

        logger.debug("Sending display brightness byte: {}, array: {}", data[4], ArcamUtil.bytesToHex(data));
        sendCommand(data, commandCode);
    }

    public void setInput(String inputStr, ArcamZone zone) {
        byte[] data = device.getInputCommand(inputStr, zone);
        ArcamCommandCode commandCode = zone == ArcamZone.MASTER ? ArcamCommandCode.MASTER_INPUT
                : ArcamCommandCode.ZONE2_INPUT;

        logger.debug("Sending input byte: {}, array: {}", data[4], ArcamUtil.bytesToHex(data));
        sendCommand(data, commandCode);
    }

    public void setMute(boolean on, ArcamZone zone) {
        byte[] data = device.getMuteCommand(on, zone);
        ArcamCommandCode commandCode = zone == ArcamZone.MASTER ? ArcamCommandCode.MASTER_MUTE
                : ArcamCommandCode.ZONE2_MUTE;

        logger.debug("Sending mute byte: {}, array: {}, zone: {}", on, ArcamUtil.bytesToHex(data), zone);
        sendCommand(data, commandCode);
    }

    public void setPower(boolean on, ArcamZone zone) {
        byte[] data = device.getPowerCommand(on, zone);
        ArcamCommandCode commandCode = zone == ArcamZone.MASTER ? ArcamCommandCode.MASTER_POWER
                : ArcamCommandCode.ZONE2_POWER;

        logger.debug("Sending power byte: {}, array: {}, zone: {}", on, ArcamUtil.bytesToHex(data), zone);
        sendCommand(data, commandCode);
    }

    public void setRoomEqualisation(String eqStr, ArcamZone zone) {
        byte[] data = device.getRoomEqualisationCommand(eqStr, zone);
        ArcamCommandCode commandCode = zone == ArcamZone.MASTER ? ArcamCommandCode.MASTER_ROOM_EQUALISATION
                : ArcamCommandCode.ZONE2_ROOM_EQUALISATION;

        logger.debug("Sending eq byte: {}, array: {}", data[4], ArcamUtil.bytesToHex(data));
        sendCommand(data, commandCode);
    }

    public void setVolume(int volume, ArcamZone zone) {
        byte[] data = device.getVolumeCommand(volume, zone);
        ArcamCommandCode commandCode = zone == ArcamZone.MASTER ? ArcamCommandCode.MASTER_VOLUME
                : ArcamCommandCode.ZONE2_VOLUME;

        logger.debug("Sending volume byte: {}, array: {}, zone: {}", volume, ArcamUtil.bytesToHex(data), zone);
        sendCommand(data, commandCode);
    }

    @Override
    public void onResponse(ArcamResponse response) {
        if (response.ac != 0x00) {
            logger.debug("There is an error with command: {}", response.cc);
            return;
        }

        // Balance
        if (response.cc == 0x3B) {
            int balance = device.getBalance(response.data.get(0));
            if (isMasterZone(response.zn)) {
                setState(ArcamBindingConstants.CHANNEL_MASTER_BALANCE, balance);
            } else {
                setState(ArcamBindingConstants.CHANNEL_ZONE2_BALANCE, balance);
            }
        }
        // DAC filter
        if (response.cc == 0x61) {
            String dacFilter = device.getDacFilter(response.data.get(0));
            logger.debug("Got DAC filter: {}, {}", ArcamUtil.bytesToHex(response.data), dacFilter);
            setState(ArcamBindingConstants.CHANNEL_DAC_FILTER, dacFilter);
        }
        // DC offset
        if (response.cc == 0x51) {
            logger.debug("Got DC offset response: {}", response.data);
            boolean dc = device.getBoolean(response.data.get(0));
            setState(ArcamBindingConstants.CHANNEL_DC_OFFSET, dc);
        }
        // Direct mode
        if (response.cc == 0x0F) {
            logger.debug("Got direct mode response: {}", response.data);
            if (response.data.size() > 1) {
                boolean directMode = device.getBoolean(response.data.get(1));
                setState(ArcamBindingConstants.CHANNEL_MASTER_DIRECT_MODE, directMode);
            }
        }
        // Display brightness
        if (response.cc == 0x01) {
            String brightness = device.getDisplayBrightness(response.data.get(0));

            logger.debug("brightness info: {}", brightness);
            setState(ArcamBindingConstants.CHANNEL_DISPLAY_BRIGHTNESS, brightness);
        }
        // Headphones
        if (response.cc == 0x02) {
            logger.debug("Got headphones response: {}", response.data);
            boolean headphones = device.getBoolean(response.data.get(0));
            setState(ArcamBindingConstants.CHANNEL_HEADPHONES, headphones);
        }
        // Incoming audio sample rate
        if (response.cc == 0x44) {
            String sampleRate = device.getIncomingSampleRate(response.data.get(0));
            logger.debug("Got incomingSampleRateresponse: {}, {}", ArcamUtil.bytesToHex(response.data), sampleRate);
            setState(ArcamBindingConstants.CHANNEL_INCOMING_SAMPLE_RATE, sampleRate);
        }
        // Input detect
        if (response.cc == 0x5A) {
            logger.debug("Got Input detect response: {}", response.data);
            boolean inputDetect = device.getBoolean(response.data.get(0));
            setState(ArcamBindingConstants.CHANNEL_MASTER_INPUT_DETECT, inputDetect);
        }
        // Input source
        if (response.cc == 0x1D) {
            String input = device.getInputName(response.data.get(0));

            if (isMasterZone(response.zn)) {
                setState(ArcamBindingConstants.CHANNEL_MASTER_INPUT, input);
            } else {
                setState(ArcamBindingConstants.CHANNEL_ZONE2_INPUT, input);
            }
        }
        // Lifter temperature
        if (response.cc == 0x56) {
            int temperature = device.getTemperature(response.data, 0);
            logger.debug("Got Lifter temperature: {}, value: {}", ArcamUtil.bytesToHex(response.data), temperature);
            setState(ArcamBindingConstants.CHANNEL_LIFTER_TEMPERATURE, temperature);
        }
        // Mute
        if (response.cc == 0x0E) {
            logger.debug("Got mute response: {}", response.data);
            boolean mute = device.getMute(response.data.get(0));
            if (isMasterZone(response.zn)) {
                setState(ArcamBindingConstants.CHANNEL_MASTER_MUTE, mute);
            } else {
                setState(ArcamBindingConstants.CHANNEL_ZONE2_MUTE, mute);
            }
        }
        // Now Playing information
        if (response.cc == 0x64) {
            logger.debug("Got now playing response: {}", response.data);
            ArcamCommandCode nowPlayingCommandCode = nowPlayingInTransit;
            if (nowPlayingCommandCode != null) {
                String value = ArcamUtil.byteListToUTF(response.data);

                switch (nowPlayingCommandCode) {
                    case MASTER_NOW_PLAYING_ALBUM:
                        setState(ArcamBindingConstants.CHANNEL_MASTER_NOW_PLAYING_ALBUM, value);
                        break;

                    case MASTER_NOW_PLAYING_APPLICATION:
                        setState(ArcamBindingConstants.CHANNEL_MASTER_NOW_PLAYING_APPLICATION, value);
                        break;

                    case MASTER_NOW_PLAYING_ARTIST:
                        setState(ArcamBindingConstants.CHANNEL_MASTER_NOW_PLAYING_ARTIST, value);
                        break;

                    case MASTER_NOW_PLAYING_AUDIO_ENCODER:
                        String audioEncoder = device.getNowPlayingEncoder(response.data.get(0));
                        setState(ArcamBindingConstants.CHANNEL_MASTER_NOW_PLAYING_AUDIO_ENCODER, audioEncoder);
                        break;

                    case MASTER_NOW_PLAYING_SAMPLE_RATE:
                        String sampleRate = device.getNowPlayingSampleRate(response.data.get(0));
                        setState(ArcamBindingConstants.CHANNEL_MASTER_NOW_PLAYING_SAMPLE_RATE, sampleRate);
                        break;

                    case MASTER_NOW_PLAYING_TITLE:
                        setState(ArcamBindingConstants.CHANNEL_MASTER_NOW_PLAYING_TITLE, value);
                        break;
                    default:
                        break;
                }
                nowPlayingInTransit = null;
            } else {
                ArcamNowPlaying nowPlaying = device.setNowPlaying(response.data);
                if (nowPlaying != null) {
                    setState(ArcamBindingConstants.CHANNEL_MASTER_NOW_PLAYING_ALBUM, nowPlaying.album);
                    setState(ArcamBindingConstants.CHANNEL_MASTER_NOW_PLAYING_APPLICATION, nowPlaying.application);
                    setState(ArcamBindingConstants.CHANNEL_MASTER_NOW_PLAYING_ARTIST, nowPlaying.artist);
                    setState(ArcamBindingConstants.CHANNEL_MASTER_NOW_PLAYING_AUDIO_ENCODER, nowPlaying.audioEncoder);
                    setState(ArcamBindingConstants.CHANNEL_MASTER_NOW_PLAYING_SAMPLE_RATE, nowPlaying.sampleRate);
                    setState(ArcamBindingConstants.CHANNEL_MASTER_NOW_PLAYING_TITLE, nowPlaying.track);
                }
            }
        }
        // Output temperature
        if (response.cc == 0x57) {
            int temperature = device.getTemperature(response.data, 0);
            logger.debug("Got Output temperature: {}, value: {}", ArcamUtil.bytesToHex(response.data), temperature);
            setState(ArcamBindingConstants.CHANNEL_OUTPUT_TEMPERATURE, temperature);
        }
        // Power
        if (response.cc == 0x00) {
            logger.debug("Got power response: {}", response.data);
            boolean power = device.getBoolean(response.data.get(0));
            if (isMasterZone(response.zn)) {
                setState(ArcamBindingConstants.CHANNEL_MASTER_POWER, power);
            } else {
                setState(ArcamBindingConstants.CHANNEL_ZONE2_POWER, power);
            }
        }
        // Room Equalisation
        if (response.cc == 0x37) {
            String eq = device.getRoomEqualisation(response.data.get(0));
            if (isMasterZone(response.zn)) {
                setState(ArcamBindingConstants.CHANNEL_MASTER_ROOM_EQUALISATION, eq);
            } else {
                setState(ArcamBindingConstants.CHANNEL_ZONE2_ROOM_EQUALISATION, eq);
            }
        }
        // Short circuit status
        if (response.cc == 0x52) {
            logger.debug("Got Short circuit status response: {}", response.data);
            boolean shortCircuit = device.getBoolean(response.data.get(0));
            setState(ArcamBindingConstants.CHANNEL_MASTER_SHORT_CIRCUIT, shortCircuit);
        }
        // SoftwareVersion
        if (response.cc == 0x04) {
            String version = device.getSoftwareVersion(response.data);
            logger.debug("Got SoftwareVersion response: {}, {}", ArcamUtil.bytesToHex(response.data), version);
            setState(ArcamBindingConstants.CHANNEL_SOFTWARE_VERSION, version);
        }
        // Timeout counter
        if (response.cc == 0x55) {
            int counter = device.getTimeoutCounter(response.data);
            logger.debug("Got Timeout counter response: {}, {}", ArcamUtil.bytesToHex(response.data), counter);
            setState(ArcamBindingConstants.CHANNEL_TIMEOUT_COUNTER, counter);
        }
        // Volume
        if (response.cc == 0x0D) {
            int volume = Byte.valueOf(response.data.get(0)).intValue();
            logger.debug("Got volume response: {}, {}", ArcamUtil.bytesToHex(response.data), volume);
            if (isMasterZone(response.zn)) {
                setPercentageState(ArcamBindingConstants.CHANNEL_MASTER_VOLUME, volume);
            } else {
                setPercentageState(ArcamBindingConstants.CHANNEL_ZONE2_VOLUME, volume);
            }
        }
    }

    private boolean isMasterZone(byte zone) {
        return zone == 0x01;
    }

    private void finishInTransit(String channelId) {
        ArcamCommandCode commandCode = ArcamCommandCode.getFromChannel(channelId);
        if (commandCode == null) {
            return;
        }

        commandInTransit.finish(commandCode);
    }

    private void setState(String channelId, @Nullable String value) {
        finishInTransit(channelId);
        StringType newValue = new StringType(value);
        state.setState(channelId, newValue);
    }

    private void setState(String channelId, int value) {
        finishInTransit(channelId);
        DecimalType newValue = new DecimalType(value);
        state.setState(channelId, newValue);
    }

    private void setPercentageState(String channelId, int value) {
        finishInTransit(channelId);
        PercentType newValue = new PercentType(value);
        state.setState(channelId, newValue);
    }

    private void setState(String channelId, boolean value) {
        finishInTransit(channelId);
        OnOffType newValue = value ? OnOffType.ON : OnOffType.OFF;
        state.setState(channelId, newValue);
    }

    private void sendCommand(byte[] data, ArcamCommandCode commandCode) {
        commandInTransit.set(commandCode);

        if (data[2] == 0x64) {
            nowPlayingInTransit = commandCode;
        }

        socket.sendCommand(data);

        commandInTransit.waitFor();
    }

    @Override
    public void onConnection() {
        connectionListener.onConnection();
    }

    @Override
    public void onError() {
        connectionListener.onError();
    }
}
