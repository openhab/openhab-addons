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
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.matter.internal.client.dto.cluster.ClusterCommand;

/**
 * ClosureDimension
 *
 * @author Dan Cunningham - Initial contribution
 */
public class ClosureDimensionCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0105;
    public static final String CLUSTER_NAME = "ClosureDimension";
    public static final String CLUSTER_PREFIX = "closureDimension";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_CURRENT_STATE = "currentState";
    public static final String ATTRIBUTE_TARGET_STATE = "targetState";
    public static final String ATTRIBUTE_RESOLUTION = "resolution";
    public static final String ATTRIBUTE_STEP_VALUE = "stepValue";
    public static final String ATTRIBUTE_UNIT = "unit";
    public static final String ATTRIBUTE_UNIT_RANGE = "unitRange";
    public static final String ATTRIBUTE_LIMIT_RANGE = "limitRange";
    public static final String ATTRIBUTE_TRANSLATION_DIRECTION = "translationDirection";
    public static final String ATTRIBUTE_ROTATION_AXIS = "rotationAxis";
    public static final String ATTRIBUTE_OVERFLOW = "overflow";
    public static final String ATTRIBUTE_MODULATION_TYPE = "modulationType";
    public static final String ATTRIBUTE_LATCH_CONTROL_MODES = "latchControlModes";

    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * Indicates the current Position, Latching and/or Speed, based on the feature flags set.
     * A value of null shall indicate that:
     * - The position and latching state are both not known yet because the closure is not calibrated, or
     * - The product has lost its position and latching state after manual motion during a shutdown.
     * Changes to this attribute shall only be marked as reportable in the following cases:
     * - When the attribute changes from null to any other value and vice versa, or
     * - When the Position changes from null to any other value and vice versa, or
     * - At most once every 5 seconds when the Position changes from one non-null value to another non-null value, or
     * - When TargetState.Position is reached, or
     * - When CurrentState.Speed changes, or
     * - When CurrentState.Latch changes.
     * The value of the different fields within the structure of this attribute depends on:
     * - The last SetTarget or Step commands.
     * - The impact of a MoveTo command received on the Closure Control Cluster instance associated with this cluster,
     * if such a Closure Control instance exists, as described in Section 5.5.4, "Association Between Closure Control
     * and Closure Dimension Clusters".
     */
    public DimensionStateStruct currentState; // 0 DimensionStateStruct R V
    /**
     * Indicates the target Position, Latching and/or Speed, based on the feature flags set.
     * A value of null shall indicate that the TargetState fields are unknown (typically after a reboot, no target has
     * been yet requested).
     * Each field shall be present only when its corresponding feature is supported. If the feature is not supported the
     * field shall NOT be present.
     * The value of TargetState.Position shall be set to a value that is an integer multiple of the Resolution
     * attribute.
     * The value of the different fields within the structure of this attribute depends on:
     * - The last SetTarget or Step commands.
     * - The impact of a MoveTo command received on the Closure Control Cluster instance associated with this cluster,
     * if such a Closure Control instance exists, as described in Section 5.5.4, "Association Between Closure Control
     * and Closure Dimension Clusters".
     */
    public DimensionStateStruct targetState; // 1 DimensionStateStruct R V
    /**
     * Indicates the minimal acceptable change to the Position field of the TargetState and CurrentState attributes.
     * Resolution should not be confused with an accuracy, such as Current = Target +/- Accuracy. This cluster does not
     * provide accuracy.
     * Resolution gives a collection of valid Current position points over a linear ruler.
     * > [!NOTE]
     * > NOTE: A resolution of 100% means that the associated dimension cannot be placed in an intermediate position -
     * its position is binary.
     */
    public Integer resolution; // 2 percent100ths R V
    /**
     * Indicates the size of a single step, expressed in percent100ths. When the Step command is used, each step changes
     * the position by this amount. The value of this attribute shall be an integer multiple of the Resolution
     * attribute.
     * > [!NOTE]
     * > NOTE: The StepValue should be large enough to cause a visible change in the closure's position when a Step
     * command is invoked.
     */
    public Integer stepValue; // 3 percent100ths R V
    /**
     * Indicates the unit related to the Position.
     */
    public ClosureUnitEnum unit; // 4 ClosureUnitEnum R V
    /**
     * Indicates the minimum and the maximum values expressed by Position following the unit indicated by "Unit".
     * The value of this attribute may be null if the product has not been set up.
     */
    public UnitRangeStruct unitRange; // 5 UnitRangeStruct R V
    /**
     * Indicates the range of possible values for the Position field in the CurrentState attribute.
     * This range may evolve dynamically.
     * LimitRange.Min and LimitRange.Max shall be equal to an integer multiple of the Resolution attribute.
     */
    public RangePercent100thsStruct limitRange; // 6 RangePercent100thsStruct R V
    /**
     * Indicates the direction of the translation.
     * A properly configured closure dimension SHOULD reflect as best as possible the translation as seen by the user.
     * This attribute is not supposed to change once the installation is finalized.
     */
    public TranslationDirectionEnum translationDirection; // 7 TranslationDirectionEnum R V
    /**
     * Indicates the axis of the rotation.
     * A properly configured closure dimension SHOULD reflect as best as possible the rotation axis as perceived by the
     * user. This attribute is not supposed to change once the installation is finalized.
     */
    public RotationAxisEnum rotationAxis; // 8 RotationAxisEnum R V
    /**
     * Indicates the overflow related to Rotation(RO).
     * A closure that rotates following an axis (with Rotation(RO) feature declared in FeatureMap), could overflow
     * Inside and/or Outside. If the axis is centered, one part goes Outside and the other part goes Inside. In this
     * case, this attribute shall use Top/Bottom/Left/Right Inside or Top/Bottom/Left/Right Outside enumerated value.
     */
    public OverflowEnum overflow; // 9 OverflowEnum R V
    /**
     * Indicates the modulation type related to Modulation(MD).
     * The server SHOULD reflect, as best as possible, the modulation type as perceived by the user. This attribute is
     * not supposed to change once the installation is finalized.
     */
    public ModulationTypeEnum modulationType; // 10 ModulationTypeEnum R V
    /**
     * This attribute shall specify whether the latch mechanism can be latched or unlatched remotely.
     */
    public LatchControlModesBitmap latchControlModes; // 11 LatchControlModesBitmap R V

    // Structs
    public static class RangePercent100thsStruct {
        public Integer min; // percent100ths
        public Integer max; // percent100ths

        public RangePercent100thsStruct(Integer min, Integer max) {
            this.min = min;
            this.max = max;
        }
    }

    public static class UnitRangeStruct {
        public Integer min; // int16
        public Integer max; // int16

        public UnitRangeStruct(Integer min, Integer max) {
            this.min = min;
            this.max = max;
        }
    }

    public static class DimensionStateStruct {
        /**
         * This field shall indicate the position of the closure, expressed as a percentage from 0.00% to 100.00%.
         * A null value indicates that the position is not known or is not set.
         */
        public Integer position; // percent100ths
        /**
         * This field shall indicate the latching state of the closure, as defined in the OverallCurrentStateStruct
         * Latch Field.
         * A null value indicates that the latch is not known or is not set.
         */
        public Boolean latch; // bool
        /**
         * This field shall indicate the current speed of the closure, as defined in the ThreeLevelAutoEnum.
         * If no speed value has yet been set, the server shall select and set one of the speed values defined in the
         * ThreeLevelAutoEnum.
         */
        public ThreeLevelAutoEnum speed; // ThreeLevelAutoEnum

        public DimensionStateStruct(Integer position, Boolean latch, ThreeLevelAutoEnum speed) {
            this.position = position;
            this.latch = latch;
            this.speed = speed;
        }
    }

    // Enums
    /**
     * Legend: !legendOpen Open !legendClosed Closed
     */
    public enum TranslationDirectionEnum implements MatterEnum {
        DOWNWARD(0, "Downward"),
        UPWARD(1, "Upward"),
        VERTICAL_MASK(2, "Vertical Mask"),
        VERTICAL_SYMMETRY(3, "Vertical Symmetry"),
        LEFTWARD(4, "Leftward"),
        RIGHTWARD(5, "Rightward"),
        HORIZONTAL_MASK(6, "Horizontal Mask"),
        HORIZONTAL_SYMMETRY(7, "Horizontal Symmetry"),
        FORWARD(8, "Forward"),
        BACKWARD(9, "Backward"),
        DEPTH_MASK(10, "Depth Mask"),
        DEPTH_SYMMETRY(11, "Depth Symmetry");

        private final Integer value;
        private final String label;

        private TranslationDirectionEnum(Integer value, String label) {
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

    /**
     * Legend: !legendOpen Open !legendClosed Closed
     */
    public enum RotationAxisEnum implements MatterEnum {
        LEFT(0, "Left"),
        CENTERED_VERTICAL(1, "Centered Vertical"),
        LEFT_AND_RIGHT(2, "Left And Right"),
        RIGHT(3, "Right"),
        TOP(4, "Top"),
        CENTERED_HORIZONTAL(5, "Centered Horizontal"),
        TOP_AND_BOTTOM(6, "Top And Bottom"),
        BOTTOM(7, "Bottom"),
        LEFT_BARRIER(8, "Left Barrier"),
        LEFT_AND_RIGHT_BARRIERS(9, "Left And Right Barriers"),
        RIGHT_BARRIER(10, "Right Barrier");

        private final Integer value;
        private final String label;

        private RotationAxisEnum(Integer value, String label) {
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

    public enum OverflowEnum implements MatterEnum {
        NO_OVERFLOW(0, "No Overflow"),
        INSIDE(1, "Inside"),
        OUTSIDE(2, "Outside"),
        TOP_INSIDE(3, "Top Inside"),
        TOP_OUTSIDE(4, "Top Outside"),
        BOTTOM_INSIDE(5, "Bottom Inside"),
        BOTTOM_OUTSIDE(6, "Bottom Outside"),
        LEFT_INSIDE(7, "Left Inside"),
        LEFT_OUTSIDE(8, "Left Outside"),
        RIGHT_INSIDE(9, "Right Inside"),
        RIGHT_OUTSIDE(10, "Right Outside");

        private final Integer value;
        private final String label;

        private OverflowEnum(Integer value, String label) {
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

    public enum ModulationTypeEnum implements MatterEnum {
        SLATS_ORIENTATION(0, "Slats Orientation"),
        SLATS_OPENWORK(1, "Slats Openwork"),
        STRIPES_ALIGNMENT(2, "Stripes Alignment"),
        OPACITY(3, "Opacity"),
        VENTILATION(4, "Ventilation");

        private final Integer value;
        private final String label;

        private ModulationTypeEnum(Integer value, String label) {
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

    public enum ClosureUnitEnum implements MatterEnum {
        MILLIMETER(0, "Millimeter"),
        DEGREE(1, "Degree");

        private final Integer value;
        private final String label;

        private ClosureUnitEnum(Integer value, String label) {
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

    /**
     * This data type is derived from enum8 and used for the Step command to indicate the direction of the steps.
     */
    public enum StepDirectionEnum implements MatterEnum {
        DECREASE(0, "Decrease"),
        INCREASE(1, "Increase");

        private final Integer value;
        private final String label;

        private StepDirectionEnum(Integer value, String label) {
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
    public static class LatchControlModesBitmap {
        /**
         * Remote latching capability
         * This bit shall indicate whether the latch supports remote latching or not:
         * - 0 = the latch can only be latched through manual, physical operation.
         * - 1 = the latch can be latched via remote control (e.g., electronic or remote actuation).
         */
        public boolean remoteLatching;
        /**
         * Remote unlatching capability
         * This bit shall indicate whether the latch supports remote unlatching or not:
         * - 0 = the latch can only be unlatched through manual, physical operation.
         * - 1 = the latch can be unlatched via remote control (e.g., electronic or remote actuation).
         */
        public boolean remoteUnlatching;

        public LatchControlModesBitmap(boolean remoteLatching, boolean remoteUnlatching) {
            this.remoteLatching = remoteLatching;
            this.remoteUnlatching = remoteUnlatching;
        }
    }

    public static class FeatureMap {
        /**
         * 
         * This feature shall indicate support for position percentage over the range of 0.00% to 100.00%, with a
         * resolution of 0.01%.
         * > [!NOTE]
         * > NOTE: In most of the products 0.00% = fully opened and 100.00% = fully closed but this is not always the
         * case. For example, if the Modulation feature is supported and the ModulationType is SlatsOrientation or
         * StripesAlignment, the panel can be fully closed at 0.00% and 100.00%, and be fully opened at 50.00%.
         */
        public boolean positioning;
        /**
         * 
         * This feature shall indicate that the closure can be latched and unlatched. An axis with the MotionLatching
         * feature has capabilities that will prevent actuators from moving along parts of that axis if the dimension is
         * latched.
         */
        public boolean motionLatching;
        /**
         * 
         * This feature shall indicate additional information about the closure dimension's possible range of movement.
         */
        public boolean unit;
        /**
         * 
         * This feature shall indicate that the closure dimension supports degradation of its functioning. Reachable
         * Position range may be limited compared to full scope of a nominal behavior.
         */
        public boolean limitation;
        /**
         * 
         * This feature shall indicate that the closure dimension can be driven at different speed levels: Low, Medium,
         * and High. Please refer to Section 5.4.5.4, "Speed Feature" for more details.
         * > [!NOTE]
         * > NOTE: The server might not support three different speed values. The manufacturer shall select speed values
         * linked to Low, Medium and High such that Low $<=$ Medium $<=$ High.
         */
        public boolean speed;
        /**
         * 
         * This feature shall indicate that the panel can move along a single axis. The possible directions include
         * downward, upward, leftward, rightward, forward, and backward. The Translation feature is used to control the
         * position of the panel along the specified axis.
         */
        public boolean translation;
        /**
         * 
         * This feature shall indicate that the panel can rotate around a single axis. The possible axes include left,
         * right, top, bottom, centered vertical, and centered horizontal. The Rotation feature is used to control the
         * orientation of the panel around the specified axis.
         */
        public boolean rotation;
        /**
         * 
         * This feature shall indicate that the panel can modify its aspect to control a particular flow, such as light,
         * air, or privacy. The possible modulation types include slats orientation, slats openwork, stripes alignment,
         * opacity, and ventilation. The Modulation feature is used to adjust the panel's properties to achieve the
         * desired effect.
         */
        public boolean modulation;

        public FeatureMap(boolean positioning, boolean motionLatching, boolean unit, boolean limitation, boolean speed,
                boolean translation, boolean rotation, boolean modulation) {
            this.positioning = positioning;
            this.motionLatching = motionLatching;
            this.unit = unit;
            this.limitation = limitation;
            this.speed = speed;
            this.translation = translation;
            this.rotation = rotation;
            this.modulation = modulation;
        }
    }

    public ClosureDimensionCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 261, "ClosureDimension");
    }

    protected ClosureDimensionCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * This command is used to move a dimension of the closure to a target position.
     * The rationale behind defining the conformance as being purely optional in the table above is to ensure that
     * commands containing one or more fields related to unsupported features are still accepted, rather than being
     * rejected outright. For example, if a simple client, which is not able to determine the capabilities of the
     * server, invokes a command that includes both position and speed, a server that does not support the speed feature
     * would simply ignore the speed field while still adjusting its position as requested.
     */
    public static ClusterCommand setTarget(Integer position, Boolean latch, ThreeLevelAutoEnum speed) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (position != null) {
            map.put("position", position);
        }
        if (latch != null) {
            map.put("latch", latch);
        }
        if (speed != null) {
            map.put("speed", speed);
        }
        return new ClusterCommand("setTarget", map);
    }

    /**
     * This command is used to move a dimension of the closure to a target position by a number of steps.
     */
    public static ClusterCommand step(StepDirectionEnum direction, Integer numberOfSteps, ThreeLevelAutoEnum speed) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (direction != null) {
            map.put("direction", direction);
        }
        if (numberOfSteps != null) {
            map.put("numberOfSteps", numberOfSteps);
        }
        if (speed != null) {
            map.put("speed", speed);
        }
        return new ClusterCommand("step", map);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "featureMap : " + featureMap + "\n";
        str += "currentState : " + currentState + "\n";
        str += "targetState : " + targetState + "\n";
        str += "resolution : " + resolution + "\n";
        str += "stepValue : " + stepValue + "\n";
        str += "unit : " + unit + "\n";
        str += "unitRange : " + unitRange + "\n";
        str += "limitRange : " + limitRange + "\n";
        str += "translationDirection : " + translationDirection + "\n";
        str += "rotationAxis : " + rotationAxis + "\n";
        str += "overflow : " + overflow + "\n";
        str += "modulationType : " + modulationType + "\n";
        str += "latchControlModes : " + latchControlModes + "\n";
        return str;
    }
}
