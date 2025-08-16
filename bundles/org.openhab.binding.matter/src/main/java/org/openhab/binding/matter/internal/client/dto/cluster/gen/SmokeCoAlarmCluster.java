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

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.matter.internal.client.dto.cluster.ClusterCommand;

/**
 * SmokeCoAlarm
 *
 * @author Dan Cunningham - Initial contribution
 */
public class SmokeCoAlarmCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x005C;
    public static final String CLUSTER_NAME = "SmokeCoAlarm";
    public static final String CLUSTER_PREFIX = "smokeCoAlarm";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_EXPRESSED_STATE = "expressedState";
    public static final String ATTRIBUTE_SMOKE_STATE = "smokeState";
    public static final String ATTRIBUTE_CO_STATE = "coState";
    public static final String ATTRIBUTE_BATTERY_ALERT = "batteryAlert";
    public static final String ATTRIBUTE_DEVICE_MUTED = "deviceMuted";
    public static final String ATTRIBUTE_TEST_IN_PROGRESS = "testInProgress";
    public static final String ATTRIBUTE_HARDWARE_FAULT_ALERT = "hardwareFaultAlert";
    public static final String ATTRIBUTE_END_OF_SERVICE_ALERT = "endOfServiceAlert";
    public static final String ATTRIBUTE_INTERCONNECT_SMOKE_ALARM = "interconnectSmokeAlarm";
    public static final String ATTRIBUTE_INTERCONNECT_CO_ALARM = "interconnectCoAlarm";
    public static final String ATTRIBUTE_CONTAMINATION_STATE = "contaminationState";
    public static final String ATTRIBUTE_SMOKE_SENSITIVITY_LEVEL = "smokeSensitivityLevel";
    public static final String ATTRIBUTE_EXPIRY_DATE = "expiryDate";

    public Integer clusterRevision; // 65533 ClusterRevision
    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * Indicates the visibly- and audibly-expressed state of the alarm. When multiple alarm conditions are being
     * reflected in the server, this attribute shall indicate the condition with the highest priority. Priority order of
     * conditions is determined by the manufacturer and shall be supplied as a part of certification procedure. If the
     * value of ExpressedState is not Normal, the attribute corresponding to the value shall NOT be Normal. For example,
     * if the ExpressedState is set to SmokeAlarm, the value of the SmokeState will indicate the severity of the alarm
     * (Warning or Critical). Clients SHOULD also read the other attributes to be aware of further alarm conditions
     * beyond the one indicated in ExpressedState.
     * Visible expression is typically a LED light pattern. Audible expression is a horn or speaker pattern. Audible
     * expression shall BE suppressed if the DeviceMuted attribute is supported and set to Muted.
     */
    public ExpressedStateEnum expressedState; // 0 ExpressedStateEnum R V
    /**
     * Indicates whether the device’s smoke sensor is currently triggering a smoke alarm.
     */
    public AlarmStateEnum smokeState; // 1 AlarmStateEnum R V
    /**
     * Indicates whether the device’s CO sensor is currently triggering a CO alarm.
     */
    public AlarmStateEnum coState; // 2 AlarmStateEnum R V
    /**
     * Indicates whether the power resource fault detection mechanism is currently triggered at the device. If the
     * detection mechanism is triggered, this attribute shall be set to Warning or Critical, otherwise it shall be set
     * to Normal. The battery state shall also be reflected in the Power Source cluster representing the device’s
     * battery using the appropriate supported attributes and events.
     */
    public AlarmStateEnum batteryAlert; // 3 AlarmStateEnum R V
    /**
     * Indicates the whether the audible expression of the device is currently muted. Audible expression is typically a
     * horn or speaker pattern.
     */
    public MuteStateEnum deviceMuted; // 4 MuteStateEnum R V
    /**
     * Indicates whether the device self-test is currently activated. If the device self-test is activated, this
     * attribute shall be set to True, otherwise it shall be set to False.
     */
    public Boolean testInProgress; // 5 bool R V
    /**
     * Indicates whether the hardware fault detection mechanism is currently triggered. If the detection mechanism is
     * triggered, this attribute shall be set to True, otherwise it shall be set to False.
     */
    public Boolean hardwareFaultAlert; // 6 bool R V
    /**
     * Indicates whether the end-of-service has been triggered at the device. This attribute shall be set to Expired
     * when the device reaches the end-of-service.
     */
    public EndOfServiceEnum endOfServiceAlert; // 7 EndOfServiceEnum R V
    /**
     * Indicates whether the interconnected smoke alarm is currently triggering by branching devices. When the
     * interconnected smoke alarm is being triggered, this attribute shall be set to Warning or Critical, otherwise it
     * shall be set to Normal.
     */
    public AlarmStateEnum interconnectSmokeAlarm; // 8 AlarmStateEnum R V
    /**
     * Indicates whether the interconnected CO alarm is currently triggering by branching devices. When the
     * interconnected CO alarm is being triggered, this attribute shall be set to Warning or Critical, otherwise it
     * shall be set to Normal.
     */
    public AlarmStateEnum interconnectCoAlarm; // 9 AlarmStateEnum R V
    /**
     * Indicates the contamination level of the smoke sensor.
     */
    public ContaminationStateEnum contaminationState; // 10 ContaminationStateEnum R V
    /**
     * Indicates the sensitivity level of the smoke sensor configured on the device.
     */
    public SensitivityEnum smokeSensitivityLevel; // 11 SensitivityEnum RW VM
    /**
     * Indicates the date when the device reaches its stated expiry date. After the ExpiryDate has been reached, the
     * EndOfServiceAlert shall start to be triggered. To account for better customer experience across time zones, the
     * EndOfServiceAlert may be delayed by up to 24 hours after the ExpiryDate. Similarly, clients may delay any actions
     * based on the ExpiryDate by up to 24 hours to best align with the local time zone.
     */
    public Integer expiryDate; // 12 epoch-s R V

    // Structs
    /**
     * This event shall be generated when SmokeState attribute changes to either Warning or Critical state.
     */
    public static class SmokeAlarm {
        /**
         * This field shall indicate the current value of the SmokeState attribute.
         */
        public AlarmStateEnum alarmSeverityLevel; // AlarmStateEnum

        public SmokeAlarm(AlarmStateEnum alarmSeverityLevel) {
            this.alarmSeverityLevel = alarmSeverityLevel;
        }
    }

    /**
     * This event shall be generated when COState attribute changes to either Warning or Critical state.
     */
    public static class CoAlarm {
        /**
         * This field shall indicate the current value of the COState attribute.
         */
        public AlarmStateEnum alarmSeverityLevel; // AlarmStateEnum

        public CoAlarm(AlarmStateEnum alarmSeverityLevel) {
            this.alarmSeverityLevel = alarmSeverityLevel;
        }
    }

    /**
     * This event shall be generated when BatteryAlert attribute changes to either Warning or Critical state.
     */
    public static class LowBattery {
        /**
         * This field shall indicate the current value of the BatteryAlert attribute.
         */
        public AlarmStateEnum alarmSeverityLevel; // AlarmStateEnum

        public LowBattery(AlarmStateEnum alarmSeverityLevel) {
            this.alarmSeverityLevel = alarmSeverityLevel;
        }
    }

    /**
     * This event shall be generated when the device detects a hardware fault that leads to setting HardwareFaultAlert
     * to True.
     */
    public static class HardwareFault {
        public HardwareFault() {
        }
    }

    /**
     * This event shall be generated when the EndOfServiceAlert is set to Expired.
     */
    public static class EndOfService {
        public EndOfService() {
        }
    }

    /**
     * This event shall be generated when the SelfTest completes, and the attribute TestInProgress changes to False.
     */
    public static class SelfTestComplete {
        public SelfTestComplete() {
        }
    }

    /**
     * This event shall be generated when the DeviceMuted attribute changes to Muted.
     */
    public static class AlarmMuted {
        public AlarmMuted() {
        }
    }

    /**
     * This event shall be generated when DeviceMuted attribute changes to NotMuted.
     */
    public static class MuteEnded {
        public MuteEnded() {
        }
    }

    /**
     * This event shall be generated when the device hosting the server receives a smoke alarm from an interconnected
     * sensor.
     */
    public static class InterconnectSmokeAlarm {
        /**
         * This field shall indicate the current value of the InterconnectSmokeAlarm attribute.
         */
        public AlarmStateEnum alarmSeverityLevel; // AlarmStateEnum

        public InterconnectSmokeAlarm(AlarmStateEnum alarmSeverityLevel) {
            this.alarmSeverityLevel = alarmSeverityLevel;
        }
    }

    /**
     * This event shall be generated when the device hosting the server receives a CO alarm from an interconnected
     * sensor.
     */
    public static class InterconnectCoAlarm {
        /**
         * This field shall indicate the current value of the InterconnectCOAlarm attribute.
         */
        public AlarmStateEnum alarmSeverityLevel; // AlarmStateEnum

        public InterconnectCoAlarm(AlarmStateEnum alarmSeverityLevel) {
            this.alarmSeverityLevel = alarmSeverityLevel;
        }
    }

    /**
     * This event shall be generated when ExpressedState attribute returns to Normal state.
     */
    public static class AllClear {
        public AllClear() {
        }
    }

    // Enums
    public enum AlarmStateEnum implements MatterEnum {
        NORMAL(0, "Normal"),
        WARNING(1, "Warning"),
        CRITICAL(2, "Critical");

        public final Integer value;
        public final String label;

        private AlarmStateEnum(Integer value, String label) {
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

    public enum SensitivityEnum implements MatterEnum {
        HIGH(0, "High"),
        STANDARD(1, "Standard"),
        LOW(2, "Low");

        public final Integer value;
        public final String label;

        private SensitivityEnum(Integer value, String label) {
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

    public enum ExpressedStateEnum implements MatterEnum {
        NORMAL(0, "Normal"),
        SMOKE_ALARM(1, "Smoke Alarm"),
        CO_ALARM(2, "Co Alarm"),
        BATTERY_ALERT(3, "Battery Alert"),
        TESTING(4, "Testing"),
        HARDWARE_FAULT(5, "Hardware Fault"),
        END_OF_SERVICE(6, "End Of Service"),
        INTERCONNECT_SMOKE(7, "Interconnect Smoke"),
        INTERCONNECT_CO(8, "Interconnect Co");

        public final Integer value;
        public final String label;

        private ExpressedStateEnum(Integer value, String label) {
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

    public enum MuteStateEnum implements MatterEnum {
        NOT_MUTED(0, "Not Muted"),
        MUTED(1, "Muted");

        public final Integer value;
        public final String label;

        private MuteStateEnum(Integer value, String label) {
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

    public enum EndOfServiceEnum implements MatterEnum {
        NORMAL(0, "Normal"),
        EXPIRED(1, "Expired");

        public final Integer value;
        public final String label;

        private EndOfServiceEnum(Integer value, String label) {
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

    public enum ContaminationStateEnum implements MatterEnum {
        NORMAL(0, "Normal"),
        LOW(1, "Low"),
        WARNING(2, "Warning"),
        CRITICAL(3, "Critical");

        public final Integer value;
        public final String label;

        private ContaminationStateEnum(Integer value, String label) {
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
         * Supports Smoke alarm
         */
        public boolean smokeAlarm;
        /**
         * 
         * Supports CO alarm
         */
        public boolean coAlarm;

        public FeatureMap(boolean smokeAlarm, boolean coAlarm) {
            this.smokeAlarm = smokeAlarm;
            this.coAlarm = coAlarm;
        }
    }

    public SmokeCoAlarmCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 92, "SmokeCoAlarm");
    }

    protected SmokeCoAlarmCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * This command shall initiate a device self-test. The return status shall indicate whether the test was
     * successfully initiated. Only one SelfTestRequest may be processed at a time. When the value of the ExpressedState
     * attribute is any of SmokeAlarm, COAlarm, Testing, InterconnectSmoke, InterconnectCO, the device shall NOT execute
     * the self-test, and shall return status code BUSY.
     * Upon successful acceptance of SelfTestRequest, the TestInProgress attribute shall be set to True and
     * ExpressedState attribute shall be set to Testing. Any faults identified during the test shall be reflected in the
     * appropriate attributes and events. Upon completion of the self test procedure, the SelfTestComplete event shall
     * be generated, the TestInProgress attribute shall be set to False and ExpressedState attribute shall be updated to
     * reflect the current state of the server.
     */
    public static ClusterCommand selfTestRequest() {
        return new ClusterCommand("selfTestRequest");
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "featureMap : " + featureMap + "\n";
        str += "expressedState : " + expressedState + "\n";
        str += "smokeState : " + smokeState + "\n";
        str += "coState : " + coState + "\n";
        str += "batteryAlert : " + batteryAlert + "\n";
        str += "deviceMuted : " + deviceMuted + "\n";
        str += "testInProgress : " + testInProgress + "\n";
        str += "hardwareFaultAlert : " + hardwareFaultAlert + "\n";
        str += "endOfServiceAlert : " + endOfServiceAlert + "\n";
        str += "interconnectSmokeAlarm : " + interconnectSmokeAlarm + "\n";
        str += "interconnectCoAlarm : " + interconnectCoAlarm + "\n";
        str += "contaminationState : " + contaminationState + "\n";
        str += "smokeSensitivityLevel : " + smokeSensitivityLevel + "\n";
        str += "expiryDate : " + expiryDate + "\n";
        return str;
    }
}
