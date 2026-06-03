/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.matter.internal.client.dto.cluster.ClusterCommand;

/**
 * CameraAvSettingsUserLevelManagement
 *
 * @author Dan Cunningham - Initial contribution
 */
public class CameraAvSettingsUserLevelManagementCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0552;
    public static final String CLUSTER_NAME = "CameraAvSettingsUserLevelManagement";
    public static final String CLUSTER_PREFIX = "cameraAvSettingsUserLevelManagement";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_MPTZ_POSITION = "mptzPosition";
    public static final String ATTRIBUTE_MAX_PRESETS = "maxPresets";
    public static final String ATTRIBUTE_MPTZ_PRESETS = "mptzPresets";
    public static final String ATTRIBUTE_DPTZ_STREAMS = "dptzStreams";
    public static final String ATTRIBUTE_ZOOM_MAX = "zoomMax";
    public static final String ATTRIBUTE_TILT_MIN = "tiltMin";
    public static final String ATTRIBUTE_TILT_MAX = "tiltMax";
    public static final String ATTRIBUTE_PAN_MIN = "panMin";
    public static final String ATTRIBUTE_PAN_MAX = "panMax";
    public static final String ATTRIBUTE_MOVEMENT_STATE = "movementState";

    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * This attribute indicates the currently selected mechanical pan, tilt, and zoom position.
     */
    public MPTZStruct mptzPosition; // 0 MPTZStruct R V
    /**
     * This attribute indicates the maximum number of presets for the mechanical pan, tilt, zoom.
     */
    public Integer maxPresets; // 1 uint8 R V
    /**
     * This attribute shall be a list of MPTZPresetStruct. Each entry in the list contains a preset for mechanical pan,
     * tilt, and/or zoom, the values for which are represented by an instance of an MPTZStruct.
     */
    public List<MPTZPresetStruct> mptzPresets; // 2 list R V
    /**
     * This attribute is a list of DPTZStruct. If a video stream is listed, it means digital movement is supported via
     * DPTZSetViewport or DPTZRelativeMove. The initial values for each Viewport entry shall be the values found in the
     * global Viewport.
     */
    public List<DPTZStruct> dptzStreams; // 3 list R V
    /**
     * Indicates the maximum value for the mechanical zoom specified by the camera manufacturer that allows for
     * increments of 1 to be noticeable. The handling of this value is implementation specific.
     */
    public Integer zoomMax; // 4 uint8 R V
    /**
     * Indicates the minimum value for the mechanical tilt specified by the camera manufacturer in angular degrees.
     */
    public Integer tiltMin; // 5 int16 R V
    /**
     * Indicates the maximum value for the mechanical tilt specified by the camera manufacturer in angular degrees.
     */
    public Integer tiltMax; // 6 int16 R V
    /**
     * Indicates the minimum value for the mechanical pan specified by the camera manufacturer in angular degrees.
     */
    public Integer panMin; // 7 int16 R V
    /**
     * Indicates the maximum value for the mechanical pan specified by the camera manufacturer in angular degrees.
     */
    public Integer panMax; // 8 int16 R V
    /**
     * Indicates the current movement state of the camera.
     */
    public PhysicalMovementEnum movementState; // 9 PhysicalMovementEnum R V

    // Structs
    /**
     * This type is used to indicate support for the per stream digital pan, tilt, and zoom values.
     */
    public static class DPTZStruct {
        /**
         * This field shall indicate the video stream this applies too.
         */
        public Integer videoStreamId; // CameraAvStreamManagement.VideoStreamID
        /**
         * This field shall indicate the per stream viewport applied to this video stream. See Viewport for details on
         * the coordinate system.
         */
        public ViewportStruct viewport; // ViewportStruct

        public DPTZStruct(Integer videoStreamId, ViewportStruct viewport) {
            this.videoStreamId = videoStreamId;
            this.viewport = viewport;
        }
    }

    /**
     * This type is used to indicate the mechanical pan, tilt, and zoom values.
     */
    public static class MPTZStruct {
        /**
         * This field shall indicate the mechanical pan value in angular degrees of angle. A zero value shall indicate
         * the home position horizontal reference for the direction of view of the camera. A negative value shall
         * indicate a leftward rotation of the camera about the vertical axis of the camera coordinate system. A
         * positive value shall indicate a rightward rotation of the camera about the vertical axis of the camera
         * coordinate system.
         */
        public Integer pan; // int16
        /**
         * This field shall indicate the mechanical tilt value in angular degrees of angle. A zero value shall indicate
         * a vertical reference for the direction of view of the camera. A negative value shall indicate a downward
         * rotation of the camera about the horizontal axis of the camera coordinate system. A positive value shall
         * indicate an upward rotation of the camera about the horizontal axis of the camera coordinate system.
         */
        public Integer tilt; // int16
        /**
         * This field shall indicate the zoom value to use. A value of 1 shall indicate the widest possible optical
         * field of view. A value of ZoomMax shall indicate the narrowest possible field of optical view.
         */
        public Integer zoom; // uint8

        public MPTZStruct(Integer pan, Integer tilt, Integer zoom) {
            this.pan = pan;
            this.tilt = tilt;
            this.zoom = zoom;
        }
    }

    /**
     * This type is used to save a preset location for mechanical pan, tilt and zoom.
     */
    public static class MPTZPresetStruct {
        /**
         * This shall be derived from uint8 and represents the ID for a saved set of preset values for mechanical pan,
         * tilt and zoom.
         */
        public Integer presetId; // uint8
        /**
         * The shall be a string representing the name of the Preset.
         */
        public String name; // string
        /**
         * This shall hold the mechanical pan, tilt and zoom values.
         */
        public MPTZStruct settings; // MPTZStruct

        public MPTZPresetStruct(Integer presetId, String name, MPTZStruct settings) {
            this.presetId = presetId;
            this.name = name;
            this.settings = settings;
        }
    }

    // Enums
    /**
     * The PhysicalMovementEnum provides an enumeration of the possible physical movement states in which the camera
     * could be.
     */
    public enum PhysicalMovementEnum implements MatterEnum {
        IDLE(0, "Idle"),
        MOVING(1, "Moving");

        private final Integer value;
        private final String label;

        private PhysicalMovementEnum(Integer value, String label) {
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
         * This feature indicates that per video stream digital pan, tilt, and zoom is supported.
         */
        public boolean digitalPtz;
        /**
         * 
         * This feature indicates that mechanical pan is supported on the camera.
         */
        public boolean mechanicalPan;
        /**
         * 
         * This feature indicates that mechanical tilt is supported on the camera.
         */
        public boolean mechanicalTilt;
        /**
         * 
         * This feature indicates that mechanical zoom is supported on the camera.
         */
        public boolean mechanicalZoom;
        /**
         * 
         * This feature indicates that the storage of presets is supported on the camera.
         */
        public boolean mechanicalPresets;

        public FeatureMap(boolean digitalPtz, boolean mechanicalPan, boolean mechanicalTilt, boolean mechanicalZoom,
                boolean mechanicalPresets) {
            this.digitalPtz = digitalPtz;
            this.mechanicalPan = mechanicalPan;
            this.mechanicalTilt = mechanicalTilt;
            this.mechanicalZoom = mechanicalZoom;
            this.mechanicalPresets = mechanicalPresets;
        }
    }

    public CameraAvSettingsUserLevelManagementCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 1362, "CameraAvSettingsUserLevelManagement");
    }

    protected CameraAvSettingsUserLevelManagementCluster(BigInteger nodeId, int endpointId, int clusterId,
            String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * This command shall move the camera to the provided values for pan, tilt, and zoom in the mechanical PTZ.
     */
    public static ClusterCommand mptzSetPosition(Integer pan, Integer tilt, Integer zoom) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (pan != null) {
            map.put("pan", pan);
        }
        if (tilt != null) {
            map.put("tilt", tilt);
        }
        if (zoom != null) {
            map.put("zoom", zoom);
        }
        return new ClusterCommand("mptzSetPosition", map);
    }

    /**
     * This command shall move the camera by the delta values relative to the currently defined position.
     */
    public static ClusterCommand mptzRelativeMove(Integer panDelta, Integer tiltDelta, Integer zoomDelta) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (panDelta != null) {
            map.put("panDelta", panDelta);
        }
        if (tiltDelta != null) {
            map.put("tiltDelta", tiltDelta);
        }
        if (zoomDelta != null) {
            map.put("zoomDelta", zoomDelta);
        }
        return new ClusterCommand("mptzRelativeMove", map);
    }

    /**
     * This command shall move the camera to the positions specified by the Preset passed.
     */
    public static ClusterCommand mptzMoveToPreset(Integer presetId) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (presetId != null) {
            map.put("presetId", presetId);
        }
        return new ClusterCommand("mptzMoveToPreset", map);
    }

    /**
     * This command allows creating a new preset or updating the values of an existing one.
     */
    public static ClusterCommand mptzSavePreset(Integer presetId, String name) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (presetId != null) {
            map.put("presetId", presetId);
        }
        if (name != null) {
            map.put("name", name);
        }
        return new ClusterCommand("mptzSavePreset", map);
    }

    /**
     * This command shall remove a preset entry from the PresetMptzTable.
     */
    public static ClusterCommand mptzRemovePreset(Integer presetId) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (presetId != null) {
            map.put("presetId", presetId);
        }
        return new ClusterCommand("mptzRemovePreset", map);
    }

    /**
     * This command allows for setting the digital viewport for a specific Video Stream. This command is a per-stream
     * version of the Viewport Attribute.
     */
    public static ClusterCommand dptzSetViewport(Integer videoStreamId, ViewportStruct viewport) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (videoStreamId != null) {
            map.put("videoStreamId", videoStreamId);
        }
        if (viewport != null) {
            map.put("viewport", viewport);
        }
        return new ClusterCommand("dptzSetViewport", map);
    }

    /**
     * This command shall change the per stream viewport by the amount specified in a relative fashion. This allows for
     * multiple users to interact with a directional arrow based user interface. It is recommended to increment or
     * decrement the values by 10% of the SensorWidth and SensorHeight found in VideoSensorParams.
     */
    public static ClusterCommand dptzRelativeMove(Integer videoStreamId, Integer deltaX, Integer deltaY,
            Integer zoomDelta) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (videoStreamId != null) {
            map.put("videoStreamId", videoStreamId);
        }
        if (deltaX != null) {
            map.put("deltaX", deltaX);
        }
        if (deltaY != null) {
            map.put("deltaY", deltaY);
        }
        if (zoomDelta != null) {
            map.put("zoomDelta", zoomDelta);
        }
        return new ClusterCommand("dptzRelativeMove", map);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "featureMap : " + featureMap + "\n";
        str += "mptzPosition : " + mptzPosition + "\n";
        str += "maxPresets : " + maxPresets + "\n";
        str += "mptzPresets : " + mptzPresets + "\n";
        str += "dptzStreams : " + dptzStreams + "\n";
        str += "zoomMax : " + zoomMax + "\n";
        str += "tiltMin : " + tiltMin + "\n";
        str += "tiltMax : " + tiltMax + "\n";
        str += "panMin : " + panMin + "\n";
        str += "panMax : " + panMax + "\n";
        str += "movementState : " + movementState + "\n";
        return str;
    }
}
