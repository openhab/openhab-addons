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
package org.openhab.binding.rotel.internal.communication;

import java.io.InterruptedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.rotel.internal.RotelException;
import org.openhab.binding.rotel.internal.RotelModel;
import org.openhab.binding.rotel.internal.RotelPlayStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for simulating the communication with the Rotel device
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class RotelSimuConnector extends RotelConnector {

    private final Logger logger = LoggerFactory.getLogger(RotelSimuConnector.class);

    private static final int STEP_TONE_LEVEL = 1;

    private Object lock = new Object();

    private byte[] feedbackMsg = new byte[1];
    private int idxInFeedbackMsg = feedbackMsg.length;

    private boolean power;
    private boolean powerZone2;
    private boolean powerZone3;
    private boolean powerZone4;
    private RotelSource source = RotelSource.CAT0_CD;
    private RotelSource recordSource = RotelSource.CAT1_CD;
    private RotelSource sourceZone2 = RotelSource.CAT1_CD;
    private RotelSource sourceZone3 = RotelSource.CAT1_CD;
    private RotelSource sourceZone4 = RotelSource.CAT1_CD;
    private boolean multiinput;
    private RotelDsp dsp = RotelDsp.CAT4_NONE;
    private int volume = 50;
    private boolean mute;
    private int volumeZone2 = 20;
    private boolean muteZone2;
    private int volumeZone3 = 30;
    private boolean muteZone3;
    private int volumeZone4 = 40;
    private boolean muteZone4;
    private int bass;
    private int treble;
    private boolean showTreble;
    private RotelPlayStatus playStatus = RotelPlayStatus.STOPPED;
    private int track = 1;
    private boolean selectingRecord;
    private int showZone;
    private int dimmer;

    private int minVolume;
    private int maxVolume;
    private int minToneLevel;
    private int maxToneLevel;

    /**
     * Constructor
     *
     * @param model the projector model in use
     * @param protocol the protocol to be used
     * @param readerThreadName the name of thread to be created
     */
    public RotelSimuConnector(RotelModel model, RotelProtocol protocol, Map<RotelSource, String> sourcesLabels,
            String readerThreadName) {
        super(model, protocol, sourcesLabels, true, readerThreadName);
        this.minVolume = 0;
        this.maxVolume = model.hasVolumeControl() ? model.getVolumeMax() : 0;
        this.maxToneLevel = model.hasToneControl() ? model.getToneLevelMax() : 0;
        this.minToneLevel = -this.maxToneLevel;
    }

    @Override
    public synchronized void open() throws RotelException {
        logger.debug("Opening simulated connection");
        Thread thread = new RotelReaderThread(this, readerThreadName);
        setReaderThread(thread);
        thread.start();
        setConnected(true);
        logger.debug("Simulated connection opened");
    }

    @Override
    public synchronized void close() {
        logger.debug("Closing simulated connection");
        super.cleanup();
        setConnected(false);
        logger.debug("Simulated connection closed");
    }

    @Override
    protected int readInput(byte[] dataBuffer) throws RotelException, InterruptedIOException {
        synchronized (lock) {
            int len = feedbackMsg.length - idxInFeedbackMsg;
            if (len > 0) {
                if (len > dataBuffer.length) {
                    len = dataBuffer.length;
                }
                System.arraycopy(feedbackMsg, idxInFeedbackMsg, dataBuffer, 0, len);
                idxInFeedbackMsg += len;
                return len;
            }
        }
        // Give more chance to someone else than the reader thread to get the lock
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return 0;
    }

    @Override
    public void sendCommand(RotelCommand cmd, @Nullable Integer value) throws RotelException {
        super.sendCommand(cmd, value);
        if ((getProtocol() == RotelProtocol.HEX && cmd.getHexType() != 0)
                || (getProtocol() == RotelProtocol.ASCII_V1 && cmd.getAsciiCommandV1() != null)
                || (getProtocol() == RotelProtocol.ASCII_V2 && cmd.getAsciiCommandV2() != null)) {
            buildFeedbackMessage(cmd, value);
        }
    }

    /**
     * Built the simulated feedback message for a sent command
     *
     * @param cmd the sent command
     * @param value the integer value considered in the sent command for volume, bass or treble adjustment
     */
    private void buildFeedbackMessage(RotelCommand cmd, @Nullable Integer value) {
        String text = buildSourceLine1Response();
        String textLine1Left = buildSourceLine1LeftResponse();
        String textLine1Right = buildVolumeLine1RightResponse();
        String textLine2 = "";
        String textAscii = "";
        boolean accepted = true;
        boolean resetZone = true;
        switch (cmd) {
            case DISPLAY_REFRESH:
                break;
            case POWER_OFF:
            case MAIN_ZONE_POWER_OFF:
                power = false;
                text = buildSourceLine1Response();
                textLine1Left = buildSourceLine1LeftResponse();
                textLine1Right = buildVolumeLine1RightResponse();
                textAscii = buildPowerAsciiResponse();
                break;
            case POWER_ON:
            case MAIN_ZONE_POWER_ON:
                power = true;
                text = buildSourceLine1Response();
                textLine1Left = buildSourceLine1LeftResponse();
                textLine1Right = buildVolumeLine1RightResponse();
                textAscii = buildPowerAsciiResponse();
                break;
            case POWER:
                textAscii = buildPowerAsciiResponse();
                break;
            case ZONE2_POWER_OFF:
                powerZone2 = false;
                text = textLine2 = buildZonePowerResponse(getModel().getNbAdditionalZones() > 1 ? "ZONE2" : "ZONE",
                        powerZone2, sourceZone2);
                showZone = 2;
                resetZone = false;
                break;
            case ZONE2_POWER_ON:
                powerZone2 = true;
                text = textLine2 = buildZonePowerResponse(getModel().getNbAdditionalZones() > 1 ? "ZONE2" : "ZONE",
                        powerZone2, sourceZone2);
                showZone = 2;
                resetZone = false;
                break;
            case ZONE3_POWER_OFF:
                powerZone3 = false;
                text = textLine2 = buildZonePowerResponse("ZONE3", powerZone3, sourceZone3);
                showZone = 3;
                resetZone = false;
                break;
            case ZONE3_POWER_ON:
                powerZone3 = true;
                text = textLine2 = buildZonePowerResponse("ZONE3", powerZone3, sourceZone3);
                showZone = 3;
                resetZone = false;
                break;
            case ZONE4_POWER_OFF:
                powerZone4 = false;
                text = textLine2 = buildZonePowerResponse("ZONE4", powerZone4, sourceZone4);
                showZone = 4;
                resetZone = false;
                break;
            case ZONE4_POWER_ON:
                powerZone4 = true;
                text = textLine2 = buildZonePowerResponse("ZONE4", powerZone4, sourceZone4);
                showZone = 4;
                resetZone = false;
                break;
            case RECORD_FONCTION_SELECT:
                if (getModel().getNbAdditionalZones() >= 1 && getModel().getZoneSelectCmd() == cmd) {
                    showZone++;
                    if (showZone > getModel().getNbAdditionalZones()) {
                        showZone = 1;
                        if (!power) {
                            showZone++;
                        }
                    }
                } else {
                    showZone = 1;
                }
                if (showZone == 1) {
                    selectingRecord = power;
                    showTreble = false;
                    textLine2 = buildRecordResponse();
                } else if (showZone == 2) {
                    selectingRecord = false;
                    text = textLine2 = buildZonePowerResponse(getModel().getNbAdditionalZones() > 1 ? "ZONE2" : "ZONE",
                            powerZone2, sourceZone2);
                } else if (showZone == 3) {
                    selectingRecord = false;
                    text = textLine2 = buildZonePowerResponse("ZONE3", powerZone3, sourceZone3);
                } else if (showZone == 4) {
                    selectingRecord = false;
                    text = textLine2 = buildZonePowerResponse("ZONE4", powerZone4, sourceZone4);
                }
                resetZone = false;
                break;
            case ZONE_SELECT:
                if (getModel().getNbAdditionalZones() == 0
                        || (getModel().getNbAdditionalZones() > 1 && getModel().getZoneSelectCmd() == cmd)
                        || (showZone == 1 && getModel().getZoneSelectCmd() != cmd)) {
                    accepted = false;
                } else {
                    if (getModel().getZoneSelectCmd() == cmd) {
                        if (!power && !powerZone2) {
                            showZone = 2;
                            powerZone2 = true;
                        } else if (showZone == 2) {
                            powerZone2 = !powerZone2;
                        } else {
                            showZone = 2;
                        }
                    } else {
                        if (showZone == 2) {
                            powerZone2 = !powerZone2;
                        } else if (showZone == 3) {
                            powerZone3 = !powerZone3;
                        } else if (showZone == 4) {
                            powerZone4 = !powerZone4;
                        }
                    }
                    if (showZone == 2) {
                        text = textLine2 = buildZonePowerResponse(
                                getModel().getNbAdditionalZones() > 1 ? "ZONE2" : "ZONE", powerZone2, sourceZone2);
                    } else if (showZone == 3) {
                        text = textLine2 = buildZonePowerResponse("ZONE3", powerZone3, sourceZone3);
                    } else if (showZone == 4) {
                        text = textLine2 = buildZonePowerResponse("ZONE4", powerZone4, sourceZone4);
                    }
                    resetZone = false;
                }
                break;
            default:
                accepted = false;
                break;
        }
        if (!accepted && powerZone2) {
            accepted = true;
            switch (cmd) {
                case ZONE2_VOLUME_UP:
                    if (volumeZone2 < maxVolume) {
                        volumeZone2++;
                    }
                    text = textLine2 = buildZoneVolumeResponse(getModel().getNbAdditionalZones() > 1 ? "ZONE2" : "ZONE",
                            muteZone2, volumeZone2);
                    break;
                case ZONE2_VOLUME_DOWN:
                    if (volumeZone2 > minVolume) {
                        volumeZone2--;
                    }
                    text = textLine2 = buildZoneVolumeResponse(getModel().getNbAdditionalZones() > 1 ? "ZONE2" : "ZONE",
                            muteZone2, volumeZone2);
                    break;
                case ZONE2_VOLUME_SET:
                    if (value != null) {
                        volumeZone2 = value;
                    }
                    text = textLine2 = buildZoneVolumeResponse(getModel().getNbAdditionalZones() > 1 ? "ZONE2" : "ZONE",
                            muteZone2, volumeZone2);
                    break;
                case VOLUME_UP:
                    if (!getModel().hasZone2Commands() && getModel().getNbAdditionalZones() >= 1 && showZone == 2) {
                        if (volumeZone2 < maxVolume) {
                            volumeZone2++;
                        }
                        text = textLine2 = buildZoneVolumeResponse(
                                getModel().getNbAdditionalZones() > 1 ? "ZONE2" : "ZONE", muteZone2, volumeZone2);
                        resetZone = false;
                    } else {
                        accepted = false;
                    }
                    break;
                case VOLUME_DOWN:
                    if (!getModel().hasZone2Commands() && getModel().getNbAdditionalZones() >= 1 && showZone == 2) {
                        if (volumeZone2 > minVolume) {
                            volumeZone2--;
                        }
                        text = textLine2 = buildZoneVolumeResponse(
                                getModel().getNbAdditionalZones() > 1 ? "ZONE2" : "ZONE", muteZone2, volumeZone2);
                        resetZone = false;
                    } else {
                        accepted = false;
                    }
                    break;
                case VOLUME_SET:
                    if (!getModel().hasZone2Commands() && getModel().getNbAdditionalZones() >= 1 && showZone == 2) {
                        if (value != null) {
                            volumeZone2 = value;
                        }
                        text = textLine2 = buildZoneVolumeResponse(
                                getModel().getNbAdditionalZones() > 1 ? "ZONE2" : "ZONE", muteZone2, volumeZone2);
                        resetZone = false;
                    } else {
                        accepted = false;
                    }
                    break;
                case ZONE2_MUTE_TOGGLE:
                    muteZone2 = !muteZone2;
                    text = textLine2 = buildZoneVolumeResponse(getModel().getNbAdditionalZones() > 1 ? "ZONE2" : "ZONE",
                            muteZone2, volumeZone2);
                    break;
                case ZONE2_MUTE_ON:
                    muteZone2 = true;
                    text = textLine2 = buildZoneVolumeResponse(getModel().getNbAdditionalZones() > 1 ? "ZONE2" : "ZONE",
                            muteZone2, volumeZone2);
                    break;
                case ZONE2_MUTE_OFF:
                    muteZone2 = false;
                    text = textLine2 = buildZoneVolumeResponse(getModel().getNbAdditionalZones() > 1 ? "ZONE2" : "ZONE",
                            muteZone2, volumeZone2);
                    break;
                default:
                    accepted = false;
                    break;
            }
            if (!accepted) {
                try {
                    sourceZone2 = getModel().getZone2SourceFromCommand(cmd);
                    powerZone2 = true;
                    text = textLine2 = buildZonePowerResponse(getModel().getNbAdditionalZones() > 1 ? "ZONE2" : "ZONE",
                            powerZone2, sourceZone2);
                    muteZone2 = false;
                    accepted = true;
                    showZone = 2;
                    resetZone = false;
                } catch (RotelException e) {
                }
            }
            if (!accepted && !getModel().hasZone2Commands() && getModel().getNbAdditionalZones() >= 1
                    && showZone == 2) {
                try {
                    sourceZone2 = getModel().getSourceFromCommand(cmd);
                    powerZone2 = true;
                    text = textLine2 = buildZonePowerResponse(getModel().getNbAdditionalZones() > 1 ? "ZONE2" : "ZONE",
                            powerZone2, sourceZone2);
                    muteZone2 = false;
                    accepted = true;
                    resetZone = false;
                } catch (RotelException e) {
                }
            }
        }
        if (!accepted && powerZone3) {
            accepted = true;
            switch (cmd) {
                case ZONE3_VOLUME_UP:
                    if (volumeZone3 < maxVolume) {
                        volumeZone3++;
                    }
                    text = textLine2 = buildZoneVolumeResponse("ZONE3", muteZone3, volumeZone3);
                    break;
                case ZONE3_VOLUME_DOWN:
                    if (volumeZone3 > minVolume) {
                        volumeZone3--;
                    }
                    text = textLine2 = buildZoneVolumeResponse("ZONE3", muteZone3, volumeZone3);
                    break;
                case ZONE3_VOLUME_SET:
                    if (value != null) {
                        volumeZone3 = value;
                    }
                    text = textLine2 = buildZoneVolumeResponse("ZONE3", muteZone3, volumeZone3);
                    break;
                case ZONE3_MUTE_TOGGLE:
                    muteZone3 = !muteZone3;
                    text = textLine2 = buildZoneVolumeResponse("ZONE3", muteZone3, volumeZone3);
                    break;
                case ZONE3_MUTE_ON:
                    muteZone3 = true;
                    text = textLine2 = buildZoneVolumeResponse("ZONE3", muteZone3, volumeZone3);
                    break;
                case ZONE3_MUTE_OFF:
                    muteZone3 = false;
                    text = textLine2 = buildZoneVolumeResponse("ZONE3", muteZone3, volumeZone3);
                    break;
                default:
                    accepted = false;
                    break;
            }
            if (!accepted) {
                try {
                    sourceZone3 = getModel().getZone3SourceFromCommand(cmd);
                    powerZone3 = true;
                    text = textLine2 = buildZonePowerResponse("ZONE3", powerZone3, sourceZone3);
                    muteZone3 = false;
                    accepted = true;
                    showZone = 3;
                    resetZone = false;
                } catch (RotelException e) {
                }
            }
        }
        if (!accepted && powerZone4) {
            accepted = true;
            switch (cmd) {
                case ZONE4_VOLUME_UP:
                    if (volumeZone4 < maxVolume) {
                        volumeZone4++;
                    }
                    text = textLine2 = buildZoneVolumeResponse("ZONE4", muteZone4, volumeZone4);
                    break;
                case ZONE4_VOLUME_DOWN:
                    if (volumeZone4 > minVolume) {
                        volumeZone4--;
                    }
                    text = textLine2 = buildZoneVolumeResponse("ZONE4", muteZone4, volumeZone4);
                    break;
                case ZONE4_VOLUME_SET:
                    if (value != null) {
                        volumeZone4 = value;
                    }
                    text = textLine2 = buildZoneVolumeResponse("ZONE4", muteZone4, volumeZone4);
                    break;
                case ZONE4_MUTE_TOGGLE:
                    muteZone4 = !muteZone4;
                    text = textLine2 = buildZoneVolumeResponse("ZONE4", muteZone4, volumeZone4);
                    break;
                case ZONE4_MUTE_ON:
                    muteZone4 = true;
                    text = textLine2 = buildZoneVolumeResponse("ZONE4", muteZone4, volumeZone4);
                    break;
                case ZONE4_MUTE_OFF:
                    muteZone4 = false;
                    text = textLine2 = buildZoneVolumeResponse("ZONE4", muteZone4, volumeZone4);
                    break;
                default:
                    accepted = false;
                    break;
            }
            if (!accepted) {
                try {
                    sourceZone4 = getModel().getZone4SourceFromCommand(cmd);
                    powerZone4 = true;
                    text = textLine2 = buildZonePowerResponse("ZONE4", powerZone4, sourceZone4);
                    muteZone4 = false;
                    accepted = true;
                    showZone = 4;
                    resetZone = false;
                } catch (RotelException e) {
                }
            }
        }
        if (!accepted && power) {
            accepted = true;
            switch (cmd) {
                case UPDATE_AUTO:
                    textAscii = buildAsciiResponse(
                            getProtocol() == RotelProtocol.ASCII_V1 ? KEY_DISPLAY_UPDATE : KEY_UPDATE_MODE, "AUTO");
                    break;
                case UPDATE_MANUAL:
                    textAscii = buildAsciiResponse(
                            getProtocol() == RotelProtocol.ASCII_V1 ? KEY_DISPLAY_UPDATE : KEY_UPDATE_MODE, "MANUAL");
                    break;
                case VOLUME_GET_MIN:
                    textAscii = buildAsciiResponse(KEY_VOLUME_MIN, minVolume);
                    break;
                case VOLUME_GET_MAX:
                    textAscii = buildAsciiResponse(KEY_VOLUME_MAX, maxVolume);
                    break;
                case VOLUME_UP:
                case MAIN_ZONE_VOLUME_UP:
                    if (volume < maxVolume) {
                        volume++;
                    }
                    text = buildVolumeLine1Response();
                    textLine1Right = buildVolumeLine1RightResponse();
                    textAscii = buildVolumeAsciiResponse();
                    break;
                case VOLUME_DOWN:
                case MAIN_ZONE_VOLUME_DOWN:
                    if (volume > minVolume) {
                        volume--;
                    }
                    text = buildVolumeLine1Response();
                    textLine1Right = buildVolumeLine1RightResponse();
                    textAscii = buildVolumeAsciiResponse();
                    break;
                case VOLUME_SET:
                    if (value != null) {
                        volume = value;
                    }
                    text = buildVolumeLine1Response();
                    textLine1Right = buildVolumeLine1RightResponse();
                    textAscii = buildVolumeAsciiResponse();
                    break;
                case VOLUME_GET:
                    textAscii = buildVolumeAsciiResponse();
                    break;
                case MUTE_TOGGLE:
                case MAIN_ZONE_MUTE_TOGGLE:
                    mute = !mute;
                    text = buildSourceLine1Response();
                    textLine1Right = buildVolumeLine1RightResponse();
                    textAscii = buildMuteAsciiResponse();
                    break;
                case MUTE_ON:
                case MAIN_ZONE_MUTE_ON:
                    mute = true;
                    text = buildSourceLine1Response();
                    textLine1Right = buildVolumeLine1RightResponse();
                    textAscii = buildMuteAsciiResponse();
                    break;
                case MUTE_OFF:
                case MAIN_ZONE_MUTE_OFF:
                    mute = false;
                    text = buildSourceLine1Response();
                    textLine1Right = buildVolumeLine1RightResponse();
                    textAscii = buildMuteAsciiResponse();
                    break;
                case MUTE:
                    textAscii = buildMuteAsciiResponse();
                    break;
                case TONE_MAX:
                    textAscii = buildAsciiResponse(KEY_TONE_MAX, "%02d", maxToneLevel);
                    break;
                case BASS_UP:
                    if (bass < maxToneLevel) {
                        bass += STEP_TONE_LEVEL;
                    }
                    text = buildBassLine1Response();
                    textLine1Right = buildBassLine1RightResponse();
                    textAscii = buildBassAsciiResponse();
                    break;
                case BASS_DOWN:
                    if (bass > minToneLevel) {
                        bass -= STEP_TONE_LEVEL;
                    }
                    text = buildBassLine1Response();
                    textLine1Right = buildBassLine1RightResponse();
                    textAscii = buildBassAsciiResponse();
                    break;
                case BASS_SET:
                    if (value != null) {
                        bass = value;
                    }
                    text = buildBassLine1Response();
                    textLine1Right = buildBassLine1RightResponse();
                    textAscii = buildBassAsciiResponse();
                    break;
                case BASS:
                    textAscii = buildBassAsciiResponse();
                    break;
                case TREBLE_UP:
                    if (treble < maxToneLevel) {
                        treble += STEP_TONE_LEVEL;
                    }
                    text = buildTrebleLine1Response();
                    textLine1Right = buildTrebleLine1RightResponse();
                    textAscii = buildTrebleAsciiResponse();
                    break;
                case TREBLE_DOWN:
                    if (treble > minToneLevel) {
                        treble -= STEP_TONE_LEVEL;
                    }
                    text = buildTrebleLine1Response();
                    textLine1Right = buildTrebleLine1RightResponse();
                    textAscii = buildTrebleAsciiResponse();
                    break;
                case TREBLE_SET:
                    if (value != null) {
                        treble = value;
                    }
                    text = buildTrebleLine1Response();
                    textLine1Right = buildTrebleLine1RightResponse();
                    textAscii = buildTrebleAsciiResponse();
                    break;
                case TREBLE:
                    textAscii = buildTrebleAsciiResponse();
                    break;
                case TONE_CONTROL_SELECT:
                    showTreble = !showTreble;
                    if (showTreble) {
                        text = buildTrebleLine1Response();
                        textLine1Right = buildTrebleLine1RightResponse();
                    } else {
                        text = buildBassLine1Response();
                        textLine1Right = buildBassLine1RightResponse();
                    }
                    break;
                case PLAY:
                    playStatus = RotelPlayStatus.PLAYING;
                    textAscii = buildPlayStatusAsciiResponse();
                    break;
                case STOP:
                    playStatus = RotelPlayStatus.STOPPED;
                    textAscii = buildPlayStatusAsciiResponse();
                    break;
                case PAUSE:
                    switch (playStatus) {
                        case PLAYING:
                            playStatus = RotelPlayStatus.PAUSED;
                            break;
                        case PAUSED:
                        case STOPPED:
                            playStatus = RotelPlayStatus.PLAYING;
                            break;
                    }
                    textAscii = buildPlayStatusAsciiResponse();
                    break;
                case CD_PLAY_STATUS:
                case PLAY_STATUS:
                    textAscii = buildPlayStatusAsciiResponse();
                    break;
                case TRACK_FORWARD:
                    track++;
                    textAscii = buildTrackAsciiResponse();
                    break;
                case TRACK_BACKWORD:
                    if (track > 1) {
                        track--;
                    }
                    textAscii = buildTrackAsciiResponse();
                    break;
                case TRACK:
                    textAscii = buildTrackAsciiResponse();
                    break;
                case SOURCE_MULTI_INPUT:
                    multiinput = !multiinput;
                    text = "MULTI IN " + (multiinput ? "ON" : "OFF");
                    try {
                        source = getModel().getSourceFromCommand(cmd);
                        textLine1Left = buildSourceLine1LeftResponse();
                        textAscii = buildSourceAsciiResponse();
                        mute = false;
                    } catch (RotelException e) {
                    }
                    break;
                case SOURCE:
                    textAscii = buildSourceAsciiResponse();
                    break;
                case STEREO:
                    dsp = RotelDsp.CAT4_NONE;
                    textLine2 = "STEREO";
                    textAscii = buildDspAsciiResponse();
                    break;
                case STEREO3:
                    dsp = RotelDsp.CAT4_STEREO3;
                    textLine2 = "DOLBY 3 STEREO";
                    textAscii = buildDspAsciiResponse();
                    break;
                case STEREO5:
                    dsp = RotelDsp.CAT4_STEREO5;
                    textLine2 = "5CH STEREO";
                    textAscii = buildDspAsciiResponse();
                    break;
                case STEREO7:
                    dsp = RotelDsp.CAT4_STEREO7;
                    textLine2 = "7CH STEREO";
                    textAscii = buildDspAsciiResponse();
                    break;
                case STEREO9:
                    dsp = RotelDsp.CAT5_STEREO9;
                    textAscii = buildDspAsciiResponse();
                    break;
                case STEREO11:
                    dsp = RotelDsp.CAT5_STEREO11;
                    textAscii = buildDspAsciiResponse();
                    break;
                case DSP1:
                    dsp = RotelDsp.CAT4_DSP1;
                    textLine2 = "DSP 1";
                    textAscii = buildDspAsciiResponse();
                    break;
                case DSP2:
                    dsp = RotelDsp.CAT4_DSP2;
                    textLine2 = "DSP 2";
                    textAscii = buildDspAsciiResponse();
                    break;
                case DSP3:
                    dsp = RotelDsp.CAT4_DSP3;
                    textLine2 = "DSP 3";
                    textAscii = buildDspAsciiResponse();
                    break;
                case DSP4:
                    dsp = RotelDsp.CAT4_DSP4;
                    textLine2 = "DSP 4";
                    textAscii = buildDspAsciiResponse();
                    break;
                case PROLOGIC:
                    dsp = RotelDsp.CAT4_PROLOGIC;
                    textLine2 = "DOLBY PRO LOGIC";
                    textAscii = buildDspAsciiResponse();
                    break;
                case PLII_CINEMA:
                    dsp = RotelDsp.CAT4_PLII_CINEMA;
                    textLine2 = "DOLBY PL  C";
                    textAscii = buildDspAsciiResponse();
                    break;
                case PLII_MUSIC:
                    dsp = RotelDsp.CAT4_PLII_MUSIC;
                    textLine2 = "DOLBY PL  M";
                    textAscii = buildDspAsciiResponse();
                    break;
                case PLII_GAME:
                    dsp = RotelDsp.CAT4_PLII_GAME;
                    textLine2 = "DOLBY PL  G";
                    textAscii = buildDspAsciiResponse();
                    break;
                case PLIIZ:
                    dsp = RotelDsp.CAT4_PLIIZ;
                    textLine2 = "DOLBY PL z";
                    textAscii = buildDspAsciiResponse();
                    break;
                case NEO6_MUSIC:
                    dsp = RotelDsp.CAT4_NEO6_MUSIC;
                    textLine2 = "DTS Neo:6 M";
                    textAscii = buildDspAsciiResponse();
                    break;
                case NEO6_CINEMA:
                    dsp = RotelDsp.CAT4_NEO6_CINEMA;
                    textLine2 = "DTS Neo:6 C";
                    textAscii = buildDspAsciiResponse();
                    break;
                case ATMOS:
                    dsp = RotelDsp.CAT5_ATMOS;
                    textAscii = buildDspAsciiResponse();
                    break;
                case NEURAL_X:
                    dsp = RotelDsp.CAT5_NEURAL_X;
                    textAscii = buildDspAsciiResponse();
                    break;
                case BYPASS:
                    dsp = RotelDsp.CAT4_BYPASS;
                    textLine2 = "BYPASS";
                    textAscii = buildDspAsciiResponse();
                    break;
                case DSP_MODE:
                    textAscii = buildDspAsciiResponse();
                    break;
                case FREQUENCY:
                    textAscii = buildAsciiResponse(KEY_FREQ, "44.1");
                    break;
                case DIMMER_LEVEL_SET:
                    if (value != null) {
                        dimmer = value;
                    }
                    textAscii = buildAsciiResponse(KEY_DIMMER, dimmer);
                    break;
                case DIMMER_LEVEL_GET:
                    textAscii = buildAsciiResponse(KEY_DIMMER, dimmer);
                    break;
                default:
                    accepted = false;
                    break;
            }
            if (!accepted) {
                try {
                    source = getModel().getMainZoneSourceFromCommand(cmd);
                    text = buildSourceLine1Response();
                    textLine1Left = buildSourceLine1LeftResponse();
                    textAscii = buildSourceAsciiResponse();
                    accepted = true;
                } catch (RotelException e) {
                }
            }
            if (!accepted) {
                try {
                    if (selectingRecord && !getModel().hasOtherThanPrimaryCommands()) {
                        recordSource = getModel().getSourceFromCommand(cmd);
                    } else {
                        source = getModel().getSourceFromCommand(cmd);
                    }
                    text = buildSourceLine1Response();
                    textLine1Left = buildSourceLine1LeftResponse();
                    textAscii = buildSourceAsciiResponse();
                    mute = false;
                    accepted = true;
                } catch (RotelException e) {
                }
            }
            if (!accepted) {
                try {
                    recordSource = getModel().getRecordSourceFromCommand(cmd);
                    text = buildSourceLine1Response();
                    textLine2 = buildRecordResponse();
                    accepted = true;
                } catch (RotelException e) {
                }
            }
        }

        if (!accepted) {
            return;
        }

        if (cmd != RotelCommand.RECORD_FONCTION_SELECT) {
            selectingRecord = false;
        }
        if (resetZone) {
            showZone = 0;
        }

        if (getModel().getRespNbChars() == 42) {
            while (textLine1Left.length() < 14) {
                textLine1Left += " ";
            }
            while (textLine1Right.length() < 7) {
                textLine1Right += " ";
            }
            while (textLine2.length() < 21) {
                textLine2 += " ";
            }
            text = textLine1Left + textLine1Right + textLine2;
        }

        if (getProtocol() == RotelProtocol.HEX) {
            byte[] chars = Arrays.copyOf(text.getBytes(StandardCharsets.US_ASCII), getModel().getRespNbChars());
            byte[] flags = new byte[getModel().getRespNbFlags()];
            try {
                getModel().setMultiInput(flags, multiinput);
            } catch (RotelException e) {
            }
            try {
                getModel().setZone2(flags, powerZone2);
            } catch (RotelException e) {
            }
            try {
                getModel().setZone3(flags, powerZone3);
            } catch (RotelException e) {
            }
            try {
                getModel().setZone4(flags, powerZone4);
            } catch (RotelException e) {
            }
            int size = 6 + getModel().getRespNbChars() + getModel().getRespNbFlags();
            byte[] dataBuffer = new byte[size];
            int idx = 0;
            dataBuffer[idx++] = START;
            dataBuffer[idx++] = (byte) (size - 4);
            dataBuffer[idx++] = getModel().getDeviceId();
            dataBuffer[idx++] = STANDARD_RESPONSE;
            if (getModel().isCharsBeforeFlags()) {
                System.arraycopy(chars, 0, dataBuffer, idx, getModel().getRespNbChars());
                idx += getModel().getRespNbChars();
                System.arraycopy(flags, 0, dataBuffer, idx, getModel().getRespNbFlags());
                idx += getModel().getRespNbFlags();
            } else {
                System.arraycopy(flags, 0, dataBuffer, idx, getModel().getRespNbFlags());
                idx += getModel().getRespNbFlags();
                System.arraycopy(chars, 0, dataBuffer, idx, getModel().getRespNbChars());
                idx += getModel().getRespNbChars();
            }
            byte checksum = computeCheckSum(dataBuffer, idx - 1);
            if ((checksum & 0x000000FF) == 0x000000FD) {
                dataBuffer[idx++] = (byte) 0xFD;
                dataBuffer[idx++] = 0;
            } else if ((checksum & 0x000000FF) == 0x000000FE) {
                dataBuffer[idx++] = (byte) 0xFD;
                dataBuffer[idx++] = 1;
            } else {
                dataBuffer[idx++] = checksum;
            }
            synchronized (lock) {
                feedbackMsg = Arrays.copyOf(dataBuffer, idx);
                idxInFeedbackMsg = 0;
            }
        } else {
            String command = textAscii + (getProtocol() == RotelProtocol.ASCII_V1 ? "!" : "$");
            synchronized (lock) {
                feedbackMsg = command.getBytes(StandardCharsets.US_ASCII);
                idxInFeedbackMsg = 0;
            }
        }
    }

    private String buildAsciiResponse(String key, String value) {
        return String.format("%s=%s", key, value);
    }

    private String buildAsciiResponse(String key, int value) {
        return buildAsciiResponse(key, "%d", value);
    }

    private String buildAsciiResponse(String key, String format, int value) {
        return String.format("%s=" + format, key, value);
    }

    private String buildAsciiResponse(String key, boolean value) {
        return buildAsciiResponse(key, value ? MSG_VALUE_ON : MSG_VALUE_OFF);
    }

    private String buildPowerAsciiResponse() {
        return buildAsciiResponse(KEY_POWER, power ? POWER_ON : STANDBY);
    }

    private String buildVolumeAsciiResponse() {
        return buildAsciiResponse(KEY_VOLUME, "%02d", volume);
    }

    private String buildMuteAsciiResponse() {
        return buildAsciiResponse(KEY_MUTE, mute);
    }

    private String buildBassAsciiResponse() {
        String result;
        if (bass == 0) {
            result = buildAsciiResponse(KEY_BASS, "000");
        } else if (bass > 0) {
            result = buildAsciiResponse(KEY_BASS, "+%02d", bass);
        } else {
            result = buildAsciiResponse(KEY_BASS, "-%02d", -bass);
        }
        return result;
    }

    private String buildTrebleAsciiResponse() {
        String result;
        if (treble == 0) {
            result = buildAsciiResponse(KEY_TREBLE, "000");
        } else if (treble > 0) {
            result = buildAsciiResponse(KEY_TREBLE, "+%02d", treble);
        } else {
            result = buildAsciiResponse(KEY_TREBLE, "-%02d", -treble);
        }
        return result;
    }

    private String buildPlayStatusAsciiResponse() {
        String status = "";
        switch (playStatus) {
            case PLAYING:
                status = PLAY;
                break;
            case PAUSED:
                status = PAUSE;
                break;
            case STOPPED:
                status = STOP;
                break;
        }
        return buildAsciiResponse(getProtocol() == RotelProtocol.ASCII_V1 ? KEY1_PLAY_STATUS : KEY2_PLAY_STATUS,
                status);
    }

    private String buildTrackAsciiResponse() {
        return buildAsciiResponse(KEY_TRACK, "%03d", track);
    }

    private String buildSourceAsciiResponse() {
        String str = null;
        RotelCommand command = source.getCommand();
        if (command != null) {
            str = command.getAsciiCommandV2();
        }
        return buildAsciiResponse(KEY_SOURCE, (str == null) ? "" : str);
    }

    private String buildDspAsciiResponse() {
        return buildAsciiResponse(KEY_DSP_MODE, dsp.getFeedback());
    }

    private String buildSourceLine1Response() {
        String text;
        if (!power) {
            text = "";
        } else if (mute) {
            text = "MUTE ON";
        } else {
            text = getSourceLabel(source, false) + " " + getSourceLabel(recordSource, true);
        }
        return text;
    }

    private String buildSourceLine1LeftResponse() {
        String text;
        if (!power) {
            text = "";
        } else {
            text = getSourceLabel(source, false);
        }
        return text;
    }

    private String buildRecordResponse() {
        String text;
        if (!power) {
            text = "";
        } else {
            text = "REC " + getSourceLabel(recordSource, true);
        }
        return text;
    }

    private String buildZonePowerResponse(String zone, boolean powerZone, RotelSource sourceZone) {
        String state = powerZone ? getSourceLabel(sourceZone, true) : "OFF";
        return zone + " " + state;
    }

    private String buildVolumeLine1Response() {
        String text;
        if (volume == minVolume) {
            text = " VOLUME  MIN ";
        } else if (volume == maxVolume) {
            text = " VOLUME  MAX ";
        } else {
            text = String.format(" VOLUME   %02d ", volume);
        }
        return text;
    }

    private String buildVolumeLine1RightResponse() {
        String text;
        if (!power) {
            text = "";
        } else if (mute) {
            text = "MUTE ON";
        } else if (volume == minVolume) {
            text = "VOL MIN";
        } else if (volume == maxVolume) {
            text = "VOL MAX";
        } else {
            text = String.format("VOL  %02d", volume);
        }
        return text;
    }

    private String buildZoneVolumeResponse(String zone, boolean muted, int vol) {
        String text;
        if (muted) {
            text = zone + " MUTE ON";
        } else if (vol == minVolume) {
            text = zone + " VOL MIN";
        } else if (vol == maxVolume) {
            text = zone + " VOL MAX";
        } else {
            text = String.format("%s VOL %02d", zone, vol);
        }
        return text;
    }

    private String buildBassLine1Response() {
        String text;
        if (bass == minToneLevel) {
            text = "   BASS  MIN ";
        } else if (bass == maxToneLevel) {
            text = "   BASS  MAX ";
        } else if (bass == 0) {
            text = "   BASS    0 ";
        } else if (bass > 0) {
            text = String.format("   BASS  +%02d ", bass);
        } else {
            text = String.format("   BASS  -%02d ", -bass);
        }
        return text;
    }

    private String buildBassLine1RightResponse() {
        String text;
        if (bass == minToneLevel) {
            text = "LF  MIN";
        } else if (bass == maxToneLevel) {
            text = "LF  MAX";
        } else if (bass == 0) {
            text = "LF    0";
        } else if (bass > 0) {
            text = String.format("LF + %02d", bass);
        } else {
            text = String.format("LF - %02d", -bass);
        }
        return text;
    }

    private String buildTrebleLine1Response() {
        String text;
        if (treble == minToneLevel) {
            text = " TREBLE  MIN ";
        } else if (treble == maxToneLevel) {
            text = " TREBLE  MAX ";
        } else if (treble == 0) {
            text = " TREBLE    0 ";
        } else if (treble > 0) {
            text = String.format(" TREBLE  +%02d ", treble);
        } else {
            text = String.format(" TREBLE  -%02d ", -treble);
        }
        return text;
    }

    private String buildTrebleLine1RightResponse() {
        String text;
        if (treble == minToneLevel) {
            text = "HF  MIN";
        } else if (treble == maxToneLevel) {
            text = "HF  MAX";
        } else if (treble == 0) {
            text = "HF    0";
        } else if (treble > 0) {
            text = String.format("HF + %02d", treble);
        } else {
            text = String.format("HF - %02d", -treble);
        }
        return text;
    }

    private String getSourceLabel(RotelSource source, boolean considerFollowMain) {
        String label;
        if (considerFollowMain && source.getName().equals(RotelSource.CAT1_FOLLOW_MAIN.getName())) {
            label = "SOURCE";
        } else {
            label = Objects.requireNonNullElse(sourcesLabels.get(source), source.getLabel());
        }

        return label;
    }
}
