/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import static org.openhab.binding.rotel.internal.RotelBindingConstants.*;
import static org.openhab.binding.rotel.internal.protocol.hex.RotelHexProtocolHandler.START;

import java.io.InterruptedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.rotel.internal.RotelException;
import org.openhab.binding.rotel.internal.RotelModel;
import org.openhab.binding.rotel.internal.RotelPlayStatus;
import org.openhab.binding.rotel.internal.RotelRepeatMode;
import org.openhab.binding.rotel.internal.protocol.RotelAbstractProtocolHandler;
import org.openhab.binding.rotel.internal.protocol.RotelProtocol;
import org.openhab.binding.rotel.internal.protocol.hex.RotelHexProtocolHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for simulating the communication with the Rotel device
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class RotelSimuConnector extends RotelConnector {

    private static final int STEP_TONE_LEVEL = 1;
    private static final double STEP_DECIBEL = 0.5;
    private static final String FIRMWARE = "V1.1.8";

    private final Logger logger = LoggerFactory.getLogger(RotelSimuConnector.class);

    private final RotelModel model;
    private final RotelProtocol protocol;
    private final Map<RotelSource, String> sourcesLabels;

    private Object lock = new Object();

    private byte[] feedbackMsg = new byte[1];
    private int idxInFeedbackMsg = feedbackMsg.length;

    private boolean[] powers = { false, false, false, false, false };
    private String powerMode = POWER_NORMAL;
    private RotelSource[] sources;
    private RotelSource recordSource;
    private boolean multiinput;
    private RotelDsp dsp = RotelDsp.CAT4_NONE;
    private boolean bypass = false;
    private int[] volumes = { 50, 10, 20, 30, 40 };
    private boolean[] mutes = { false, false, false, false, false };
    private boolean tcbypass;
    private int[] basses = { 0, 0, 0, 0, 0 };
    private int[] trebles = { 0, 0, 0, 0, 0 };
    private int[] balances = { 0, 0, 0, 0, 0 };
    private boolean showTreble;
    private boolean speakerA = true;
    private boolean speakerB = false;
    private RotelPlayStatus playStatus = RotelPlayStatus.STOPPED;
    private int track = 1;
    private boolean randomMode;
    private RotelRepeatMode repeatMode = RotelRepeatMode.OFF;
    private int fmPreset = 5;
    private int dabPreset = 15;
    private int iradioPreset = 25;
    private boolean selectingRecord;
    private int showZone;
    private int dimmer;
    private int pcUsbClass = 1;
    private double subLevel;
    private double centerLevel;
    private double surroundRightLevel;
    private double surroundLefLevel;
    private double centerBackRightLevel;
    private double centerBackLefLevel;
    private double ceilingFrontRightLevel;
    private double ceilingFrontLefLevel;
    private double ceilingRearRightLevel;
    private double ceilingRearLefLevel;

    private int minVolume;
    private int maxVolume;
    private int minToneLevel;
    private int maxToneLevel;
    private int minBalance;
    private int maxBalance;

    /**
     * Constructor
     *
     * @param model the projector model in use
     * @param protocolHandler the protocol handler
     * @param sourcesLabels the custom labels for sources
     * @param readerThreadName the name of thread to be created
     */
    public RotelSimuConnector(RotelModel model, RotelAbstractProtocolHandler protocolHandler,
            Map<RotelSource, String> sourcesLabels, String readerThreadName) {
        super(protocolHandler, true, readerThreadName);
        this.model = model;
        this.protocol = protocolHandler.getProtocol();
        this.sourcesLabels = sourcesLabels;
        this.minVolume = 0;
        this.maxVolume = model.hasVolumeControl() ? model.getVolumeMax() : 0;
        this.maxToneLevel = model.hasToneControl() ? model.getToneLevelMax() : 0;
        this.minToneLevel = -this.maxToneLevel;
        this.maxBalance = model.hasBalanceControl() ? model.getBalanceLevelMax() : 0;
        this.minBalance = -this.maxBalance;
        List<RotelSource> modelSources = model.getSources();
        RotelSource source = modelSources.isEmpty() ? RotelSource.CAT0_CD : modelSources.get(0);
        sources = new RotelSource[] { source, source, source, source, source };
        recordSource = source;
    }

    @Override
    public synchronized void open() throws RotelException {
        logger.debug("Opening simulated connection");
        startReaderThread();
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

    /**
     * Built the simulated feedback message for a sent command
     *
     * @param cmd the sent command
     * @param value the integer value considered in the sent command for volume, bass or treble adjustment
     */
    public void buildFeedbackMessage(RotelCommand cmd, @Nullable Integer value) {
        String text = buildSourceLine1Response();
        String textLine1Left = buildSourceLine1LeftResponse();
        String textLine1Right = buildVolumeLine1RightResponse();
        String textLine2 = "";
        String textAscii = "";
        boolean variableLength = false;
        boolean accepted = true;
        boolean resetZone = true;
        int numZone = 0;
        switch (cmd) {
            case ZONE1_VOLUME_UP:
            case ZONE1_VOLUME_DOWN:
            case ZONE1_VOLUME_SET:
            case ZONE1_MUTE_TOGGLE:
            case ZONE1_MUTE_ON:
            case ZONE1_MUTE_OFF:
            case ZONE1_BASS_UP:
            case ZONE1_BASS_DOWN:
            case ZONE1_BASS_SET:
            case ZONE1_TREBLE_UP:
            case ZONE1_TREBLE_DOWN:
            case ZONE1_TREBLE_SET:
            case ZONE1_BALANCE_LEFT:
            case ZONE1_BALANCE_RIGHT:
            case ZONE1_BALANCE_SET:
                numZone = 1;
                break;
            case ZONE2_POWER_OFF:
            case ZONE2_POWER_ON:
            case ZONE2_VOLUME_UP:
            case ZONE2_VOLUME_DOWN:
            case ZONE2_VOLUME_SET:
            case ZONE2_MUTE_TOGGLE:
            case ZONE2_MUTE_ON:
            case ZONE2_MUTE_OFF:
            case ZONE2_BASS_UP:
            case ZONE2_BASS_DOWN:
            case ZONE2_BASS_SET:
            case ZONE2_TREBLE_UP:
            case ZONE2_TREBLE_DOWN:
            case ZONE2_TREBLE_SET:
            case ZONE2_BALANCE_LEFT:
            case ZONE2_BALANCE_RIGHT:
            case ZONE2_BALANCE_SET:
                numZone = 2;
                break;
            case ZONE3_POWER_OFF:
            case ZONE3_POWER_ON:
            case ZONE3_VOLUME_UP:
            case ZONE3_VOLUME_DOWN:
            case ZONE3_VOLUME_SET:
            case ZONE3_MUTE_TOGGLE:
            case ZONE3_MUTE_ON:
            case ZONE3_MUTE_OFF:
            case ZONE3_BASS_UP:
            case ZONE3_BASS_DOWN:
            case ZONE3_BASS_SET:
            case ZONE3_TREBLE_UP:
            case ZONE3_TREBLE_DOWN:
            case ZONE3_TREBLE_SET:
            case ZONE3_BALANCE_LEFT:
            case ZONE3_BALANCE_RIGHT:
            case ZONE3_BALANCE_SET:
                numZone = 3;
                break;
            case ZONE4_POWER_OFF:
            case ZONE4_POWER_ON:
            case ZONE4_VOLUME_UP:
            case ZONE4_VOLUME_DOWN:
            case ZONE4_VOLUME_SET:
            case ZONE4_MUTE_TOGGLE:
            case ZONE4_MUTE_ON:
            case ZONE4_MUTE_OFF:
            case ZONE4_BASS_UP:
            case ZONE4_BASS_DOWN:
            case ZONE4_BASS_SET:
            case ZONE4_TREBLE_UP:
            case ZONE4_TREBLE_DOWN:
            case ZONE4_TREBLE_SET:
            case ZONE4_BALANCE_LEFT:
            case ZONE4_BALANCE_RIGHT:
            case ZONE4_BALANCE_SET:
                numZone = 4;
                break;
            default:
                break;
        }
        switch (cmd) {
            case DISPLAY_REFRESH:
                break;
            case POWER_OFF:
            case MAIN_ZONE_POWER_OFF:
                powers[0] = false;
                if (model.getNumberOfZones() > 1 && !model.hasPowerControlPerZone()) {
                    for (int zone = 1; zone <= model.getNumberOfZones(); zone++) {
                        powers[zone] = false;
                    }
                }
                text = buildSourceLine1Response();
                textLine1Left = buildSourceLine1LeftResponse();
                textLine1Right = buildVolumeLine1RightResponse();
                textAscii = buildPowerAsciiResponse();
                break;
            case POWER_ON:
            case MAIN_ZONE_POWER_ON:
                powers[0] = true;
                if (model.getNumberOfZones() > 1 && !model.hasPowerControlPerZone()) {
                    for (int zone = 1; zone <= model.getNumberOfZones(); zone++) {
                        powers[zone] = true;
                    }
                }
                text = buildSourceLine1Response();
                textLine1Left = buildSourceLine1LeftResponse();
                textLine1Right = buildVolumeLine1RightResponse();
                textAscii = buildPowerAsciiResponse();
                break;
            case POWER:
                textAscii = buildPowerAsciiResponse();
                break;
            case ZONE2_POWER_OFF:
            case ZONE3_POWER_OFF:
            case ZONE4_POWER_OFF:
                powers[numZone] = false;
                text = textLine2 = buildZonePowerResponse(numZone);
                showZone = numZone;
                resetZone = false;
                break;
            case ZONE2_POWER_ON:
            case ZONE3_POWER_ON:
            case ZONE4_POWER_ON:
                powers[numZone] = true;
                text = textLine2 = buildZonePowerResponse(numZone);
                showZone = numZone;
                resetZone = false;
                break;
            case RECORD_FONCTION_SELECT:
                if (model.getNumberOfZones() > 1 && model.getZoneSelectCmd() == cmd) {
                    showZone++;
                    if (showZone >= model.getNumberOfZones()) {
                        showZone = 1;
                        if (!powers[0]) {
                            showZone++;
                        }
                    }
                } else {
                    showZone = 1;
                }
                if (showZone == 1) {
                    selectingRecord = powers[0];
                    showTreble = false;
                    textLine2 = buildRecordResponse();
                } else if (showZone >= 2 && showZone <= 4) {
                    selectingRecord = false;
                    text = textLine2 = buildZonePowerResponse(showZone);
                }
                resetZone = false;
                break;
            case ZONE_SELECT:
                if (model.getNumberOfZones() == 1 || (model.getNumberOfZones() > 2 && model.getZoneSelectCmd() == cmd)
                        || (showZone == 1 && model.getZoneSelectCmd() != cmd)) {
                    accepted = false;
                } else {
                    if (model.getZoneSelectCmd() == cmd) {
                        if (!powers[0] && !powers[2]) {
                            showZone = 2;
                            powers[2] = true;
                        } else if (showZone == 2) {
                            powers[2] = !powers[2];
                        } else {
                            showZone = 2;
                        }
                    } else if (showZone >= 2 && showZone <= 4) {
                        powers[showZone] = !powers[showZone];
                    }
                    if (showZone >= 2 && showZone <= 4) {
                        text = textLine2 = buildZonePowerResponse(showZone);
                    }
                    resetZone = false;
                }
                break;
            default:
                accepted = false;
                break;
        }
        if (!accepted && numZone > 0 && powers[numZone]) {
            accepted = true;
            switch (cmd) {
                case ZONE1_VOLUME_UP:
                case ZONE2_VOLUME_UP:
                case ZONE3_VOLUME_UP:
                case ZONE4_VOLUME_UP:
                    if (volumes[numZone] < maxVolume) {
                        volumes[numZone]++;
                    }
                    text = textLine2 = buildZoneVolumeResponse(numZone);
                    textAscii = buildVolumeAsciiResponse();
                    break;
                case ZONE1_VOLUME_DOWN:
                case ZONE2_VOLUME_DOWN:
                case ZONE3_VOLUME_DOWN:
                case ZONE4_VOLUME_DOWN:
                    if (volumes[numZone] > minVolume) {
                        volumes[numZone]--;
                    }
                    text = textLine2 = buildZoneVolumeResponse(numZone);
                    textAscii = buildVolumeAsciiResponse();
                    break;
                case ZONE1_VOLUME_SET:
                case ZONE2_VOLUME_SET:
                case ZONE3_VOLUME_SET:
                case ZONE4_VOLUME_SET:
                    if (value != null) {
                        volumes[numZone] = value;
                    }
                    text = textLine2 = buildZoneVolumeResponse(numZone);
                    textAscii = buildVolumeAsciiResponse();
                    break;
                case ZONE1_MUTE_TOGGLE:
                case ZONE2_MUTE_TOGGLE:
                case ZONE3_MUTE_TOGGLE:
                case ZONE4_MUTE_TOGGLE:
                    mutes[numZone] = !mutes[numZone];
                    text = textLine2 = buildZoneVolumeResponse(numZone);
                    textAscii = buildMuteAsciiResponse();
                    break;
                case ZONE1_MUTE_ON:
                case ZONE2_MUTE_ON:
                case ZONE3_MUTE_ON:
                case ZONE4_MUTE_ON:
                    mutes[numZone] = true;
                    text = textLine2 = buildZoneVolumeResponse(numZone);
                    textAscii = buildMuteAsciiResponse();
                    break;
                case ZONE1_MUTE_OFF:
                case ZONE2_MUTE_OFF:
                case ZONE3_MUTE_OFF:
                case ZONE4_MUTE_OFF:
                    mutes[numZone] = false;
                    text = textLine2 = buildZoneVolumeResponse(numZone);
                    textAscii = buildMuteAsciiResponse();
                    break;
                case ZONE1_BASS_UP:
                case ZONE2_BASS_UP:
                case ZONE3_BASS_UP:
                case ZONE4_BASS_UP:
                    if (!tcbypass && basses[numZone] < maxToneLevel) {
                        basses[numZone] += STEP_TONE_LEVEL;
                    }
                    textAscii = buildBassAsciiResponse();
                    break;
                case ZONE1_BASS_DOWN:
                case ZONE2_BASS_DOWN:
                case ZONE3_BASS_DOWN:
                case ZONE4_BASS_DOWN:
                    if (!tcbypass && basses[numZone] > minToneLevel) {
                        basses[numZone] -= STEP_TONE_LEVEL;
                    }
                    textAscii = buildBassAsciiResponse();
                    break;
                case ZONE1_BASS_SET:
                case ZONE2_BASS_SET:
                case ZONE3_BASS_SET:
                case ZONE4_BASS_SET:
                    if (!tcbypass && value != null) {
                        basses[numZone] = value;
                    }
                    textAscii = buildBassAsciiResponse();
                    break;
                case ZONE1_TREBLE_UP:
                case ZONE2_TREBLE_UP:
                case ZONE3_TREBLE_UP:
                case ZONE4_TREBLE_UP:
                    if (!tcbypass && trebles[numZone] < maxToneLevel) {
                        trebles[numZone] += STEP_TONE_LEVEL;
                    }
                    textAscii = buildTrebleAsciiResponse();
                    break;
                case ZONE1_TREBLE_DOWN:
                case ZONE2_TREBLE_DOWN:
                case ZONE3_TREBLE_DOWN:
                case ZONE4_TREBLE_DOWN:
                    if (!tcbypass && trebles[numZone] > minToneLevel) {
                        trebles[numZone] -= STEP_TONE_LEVEL;
                    }
                    textAscii = buildTrebleAsciiResponse();
                    break;
                case ZONE1_TREBLE_SET:
                case ZONE2_TREBLE_SET:
                case ZONE3_TREBLE_SET:
                case ZONE4_TREBLE_SET:
                    if (!tcbypass && value != null) {
                        trebles[numZone] = value;
                    }
                    textAscii = buildTrebleAsciiResponse();
                    break;
                case ZONE1_BALANCE_LEFT:
                case ZONE2_BALANCE_LEFT:
                case ZONE3_BALANCE_LEFT:
                case ZONE4_BALANCE_LEFT:
                    if (balances[numZone] > minBalance) {
                        balances[numZone]--;
                    }
                    textAscii = buildBalanceAsciiResponse();
                    break;
                case ZONE1_BALANCE_RIGHT:
                case ZONE2_BALANCE_RIGHT:
                case ZONE3_BALANCE_RIGHT:
                case ZONE4_BALANCE_RIGHT:
                    if (balances[numZone] < maxBalance) {
                        balances[numZone]++;
                    }
                    textAscii = buildBalanceAsciiResponse();
                    break;
                case ZONE1_BALANCE_SET:
                case ZONE2_BALANCE_SET:
                case ZONE3_BALANCE_SET:
                case ZONE4_BALANCE_SET:
                    if (value != null) {
                        balances[numZone] = value;
                    }
                    textAscii = buildBalanceAsciiResponse();
                    break;
                default:
                    accepted = false;
                    break;
            }
        }
        if (!accepted) {
            // Check if command is a change of source input for a zone
            for (int zone = 1; zone <= model.getNumberOfZones(); zone++) {
                if (powers[zone]) {
                    try {
                        sources[zone] = model.getZoneSourceFromCommand(cmd, zone);
                        text = textLine2 = buildZonePowerResponse(zone);
                        textAscii = buildSourceAsciiResponse();
                        mutes[zone] = false;
                        accepted = true;
                        showZone = zone;
                        resetZone = false;
                        break;
                    } catch (RotelException e) {
                    }
                }
            }
        }
        if (!accepted && powers[2] && !model.hasZoneCommands(2) && model.getNumberOfZones() > 1 && showZone == 2) {
            accepted = true;
            switch (cmd) {
                case VOLUME_UP:
                    if (volumes[2] < maxVolume) {
                        volumes[2]++;
                    }
                    text = textLine2 = buildZoneVolumeResponse(2);
                    resetZone = false;
                    break;
                case VOLUME_DOWN:
                    if (volumes[2] > minVolume) {
                        volumes[2]--;
                    }
                    text = textLine2 = buildZoneVolumeResponse(2);
                    resetZone = false;
                    break;
                case VOLUME_SET:
                    if (value != null) {
                        volumes[2] = value;
                    }
                    text = textLine2 = buildZoneVolumeResponse(2);
                    resetZone = false;
                    break;
                default:
                    accepted = false;
                    break;
            }
            if (!accepted) {
                try {
                    sources[2] = model.getSourceFromCommand(cmd);
                    text = textLine2 = buildZonePowerResponse(2);
                    mutes[2] = false;
                    accepted = true;
                    resetZone = false;
                } catch (RotelException e) {
                }
            }
        }
        if (!accepted && powers[0]) {
            accepted = true;
            switch (cmd) {
                case UPDATE_AUTO:
                    textAscii = buildAsciiResponse(
                            protocol == RotelProtocol.ASCII_V1 ? KEY_DISPLAY_UPDATE : KEY_UPDATE_MODE, AUTO);
                    break;
                case UPDATE_MANUAL:
                    textAscii = buildAsciiResponse(
                            protocol == RotelProtocol.ASCII_V1 ? KEY_DISPLAY_UPDATE : KEY_UPDATE_MODE, MANUAL);
                    break;
                case POWER_MODE_QUICK:
                    powerMode = POWER_QUICK;
                    textAscii = buildAsciiResponse(KEY_POWER_MODE, powerMode);
                    break;
                case POWER_MODE_NORMAL:
                    powerMode = POWER_NORMAL;
                    textAscii = buildAsciiResponse(KEY_POWER_MODE, powerMode);
                    break;
                case POWER_MODE:
                    textAscii = buildAsciiResponse(KEY_POWER_MODE, powerMode);
                    break;
                case VOLUME_GET_MIN:
                    textAscii = buildAsciiResponse(KEY_VOLUME_MIN, minVolume);
                    break;
                case VOLUME_GET_MAX:
                    textAscii = buildAsciiResponse(KEY_VOLUME_MAX, maxVolume);
                    break;
                case VOLUME_UP:
                case MAIN_ZONE_VOLUME_UP:
                    if (volumes[0] < maxVolume) {
                        volumes[0]++;
                    }
                    text = buildVolumeLine1Response();
                    textLine1Right = buildVolumeLine1RightResponse();
                    textAscii = buildVolumeAsciiResponse();
                    break;
                case VOLUME_DOWN:
                case MAIN_ZONE_VOLUME_DOWN:
                    if (volumes[0] > minVolume) {
                        volumes[0]--;
                    }
                    text = buildVolumeLine1Response();
                    textLine1Right = buildVolumeLine1RightResponse();
                    textAscii = buildVolumeAsciiResponse();
                    break;
                case VOLUME_SET:
                    if (value != null) {
                        volumes[0] = value;
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
                    mutes[0] = !mutes[0];
                    text = buildSourceLine1Response();
                    textLine1Right = buildVolumeLine1RightResponse();
                    textAscii = buildMuteAsciiResponse();
                    break;
                case MUTE_ON:
                case MAIN_ZONE_MUTE_ON:
                    mutes[0] = true;
                    text = buildSourceLine1Response();
                    textLine1Right = buildVolumeLine1RightResponse();
                    textAscii = buildMuteAsciiResponse();
                    break;
                case MUTE_OFF:
                case MAIN_ZONE_MUTE_OFF:
                    mutes[0] = false;
                    text = buildSourceLine1Response();
                    textLine1Right = buildVolumeLine1RightResponse();
                    textAscii = buildMuteAsciiResponse();
                    break;
                case MUTE:
                    textAscii = buildMuteAsciiResponse();
                    break;
                case TONE_MAX:
                    textAscii = buildAsciiResponse(KEY_TONE_MAX, String.format("%02d", maxToneLevel));
                    break;
                case TONE_CONTROLS_ON:
                    tcbypass = false;
                    textAscii = buildAsciiResponse(KEY_TONE, !tcbypass);
                    break;
                case TONE_CONTROLS_OFF:
                    tcbypass = true;
                    textAscii = buildAsciiResponse(KEY_TONE, !tcbypass);
                    break;
                case TONE_CONTROLS:
                    textAscii = buildAsciiResponse(KEY_TONE, !tcbypass);
                    break;
                case TCBYPASS_ON:
                    tcbypass = true;
                    textAscii = buildAsciiResponse(KEY_TCBYPASS, tcbypass);
                    break;
                case TCBYPASS_OFF:
                    tcbypass = false;
                    textAscii = buildAsciiResponse(KEY_TCBYPASS, tcbypass);
                    break;
                case TCBYPASS:
                    textAscii = buildAsciiResponse(KEY_TCBYPASS, tcbypass);
                    break;
                case BASS_UP:
                    if (!tcbypass && basses[0] < maxToneLevel) {
                        basses[0] += STEP_TONE_LEVEL;
                    }
                    text = buildBassLine1Response();
                    textLine1Right = buildBassLine1RightResponse();
                    textAscii = buildBassAsciiResponse();
                    break;
                case BASS_DOWN:
                    if (!tcbypass && basses[0] > minToneLevel) {
                        basses[0] -= STEP_TONE_LEVEL;
                    }
                    text = buildBassLine1Response();
                    textLine1Right = buildBassLine1RightResponse();
                    textAscii = buildBassAsciiResponse();
                    break;
                case BASS_SET:
                    if (!tcbypass && value != null) {
                        basses[0] = value;
                    }
                    text = buildBassLine1Response();
                    textLine1Right = buildBassLine1RightResponse();
                    textAscii = buildBassAsciiResponse();
                    break;
                case BASS:
                    textAscii = buildBassAsciiResponse();
                    break;
                case TREBLE_UP:
                    if (!tcbypass && trebles[0] < maxToneLevel) {
                        trebles[0] += STEP_TONE_LEVEL;
                    }
                    text = buildTrebleLine1Response();
                    textLine1Right = buildTrebleLine1RightResponse();
                    textAscii = buildTrebleAsciiResponse();
                    break;
                case TREBLE_DOWN:
                    if (!tcbypass && trebles[0] > minToneLevel) {
                        trebles[0] -= STEP_TONE_LEVEL;
                    }
                    text = buildTrebleLine1Response();
                    textLine1Right = buildTrebleLine1RightResponse();
                    textAscii = buildTrebleAsciiResponse();
                    break;
                case TREBLE_SET:
                    if (!tcbypass && value != null) {
                        trebles[0] = value;
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
                case BALANCE_LEFT:
                    if (balances[0] > minBalance) {
                        balances[0]--;
                    }
                    textAscii = buildBalanceAsciiResponse();
                    break;
                case BALANCE_RIGHT:
                    if (balances[0] < maxBalance) {
                        balances[0]++;
                    }
                    textAscii = buildBalanceAsciiResponse();
                    break;
                case BALANCE_SET:
                    if (value != null) {
                        balances[0] = value;
                    }
                    textAscii = buildBalanceAsciiResponse();
                    break;
                case BALANCE:
                    textAscii = buildBalanceAsciiResponse();
                    break;
                case SPEAKER_A_TOGGLE:
                    speakerA = !speakerA;
                    textAscii = buildSpeakerAsciiResponse();
                    break;
                case SPEAKER_A_ON:
                    speakerA = true;
                    textAscii = buildSpeakerAsciiResponse();
                    break;
                case SPEAKER_A_OFF:
                    speakerA = false;
                    textAscii = buildSpeakerAsciiResponse();
                    break;
                case SPEAKER_B_TOGGLE:
                    speakerB = !speakerB;
                    textAscii = buildSpeakerAsciiResponse();
                    break;
                case SPEAKER_B_ON:
                    speakerB = true;
                    textAscii = buildSpeakerAsciiResponse();
                    break;
                case SPEAKER_B_OFF:
                    speakerB = false;
                    textAscii = buildSpeakerAsciiResponse();
                    break;
                case SPEAKER:
                    textAscii = buildSpeakerAsciiResponse();
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
                case TRACK_FWD:
                    track++;
                    textAscii = buildTrackAsciiResponse();
                    break;
                case TRACK_BACK:
                    if (track > 1) {
                        track--;
                    }
                    textAscii = buildTrackAsciiResponse();
                    break;
                case TRACK:
                    textAscii = buildTrackAsciiResponse();
                    break;
                case RANDOM_TOGGLE:
                    randomMode = !randomMode;
                    textAscii = buildRandomModeAsciiResponse();
                    break;
                case RANDOM_MODE:
                    textAscii = buildRandomModeAsciiResponse();
                    break;
                case REPEAT_TOGGLE:
                    switch (repeatMode) {
                        case TRACK:
                            repeatMode = RotelRepeatMode.DISC;
                            break;
                        case DISC:
                            repeatMode = RotelRepeatMode.OFF;
                            break;
                        case OFF:
                            repeatMode = RotelRepeatMode.TRACK;
                            break;
                    }
                    textAscii = buildRepeatModeAsciiResponse();
                    break;
                case REPEAT_MODE:
                    textAscii = buildRepeatModeAsciiResponse();
                    break;
                case CALL_FM_PRESET:
                    if (value != null) {
                        fmPreset = value.intValue();
                        if (protocol == RotelProtocol.ASCII_V1) {
                            variableLength = true;
                            textAscii = buildAsciiResponse(String.format("%s%d", KEY_FM_PRESET, fmPreset),
                                    "8,Radio FM");
                        } else {
                            accepted = false;
                        }
                    } else {
                        accepted = false;
                    }
                    break;
                case CALL_DAB_PRESET:
                    if (value != null) {
                        dabPreset = value.intValue();
                        if (protocol == RotelProtocol.ASCII_V1) {
                            variableLength = true;
                            textAscii = buildAsciiResponse(String.format("%s%d", KEY_DAB_PRESET, dabPreset),
                                    "9,Radio DAB");
                        } else {
                            accepted = false;
                        }
                    } else {
                        accepted = false;
                    }
                    break;
                case CALL_IRADIO_PRESET:
                    if (value != null) {
                        iradioPreset = value.intValue();
                        variableLength = true;
                        textAscii = buildAsciiResponse(String.format("%s%d", KEY_IRADIO_PRESET, iradioPreset),
                                "12,Radio iRadio");
                    } else {
                        accepted = false;
                    }
                    break;
                case PRESET:
                    if ("FM".equals(sources[0].getName())) {
                        textAscii = buildAsciiResponse(KEY_PRESET_FM, fmPreset);
                    } else if ("DAB".equals(sources[0].getName())) {
                        textAscii = buildAsciiResponse(KEY_PRESET_DAB, dabPreset);
                    } else if ("IRADIO".equals(sources[0].getName())) {
                        textAscii = buildAsciiResponse(KEY_PRESET_IRADIO, iradioPreset);
                    } else {
                        textAscii = buildAsciiResponse(KEY_PRESET_FM, 0);
                    }
                    break;
                case FM_PRESET:
                    if ("FM".equals(sources[0].getName())) {
                        textAscii = buildAsciiResponse(KEY_FM, String.format("%02d", fmPreset));
                    } else {
                        textAscii = buildAsciiResponse(KEY_FM, "00");
                    }
                    break;
                case DAB_PRESET:
                    if ("DAB".equals(sources[0].getName())) {
                        textAscii = buildAsciiResponse(KEY_DAB, String.format("%02d", dabPreset));
                    } else {
                        textAscii = buildAsciiResponse(KEY_DAB, "00");
                    }
                    break;
                case SOURCE_MULTI_INPUT:
                    multiinput = !multiinput;
                    text = "MULTI IN " + (multiinput ? "ON" : "OFF");
                    try {
                        sources[0] = model.getSourceFromCommand(cmd);
                        textLine1Left = buildSourceLine1LeftResponse();
                        textAscii = buildSourceAsciiResponse();
                        mutes[0] = false;
                    } catch (RotelException e) {
                    }
                    break;
                case SOURCE:
                case INPUT:
                    textAscii = buildSourceAsciiResponse();
                    break;
                case STEREO:
                    dsp = RotelDsp.CAT4_NONE;
                    textLine2 = bypass ? "BYPASS" : "STEREO";
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
                    dsp = RotelDsp.CAT5_BYPASS;
                    textAscii = buildDspAsciiResponse();
                    break;
                case DSP_MODE:
                    textAscii = buildDspAsciiResponse();
                    break;
                case STEREO_BYPASS_TOGGLE:
                    bypass = !bypass;
                    textLine2 = bypass ? "BYPASS" : "STEREO";
                    break;
                case FREQUENCY:
                    textAscii = model.getNumberOfZones() > 1 ? buildAsciiResponse(KEY_FREQ, "44.1,48,none,176.4")
                            : buildAsciiResponse(KEY_FREQ, "44.1");
                    break;
                case SUB_LEVEL_UP:
                    subLevel += STEP_DECIBEL;
                    textAscii = buildAsciiResponse(KEY_SUB_LEVEL, buildDecibelValue(subLevel));
                    break;
                case SUB_LEVEL_DOWN:
                    subLevel -= STEP_DECIBEL;
                    textAscii = buildAsciiResponse(KEY_SUB_LEVEL, buildDecibelValue(subLevel));
                    break;
                case C_LEVEL_UP:
                    centerLevel += STEP_DECIBEL;
                    textAscii = buildAsciiResponse(KEY_CENTER_LEVEL, buildDecibelValue(centerLevel));
                    break;
                case C_LEVEL_DOWN:
                    centerLevel -= STEP_DECIBEL;
                    textAscii = buildAsciiResponse(KEY_CENTER_LEVEL, buildDecibelValue(centerLevel));
                    break;
                case SR_LEVEL_UP:
                    surroundRightLevel += STEP_DECIBEL;
                    textAscii = buildAsciiResponse(KEY_SURROUND_RIGHT_LEVEL, buildDecibelValue(surroundRightLevel));
                    break;
                case SR_LEVEL_DOWN:
                    surroundRightLevel -= STEP_DECIBEL;
                    textAscii = buildAsciiResponse(KEY_SURROUND_RIGHT_LEVEL, buildDecibelValue(surroundRightLevel));
                    break;
                case SL_LEVEL_UP:
                    surroundLefLevel += STEP_DECIBEL;
                    textAscii = buildAsciiResponse(KEY_SURROUND_LEFT_LEVEL, buildDecibelValue(surroundLefLevel));
                    break;
                case SL_LEVEL_DOWN:
                    surroundLefLevel -= STEP_DECIBEL;
                    textAscii = buildAsciiResponse(KEY_SURROUND_LEFT_LEVEL, buildDecibelValue(surroundLefLevel));
                    break;
                case CBR_LEVEL_UP:
                    centerBackRightLevel += STEP_DECIBEL;
                    textAscii = buildAsciiResponse(KEY_CENTER_BACK_RIGHT_LEVEL,
                            buildDecibelValue(centerBackRightLevel));
                    break;
                case CBR_LEVEL_DOWN:
                    centerBackRightLevel -= STEP_DECIBEL;
                    textAscii = buildAsciiResponse(KEY_CENTER_BACK_RIGHT_LEVEL,
                            buildDecibelValue(centerBackRightLevel));
                    break;
                case CBL_LEVEL_UP:
                    centerBackLefLevel += STEP_DECIBEL;
                    textAscii = buildAsciiResponse(KEY_CENTER_BACK_LEFT_LEVEL, buildDecibelValue(centerBackLefLevel));
                    break;
                case CBL_LEVEL_DOWN:
                    centerBackLefLevel -= STEP_DECIBEL;
                    textAscii = buildAsciiResponse(KEY_CENTER_BACK_LEFT_LEVEL, buildDecibelValue(centerBackLefLevel));
                    break;
                case CFR_LEVEL_UP:
                    ceilingFrontRightLevel += STEP_DECIBEL;
                    textAscii = buildAsciiResponse(KEY_CEILING_FRONT_RIGHT_LEVEL,
                            buildDecibelValue(ceilingFrontRightLevel));
                    break;
                case CFR_LEVEL_DOWN:
                    ceilingFrontRightLevel -= STEP_DECIBEL;
                    textAscii = buildAsciiResponse(KEY_CEILING_FRONT_RIGHT_LEVEL,
                            buildDecibelValue(ceilingFrontRightLevel));
                    break;
                case CFL_LEVEL_UP:
                    ceilingFrontLefLevel += STEP_DECIBEL;
                    textAscii = buildAsciiResponse(KEY_CEILING_FRONT_LEFT_LEVEL,
                            buildDecibelValue(ceilingFrontLefLevel));
                    break;
                case CFL_LEVEL_DOWN:
                    ceilingFrontLefLevel -= STEP_DECIBEL;
                    textAscii = buildAsciiResponse(KEY_CEILING_FRONT_LEFT_LEVEL,
                            buildDecibelValue(ceilingFrontLefLevel));
                    break;
                case CRR_LEVEL_UP:
                    ceilingRearRightLevel += STEP_DECIBEL;
                    textAscii = buildAsciiResponse(KEY_CEILING_REAR_RIGHT_LEVEL,
                            buildDecibelValue(ceilingRearRightLevel));
                    break;
                case CRR_LEVEL_DOWN:
                    ceilingRearRightLevel -= STEP_DECIBEL;
                    textAscii = buildAsciiResponse(KEY_CEILING_REAR_RIGHT_LEVEL,
                            buildDecibelValue(ceilingRearRightLevel));
                    break;
                case CRL_LEVEL_UP:
                    ceilingRearLefLevel += STEP_DECIBEL;
                    textAscii = buildAsciiResponse(KEY_CEILING_REAR_LEFT_LEVEL, buildDecibelValue(ceilingRearLefLevel));
                    break;
                case CRL_LEVEL_DOWN:
                    ceilingRearLefLevel -= STEP_DECIBEL;
                    textAscii = buildAsciiResponse(KEY_CEILING_REAR_LEFT_LEVEL, buildDecibelValue(ceilingRearLefLevel));
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
                case PCUSB_CLASS_1:
                    pcUsbClass = 1;
                    textAscii = buildAsciiResponse(KEY_PCUSB_CLASS, pcUsbClass);
                    break;
                case PCUSB_CLASS_2:
                    pcUsbClass = 2;
                    textAscii = buildAsciiResponse(KEY_PCUSB_CLASS, pcUsbClass);
                    break;
                case PCUSB_CLASS:
                    textAscii = buildAsciiResponse(KEY_PCUSB_CLASS, pcUsbClass);
                    break;
                case MODEL:
                    if (protocol == RotelProtocol.ASCII_V1) {
                        variableLength = true;
                        textAscii = buildAsciiResponse(KEY_PRODUCT_TYPE,
                                String.format("%d,%s", model.getName().length(), model.getName()));
                    } else {
                        textAscii = buildAsciiResponse(KEY_MODEL, model.getName());
                    }
                    break;
                case VERSION:
                    if (protocol == RotelProtocol.ASCII_V1) {
                        variableLength = true;
                        textAscii = buildAsciiResponse(KEY_PRODUCT_VERSION,
                                String.format("%d,%s", FIRMWARE.length(), FIRMWARE));
                    } else {
                        textAscii = buildAsciiResponse(KEY_VERSION, FIRMWARE);
                    }
                    break;
                default:
                    accepted = false;
                    break;
            }
            if (!accepted) {
                // Check if command is a change of source input for the main zone
                try {
                    sources[0] = model.getZoneSourceFromCommand(cmd, 1);
                    text = buildSourceLine1Response();
                    textLine1Left = buildSourceLine1LeftResponse();
                    textAscii = buildSourceAsciiResponse();
                    accepted = true;
                } catch (RotelException e) {
                }
            }
            if (!accepted) {
                // Check if command is a change of source input
                try {
                    if (selectingRecord && !model.hasOtherThanPrimaryCommands()) {
                        recordSource = model.getSourceFromCommand(cmd);
                    } else {
                        sources[0] = model.getSourceFromCommand(cmd);
                    }
                    text = buildSourceLine1Response();
                    textLine1Left = buildSourceLine1LeftResponse();
                    textAscii = buildSourceAsciiResponse();
                    mutes[0] = false;
                    accepted = true;
                } catch (RotelException e) {
                }
            }
            if (!accepted) {
                // Check if command is a change of record source
                try {
                    recordSource = model.getRecordSourceFromCommand(cmd);
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

        if (model.getRespNbChars() == 42) {
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

        if (protocol == RotelProtocol.HEX) {
            byte[] chars = Arrays.copyOf(text.getBytes(StandardCharsets.US_ASCII), model.getRespNbChars());
            byte[] flags = new byte[model.getRespNbFlags()];
            try {
                model.setMultiInput(flags, multiinput);
            } catch (RotelException e) {
            }
            try {
                model.setZone2(flags, powers[2]);
            } catch (RotelException e) {
            }
            try {
                model.setZone3(flags, powers[3]);
            } catch (RotelException e) {
            }
            try {
                model.setZone4(flags, powers[4]);
            } catch (RotelException e) {
            }
            int size = 6 + model.getRespNbChars() + model.getRespNbFlags();
            byte[] dataBuffer = new byte[size];
            int idx = 0;
            dataBuffer[idx++] = START;
            dataBuffer[idx++] = (byte) (size - 4);
            dataBuffer[idx++] = model.getDeviceId();
            dataBuffer[idx++] = STANDARD_RESPONSE;
            if (model.isCharsBeforeFlags()) {
                System.arraycopy(chars, 0, dataBuffer, idx, model.getRespNbChars());
                idx += model.getRespNbChars();
                System.arraycopy(flags, 0, dataBuffer, idx, model.getRespNbFlags());
                idx += model.getRespNbFlags();
            } else {
                System.arraycopy(flags, 0, dataBuffer, idx, model.getRespNbFlags());
                idx += model.getRespNbFlags();
                System.arraycopy(chars, 0, dataBuffer, idx, model.getRespNbChars());
                idx += model.getRespNbChars();
            }
            byte checksum = RotelHexProtocolHandler.computeCheckSum(dataBuffer, idx - 1);
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
            String command = textAscii;
            if (protocol == RotelProtocol.ASCII_V1 && !variableLength) {
                command += "!";
            } else if (protocol == RotelProtocol.ASCII_V2 && !variableLength) {
                command += "$";
            } else if (protocol == RotelProtocol.ASCII_V2 && variableLength) {
                command += "$$";
            }
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
        return String.format("%s=%d", key, value);
    }

    private String buildAsciiResponse(String key, boolean value) {
        return buildAsciiResponse(key, buildOnOffValue(value));
    }

    private String buildOnOffValue(boolean on) {
        return on ? MSG_VALUE_ON : MSG_VALUE_OFF;
    }

    private String buildPowerAsciiResponse() {
        return buildAsciiResponse(KEY_POWER, powers[0] ? POWER_ON : STANDBY);
    }

    private String buildVolumeAsciiResponse() {
        if (model.getNumberOfZones() > 1) {
            StringJoiner sj = new StringJoiner(",");
            for (int zone = 1; zone <= model.getNumberOfZones(); zone++) {
                sj.add(String.format("%02d", volumes[zone]));
            }
            return buildAsciiResponse(KEY_VOLUME, sj.toString());
        } else {
            return buildAsciiResponse(KEY_VOLUME, String.format("%02d", volumes[0]));
        }
    }

    private String buildMuteAsciiResponse() {
        if (model.getNumberOfZones() > 1) {
            StringJoiner sj = new StringJoiner(",");
            for (int zone = 1; zone <= model.getNumberOfZones(); zone++) {
                sj.add(buildOnOffValue(mutes[zone]));
            }
            return buildAsciiResponse(KEY_MUTE, sj.toString());
        } else {
            return buildAsciiResponse(KEY_MUTE, mutes[0]);
        }
    }

    private String buildBassAsciiResponse() {
        if (model.getNumberOfZones() > 1) {
            StringJoiner sj = new StringJoiner(",");
            for (int zone = 1; zone <= model.getNumberOfZones(); zone++) {
                sj.add(buildBassTrebleValue(basses[zone]));
            }
            return buildAsciiResponse(KEY_BASS, sj.toString());
        } else {
            return buildAsciiResponse(KEY_BASS, buildBassTrebleValue(basses[0]));
        }
    }

    private String buildTrebleAsciiResponse() {
        if (model.getNumberOfZones() > 1) {
            StringJoiner sj = new StringJoiner(",");
            for (int zone = 1; zone <= model.getNumberOfZones(); zone++) {
                sj.add(buildBassTrebleValue(trebles[zone]));
            }
            return buildAsciiResponse(KEY_TREBLE, sj.toString());
        } else {
            return buildAsciiResponse(KEY_TREBLE, buildBassTrebleValue(trebles[0]));
        }
    }

    private String buildBassTrebleValue(int value) {
        if (tcbypass || value == 0) {
            return "000";
        } else if (value > 0) {
            return String.format("+%02d", value);
        } else {
            return String.format("-%02d", -value);
        }
    }

    private String buildBalanceAsciiResponse() {
        if (model.getNumberOfZones() > 1) {
            StringJoiner sj = new StringJoiner(",");
            for (int zone = 1; zone <= model.getNumberOfZones(); zone++) {
                sj.add(buildBalanceValue(balances[zone]));
            }
            return buildAsciiResponse(KEY_BALANCE, sj.toString());
        } else {
            return buildAsciiResponse(KEY_BALANCE, buildBalanceValue(balances[0]));
        }
    }

    private String buildBalanceValue(int value) {
        if (value == 0) {
            return "000";
        } else if (value > 0) {
            return String.format("r%02d", value);
        } else {
            return String.format("l%02d", -value);
        }
    }

    private String buildSpeakerAsciiResponse() {
        String value;
        if (speakerA && speakerB) {
            value = MSG_VALUE_SPEAKER_AB;
        } else if (speakerA && !speakerB) {
            value = MSG_VALUE_SPEAKER_A;
        } else if (!speakerA && speakerB) {
            value = MSG_VALUE_SPEAKER_B;
        } else {
            value = MSG_VALUE_OFF;
        }
        return buildAsciiResponse(KEY_SPEAKER, value);
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
        return buildAsciiResponse(protocol == RotelProtocol.ASCII_V1 ? KEY1_PLAY_STATUS : KEY2_PLAY_STATUS, status);
    }

    private String buildTrackAsciiResponse() {
        return buildAsciiResponse(KEY_TRACK, String.format("%03d", track));
    }

    private String buildRandomModeAsciiResponse() {
        return buildAsciiResponse(KEY_RANDOM, randomMode);
    }

    private String buildRepeatModeAsciiResponse() {
        String mode = "";
        switch (repeatMode) {
            case TRACK:
                mode = TRACK;
                break;
            case DISC:
                mode = DISC;
                break;
            case OFF:
                mode = MSG_VALUE_OFF;
                break;
        }
        return buildAsciiResponse(KEY_REPEAT, mode);
    }

    private String buildSourceAsciiResponse() {
        if (model.getNumberOfZones() > 1) {
            StringJoiner sj = new StringJoiner(",");
            for (int zone = 1; zone <= model.getNumberOfZones(); zone++) {
                sj.add(buildZoneSourceValue(sources[zone]));
            }
            return buildAsciiResponse(KEY_INPUT, sj.toString());
        } else {
            return buildAsciiResponse(KEY_SOURCE, buildSourceValue(sources[0]));
        }
    }

    private String buildSourceValue(RotelSource source) {
        String str = null;
        RotelCommand command = source.getCommand();
        if (command != null) {
            str = protocol == RotelProtocol.ASCII_V1 ? command.getAsciiCommandV1() : command.getAsciiCommandV2();
        }
        return str == null ? "" : str;
    }

    private String buildZoneSourceValue(RotelSource source) {
        String str = buildSourceValue(source);
        int idx = str.indexOf("input_");
        return idx < 0 ? str : str.substring(idx + 6);
    }

    private String buildDspAsciiResponse() {
        return buildAsciiResponse(KEY_DSP_MODE, dsp.getFeedback());
    }

    private String buildDecibelValue(double value) {
        if (value == 0.0) {
            return "000.0db";
        } else {
            return String.format("%+05.1fdb", value).replace(",", ".");
        }
    }

    private String buildSourceLine1Response() {
        String text;
        if (!powers[0]) {
            text = "";
        } else if (mutes[0]) {
            text = "MUTE ON";
        } else {
            text = getSourceLabel(sources[0], false) + " " + getSourceLabel(recordSource, true);
        }
        return text;
    }

    private String buildSourceLine1LeftResponse() {
        String text;
        if (!powers[0]) {
            text = "";
        } else {
            text = getSourceLabel(sources[0], false);
        }
        return text;
    }

    private String buildRecordResponse() {
        String text;
        if (!powers[0]) {
            text = "";
        } else {
            text = "REC " + getSourceLabel(recordSource, true);
        }
        return text;
    }

    private String buildZonePowerResponse(int numZone) {
        String zone;
        if (numZone == 2) {
            zone = model.getNumberOfZones() > 2 ? "ZONE2" : "ZONE";
        } else {
            zone = String.format("ZONE%d", numZone);
        }
        String state = powers[numZone] ? getSourceLabel(sources[numZone], true) : "OFF";
        return zone + " " + state;
    }

    private String buildVolumeLine1Response() {
        String text;
        if (volumes[0] == minVolume) {
            text = " VOLUME  MIN ";
        } else if (volumes[0] == maxVolume) {
            text = " VOLUME  MAX ";
        } else {
            text = String.format(" VOLUME   %02d ", volumes[0]);
        }
        return text;
    }

    private String buildVolumeLine1RightResponse() {
        String text;
        if (!powers[0]) {
            text = "";
        } else if (mutes[0]) {
            text = "MUTE ON";
        } else if (volumes[0] == minVolume) {
            text = "VOL MIN";
        } else if (volumes[0] == maxVolume) {
            text = "VOL MAX";
        } else {
            text = String.format("VOL  %02d", volumes[0]);
        }
        return text;
    }

    private String buildZoneVolumeResponse(int numZone) {
        String zone;
        if (numZone == 2) {
            zone = model.getNumberOfZones() > 2 ? "ZONE2" : "ZONE";
        } else {
            zone = String.format("ZONE%d", numZone);
        }
        String text;
        if (mutes[numZone]) {
            text = zone + " MUTE ON";
        } else if (volumes[numZone] == minVolume) {
            text = zone + " VOL MIN";
        } else if (volumes[numZone] == maxVolume) {
            text = zone + " VOL MAX";
        } else {
            text = String.format("%s VOL %02d", zone, volumes[numZone]);
        }
        return text;
    }

    private String buildBassLine1Response() {
        String text;
        if (basses[0] == minToneLevel) {
            text = "   BASS  MIN ";
        } else if (basses[0] == maxToneLevel) {
            text = "   BASS  MAX ";
        } else if (basses[0] == 0) {
            text = "   BASS    0 ";
        } else if (basses[0] > 0) {
            text = String.format("   BASS  +%02d ", basses[0]);
        } else {
            text = String.format("   BASS  -%02d ", -basses[0]);
        }
        return text;
    }

    private String buildBassLine1RightResponse() {
        String text;
        if (basses[0] == minToneLevel) {
            text = "LF  MIN";
        } else if (basses[0] == maxToneLevel) {
            text = "LF  MAX";
        } else if (basses[0] == 0) {
            text = "LF    0";
        } else if (basses[0] > 0) {
            text = String.format("LF + %02d", basses[0]);
        } else {
            text = String.format("LF - %02d", -basses[0]);
        }
        return text;
    }

    private String buildTrebleLine1Response() {
        String text;
        if (trebles[0] == minToneLevel) {
            text = " TREBLE  MIN ";
        } else if (trebles[0] == maxToneLevel) {
            text = " TREBLE  MAX ";
        } else if (trebles[0] == 0) {
            text = " TREBLE    0 ";
        } else if (trebles[0] > 0) {
            text = String.format(" TREBLE  +%02d ", trebles[0]);
        } else {
            text = String.format(" TREBLE  -%02d ", -trebles[0]);
        }
        return text;
    }

    private String buildTrebleLine1RightResponse() {
        String text;
        if (trebles[0] == minToneLevel) {
            text = "HF  MIN";
        } else if (trebles[0] == maxToneLevel) {
            text = "HF  MAX";
        } else if (trebles[0] == 0) {
            text = "HF    0";
        } else if (trebles[0] > 0) {
            text = String.format("HF + %02d", trebles[0]);
        } else {
            text = String.format("HF - %02d", -trebles[0]);
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
