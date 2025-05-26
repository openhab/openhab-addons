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
        UNSUPPORTED_KEY(1, "Unsupported Key"),
        INVALID_KEY_IN_CURRENT_STATE(2, "Invalid Key In Current State");

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
        RIGHT_UP(5, "Right Up"),
        RIGHT_DOWN(6, "Right Down"),
        LEFT_UP(7, "Left Up"),
        LEFT_DOWN(8, "Left Down"),
        ROOT_MENU(9, "Root Menu"),
        SETUP_MENU(10, "Setup Menu"),
        CONTENTS_MENU(11, "Contents Menu"),
        FAVORITE_MENU(12, "Favorite Menu"),
        EXIT(13, "Exit"),
        MEDIA_TOP_MENU(16, "Media Top Menu"),
        MEDIA_CONTEXT_SENSITIVE_MENU(17, "Media Context Sensitive Menu"),
        NUMBER_ENTRY_MODE(29, "Number Entry Mode"),
        NUMBER11(30, "Number 11"),
        NUMBER12(31, "Number 12"),
        NUMBER0OR_NUMBER10(32, "Number 0 Or Number 10"),
        NUMBERS1(33, "Numbers 1"),
        NUMBERS2(34, "Numbers 2"),
        NUMBERS3(35, "Numbers 3"),
        NUMBERS4(36, "Numbers 4"),
        NUMBERS5(37, "Numbers 5"),
        NUMBERS6(38, "Numbers 6"),
        NUMBERS7(39, "Numbers 7"),
        NUMBERS8(40, "Numbers 8"),
        NUMBERS9(41, "Numbers 9"),
        DOT(42, "Dot"),
        ENTER(43, "Enter"),
        CLEAR(44, "Clear"),
        NEXT_FAVORITE(47, "Next Favorite"),
        CHANNEL_UP(48, "Channel Up"),
        CHANNEL_DOWN(49, "Channel Down"),
        PREVIOUS_CHANNEL(50, "Previous Channel"),
        SOUND_SELECT(51, "Sound Select"),
        INPUT_SELECT(52, "Input Select"),
        DISPLAY_INFORMATION(53, "Display Information"),
        HELP(54, "Help"),
        PAGE_UP(55, "Page Up"),
        PAGE_DOWN(56, "Page Down"),
        POWER(64, "Power"),
        VOLUME_UP(65, "Volume Up"),
        VOLUME_DOWN(66, "Volume Down"),
        MUTE(67, "Mute"),
        PLAY(68, "Play"),
        STOP(69, "Stop"),
        PAUSE(70, "Pause"),
        RECORD(71, "Record"),
        REWIND(72, "Rewind"),
        FAST_FORWARD(73, "Fast Forward"),
        EJECT(74, "Eject"),
        FORWARD(75, "Forward"),
        BACKWARD(76, "Backward"),
        STOP_RECORD(77, "Stop Record"),
        PAUSE_RECORD(78, "Pause Record"),
        ANGLE(80, "Angle"),
        SUB_PICTURE(81, "Sub Picture"),
        VIDEO_ON_DEMAND(82, "Video On Demand"),
        ELECTRONIC_PROGRAM_GUIDE(83, "Electronic Program Guide"),
        TIMER_PROGRAMMING(84, "Timer Programming"),
        INITIAL_CONFIGURATION(85, "Initial Configuration"),
        SELECT_BROADCAST_TYPE(86, "Select Broadcast Type"),
        SELECT_SOUND_PRESENTATION(87, "Select Sound Presentation"),
        PLAY_FUNCTION(96, "Play Function"),
        PAUSE_PLAY_FUNCTION(97, "Pause Play Function"),
        RECORD_FUNCTION(98, "Record Function"),
        PAUSE_RECORD_FUNCTION(99, "Pause Record Function"),
        STOP_FUNCTION(100, "Stop Function"),
        MUTE_FUNCTION(101, "Mute Function"),
        RESTORE_VOLUME_FUNCTION(102, "Restore Volume Function"),
        TUNE_FUNCTION(103, "Tune Function"),
        SELECT_MEDIA_FUNCTION(104, "Select Media Function"),
        SELECT_AV_INPUT_FUNCTION(105, "Select Av Input Function"),
        SELECT_AUDIO_INPUT_FUNCTION(106, "Select Audio Input Function"),
        POWER_TOGGLE_FUNCTION(107, "Power Toggle Function"),
        POWER_OFF_FUNCTION(108, "Power Off Function"),
        POWER_ON_FUNCTION(109, "Power On Function"),
        F1BLUE(113, "F 1 Blue"),
        F2RED(114, "F 2 Red"),
        F3GREEN(115, "F 3 Green"),
        F4YELLOW(116, "F 4 Yellow"),
        F5(117, "F 5"),
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
         * 
         * Supports UP, DOWN, LEFT, RIGHT, SELECT, BACK, EXIT, MENU
         */
        public boolean navigationKeyCodes;
        /**
         * 
         * Supports CEC keys 0x0A (Settings) and 0x09 (Home)
         */
        public boolean locationKeys;
        /**
         * 
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

    protected KeypadInputCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
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
