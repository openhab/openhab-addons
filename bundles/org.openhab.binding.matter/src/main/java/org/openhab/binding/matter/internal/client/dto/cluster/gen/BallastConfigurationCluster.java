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
 * BallastConfiguration
 *
 * @author Dan Cunningham - Initial contribution
 */
public class BallastConfigurationCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0301;
    public static final String CLUSTER_NAME = "BallastConfiguration";
    public static final String CLUSTER_PREFIX = "ballastConfiguration";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_PHYSICAL_MIN_LEVEL = "physicalMinLevel";
    public static final String ATTRIBUTE_PHYSICAL_MAX_LEVEL = "physicalMaxLevel";
    public static final String ATTRIBUTE_BALLAST_STATUS = "ballastStatus";
    public static final String ATTRIBUTE_MIN_LEVEL = "minLevel";
    public static final String ATTRIBUTE_MAX_LEVEL = "maxLevel";
    public static final String ATTRIBUTE_INTRINSIC_BALLAST_FACTOR = "intrinsicBallastFactor";
    public static final String ATTRIBUTE_BALLAST_FACTOR_ADJUSTMENT = "ballastFactorAdjustment";
    public static final String ATTRIBUTE_LAMP_QUANTITY = "lampQuantity";
    public static final String ATTRIBUTE_LAMP_TYPE = "lampType";
    public static final String ATTRIBUTE_LAMP_MANUFACTURER = "lampManufacturer";
    public static final String ATTRIBUTE_LAMP_RATED_HOURS = "lampRatedHours";
    public static final String ATTRIBUTE_LAMP_BURN_HOURS = "lampBurnHours";
    public static final String ATTRIBUTE_LAMP_ALARM_MODE = "lampAlarmMode";
    public static final String ATTRIBUTE_LAMP_BURN_HOURS_TRIP_POINT = "lampBurnHoursTripPoint";

    public Integer clusterRevision; // 65533 ClusterRevision
    /**
     * This attribute shall specify the minimum light output the ballast can achieve according to the dimming light
     * curve (see Dimming Curve).
     */
    public Integer physicalMinLevel; // 0 uint8 R V
    /**
     * This attribute shall specify the maximum light output the ballast can achieve according to the dimming light
     * curve (see Dimming Curve).
     */
    public Integer physicalMaxLevel; // 1 uint8 R V
    /**
     * This attribute shall specify the status of various aspects of the ballast or the connected lights, see
     * BallastStatusBitmap.
     */
    public BallastStatusBitmap ballastStatus; // 2 BallastStatusBitmap R V
    /**
     * This attribute shall specify the light output of the ballast according to the dimming light curve (see Dimming
     * Curve) when the Level Control Cluster’s CurrentLevel attribute equals to 1 (and the On/Off Cluster’s OnOff
     * attribute equals to TRUE).
     * The value of this attribute shall be both greater than or equal to PhysicalMinLevel and less than or equal to
     * MaxLevel. If an attempt is made to set this attribute to a level where these conditions are not met, a response
     * shall be returned with status code set to CONSTRAINT_ERROR, and the level shall NOT be set.
     */
    public Integer minLevel; // 16 uint8 RW VM
    /**
     * This attribute shall specify the light output of the ballast according to the dimming light curve (see Dimming
     * Curve) when the Level Control Cluster’s CurrentLevel attribute equals to 254 (and the On/Off Cluster’s OnOff
     * attribute equals to TRUE).
     * The value of this attribute shall be both less than or equal to PhysicalMaxLevel and greater than or equal to
     * MinLevel. If an attempt is made to set this attribute to a level where these conditions are not met, a response
     * shall be returned with status code set to CONSTRAINT_ERROR, and the level shall NOT be set.
     */
    public Integer maxLevel; // 17 uint8 RW VM
    /**
     * This attribute shall specify the ballast factor, as a percentage, of the ballast/lamp combination, prior to any
     * adjustment.
     * A value of null indicates in invalid value.
     */
    public Integer intrinsicBallastFactor; // 20 uint8 RW VM
    /**
     * This attribute shall specify the multiplication factor, as a percentage, to be applied to the configured light
     * output of the lamps. A typical use for this attribute is to compensate for reduction in efficiency over the
     * lifetime of a lamp.
     * ### The light output is given by
     * actual light output &#x3D; configured light output x BallastFactorAdjustment / 100%
     * The range for this attribute is manufacturer dependent. If an attempt is made to set this attribute to a level
     * that cannot be supported, a response shall be returned with status code set to CONSTRAINT_ERROR, and the level
     * shall NOT be changed. The value of null indicates that ballast factor scaling is not in use.
     */
    public Integer ballastFactorAdjustment; // 21 uint8 RW VM
    /**
     * This attribute shall specify the number of lamps connected to this ballast. (Note 1: this number does not take
     * into account whether lamps are actually in their sockets or not).
     */
    public Integer lampQuantity; // 32 uint8 R V
    /**
     * This attribute shall specify the type of lamps (including their wattage) connected to the ballast.
     */
    public String lampType; // 48 string RW VM
    /**
     * This attribute shall specify the name of the manufacturer of the currently connected lamps.
     */
    public String lampManufacturer; // 49 string RW VM
    /**
     * This attribute shall specify the number of hours of use the lamps are rated for by the manufacturer.
     * A value of null indicates an invalid or unknown time.
     */
    public Integer lampRatedHours; // 50 uint24 RW VM
    /**
     * This attribute shall specify the length of time, in hours, the currently connected lamps have been operated,
     * cumulative since the last re-lamping. Burn hours shall NOT be accumulated if the lamps are off.
     * This attribute SHOULD be reset to zero (e.g., remotely) when the lamps are changed. If partially used lamps are
     * connected, LampBurnHours SHOULD be updated to reflect the burn hours of the lamps.
     * A value of null indicates an invalid or unknown time.
     */
    public Integer lampBurnHours; // 51 uint24 RW VM
    /**
     * This attribute shall specify which attributes may cause an alarm notification to be generated. Ain each bit
     * position means that its associated attribute is able to generate an alarm.
     */
    public LampAlarmModeBitmap lampAlarmMode; // 52 LampAlarmModeBitmap RW VM
    /**
     * This attribute shall specify the number of hours the LampBurnHours attribute may reach before an alarm is
     * generated.
     * If the Alarms cluster is not present on the same device this attribute is not used and thus may be omitted (see
     * Dependencies).
     * The Alarm Code field included in the generated alarm shall be 0x01.
     * If this attribute has the value of null, then this alarm shall NOT be generated.
     */
    public Integer lampBurnHoursTripPoint; // 53 uint24 RW VM

    // Bitmaps
    public static class BallastStatusBitmap {
        /**
         * Operational state of the ballast.
         * This bit shall indicate whether the ballast is operational.
         * • 0 &#x3D; The ballast is fully operational
         * • 1 &#x3D; The ballast is not fully operational
         */
        public boolean ballastNonOperational;
        /**
         * Operational state of the lamps.
         * This bit shall indicate whether all lamps is operational.
         * • 0 &#x3D; All lamps are operational
         * • 1 &#x3D; One or more lamp is not in its socket or is faulty
         */
        public boolean lampFailure;

        public BallastStatusBitmap(boolean ballastNonOperational, boolean lampFailure) {
            this.ballastNonOperational = ballastNonOperational;
            this.lampFailure = lampFailure;
        }
    }

    public static class LampAlarmModeBitmap {
        /**
         * State of LampBurnHours alarm generation
         * This bit shall indicate that the LampBurnHours attribute may generate an alarm.
         */
        public boolean lampBurnHours;

        public LampAlarmModeBitmap(boolean lampBurnHours) {
            this.lampBurnHours = lampBurnHours;
        }
    }

    public BallastConfigurationCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 769, "BallastConfiguration");
    }

    protected BallastConfigurationCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "physicalMinLevel : " + physicalMinLevel + "\n";
        str += "physicalMaxLevel : " + physicalMaxLevel + "\n";
        str += "ballastStatus : " + ballastStatus + "\n";
        str += "minLevel : " + minLevel + "\n";
        str += "maxLevel : " + maxLevel + "\n";
        str += "intrinsicBallastFactor : " + intrinsicBallastFactor + "\n";
        str += "ballastFactorAdjustment : " + ballastFactorAdjustment + "\n";
        str += "lampQuantity : " + lampQuantity + "\n";
        str += "lampType : " + lampType + "\n";
        str += "lampManufacturer : " + lampManufacturer + "\n";
        str += "lampRatedHours : " + lampRatedHours + "\n";
        str += "lampBurnHours : " + lampBurnHours + "\n";
        str += "lampAlarmMode : " + lampAlarmMode + "\n";
        str += "lampBurnHoursTripPoint : " + lampBurnHoursTripPoint + "\n";
        return str;
    }
}
