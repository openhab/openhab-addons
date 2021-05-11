/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.carnet.internal.provider;

import static org.openhab.binding.carnet.internal.CarNetBindingConstants.*;
import static org.openhab.binding.carnet.internal.CarNetUtils.mkChannelId;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.carnet.internal.CarNetTextResources;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CarNetVehicleStatus.CNStoredVehicleDataResponse.CNVehicleData.CNStatusData.CNStatusField;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CarNetIChanneldMapper} maps status value IDs from the API to channel definitions.
 *
 * @author Markus Michels - Initial contribution
 * @author Lorenzo Bernardi - Additional contribution
 *
 */
@NonNullByDefault
@Component(service = CarNetIChanneldMapper.class)
public class CarNetIChanneldMapper {
    private final Logger logger = LoggerFactory.getLogger(CarNetIChanneldMapper.class);
    private final Map<String, ChannelIdMapEntry> map = new LinkedHashMap<>();
    private final CarNetTextResources resources;

    @Activate
    public CarNetIChanneldMapper(@Reference CarNetTextResources resources) {
        this.resources = resources;

        // Status
        add("KILOMETER_STATUS", "0x0101010002", "kilometerStatus", ITEMT_DISTANCE, CHANNEL_GROUP_STATUS, KILOMETRE);
        add("TEMPERATURE_OUTSIDE", "0x0301020001", "tempOutside", ITEMT_TEMP, CHANNEL_GROUP_STATUS, SIUnits.CELSIUS);
        add("STATE1_PARKING_LIGHT", "0x0301010001", "parkingLight", ITEMT_SWITCH);
        add("STATE1_PARKING_BRAKE", "0x0301030001", "parkingBrake", ITEMT_SWITCH);
        add("POSITION_CONVERTIBLE_TOP", "0x030105000A", "positionConvertableTop", ITEMT_PERCENT);
        add("STATE3_SUN_ROOF_MOTOR_COVER", "0x030105000B", "roofMotorCoverState", ITEMT_SWITCH);
        add("POSITION_SUN_ROOF_MOTOR_COVER", "0x030105000C");
        add("STATE3_SUN_ROOF_REAR_MOTOR_COVER", "0x030105000D", "roofRearMotorCoverState", ITEMT_SWITCH);
        add("POSITION_SUN_ROOF_REAR_MOTOR_COVER", "0x030105000E");
        add("STATE3_SPOILER", "0x0301050011", "spoilerState", ITEMT_SWITCH, CHANNEL_GROUP_STATUS, null, true, true);
        add("POSITION_SPOILER", "0x0301050012");
        add("STATE3_SERVICE_FLAP", "0x030105000F", "serviceFlapState", ITEMT_SWITCH);
        add("POSITION_SERVICE_FLAP", "0x0301050010");
        add("CURRENT_SPEED", "0x0301030004", "currentSpeed", ITEMT_SPEED, CHANNEL_GROUP_STATUS,
                SIUnits.KILOMETRE_PER_HOUR);
        add("BEM", "0x0301030003"); // what does BEM means?

        // Range
        add("FUEL_LEVEL_PERCENT", "0x030103000A", "fuelPercentage", ITEMT_PERCENT, CHANNEL_GROUP_RANGE, PERCENT);
        add("FUEL_METHOD", "0x030103000B", "fuelMethod", ITEMT_STRING, CHANNEL_GROUP_RANGE, null, true, true); // '0':'measured',
        // '1':'calculated'
        add("TOTAL_RANGE", "0x0301030005", "totalRange", ITEMT_DISTANCE, CHANNEL_GROUP_RANGE, KILOMETRE);
        add("PRIMARY_RANGE", "0x0301030006", "primaryRange", ITEMT_DISTANCE, CHANNEL_GROUP_RANGE, KILOMETRE, true,
                true);
        add("PRIMARY_FUEL_TYPE", "0x0301030007", "primaryFuelType", ITEMT_NUMBER, CHANNEL_GROUP_RANGE, null, true,
                true);
        add("SECONDARY_RANGE", "0x0301030008", "secondaryRange", ITEMT_DISTANCE, CHANNEL_GROUP_RANGE, KILOMETRE, true,
                true);
        add("SECONDARY_DRIVE", "0x0301030009", "secondaryFuelType", ITEMT_NUMBER, CHANNEL_GROUP_RANGE, null, true,
                true);
        add("CHARGING_LEVEL_PERCENT", "0x0301030002", "chargingLevel", ITEMT_PERCENT, CHANNEL_GROUP_RANGE, PERCENT,
                true, true);
        add("15CNG_LEVEL_IN_PERCENT", "0x030103000D", "gasPercentage", ITEMT_PERCENT, CHANNEL_GROUP_RANGE, PERCENT,
                true, true);

        // Maintenance
        add("MAINT_ALARM_INSPECTION", "0x0203010006", "alarmInspection", ITEMT_SWITCH, CHANNEL_GROUP_MAINT);
        add("MAINT_DIST_TO_INSPECTION", "0x0203010003", "distanceToInspection", ITEMT_DISTANCE, CHANNEL_GROUP_MAINT,
                KILOMETRE);
        add("MAINT_TIME_TO_INSPECTION", "0x0203010004", "timeToInspection", ITEMT_TIME, CHANNEL_GROUP_MAINT, QDAYS);
        add("MAINT_INTERVAL_AD_BLUE_RANGE", "0x02040C0001", "distanceAdBlue", ITEMT_DISTANCE, CHANNEL_GROUP_MAINT,
                KILOMETRE);
        add("MAINT_ALARM_OIL_CHANGE", "0x0203010005", "oilWarningChange", ITEMT_SWITCH, CHANNEL_GROUP_MAINT);
        add("MAINT_ALARM_OIL_MINIMUM", "0x0204040002", "oilWarningLevel", ITEMT_SWITCH, CHANNEL_GROUP_MAINT);
        add("MAINT_OIL_DIPSTICK_PERCENT", "0x0204040003", "oilPercentage", ITEMT_PERCENT, CHANNEL_GROUP_MAINT, PERCENT);
        add("MAINT_OIL_LEVEL_AMOUNT_IN_LITERS", "0x0204040001");
        add("MAINT_OIL_LEVEL_DISPLAY", "0x0204040004");
        add("MAINT_DISTANCE_TO_OIL_CHANGE", "0x0203010001", "distanceOilChange", ITEMT_DISTANCE, CHANNEL_GROUP_MAINT,
                KILOMETRE);
        add("MAINT_OIL_TIME_TO_CHANGE", "0x0203010002", "intervalOilChange", ITEMT_TIME, CHANNEL_GROUP_MAINT, QDAYS);
        add("MAINT_MONTHLY_MILEAGE", "0x0203010007", "monthlyMilage", ITEMT_DISTANCE, CHANNEL_GROUP_STATUS, KILOMETRE);

        // Doors/trunk
        add("STATE3_CONVERTABLE_TOP", "0x0301050009", "covertableTopState", ITEMT_NUMBER, CHANNEL_GROUP_DOORS);
        add("STATE3_TRUNK_LID", "0x030104000E", "trunkLidState", ITEMT_SWITCH, CHANNEL_GROUP_DOORS);
        add("LOCK2_TRUNK_LID", "0x030104000D", "trunkLidLocked", ITEMT_SWITCH, CHANNEL_GROUP_DOORS);
        add("SAFETY_TRUNK_LID", "0x030104000F", "trunkLidSafety", ITEMT_SWITCH, CHANNEL_GROUP_DOORS);
        add("STATE3_HOOD", "0x0301040011", "hoodState", ITEMT_SWITCH, CHANNEL_GROUP_DOORS);
        add("LOCK3_HOOD", "0x0301040010", "hoodLocked", ITEMT_SWITCH, CHANNEL_GROUP_DOORS);
        add("SAFETY_HOOD", "0x0301040012", "hoodSafety", ITEMT_SWITCH, CHANNEL_GROUP_DOORS);
        add("STATE3_LEFT_FRONT_DOOR", "0x0301040002", "doorFrontLeftState", ITEMT_SWITCH, CHANNEL_GROUP_DOORS);
        add("LOCK2_LEFT_FRONT_DOOR", "0x0301040001", "doorFrontLeftLocked", ITEMT_SWITCH, CHANNEL_GROUP_DOORS);
        add("SAFETY_LEFT_FRONT_DOOR", "0x0301040003", "doorFrontLeftSafety", ITEMT_SWITCH, CHANNEL_GROUP_DOORS);
        add("STATE3_RIGHT_FRONT_DOOR", "0x0301040008", "doorFrontRightState", ITEMT_SWITCH, CHANNEL_GROUP_DOORS);
        add("LOCK2_RIGHT_FRONT_DOOR", "0x0301040007", "doorFrontRightLocked", ITEMT_SWITCH, CHANNEL_GROUP_DOORS);
        add("SAFETY_RIGHT_FRONT_DOOR", "0x0301040009", "doorFrontRightSafety", ITEMT_SWITCH, CHANNEL_GROUP_DOORS);
        add("STATE3_LEFT_REAR_DOOR", "0x0301040005", "doorRearLeftState", ITEMT_SWITCH, CHANNEL_GROUP_DOORS);
        add("LOCK2_LEFT_REAR_DOOR", "0x0301040004", "doorRearLeftLocked", ITEMT_SWITCH, CHANNEL_GROUP_DOORS);
        add("SAFETY_LEFT_REAR_DOOR", "0x0301040006", "doorRearLeftSafety", ITEMT_SWITCH, CHANNEL_GROUP_DOORS);
        add("STATE3_RIGHT_REAR_DOOR", "0x030104000B", "doorRearRightState", ITEMT_SWITCH, CHANNEL_GROUP_DOORS);
        add("LOCK2_RIGHT_REAR_DOOR", "0x030104000A", "doorRearRightLocked", ITEMT_SWITCH, CHANNEL_GROUP_DOORS);
        add("SAFETY_RIGHT_REAR_DOOR", "0x030104000C", "doorRearRightSafety", ITEMT_SWITCH, CHANNEL_GROUP_DOORS);

        // Windows
        add("STATE3_LEFT_FRONT_WINDOW", "0x0301050001", "windowFrontLeftState", ITEMT_SWITCH, CHANNEL_GROUP_WINDOWS);
        add("POSITION_LEFT_FRONT_WINDOW", "0x0301050002", "windowFrontLeftPos", ITEMT_PERCENT, CHANNEL_GROUP_WINDOWS);
        add("STATE3_LEFT_REAR_WINDOW", "0x0301050003", "windowRearLeftState", ITEMT_SWITCH, CHANNEL_GROUP_WINDOWS);
        add("POSITION_LEFT_REAR_WINDOW", "0x0301050004", "windowRearLeftPos", ITEMT_PERCENT, CHANNEL_GROUP_WINDOWS);
        add("STATE3_RIGHT_FRONT_WINDOW", "0x0301050005", "windowFrontRightState", ITEMT_SWITCH, CHANNEL_GROUP_WINDOWS);
        add("POSITION_RIGHT_FRONT_WINDOW", "0x0301050006", "windowFrontRightPos", ITEMT_PERCENT, CHANNEL_GROUP_WINDOWS);
        add("STATE3_RIGHT_REAR_WINDOW", "0x0301050007", "windowRearRightState", ITEMT_SWITCH, CHANNEL_GROUP_WINDOWS);
        add("POSITION_RIGHT_REAR_WINDOW", "0x0301050008", "windowRearRightPos", ITEMT_PERCENT, CHANNEL_GROUP_WINDOWS);

        // Tires
        add("TIREPRESS_LEFT_FRONT_CURRENT", "0x0301060001", "tirePresFrontLeft", ITEMT_SWITCH, CHANNEL_GROUP_TIRES);
        add("TIREPRESS_LEFT_FRONT_DESIRED", "0x0301060002");
        add("TIREPRESS_LEFT_REAR_CURRENT", "0x0301060003", "tirePresRearLeft", ITEMT_SWITCH, CHANNEL_GROUP_TIRES);
        add("TIREPRESS_LEFT_REAR_DESIRED", "0x0301060004");
        add("TIREPRESS_RIGHT_FRONT_CURRENT", "0x0301060005", "tirePresFrontRight", ITEMT_SWITCH, CHANNEL_GROUP_TIRES);
        add("TIREPRESS_RIGHT_FRONT_DESIRED", "0x0301060006");
        add("TIREPRESS_RIGHT_REAR_CURRENT", "0x0301060007", "tirePresRearRight", ITEMT_SWITCH, CHANNEL_GROUP_TIRES);
        add("TIREPRESS_RIGHT_REAR_DESIRED", "0x0301060008");
        add("TIREPRESS_LEFT_FRONT_TIRE_DIFF", "0x030106000B");
        add("TIREPRESS_LEFT_REAR_TIRE_DIFF", "0x030106000C");
        add("TIREPRESS_RIGHT_FRONT_TIRE_DIFF", "0x030106000D");
        add("TIREPRESS_RIGHT_REAR_TIRE_DIFF", "0x030106000E");
        add("TIREPRESS_SPARE_CURRENT", "0x0301060009");
        add("TIREPRESS_SPARE_DESIRED", "0x030106000A");
        add("TIREPRESS_SPARE_DIFF", "0x030106000F");

        // Misc
        add("UTC_TIME_STATUS", "0x0101010001");
    }

    public class ChannelIdMapEntry {
        private final CarNetTextResources resources;

        public String id = "";
        public String symbolicName = "";
        public String channelName = "";
        public String itemType = "";
        public String groupName = "";

        public boolean advanced = false;
        public boolean readOnly = true;
        public @Nullable Unit<?> fromUnit;
        public @Nullable Unit<?> unit;
        public Optional<Integer> min = Optional.empty();
        public Optional<Integer> max = Optional.empty();
        public Optional<Integer> step = Optional.empty();
        public Optional<String> pattern = Optional.empty();

        public ChannelIdMapEntry(CarNetTextResources resources) {
            this.resources = resources;
        }

        public String getGroup() {
            return !groupName.isEmpty() ? groupName : CHANNEL_GROUP_STATUS;
        }

        public String getLabel() {
            return getChannelAttribute("label");
        }

        public String getDescription() {
            return getChannelAttribute("description");
        }

        public String getAdvanced() {
            return getChannelAttribute("advanced");
        }

        public String getReadOnly() {
            return getChannelAttribute("readonly");
        }

        public String getChannelAttribute(String attribute) {
            String key = "channel-type.carnet." + channelName + "." + attribute;
            String value = resources.getText(key);
            return !value.equals(key) ? value : "";
        }
    }

    public @Nullable ChannelIdMapEntry find(String id) {
        for (Map.Entry<String, ChannelIdMapEntry> e : map.entrySet()) {
            if (e.getKey().equalsIgnoreCase(id)) {
                return e.getValue();
            }
            ChannelIdMapEntry v = e.getValue();
            if (v.symbolicName.equalsIgnoreCase(id) || v.channelName.equalsIgnoreCase(id)) {
                return v;
            }
        }
        return null;
    }

    public ChannelIdMapEntry updateDefinition(CNStatusField field, ChannelIdMapEntry definition) {
        String itemType = "";
        Unit<?> unit = null;

        String fieldUnit = gs(field.unit);
        if (fieldUnit.isEmpty() || "null".equalsIgnoreCase(fieldUnit)) {
            return definition;
        }
        if (fieldUnit.contains("%")) {
            itemType = ITEMT_PERCENT;
            unit = Units.PERCENT;
        } else if ("d".equalsIgnoreCase(fieldUnit)) {
            itemType = ITEMT_TIME;
            unit = QDAYS;
        } else if ("min".equalsIgnoreCase(fieldUnit)) {
            itemType = ITEMT_TIME;
            unit = QMINUTES;
        } else if ("l".equalsIgnoreCase(fieldUnit)) {
            itemType = ITEMT_VOLUME;
            unit = Units.LITRE;
            /*
             * } else if (fieldUnit.equalsIgnoreCase("gal")) {
             * itemType = ITEMT_VOLUME;
             * unit = CustomUnits.GALLON;
             */
        } else if ("dK".equalsIgnoreCase(fieldUnit)) {
            itemType = ITEMT_TEMP;
            unit = DKELVIN;
        } else if ("C".equalsIgnoreCase(fieldUnit)) {
            itemType = ITEMT_TEMP;
            unit = SIUnits.CELSIUS;
        } else if ("F".equalsIgnoreCase(fieldUnit)) {
            itemType = ITEMT_TEMP;
            unit = ImperialUnits.FAHRENHEIT;
        } else if ("km".equalsIgnoreCase(fieldUnit)) {
            itemType = ITEMT_DISTANCE;
            unit = KILOMETRE;
        } else if ("mi".equalsIgnoreCase(fieldUnit)) {
            itemType = ITEMT_DISTANCE;
            unit = ImperialUnits.MILE;
        } else if ("in".equalsIgnoreCase(fieldUnit)) {
            itemType = ITEMT_DISTANCE;
            unit = ImperialUnits.INCH;
        } else if ("km/h".equalsIgnoreCase(fieldUnit)) {
            itemType = ITEMT_SPEED;
            unit = SIUnits.KILOMETRE_PER_HOUR;
        } else if ("mph".equalsIgnoreCase(fieldUnit) || "mi/h".equalsIgnoreCase(fieldUnit)) {
            itemType = ITEMT_SPEED;
            unit = ImperialUnits.MILES_PER_HOUR;
        } else {
            logger.debug("Field unit unknown: {}", fieldUnit);
        }

        if (unit != null) {
            definition.fromUnit = unit; // needs conversion
        }
        if (!itemType.isEmpty() && !definition.itemType.equals(itemType)) {
            logger.debug("itemType for channel {} differs from sensor value {}", definition.itemType, itemType);
        }
        return definition;
    }

    public ChannelIdMapEntry add(String name, String id, String channel, String itemType, String group,
            @Nullable Unit<?> unit, boolean advanced, boolean readOnly) {
        ChannelIdMapEntry entry = new ChannelIdMapEntry(resources);
        entry.id = id;
        entry.symbolicName = name;
        entry.groupName = group;
        entry.channelName = channel;
        entry.itemType = itemType;
        if (ITEMT_PERCENT.equals(itemType) && (unit == null)) {
            entry.unit = Units.PERCENT;
        } else {
            entry.unit = unit;
        }
        if (!map.containsKey(id)) {
            map.put(id, entry);
        }
        entry.advanced = advanced;
        entry.readOnly = readOnly;
        return entry;
    }

    public ChannelIdMapEntry add(String name, String id, String channel, String itemType, String group,
            @Nullable Unit<?> unit) {
        boolean advanced = CHANNEL_GROUP_STATUS.equals(group) || CHANNEL_GROUP_WINDOWS.equals(group)
                || CHANNEL_GROUP_DOORS.equals(group) || CHANNEL_GROUP_TIRES.equals(group);
        return add(name, id, channel, itemType, group, unit, advanced, true);
    }

    public ChannelIdMapEntry add(String name, String id, String channelName, String itemType, String groupName) {
        return add(name, id, channelName, itemType, groupName, null);
    }

    public ChannelIdMapEntry add(String name, String id, String channelName, String itemType) {
        return add(name, id, channelName, itemType, CHANNEL_GROUP_STATUS, null, false, true);
    }

    public ChannelIdMapEntry add(String name, String id) {
        return add(name, id, "", "", CHANNEL_GROUP_STATUS, null, false, true);
    }

    public ChannelIdMapEntry add(String group, String channel, String itemType, @Nullable Unit<?> unit,
            boolean advanced, boolean readOnly) {
        return add(mkChannelId(group, channel), channel, channel, itemType, group, unit, advanced, readOnly);
    }

    private static String gs(@Nullable String s) {
        return s != null ? s : "";
    }
}
