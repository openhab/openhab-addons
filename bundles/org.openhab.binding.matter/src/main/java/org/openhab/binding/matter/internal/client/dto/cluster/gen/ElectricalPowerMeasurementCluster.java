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
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

/**
 * ElectricalPowerMeasurement
 *
 * @author Dan Cunningham - Initial contribution
 */
public class ElectricalPowerMeasurementCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0090;
    public static final String CLUSTER_NAME = "ElectricalPowerMeasurement";
    public static final String CLUSTER_PREFIX = "electricalPowerMeasurement";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_POWER_MODE = "powerMode";
    public static final String ATTRIBUTE_NUMBER_OF_MEASUREMENT_TYPES = "numberOfMeasurementTypes";
    public static final String ATTRIBUTE_ACCURACY = "accuracy";
    public static final String ATTRIBUTE_RANGES = "ranges";
    public static final String ATTRIBUTE_VOLTAGE = "voltage";
    public static final String ATTRIBUTE_ACTIVE_CURRENT = "activeCurrent";
    public static final String ATTRIBUTE_REACTIVE_CURRENT = "reactiveCurrent";
    public static final String ATTRIBUTE_APPARENT_CURRENT = "apparentCurrent";
    public static final String ATTRIBUTE_ACTIVE_POWER = "activePower";
    public static final String ATTRIBUTE_REACTIVE_POWER = "reactivePower";
    public static final String ATTRIBUTE_APPARENT_POWER = "apparentPower";
    public static final String ATTRIBUTE_RMS_VOLTAGE = "rmsVoltage";
    public static final String ATTRIBUTE_RMS_CURRENT = "rmsCurrent";
    public static final String ATTRIBUTE_RMS_POWER = "rmsPower";
    public static final String ATTRIBUTE_FREQUENCY = "frequency";
    public static final String ATTRIBUTE_HARMONIC_CURRENTS = "harmonicCurrents";
    public static final String ATTRIBUTE_HARMONIC_PHASES = "harmonicPhases";
    public static final String ATTRIBUTE_POWER_FACTOR = "powerFactor";
    public static final String ATTRIBUTE_NEUTRAL_CURRENT = "neutralCurrent";

    public Integer clusterRevision; // 65533 ClusterRevision
    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * This shall indicate the current mode of the server. For some servers, such as an EV, this may change depending on
     * the mode of charging or discharging.
     */
    public PowerModeEnum powerMode; // 0 PowerModeEnum R V
    /**
     * This shall indicate the maximum number of measurement types the server is capable of reporting.
     */
    public Integer numberOfMeasurementTypes; // 1 uint8 R V
    /**
     * This shall indicate a list of accuracy specifications for the measurement types supported by the server. There
     * shall be an entry for ActivePower, as well as any other measurement types implemented by this server.
     */
    public List<MeasurementAccuracyStruct> accuracy; // 2 list R V
    /**
     * This shall indicate a list of measured ranges for different measurement types. Each measurement type shall have
     * at most one entry in this list, representing the range of measurements in the most recent measurement period.
     * The reporting interval of this attribute shall be manufacturer dependent. The server may choose to omit
     * publication of deltas considered not meaningful.
     * The server shall NOT mark this attribute ready for report if the last time this was done was more recently than 1
     * second ago.
     * The server may delay marking this attribute ready for report for longer periods if needed, however the server
     * shall NOT delay marking this attribute as ready for report for longer than 60 seconds.
     */
    public List<MeasurementRangeStruct> ranges; // 3 list R V
    /**
     * This shall indicate the most recent Voltage reading in millivolts (mV).
     * The reporting interval of this attribute shall be manufacturer dependent. The server may choose to omit
     * publication of deltas considered not meaningful.
     * The server shall NOT mark this attribute ready for report if the last time this was done was more recently than 1
     * second ago.
     * The server may delay marking this attribute ready for report for longer periods if needed, however the server
     * shall NOT delay marking this attribute as ready for report for longer than 60 seconds.
     * If the voltage cannot be measured, a value of null shall be returned.
     */
    public BigInteger voltage; // 4 voltage-mV R V
    /**
     * This shall indicate the most recent ActiveCurrent reading in milliamps (mA).
     * A positive value represents current flowing into the server, while a negative value represents current flowing
     * out of the server.
     * The reporting interval of this attribute shall be manufacturer dependent. The server may choose to omit
     * publication of deltas considered not meaningful.
     * The server shall NOT mark this attribute ready for report if the last time this was done was more recently than 1
     * second ago.
     * The server may delay marking this attribute ready for report for longer periods if needed, however the server
     * shall NOT delay marking this attribute as ready for report for longer than 60 seconds.
     * If the current cannot be measured, a value of null shall be returned.
     */
    public BigInteger activeCurrent; // 5 amperage-mA R V
    /**
     * This shall indicate the most recent ReactiveCurrent reading in milliamps (mA).
     * A positive value represents current flowing into the server, while a negative value represents current flowing
     * out of the server.
     * The reporting interval of this attribute shall be manufacturer dependent. The server may choose to omit
     * publication of deltas considered not meaningful.
     * The server shall NOT mark this attribute ready for report if the last time this was done was more recently than 1
     * second ago.
     * The server may delay marking this attribute ready for report for longer periods if needed, however the server
     * shall NOT delay marking this attribute as ready for report for longer than 60 seconds.
     * If the current cannot be measured, a value of null shall be returned.
     */
    public BigInteger reactiveCurrent; // 6 amperage-mA R V
    /**
     * This shall indicate the most recent ApparentCurrent (square root sum of the squares of active and reactive
     * currents) reading in milliamps (mA).
     * The reporting interval of this attribute shall be manufacturer dependent. The server may choose to omit
     * publication of deltas considered not meaningful.
     * The server shall NOT mark this attribute ready for report if the last time this was done was more recently than 1
     * second ago.
     * The server may delay marking this attribute ready for report for longer periods if needed, however the server
     * shall NOT delay marking this attribute as ready for report for longer than 60 seconds.
     * If the active or reactive currents cannot be measured, a value of null shall be returned.
     */
    public BigInteger apparentCurrent; // 7 amperage-mA R V
    /**
     * This shall indicate the most recent ActivePower reading in milliwatts (mW). If the power cannot be measured, a
     * value of null shall be returned.
     * A positive value represents power imported, while a negative value represents power exported.
     * The reporting interval of this attribute shall be manufacturer dependent. The server may choose to omit
     * publication of deltas considered not meaningful.
     * The server shall NOT mark this attribute ready for report if the last time this was done was more recently than 1
     * second ago.
     * The server may delay marking this attribute ready for report for longer periods if needed, however the server
     * shall NOT delay marking this attribute as ready for report for longer than 60 seconds.
     * If the Polyphase Power feature is set, this value represents the combined active power imported or exported.
     */
    public BigInteger activePower; // 8 power-mW R V
    /**
     * This shall indicate the most recent ReactivePower reading in millivolt-amps reactive (mVAR). A positive value
     * represents power imported, while a negative value represents power exported.
     * The reporting interval of this attribute shall be manufacturer dependent. The server may choose to omit
     * publication of deltas considered not meaningful.
     * The server shall NOT mark this attribute ready for report if the last time this was done was more recently than 1
     * second ago.
     * The server may delay marking this attribute ready for report for longer periods if needed, however the server
     * shall NOT delay marking this attribute as ready for report for longer than 60 seconds.
     * If the reactive power cannot be measured, a value of null shall be returned.
     * If the Polyphase Power feature is supported, this value represents the combined reactive power imported or
     * exported.
     */
    public BigInteger reactivePower; // 9 power-mW R V
    /**
     * This shall indicate the most recent ApparentPower reading in millivolt-amps (mVA).
     * A positive value represents power imported, while a negative value represents power exported.
     * The reporting interval of this attribute shall be manufacturer dependent. The server may choose to omit
     * publication of deltas considered not meaningful.
     * The server shall NOT mark this attribute ready for report if the last time this was done was more recently than 1
     * second ago.
     * The server may delay marking this attribute ready for report for longer periods if needed, however the server
     * shall NOT delay marking this attribute as ready for report for longer than 60 seconds.
     * If the apparent power cannot be measured, a value of null shall be returned.
     */
    public BigInteger apparentPower; // 10 power-mW R V
    /**
     * This shall indicate the most recent RMSVoltage reading in millivolts (mV).
     * The reporting interval of this attribute shall be manufacturer dependent. The server may choose to omit
     * publication of deltas considered not meaningful.
     * The server shall NOT mark this attribute ready for report if the last time this was done was more recently than 1
     * second ago.
     * The server may delay marking this attribute ready for report for longer periods if needed, however the server
     * shall NOT delay marking this attribute as ready for report for longer than 60 seconds.
     * If the RMS voltage cannot be measured, a value of null shall be returned.
     */
    public BigInteger rmsVoltage; // 11 voltage-mV R V
    /**
     * This shall indicate the most recent RMSCurrent reading in milliamps (mA).
     * A positive value represents current flowing into the server, while a negative value represents current flowing
     * out of the server.
     * The reporting interval of this attribute shall be manufacturer dependent. The server may choose to omit
     * publication of deltas considered not meaningful.
     * The server shall NOT mark this attribute ready for report if the last time this was done was more recently than 1
     * second ago.
     * The server may delay marking this attribute ready for report for longer periods if needed, however the server
     * shall NOT delay marking this attribute as ready for report for longer than 60 seconds.
     * If the RMS current cannot be measured, a value of null shall be returned.
     */
    public BigInteger rmsCurrent; // 12 amperage-mA R V
    /**
     * This shall indicate the most recent RMSPower reading in milliwatts (mW).
     * A positive value represents power imported, while a negative value represents power exported.
     * The reporting interval of this attribute shall be manufacturer dependent. The server may choose to omit
     * publication of deltas considered not meaningful.
     * The server shall NOT mark this attribute ready for report if the last time this was done was more recently than 1
     * second ago.
     * The server may delay marking this attribute ready for report for longer periods if needed, however the server
     * shall NOT delay marking this attribute as ready for report for longer than 60 seconds.
     * If the RMS power cannot be measured, a value of null shall be returned.
     */
    public BigInteger rmsPower; // 13 power-mW R V
    /**
     * This shall indicate the most recent Frequency reading in millihertz (mHz).
     * The reporting interval of this attribute shall be manufacturer dependent. The server may choose to omit
     * publication of deltas considered not meaningful.
     * The server shall NOT mark this attribute ready for report if the last time this was done was more recently than 1
     * second ago.
     * The server may delay marking this attribute ready for report for longer periods if needed, however the server
     * shall NOT delay marking this attribute as ready for report for longer than 60 seconds.
     * If the frequency cannot be measured, a value of null shall be returned.
     */
    public BigInteger frequency; // 14 int64 R V
    /**
     * This shall indicate a list of HarmonicMeasurementStruct values, with each HarmonicMeasurementStruct representing
     * the harmonic current reading for the harmonic order specified by Order.
     * The reporting interval of this attribute shall be manufacturer dependent. The server may choose to omit
     * publication of deltas considered not meaningful.
     * The server shall NOT mark this attribute ready for report if the last time this was done was more recently than 1
     * second ago.
     * The server may delay marking this attribute ready for report for longer periods if needed, however the server
     * shall NOT delay marking this attribute as ready for report for longer than 60 seconds.
     */
    public List<HarmonicMeasurementStruct> harmonicCurrents; // 15 list R V
    /**
     * This shall indicate a list of HarmonicMeasurementStruct values, with each HarmonicMeasurementStruct representing
     * the most recent phase of the harmonic current reading for the harmonic order specified by Order.
     * The reporting interval of this attribute shall be manufacturer dependent. The server may choose to omit
     * publication of deltas considered not meaningful.
     * The server shall NOT mark this attribute ready for report if the last time this was done was more recently than 1
     * second ago.
     * The server may delay marking this attribute ready for report for longer periods if needed, however the server
     * shall NOT delay marking this attribute as ready for report for longer than 60 seconds.
     */
    public List<HarmonicMeasurementStruct> harmonicPhases; // 16 list R V
    /**
     * This shall indicate the Power Factor ratio in +/- 1/100ths of a percent.
     * The reporting interval of this attribute shall be manufacturer dependent. The server may choose to omit
     * publication of deltas considered not meaningful.
     * The server shall NOT mark this attribute ready for report if the last time this was done was more recently than 1
     * second ago.
     * The server may delay marking this attribute ready for report for longer periods if needed, however the server
     * shall NOT delay marking this attribute as ready for report for longer than 60 seconds.
     */
    public BigInteger powerFactor; // 17 int64 R V
    /**
     * This shall indicate the most recent NeutralCurrent reading in milliamps (mA). Typically this is a derived value,
     * taking the magnitude of the vector sum of phase currents.
     * If the neutral current cannot be measured or derived, a value of null shall be returned.
     * A positive value represents an imbalance between the phase currents when power is imported.
     * A negative value represents an imbalance between the phase currents when power is exported.
     * The reporting interval of this attribute shall be manufacturer dependent. The server may choose to omit
     * publication of deltas considered not meaningful.
     * The server shall NOT mark this attribute ready for report if the last time this was done was more recently than 1
     * second ago.
     * The server may delay marking this attribute ready for report for longer periods if needed, however the server
     * shall NOT delay marking this attribute as ready for report for longer than 60 seconds.
     */
    public BigInteger neutralCurrent; // 18 amperage-mA R V

    // Structs
    /**
     * If supported, this event shall be generated at the end of a measurement period. The start and end times for
     * measurement periods shall be determined by the server, and may represent overlapping periods.
     */
    public static class MeasurementPeriodRanges {
        /**
         * This shall indicate the value of the Ranges attribute at the time of event generation.
         */
        public List<MeasurementRangeStruct> ranges; // list

        public MeasurementPeriodRanges(List<MeasurementRangeStruct> ranges) {
            this.ranges = ranges;
        }
    }

    /**
     * This struct shall indicate the maximum and minimum values of a given measurement type during a measurement
     * period, along with the observation times of these values.
     * A server which does not have the ability to determine the time in UTC, or has not yet done so, shall use the
     * system time fields to specify the measurement period and observation times.
     * A server which has determined the time in UTC shall use the timestamp fields to specify the measurement period
     * and observation times. Such a server may also include the systime fields to indicate how many seconds had passed
     * since boot for a given timestamp; this allows for client-side resolution of UTC time for previous reports that
     * only included systime.
     */
    public static class MeasurementRangeStruct {
        /**
         * This field shall be the type of measurement for the range provided.
         */
        public MeasurementTypeEnum measurementType; // MeasurementTypeEnum
        /**
         * This field shall be the smallest measured value for the associated measurement over either the period between
         * StartTimestamp and EndTimestamp, or the period between StartSystime and EndSystime, or both.
         */
        public BigInteger min; // int64
        /**
         * This field shall be the largest measured value for the associated measurement over the period between either
         * StartTimestamp and EndTimestamp or the period between StartSystime and EndSystime, or both.
         */
        public BigInteger max; // int64
        /**
         * This field shall be the timestamp in UTC of the beginning of the measurement period.
         * If the server had not yet determined the time in UTC at or before the beginning of the measurement period, or
         * does not have the capability of determining the time in UTC, this field shall be omitted.
         */
        public Integer startTimestamp; // epoch-s
        /**
         * This field shall be the timestamp in UTC of the end of the measurement period.
         * If the server had not yet determined the time in UTC at or before the beginning of the measurement period, or
         * does not have the capability of determining the time in UTC, this field shall be omitted.
         */
        public Integer endTimestamp; // epoch-s
        /**
         * This field shall be the most recent timestamp in UTC that the value in the Min field was measured.
         * This field shall be greater than or equal to the value of the StartTimestamp field. This field shall be less
         * than or equal to the value of the EndTimestamp field.
         */
        public Integer minTimestamp; // epoch-s
        /**
         * This field shall be the most recent timestamp in UTC of the value in the Max field. This field shall be
         * greater than or equal to the value of the StartTimestamp field. This field shall be less than or equal to the
         * value of the EndTimestamp field.
         */
        public Integer maxTimestamp; // epoch-s
        /**
         * This field shall be the time since boot of the beginning of the measurement period.
         * If the server had determined the time in UTC at or before the start of the measurement period, this field may
         * be omitted along with the EndSystime, MinSystime, and MaxSystime fields.
         */
        public BigInteger startSystime; // systime-ms
        /**
         * This field shall be the time since boot of the end of the measurement period.
         * If the server had determined the time in UTC at the end of the measurement period, this field may be omitted
         * along with the StartSystime field, MinSystime, and MaxSystime fields.
         */
        public BigInteger endSystime; // systime-ms
        /**
         * This field shall be the measurement time since boot of the value in the Min field was measured. This field
         * shall be greater than or equal to the value of the StartSystime field.
         * This field shall be less than or equal to the value of the EndSystime field.
         */
        public BigInteger minSystime; // systime-ms
        /**
         * This field shall be the measurement time since boot of the value in the Max field. This field shall be
         * greater than or equal to the value of the StartSystime field.
         * This field shall be less than or equal to the value of the EndSystime field.
         */
        public BigInteger maxSystime; // systime-ms

        public MeasurementRangeStruct(MeasurementTypeEnum measurementType, BigInteger min, BigInteger max,
                Integer startTimestamp, Integer endTimestamp, Integer minTimestamp, Integer maxTimestamp,
                BigInteger startSystime, BigInteger endSystime, BigInteger minSystime, BigInteger maxSystime) {
            this.measurementType = measurementType;
            this.min = min;
            this.max = max;
            this.startTimestamp = startTimestamp;
            this.endTimestamp = endTimestamp;
            this.minTimestamp = minTimestamp;
            this.maxTimestamp = maxTimestamp;
            this.startSystime = startSystime;
            this.endSystime = endSystime;
            this.minSystime = minSystime;
            this.maxSystime = maxSystime;
        }
    }

    public static class HarmonicMeasurementStruct {
        /**
         * This field shall be the order of the harmonic being measured. Typically this is an odd number, but servers
         * may choose to report even harmonics.
         */
        public Integer order; // uint8
        /**
         * This field shall be the measured value for the given harmonic order.
         * For the Harmonic Currents attribute, this value is the most recently measured harmonic current reading in
         * milliamps (mA). A positive value indicates that the measured harmonic current is positive, and a negative
         * value indicates that the measured harmonic current is negative.
         * For the Harmonic Phases attribute, this value is the most recent phase of the given harmonic order in
         * millidegrees (mDeg). A positive value indicates that the measured phase is leading, and a negative value
         * indicates that the measured phase is lagging.
         * If this measurement is not currently available, a value of null shall be returned.
         */
        public BigInteger measurement; // int64

        public HarmonicMeasurementStruct(Integer order, BigInteger measurement) {
            this.order = order;
            this.measurement = measurement;
        }
    }

    // Enums
    public enum PowerModeEnum implements MatterEnum {
        UNKNOWN(0, "Unknown"),
        DC(1, "Dc"),
        AC(2, "Ac");

        public final Integer value;
        public final String label;

        private PowerModeEnum(Integer value, String label) {
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
         * This feature indicates the cluster can measure a direct current.
         */
        public boolean directCurrent;
        /**
         * 
         * This feature indicates the cluster can measure an alternating current.
         */
        public boolean alternatingCurrent;
        /**
         * 
         * This feature indicates the cluster represents the collective measurements for a Polyphase power supply.
         */
        public boolean polyphasePower;
        /**
         * 
         * This feature indicates the cluster can measure the harmonics of an alternating current.
         */
        public boolean harmonics;
        /**
         * 
         * This feature indicates the cluster can measure the harmonic phases of an alternating current.
         */
        public boolean powerQuality;

        public FeatureMap(boolean directCurrent, boolean alternatingCurrent, boolean polyphasePower, boolean harmonics,
                boolean powerQuality) {
            this.directCurrent = directCurrent;
            this.alternatingCurrent = alternatingCurrent;
            this.polyphasePower = polyphasePower;
            this.harmonics = harmonics;
            this.powerQuality = powerQuality;
        }
    }

    public ElectricalPowerMeasurementCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 144, "ElectricalPowerMeasurement");
    }

    protected ElectricalPowerMeasurementCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "featureMap : " + featureMap + "\n";
        str += "powerMode : " + powerMode + "\n";
        str += "numberOfMeasurementTypes : " + numberOfMeasurementTypes + "\n";
        str += "accuracy : " + accuracy + "\n";
        str += "ranges : " + ranges + "\n";
        str += "voltage : " + voltage + "\n";
        str += "activeCurrent : " + activeCurrent + "\n";
        str += "reactiveCurrent : " + reactiveCurrent + "\n";
        str += "apparentCurrent : " + apparentCurrent + "\n";
        str += "activePower : " + activePower + "\n";
        str += "reactivePower : " + reactivePower + "\n";
        str += "apparentPower : " + apparentPower + "\n";
        str += "rmsVoltage : " + rmsVoltage + "\n";
        str += "rmsCurrent : " + rmsCurrent + "\n";
        str += "rmsPower : " + rmsPower + "\n";
        str += "frequency : " + frequency + "\n";
        str += "harmonicCurrents : " + harmonicCurrents + "\n";
        str += "harmonicPhases : " + harmonicPhases + "\n";
        str += "powerFactor : " + powerFactor + "\n";
        str += "neutralCurrent : " + neutralCurrent + "\n";
        return str;
    }
}
