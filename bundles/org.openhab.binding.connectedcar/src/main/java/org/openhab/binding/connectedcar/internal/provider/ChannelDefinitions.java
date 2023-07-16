/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.connectedcar.internal.provider;

import static org.openhab.binding.connectedcar.internal.BindingConstants.*;
import static org.openhab.binding.connectedcar.internal.util.Helpers.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CarNetVehicleStatus.CNStoredVehicleDataResponse.CNVehicleData.CNStatusData.CNStatusField;
import org.openhab.binding.connectedcar.internal.util.TextResources;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.type.ChannelGroupTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ChannelDefinitions} maps status value IDs from the API to channel definitions.
 *
 * @author Markus Michels - Initial contribution
 * @author Lorenzo Bernardi - Additional contribution
 * @author Thomas Knaller - Maintainer
 * @author Dr. Yves Kreis - Maintainer
 */
@NonNullByDefault
@Component(service = ChannelDefinitions.class)
public class ChannelDefinitions {
    private final Logger logger = LoggerFactory.getLogger(ChannelDefinitions.class);
    private static final Map<String, ChannelIdMapEntry> CHANNEL_DEFINITIONS = new ConcurrentHashMap<>();
    private final TextResources resources;

    public class ChannelIdMapEntry {
        private final TextResources resources;

        public String id = "";
        public String symbolicName = "";
        public String channelName = "";
        public String itemType = "";
        public String groupName = "";

        public boolean disabled = false;
        public boolean advanced = false;
        public boolean readOnly = true;
        public String options = "";
        public @Nullable Unit<?> fromUnit;
        public @Nullable Unit<?> unit;
        public int digits = -1;
        public int min = -1;
        public int max = -1;
        public int step = -1;
        public String pattern = "";

        @Activate
        public ChannelIdMapEntry(@Reference TextResources resources) {
            this.resources = resources;
        }

        public String getGroup() {
            return !groupName.isEmpty() ? groupName : CHANNEL_GROUP_STATUS;
        }

        public String getGroupIndex() {
            if (groupName.isEmpty()) {
                return "";
            }
            char index = groupName.charAt(groupName.length() - 1);
            return Character.isDigit(index) ? String.valueOf(index) : "";
        }

        public ChannelGroupTypeUID getGroupTypeUID() {
            return new ChannelGroupTypeUID(BINDING_ID, groupName);
        }

        public ChannelTypeUID getChannelTypeUID() {
            String groupIndex = ""; // getGroupIndex();
            return new ChannelTypeUID(BINDING_ID, groupIndex.isEmpty() ? channelName : channelName + groupIndex);
        }

        public String getLabel() {
            String key = channelName;
            Character index = channelName.charAt(key.length() - 1);
            boolean hasIndex = Character.isDigit(index);
            if (hasIndex) {
                key = key.substring(0, key.length() - 1); // ignore channel index for lookup
            }
            String label = getChannelAttribute(resources, key, "label");
            if (label.isEmpty()) {
                throw new IllegalArgumentException("Missing label in channel definition " + channelName);
            }
            if (hasIndex) {
                label = label + index;
            }

            if (groupName.isEmpty()) {
                return label;
            }

            return groupName + "_" + label.replaceAll("[ /\\(\\)]", "");
        }

        public String getDescription() {
            return getChannelAttribute(resources, channelName, "description");
        }

        public String getAdvanced() {
            return getChannelAttribute(resources, channelName, "advanced");
        }

        public String getReadOnly() {
            return getChannelAttribute(resources, channelName, "readonly");
        }

        public Integer getMin() {
            String value = getChannelAttribute(resources, channelName, "min");
            return !value.isEmpty() ? Integer.parseInt(value) : -1;
        }

        public Integer getMax() {
            String value = getChannelAttribute(resources, channelName, "max");
            return !value.isEmpty() ? Integer.parseInt(value) : -1;
        }

        public Integer getStep() {
            String value = getChannelAttribute(resources, channelName, "step");
            return !value.isEmpty() ? Integer.parseInt(value) : -1;
        }

        public String getOptions() {
            return getChannelAttribute(resources, channelName, "options");
        }

        public String getPattern() {
            return getChannelAttribute(resources, channelName, "pattern");
        }

        public int getDigits() {
            String digits = getChannelAttribute(resources, channelName, "digits");
            return !digits.isEmpty() ? Integer.parseInt(digits) : -1;
        }
    }

    @Activate
    public ChannelDefinitions(@Reference TextResources resources) {
        this.resources = resources;
        initializeChannelTable();
    }

    public Map<String, ChannelIdMapEntry> getDefinitions() {
        return CHANNEL_DEFINITIONS;
    }

    /**
     * Lookup channel definition from channel name
     *
     * @param id Channel Id to match
     * @return Returns channel definition
     */
    public @Nullable ChannelIdMapEntry find(String id) {
        for (Map.Entry<String, ChannelIdMapEntry> e : CHANNEL_DEFINITIONS.entrySet()) {
            String key = e.getKey();
            if (!key.isEmpty() && id.equalsIgnoreCase(key)) {
                return e.getValue();
            }
            ChannelIdMapEntry v = e.getValue();
            if ((!v.symbolicName.isEmpty() && id.startsWith(v.symbolicName))
                    || (!v.channelName.isEmpty() && id.equalsIgnoreCase(v.channelName))) {
                return v;
            }
        }
        return null;
    }

    /**
     * Map Datapoint units returned from the API to OH Units
     *
     * @param field Includes value and unit as returned from API
     * @param definition Channel definition to be updated
     * @return Returns updated definition
     */
    public ChannelIdMapEntry updateDefinition(CNStatusField field, ChannelIdMapEntry definition) {
        String itemType = "";
        Unit<?> unit = null;

        String fieldUnit = getString(field.unit);
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

    private ChannelIdMapEntry add(String name, String id, String channel, String itemType, String group,
            @Nullable Unit<?> unit, boolean advanced, boolean readOnly) {
        if (!channel.isEmpty() && (group.isEmpty() || itemType.isEmpty())) {
            logger.warn("Incomplete channel definitoion: SYMNAME={}, group={}, chan={}, type={}", name, group, channel,
                    itemType);
        }
        ChannelIdMapEntry entry = new ChannelIdMapEntry(resources);
        entry.id = !id.isEmpty() ? id : channel;
        entry.symbolicName = !name.isEmpty() ? name : channel.toUpperCase();
        entry.groupName = !group.isEmpty() ? group : CHANNEL_GROUP_STATUS;
        entry.channelName = channel;
        entry.itemType = itemType;
        entry.unit = ((unit == null) && ITEMT_PERCENT.equals(itemType)) ? Units.PERCENT : unit;
        entry.advanced = advanced;
        entry.readOnly = group.equals(CHANNEL_GROUP_STATUS) || group.equals(CHANNEL_GROUP_GENERAL) ? true : readOnly;

        entry.min = entry.getMin();
        entry.max = entry.getMax();
        entry.options = entry.getOptions();
        entry.pattern = entry.getPattern();
        entry.digits = entry.getDigits();
        if (entry.digits == -1 && !entry.pattern.isEmpty()) {
            if (entry.pattern.startsWith("%.") && entry.pattern.indexOf('f') > 0) {
                String digits = substringBetween(entry.pattern, "%.", "f");
                entry.digits = Integer.parseInt(digits);
            } else if (entry.pattern.startsWith("%d")) {
                entry.digits = 0;
            }
        } else if (entry.digits > 0 && entry.pattern.isEmpty()) {
            entry.pattern = "%." + entry.digits + "f %%unit%%";
        }

        if (!CHANNEL_DEFINITIONS.containsKey(entry.id)) {
            CHANNEL_DEFINITIONS.put(entry.id, entry);
        }

        return entry;
    }

    public static String getGroupAttribute(TextResources resources, String groupName, String attribute) {
        int len = groupName.length() - 1;
        char index = groupName.charAt(len);
        String groupType = Character.isDigit(index) ? groupName.substring(0, len) : groupName;
        String key = "thing-type." + BINDING_ID + ".vehicle.group." + groupType + "." + attribute;
        String value = resources.getText(key);
        return !value.equals(key) ? value : "";
    }

    /**
     * Return attribute (label, description etc.) from properties file
     *
     * @param attribute Attribute to lookup from i18n properties
     * @return Returns the attribute value as defined in the properties file
     */
    public static String getChannelAttribute(TextResources resources, String channelName, String attribute) {
        String key = "channel-type." + BINDING_ID + "." + channelName + "." + attribute;
        String value = resources.getText(key);
        return !value.equals(key) ? value : "";
    }

    /**
     * The table provides the mapping from the Datapoint Id to channel definition.
     * Channel label and description is retrieved from i18 to support multi-language resources
     */
    private void initializeChannelTable() {
        // Status
        add("GT0_KILOMETER_STATUS", "0x0101010002", CHANNEL_STATUS_ODOMETER, ITEMT_DISTANCE, CHANNEL_GROUP_STATUS,
                KILOMETRE);
        add("TEMPERATURE_OUTSIDE", "0x0301020001", CHANNEL_STATUS_TEMPOUT, ITEMT_TEMP, CHANNEL_GROUP_STATUS,
                SIUnits.CELSIUS);
        add("STATE3_PARKING_LIGHT", "0x0301010001", CHANNEL_STATUS_PLIGHT, ITEMT_SWITCH);
        add("STATE30_PARKING_BRAKE", "0x0301030001", CHANNEL_STATUS_PBRAKE, ITEMT_SWITCH);
        add("STATE3_SPOILER", "0x0301050011");
        add("POS_SPOILER", "0x0301050012");
        add("STATE1_SERVICE_FLAP", "0x030105000F");
        add("POS_SERVICE_FLAP", "0x0301050010");
        add("CURRENT_SPEED", "0x0301030004", CHANNEL_STATUS_SPEED, ITEMT_SPEED, CHANNEL_GROUP_STATUS,
                SIUnits.KILOMETRE_PER_HOUR);
        add("BEM", "0x0301030003"); // Battery Energy Management, always 0

        // Range
        add("FUEL_LEVEL_PERCENT", "0x030103000A", CHANNEL_RANGE_FUEL, ITEMT_PERCENT, CHANNEL_GROUP_RANGE, PERCENT);
        add("FUEL_METHOD", "0x030103000B", CHANNEL_RANGE_FMETHOD, ITEMT_STRING, CHANNEL_GROUP_RANGE, null, true, true); // '0':'measured',
        // '1':'calculated'
        add("TOTAL_RANGE", "0x0301030005", CHANNEL_RANGE_TOTAL, ITEMT_DISTANCE, CHANNEL_GROUP_RANGE, KILOMETRE);
        add("PRIMARY_RANGE", "0x0301030006", CHANNEL_RANGE_PRANGE, ITEMT_DISTANCE, CHANNEL_GROUP_RANGE, KILOMETRE, true,
                true);
        add("SECONDARY_RANGE", "0x0301030008", CHANNEL_RANGE_SRANGE, ITEMT_DISTANCE, CHANNEL_GROUP_RANGE, KILOMETRE,
                true, true);
        add("CHARGING_LEVEL_PERCENT", "0x0301030002", CHANNEL_CHARGER_CHGLVL, ITEMT_PERCENT, CHANNEL_GROUP_CHARGER,
                PERCENT, true, true);
        add("GASLEVEL_IN_PERCENT", "0x030103000D", CHANNEL_RANGE_GAS, ITEMT_PERCENT, CHANNEL_GROUP_RANGE, PERCENT, true,
                true);
        add("PRIMARY_FTYPE", "0x0301030007");
        add("SECONDARY_FTYPE", "0x0301030009");

        // Maintenance
        add("MAINT_ALARM_INSPECTION", "0x0203010006", CHANNEL_MAINT_ALARMINSP, ITEMT_SWITCH, CHANNEL_GROUP_MAINT);
        add("MAINT_DISTANCE_INSPECTION", "0x0203010003", CHANNEL_MAINT_DISTINSP, ITEMT_DISTANCE, CHANNEL_GROUP_MAINT,
                KILOMETRE);
        add("MAINT_INSPECTION_TIME", "0x0203010004", CHANNEL_MAINT_DISTTIME, ITEMT_TIME, CHANNEL_GROUP_MAINT, QDAYS);
        add("MAINT_ALARM_OIL_CHANGE", "0x0203010005", CHANNEL_MAINT_OILWARNCHG, ITEMT_SWITCH, CHANNEL_GROUP_MAINT);
        add("MAINT_ALARM_OIL_MINIMUM", "0x0204040002", CHANNEL_MAINT_OILWARNLVL, ITEMT_SWITCH, CHANNEL_GROUP_MAINT);
        add("MAINT_OIL_DISTANCE_CHANGE", "0x0203010001", CHANNEL_MAINT_OILDIST, ITEMT_DISTANCE, CHANNEL_GROUP_MAINT,
                KILOMETRE);
        add("MAINT_OIL_TIME_CHANGE", "0x0203010002", CHANNEL_MAINT_OILINTV, ITEMT_TIME, CHANNEL_GROUP_MAINT, QDAYS);
        add("MAINT_OIL_DIPSTICK", "0x0204040003", CHANNEL_MAINT_OILPERC, ITEMT_PERCENT, CHANNEL_GROUP_MAINT, PERCENT);
        add("MAINT_OIL_LEVEL_AMOUNT", "0x0204040001");
        add("MAINT_OIL_LEVEL_DISPLAY", "0x0204040004");
        add("MAINT_ADBLUE_DISTANCE", "0x02040C0001", CHANNEL_MAINT_ABDIST, ITEMT_DISTANCE, CHANNEL_GROUP_MAINT,
                KILOMETRE);

        add("MAINT_MONTHLY_MILEAGE", "0x0203010007", CHANNEL_STATUS_MMILAGE, ITEMT_DISTANCE, CHANNEL_GROUP_STATUS,
                KILOMETRE);

        // Doors/trunk
        add("STATE3_LEFT_FRONT_DOOR", "0x0301040002", CHANNEL_DOORS_FLSTATE, ITEMT_CONTACT, CHANNEL_GROUP_DOORS);
        add("LOCK2_LEFT_FRONT_DOOR", "0x0301040001", CHANNEL_DOORS_FLLOCKED, ITEMT_SWITCH, CHANNEL_GROUP_DOORS);
        add("SAFETY_LEFT_FRONT_DOOR", "0x0301040003"/* , "doorFrontLeftSafety", ITEMT_SWITCH, CHANNEL_GROUP_DOORS */);
        add("STATE3_RIGHT_FRONT_DOOR", "0x0301040008", CHANNEL_DOORS_FRSTATE, ITEMT_CONTACT, CHANNEL_GROUP_DOORS);
        add("LOCK2_RIGHT_FRONT_DOOR", "0x0301040007", CHANNEL_DOORS_FRLOCKED, ITEMT_SWITCH, CHANNEL_GROUP_DOORS);
        add("SAFETY_RIGHT_FRONT_DOOR", "0x0301040009"/* , "doorFrontRightSafety", ITEMT_SWITCH, CHANNEL_GROUP_DOORS */);
        add("STATE3_LEFT_REAR_DOOR", "0x0301040005", CHANNEL_DOORS_RLSTATE, ITEMT_CONTACT, CHANNEL_GROUP_DOORS);
        add("LOCK2_LEFT_REAR_DOOR", "0x0301040004", CHANNEL_DOORS_RLLOCKED, ITEMT_SWITCH, CHANNEL_GROUP_DOORS);
        add("SAFETY_LEFT_REAR_DOOR", "0x0301040006"/* , "doorRearLeftSafety", ITEMT_SWITCH, CHANNEL_GROUP_DOORS */);
        add("STATE3_RIGHT_REAR_DOOR", "0x030104000B", CHANNEL_DOORS_RRSTATE, ITEMT_CONTACT, CHANNEL_GROUP_DOORS);
        add("LOCK2_RIGHT_REAR_DOOR", "0x030104000A", CHANNEL_DOORS_RRLOCKED, ITEMT_SWITCH, CHANNEL_GROUP_DOORS);
        add("SAFETY_RIGHT_REAR_DOOR", "0x030104000C"/* , "doorRearRightSafety", ITEMT_SWITCH, CHANNEL_GROUP_DOORS */);
        add("STATE3_HOOD", "0x0301040011", CHANNEL_DOORS_HOODSTATE, ITEMT_CONTACT, CHANNEL_GROUP_DOORS);
        add("LOCK3_HOOD", "0x0301040010", CHANNEL_DOORS_HOODLOCKED, ITEMT_SWITCH, CHANNEL_GROUP_DOORS);
        add("SAFETY_HOOD", "0x0301040012"/* , "hoodSafety", ITEMT_SWITCH, CHANNEL_GROUP_DOORS */);
        add("STATE3_TRUNK_LID", "0x030104000E", CHANNEL_DOORS_TRUNKLSTATE, ITEMT_CONTACT, CHANNEL_GROUP_DOORS);
        add("LOCK2_TRUNK_LID", "0x030104000D", CHANNEL_DOORS_TRUNKLLOCKED, ITEMT_SWITCH, CHANNEL_GROUP_DOORS);
        add("SAFETY_TRUNK_LID", "0x030104000F"/* , "trunkLidSafety", ITEMT_SWITCH, CHANNEL_GROUP_DOORS */);
        add("STATE3_CONVERTIBLE_TOP", "0x0301050009", CHANNEL_DOORS_CTOPSTATE, ITEMT_CONTACT, CHANNEL_GROUP_DOORS);
        add("POS_CONVERTIBLE_TOP", "0x030105000A", CHANNEL_DOORS_CTOPPOS, ITEMT_PERCENT, CHANNEL_GROUP_DOORS);
        add("", "", CHANNEL_DOORS_ITAILGSTATE, ITEMT_CONTACT, CHANNEL_GROUP_DOORS);
        add("", "", CHANNEL_STATUS_SWUPDATE, ITEMT_SWITCH, CHANNEL_GROUP_STATUS);
        add("", "", CHANNEL_STATUS_DEEPSLEEP, ITEMT_SWITCH, CHANNEL_GROUP_STATUS);

        // Windows
        add("STATE3_LEFT_FRONT_WINDOW", "0x0301050001", CHANNEL_WIN_FLSTATE, ITEMT_CONTACT, CHANNEL_GROUP_WINDOWS);
        add("POS_LEFT_FRONT_WINDOW", "0x0301050002", CHANNEL_WIN_FLPOS, ITEMT_PERCENT, CHANNEL_GROUP_WINDOWS);
        add("STATE3_LEFT_REAR_WINDOW", "0x0301050003", CHANNEL_WIN_RLSTATE, ITEMT_CONTACT, CHANNEL_GROUP_WINDOWS);
        add("POS_LEFT_REAR_WINDOW", "0x0301050004", CHANNEL_WIN_RLPOS, ITEMT_PERCENT, CHANNEL_GROUP_WINDOWS);
        add("STATE3_RIGHT_FRONT_WINDOW", "0x0301050005", CHANNEL_WIN_FRSTATE, ITEMT_CONTACT, CHANNEL_GROUP_WINDOWS);
        add("POS_RIGHT_FRONT_WINDOW", "0x0301050006", CHANNEL_WIN_FRPOS, ITEMT_PERCENT, CHANNEL_GROUP_WINDOWS);
        add("STATE3_RIGHT_REAR_WINDOW", "0x0301050007", CHANNEL_WIN_RRSTATE, ITEMT_CONTACT, CHANNEL_GROUP_WINDOWS);
        add("POS_RIGHT_REAR_WINDOW", "0x0301050008", CHANNEL_WIN_RRPOS, ITEMT_PERCENT, CHANNEL_GROUP_WINDOWS);
        add("STATE3_ROOF_FRONT_COVER", "0x030105000B", CHANNEL_WIN_FROOFSTATE, ITEMT_CONTACT, CHANNEL_GROUP_WINDOWS);
        add("POS_ROOF_FRONT_COVER", "0x030105000C", CHANNEL_WIN_FROOFPOS, ITEMT_PERCENT, CHANNEL_GROUP_WINDOWS);
        add("STATE3_ROOF_REAR_COVER", "0x030105000D", CHANNEL_WIN_RROOFSTATE, ITEMT_CONTACT, CHANNEL_GROUP_WINDOWS);
        add("POS_ROOF_REAR_COVER", "0x030105000E", CHANNEL_WIN_RROOFPOS, ITEMT_PERCENT, CHANNEL_GROUP_WINDOWS);
        add("STATE3_SUNROOF_COVER", "", CHANNEL_WIN_SROOFSTATE, ITEMT_CONTACT, CHANNEL_GROUP_WINDOWS);
        add("POS_SUNROOF_COVER", "", CHANNEL_WIN_SROOFPOS, ITEMT_PERCENT, CHANNEL_GROUP_WINDOWS);

        // Tires
        add("STATE1_TPRESS_LEFT_FRONT_CURRENT", "0x0301060001", CHANNEL_TIREP_FRONTLEFT, ITEMT_SWITCH,
                CHANNEL_GROUP_TIRES);
        add("STATE1_TPRESS_LEFT_FRONT_DESIRED", "0x0301060002");
        add("STATE1_TPRESS_LEFT_FRONT_TIRE_DIFF", "0x030106000B");
        add("STATE1_TPRESS_LEFT_REAR_CURRENT", "0x0301060003", CHANNEL_TIREP_REARLEFT, ITEMT_SWITCH,
                CHANNEL_GROUP_TIRES);
        add("STATE1_TPRESS_LEFT_REAR_DESIRED", "0x0301060004");
        add("STATE1_TPRESS_LEFT_REAR_TIRE_DIFF", "0x030106000C");
        add("STATE1_TPRESS_RIGHT_FRONT_CURRENT", "0x0301060005", CHANNEL_TIREP_FRONTRIGHT, ITEMT_SWITCH,
                CHANNEL_GROUP_TIRES);
        add("STATE1_TPRESS_RIGHT_FRONT_DESIRED", "0x0301060006");
        add("STATE1_TPRESS_RIGHT_FRONT_TIRE_DIFF", "0x030106000D");
        add("STATE1_TPRESS_RIGHT_REAR_CURRENT", "0x0301060007", CHANNEL_TIREP_REARRIGHT, ITEMT_SWITCH,
                CHANNEL_GROUP_TIRES);
        add("STATE1_TPRESS_RIGHT_REAR_DESIRED", "0x0301060008");
        add("STATE1_TPRESS_RIGHT_REAR_TIRE_DIFF", "0x030106000E");
        add("STATE1_TPRESS_SPARE_CURRENT", "0x0301060009", CHANNEL_TIREP_SPARE, ITEMT_SWITCH, CHANNEL_GROUP_TIRES);
        add("STATE1_TPRESS_SPARE_DESIRED", "0x030106000A");
        add("STATE1_TPRESS_SPARE_DIFF", "0x030106000F");
        add("", "", CHANNEL_TIREP_INNERREARLEFT, ITEMT_SWITCH, CHANNEL_GROUP_TIRES);
        add("", "", CHANNEL_TIREP_INNERREARRIGHT, ITEMT_SWITCH, CHANNEL_GROUP_TIRES);

        // Misc
        add("UTC_TIME_STATUS", "0x0101010001");
        add("UNKNOWN_0x02040C0002", "0x02040C0002"); // no yet decoded

        // Group status
        String group = CHANNEL_GROUP_STATUS;
        add("", "", CHANNEL_STATUS_ERROR, ITEMT_STRING);
        add("", "", CHANNEL_STATUS_PBRAKE, ITEMT_SWITCH);
        add("", "", CHANNEL_STATUS_LIGHTS, ITEMT_SWITCH);
        add("", "", CHANNEL_CAR_MOVING, ITEMT_SWITCH);
        add("", "", CHANNEL_STATUS_LOCKED, ITEMT_SWITCH);
        add("", "", CHANNEL_STATUS_MAINTREQ, ITEMT_SWITCH);
        add("", "", CHANNEL_STATUS_WINCLOSED, ITEMT_SWITCH);
        add("", "", CHANNEL_STATUS_DOORSCLOSED, ITEMT_SWITCH);
        add("", "", CHANNEL_STATUS_TIRESOK, ITEMT_SWITCH);
        add("", "", CHANNEL_STATUS_TIMEINCAR, ITEMT_DATETIME);

        // Group general
        group = CHANNEL_GROUP_GENERAL;
        add("", "", CHANNEL_GENERAL_UPDATED, ITEMT_DATETIME, group);
        add("", "", CHANNEL_GENERAL_ACTION, ITEMT_STRING, group);
        add("", "", CHANNEL_GENERAL_ACTION_STATUS, ITEMT_STRING, group);
        add("", "", CHANNEL_GENERAL_ACTION_PENDING, ITEMT_SWITCH, group);
        add("", "", CHANNEL_GENERAL_ACTION, ITEMT_STRING, group, null, false, true);
        add("", "", CHANNEL_GENERAL_ACTION_STATUS, ITEMT_STRING, group, null, false, true);
        add("", "", CHANNEL_GENERAL_ACTION_PENDING, ITEMT_SWITCH, group, null, false, true);
        add("", "", CHANNEL_GENERAL_RATELIM, ITEMT_NUMBER, group, null, false, true);

        // Group control
        group = CHANNEL_GROUP_CONTROL;
        add("", "", CHANNEL_CONTROL_UPDATE, ITEMT_SWITCH, group, null, false, false);
        add("", "", CHANNEL_CONTROL_LOCK, ITEMT_SWITCH, group, null, false, false);
        add("", "", CHANNEL_CONTROL_ENGINE, ITEMT_SWITCH, group, null, false, false);
        add("", "", CHANNEL_CONTROL_RESTART, ITEMT_SWITCH, group, null, false, false);
        add("", "", CHANNEL_CONTROL_CHARGER, ITEMT_SWITCH, group, null, false, false);
        add("", "", CHANNEL_CONTROL_TARGETCHG, ITEMT_PERCENT, group, null, false, false);
        add("", "", CHANNEL_CONTROL_CLIMATER, ITEMT_SWITCH, group, null, false, false);
        add("", "", CHANNEL_CONTROL_TARGET_TEMP, ITEMT_TEMP, group, SIUnits.CELSIUS);
        add("", "", CHANNEL_CONTROL_WINHEAT, ITEMT_SWITCH, group, null, false, false);
        add("", "", CHANNEL_CONTROL_PREHEAT, ITEMT_SWITCH, group, null, false, false);
        add("", "", CHANNEL_CONTROL_VENT, ITEMT_SWITCH, group, null, false, false);
        add("", "", CHANNEL_CONTROL_DURATION, ITEMT_NUMBER, group, null, false, false);
        add("", "", CHANNEL_CONTROL_FLASH, ITEMT_SWITCH, group, null, false, false);
        add("", "", CHANNEL_CONTROL_HONKFLASH, ITEMT_SWITCH, group, null, false, false);
        add("", "", CHANNEL_CONTROL_HFDURATION, ITEMT_NUMBER, group, null, true, false);

        // Group location
        group = CHANNEL_GROUP_LOCATION;
        add("", "", CHANNEL_LOCATTION_GEO, ITEMT_LOCATION, group);
        add("", "", CHANNEL_LOCATTION_ADDRESS, ITEMT_STRING, group);
        add("", "", CHANNEL_LOCATTION_TIME, ITEMT_DATETIME, group);
        add("", "", CHANNEL_PARK_LOCATION, ITEMT_LOCATION, group);
        add("", "", CHANNEL_PARK_ADDRESS, ITEMT_STRING, group);
        add("", "", CHANNEL_PARK_TIME, ITEMT_DATETIME, group);

        // Group charger
        group = CHANNEL_GROUP_CHARGER;
        add("", "", CHANNEL_CHARGER_CHG_STATE, ITEMT_STRING, group);
        add("", "", CHANNEL_CHARGER_MODE, ITEMT_STRING, group);
        add("", "", CHANNEL_CHARGER_STATUS, ITEMT_STRING, group);
        add("", "", CHANNEL_CHARGER_MAXCURRENT, ITEMT_AMP, group, Units.AMPERE, false, false);
        add("", "", CHANNEL_CHARGER_REMAINING, ITEMT_TIME, group, Units.MINUTE);
        add("", "", CHANNEL_CHARGER_POWER, ITEMT_VOLT, group);
        add("", "", CHANNEL_CHARGER_PWR_STATE, ITEMT_STRING, group);
        add("", "", CHANNEL_CHARGER_RATE, ITEMT_NUMBER, group);
        add("", "", CHANNEL_CHARGER_CHGLVL, ITEMT_PERCENT, group);
        add("", "", CHANNEL_CHARGER_FLOW, ITEMT_STRING, group);
        add("", "", CHANNEL_CHARGER_BAT_STATE, ITEMT_NUMBER, group);
        add("", "", CHANNEL_CHARGER_LOCK_STATE, ITEMT_SWITCH, group);
        add("", "", CHANNEL_CHARGER_PLUG_STATE, ITEMT_STRING, group);
        add("", "", CHANNEL_CHARGER_ERROR, ITEMT_NUMBER, group);
        add("", "", CHANNEL_CHARGER_NAME, ITEMT_STRING, group);
        add("", "", CHANNEL_CHARGER_ADDRESS, ITEMT_STRING, group);
        add("", "", CHANNEL_CHARGER_LAST_CONNECT, ITEMT_DATETIME, group);
        add("", "", CHANNEL_CHARGER_CYCLES, ITEMT_NUMBER, group);
        add("", "", CHANNEL_CHARGER_ENERGY, ITEMT_ENERGY, group, Units.KILOWATT_HOUR, false, true);

        // Group climater
        group = CHANNEL_GROUP_CLIMATER;
        add("", "", CHANNEL_CLIMATER_GEN_STATE, ITEMT_STRING, group);
        add("", "", CHANNEL_CLIMATER_HEATSOURCE, ITEMT_STRING, group, null, true, false);
        add("", "", CHANNEL_CLIMATER_REMAINING, ITEMT_TIME, group, Units.MINUTE);
        add("", "", CHANNEL_CLIMATER_MIRROR_HEAT, ITEMT_SWITCH, group);

        // Group destination
        group = CHANNEL_GROUP_DEST_PRE;
        add("", "", CHANNEL_DEST_NAME, ITEMT_STRING, group);
        add("", "", CHANNEL_DEST_POI, ITEMT_STRING, group);
        add("", "", CHANNEL_DEST_GEO, ITEMT_LOCATION, group);
        add("", "", CHANNEL_DEST_STREET, ITEMT_STRING, group);
        add("", "", CHANNEL_DEST_CITY, ITEMT_STRING, group);
        add("", "", CHANNEL_DEST_ZIP, ITEMT_STRING, group, null, true, true);
        add("", "", CHANNEL_DEST_COUNTY, ITEMT_STRING, group, null, true, true);
        add("", "", CHANNEL_DEST_SOURCE, ITEMT_STRING, group, null, true, true);

        // Group rluHistory
        group = CHANNEL_GROUP_RLUHIST;
        add("", "", CHANNEL_RLUHIST_OP, ITEMT_STRING, group, null, false, true);
        add("", "", CHANNEL_RLUHIST_TS, ITEMT_DATETIME, group, null, false, true);
        add("", "", CHANNEL_RLUHIST_RES, ITEMT_STRING, group, null, false, true);

        // Group speedAlerts
        group = CHANNEL_GROUP_SPEEDALERT;
        add("", "", CHANNEL_SPEEDALERT_TYPE, ITEMT_STRING, group, null, false, true);
        add("", "", CHANNEL_SPEEDALERT_TIME, ITEMT_DATETIME, group, null, false, true);
        add("", "", CHANNEL_SPEEDALERT_DESCR, ITEMT_STRING, group, null, false, true);
        add("", "", CHANNEL_SPEEDALERT_LIMIT, ITEMT_SPEED, group, null, false, true);

        // Group geoFenceAlerts
        group = CHANNEL_GROUP_SPEEDALERT;
        add("", "", CHANNEL_GEOFENCE_TYPE, ITEMT_STRING, group, null, false, true);
        add("", "", CHANNEL_GEOFENCE_TIME, ITEMT_DATETIME, group, null, false, true);
        add("", "", CHANNEL_GEOFENCE_DESCR, ITEMT_STRING, group, null, false, true);

        // Group
        group = CHANNEL_GROUP_STRIP; // tripShort
        add("", "", CHANNEL_TRIP_TIME, ITEMT_DATETIME, group, null, false, true);
        add("", "", CHANNEL_TRIP_AVG_ELCON, ITEMT_ENERGY, group, Units.KILOWATT_HOUR, false, true);
        add("", "", CHANNEL_TRIP_AVG_FUELCON, ITEMT_VOLUME, group, Units.LITRE, false, true);
        add("", "", CHANNEL_TRIP_AVG_SPEED, ITEMT_SPEED, group, SIUnits.KILOMETRE_PER_HOUR, false, true);
        add("", "", CHANNEL_TRIP_START_MIL, ITEMT_DISTANCE, group, KILOMETRE, false, true);
        add("", "", CHANNEL_TRIP_MILAGE, ITEMT_DISTANCE, group, KILOMETRE, false, true);
        add("", "", CHANNEL_TRIP_OVR_MILAGE, ITEMT_DISTANCE, group, KILOMETRE, true, true);
        group = CHANNEL_GROUP_LTRIP; // tripLong
        add("", "", CHANNEL_TRIP_TIME, ITEMT_DATETIME, group);
        add("", "", CHANNEL_TRIP_AVG_ELCON, ITEMT_ENERGY, group, Units.KILOWATT_HOUR, false, true);
        add("", "", CHANNEL_TRIP_AVG_FUELCON, ITEMT_VOLUME, group, Units.LITRE, false, true);
        add("", "", CHANNEL_TRIP_AVG_SPEED, ITEMT_SPEED, group, SIUnits.KILOMETRE_PER_HOUR, false, true);
        add("", "", CHANNEL_TRIP_START_MIL, ITEMT_DISTANCE, group, KILOMETRE, false, true);
        add("", "", CHANNEL_TRIP_MILAGE, ITEMT_DISTANCE, group, KILOMETRE, false, true);
        add("", "", CHANNEL_TRIP_OVR_MILAGE, ITEMT_DISTANCE, group, KILOMETRE, true, true);

        // Subscriptions
        group = CHANNEL_GROUP_SUBSCRIPTION;
        add("", "", CHANNEL_SUB_ENDDATE, ITEMT_DATETIME, group);
        add("", "", CHANNEL_SUB_STATUS, ITEMT_STRING, group);
        add("", "", CHANNEL_SUB_TARIFF, ITEMT_STRING, group);
        add("", "", CHANNEL_SUB_MFEE, ITEMT_NUMBER, group);

        // RFID cards
        group = CHANNEL_CHANNEL_GROUP_RFID;
        add("", "", CHANNEL_RFID_ID, ITEMT_STRING, group);
        add("", "", CHANNEL_RFID_PUBLIC, ITEMT_SWITCH, group, null, true, true);
        add("", "", CHANNEL_RFID_STATUS, ITEMT_STRING, group);
        add("", "", CHANNEL_RFID_UPDATE, ITEMT_DATETIME, group);

        // Chasrging Records
        group = CHANNEL_CHANNEL_GROUP_TRANSACTIONS;
        add("", "", CHANNEL_TRANS_ID, ITEMT_STRING, group, null, true, true);
        add("", "", CHANNEL_TRANS_PUBLIC, ITEMT_SWITCH, group);
        add("", "", CHANNEL_TRANS_LOCATION, ITEMT_LOCATION, group);
        add("", "", CHANNEL_TRANS_ADDRESS, ITEMT_STRING, group);
        add("", "", CHANNEL_TRANS_SUBID, ITEMT_STRING, group, null, true, true);
        add("", "", CHANNEL_TRANS_EVSE, ITEMT_STRING, group);
        add("", "", CHANNEL_TRANS_PTYPE, ITEMT_STRING, group, null, true, true);
        add("", "", CHANNEL_TRANS_START, ITEMT_STRING, group);
        add("", "", CHANNEL_TRANS_END, ITEMT_STRING, group);
        add("", "", CHANNEL_TRANS_ENERGY, ITEMT_ENERGY, group, Units.KILOWATT_HOUR, false, true);
        add("", "", CHANNEL_TRANS_PRICE, ITEMT_NUMBER, group);
        add("", "", CHANNEL_TRANS_DURATION, ITEMT_TIME, group, Units.MINUTE);
        add("", "", CHANNEL_TRANS_RFID, ITEMT_STRING, group);
        add("", "", CHANNEL_TRANS_TARIFF, ITEMT_STRING, group);
    }

    private ChannelIdMapEntry add(String name, String id, String channel, String itemType, String group,
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

    public void dumpChannelDefinitions(String thindId) {
        try (FileWriter myWriter = new FileWriter("ConnectedCarChannels_" + thindId + ".md")) {
            String lastGroup = "";
            for (Map.Entry<String, ChannelIdMapEntry> m : CHANNEL_DEFINITIONS.entrySet()) {
                ChannelIdMapEntry e = m.getValue();
                if (!e.channelName.isEmpty()) {
                    String group = lastGroup.equals(e.groupName) ? "" : e.groupName;
                    String s = String.format("| %-12.12s | %-27.27s | %-20.20s | %-7s | %-95s |\n", group,
                            e.channelName, e.itemType, e.readOnly ? "yes" : "no", e.getDescription());
                    myWriter.write(s);
                    lastGroup = e.groupName;
                }
            }
        } catch (IOException e) {
        }
    }
}
