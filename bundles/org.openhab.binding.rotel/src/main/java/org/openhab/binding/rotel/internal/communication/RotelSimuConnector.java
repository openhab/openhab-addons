/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
    private int showZone;

    private int minVolume;
    private int maxVolume;
    private int maxToneLevel;

    /**
     * Constructor
     *
     * @param model the projector model in use
     * @param protocol the protocol to be used
     */
    public RotelSimuConnector(RotelModel model, RotelProtocol protocol) {
        super(model, protocol, true);
        this.minVolume = 0;
        this.maxVolume = model.hasVolumeControl() ? model.getVolumeMax() : 0;
        this.maxToneLevel = model.hasToneControl() ? model.getToneLevelMax() : 0;
    }

    @Override
    public synchronized void open() throws RotelException {
        logger.debug("Opening simulated connection");
        Thread thread = new RotelReaderThread(this);
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
            throw new InterruptedIOException(e.getMessage());
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
        String text = "XXX";
        String textLine1Left = source.getLabel();
        String textLine1Right;
        String textLine2 = "";
        if (mute) {
            textLine1Right = "MUTE ON";
        } else if (volume == minVolume) {
            textLine1Right = "VOL MIN";
        } else if (volume == maxVolume) {
            textLine1Right = "VOL MAX";
        } else {
            textLine1Right = String.format("VOL  %02d", volume);
        }
        String textAscii = "";
        String zone;
        if (cmd == RotelCommand.POWER_OFF || cmd == RotelCommand.MAIN_ZONE_POWER_OFF) {
            power = false;
            text = "";
            textLine1Left = "";
            textLine1Right = "";
            textAscii = KEY_POWER + "=" + STANDBY;
        } else if (cmd == RotelCommand.POWER_ON || cmd == RotelCommand.MAIN_ZONE_POWER_ON) {
            power = true;
            text = source.getLabel() + "  YYY";
            textAscii = KEY_POWER + "=" + POWER_ON;
        } else if (cmd == RotelCommand.POWER) {
            textAscii = KEY_POWER + "=" + (power ? POWER_ON : STANDBY);
        } else if (!power) {
            text = "";
            textLine1Left = "";
            textLine1Right = "";
        } else if (cmd == RotelCommand.ZONE2_POWER_OFF) {
            powerZone2 = false;
            zone = getModel().getNbAdditionalZones() > 1 ? "ZONE2 " : "ZONE ";
            text = zone + "OFF";
            textLine2 = zone + "OFF";
        } else if (cmd == RotelCommand.ZONE2_POWER_ON) {
            powerZone2 = true;
            zone = getModel().getNbAdditionalZones() > 1 ? "ZONE2 " : "ZONE ";
            text = zone + getSourceLabel(sourceZone2);
            textLine2 = zone + getSourceLabel(sourceZone2);
        } else if (cmd == RotelCommand.ZONE3_POWER_OFF) {
            powerZone3 = false;
            zone = "ZONE3 ";
            text = zone + "OFF";
            textLine2 = zone + "OFF";
        } else if (cmd == RotelCommand.ZONE3_POWER_ON) {
            powerZone3 = true;
            zone = "ZONE3 ";
            text = zone + getSourceLabel(sourceZone3);
            textLine2 = zone + getSourceLabel(sourceZone3);
        } else if (cmd == RotelCommand.ZONE4_POWER_OFF) {
            powerZone4 = false;
            zone = "ZONE4 ";
            text = zone + "OFF";
            textLine2 = zone + "OFF";
        } else if (cmd == RotelCommand.ZONE4_POWER_ON) {
            powerZone4 = true;
            zone = "ZONE4 ";
            text = zone + getSourceLabel(sourceZone4);
            textLine2 = zone + getSourceLabel(sourceZone4);
        } else if (cmd == RotelCommand.DISPLAY_REFRESH) {
            text = source.getLabel() + "  XXX";
        } else if (cmd == RotelCommand.UPDATE_AUTO) {
            textAscii = (getProtocol() == RotelProtocol.ASCII_V1 ? KEY_DISPLAY_UPDATE : KEY_UPDATE_MODE) + "=" + AUTO;
        } else if (cmd == RotelCommand.UPDATE_MANUAL) {
            textAscii = (getProtocol() == RotelProtocol.ASCII_V1 ? KEY_DISPLAY_UPDATE : KEY_UPDATE_MODE) + "=" + MANUAL;
        } else if (cmd == RotelCommand.VOLUME_GET_MIN) {
            textAscii = String.format("%s=%d", KEY_VOLUME_MIN, minVolume);
        } else if (cmd == RotelCommand.VOLUME_GET_MAX) {
            textAscii = String.format("%s=%d", KEY_VOLUME_MAX, maxVolume);
        } else if (cmd == RotelCommand.VOLUME_UP || cmd == RotelCommand.MAIN_ZONE_VOLUME_UP) {
            if (volume < maxVolume) {
                volume++;
            }
            if (volume == maxVolume) {
                text = " VOLUME  MAX ";
                textLine1Right = "VOL MAX";
            } else {
                text = String.format(" VOLUME   %02d ", volume);
                textLine1Right = String.format("VOL  %02d", volume);
            }
            textAscii = String.format("%s=%02d", KEY_VOLUME, volume);
        } else if (cmd == RotelCommand.VOLUME_DOWN || cmd == RotelCommand.MAIN_ZONE_VOLUME_DOWN) {
            if (volume > minVolume) {
                volume--;
            }
            if (volume == minVolume) {
                text = " VOLUME  MIN ";
                textLine1Right = "VOL MIN";
            } else {
                text = String.format(" VOLUME   %02d ", volume);
                textLine1Right = String.format("VOL  %02d", volume);
            }
            textAscii = String.format("%s=%02d", KEY_VOLUME, volume);
        } else if (cmd == RotelCommand.VOLUME_SET && value != null) {
            volume = value;
            if (volume == minVolume) {
                text = " VOLUME  MIN ";
                textLine1Right = "VOL MIN";
            } else if (volume == maxVolume) {
                text = " VOLUME  MAX ";
                textLine1Right = "VOL MAX";
            } else {
                text = String.format(" VOLUME   %02d ", volume);
                textLine1Right = String.format("VOL  %02d", volume);
            }
            textAscii = String.format("%s=%02d", KEY_VOLUME, volume);
        } else if (cmd == RotelCommand.VOLUME_GET) {
            textAscii = String.format("%s=%02d", KEY_VOLUME, volume);
        } else if (cmd == RotelCommand.ZONE2_VOLUME_UP) {
            if (volumeZone2 < maxVolume) {
                volumeZone2++;
            }
            zone = getModel().getNbAdditionalZones() > 1 ? "ZONE2" : "ZONE";
            if (volumeZone2 == maxVolume) {
                text = zone + " VOL MAX";
                textLine2 = zone + " VOL MAX";
            } else {
                text = String.format("%s VOL %02d", zone, volumeZone2);
                textLine2 = String.format("%s VOL %02d", zone, volumeZone2);
            }
        } else if (cmd == RotelCommand.ZONE2_VOLUME_DOWN) {
            if (volumeZone2 > minVolume) {
                volumeZone2--;
            }
            zone = getModel().getNbAdditionalZones() > 1 ? "ZONE2" : "ZONE";
            if (volumeZone2 == minVolume) {
                text = zone + " VOL MIN";
                textLine2 = zone + " VOL MIN";
            } else {
                text = String.format("%s VOL %02d", zone, volumeZone2);
                textLine2 = String.format("%s VOL %02d", zone, volumeZone2);
            }
        } else if (cmd == RotelCommand.ZONE2_VOLUME_SET && value != null) {
            volumeZone2 = value;
            zone = getModel().getNbAdditionalZones() > 1 ? "ZONE2" : "ZONE";
            if (volumeZone2 == minVolume) {
                text = zone + " VOL MIN";
                textLine2 = zone + " VOL MIN";
            } else if (volumeZone2 == maxVolume) {
                text = zone + " VOL MAX";
                textLine2 = zone + " VOL MAX";
            } else {
                text = String.format("%s VOL %02d", zone, volumeZone2);
                textLine2 = String.format("%s VOL %02d", zone, volumeZone2);
            }
        } else if (cmd == RotelCommand.ZONE3_VOLUME_UP) {
            if (volumeZone3 < maxVolume) {
                volumeZone3++;
            }
            zone = "ZONE3";
            if (volumeZone3 == maxVolume) {
                text = zone + " VOL MAX";
                textLine2 = zone + " VOL MAX";
            } else {
                text = String.format("%s VOL %02d", zone, volumeZone3);
                textLine2 = String.format("%s VOL %02d", zone, volumeZone3);
            }
        } else if (cmd == RotelCommand.ZONE3_VOLUME_DOWN) {
            if (volumeZone3 > minVolume) {
                volumeZone3--;
            }
            zone = "ZONE3";
            if (volumeZone3 == minVolume) {
                text = zone + " VOL MIN";
                textLine2 = zone + " VOL MIN";
            } else {
                text = String.format("%s VOL %02d", zone, volumeZone3);
                textLine2 = String.format("%s VOL %02d", zone, volumeZone3);
            }
        } else if (cmd == RotelCommand.ZONE3_VOLUME_SET && value != null) {
            volumeZone3 = value;
            zone = "ZONE3";
            if (volumeZone3 == minVolume) {
                text = zone + " VOL MIN";
                textLine2 = zone + " VOL MIN";
            } else if (volumeZone3 == maxVolume) {
                text = zone + " VOL MAX";
                textLine2 = zone + " VOL MAX";
            } else {
                text = String.format("%s VOL %02d", zone, volumeZone3);
                textLine2 = String.format("%s VOL %02d", zone, volumeZone3);
            }
        } else if (cmd == RotelCommand.ZONE4_VOLUME_UP) {
            if (volumeZone4 < maxVolume) {
                volumeZone4++;
            }
            zone = "ZONE4";
            if (volumeZone4 == maxVolume) {
                text = zone + " VOL MAX";
                textLine2 = zone + " VOL MAX";
            } else {
                text = String.format("%s VOL %02d", zone, volumeZone4);
                textLine2 = String.format("%s VOL %02d", zone, volumeZone4);
            }
        } else if (cmd == RotelCommand.ZONE4_VOLUME_DOWN) {
            if (volumeZone4 > minVolume) {
                volumeZone4--;
            }
            zone = "ZONE4";
            if (volumeZone4 == minVolume) {
                text = zone + " VOL MIN";
                textLine2 = zone + " VOL MIN";
            } else {
                text = String.format("%s VOL %02d", zone, volumeZone4);
                textLine2 = String.format("%s VOL %02d", zone, volumeZone4);
            }
        } else if (cmd == RotelCommand.ZONE4_VOLUME_SET && value != null) {
            volumeZone4 = value;
            zone = "ZONE4";
            if (volumeZone4 == minVolume) {
                text = zone + " VOL MIN";
                textLine2 = zone + " VOL MIN";
            } else if (volumeZone4 == maxVolume) {
                text = zone + " VOL MAX";
                textLine2 = zone + " VOL MAX";
            } else {
                text = String.format("%s VOL %02d", zone, volumeZone4);
                textLine2 = String.format("%s VOL %02d", zone, volumeZone4);
            }
        } else if (cmd == RotelCommand.MUTE_TOGGLE || cmd == RotelCommand.MAIN_ZONE_MUTE_TOGGLE) {
            mute = !mute;
            text = mute ? "MUTE ON" : source.getLabel() + "  WWW";
            if (mute) {
                textLine1Right = "MUTE ON";
            } else if (volume == minVolume) {
                textLine1Right = "VOL MIN";
            } else if (volume == maxVolume) {
                textLine1Right = "VOL MAX";
            } else {
                textLine1Right = String.format("VOL  %02d", volume);
            }
            textAscii = KEY_MUTE + "=" + (mute ? MSG_VALUE_ON : MSG_VALUE_OFF);
        } else if (cmd == RotelCommand.MUTE_ON || cmd == RotelCommand.MAIN_ZONE_MUTE_ON) {
            mute = true;
            textAscii = KEY_MUTE + "=" + MSG_VALUE_ON;
        } else if (cmd == RotelCommand.MUTE_OFF || cmd == RotelCommand.MAIN_ZONE_MUTE_OFF) {
            mute = false;
            textAscii = KEY_MUTE + "=" + MSG_VALUE_OFF;
        } else if (cmd == RotelCommand.MUTE) {
            textAscii = KEY_MUTE + "=" + (mute ? MSG_VALUE_ON : MSG_VALUE_OFF);
        } else if (cmd == RotelCommand.ZONE2_MUTE_TOGGLE) {
            muteZone2 = !muteZone2;
            zone = getModel().getNbAdditionalZones() > 1 ? "ZONE2" : "ZONE";
            if (muteZone2) {
                text = zone + " MUTE ON";
                textLine2 = zone + " MUTE ON";
            } else if (volumeZone2 == minVolume) {
                text = zone + " VOL MIN";
                textLine2 = zone + " VOL MIN";
            } else if (volumeZone2 == maxVolume) {
                text = zone + " VOL MAX";
                textLine2 = zone + " VOL MAX";
            } else {
                text = String.format("%s VOL %02d", zone, volumeZone2);
                textLine2 = String.format("%s VOL %02d", zone, volumeZone2);
            }
        } else if (cmd == RotelCommand.ZONE2_MUTE_ON) {
            muteZone2 = true;
            zone = getModel().getNbAdditionalZones() > 1 ? "ZONE2" : "ZONE";
            text = zone + " MUTE ON";
            textLine2 = zone + " MUTE ON";
        } else if (cmd == RotelCommand.ZONE2_MUTE_OFF) {
            muteZone2 = false;
            zone = getModel().getNbAdditionalZones() > 1 ? "ZONE2" : "ZONE";
            if (volumeZone2 == minVolume) {
                text = zone + " VOL MIN";
                textLine2 = zone + " VOL MIN";
            } else if (volumeZone2 == maxVolume) {
                text = zone + " VOL MAX";
                textLine2 = zone + " VOL MAX";
            } else {
                text = String.format("%s VOL %02d", zone, volumeZone2);
                textLine2 = String.format("%s VOL %02d", zone, volumeZone2);
            }
        } else if (cmd == RotelCommand.ZONE3_MUTE_TOGGLE) {
            muteZone3 = !muteZone3;
            zone = "ZONE3";
            if (muteZone3) {
                text = zone + " MUTE ON";
                textLine2 = zone + " MUTE ON";
            } else if (volumeZone3 == minVolume) {
                text = zone + " VOL MIN";
                textLine2 = zone + " VOL MIN";
            } else if (volumeZone3 == maxVolume) {
                text = zone + " VOL MAX";
                textLine2 = zone + " VOL MAX";
            } else {
                text = String.format("%s VOL %02d", zone, volumeZone3);
                textLine2 = String.format("%s VOL %02d", zone, volumeZone3);
            }
        } else if (cmd == RotelCommand.ZONE3_MUTE_ON) {
            muteZone3 = true;
            zone = "ZONE3";
            text = zone + " MUTE ON";
            textLine2 = zone + " MUTE ON";
        } else if (cmd == RotelCommand.ZONE3_MUTE_OFF) {
            muteZone3 = false;
            zone = "ZONE3";
            if (volumeZone3 == minVolume) {
                text = zone + " VOL MIN";
                textLine2 = zone + " VOL MIN";
            } else if (volumeZone3 == maxVolume) {
                text = zone + " VOL MAX";
                textLine2 = zone + " VOL MAX";
            } else {
                text = String.format("%s VOL %02d", zone, volumeZone3);
                textLine2 = String.format("%s VOL %02d", zone, volumeZone3);
            }
        } else if (cmd == RotelCommand.ZONE4_MUTE_TOGGLE) {
            muteZone4 = !muteZone4;
            zone = "ZONE4";
            if (muteZone4) {
                text = zone + " MUTE ON";
                textLine2 = zone + " MUTE ON";
            } else if (volumeZone4 == minVolume) {
                text = zone + " VOL MIN";
                textLine2 = zone + " VOL MIN";
            } else if (volumeZone4 == maxVolume) {
                text = zone + " VOL MAX";
                textLine2 = zone + " VOL MAX";
            } else {
                text = String.format("%s VOL %02d", zone, volumeZone4);
                textLine2 = String.format("%s VOL %02d", zone, volumeZone4);
            }
        } else if (cmd == RotelCommand.ZONE4_MUTE_ON) {
            muteZone4 = true;
            zone = "ZONE4";
            text = zone + " MUTE ON";
            textLine2 = zone + " MUTE ON";
        } else if (cmd == RotelCommand.ZONE4_MUTE_OFF) {
            muteZone4 = false;
            zone = "ZONE4";
            if (volumeZone4 == minVolume) {
                text = zone + " VOL MIN";
                textLine2 = zone + " VOL MIN";
            } else if (volumeZone4 == maxVolume) {
                text = zone + " VOL MAX";
                textLine2 = zone + " VOL MAX";
            } else {
                text = String.format("%s VOL %02d", zone, volumeZone4);
                textLine2 = String.format("%s VOL %02d", zone, volumeZone4);
            }
        } else if (cmd == RotelCommand.TONE_MAX) {
            textAscii = String.format("%s=%02d", KEY_TONE_MAX, maxToneLevel);
        } else if (cmd == RotelCommand.BASS_UP) {
            if (bass < maxToneLevel) {
                bass += STEP_TONE_LEVEL;
            }
            if (bass == maxToneLevel) {
                text = "   BASS  MAX ";
                textLine1Right = "LF  MAX";
            } else if (bass == 0) {
                text = "   BASS    0 ";
                textLine1Right = "LF    0";
            } else if (bass > 0) {
                text = String.format("   BASS  +%02d ", bass);
                textLine1Right = String.format("LF + %02d", bass);
            } else {
                text = String.format("   BASS  -%02d ", -bass);
                textLine1Right = String.format("LF - %02d", -bass);
            }
            if (bass == 0) {
                textAscii = KEY_BASS + "=000";
            } else if (bass > 0) {
                textAscii = String.format("%s=+%02d", KEY_BASS, bass);
            } else {
                textAscii = String.format("%s=-%02d", KEY_BASS, -bass);
            }
        } else if (cmd == RotelCommand.BASS_DOWN) {
            if (bass > -maxToneLevel) {
                bass -= STEP_TONE_LEVEL;
            }
            if (bass == -maxToneLevel) {
                text = "   BASS  MIN ";
                textLine1Right = "LF  MIN";
            } else if (bass == 0) {
                text = "   BASS    0 ";
                textLine1Right = "LF    0";
            } else if (bass > 0) {
                text = String.format("   BASS  +%02d ", bass);
                textLine1Right = String.format("LF + %02d", bass);
            } else {
                text = String.format("   BASS  -%02d ", -bass);
                textLine1Right = String.format("LF - %02d", -bass);
            }
            if (bass == 0) {
                textAscii = KEY_BASS + "=000";
            } else if (bass > 0) {
                textAscii = String.format("%s=+%02d", KEY_BASS, bass);
            } else {
                textAscii = String.format("%s=-%02d", KEY_BASS, -bass);
            }
        } else if (cmd == RotelCommand.BASS_SET && value != null) {
            bass = value;
            if (bass == -maxToneLevel) {
                text = "   BASS  MIN ";
                textLine1Right = "LF  MIN";
            } else if (bass == maxToneLevel) {
                text = "   BASS  MAX ";
                textLine1Right = "LF  MAX";
            } else if (bass == 0) {
                text = "   BASS    0 ";
                textLine1Right = "LF    0";
            } else if (bass > 0) {
                text = String.format("   BASS  +%02d ", bass);
                textLine1Right = String.format("LF + %02d", bass);
            } else {
                text = String.format("   BASS  -%02d ", -bass);
                textLine1Right = String.format("LF - %02d", -bass);
            }
            if (bass == 0) {
                textAscii = KEY_BASS + "=000";
            } else if (bass > 0) {
                textAscii = String.format("%s=+%02d", KEY_BASS, bass);
            } else {
                textAscii = String.format("%s=-%02d", KEY_BASS, -bass);
            }
        } else if (cmd == RotelCommand.BASS) {
            if (bass == 0) {
                textAscii = KEY_BASS + "=000";
            } else if (bass > 0) {
                textAscii = String.format("%s=+%02d", KEY_BASS, bass);
            } else {
                textAscii = String.format("%s=-%02d", KEY_BASS, -bass);
            }
        } else if (cmd == RotelCommand.TREBLE_UP) {
            if (treble < maxToneLevel) {
                treble += STEP_TONE_LEVEL;
            }
            if (treble == maxToneLevel) {
                text = " TREBLE  MAX ";
                textLine1Right = "HF  MAX";
            } else if (treble == 0) {
                text = " TREBLE    0 ";
                textLine1Right = "HF    0";
            } else if (treble > 0) {
                text = String.format(" TREBLE  +%02d ", treble);
                textLine1Right = String.format("HF + %02d", treble);
            } else {
                text = String.format(" TREBLE  -%02d ", -treble);
                textLine1Right = String.format("HF - %02d", -treble);
            }
            if (treble == 0) {
                textAscii = KEY_TREBLE + "=000";
            } else if (treble > 0) {
                textAscii = String.format("%s=+%02d", KEY_TREBLE, treble);
            } else {
                textAscii = String.format("%s=-%02d", KEY_TREBLE, -treble);
            }
        } else if (cmd == RotelCommand.TREBLE_DOWN) {
            if (treble > -maxToneLevel) {
                treble -= STEP_TONE_LEVEL;
            }
            if (treble == -maxToneLevel) {
                text = " TREBLE  MIN ";
                textLine1Right = "HF  MIN";
            } else if (treble == 0) {
                text = " TREBLE    0 ";
                textLine1Right = "HF    0";
            } else if (treble > 0) {
                text = String.format(" TREBLE  +%02d ", treble);
                textLine1Right = String.format("HF + %02d", treble);
            } else {
                text = String.format(" TREBLE  -%02d ", -treble);
                textLine1Right = String.format("HF - %02d", -treble);
            }
            if (treble == 0) {
                textAscii = KEY_TREBLE + "=000";
            } else if (treble > 0) {
                textAscii = String.format("%s=+%02d", KEY_TREBLE, treble);
            } else {
                textAscii = String.format("%s=-%02d", KEY_TREBLE, -treble);
            }
        } else if (cmd == RotelCommand.TREBLE_SET && value != null) {
            treble = value;
            if (treble == -maxToneLevel) {
                text = " TREBLE  MIN ";
                textLine1Right = "HF  MIN";
            } else if (treble == maxToneLevel) {
                text = " TREBLE  MAX ";
                textLine1Right = "HF  MAX";
            } else if (treble == 0) {
                text = " TREBLE    0 ";
                textLine1Right = "HF    0";
            } else if (treble > 0) {
                text = String.format(" TREBLE  +%02d ", treble);
                textLine1Right = String.format("HF + %02d", treble);
            } else {
                text = String.format(" TREBLE  -%02d ", -treble);
                textLine1Right = String.format("HF - %02d", -treble);
            }
            if (treble == 0) {
                textAscii = KEY_TREBLE + "=000";
            } else if (treble > 0) {
                textAscii = String.format("%s=+%02d", KEY_TREBLE, treble);
            } else {
                textAscii = String.format("%s=-%02d", KEY_TREBLE, -treble);
            }
        } else if (cmd == RotelCommand.TREBLE) {
            if (treble == 0) {
                textAscii = KEY_TREBLE + "=000";
            } else if (treble > 0) {
                textAscii = String.format("%s=+%02d", KEY_TREBLE, treble);
            } else {
                textAscii = String.format("%s=-%02d", KEY_TREBLE, -treble);
            }
        } else if (cmd == RotelCommand.TONE_CONTROL_SELECT) {
            showTreble = !showTreble;
            if (showTreble) {
                if (treble == -maxToneLevel) {
                    text = " TREBLE  MIN ";
                    textLine1Right = "HF  MIN";
                } else if (treble == maxToneLevel) {
                    text = " TREBLE  MAX ";
                    textLine1Right = "HF  MAX";
                } else if (treble == 0) {
                    text = " TREBLE    0 ";
                    textLine1Right = "HF    0";
                } else if (treble > 0) {
                    text = String.format(" TREBLE  +%02d ", treble);
                    textLine1Right = String.format("HF + %02d", treble);
                } else {
                    text = String.format(" TREBLE  -%02d ", -treble);
                    textLine1Right = String.format("HF - %02d", -treble);
                }
            } else {
                if (bass == -maxToneLevel) {
                    text = "   BASS  MIN ";
                    textLine1Right = "LF  MIN";
                } else if (bass == maxToneLevel) {
                    text = "   BASS  MAX ";
                    textLine1Right = "LF  MAX";
                } else if (bass == 0) {
                    text = "   BASS    0 ";
                    textLine1Right = "LF    0";
                } else if (bass > 0) {
                    text = String.format("   BASS  +%02d ", bass);
                    textLine1Right = String.format("LF + %02d", bass);
                } else {
                    text = String.format("   BASS  -%02d ", -bass);
                    textLine1Right = String.format("LF - %02d", -bass);
                }
            }
        } else if (cmd == RotelCommand.RECORD_FONCTION_SELECT) {
            showZone = 0;
            textLine2 = "REC " + getSourceLabel(recordSource);
        } else if (cmd == RotelCommand.ZONE_SELECT) {
            if (showZone >= getModel().getNbAdditionalZones()) {
                showZone = 0;
            }
            showZone++;
            if (showZone == 1) {
                zone = getModel().getNbAdditionalZones() > 1 ? "ZONE2 " : "ZONE ";
                text = zone + (powerZone2 ? getSourceLabel(sourceZone2) : "OFF");
                textLine2 = zone + (powerZone2 ? getSourceLabel(sourceZone2) : "OFF");
            } else if (showZone == 2) {
                zone = "ZONE3 ";
                text = zone + (powerZone3 ? getSourceLabel(sourceZone3) : "OFF");
                textLine2 = zone + (powerZone3 ? getSourceLabel(sourceZone3) : "OFF");
            } else if (showZone == 3) {
                zone = "ZONE4 ";
                text = zone + (powerZone4 ? getSourceLabel(sourceZone4) : "OFF");
                textLine2 = zone + (powerZone4 ? getSourceLabel(sourceZone4) : "OFF");
            }
        } else if (cmd == RotelCommand.PLAY) {
            playStatus = RotelPlayStatus.PLAYING;
            textAscii = String.format("%s=%s",
                    getProtocol() == RotelProtocol.ASCII_V1 ? KEY1_PLAY_STATUS : KEY2_PLAY_STATUS, PLAY);
        } else if (cmd == RotelCommand.STOP) {
            playStatus = RotelPlayStatus.STOPPED;
            textAscii = String.format("%s=%s",
                    getProtocol() == RotelProtocol.ASCII_V1 ? KEY1_PLAY_STATUS : KEY2_PLAY_STATUS, STOP);
        } else if (cmd == RotelCommand.PAUSE) {
            switch (playStatus) {
                case PLAYING:
                    playStatus = RotelPlayStatus.PAUSED;
                    break;
                case PAUSED:
                case STOPPED:
                    playStatus = RotelPlayStatus.PLAYING;
                    break;
            }
            textAscii = String.format("%s=%s",
                    getProtocol() == RotelProtocol.ASCII_V1 ? KEY1_PLAY_STATUS : KEY2_PLAY_STATUS,
                    playStatus == RotelPlayStatus.PAUSED ? PAUSE : PLAY);
        } else if (cmd == RotelCommand.CD_PLAY_STATUS || cmd == RotelCommand.PLAY_STATUS) {
            switch (playStatus) {
                case PLAYING:
                    textAscii = String.format("%s=%s",
                            getProtocol() == RotelProtocol.ASCII_V1 ? KEY1_PLAY_STATUS : KEY2_PLAY_STATUS, PLAY);
                    break;
                case PAUSED:
                    textAscii = String.format("%s=%s",
                            getProtocol() == RotelProtocol.ASCII_V1 ? KEY1_PLAY_STATUS : KEY2_PLAY_STATUS, PAUSE);
                    break;
                case STOPPED:
                    textAscii = String.format("%s=%s",
                            getProtocol() == RotelProtocol.ASCII_V1 ? KEY1_PLAY_STATUS : KEY2_PLAY_STATUS, STOP);
                    break;
            }
        } else if (cmd == RotelCommand.TRACK_FORWARD) {
            track++;
            textAscii = String.format("%s=%03d", KEY_TRACK, track);
        } else if (cmd == RotelCommand.TRACK_BACKWORD) {
            if (track > 1) {
                track--;
            }
            textAscii = String.format("%s=%03d", KEY_TRACK, track);
        } else if (cmd == RotelCommand.TRACK) {
            textAscii = String.format("%s=%03d", KEY_TRACK, track);
        } else if (cmd == RotelCommand.SOURCE_MULTI_INPUT) {
            multiinput = !multiinput;
            text = "MULTI IN " + (multiinput ? "ON" : "OFF");
            try {
                source = getModel().getSourceFromCommand(cmd);
                textLine1Left = source.getLabel();
                RotelCommand command = source.getCommand();
                textAscii = KEY_SOURCE + "=" + ((command != null) ? command.getAsciiCommandV2() : "");
                mute = false;
            } catch (RotelException e) {
            }
        } else if (cmd == RotelCommand.SOURCE) {
            RotelCommand command = source.getCommand();
            textAscii = KEY_SOURCE + "=" + ((command != null) ? command.getAsciiCommandV2() : "");
        } else if (cmd == RotelCommand.STEREO) {
            dsp = RotelDsp.CAT4_NONE;
            textLine2 = "STEREO";
            textAscii = KEY_DSP_MODE + "=" + dsp.getFeedback();
        } else if (cmd == RotelCommand.STEREO3) {
            dsp = RotelDsp.CAT4_STEREO3;
            textLine2 = "DOLBY 3 STEREO";
            textAscii = KEY_DSP_MODE + "=" + dsp.getFeedback();
        } else if (cmd == RotelCommand.STEREO5) {
            dsp = RotelDsp.CAT4_STEREO5;
            textLine2 = "5CH STEREO";
            textAscii = KEY_DSP_MODE + "=" + dsp.getFeedback();
        } else if (cmd == RotelCommand.STEREO7) {
            dsp = RotelDsp.CAT4_STEREO7;
            textLine2 = "7CH STEREO";
            textAscii = KEY_DSP_MODE + "=" + dsp.getFeedback();
        } else if (cmd == RotelCommand.STEREO9) {
            dsp = RotelDsp.CAT5_STEREO9;
            textAscii = KEY_DSP_MODE + "=" + dsp.getFeedback();
        } else if (cmd == RotelCommand.STEREO11) {
            dsp = RotelDsp.CAT5_STEREO11;
            textAscii = KEY_DSP_MODE + "=" + dsp.getFeedback();
        } else if (cmd == RotelCommand.DSP1) {
            dsp = RotelDsp.CAT4_DSP1;
            textLine2 = "DSP 1";
            textAscii = KEY_DSP_MODE + "=" + dsp.getFeedback();
        } else if (cmd == RotelCommand.DSP2) {
            dsp = RotelDsp.CAT4_DSP2;
            textLine2 = "DSP 2";
            textAscii = KEY_DSP_MODE + "=" + dsp.getFeedback();
        } else if (cmd == RotelCommand.DSP3) {
            dsp = RotelDsp.CAT4_DSP3;
            textLine2 = "DSP 3";
            textAscii = KEY_DSP_MODE + "=" + dsp.getFeedback();
        } else if (cmd == RotelCommand.DSP4) {
            dsp = RotelDsp.CAT4_DSP4;
            textLine2 = "DSP 4";
            textAscii = KEY_DSP_MODE + "=" + dsp.getFeedback();
        } else if (cmd == RotelCommand.PROLOGIC) {
            dsp = RotelDsp.CAT4_PROLOGIC;
            textLine2 = "DOLBY PRO LOGIC";
            textAscii = KEY_DSP_MODE + "=" + dsp.getFeedback();
        } else if (cmd == RotelCommand.PLII_CINEMA) {
            dsp = RotelDsp.CAT4_PLII_CINEMA;
            textLine2 = "DOLBY PL  C";
            textAscii = KEY_DSP_MODE + "=" + dsp.getFeedback();
        } else if (cmd == RotelCommand.PLII_MUSIC) {
            dsp = RotelDsp.CAT4_PLII_MUSIC;
            textLine2 = "DOLBY PL  M";
            textAscii = KEY_DSP_MODE + "=" + dsp.getFeedback();
        } else if (cmd == RotelCommand.PLII_GAME) {
            dsp = RotelDsp.CAT4_PLII_GAME;
            textLine2 = "DOLBY PL  G";
            textAscii = KEY_DSP_MODE + "=" + dsp.getFeedback();
        } else if (cmd == RotelCommand.PLIIZ) {
            dsp = RotelDsp.CAT4_PLIIZ;
            textLine2 = "DOLBY PL z";
            textAscii = KEY_DSP_MODE + "=" + dsp.getFeedback();
        } else if (cmd == RotelCommand.NEO6_MUSIC) {
            dsp = RotelDsp.CAT4_NEO6_MUSIC;
            textLine2 = "DTS Neo:6 M";
            textAscii = KEY_DSP_MODE + "=" + dsp.getFeedback();
        } else if (cmd == RotelCommand.NEO6_CINEMA) {
            dsp = RotelDsp.CAT4_NEO6_CINEMA;
            textLine2 = "DTS Neo:6 C";
            textAscii = KEY_DSP_MODE + "=" + dsp.getFeedback();
        } else if (cmd == RotelCommand.ATMOS) {
            dsp = RotelDsp.CAT5_ATMOS;
            textAscii = KEY_DSP_MODE + "=" + dsp.getFeedback();
        } else if (cmd == RotelCommand.NEURAL_X) {
            dsp = RotelDsp.CAT5_NEURAL_X;
            textAscii = KEY_DSP_MODE + "=" + dsp.getFeedback();
        } else if (cmd == RotelCommand.BYPASS) {
            dsp = RotelDsp.CAT4_BYPASS;
            textLine2 = "BYPASS";
            textAscii = KEY_DSP_MODE + "=" + dsp.getFeedback();
        } else if (cmd == RotelCommand.DSP_MODE) {
            textAscii = KEY_DSP_MODE + "=" + dsp.getFeedback();
        } else {
            try {
                source = getModel().getSourceFromCommand(cmd);
                text = source.getLabel() + "  ZZZ";
                textLine1Left = source.getLabel();
                RotelCommand command = source.getCommand();
                textAscii = KEY_SOURCE + "=" + ((command != null) ? command.getAsciiCommandV2() : "");
                mute = false;
            } catch (RotelException e) {
            }
            try {
                recordSource = getModel().getRecordSourceFromCommand(cmd);
                textLine2 = "REC " + getSourceLabel(recordSource);
            } catch (RotelException e) {
            }
            try {
                sourceZone2 = getModel().getZone2SourceFromCommand(cmd);
                powerZone2 = true;
                zone = getModel().getNbAdditionalZones() > 1 ? "ZONE2 " : "ZONE ";
                text = zone + getSourceLabel(sourceZone2);
                textLine2 = zone + getSourceLabel(sourceZone2);
                muteZone2 = false;
            } catch (RotelException e) {
            }
            try {
                sourceZone3 = getModel().getZone3SourceFromCommand(cmd);
                powerZone3 = true;
                zone = "ZONE3 ";
                text = zone + getSourceLabel(sourceZone3);
                textLine2 = zone + getSourceLabel(sourceZone3);
                muteZone3 = false;
            } catch (RotelException e) {
            }
            try {
                sourceZone4 = getModel().getZone4SourceFromCommand(cmd);
                powerZone4 = true;
                zone = "ZONE4 ";
                text = zone + getSourceLabel(sourceZone4);
                textLine2 = zone + getSourceLabel(sourceZone4);
                muteZone4 = false;
            } catch (RotelException e) {
            }
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

        if (cmd != RotelCommand.POWER_OFF && cmd != RotelCommand.MAIN_ZONE_POWER_OFF && cmd != RotelCommand.POWER_ON
                && cmd != RotelCommand.MAIN_ZONE_POWER_ON && cmd != RotelCommand.POWER
                && cmd != RotelCommand.DISPLAY_REFRESH && !power) {
            return;
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

    private String getSourceLabel(RotelSource source) {
        return source.getName().equals(RotelSource.CAT1_FOLLOW_MAIN.getName()) ? "SOURCE" : source.getLabel();
    }
}
