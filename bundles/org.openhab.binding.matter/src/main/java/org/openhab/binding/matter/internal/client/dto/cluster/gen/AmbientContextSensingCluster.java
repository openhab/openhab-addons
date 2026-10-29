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
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

/**
 * AmbientContextSensing
 *
 * @author Dan Cunningham - Initial contribution
 */
public class AmbientContextSensingCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0431;
    public static final String CLUSTER_NAME = "AmbientContextSensing";
    public static final String CLUSTER_PREFIX = "ambientContextSensing";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_HUMAN_ACTIVITY_DETECTED = "humanActivityDetected";
    public static final String ATTRIBUTE_OBJECT_IDENTIFIED = "objectIdentified";
    public static final String ATTRIBUTE_AUDIO_CONTEXT_DETECTED = "audioContextDetected";
    public static final String ATTRIBUTE_AMBIENT_CONTEXT_TYPE = "ambientContextType";
    public static final String ATTRIBUTE_AMBIENT_CONTEXT_TYPE_SUPPORTED = "ambientContextTypeSupported";
    public static final String ATTRIBUTE_OBJECT_COUNT_THRESHOLD_REACHED = "objectCountThresholdReached";
    public static final String ATTRIBUTE_OBJECT_COUNT_CONFIG = "objectCountConfig";
    public static final String ATTRIBUTE_OBJECT_COUNT = "objectCount";
    public static final String ATTRIBUTE_SIMULTANEOUS_DETECTION_LIMIT = "simultaneousDetectionLimit";
    public static final String ATTRIBUTE_HOLD_TIME = "holdTime";
    public static final String ATTRIBUTE_HOLD_TIME_LIMITS = "holdTimeLimits";
    public static final String ATTRIBUTE_PREDICTED_ACTIVITY = "predictedActivity";

    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * Indicates the human activity detection in Boolean data. The detected human activity type can be found from the
     * AmbientContextType attribute.
     */
    public Boolean humanActivityDetected; // 0 bool R V
    /**
     * Indicates the occurrence of object identification in Boolean data. The identified object information can be found
     * from the AmbientContextType attribute.
     */
    public Boolean objectIdentified; // 1 bool R V
    /**
     * Indicates the ambient audio context detection in Boolean data. The detected audio context type can be found from
     * the AmbientContextType attribute.
     */
    public Boolean audioContextDetected; // 2 bool R V
    /**
     * Indicates the details for the currently observed and detected ambient context. This attribute supports multiple
     * simultaneous ambient context detections. The attribute expression rule is defined in the
     * MultipleAmbientSensingDetection section. The total number of simultaneous ambient context detections is
     * constrained by the SimultaneousDetectionLimit attribute.
     */
    public List<AmbientContextTypeStruct> ambientContextType; // 3 list R V
    /**
     * Indicates the list of ambient context detection types supported by the server. Each supported ambient context
     * detection type element shall correspond to a feature type supported in the AmbientContextFeatureMap and shall be
     * represented by a SemanticTagStruct from one of the following StandardNamespaces: Identified Human Activity
     * Namespace, Identified Object Namespace, or Identified Sound Namespace.
     */
    public List<Semtag> ambientContextTypeSupported; // 4 list R V
    /**
     * Indicates whether the number count of the specified object is greater or equal to the threshold specified by the
     * ObjectCountThreshold. The object being counted shall be limited to one identified object type and shall be
     * identified by the tag ID from the Identified Object namespace, as presented in the AmbientContextTypeSupported
     * attribute.
     */
    public Boolean objectCountThresholdReached; // 5 bool R V
    /**
     * This attribute shall specify the configuration parameters required to support an object counting feature,
     * including the identification of the object to be detected and counted, as well as the counting threshold value
     * for the ObjectCountThresholdReached detection.
     */
    public ObjectCountConfigStruct objectCountConfig; // 6 ObjectCountConfigStruct RW VM
    /**
     * This optional attribute shall indicate the number of objects detected in the area covered by the sensor.
     * ObjectCount shall be exposed only when the ObjectCountThresholdReached attribute is true.
     */
    public Integer objectCount; // 7 uint16 R V
    /**
     * Indicates the maximum number of simultaneous multiple ambient context detections supported by the server. If an
     * additional detection event causes the total number of simultaneous detection events to exceed a
     * SimultaneousDetectionLimit, the oldest ambient sensing detection event shall be removed and the latest detection
     * shall be added. The consecutive recurrence of the same ambient context sensing event within the HoldTime duration
     * shall not increase the total number of simultaneous detection events. If simultaneous detection is not supported,
     * then the value shall be set to 1.
     */
    public Integer simultaneousDetectionLimit; // 8 uint8 R V
    /**
     * Indicates the time duration (in seconds) of True state before transitioning its sensing detection state from True
     * to False after the last detection. Low HoldTime value SHOULD be avoided to prevent excessive data reporting for
     * subscription. This attribute is equivalent to the HoldTime attribute of the OccupancySensing cluster. For further
     * information, refer to the HoldTime attribute description of the Occupancy Sensing Cluster. The HoldTime shall be
     * applied individually to each ambient context detection occurrence individually. A more detailed HoldTime
     * implementation example for multiple simultaneous ambient context detections can be found in
     * theMultipleAmbientSensingDetection section.
     */
    public Integer holdTime; // 9 uint16 RW VM
    /**
     * Indicates the server's limits, and default value, for the HoldTime attribute. This is equivalent to the
     * HoldTimeLimits attribute of the Occupancy Sensing Cluster attribute. For further information, refer to the
     * HoldTimeLimits attribute description of the Occupancy Sensing Cluster.
     */
    public HoldTimeLimitsStruct holdTimeLimits; // 10 HoldTimeLimitsStruct R V
    /**
     * Indicates the server's prediction of upcoming changes to the monitored area's ambient context.
     * The value of the StartTimestamp field on each PredictedActivityStruct in this list other than the first shall be
     * greater than the value of the EndTimestamp field on the previous PredictedActivityStruct in this list.
     */
    public List<PredictedActivityStruct> predictedActivity; // 11 list R V

    // Structs
    /**
     * This event shall be generated when a new ambient context detection is added to the AmbientContextType attribute.
     */
    public static class AmbientContextDetectStarted {
        /**
         * This field shall indicate the detail ambient context information that triggers this event reporting. The
         * detail ambient context information shall be represented by the namespace ID and semantic tag ID available
         * from Identified Human Activity, Identified Object, and Identified Sound namespaces in the StandardNamespaces.
         * For the object counting feature, the AmbientContextDetected field shall represent the object being counted.
         */
        public AmbientContextTypeStruct ambientContextDetected; // AmbientContextTypeStruct
        /**
         * This field shall indicate an ObjectCountThresholdReached attribute value when the event reporting is
         * triggered by the object counting threshold detection.
         */
        public Boolean objectCountThresholdReached; // bool
        /**
         * This field shall indicate the number of objects detected in the area covered by the sensor when
         * ObjectCountThresholdReached attribute is changed to True.
         */
        public Integer objectCount; // uint16

        public AmbientContextDetectStarted(AmbientContextTypeStruct ambientContextDetected,
                Boolean objectCountThresholdReached, Integer objectCount) {
            this.ambientContextDetected = ambientContextDetected;
            this.objectCountThresholdReached = objectCountThresholdReached;
            this.objectCount = objectCount;
        }
    }

    /**
     * This event shall be generated when the ambient context previously reported by an AmbientContextDetectStarted
     * event is removed from the AmbientContextType attribute. This termination event doesn't necessarily reflect or
     * coincide with the end of the actual event progression. For example, while both AmbientContextDetectStarted and
     * AmbientContextDetectEnded events are used to inform the "sleeping" event occurrence, the
     * AmbientContextDetectEnded event doesn't necessarily indicate the actual end of "sleeping" action. The
     * AmbientContextDetectStarted event start time information is provided to facilitate the mapping of the matching
     * the AmbientContextDetectStarted event and the AmbientContextDetectEnded event for event logging purpose. If a
     * server is time-synchronized or capable of supporting both the POSIX time stamp and the system time stamp, a
     * server shall provide the EventStartTimePos data field instead of the EventStartTimeSys data field.
     */
    public static class AmbientContextDetectEnded {
        /**
         * This field shall indicate the POSIX time stamp when the corresponding AmbientContextDetectStarted Event was
         * generated.
         */
        public BigInteger eventStartTimePos; // posix-ms
        /**
         * This field shall indicate the system time stamp when the corresponding AmbientContextDetectStarted Event was
         * generated.
         */
        public BigInteger eventStartTimeSys; // systime-ms

        public AmbientContextDetectEnded(BigInteger eventStartTimePos, BigInteger eventStartTimeSys) {
            this.eventStartTimePos = eventStartTimePos;
            this.eventStartTimeSys = eventStartTimeSys;
        }
    }

    /**
     * This structure provides information on the server's supported values for the HoldTime attribute.
     */
    public static class HoldTimeLimitsStruct {
        /**
         * This field shall specify the minimum value supported by the server for the HoldTime attribute, in seconds.
         */
        public Integer holdTimeMin; // uint16
        /**
         * This field shall specify the maximum value supported by the server for the HoldTime attribute, in seconds.
         * This field also specifies the maximum duration time that is allowed to be continuously in triggered detection
         * state.
         */
        public Integer holdTimeMax; // uint16
        /**
         * This field shall specify the (manufacturer-determined) default value of the server's HoldTime attribute, in
         * seconds. This is the value that a client who wants to reset the settings to a valid default SHOULD use.
         */
        public Integer holdTimeDefault; // uint16

        public HoldTimeLimitsStruct(Integer holdTimeMin, Integer holdTimeMax, Integer holdTimeDefault) {
            this.holdTimeMin = holdTimeMin;
            this.holdTimeMax = holdTimeMax;
            this.holdTimeDefault = holdTimeDefault;
        }
    }

    /**
     * This structure provides information on the server's supported values for the Ambient Context type attribute.
     */
    public static class AmbientContextTypeStruct {
        /**
         * This field specifies the detail ambient context information associated with the Boolean detection attributes,
         * HumanActivityDetected, ObjectIdentified, and AudioContextDetected. The detail ambient context information
         * shall be represented by the namespace ID and semantic tag ID of the SemanticTagStruct available from
         * Identified Human Activity, Identified Object, and Identified Sound namespaces in the StandardNamespaces. When
         * AmbientContextSensed field contains multiple data elements, it shall indicate a single combined ambient
         * context event rather than unrelated independent ambient context events. For an example, if a joint event
         * exposure of "Child Fall" is intended, then the AmbientContextType attribute can be exposed as follows:
         * where AmbientContextSensed field contains the SemanticTag data list of "Child" tag ID (=2) from
         * IdentifiedObject namespace (=0x4B) and "Fall" tag ID (=1) from IdentifiedHumanActivity namespace (=0x49).
         * However, if two independent events exposure is intended, then the AmbientContextType attribute can be exposed
         * as follows:
         * where each AmbientContextSensed field contains only one individual ambient sensing context. In order to
         * prevent confusion arising from excessive joint permutations, AmbientContextSensed field shall NOT include
         * more than two ambient context events.
         */
        public List<Semtag> ambientContextSensed; // list

        public AmbientContextTypeStruct(List<Semtag> ambientContextSensed) {
            this.ambientContextSensed = ambientContextSensed;
        }
    }

    /**
     * This structure provides information on the server's supported values for the ObjectCountConfig attribute.
     */
    public static class ObjectCountConfigStruct {
        /**
         * This field shall indicate the object to be detected and counted. If the MfgCode field in CountingObject is
         * NULL, the object shall be specified by ObjectIdentified namespace ID and its tag ID available from the
         * AmbientContextTypeSupported attribute.
         */
        public Semtag countingObject; // semtag
        /**
         * This field shall indicate the minimum number of detected objects to render the true Boolean state of
         * CountThresholdReached attribute.
         */
        public Integer objectCountThreshold; // uint16

        public ObjectCountConfigStruct(Semtag countingObject, Integer objectCountThreshold) {
            this.countingObject = countingObject;
            this.objectCountThreshold = objectCountThreshold;
        }
    }

    /**
     * This data structure provides information on future predicted activities.
     */
    public static class PredictedActivityStruct {
        /**
         * This field shall indicate the predicted start time for the predicted activity.
         */
        public Long startTimestamp; // epoch-s
        /**
         * This field shall indicate the predicted end time for the predicted activity.
         */
        public Long endTimestamp; // epoch-s
        /**
         * This field shall indicate the predicted state of the AmbientContextType attribute for the specified time
         * period.
         */
        public List<Semtag> ambientContextType; // list
        /**
         * This field shall indicate the predicted state of the CrowdDetected attribute for the specified time period.
         */
        public Boolean crowdDetected; // bool
        /**
         * This field shall indicate the predicted value of the CrowdCount attribute for the specified time period.
         */
        public Integer crowdCount; // uint8
        /**
         * This field shall indicate confidence level for the predicted activity state.
         * A value of 100% shall indicate a complete certainty of the predicted occupancy state, while a 0% value shall
         * indicate no certainty. The algorithm to calculate the likelihood of a predicted occupancy state is not
         * specified and is considered manufacturer specific.
         */
        public Integer confidence; // percent

        public PredictedActivityStruct(Long startTimestamp, Long endTimestamp, List<Semtag> ambientContextType,
                Boolean crowdDetected, Integer crowdCount, Integer confidence) {
            this.startTimestamp = startTimestamp;
            this.endTimestamp = endTimestamp;
            this.ambientContextType = ambientContextType;
            this.crowdDetected = crowdDetected;
            this.crowdCount = crowdCount;
            this.confidence = confidence;
        }
    }

    // Bitmaps
    public static class FeatureMap {
        /**
         * 
         * Supports various human actions and activities classification
         */
        public boolean humanActivity;
        /**
         * 
         * Supports object counting
         */
        public boolean objectCounting;
        /**
         * 
         * Supports object identification
         */
        public boolean objectIdentification;
        /**
         * 
         * Supports sound identification
         */
        public boolean soundIdentification;
        /**
         * 
         * Supports predicting various human actions and activities
         */
        public boolean predictedActivity;

        public FeatureMap(boolean humanActivity, boolean objectCounting, boolean objectIdentification,
                boolean soundIdentification, boolean predictedActivity) {
            this.humanActivity = humanActivity;
            this.objectCounting = objectCounting;
            this.objectIdentification = objectIdentification;
            this.soundIdentification = soundIdentification;
            this.predictedActivity = predictedActivity;
        }
    }

    public AmbientContextSensingCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 1073, "AmbientContextSensing");
    }

    protected AmbientContextSensingCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "featureMap : " + featureMap + "\n";
        str += "humanActivityDetected : " + humanActivityDetected + "\n";
        str += "objectIdentified : " + objectIdentified + "\n";
        str += "audioContextDetected : " + audioContextDetected + "\n";
        str += "ambientContextType : " + ambientContextType + "\n";
        str += "ambientContextTypeSupported : " + ambientContextTypeSupported + "\n";
        str += "objectCountThresholdReached : " + objectCountThresholdReached + "\n";
        str += "objectCountConfig : " + objectCountConfig + "\n";
        str += "objectCount : " + objectCount + "\n";
        str += "simultaneousDetectionLimit : " + simultaneousDetectionLimit + "\n";
        str += "holdTime : " + holdTime + "\n";
        str += "holdTimeLimits : " + holdTimeLimits + "\n";
        str += "predictedActivity : " + predictedActivity + "\n";
        return str;
    }
}
