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

/**
 * ElectricalEnergyMeasurement
 *
 * @author Dan Cunningham - Initial contribution
 */
public class ElectricalEnergyMeasurementCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0091;
    public static final String CLUSTER_NAME = "ElectricalEnergyMeasurement";
    public static final String CLUSTER_PREFIX = "electricalEnergyMeasurement";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_ACCURACY = "accuracy";
    public static final String ATTRIBUTE_CUMULATIVE_ENERGY_IMPORTED = "cumulativeEnergyImported";
    public static final String ATTRIBUTE_CUMULATIVE_ENERGY_EXPORTED = "cumulativeEnergyExported";
    public static final String ATTRIBUTE_PERIODIC_ENERGY_IMPORTED = "periodicEnergyImported";
    public static final String ATTRIBUTE_PERIODIC_ENERGY_EXPORTED = "periodicEnergyExported";
    public static final String ATTRIBUTE_CUMULATIVE_ENERGY_RESET = "cumulativeEnergyReset";

    public Integer clusterRevision; // 65533 ClusterRevision
    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * Indicates the accuracy of energy measurement by this server. The value of the MeasurementType field on this
     * MeasurementAccuracyStruct shall be ElectricalEnergy.
     */
    public MeasurementAccuracyStruct accuracy; // 0 MeasurementAccuracyStruct R V
    /**
     * Indicates the most recent measurement of cumulative energy imported by the server over the lifetime of the
     * device, and the timestamp of when the measurement was recorded.
     * The reporting interval of this attribute shall be manufacturer dependent. The server may choose to omit
     * publication of deltas considered not meaningful.
     * The server shall NOT mark this attribute ready for report if the last time this was done was more recently than 1
     * second ago.
     * The server may delay marking this attribute ready for report for longer periods if needed, however the server
     * shall NOT delay marking this attribute as ready for report for longer than 60 seconds.
     * If the cumulative energy imported cannot currently be determined, a value of null shall be returned.
     */
    public EnergyMeasurementStruct cumulativeEnergyImported; // 1 EnergyMeasurementStruct R V
    /**
     * Indicates the most recent measurement of cumulative energy exported by the server over the lifetime of the
     * device, and the timestamp of when the measurement was recorded.
     * The reporting interval of this attribute shall be manufacturer dependent. The server may choose to omit
     * publication of deltas considered not meaningful.
     * The server shall NOT mark this attribute ready for report if the last time this was done was more recently than 1
     * second ago.
     * The server may delay marking this attribute ready for report for longer periods if needed, however the server
     * shall NOT delay marking this attribute as ready for report for longer than 60 seconds.
     * If the cumulative energy exported cannot currently be determined, a value of null shall be returned.
     */
    public EnergyMeasurementStruct cumulativeEnergyExported; // 2 EnergyMeasurementStruct R V
    /**
     * Indicates the most recent measurement of energy imported by the server and the period during which it was
     * measured.
     * The reporting interval of this attribute shall be manufacturer dependent. The server may choose to omit
     * publication of deltas considered not meaningful.
     * The server shall NOT mark this attribute ready for report if the last time this was done was more recently than 1
     * second ago.
     * The server may delay marking this attribute ready for report for longer periods if needed, however the server
     * shall NOT delay marking this attribute as ready for report for longer than 60 seconds.
     * If the periodic energy imported cannot currently be determined, a value of null shall be returned.
     */
    public EnergyMeasurementStruct periodicEnergyImported; // 3 EnergyMeasurementStruct R V
    /**
     * Indicates the most recent measurement of energy exported by the server and the period during which it was
     * measured.
     * The reporting interval of this attribute shall be manufacturer dependent. The server may choose to omit
     * publication of deltas considered not meaningful.
     * The server shall NOT mark this attribute ready for report if the last time this was done was more recently than 1
     * second ago.
     * The server may delay marking this attribute ready for report for longer periods if needed, however the server
     * shall NOT delay marking this attribute as ready for report for longer than 60 seconds.
     * If the periodic energy exported cannot currently be determined, a value of null shall be returned.
     */
    public EnergyMeasurementStruct periodicEnergyExported; // 4 EnergyMeasurementStruct R V
    /**
     * Indicates when cumulative measurements were most recently zero.
     */
    public CumulativeEnergyResetStruct cumulativeEnergyReset; // 5 CumulativeEnergyResetStruct R V

    // Structs
    /**
     * This event shall be generated when the server takes a snapshot of the cumulative energy imported by the server,
     * exported from the server, or both, but not more frequently than the rate mentioned in the description above of
     * the related attribute.
     */
    public static class CumulativeEnergyMeasured {
        /**
         * This field shall be the value of CumulativeEnergyImported attribute at the timestamp indicated in its
         * EndTimestamp field, EndSystime field, or both.
         */
        public EnergyMeasurementStruct energyImported; // EnergyMeasurementStruct
        /**
         * This field shall be the value of CumulativeEnergyExported attribute at the timestamp indicated in its
         * EndTimestamp field, EndSystime field, or both.
         */
        public EnergyMeasurementStruct energyExported; // EnergyMeasurementStruct

        public CumulativeEnergyMeasured(EnergyMeasurementStruct energyImported,
                EnergyMeasurementStruct energyExported) {
            this.energyImported = energyImported;
            this.energyExported = energyExported;
        }
    }

    /**
     * This event shall be generated when the server reaches the end of a reporting period for imported energy, exported
     * energy, or both.
     */
    public static class PeriodicEnergyMeasured {
        /**
         * This field shall be the value of PeriodicEnergyImported attribute at the timestamp indicated in its
         * EndTimestamp field, EndSystime field, or both.
         */
        public EnergyMeasurementStruct energyImported; // EnergyMeasurementStruct
        /**
         * This field shall be the value of PeriodicEnergyExported attribute at the timestamp indicated in its
         * EndTimestamp field, EndSystime field, or both.
         */
        public EnergyMeasurementStruct energyExported; // EnergyMeasurementStruct

        public PeriodicEnergyMeasured(EnergyMeasurementStruct energyImported, EnergyMeasurementStruct energyExported) {
            this.energyImported = energyImported;
            this.energyExported = energyExported;
        }
    }

    /**
     * This struct shall indicate the amount of energy measured during a given measurement period.
     * A server which does not have the ability to determine the time in UTC, or has not yet done so, shall use the
     * system time fields to specify the measurement period and observation times.
     * A server which has determined the time in UTC shall use the timestamp fields to specify the measurement period.
     * Such a server may also include the systime fields to indicate how many seconds had passed since boot for a given
     * timestamp; this allows for client-side resolution of UTC time for previous reports that only included systime.
     */
    public static class EnergyMeasurementStruct {
        /**
         * This field shall be the reported energy.
         * If the EnergyMeasurementStruct represents cumulative energy, then this shall represent the cumulative energy
         * recorded at either the value of the EndTimestamp field or the value of the EndSystime field, or both.
         * If the EnergyMeasurementStruct represents periodic energy, then this shall represent the energy recorded
         * during the period specified by either the StartTimestamp and EndTimestamp fields, the period specified by the
         * StartSystime and EndSystime fields, or both.
         */
        public BigInteger energy; // energy-mWh
        /**
         * This field shall indicate the timestamp in UTC of the beginning of the period during which the value of the
         * Energy field was measured.
         * If this EnergyMeasurementStruct represents cumulative energy, this field shall be omitted.
         * Otherwise, if the server had determined the time in UTC at or before the beginning of the measurement period,
         * this field shall be indicated.
         * Otherwise, if the server had not yet determined the time in UTC at or before the beginning of the measurement
         * period, or does not have the capability of determining the time in UTC, this field shall be omitted.
         */
        public Integer startTimestamp; // epoch-s
        /**
         * This field shall indicate the timestamp in UTC of the end of the period during which the value of the Energy
         * field was measured.
         * If the server had determined the time in UTC by the end of the measurement period, this field shall be
         * indicated.
         * Otherwise, if the server had not yet determined the time in UTC by the end of the measurement period, or does
         * not have the capability of determining the time in UTC, this field shall be omitted.
         */
        public Integer endTimestamp; // epoch-s
        /**
         * This field shall indicate the time elapsed since boot at the beginning of the period during which the value
         * of the Energy field was measured.
         * If this EnergyMeasurementStruct represents cumulative energy, this field shall be omitted.
         * Otherwise, if the server had not yet determined the time in UTC at the start of the measurement period, or
         * does not have the capability of determining the time in UTC, this field shall be indicated.
         * Otherwise, if the server had determined the time in UTC at or before the beginning of the measurement period,
         * this field may be omitted; if it is indicated, its value shall be the time elapsed since boot at the UTC time
         * indicated in StartTimestamp.
         */
        public BigInteger startSystime; // systime-ms
        /**
         * This field shall indicate the time elapsed since boot at the end of the period during which the value of the
         * Energy field was measured.
         * If the server had not yet determined the time in UTC by the end of the measurement period, or does not have
         * the capability of determining the time in UTC, this field shall be indicated.
         * Otherwise, if the server had determined the time in UTC by the end of the measurement period, this field may
         * be omitted; if it is indicated, its value shall be the time elapsed since boot at the UTC time indicated in
         * EndTimestamp.
         */
        public BigInteger endSystime; // systime-ms

        public EnergyMeasurementStruct(BigInteger energy, Integer startTimestamp, Integer endTimestamp,
                BigInteger startSystime, BigInteger endSystime) {
            this.energy = energy;
            this.startTimestamp = startTimestamp;
            this.endTimestamp = endTimestamp;
            this.startSystime = startSystime;
            this.endSystime = endSystime;
        }
    }

    /**
     * This struct shall represent the times at which cumulative measurements were last zero, either due to
     * initialization of the device, or an internal reset of the cumulative value.
     */
    public static class CumulativeEnergyResetStruct {
        /**
         * This field shall indicate the timestamp in UTC when the value of the Energy field on the
         * CumulativeEnergyImported attribute was most recently zero.
         * If the server had determined the time in UTC when the value of the Energy field on the
         * CumulativeEnergyImported attribute was most recently zero, this field shall be indicated.
         * Otherwise, if the server had not yet determined the time in UTC when the value of the Energy field on the
         * CumulativeEnergyImported attribute was most recently zero, or does not have the capability of determining the
         * time in UTC, this field shall be omitted.
         * If the timestamp in UTC when the value of the Energy field on the CumulativeEnergyImported attribute was most
         * recently zero cannot currently be determined, a value of null shall be returned.
         */
        public Integer importedResetTimestamp; // epoch-s
        /**
         * This field shall indicate the timestamp in UTC when the value of the Energy field on the
         * CumulativeEnergyExported attribute was most recently zero.
         * If the server had determined the time in UTC when the value of the Energy field on the
         * CumulativeEnergyExported attribute was most recently zero, this field shall be indicated.
         * Otherwise, if the server had not yet determined the time in UTC when the value of the Energy field on the
         * CumulativeEnergyExported attribute was most recently zero, or does not have the capability of determining the
         * time in UTC, this field shall be omitted.
         * If the timestamp in UTC when the value of the Energy field on the CumulativeEnergyExported attribute was most
         * recently zero cannot currently be determined, a value of null shall be returned.
         */
        public Integer exportedResetTimestamp; // epoch-s
        /**
         * This field shall indicate the time elapsed since boot when the value of the Energy field on the
         * CumulativeEnergyImported attribute was most recently zero.
         * If the server had not yet determined the time in UTC when the value of the Energy field on the
         * CumulativeEnergyImported attribute was most recently zero, or does not have the capability of determining the
         * time in UTC, this field shall be indicated.
         * Otherwise, if the server had determined the time in UTC when the value of the Energy field on the
         * CumulativeEnergyImported attribute was most recently zero, this field may be omitted; if it is indicated, its
         * value shall be the time elapsed since boot at the UTC time indicated in ImportedResetTimestamp.
         */
        public BigInteger importedResetSystime; // systime-ms
        /**
         * This field shall indicate the time elapsed since boot when the value of the Energy field on the
         * CumulativeEnergyExported attribute was most recently zero.
         * If the server had not yet determined the time in UTC when the value of the Energy field on the
         * CumulativeEnergyExported attribute was most recently zero, or does not have the capability of determining the
         * time in UTC, this field shall be indicated.
         * Otherwise, if the server had determined the time in UTC when the value of the Energy field on the
         * CumulativeEnergyExported attribute was most recently zero, this field may be omitted; if it is indicated, its
         * value shall be the time elapsed since boot at the UTC time indicated in ImportedResetTimestamp.
         */
        public BigInteger exportedResetSystime; // systime-ms

        public CumulativeEnergyResetStruct(Integer importedResetTimestamp, Integer exportedResetTimestamp,
                BigInteger importedResetSystime, BigInteger exportedResetSystime) {
            this.importedResetTimestamp = importedResetTimestamp;
            this.exportedResetTimestamp = exportedResetTimestamp;
            this.importedResetSystime = importedResetSystime;
            this.exportedResetSystime = exportedResetSystime;
        }
    }

    // Bitmaps
    public static class FeatureMap {
        /**
         * 
         * The feature indicates the server is capable of measuring how much energy is imported by the server.
         */
        public boolean importedEnergy;
        /**
         * 
         * The feature indicates the server is capable of measuring how much energy is exported by the server.
         */
        public boolean exportedEnergy;
        /**
         * 
         * The feature indicates the server is capable of measuring how much energy has been imported or exported by the
         * server over the device’s lifetime. This measurement may start from when a device’s firmware is updated to
         * include this feature, when a device’s firmware is updated to correct measurement errors, or when a device is
         * factory reset.
         */
        public boolean cumulativeEnergy;
        /**
         * 
         * The feature indicates the server is capable of measuring how much energy has been imported or exported by the
         * server during a certain period of time. The start and end times for measurement periods shall be determined
         * by the server, and may represent overlapping periods.
         */
        public boolean periodicEnergy;

        public FeatureMap(boolean importedEnergy, boolean exportedEnergy, boolean cumulativeEnergy,
                boolean periodicEnergy) {
            this.importedEnergy = importedEnergy;
            this.exportedEnergy = exportedEnergy;
            this.cumulativeEnergy = cumulativeEnergy;
            this.periodicEnergy = periodicEnergy;
        }
    }

    public ElectricalEnergyMeasurementCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 145, "ElectricalEnergyMeasurement");
    }

    protected ElectricalEnergyMeasurementCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "featureMap : " + featureMap + "\n";
        str += "accuracy : " + accuracy + "\n";
        str += "cumulativeEnergyImported : " + cumulativeEnergyImported + "\n";
        str += "cumulativeEnergyExported : " + cumulativeEnergyExported + "\n";
        str += "periodicEnergyImported : " + periodicEnergyImported + "\n";
        str += "periodicEnergyExported : " + periodicEnergyExported + "\n";
        str += "cumulativeEnergyReset : " + cumulativeEnergyReset + "\n";
        return str;
    }
}
