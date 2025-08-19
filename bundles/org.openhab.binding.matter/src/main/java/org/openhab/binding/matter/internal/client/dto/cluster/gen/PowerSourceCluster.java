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
 * PowerSource
 *
 * @author Dan Cunningham - Initial contribution
 */
public class PowerSourceCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x002F;
    public static final String CLUSTER_NAME = "PowerSource";
    public static final String CLUSTER_PREFIX = "powerSource";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_STATUS = "status";
    public static final String ATTRIBUTE_ORDER = "order";
    public static final String ATTRIBUTE_DESCRIPTION = "description";
    public static final String ATTRIBUTE_WIRED_ASSESSED_INPUT_VOLTAGE = "wiredAssessedInputVoltage";
    public static final String ATTRIBUTE_WIRED_ASSESSED_INPUT_FREQUENCY = "wiredAssessedInputFrequency";
    public static final String ATTRIBUTE_WIRED_CURRENT_TYPE = "wiredCurrentType";
    public static final String ATTRIBUTE_WIRED_ASSESSED_CURRENT = "wiredAssessedCurrent";
    public static final String ATTRIBUTE_WIRED_NOMINAL_VOLTAGE = "wiredNominalVoltage";
    public static final String ATTRIBUTE_WIRED_MAXIMUM_CURRENT = "wiredMaximumCurrent";
    public static final String ATTRIBUTE_WIRED_PRESENT = "wiredPresent";
    public static final String ATTRIBUTE_ACTIVE_WIRED_FAULTS = "activeWiredFaults";
    public static final String ATTRIBUTE_BAT_VOLTAGE = "batVoltage";
    public static final String ATTRIBUTE_BAT_PERCENT_REMAINING = "batPercentRemaining";
    public static final String ATTRIBUTE_BAT_TIME_REMAINING = "batTimeRemaining";
    public static final String ATTRIBUTE_BAT_CHARGE_LEVEL = "batChargeLevel";
    public static final String ATTRIBUTE_BAT_REPLACEMENT_NEEDED = "batReplacementNeeded";
    public static final String ATTRIBUTE_BAT_REPLACEABILITY = "batReplaceability";
    public static final String ATTRIBUTE_BAT_PRESENT = "batPresent";
    public static final String ATTRIBUTE_ACTIVE_BAT_FAULTS = "activeBatFaults";
    public static final String ATTRIBUTE_BAT_REPLACEMENT_DESCRIPTION = "batReplacementDescription";
    public static final String ATTRIBUTE_BAT_COMMON_DESIGNATION = "batCommonDesignation";
    public static final String ATTRIBUTE_BAT_ANSI_DESIGNATION = "batAnsiDesignation";
    public static final String ATTRIBUTE_BAT_IEC_DESIGNATION = "batIecDesignation";
    public static final String ATTRIBUTE_BAT_APPROVED_CHEMISTRY = "batApprovedChemistry";
    public static final String ATTRIBUTE_BAT_CAPACITY = "batCapacity";
    public static final String ATTRIBUTE_BAT_QUANTITY = "batQuantity";
    public static final String ATTRIBUTE_BAT_CHARGE_STATE = "batChargeState";
    public static final String ATTRIBUTE_BAT_TIME_TO_FULL_CHARGE = "batTimeToFullCharge";
    public static final String ATTRIBUTE_BAT_FUNCTIONAL_WHILE_CHARGING = "batFunctionalWhileCharging";
    public static final String ATTRIBUTE_BAT_CHARGING_CURRENT = "batChargingCurrent";
    public static final String ATTRIBUTE_ACTIVE_BAT_CHARGE_FAULTS = "activeBatChargeFaults";
    public static final String ATTRIBUTE_ENDPOINT_LIST = "endpointList";

    public Integer clusterRevision; // 65533 ClusterRevision
    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * Indicates the participation of this power source in providing power to the Node as specified in
     * PowerSourceStatusEnum.
     */
    public PowerSourceStatusEnum status; // 0 PowerSourceStatusEnum R V
    /**
     * Indicates the relative preference with which the Node will select this source to provide power. A source with a
     * lower order shall be selected by the Node to provide power before any other source with a higher order, if the
     * lower order source is available (see Status).
     * Note, Order is read-only and therefore NOT intended to allow clients control over power source selection.
     */
    public Integer order; // 1 uint8 R V
    /**
     * This attribute shall provide a user-facing description of this source, used to distinguish it from other power
     * sources, e.g. &quot;DC Power&quot;, &quot;Primary Battery&quot; or &quot;Battery back-up&quot;. This attribute
     * shall NOT be used to convey information such as battery form factor, or chemistry.
     */
    public String description; // 2 string R V
    /**
     * Indicates the assessed RMS or DC voltage currently provided by the hard-wired source, in mV (millivolts). A value
     * of NULL shall indicate the Node is currently unable to assess the value. If the wired source is not connected,
     * but the Node is still able to assess a value, then the assessed value may be reported.
     */
    public Integer wiredAssessedInputVoltage; // 3 uint32 R V
    /**
     * Indicates the assessed frequency of the voltage, currently provided by the hard-wired source, in Hz. A value of
     * NULL shall indicate the Node is currently unable to assess the value. If the wired source is not connected, but
     * the Node is still able to assess a value, then the assessed value may be reported.
     */
    public Integer wiredAssessedInputFrequency; // 4 uint16 R V
    /**
     * Indicates the type of current the Node expects to be provided by the hard-wired source as specified in
     * WiredCurrentTypeEnum.
     */
    public WiredCurrentTypeEnum wiredCurrentType; // 5 WiredCurrentTypeEnum R V
    /**
     * Indicates the assessed instantaneous current draw of the Node on the hard-wired source, in mA (milliamps). A
     * value of NULL shall indicate the Node is currently unable to assess the value. If the wired source is not
     * connected, but the Node is still able to assess a value, then the assessed value may be reported.
     */
    public Integer wiredAssessedCurrent; // 6 uint32 R V
    /**
     * Indicates the nominal voltage, printed as part of the Node’s regulatory compliance label in mV (millivolts),
     * expected to be provided by the hard-wired source.
     */
    public Integer wiredNominalVoltage; // 7 uint32 R V
    /**
     * Indicates the maximum current, printed as part of the Node’s regulatory compliance label in mA (milliamps),
     * expected to be provided by the hard-wired source.
     */
    public Integer wiredMaximumCurrent; // 8 uint32 R V
    /**
     * Indicates if the Node detects that the hard-wired power source is properly connected.
     */
    public Boolean wiredPresent; // 9 bool R V
    /**
     * Indicates the set of wired faults currently detected by the Node on this power source. This set is represented as
     * a list of WiredFaultEnum. When the Node detects a fault has been raised, the appropriate WiredFaultEnum value
     * shall be added to this list, provided it is not already present. This list shall NOT contain more than one
     * instance of a specific WiredFaultEnum value. When the Node detects all conditions contributing to a fault have
     * been cleared, the corresponding WiredFaultEnum value shall be removed from this list. An empty list shall
     * indicate there are currently no active faults. The order of this list SHOULD have no significance. Clients
     * interested in monitoring changes in active faults may subscribe to this attribute, or they may subscribe to
     * WiredFaultChange.
     */
    public List<WiredFaultEnum> activeWiredFaults; // 10 list R V
    /**
     * Indicates the currently measured output voltage of the battery in mV (millivolts). A value of NULL shall indicate
     * the Node is currently unable to assess the value.
     */
    public Integer batVoltage; // 11 uint32 R V
    /**
     * Indicates the estimated percentage of battery charge remaining until the battery will no longer be able to
     * provide power to the Node. Values are expressed in half percent units, ranging from 0 to 200. E.g. a value of 48
     * is equivalent to 24%. A value of NULL shall indicate the Node is currently unable to assess the value.
     * Changes to this attribute shall only be marked as reportable in the following cases:
     * • At most once every 10 seconds, or
     * • When it changes from null to any other value and vice versa.
     * Since reporting consumes power, devices SHOULD be careful not to over-report.
     */
    public Integer batPercentRemaining; // 12 uint8 R V
    /**
     * Indicates the estimated time in seconds before the battery will no longer be able to provide power to the Node. A
     * value of NULL shall indicate the Node is currently unable to assess the value.
     * Changes to this attribute shall only be marked as reportable in the following cases:
     * • At most once every 10 seconds, or
     * • When it changes from null to any other value and vice versa.
     * Since reporting consumes power, devices SHOULD be careful not to over-report.
     */
    public Integer batTimeRemaining; // 13 uint32 R V
    /**
     * Indicates a coarse ranking of the charge level of the battery, used to indicate when intervention is required as
     * specified in BatChargeLevelEnum.
     */
    public BatChargeLevelEnum batChargeLevel; // 14 BatChargeLevelEnum R V
    /**
     * Indicates if the battery needs to be replaced. Replacement may be simple routine maintenance, such as with a
     * single use, non-rechargeable cell. Replacement, however, may also indicate end of life, or serious fault with a
     * rechargeable or even non-replaceable cell.
     */
    public Boolean batReplacementNeeded; // 15 bool R V
    /**
     * This attribute shall indicate the replaceability of the battery as specified in BatReplaceabilityEnum.
     */
    public BatReplaceabilityEnum batReplaceability; // 16 BatReplaceabilityEnum R V
    /**
     * Indicates whether the Node detects that the batteries are properly installed.
     */
    public Boolean batPresent; // 17 bool R V
    /**
     * Indicates the set of battery faults currently detected by the Node on this power source. This set is represented
     * as a list of BatFaultEnum. When the Node detects a fault has been raised, the appropriate BatFaultEnum value
     * shall be added to this list, provided it is not already present. This list shall NOT contain more than one
     * instance of a specific BatFaultEnum value. When the Node detects all conditions contributing to a fault have been
     * cleared, the corresponding BatFaultEnum value shall be removed from this list. An empty list shall indicate there
     * are currently no active faults. The order of this list SHOULD have no significance. Clients interested in
     * monitoring changes in active faults may subscribe to this attribute, or they may subscribe to BatFaultChange.
     */
    public List<BatFaultEnum> activeBatFaults; // 18 list R V
    /**
     * This attribute shall provide a user-facing description of this battery, which SHOULD contain information required
     * to identify a replacement, such as form factor, chemistry or preferred manufacturer.
     */
    public String batReplacementDescription; // 19 string R V
    /**
     * Indicates the ID of the common or colloquial designation of the battery, as specified in
     * BatCommonDesignationEnum.
     */
    public BatCommonDesignationEnum batCommonDesignation; // 20 BatCommonDesignationEnum R V
    /**
     * Indicates the string representing the ANSI designation for the battery as specified in ANSI C18.
     */
    public String batAnsiDesignation; // 21 string R V
    /**
     * Indicates the string representing the IEC designation for the battery as specified in IEC 60086.
     */
    public String batIecDesignation; // 22 string R V
    /**
     * Indicates the ID of the preferred chemistry of the battery source as specified in BatApprovedChemistryEnum.
     */
    public BatApprovedChemistryEnum batApprovedChemistry; // 23 BatApprovedChemistryEnum R V
    /**
     * Indicates the preferred minimum charge capacity rating in mAh of individual, user- or factory-serviceable battery
     * cells or packs in the battery source.
     */
    public Integer batCapacity; // 24 uint32 R V
    /**
     * Indicates the quantity of individual, user- or factory-serviceable battery cells or packs in the battery source.
     */
    public Integer batQuantity; // 25 uint8 R V
    /**
     * Indicates the current state of the battery source with respect to charging as specified in BatChargeStateEnum.
     */
    public BatChargeStateEnum batChargeState; // 26 BatChargeStateEnum R V
    /**
     * Indicates the estimated time in seconds before the battery source will be at full charge. A value of NULL shall
     * indicate the Node is currently unable to assess the value.
     * Changes to this attribute shall only be marked as reportable in the following cases:
     * • At most once every 10 seconds, or
     * • When it changes from null to any other value and vice versa.
     * Since reporting consumes power, devices SHOULD be careful not to over-report.
     */
    public Integer batTimeToFullCharge; // 27 uint32 R V
    /**
     * Indicates whether the Node can remain operational while the battery source is charging.
     */
    public Boolean batFunctionalWhileCharging; // 28 bool R V
    /**
     * Indicates assessed current in mA (milliamps) presently supplied to charge the battery source. A value of NULL
     * shall indicate the Node is currently unable to assess the value.
     */
    public Integer batChargingCurrent; // 29 uint32 R V
    /**
     * Indicates the set of charge faults currently detected by the Node on this power source. This set is represented
     * as a list of BatChargeFaultEnum. When the Node detects a fault has been raised, the appropriate
     * BatChargeFaultEnum value shall be added to this list, provided it is not already present. This list shall NOT
     * contain more than one instance of a specific BatChargeFaultEnum value. When the Node detects all conditions
     * contributing to a fault have been cleared, the corresponding BatChargeFaultEnum value shall be removed from this
     * list. An empty list shall indicate there are currently no active faults. The order of this list SHOULD have no
     * significance. Clients interested in monitoring changes in active faults may subscribe to this attribute, or they
     * may subscribe to the BatFaultChange event.
     */
    public List<BatChargeFaultEnum> activeBatChargeFaults; // 30 list R V
    /**
     * Indicates a list of endpoints that are powered by the source defined by this cluster. Multiple instances of this
     * cluster may list the same endpoint, because it is possible for power for an endpoint to come from multiple
     * sources. In that case the Order attribute indicates their priority.
     * For each power source on a node, there shall only be one instance of this cluster.
     * A cluster instance with an empty list shall indicate that the power source is for the entire node, which includes
     * all endpoints.
     * A cluster instance with a non-empty list shall include the endpoint, upon which the cluster instance resides.
     * The above rules allow that some endpoints can have an unknown power source, and therefore would not be indicated
     * by any instance of this cluster.
     * Typically, there is one power source for the node. Also common is mains power for the node with battery backup
     * power for the node. In both these common cases, for each cluster instance described, the list is empty.
     * A node has a mains power source with Order as 0 (zero), but some application endpoints (not all) have a battery
     * back up source with Order as 1, which means this list is empty for the Power Source cluster associated with the
     * mains power, because it indicates the entire node, but the Power Source cluster instance associated with the
     * battery backup would list the endpoints that have a battery backup.
     */
    public List<Integer> endpointList; // 31 list R V

    // Structs
    /**
     * The WiredFaultChange Event shall be generated when the set of wired faults currently detected by the Node on this
     * wired power source changes. This event shall correspond to a change in value of ActiveWiredFaults.
     */
    public static class WiredFaultChange {
        /**
         * This field shall represent the set of faults currently detected, as per ActiveWiredFaults.
         */
        public List<WiredFaultEnum> current; // list
        /**
         * This field shall represent the set of faults detected prior to this change event, as per ActiveWiredFaults.
         */
        public List<WiredFaultEnum> previous; // list

        public WiredFaultChange(List<WiredFaultEnum> current, List<WiredFaultEnum> previous) {
            this.current = current;
            this.previous = previous;
        }
    }

    /**
     * The BatFaultChange Event shall be generated when the set of battery faults currently detected by the Node on this
     * battery power source changes. This event shall correspond to a change in value of ActiveBatFaults.
     */
    public static class BatFaultChange {
        /**
         * This field shall represent the set of faults currently detected, as per ActiveBatFaults.
         */
        public List<BatFaultEnum> current; // list
        /**
         * This field shall represent the set of faults detected prior to this change event, as per ActiveBatFaults.
         */
        public List<BatFaultEnum> previous; // list

        public BatFaultChange(List<BatFaultEnum> current, List<BatFaultEnum> previous) {
            this.current = current;
            this.previous = previous;
        }
    }

    /**
     * The BatChargeFaultChange Event shall be generated when the set of charge faults currently detected by the Node on
     * this battery power source changes. This event shall correspond to a change in value of ActiveBatChargeFaults.
     */
    public static class BatChargeFaultChange {
        /**
         * This field shall represent the set of faults currently detected, as per ActiveBatChargeFaults.
         */
        public List<BatChargeFaultEnum> current; // list
        /**
         * This field shall represent the set of faults detected prior to this change event, as per
         * ActiveBatChargeFaults.
         */
        public List<BatChargeFaultEnum> previous; // list

        public BatChargeFaultChange(List<BatChargeFaultEnum> current, List<BatChargeFaultEnum> previous) {
            this.current = current;
            this.previous = previous;
        }
    }

    // Enums
    public enum WiredFaultEnum implements MatterEnum {
        UNSPECIFIED(0, "Unspecified"),
        OVER_VOLTAGE(1, "Over Voltage"),
        UNDER_VOLTAGE(2, "Under Voltage");

        public final Integer value;
        public final String label;

        private WiredFaultEnum(Integer value, String label) {
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

    public enum BatFaultEnum implements MatterEnum {
        UNSPECIFIED(0, "Unspecified"),
        OVER_TEMP(1, "Over Temp"),
        UNDER_TEMP(2, "Under Temp");

        public final Integer value;
        public final String label;

        private BatFaultEnum(Integer value, String label) {
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

    public enum BatChargeFaultEnum implements MatterEnum {
        UNSPECIFIED(0, "Unspecified"),
        AMBIENT_TOO_HOT(1, "Ambient Too Hot"),
        AMBIENT_TOO_COLD(2, "Ambient Too Cold"),
        BATTERY_TOO_HOT(3, "Battery Too Hot"),
        BATTERY_TOO_COLD(4, "Battery Too Cold"),
        BATTERY_ABSENT(5, "Battery Absent"),
        BATTERY_OVER_VOLTAGE(6, "Battery Over Voltage"),
        BATTERY_UNDER_VOLTAGE(7, "Battery Under Voltage"),
        CHARGER_OVER_VOLTAGE(8, "Charger Over Voltage"),
        CHARGER_UNDER_VOLTAGE(9, "Charger Under Voltage"),
        SAFETY_TIMEOUT(10, "Safety Timeout");

        public final Integer value;
        public final String label;

        private BatChargeFaultEnum(Integer value, String label) {
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

    public enum PowerSourceStatusEnum implements MatterEnum {
        UNSPECIFIED(0, "Unspecified"),
        ACTIVE(1, "Active"),
        STANDBY(2, "Standby"),
        UNAVAILABLE(3, "Unavailable");

        public final Integer value;
        public final String label;

        private PowerSourceStatusEnum(Integer value, String label) {
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

    public enum WiredCurrentTypeEnum implements MatterEnum {
        AC(0, "Ac"),
        DC(1, "Dc");

        public final Integer value;
        public final String label;

        private WiredCurrentTypeEnum(Integer value, String label) {
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

    public enum BatChargeLevelEnum implements MatterEnum {
        OK(0, "Ok"),
        WARNING(1, "Warning"),
        CRITICAL(2, "Critical");

        public final Integer value;
        public final String label;

        private BatChargeLevelEnum(Integer value, String label) {
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

    public enum BatReplaceabilityEnum implements MatterEnum {
        UNSPECIFIED(0, "Unspecified"),
        NOT_REPLACEABLE(1, "Not Replaceable"),
        USER_REPLACEABLE(2, "User Replaceable"),
        FACTORY_REPLACEABLE(3, "Factory Replaceable");

        public final Integer value;
        public final String label;

        private BatReplaceabilityEnum(Integer value, String label) {
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

    public enum BatCommonDesignationEnum implements MatterEnum {
        UNSPECIFIED(0, "Unspecified"),
        AAA(1, "Aaa"),
        AA(2, "Aa"),
        C(3, "C"),
        D(4, "D"),
        V4V5(5, "4 V 5"),
        V6V0(6, "6 V 0"),
        V9V0(7, "9 V 0"),
        V12AA(8, "12 Aa"),
        AAAA(9, "Aaaa"),
        A(10, "A"),
        B(11, "B"),
        F(12, "F"),
        N(13, "N"),
        NO6(14, "No 6"),
        SUB_C(15, "Sub C"),
        A23(16, "A 23"),
        A27(17, "A 27"),
        BA5800(18, "Ba 5800"),
        DUPLEX(19, "Duplex"),
        V4SR44(20, "4 Sr 44"),
        V523(21, "523"),
        V531(22, "531"),
        V15V0(23, "15 V 0"),
        V22V5(24, "22 V 5"),
        V30V0(25, "30 V 0"),
        V45V0(26, "45 V 0"),
        V67V5(27, "67 V 5"),
        J(28, "J"),
        CR123A(29, "Cr 123 A"),
        CR2(30, "Cr 2"),
        V2CR5(31, "2 Cr 5"),
        CR_P2(32, "Cr P 2"),
        CR_V3(33, "Cr V 3"),
        SR41(34, "Sr 41"),
        SR43(35, "Sr 43"),
        SR44(36, "Sr 44"),
        SR45(37, "Sr 45"),
        SR48(38, "Sr 48"),
        SR54(39, "Sr 54"),
        SR55(40, "Sr 55"),
        SR57(41, "Sr 57"),
        SR58(42, "Sr 58"),
        SR59(43, "Sr 59"),
        SR60(44, "Sr 60"),
        SR63(45, "Sr 63"),
        SR64(46, "Sr 64"),
        SR65(47, "Sr 65"),
        SR66(48, "Sr 66"),
        SR67(49, "Sr 67"),
        SR68(50, "Sr 68"),
        SR69(51, "Sr 69"),
        SR516(52, "Sr 516"),
        SR731(53, "Sr 731"),
        SR712(54, "Sr 712"),
        LR932(55, "Lr 932"),
        A5(56, "A 5"),
        A10(57, "A 10"),
        A13(58, "A 13"),
        A312(59, "A 312"),
        A675(60, "A 675"),
        AC41E(61, "Ac 41 E"),
        V10180(62, "10180"),
        V10280(63, "10280"),
        V10440(64, "10440"),
        V14250(65, "14250"),
        V14430(66, "14430"),
        V14500(67, "14500"),
        V14650(68, "14650"),
        V15270(69, "15270"),
        V16340(70, "16340"),
        RCR123A(71, "Rcr 123 A"),
        V17500(72, "17500"),
        V17670(73, "17670"),
        V18350(74, "18350"),
        V18500(75, "18500"),
        V18650(76, "18650"),
        V19670(77, "19670"),
        V25500(78, "25500"),
        V26650(79, "26650"),
        V32600(80, "32600");

        public final Integer value;
        public final String label;

        private BatCommonDesignationEnum(Integer value, String label) {
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

    public enum BatApprovedChemistryEnum implements MatterEnum {
        UNSPECIFIED(0, "Unspecified"),
        ALKALINE(1, "Alkaline"),
        LITHIUM_CARBON_FLUORIDE(2, "Lithium Carbon Fluoride"),
        LITHIUM_CHROMIUM_OXIDE(3, "Lithium Chromium Oxide"),
        LITHIUM_COPPER_OXIDE(4, "Lithium Copper Oxide"),
        LITHIUM_IRON_DISULFIDE(5, "Lithium Iron Disulfide"),
        LITHIUM_MANGANESE_DIOXIDE(6, "Lithium Manganese Dioxide"),
        LITHIUM_THIONYL_CHLORIDE(7, "Lithium Thionyl Chloride"),
        MAGNESIUM(8, "Magnesium"),
        MERCURY_OXIDE(9, "Mercury Oxide"),
        NICKEL_OXYHYDRIDE(10, "Nickel Oxyhydride"),
        SILVER_OXIDE(11, "Silver Oxide"),
        ZINC_AIR(12, "Zinc Air"),
        ZINC_CARBON(13, "Zinc Carbon"),
        ZINC_CHLORIDE(14, "Zinc Chloride"),
        ZINC_MANGANESE_DIOXIDE(15, "Zinc Manganese Dioxide"),
        LEAD_ACID(16, "Lead Acid"),
        LITHIUM_COBALT_OXIDE(17, "Lithium Cobalt Oxide"),
        LITHIUM_ION(18, "Lithium Ion"),
        LITHIUM_ION_POLYMER(19, "Lithium Ion Polymer"),
        LITHIUM_IRON_PHOSPHATE(20, "Lithium Iron Phosphate"),
        LITHIUM_SULFUR(21, "Lithium Sulfur"),
        LITHIUM_TITANATE(22, "Lithium Titanate"),
        NICKEL_CADMIUM(23, "Nickel Cadmium"),
        NICKEL_HYDROGEN(24, "Nickel Hydrogen"),
        NICKEL_IRON(25, "Nickel Iron"),
        NICKEL_METAL_HYDRIDE(26, "Nickel Metal Hydride"),
        NICKEL_ZINC(27, "Nickel Zinc"),
        SILVER_ZINC(28, "Silver Zinc"),
        SODIUM_ION(29, "Sodium Ion"),
        SODIUM_SULFUR(30, "Sodium Sulfur"),
        ZINC_BROMIDE(31, "Zinc Bromide"),
        ZINC_CERIUM(32, "Zinc Cerium");

        public final Integer value;
        public final String label;

        private BatApprovedChemistryEnum(Integer value, String label) {
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

    public enum BatChargeStateEnum implements MatterEnum {
        UNKNOWN(0, "Unknown"),
        IS_CHARGING(1, "Is Charging"),
        IS_AT_FULL_CHARGE(2, "Is At Full Charge"),
        IS_NOT_CHARGING(3, "Is Not Charging");

        public final Integer value;
        public final String label;

        private BatChargeStateEnum(Integer value, String label) {
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
         * A wired power source
         */
        public boolean wired;
        /**
         * 
         * A battery power source
         */
        public boolean battery;
        /**
         * 
         * A rechargeable battery power source
         */
        public boolean rechargeable;
        /**
         * 
         * A replaceable battery power source
         */
        public boolean replaceable;

        public FeatureMap(boolean wired, boolean battery, boolean rechargeable, boolean replaceable) {
            this.wired = wired;
            this.battery = battery;
            this.rechargeable = rechargeable;
            this.replaceable = replaceable;
        }
    }

    public PowerSourceCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 47, "PowerSource");
    }

    protected PowerSourceCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "featureMap : " + featureMap + "\n";
        str += "status : " + status + "\n";
        str += "order : " + order + "\n";
        str += "description : " + description + "\n";
        str += "wiredAssessedInputVoltage : " + wiredAssessedInputVoltage + "\n";
        str += "wiredAssessedInputFrequency : " + wiredAssessedInputFrequency + "\n";
        str += "wiredCurrentType : " + wiredCurrentType + "\n";
        str += "wiredAssessedCurrent : " + wiredAssessedCurrent + "\n";
        str += "wiredNominalVoltage : " + wiredNominalVoltage + "\n";
        str += "wiredMaximumCurrent : " + wiredMaximumCurrent + "\n";
        str += "wiredPresent : " + wiredPresent + "\n";
        str += "activeWiredFaults : " + activeWiredFaults + "\n";
        str += "batVoltage : " + batVoltage + "\n";
        str += "batPercentRemaining : " + batPercentRemaining + "\n";
        str += "batTimeRemaining : " + batTimeRemaining + "\n";
        str += "batChargeLevel : " + batChargeLevel + "\n";
        str += "batReplacementNeeded : " + batReplacementNeeded + "\n";
        str += "batReplaceability : " + batReplaceability + "\n";
        str += "batPresent : " + batPresent + "\n";
        str += "activeBatFaults : " + activeBatFaults + "\n";
        str += "batReplacementDescription : " + batReplacementDescription + "\n";
        str += "batCommonDesignation : " + batCommonDesignation + "\n";
        str += "batAnsiDesignation : " + batAnsiDesignation + "\n";
        str += "batIecDesignation : " + batIecDesignation + "\n";
        str += "batApprovedChemistry : " + batApprovedChemistry + "\n";
        str += "batCapacity : " + batCapacity + "\n";
        str += "batQuantity : " + batQuantity + "\n";
        str += "batChargeState : " + batChargeState + "\n";
        str += "batTimeToFullCharge : " + batTimeToFullCharge + "\n";
        str += "batFunctionalWhileCharging : " + batFunctionalWhileCharging + "\n";
        str += "batChargingCurrent : " + batChargingCurrent + "\n";
        str += "activeBatChargeFaults : " + activeBatChargeFaults + "\n";
        str += "endpointList : " + endpointList + "\n";
        return str;
    }
}
