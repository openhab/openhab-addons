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
package org.openhab.binding.mercedesme.internal.api;

import static org.openhab.binding.mercedesme.internal.Constants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.daimler.mbcarkit.proto.VehicleEvents.BoolAttribute;
import com.daimler.mbcarkit.proto.VehicleEvents.DoubleAttribute;
import com.daimler.mbcarkit.proto.VehicleEvents.DoubleCombustionConsumptionAttribute;
import com.daimler.mbcarkit.proto.VehicleEvents.DoubleDistanceAttribute;
import com.daimler.mbcarkit.proto.VehicleEvents.DoubleElectricityConsumptionAttribute;
import com.daimler.mbcarkit.proto.VehicleEvents.DoublePressureAttribute;
import com.daimler.mbcarkit.proto.VehicleEvents.DoubleSpeedAttribute;
import com.daimler.mbcarkit.proto.VehicleEvents.Int64Attribute;
import com.daimler.mbcarkit.proto.VehicleEvents.Int64ClockHourAttribute;
import com.daimler.mbcarkit.proto.VehicleEvents.Int64DistanceAttribute;
import com.daimler.mbcarkit.proto.VehicleEvents.Int64RatioAttribute;
import com.daimler.mbcarkit.proto.VehicleEvents.VEPUpdate;
import com.daimler.mbcarkit.proto.VehicleEvents.VSUMetadata;
import com.daimler.mbcarkit.proto.VehicleEvents.VehicleAttributeStatus;
import com.daimler.mbcarkit.proto.VehicleEvents.VehicleStatusUpdate;
import com.google.protobuf.Timestamp;

/**
 * The {@link VehicleStatusUpdateConverter} converts a typed {@link VehicleStatusUpdate} (the new push format,
 * added in app version 165-1) into a {@link VEPUpdate}-shaped {@code Map<String, VehicleAttributeStatus>} so it
 * can be handed to the existing {@code AccountHandler.distributeVepUpdates()} / {@code VehicleHandler} pipeline
 * unchanged.
 * <p>
 * Only the ~90 fields that have a 1:1 counterpart in the old attribute set (see
 * {@code docs/ATTRIBUTES_MAPPING.md}) are converted here. Complex array-typed fields (temperature points, charge
 * programs, auxiliary warnings) and the ~200 genuinely new fields without an old channel are intentionally left
 * out and logged as not-yet-mapped by the caller.
 * <p>
 * Enum-typed fields are converted to {@code VehicleAttributeStatus.int_value} using the new enum's ordinal
 * ({@code getValueValue()}), matching the convention the old API already used for status codes. This assumes the
 * ordinal numbering is unchanged between the old int codes and the new typed enums (same backend, same
 * {@code .proto} family) - not yet verified against a live vehicle, see {@code docs/ATTRIBUTES_MAPPING.md}.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class VehicleStatusUpdateConverter {

    private VehicleStatusUpdateConverter() {
        // utility class - no instances
    }

    /**
     * Converts one vehicle's {@link VehicleStatusUpdate} into a {@link VEPUpdate} carrying the same attribute keys
     * the old {@code VEPUpdatesByVIN} push used, so downstream channel handling doesn't need to change.
     *
     * @param vsu the typed status update for a single VIN
     * @return a {@link VEPUpdate} with the mapped attributes
     */
    public static VEPUpdate convert(VehicleStatusUpdate vsu) {
        Map<String, VehicleAttributeStatus> attributes = new HashMap<>();

        // bool
        putBool(attributes, MB_KEY_WARNINGBRAKEFLUID, vsu.getWarningbrakefluid());
        putBool(attributes, MB_KEY_WARNINGBRAKELININGWEAR, vsu.getWarningbrakeliningwear());
        putBool(attributes, MB_KEY_WARNINGCOOLANTLEVELLOW, vsu.getWarningcoolantlevellow());
        putBool(attributes, MB_KEY_WARNINGENGINELIGHT, vsu.getWarningenginelight());
        putBool(attributes, MB_KEY_CHARGINGACTIVE, vsu.getChargingactive());

        // plain int64 / double (no unit)
        putInt64(attributes, MB_KEY_SERVICEINTERVALDAYS, vsu.getServiceintervaldays());
        putInt64(attributes, MB_KEY_TIRE_PRESS_MEAS_TIMESTAMP, vsu.getTirePressMeasTimestamp());
        putInt64(attributes, MB_KEY_DRIVEN_TIME_RESET, vsu.getDrivenTimeReset());
        putInt64(attributes, MB_KEY_DRIVEN_TIME_START, vsu.getDrivenTimeStart());
        putDouble(attributes, MB_KEY_POSITION_HEADING, vsu.getPositionHeading());
        putDouble(attributes, MB_KEY_CHARGING_POWER, vsu.getChargingPower());
        putDouble(attributes, MB_KEY_POSITION_LONG, vsu.getPositionLong());
        putDouble(attributes, MB_KEY_POSITION_LAT, vsu.getPositionLat());

        // enum-typed status attributes -> int_value = new enum ordinal
        putEnum(attributes, MB_KEY_TIRE_SENSOR_AVAILABLE, vsu.getTireSensorAvailable().getValueValue(),
                vsu.getTireSensorAvailable().getMetadata());
        putEnum(attributes, MB_KEY_CHARGE_COUPLER_DC_LOCK_STATUS, vsu.getChargeCouplerDCLockStatus().getValueValue(),
                vsu.getChargeCouplerDCLockStatus().getMetadata());
        putEnum(attributes, MB_KEY_CHARGE_COUPLER_DC_STATUS, vsu.getChargeCouplerDCStatus().getValueValue(),
                vsu.getChargeCouplerDCStatus().getMetadata());
        putEnum(attributes, MB_KEY_CHARGE_COUPLER_AC_STATUS, vsu.getChargeCouplerACStatus().getValueValue(),
                vsu.getChargeCouplerACStatus().getMetadata());
        putEnum(attributes, MB_KEY_CHARGE_FLAP_DC_STATUS, vsu.getChargeFlapDCStatus().getValueValue(),
                vsu.getChargeFlapDCStatus().getMetadata());
        putEnum(attributes, MB_KEY_CHARGE_STATUS, vsu.getChargingstatus().getValueValue(),
                vsu.getChargingstatus().getMetadata());
        putEnum(attributes, MB_KEY_CHARGE_ERROR, vsu.getChargingErrorDetails().getValueValue(),
                vsu.getChargingErrorDetails().getMetadata());
        putEnum(attributes, MB_KEY_TIREWARNINGSRDK, vsu.getTirewarningsrdk().getValueValue(),
                vsu.getTirewarningsrdk().getMetadata());
        putEnum(attributes, MB_KEY_STARTER_BATTERY_STATE, vsu.getStarterBatteryState().getValueValue(),
                vsu.getStarterBatteryState().getMetadata());
        putEnum(attributes, MB_KEY_FLIP_WINDOW_STATUS, vsu.getFlipWindowStatus().getValueValue(),
                vsu.getFlipWindowStatus().getMetadata());
        putEnum(attributes, MB_KEY_WINDOW_STATUS_REAR_BLIND, vsu.getWindowStatusRearBlind().getValueValue(),
                vsu.getWindowStatusRearBlind().getMetadata());
        putEnum(attributes, MB_KEY_WINDOW_STATUS_REAR_LEFT_BLIND, vsu.getWindowStatusRearLeftBlind().getValueValue(),
                vsu.getWindowStatusRearLeftBlind().getMetadata());
        putEnum(attributes, MB_KEY_WINDOW_STATUS_REAR_RIGHT_BLIND, vsu.getWindowStatusRearRightBlind().getValueValue(),
                vsu.getWindowStatusRearRightBlind().getMetadata());
        putEnum(attributes, MB_KEY_WINDOWSTATUSREARRIGHT, vsu.getWindowstatusrearright().getValueValue(),
                vsu.getWindowstatusrearright().getMetadata());
        putEnum(attributes, MB_KEY_WINDOWSTATUSREARLEFT, vsu.getWindowstatusrearleft().getValueValue(),
                vsu.getWindowstatusrearleft().getMetadata());
        putEnum(attributes, MB_KEY_WINDOWSTATUSFRONTRIGHT, vsu.getWindowstatusfrontright().getValueValue(),
                vsu.getWindowstatusfrontright().getMetadata());
        putEnum(attributes, MB_KEY_WINDOWSTATUSFRONTLEFT, vsu.getWindowstatusfrontleft().getValueValue(),
                vsu.getWindowstatusfrontleft().getMetadata());
        putEnum(attributes, MB_KEY_ROOFTOPSTATUS, vsu.getRooftopstatus().getValueValue(),
                vsu.getRooftopstatus().getMetadata());
        putEnum(attributes, MB_KEY_SUNROOF_STATUS_REAR_BLIND, vsu.getSunroofStatusRearBlind().getValueValue(),
                vsu.getSunroofStatusRearBlind().getMetadata());
        putEnum(attributes, MB_KEY_SUNROOF_STATUS_FRONT_BLIND, vsu.getSunroofStatusFrontBlind().getValueValue(),
                vsu.getSunroofStatusFrontBlind().getMetadata());
        putEnum(attributes, MB_KEY_SUNROOFSTATUS, vsu.getSunroofstatus().getValueValue(),
                vsu.getSunroofstatus().getMetadata());
        putEnum(attributes, MB_KEY_IGNITIONSTATE, vsu.getIgnitionstate().getValueValue(),
                vsu.getIgnitionstate().getMetadata());
        putEnum(attributes, MB_KEY_DOOR_STATUS_OVERALL, vsu.getDoorStatusOverall().getValueValue(),
                vsu.getDoorStatusOverall().getMetadata());
        putEnum(attributes, MB_KEY_WINDOW_STATUS_OVERALL, vsu.getWindowStatusOverall().getValueValue(),
                vsu.getWindowStatusOverall().getMetadata());
        putEnum(attributes, MB_KEY_DOOR_LOCK_STATUS_OVERALL, vsu.getDoorlockstatusvehicle().getValueValue(),
                vsu.getDoorlockstatusvehicle().getMetadata());
        putEnum(attributes, MB_KEY_TIRE_MARKER_FRONT_RIGHT, vsu.getTireMarkerFrontRight().getValueValue(),
                vsu.getTireMarkerFrontRight().getMetadata());
        putEnum(attributes, MB_KEY_TIRE_MARKER_FRONT_LEFT, vsu.getTireMarkerFrontLeft().getValueValue(),
                vsu.getTireMarkerFrontLeft().getMetadata());
        putEnum(attributes, MB_KEY_TIRE_MARKER_REAR_RIGHT, vsu.getTireMarkerRearRight().getValueValue(),
                vsu.getTireMarkerRearRight().getMetadata());
        putEnum(attributes, MB_KEY_TIRE_MARKER_REAR_LEFT, vsu.getTireMarkerRearLeft().getValueValue(),
                vsu.getTireMarkerRearLeft().getMetadata());
        putEnum(attributes, MB_KEY_PARKBRAKESTATUS, vsu.getParkbrakestatus().getValueValue(),
                vsu.getParkbrakestatus().getMetadata());
        putEnum(attributes, MB_KEY_PRECOND_NOW, vsu.getPrecondNow().getValueValue(), vsu.getPrecondNow().getMetadata());
        putEnum(attributes, MB_KEY_PRECOND_SEAT_FRONT_RIGHT, vsu.getPrecondSeatFrontRight().getValueValue(),
                vsu.getPrecondSeatFrontRight().getMetadata());
        putEnum(attributes, MB_KEY_PRECOND_SEAT_FRONT_LEFT, vsu.getPrecondSeatFrontLeft().getValueValue(),
                vsu.getPrecondSeatFrontLeft().getMetadata());
        putEnum(attributes, MB_KEY_PRECOND_SEAT_REAR_RIGHT, vsu.getPrecondSeatRearRight().getValueValue(),
                vsu.getPrecondSeatRearRight().getMetadata());
        putEnum(attributes, MB_KEY_PRECOND_SEAT_REAR_LEFT, vsu.getPrecondSeatRearLeft().getValueValue(),
                vsu.getPrecondSeatRearLeft().getMetadata());
        putEnum(attributes, MB_KEY_WARNINGWASHWATER, vsu.getWarningwashwater().getValueValue(),
                vsu.getWarningwashwater().getMetadata());
        putEnum(attributes, MB_KEY_DOORLOCKSTATUSFRONTRIGHT, vsu.getDoorlockstatusfrontright().getValueValue(),
                vsu.getDoorlockstatusfrontright().getMetadata());
        putEnum(attributes, MB_KEY_DOORLOCKSTATUSFRONTLEFT, vsu.getDoorlockstatusfrontleft().getValueValue(),
                vsu.getDoorlockstatusfrontleft().getMetadata());
        putEnum(attributes, MB_KEY_DOORLOCKSTATUSREARRIGHT, vsu.getDoorlockstatusrearright().getValueValue(),
                vsu.getDoorlockstatusrearright().getMetadata());
        putEnum(attributes, MB_KEY_DOORLOCKSTATUSREARLEFT, vsu.getDoorlockstatusrearleft().getValueValue(),
                vsu.getDoorlockstatusrearleft().getMetadata());
        putEnum(attributes, MB_KEY_DOORLOCKSTATUSDECKLID, vsu.getDoorlockstatusdecklid().getValueValue(),
                vsu.getDoorlockstatusdecklid().getMetadata());
        putEnum(attributes, MB_KEY_DOORLOCKSTATUSGAS, vsu.getDoorlockstatusgas().getValueValue(),
                vsu.getDoorlockstatusgas().getMetadata());
        putEnum(attributes, MB_KEY_ENGINE_HOOD_STATUS, vsu.getEngineHoodStatus().getValueValue(),
                vsu.getEngineHoodStatus().getMetadata());
        putEnum(attributes, MB_KEY_DECKLIDSTATUS, vsu.getDecklidstatus().getValueValue(),
                vsu.getDecklidstatus().getMetadata());
        putEnum(attributes, MB_KEY_DOORSTATUSREARLEFT, vsu.getDoorstatusrearleft().getValueValue(),
                vsu.getDoorstatusrearleft().getMetadata());
        putEnum(attributes, MB_KEY_DOORSTATUSREARRIGHT, vsu.getDoorstatusrearright().getValueValue(),
                vsu.getDoorstatusrearright().getMetadata());
        putEnum(attributes, MB_KEY_DOORSTATUSFRONTLEFT, vsu.getDoorstatusfrontleft().getValueValue(),
                vsu.getDoorstatusfrontleft().getMetadata());
        putEnum(attributes, MB_KEY_DOORSTATUSFRONTRIGHT, vsu.getDoorstatusfrontright().getValueValue(),
                vsu.getDoorstatusfrontright().getMetadata());
        putEnum(attributes, MB_KEY_ENDOFCHARGEDAY, vsu.getEndofChargeTimeWeekday().getValueValue(),
                vsu.getEndofChargeTimeWeekday().getMetadata());
        putEnum(attributes, MB_KEY_SELECTED_CHARGE_PROGRAM, vsu.getSelectedChargeProgram().getValueValue(),
                vsu.getSelectedChargeProgram().getMetadata());
        putEnum(attributes, MB_KEY_POSITION_ERROR, vsu.getVehiclePositionErrorCode().getValueValue(),
                vsu.getVehiclePositionErrorCode().getMetadata());
        putEnum(attributes, MB_KEY_PRECOND_NOW_ERROR, vsu.getPrecondNowError().getValueValue(),
                vsu.getPrecondNowError().getMetadata());

        // distance (int64 / double, with unit + display value)
        putInt64Distance(attributes, MB_KEY_RANGELIQUID, vsu.getRangeliquid());
        putInt64Distance(attributes, MB_KEY_RANGEELECTRIC, vsu.getRangeelectric());
        putInt64Distance(attributes, MB_KEY_ODO, vsu.getOdo());
        putDoubleDistance(attributes, MB_KEY_DISTANCE_RESET, vsu.getDistanceReset());
        putDoubleDistance(attributes, MB_KEY_DISTANCE_START, vsu.getDistanceStart());
        putDoubleDistance(attributes, MB_KEY_OVERALL_RANGE, vsu.getOverallRange());
        putDoubleDistance(attributes, MB_KEY_ECOSCORE_BONUS, vsu.getEcoscorebonusrange());

        // pressure
        putPressure(attributes, MB_KEY_TIREPRESSURE_FRONT_LEFT, vsu.getTirepressureFrontLeft());
        putPressure(attributes, MB_KEY_TIREPRESSURE_FRONT_RIGHT, vsu.getTirepressureFrontRight());
        putPressure(attributes, MB_KEY_TIREPRESSURE_REAR_LEFT, vsu.getTirepressureRearLeft());
        putPressure(attributes, MB_KEY_TIREPRESSURE_REAR_RIGHT, vsu.getTirepressureRearRight());

        // speed
        putSpeed(attributes, MB_KEY_AVERAGE_SPEED_RESET, vsu.getAverageSpeedReset());
        putSpeed(attributes, MB_KEY_AVERAGE_SPEED_START, vsu.getAverageSpeedStart());

        // ratio (percent)
        putRatio(attributes, MB_KEY_TANKLEVELPERCENT, vsu.getTanklevelpercent());
        putRatio(attributes, MB_KEY_ADBLUELEVELPERCENT, vsu.getTankLevelAdBlue());
        putRatio(attributes, MB_KEY_SOC, vsu.getSoc());
        putRatio(attributes, MB_KEY_MAX_SOC, vsu.getMaxSoc());
        putRatio(attributes, MB_KEY_MAX_SOC_LOWER_LIMIT, vsu.getMaxSocLowerLimit());
        putRatio(attributes, MB_KEY_MAX_SOC_UPPER_LIMIT, vsu.getMaxSocUpperLimit());
        putRatio(attributes, MB_KEY_ECOSCORE_ACCEL, vsu.getEcoscoreaccel());
        putRatio(attributes, MB_KEY_ECOSCORE_CONSTANT, vsu.getEcoscoreconst());
        putRatio(attributes, MB_KEY_ECOSCORE_COASTING, vsu.getEcoscorefreewhl());

        // clock hour
        putClockHour(attributes, MB_KEY_ENDOFCHARGETIME, vsu.getEndofchargetime());

        // consumption
        putCombustionConsumption(attributes, MB_KEY_LIQUIDCONSUMPTIONRESET, vsu.getLiquidconsumptionreset());
        putCombustionConsumption(attributes, MB_KEY_LIQUIDCONSUMPTIONSTART, vsu.getLiquidconsumptionstart());
        putElectricityConsumption(attributes, MB_KEY_ELECTRICCONSUMPTIONRESET, vsu.getElectricconsumptionreset());
        putElectricityConsumption(attributes, MB_KEY_ELECTRICCONSUMPTIONSTART, vsu.getElectricconsumptionstart());

        // NOTE: temperature_points (MB_KEY_TEMPERATURE_POINTS), charge_programs (MB_KEY_CHARGE_PROGRAMS) and
        // auxheatwarnings (MB_KEY_AUXILIARY_WARNINGS) are complex array-typed fields with a different sub-message
        // shape in VehicleStatusUpdate than in the old VehicleAttributeStatus oneof - not converted yet, see
        // docs/ATTRIBUTES_MAPPING.md section 4.

        return VEPUpdate.newBuilder().setVin(vsu.getFinOrVin()).setFullUpdate(vsu.getFullUpdate())
                .putAllAttributes(attributes).build();
    }

    private static long toMillis(VSUMetadata metadata) {
        if (!metadata.hasTimestamp()) {
            return 0;
        }
        Timestamp ts = metadata.getTimestamp();
        return ts.getSeconds() * 1000 + ts.getNanos() / 1_000_000;
    }

    private static VehicleAttributeStatus.Builder baseBuilder(VSUMetadata metadata) {
        return VehicleAttributeStatus.newBuilder().setStatus(metadata.getStatusValue())
                .setTimestampInMs(toMillis(metadata));
    }

    private static void putBool(Map<String, VehicleAttributeStatus> map, String key, BoolAttribute a) {
        map.put(key, baseBuilder(a.getMetadata()).setBoolValue(a.getValue()).build());
    }

    private static void putInt64(Map<String, VehicleAttributeStatus> map, String key, Int64Attribute a) {
        map.put(key, baseBuilder(a.getMetadata()).setIntValue(a.getValue()).build());
    }

    private static void putDouble(Map<String, VehicleAttributeStatus> map, String key, DoubleAttribute a) {
        map.put(key, baseBuilder(a.getMetadata()).setDoubleValue(a.getValue()).build());
    }

    private static void putEnum(Map<String, VehicleAttributeStatus> map, String key, int enumOrdinal,
            VSUMetadata metadata) {
        map.put(key, baseBuilder(metadata).setIntValue(enumOrdinal).build());
    }

    private static void putInt64Distance(Map<String, VehicleAttributeStatus> map, String key,
            Int64DistanceAttribute a) {
        map.put(key, baseBuilder(a.getMetadata()).setIntValue(a.getValue()).setDistanceUnit(a.getUnit())
                .setDisplayValue(a.getDisplayValue()).build());
    }

    private static void putDoubleDistance(Map<String, VehicleAttributeStatus> map, String key,
            DoubleDistanceAttribute a) {
        map.put(key, baseBuilder(a.getMetadata()).setDoubleValue(a.getValue()).setDistanceUnit(a.getUnit())
                .setDisplayValue(a.getDisplayValue()).build());
    }

    private static void putPressure(Map<String, VehicleAttributeStatus> map, String key, DoublePressureAttribute a) {
        map.put(key, baseBuilder(a.getMetadata()).setDoubleValue(a.getValue()).setPressureUnit(a.getUnit())
                .setDisplayValue(a.getDisplayValue()).build());
    }

    private static void putSpeed(Map<String, VehicleAttributeStatus> map, String key, DoubleSpeedAttribute a) {
        map.put(key, baseBuilder(a.getMetadata()).setDoubleValue(a.getValue()).setSpeedUnit(a.getUnit())
                .setDisplayValue(a.getDisplayValue()).build());
    }

    private static void putRatio(Map<String, VehicleAttributeStatus> map, String key, Int64RatioAttribute a) {
        map.put(key, baseBuilder(a.getMetadata()).setIntValue(a.getValue()).setRatioUnit(a.getUnit())
                .setDisplayValue(a.getDisplayValue()).build());
    }

    private static void putClockHour(Map<String, VehicleAttributeStatus> map, String key, Int64ClockHourAttribute a) {
        map.put(key, baseBuilder(a.getMetadata()).setIntValue(a.getValue()).setClockHourUnit(a.getUnit())
                .setDisplayValue(a.getDisplayValue()).build());
    }

    private static void putCombustionConsumption(Map<String, VehicleAttributeStatus> map, String key,
            DoubleCombustionConsumptionAttribute a) {
        map.put(key, baseBuilder(a.getMetadata()).setDoubleValue(a.getValue()).setCombustionConsumptionUnit(a.getUnit())
                .setDisplayValue(a.getDisplayValue()).build());
    }

    private static void putElectricityConsumption(Map<String, VehicleAttributeStatus> map, String key,
            DoubleElectricityConsumptionAttribute a) {
        map.put(key, baseBuilder(a.getMetadata()).setDoubleValue(a.getValue())
                .setElectricityConsumptionUnit(a.getUnit()).setDisplayValue(a.getDisplayValue()).build());
    }
}
