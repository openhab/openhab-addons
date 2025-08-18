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
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.matter.internal.client.dto.cluster.ClusterCommand;

/**
 * EnergyEvse
 *
 * @author Dan Cunningham - Initial contribution
 */
public class EnergyEvseCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0099;
    public static final String CLUSTER_NAME = "EnergyEvse";
    public static final String CLUSTER_PREFIX = "energyEvse";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_STATE = "state";
    public static final String ATTRIBUTE_SUPPLY_STATE = "supplyState";
    public static final String ATTRIBUTE_FAULT_STATE = "faultState";
    public static final String ATTRIBUTE_CHARGING_ENABLED_UNTIL = "chargingEnabledUntil";
    public static final String ATTRIBUTE_DISCHARGING_ENABLED_UNTIL = "dischargingEnabledUntil";
    public static final String ATTRIBUTE_CIRCUIT_CAPACITY = "circuitCapacity";
    public static final String ATTRIBUTE_MINIMUM_CHARGE_CURRENT = "minimumChargeCurrent";
    public static final String ATTRIBUTE_MAXIMUM_CHARGE_CURRENT = "maximumChargeCurrent";
    public static final String ATTRIBUTE_MAXIMUM_DISCHARGE_CURRENT = "maximumDischargeCurrent";
    public static final String ATTRIBUTE_USER_MAXIMUM_CHARGE_CURRENT = "userMaximumChargeCurrent";
    public static final String ATTRIBUTE_RANDOMIZATION_DELAY_WINDOW = "randomizationDelayWindow";
    public static final String ATTRIBUTE_NEXT_CHARGE_START_TIME = "nextChargeStartTime";
    public static final String ATTRIBUTE_NEXT_CHARGE_TARGET_TIME = "nextChargeTargetTime";
    public static final String ATTRIBUTE_NEXT_CHARGE_REQUIRED_ENERGY = "nextChargeRequiredEnergy";
    public static final String ATTRIBUTE_NEXT_CHARGE_TARGET_SO_C = "nextChargeTargetSoC";
    public static final String ATTRIBUTE_APPROXIMATE_EV_EFFICIENCY = "approximateEvEfficiency";
    public static final String ATTRIBUTE_STATE_OF_CHARGE = "stateOfCharge";
    public static final String ATTRIBUTE_BATTERY_CAPACITY = "batteryCapacity";
    public static final String ATTRIBUTE_VEHICLE_ID = "vehicleId";
    public static final String ATTRIBUTE_SESSION_ID = "sessionId";
    public static final String ATTRIBUTE_SESSION_DURATION = "sessionDuration";
    public static final String ATTRIBUTE_SESSION_ENERGY_CHARGED = "sessionEnergyCharged";
    public static final String ATTRIBUTE_SESSION_ENERGY_DISCHARGED = "sessionEnergyDischarged";

    public Integer clusterRevision; // 65533 ClusterRevision
    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * Indicates the current status of the EVSE. This higher-level status is partly derived from the signaling protocol
     * as communicated between the EVSE and the vehicle through the pilot signal.
     * The State attribute shall change when the EVSE detects change of condition of the EV (plugged in or unplugged,
     * whether the vehicle is asking for demand or not, and if it is charging or discharging).
     * &gt; [!NOTE]
     * &gt; SessionEnding is not really a state but a transition. However, the transition period may take a few seconds
     * and is useful for some clean up purposes.
     * The Fault state is used to indicate that the FaultState attribute is not NoError. A null value shall indicate
     * that the state cannot be determined.
     */
    public StateEnum state; // 0 StateEnum R V
    /**
     * Indicates whether the EV is currently allowed to charge from or discharge to the EVSE.
     */
    public SupplyStateEnum supplyState; // 1 SupplyStateEnum R V
    /**
     * Indicates the type of fault detected by the EVSE (internally or as detected in the pilot signal).
     * When the SupplyState attribute is DisabledError, the FaultState attribute will be one of the values listed in
     * FaultStateEnum, except NoError. For all values of SupplyState other than DisabledError, the FaultState attribute
     * shall be NoError.
     */
    public FaultStateEnum faultState; // 2 FaultStateEnum R V
    /**
     * Indicates the time, in UTC, that the EVSE will automatically stop current flow to the EV.
     * A null value indicates the EVSE is always enabled for charging.
     * A value in the past or 0x0 indicates that EVSE charging shall be disabled. The attribute is only set via the
     * payload of the EnableCharging command.
     * This attribute shall be persisted, for example a temporary power failure should not stop the vehicle from being
     * charged.
     */
    public Integer chargingEnabledUntil; // 3 epoch-s R V
    /**
     * Indicates the time, in UTC, that the EVSE will automatically stop current flow from the EV.
     * A null value indicates the EVSE is always enabled for discharging.
     * A value in the past or 0x0 indicates that EVSE discharging shall be disabled. The attribute is only set via the
     * payload of the EnableDischarging command.
     * This attribute shall be persisted, for example a temporary power failure should not stop the vehicle from being
     * discharged.
     */
    public Integer dischargingEnabledUntil; // 4 epoch-s R V
    /**
     * Indicates the capacity that the circuit that the EVSE is connected to can provide. It is intended to allow
     * implementation of a self-managed network of EVSEs. It is assumed that the device will allow the setting of such
     * values by an installer.
     */
    public BigInteger circuitCapacity; // 5 amperage-mA R V
    /**
     * Indicates the minimum current that can be delivered by the EVSE to the EV. The attribute can be set using the
     * EnableCharging command.
     */
    public BigInteger minimumChargeCurrent; // 6 amperage-mA R V
    /**
     * Indicates the maximum current that can be delivered by the EVSE to the EV.
     * This shall represent the actual maximum current offered to the EV at any time. Note that the EV can draw less
     * current than this value. For example, the EV may be limiting its power draw based on the operating conditions of
     * the battery, such as temperature and state of charge.
     * The attribute can be initially set using the EnableCharging command or by adjusting the UserMaximumChargeCurrent
     * attribute.
     * This attribute value shall be the minimum of:
     * • CircuitCapacity - Electrician’s installation setting
     * • CableAssemblyCurrentLimit (detected by the EVSE when the cable is plugged in)
     * • MaximumChargeCurrent field in the EnableCharging command
     * • UserMaximumChargeCurrent attribute
     */
    public BigInteger maximumChargeCurrent; // 7 amperage-mA R V
    /**
     * Indicates the maximum current that can be received by the EVSE from the EV. This attribute can be set using the
     * EnableDischarging command.
     * This attribute value shall be the minimum of:
     * • CircuitCapacity - Electrician’s installation setting
     * • CableAssemblyCurrentLimit (detected by the EVSE when the cable is plugged in)
     * • MaximumDischargeCurrent field in the EnableDischarging command
     */
    public BigInteger maximumDischargeCurrent; // 8 amperage-mA R V
    /**
     * Indicates a maximum current that can set by the consumer (e.g. via an app) as a preference to further reduce the
     * charging rate. This may be desirable if the home owner has a solar PV or battery storage system which may only be
     * able to deliver a limited amount of power. The consumer can manually control how much they allow the EV to take.
     * This attribute value shall be limited by the EVSE to be in the range of:
     * MinimumChargeCurrent &lt;&#x3D; UserMaximumChargeCurrent &lt;&#x3D; MaximumChargeCurrent where
     * MinimumChargeCurrent and MaximumChargeCurrent are the values received in the EnableCharging command.
     * Its default value SHOULD be initialized to the same as the CircuitCapacity attribute. This value shall be
     * persisted across reboots to ensure it does not cause charging issues during temporary power failures.
     */
    public BigInteger userMaximumChargeCurrent; // 9 amperage-mA RW VM
    /**
     * Indicates the size of a random window over which the EVSE will randomize the start of a charging session. This
     * value is in seconds.
     * This is a feature that is mandated in some markets (such as UK) where the EVSE should by default randomize its
     * start time within the randomization window. By default in the UK this should be 600s.
     * For example, if the RandomizationDelayWindow is 600s (i.e. 10 minutes) and if there was a cheap rate energy
     * starting at 00:30, then the EVSE must compute a random delay between 0-599s and add this to its initial planned
     * start time.
     */
    public Integer randomizationDelayWindow; // 10 elapsed-s RW VM
    /**
     * Indicates the time, in UTC, when the EVSE plans to start the next scheduled charge based on the charging
     * preferences.
     * A null value indicates that there is no scheduled charging (for example, the EVSE Mode is set to use Manual mode
     * tag), or that the vehicle is not plugged in with the SupplyState indicating that charging is enabled.
     */
    public Integer nextChargeStartTime; // 35 epoch-s R V
    /**
     * Indicates the time, in UTC, when the EVSE SHOULD complete the next scheduled charge based on the charging
     * preferences.
     * A null value indicates that there is no scheduled charging (for example, the EVSE Mode is set to use Manual mode
     * tag), or that the vehicle is not plugged in with the SupplyState indicating that charging is enabled.
     */
    public Integer nextChargeTargetTime; // 36 epoch-s R V
    /**
     * Indicates the amount of energy that the EVSE is going to attempt to add to the vehicle in the next charging
     * target.
     * A null value indicates that there is no scheduled charging (for example, the EVSE Mode is set to use Manual mode
     * tag), or that the vehicle is not plugged in with the SupplyState indicating that charging is enabled, or that the
     * next ChargingTargetStruct is using the TargetSoC value to charge the vehicle.
     */
    public BigInteger nextChargeRequiredEnergy; // 37 energy-mWh R V
    /**
     * Indicates the target SoC the EVSE is going to attempt to reach when the vehicle is next charged.
     * A null value indicates that there is no scheduled charging (for example, the EVSE Mode is set to use Manual mode
     * tag), or that the vehicle is not plugged in with the SupplyState indicating that charging is enabled, or that the
     * next ChargingTargetStruct is using the AddedEnergy value to charge the vehicle.
     * If the SOC feature is not supported, only the values null and 100% are supported.
     */
    public Integer nextChargeTargetSoC; // 38 percent R V
    /**
     * Indicates the vehicle efficiency rating for a connected vehicle.
     * This can be used to help indicate to the user approximately how many miles or km of range will be added. It
     * allows user interfaces to display to the user simpler terms that they can relate to compared to kWh.
     * This is value is stored in km per kWh multiplied by a scaling factor of 1000.
     * A null value indicates that the EV efficiency is unknown and the NextChargeRequiredEnergy attribute cannot be
     * converted from Wh to miles or km.
     * To convert from Wh into Range:
     * AddedRange (km) &#x3D; AddedEnergy (Wh) x ApproxEVEfficiency (km/kWh x 1000) AddedRange (Miles) &#x3D;
     * AddedEnergy (Wh) x ApproxEVEfficiency (km/kWh x 1000) x 0.6213
     * Example:
     * ApproxEVEfficiency (km/kWh x 1000): 4800 (i.e. 4.8km/kWh x 1000)
     * ### AddedEnergy (Wh): 10,000
     * AddedRange (km) &#x3D; 10,000 x 4800 / 1,000,000 &#x3D; 48 km
     * AddedRange (Miles) &#x3D; AddedEnergy (Wh) x ApproxEVEfficiency (km/kWh x 1000) x
     * 0.6213
     * &#x3D; 29.82 Miles
     */
    public Integer approximateEvEfficiency; // 39 uint16 RW VM
    /**
     * Indicates the state of charge of the EV battery in steps of 1%. The values are in the 0-100%. This attribute is
     * only available on EVSEs which can read the state of charge from the vehicle and that support the SOC feature. If
     * the StateOfCharge cannot be read from the vehicle it shall be returned with a NULL value.
     */
    public Integer stateOfCharge; // 48 percent R V
    /**
     * Indicates the capacity of the EV battery in mWh. This value is always positive.
     */
    public BigInteger batteryCapacity; // 49 energy-mWh R V
    /**
     * Indicates the vehicle ID read by the EVSE via ISO-15118 using the PNC feature, if the EVSE supports this
     * capability.
     * The field may be based on the e-Mobility Account Identifier (EMAID). A null value shall indicate that this is
     * unknown.
     */
    public String vehicleId; // 50 string R V
    public Integer sessionId; // 64 uint32 R V
    public Integer sessionDuration; // 65 elapsed-s R V
    public BigInteger sessionEnergyCharged; // 66 energy-mWh R V
    public BigInteger sessionEnergyDischarged; // 67 energy-mWh R V

    // Structs
    /**
     * This event shall be generated when the EV is plugged in.
     */
    public static class EvConnected {
        /**
         * This is the new session ID created after the vehicle is plugged in.
         */
        public Integer sessionId; // uint32

        public EvConnected(Integer sessionId) {
            this.sessionId = sessionId;
        }
    }

    /**
     * This event shall be generated when the EV is unplugged or not detected (having been previously plugged in). When
     * the vehicle is unplugged then the session is ended.
     */
    public static class EvNotDetected {
        /**
         * This field shall indicate the current value of the SessionID attribute.
         */
        public Integer sessionId; // uint32
        /**
         * This field shall indicate the value of the State attribute prior to the EV not being detected.
         */
        public StateEnum state; // StateEnum
        /**
         * This field shall indicate the total duration of the session, from the start of the session when the EV was
         * plugged in, until it was unplugged.
         */
        public Integer sessionDuration; // elapsed-s
        /**
         * This field shall indicate the total amount of energy transferred from the EVSE to the EV during the session.
         * Note that if bi-directional charging occurs during the session, then this value shall only include the sum of
         * energy transferred from the EVSE to the EV, and shall NOT be a net value of charging and discharging energy.
         */
        public BigInteger sessionEnergyCharged; // energy-mWh
        /**
         * This field shall indicate the total amount of energy transferred from the EV to the EVSE during the session.
         * Note that if bi-directional discharging occurs during the session, then this value shall only include the sum
         * of energy transferred from the EV to the EVSE, and shall NOT be a net value of charging and discharging
         * energy.
         */
        public BigInteger sessionEnergyDischarged; // energy-mWh

        public EvNotDetected(Integer sessionId, StateEnum state, Integer sessionDuration,
                BigInteger sessionEnergyCharged, BigInteger sessionEnergyDischarged) {
            this.sessionId = sessionId;
            this.state = state;
            this.sessionDuration = sessionDuration;
            this.sessionEnergyCharged = sessionEnergyCharged;
            this.sessionEnergyDischarged = sessionEnergyDischarged;
        }
    }

    /**
     * This event shall be generated whenever the EV starts charging or discharging, except when an EV has switched
     * between charging and discharging under the control of the PowerAdjustment feature of the Device Energy Management
     * cluster of the associated Device Energy Management device.
     */
    public static class EnergyTransferStarted {
        /**
         * This field shall indicate the value of the SessionID attribute at the time the event was generated.
         */
        public Integer sessionId; // uint32
        /**
         * This field shall indicate the value of the State attribute at the time the event was generated.
         */
        public StateEnum state; // StateEnum
        /**
         * This field shall indicate the value of the maximum charging current at the time the event was generated.
         * A non-zero value indicates that the EV has been enabled for charging and the value is taken directly from the
         * MaximumChargeCurrent attribute. A zero value indicates that the EV has not been enabled for charging.
         */
        public BigInteger maximumCurrent; // amperage-mA
        /**
         * This field shall indicate the value of the maximum discharging current at the time the event was generated.
         * A non-zero value indicates that the EV has been enabled for discharging and the value is taken directly from
         * the MaximumDischargeCurrent attribute. A zero value indicates that the EV has not been enabled for
         * discharging.
         */
        public BigInteger maximumDischargeCurrent; // amperage-mA

        public EnergyTransferStarted(Integer sessionId, StateEnum state, BigInteger maximumCurrent,
                BigInteger maximumDischargeCurrent) {
            this.sessionId = sessionId;
            this.state = state;
            this.maximumCurrent = maximumCurrent;
            this.maximumDischargeCurrent = maximumDischargeCurrent;
        }
    }

    /**
     * This event shall be generated whenever the EV stops charging or discharging, except when an EV has switched
     * between charging and discharging under the control of the PowerAdjustment feature of the Device Energy Management
     * cluster of the associated Device Energy Management device.
     */
    public static class EnergyTransferStopped {
        /**
         * This field shall indicate the value of the SessionID attribute prior to the energy transfer stopping.
         */
        public Integer sessionId; // uint32
        /**
         * This field shall indicate the value of the State attribute prior to the energy transfer stopping.
         */
        public StateEnum state; // StateEnum
        /**
         * This field shall indicate the reason why the energy transferred stopped.
         */
        public EnergyTransferStoppedReasonEnum reason; // EnergyTransferStoppedReasonEnum
        /**
         * This field shall indicate the amount of energy transferred from the EVSE to the EV since the previous
         * EnergyTransferStarted event, in mWh.
         */
        public BigInteger energyTransferred; // energy-mWh
        /**
         * This field shall indicate the amount of energy transferred from the EV to the EVSE since the previous
         * EnergyTransferStarted event, in mWh.
         */
        public BigInteger energyDischarged; // energy-mWh

        public EnergyTransferStopped(Integer sessionId, StateEnum state, EnergyTransferStoppedReasonEnum reason,
                BigInteger energyTransferred, BigInteger energyDischarged) {
            this.sessionId = sessionId;
            this.state = state;
            this.reason = reason;
            this.energyTransferred = energyTransferred;
            this.energyDischarged = energyDischarged;
        }
    }

    /**
     * If the EVSE detects a fault it shall generate a Fault Event. The SupplyState attribute shall be set to
     * DisabledError and the type of fault detected by the EVSE shall be stored in the FaultState attribute.
     * This event shall be generated when the FaultState changes from any error state. i.e. if it changes from NoError
     * to any other state and if the error then clears, this would generate 2 events.
     * It is assumed that the fault will be cleared locally on the EVSE device. When all faults have been cleared, the
     * EVSE device shall set the FaultState attribute to NoError and the SupplyState attribute shall be set back to its
     * previous state.
     */
    public static class Fault {
        /**
         * This field shall indicate the value of the SessionID attribute prior to the Fault State being changed. A
         * value of null indicates no sessions have occurred before the fault occurred.
         */
        public Integer sessionId; // uint32
        /**
         * This field shall indicate the value of the State attribute prior to the Fault State being changed.
         */
        public StateEnum state; // StateEnum
        /**
         * This field shall indicate the value of the FaultState attribute prior to the Fault State being changed.
         */
        public FaultStateEnum faultStatePreviousState; // FaultStateEnum
        /**
         * This field shall indicate the current value of the FaultState attribute.
         */
        public FaultStateEnum faultStateCurrentState; // FaultStateEnum

        public Fault(Integer sessionId, StateEnum state, FaultStateEnum faultStatePreviousState,
                FaultStateEnum faultStateCurrentState) {
            this.sessionId = sessionId;
            this.state = state;
            this.faultStatePreviousState = faultStatePreviousState;
            this.faultStateCurrentState = faultStateCurrentState;
        }
    }

    /**
     * This event shall be generated when a RFID card has been read. This allows a controller to register the card ID
     * and use this to authenticate and start the charging session.
     */
    public static class Rfid {
        /**
         * The UID field (ISO 14443A UID) is either 4, 7 or 10 bytes.
         */
        public OctetString uid; // octstr

        public Rfid(OctetString uid) {
            this.uid = uid;
        }
    }

    /**
     * This represents a single user specified charging target for an EV.
     * An EVSE or EMS system optimizer may use this information to take the Time of Use Tariff, grid carbon intensity,
     * local generation (solar PV) into account to provide the cheapest and cleanest energy to the EV.
     * The optimization strategy is not defined here, however in simple terms, the AddedEnergy requirement can be
     * fulfilled by knowing the charging Power (W) and the time needed to charge.
     * To compute the Charging Time: Required Energy (Wh) &#x3D; Power (W) x ChargingTime (s) / 3600 Therefore:
     * ChargingTime (s) &#x3D; (3600 x RequiredEnergy (wH)) / Power (W)
     * To compute the charging time: Charging StartTime &#x3D; TargetTimeMinutesPastMidnight - ChargingTime
     */
    public static class ChargingTargetStruct {
        /**
         * This field shall indicate the desired charging completion time of the associated day. The time will be
         * represented by a 16 bits unsigned integer to designate the minutes since midnight. For example, 6am will be
         * represented by 360 minutes since midnight and 11:30pm will be represented by 1410 minutes since midnight.
         * This field is based on local wall clock time. In case of Daylight Savings Time transition which may result in
         * an extra hour or one hour less in the day, the charging algorithm should take into account the shift
         * appropriately.
         * Note that if the TargetTimeMinutesPastMidnight values are too close together (e.g. 2 per day) these may
         * overlap. The EVSE may have to coalesce the charging targets into a single target. e.g. if the 1st charging
         * target cannot be met in the time available, the EVSE may be forced to begin working towards the 2nd charging
         * target and immediately continue until both targets have been satisfied (or the vehicle becomes full).
         * The EVSE itself cannot predict the behavior of the vehicle (i.e. if it cannot obtain the SoC from the
         * vehicle), so should attempt to perform a sensible operation based on these targets. It is recommended that
         * the charging schedule is pessimistic (i.e. starts earlier) since the vehicle may charge more slowly than the
         * electrical supply may provide power (especially if it is cold).
         * If the user configures large charging targets (e.g. high values of AddedEnergy or SoC) then it is expected
         * that the EVSE may need to begin charging immediately, and may not be able to guarantee that the vehicle will
         * be able to reach the target.
         */
        public Integer targetTimeMinutesPastMidnight; // uint16
        /**
         * This field represents the target SoC that the vehicle should be charged to before the
         * TargetTimeMinutesPastMidnight.
         * If the EVSE supports the SOC feature and can obtain the SoC of the vehicle:
         * • the TargetSoC field shall take precedence over the AddedEnergy field.
         * • the EVSE SHOULD charge to the TargetSoC and then stop the charging automatically when it reaches that
         * point.
         * • if the TargetSoC value is set to 100% then the EVSE SHOULD continue to charge the vehicle until the vehicle
         * decides to stop charging.
         * If the EVSE does not support the SOC feature or cannot obtain the SoC of the vehicle:
         * • the AddedEnergy field shall take precedence over the TargetSoC field, and if the EVSE does not support the
         * SOC feature then the TargetSoC field may only take the values null or 100%.
         * • if the AddedEnergy field has not been provided, the EVSE SHOULD assume the vehicle is empty and charge
         * until the vehicle stops demanding a charge.
         */
        public Integer targetSoC; // percent
        /**
         * This field represents the amount of energy that the user would like to have added to the vehicle before the
         * TargetTimeMinutesPastMidnight.
         * This represents a positive value in mWh that SHOULD be added during the session (i.e. if the vehicle charging
         * is stopped and started several times, this equates to the total energy since the vehicle has been plugged
         * in).
         * The maximum value (500kWh) is much larger than most EV batteries on the market today. If the client tries to
         * set this value too high then the EVSE will need to start charging immediately and continue charging until the
         * vehicle stops demanding charge (i.e. it is full). Therefore the maximum value should be set based on typical
         * battery size of the vehicles on the market (e.g. 70000Wh), however this is up to the client to carefully
         * choose a value.
         * &gt; [!NOTE]
         * &gt; If the EVSE can obtain the Battery Capacity of the vehicle, it SHOULD NOT limit this AddedEnergy value
         * to the Battery Capacity of the vehicle, since the EV may also require energy for heating and cooling of the
         * battery during charging, or for heating or cooling the cabin.
         */
        public BigInteger addedEnergy; // energy-mWh

        public ChargingTargetStruct(Integer targetTimeMinutesPastMidnight, Integer targetSoC, BigInteger addedEnergy) {
            this.targetTimeMinutesPastMidnight = targetTimeMinutesPastMidnight;
            this.targetSoC = targetSoC;
            this.addedEnergy = addedEnergy;
        }
    }

    /**
     * This represents a set of user specified charging targets for an EV for a set of specified days.
     */
    public static class ChargingTargetScheduleStruct {
        /**
         * This field shall indicate the days of the week that the charging targets SHOULD be associated to. This field
         * is a bitmap and therefore the associated targets could be applied to multiple days.
         */
        public TargetDayOfWeekBitmap dayOfWeekForSequence; // TargetDayOfWeekBitmap
        /**
         * This field shall indicate a list of up to 10 charging targets for each of the associated days of the week.
         */
        public List<ChargingTargetStruct> chargingTargets; // list

        public ChargingTargetScheduleStruct(TargetDayOfWeekBitmap dayOfWeekForSequence,
                List<ChargingTargetStruct> chargingTargets) {
            this.dayOfWeekForSequence = dayOfWeekForSequence;
            this.chargingTargets = chargingTargets;
        }
    }

    // Enums
    public enum StateEnum implements MatterEnum {
        NOT_PLUGGED_IN(0, "Not Plugged In"),
        PLUGGED_IN_NO_DEMAND(1, "Plugged In No Demand"),
        PLUGGED_IN_DEMAND(2, "Plugged In Demand"),
        PLUGGED_IN_CHARGING(3, "Plugged In Charging"),
        PLUGGED_IN_DISCHARGING(4, "Plugged In Discharging"),
        SESSION_ENDING(5, "Session Ending"),
        FAULT(6, "Fault");

        public final Integer value;
        public final String label;

        private StateEnum(Integer value, String label) {
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

    public enum SupplyStateEnum implements MatterEnum {
        DISABLED(0, "Disabled"),
        CHARGING_ENABLED(1, "Charging Enabled"),
        DISCHARGING_ENABLED(2, "Discharging Enabled"),
        DISABLED_ERROR(3, "Disabled Error"),
        DISABLED_DIAGNOSTICS(4, "Disabled Diagnostics"),
        ENABLED(5, "Enabled");

        public final Integer value;
        public final String label;

        private SupplyStateEnum(Integer value, String label) {
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

    public enum FaultStateEnum implements MatterEnum {
        NO_ERROR(0, "No Error"),
        METER_FAILURE(1, "Meter Failure"),
        OVER_VOLTAGE(2, "Over Voltage"),
        UNDER_VOLTAGE(3, "Under Voltage"),
        OVER_CURRENT(4, "Over Current"),
        CONTACT_WET_FAILURE(5, "Contact Wet Failure"),
        CONTACT_DRY_FAILURE(6, "Contact Dry Failure"),
        GROUND_FAULT(7, "Ground Fault"),
        POWER_LOSS(8, "Power Loss"),
        POWER_QUALITY(9, "Power Quality"),
        PILOT_SHORT_CIRCUIT(10, "Pilot Short Circuit"),
        EMERGENCY_STOP(11, "Emergency Stop"),
        EV_DISCONNECTED(12, "Ev Disconnected"),
        WRONG_POWER_SUPPLY(13, "Wrong Power Supply"),
        LIVE_NEUTRAL_SWAP(14, "Live Neutral Swap"),
        OVER_TEMPERATURE(15, "Over Temperature"),
        OTHER(255, "Other");

        public final Integer value;
        public final String label;

        private FaultStateEnum(Integer value, String label) {
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

    public enum EnergyTransferStoppedReasonEnum implements MatterEnum {
        EV_STOPPED(0, "Ev Stopped"),
        EVSE_STOPPED(1, "Evse Stopped"),
        OTHER(2, "Other");

        public final Integer value;
        public final String label;

        private EnergyTransferStoppedReasonEnum(Integer value, String label) {
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
    public static class TargetDayOfWeekBitmap {
        public boolean sunday;
        public boolean monday;
        public boolean tuesday;
        public boolean wednesday;
        public boolean thursday;
        public boolean friday;
        public boolean saturday;

        public TargetDayOfWeekBitmap(boolean sunday, boolean monday, boolean tuesday, boolean wednesday,
                boolean thursday, boolean friday, boolean saturday) {
            this.sunday = sunday;
            this.monday = monday;
            this.tuesday = tuesday;
            this.wednesday = wednesday;
            this.thursday = thursday;
            this.friday = friday;
            this.saturday = saturday;
        }
    }

    public static class FeatureMap {
        /**
         * 
         * Since some EVSEs cannot obtain the SoC from the vehicle, some EV charging solutions allow the consumer to
         * specify a daily charging target (for adding energy to the EV’s battery). This feature allows the consumer to
         * specify how many miles or km of additional range they need for their typical daily commute. This range
         * requirement can be converted into a daily energy demand with a target charging completion time.
         * The EVSE itself can use this information (or may allow a controller such as an EMS) to compute an optimized
         * charging schedule.
         * An EVSE device which includes a Device Energy Management device with the Device Energy Management cluster PFR
         * (Power Forecast Reporting) feature can use the charging preferences information to produce its power
         * forecast.
         * EVSE devices that support the Device Energy Management cluster’s FA feature can have their charging profiles
         * set by a controller device such as an EMS. For example, if the EVSE advertises a simple power forecast which
         * allows the EMS to adjust over a wide range of power and time durations, then the EVSE may allow the EMS to
         * propose a revised optimized forecast (which is the charging profile). For example, a solar PV ESA may also
         * share its Forecast, so enabling an EMS to adjust the EVSE forecast to the best time to charge so that any
         * excess solar generation is used to charge the EV.
         * See the Device Energy Management Cluster for more details.
         */
        public boolean chargingPreferences;
        /**
         * 
         * Vehicles and EVSEs which support ISO 15118 may allow the vehicle to report its battery size and state of
         * charge. If the EVSE supports PLC it may have a vehicle connected which optionally supports reporting of its
         * battery size and current State of Charge (SoC).
         * If the EVSE supports reporting of State of Charge this feature will only work if a compatible EV is
         * connected.
         * Note some EVSEs may use other undefined mechanisms to obtain vehicle State of Charge outside the scope of
         * this cluster.
         */
        public boolean soCReporting;
        /**
         * 
         * If the EVSE supports PLC, it may be able to support the Plug and Charge feature. e.g. this may allow the
         * vehicle ID to be obtained which may allow an energy management system to track energy usage per vehicle (e.g.
         * to give the owner an indicative cost of charging, or for work place charging).
         * If the EVSE supports the Plug and Charge feature, it will only work if a compatible EV is connected.
         */
        public boolean plugAndCharge;
        /**
         * 
         * If the EVSE is fitted with an RFID reader, it may be possible to obtain the User or Vehicle ID from an RFID
         * card. This may be used to record a charging session against a specific charging account, and may optionally
         * be used to authorize a charging session.
         * An RFID event can be generated when a user taps an RFID card onto the RFID reader. The event must be
         * subscribed to by the EVSE Management cluster client. This client may use this to enable the EV to charge or
         * discharge. The lookup and authorization of RIFD UID is outside the scope of this cluster.
         */
        public boolean rfid;
        /**
         * 
         * If the EVSE can support bi-directional charging, it may be possible to request that the vehicle can discharge
         * to the home or grid.
         * The charging and discharging may be controlled by a home Energy Management System (EMS) using the Device
         * Energy Management cluster of the associated Device Energy Management device. For example, an EMS may use the
         * PA (Power Adjustment) feature to continually adjust the charge/discharge current to/from the EV so as to
         * minimise the energy flow from/to the grid as the demand in the home and the solar supply to the home both
         * fluctuate.
         */
        public boolean v2X;

        public FeatureMap(boolean chargingPreferences, boolean soCReporting, boolean plugAndCharge, boolean rfid,
                boolean v2X) {
            this.chargingPreferences = chargingPreferences;
            this.soCReporting = soCReporting;
            this.plugAndCharge = plugAndCharge;
            this.rfid = rfid;
            this.v2X = v2X;
        }
    }

    public EnergyEvseCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 153, "EnergyEvse");
    }

    protected EnergyEvseCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * Allows a client to disable the EVSE from charging and discharging.
     */
    public static ClusterCommand disable() {
        return new ClusterCommand("disable");
    }

    /**
     * This command allows a client to enable the EVSE to charge an EV, and to provide or update the maximum and minimum
     * charge current.
     */
    public static ClusterCommand enableCharging(Integer chargingEnabledUntil, BigInteger minimumChargeCurrent,
            BigInteger maximumChargeCurrent) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (chargingEnabledUntil != null) {
            map.put("chargingEnabledUntil", chargingEnabledUntil);
        }
        if (minimumChargeCurrent != null) {
            map.put("minimumChargeCurrent", minimumChargeCurrent);
        }
        if (maximumChargeCurrent != null) {
            map.put("maximumChargeCurrent", maximumChargeCurrent);
        }
        return new ClusterCommand("enableCharging", map);
    }

    /**
     * Upon receipt, this shall allow a client to enable the discharge of an EV, and to provide or update the maximum
     * discharge current.
     */
    public static ClusterCommand enableDischarging(Integer dischargingEnabledUntil,
            BigInteger maximumDischargeCurrent) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (dischargingEnabledUntil != null) {
            map.put("dischargingEnabledUntil", dischargingEnabledUntil);
        }
        if (maximumDischargeCurrent != null) {
            map.put("maximumDischargeCurrent", maximumDischargeCurrent);
        }
        return new ClusterCommand("enableDischarging", map);
    }

    /**
     * Allows a client to put the EVSE into a self-diagnostics mode.
     */
    public static ClusterCommand startDiagnostics() {
        return new ClusterCommand("startDiagnostics");
    }

    /**
     * Allows a client to set the user specified charging targets.
     */
    public static ClusterCommand setTargets(List<ChargingTargetScheduleStruct> chargingTargetSchedules) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (chargingTargetSchedules != null) {
            map.put("chargingTargetSchedules", chargingTargetSchedules);
        }
        return new ClusterCommand("setTargets", map);
    }

    /**
     * Allows a client to retrieve the current set of charging targets.
     */
    public static ClusterCommand getTargets() {
        return new ClusterCommand("getTargets");
    }

    /**
     * Allows a client to clear all stored charging targets.
     */
    public static ClusterCommand clearTargets() {
        return new ClusterCommand("clearTargets");
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "featureMap : " + featureMap + "\n";
        str += "state : " + state + "\n";
        str += "supplyState : " + supplyState + "\n";
        str += "faultState : " + faultState + "\n";
        str += "chargingEnabledUntil : " + chargingEnabledUntil + "\n";
        str += "dischargingEnabledUntil : " + dischargingEnabledUntil + "\n";
        str += "circuitCapacity : " + circuitCapacity + "\n";
        str += "minimumChargeCurrent : " + minimumChargeCurrent + "\n";
        str += "maximumChargeCurrent : " + maximumChargeCurrent + "\n";
        str += "maximumDischargeCurrent : " + maximumDischargeCurrent + "\n";
        str += "userMaximumChargeCurrent : " + userMaximumChargeCurrent + "\n";
        str += "randomizationDelayWindow : " + randomizationDelayWindow + "\n";
        str += "nextChargeStartTime : " + nextChargeStartTime + "\n";
        str += "nextChargeTargetTime : " + nextChargeTargetTime + "\n";
        str += "nextChargeRequiredEnergy : " + nextChargeRequiredEnergy + "\n";
        str += "nextChargeTargetSoC : " + nextChargeTargetSoC + "\n";
        str += "approximateEvEfficiency : " + approximateEvEfficiency + "\n";
        str += "stateOfCharge : " + stateOfCharge + "\n";
        str += "batteryCapacity : " + batteryCapacity + "\n";
        str += "vehicleId : " + vehicleId + "\n";
        str += "sessionId : " + sessionId + "\n";
        str += "sessionDuration : " + sessionDuration + "\n";
        str += "sessionEnergyCharged : " + sessionEnergyCharged + "\n";
        str += "sessionEnergyDischarged : " + sessionEnergyDischarged + "\n";
        return str;
    }
}
