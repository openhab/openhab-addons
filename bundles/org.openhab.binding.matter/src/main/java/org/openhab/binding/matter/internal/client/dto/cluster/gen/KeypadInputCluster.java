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

// AUTO-GENERATED, DO NOT EDIT!

package org.openhab.binding.matter.internal.client.dto.cluster.gen;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.matter.internal.client.dto.cluster.ClusterCommand;

/**
 * KeypadInput
 *
 * @author Dan Cunningham - Initial contribution
 */
public class KeypadInputCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0509;
    public static final String CLUSTER_NAME = "KeypadInput";
    public static final String CLUSTER_PREFIX = "keypadInput";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";

    public Integer clusterRevision; // 65533 ClusterRevision
    public FeatureMap featureMap; // 65532 FeatureMap

    // Enums
    public enum StatusEnum implements MatterEnum {
        SUCCESS(0, "Success"),
        UNSUPPORTED_KEY(1, "UnsupportedKey"),
        INVALID_KEY_IN_CURRENT_STATE(2, "InvalidKeyInCurrentState");

        public final Integer value;
        public final String label;

        private StatusEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum CecKeyCodeEnum implements MatterEnum {
        SELECT(0, "Select"),
        UP(1, "Up"),
        DOWN(2, "Down"),
        LEFT(3, "Left"),
        RIGHT(4, "Right"),
        RIGHT_UP(5, "RightUp"),
        RIGHT_DOWN(6, "RightDown"),
        LEFT_UP(7, "LeftUp"),
        LEFT_DOWN(8, "LeftDown"),
        ROOT_MENU(9, "RootMenu"),
        SETUP_MENU(10, "SetupMenu"),
        CONTENTS_MENU(11, "ContentsMenu"),
        FAVORITE_MENU(12, "FavoriteMenu"),
        EXIT(13, "Exit"),
        MEDIA_TOP_MENU(16, "MediaTopMenu"),
        MEDIA_CONTEXT_SENSITIVE_MENU(17, "MediaContextSensitiveMenu"),
        NUMBER_ENTRY_MODE(29, "NumberEntryMode"),
        NUMBER11(30, "Number11"),
        NUMBER12(31, "Number12"),
        NUMBER0OR_NUMBER10(32, "Number0OrNumber10"),
        NUMBERS1(33, "Numbers1"),
        NUMBERS2(34, "Numbers2"),
        NUMBERS3(35, "Numbers3"),
        NUMBERS4(36, "Numbers4"),
        NUMBERS5(37, "Numbers5"),
        NUMBERS6(38, "Numbers6"),
        NUMBERS7(39, "Numbers7"),
        NUMBERS8(40, "Numbers8"),
        NUMBERS9(41, "Numbers9"),
        DOT(42, "Dot"),
        ENTER(43, "Enter"),
        CLEAR(44, "Clear"),
        NEXT_FAVORITE(47, "NextFavorite"),
        CHANNEL_UP(48, "ChannelUp"),
        CHANNEL_DOWN(49, "ChannelDown"),
        PREVIOUS_CHANNEL(50, "PreviousChannel"),
        SOUND_SELECT(51, "SoundSelect"),
        INPUT_SELECT(52, "InputSelect"),
        DISPLAY_INFORMATION(53, "DisplayInformation"),
        HELP(54, "Help"),
        PAGE_UP(55, "PageUp"),
        PAGE_DOWN(56, "PageDown"),
        POWER(64, "Power"),
        VOLUME_UP(65, "VolumeUp"),
        VOLUME_DOWN(66, "VolumeDown"),
        MUTE(67, "Mute"),
        PLAY(68, "Play"),
        STOP(69, "Stop"),
        PAUSE(70, "Pause"),
        RECORD(71, "Record"),
        REWIND(72, "Rewind"),
        FAST_FORWARD(73, "FastForward"),
        EJECT(74, "Eject"),
        FORWARD(75, "Forward"),
        BACKWARD(76, "Backward"),
        STOP_RECORD(77, "StopRecord"),
        PAUSE_RECORD(78, "PauseRecord"),
        ANGLE(80, "Angle"),
        SUB_PICTURE(81, "SubPicture"),
        VIDEO_ON_DEMAND(82, "VideoOnDemand"),
        ELECTRONIC_PROGRAM_GUIDE(83, "ElectronicProgramGuide"),
        TIMER_PROGRAMMING(84, "TimerProgramming"),
        INITIAL_CONFIGURATION(85, "InitialConfiguration"),
        SELECT_BROADCAST_TYPE(86, "SelectBroadcastType"),
        SELECT_SOUND_PRESENTATION(87, "SelectSoundPresentation"),
        PLAY_FUNCTION(96, "PlayFunction"),
        PAUSE_PLAY_FUNCTION(97, "PausePlayFunction"),
        RECORD_FUNCTION(98, "RecordFunction"),
        PAUSE_RECORD_FUNCTION(99, "PauseRecordFunction"),
        STOP_FUNCTION(100, "StopFunction"),
        MUTE_FUNCTION(101, "MuteFunction"),
        RESTORE_VOLUME_FUNCTION(102, "RestoreVolumeFunction"),
        TUNE_FUNCTION(103, "TuneFunction"),
        SELECT_MEDIA_FUNCTION(104, "SelectMediaFunction"),
        SELECT_AV_INPUT_FUNCTION(105, "SelectAvInputFunction"),
        SELECT_AUDIO_INPUT_FUNCTION(106, "SelectAudioInputFunction"),
        POWER_TOGGLE_FUNCTION(107, "PowerToggleFunction"),
        POWER_OFF_FUNCTION(108, "PowerOffFunction"),
        POWER_ON_FUNCTION(109, "PowerOnFunction"),
        F1BLUE(113, "F1Blue"),
        F2RED(114, "F2Red"),
        F3GREEN(115, "F3Green"),
        F4YELLOW(116, "F4Yellow"),
        F5(117, "F5"),
        DATA(118, "Data");

        public final Integer value;
        public final String label;

        private CecKeyCodeEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    // Bitmaps
    public static class FeatureMap {
        /**
         * NavigationKeyCodes
         * Supports UP, DOWN, LEFT, RIGHT, SELECT, BACK, EXIT, MENU
         */
        public boolean navigationKeyCodes;
        /**
         * LocationKeys
         * Supports CEC keys 0x0A (Settings) and 0x09 (Home)
         */
        public boolean locationKeys;
        /**
         * NumberKeys
         * Supports numeric input 0..9
         */
        public boolean numberKeys;

        public FeatureMap(boolean navigationKeyCodes, boolean locationKeys, boolean numberKeys) {
            this.navigationKeyCodes = navigationKeyCodes;
            this.locationKeys = locationKeys;
            this.numberKeys = numberKeys;
        }
    }

    public KeypadInputCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 1289, "KeypadInput");
    }

    // commands
    /**
     * Upon receipt, this shall process a keycode as input to the media endpoint.
     * If a device has multiple media endpoints implementing this cluster, such as a casting video player endpoint with
     * one or more content app endpoints, then only the endpoint receiving the command shall process the keycode as
     * input. In other words, a specific content app endpoint shall NOT process a keycode received by a different
     * content app endpoint.
     * If a second SendKey request with the same KeyCode value is received within 200 ms, then the endpoint will
     * consider the first key press to be a press and hold. When such a repeat KeyCode value is not received within 200
     * ms, then the endpoint will consider the last key press to be a release.
     */
    public static ClusterCommand sendKey(CecKeyCodeEnum keyCode) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (keyCode != null) {
            map.put("keyCode", keyCode);
        }
        return new ClusterCommand("sendKey", map);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "featureMap : " + featureMap + "\n";
        return str;
    }
}
