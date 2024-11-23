/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.solarwatt.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.solarwatt.internal.domain.SolarwattTag;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SolarwattBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Sven Carstens - Initial contribution
 */
@NonNullByDefault
public class SolarwattBindingConstants {

    private SolarwattBindingConstants() {
    }

    public static final String BINDING_ID = "solarwatt";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ENERGY_MANAGER = new ThingTypeUID(BINDING_ID, "energymanager");
    public static final ThingTypeUID THING_TYPE_INVERTER = new ThingTypeUID(BINDING_ID, "inverter");
    public static final ThingTypeUID THING_TYPE_LOCATION = new ThingTypeUID(BINDING_ID, "location");
    public static final ThingTypeUID THING_TYPE_BATTERYCONVERTER = new ThingTypeUID(BINDING_ID, "batteryconverter");
    public static final ThingTypeUID THING_TYPE_POWERMETER = new ThingTypeUID(BINDING_ID, "powermeter");
    public static final ThingTypeUID THING_TYPE_EVSTATION = new ThingTypeUID(BINDING_ID, "evstation");
    public static final ThingTypeUID THING_TYPE_PVPLANT = new ThingTypeUID(BINDING_ID, "pvplant");
    public static final ThingTypeUID THING_TYPE_GRIDFLOW = new ThingTypeUID(BINDING_ID, "gridflow");
    public static final ThingTypeUID THING_TYPE_SMARTHEATER = new ThingTypeUID(BINDING_ID, "smartheater");

    public static final String PROPERTY_ID_NAME = "IdName";
    public static final String PROPERTY_ID_FIRMWARE = "IdFirmware";
    public static final String PROPERTY_ID_MANUFACTURER = "IdManufacturer";

    // List of all Channel ids, taken from the tagNames
    public static final SolarwattTag CHANNEL_WORK_AC_OUT = new SolarwattTag("WorkACOut");
    public static final SolarwattTag CHANNEL_WORK_AC_IN = new SolarwattTag("WorkACIn");
    public static final SolarwattTag CHANNEL_WORK_AC_IN_SESSION = new SolarwattTag("WorkACInSession");
    public static final SolarwattTag CHANNEL_POWER_INSTALLED_PEAK = new SolarwattTag("PowerInstalledPeak");
    public static final SolarwattTag CHANNEL_POWER_AC_OUT_MAX = new SolarwattTag("PowerACOutMax");
    public static final SolarwattTag CHANNEL_POWER_AC_OUT = new SolarwattTag("PowerACOut");
    public static final SolarwattTag CHANNEL_POWER_AC_IN = new SolarwattTag("PowerACIn");
    public static final SolarwattTag CHANNEL_POWER_AC_OUT_LIMIT = new SolarwattTag("PowerACOutLimit");
    public static final SolarwattTag CHANNEL_DIRECTION_METERING = new SolarwattTag("DirectionMetering");
    public static final SolarwattTag CHANNEL_POWER_IN = new SolarwattTag("PowerIn");
    public static final SolarwattTag CHANNEL_POWER_OUT = new SolarwattTag("PowerOut");
    public static final SolarwattTag CHANNEL_WORK_IN = new SolarwattTag("WorkIn");
    public static final SolarwattTag CHANNEL_WORK_OUT = new SolarwattTag("WorkOut");
    public static final SolarwattTag CHANNEL_CONSUMPTION_ENERGY_SUM = new SolarwattTag("ConsumptionEnergySum");
    public static final SolarwattTag CHANNEL_MODE_STATION = new SolarwattTag("ModeStation");
    public static final SolarwattTag CHANNEL_CONNECTIVITY_STATUS = new SolarwattTag("ConnectivityStatus");
    public static final SolarwattTag CHANNEL_TIMESTAMP = new SolarwattTag("Timestamp");
    public static final SolarwattTag CHANNEL_DATETIME = new SolarwattTag("Datetime");
    public static final SolarwattTag CHANNEL_IDTIMEZONE = new SolarwattTag("IdTimezone");
    public static final SolarwattTag CHANNEL_FRACTION_CPU_LOAD_TOTAL = new SolarwattTag("FractionCPULoadTotal");
    public static final SolarwattTag CHANNEL_FRACTION_CPU_LOAD_USER = new SolarwattTag("FractionCPULoadUser");
    public static final SolarwattTag CHANNEL_FRACTION_CPU_LOAD_KERNEL = new SolarwattTag("FractionCPULoadKernel");
    public static final SolarwattTag CHANNEL_FRACTION_CPU_LOAD_AVERAGE_LAST_MINUTE = new SolarwattTag(
            "FractionCPULoadAverageLastMinute");
    public static final SolarwattTag CHANNEL_FRACTION_CPU_LOAD_AVERAGE_LAST_FIVE_MINUTES = new SolarwattTag(
            "FractionCPULoadAverageLastFiveMinutes");
    public static final SolarwattTag CHANNEL_FRACTION_CPU_LOAD_AVERAGE_LAST_FIFTEEN_MINUTES = new SolarwattTag(
            "FractionCPULoadAverageLastFifteenMinutes");
    public static final SolarwattTag CHANNEL_MODE_CONVERTER = new SolarwattTag("ModeConverter");
    public static final SolarwattTag CHANNEL_STATE_OF_CHARGE = new SolarwattTag("StateOfCharge");
    public static final SolarwattTag CHANNEL_STATE_OF_HEALTH = new SolarwattTag("StateOfHealth");
    public static final SolarwattTag CHANNEL_TEMPERATURE_BATTERY = new SolarwattTag("TemperatureBattery");
    public static final SolarwattTag CHANNEL_POWER_BUFFERED = new SolarwattTag("PowerBuffered");
    public static final SolarwattTag CHANNEL_POWER_BUFFERED_FROM_GRID = new SolarwattTag("PowerBufferedFromGrid");
    public static final SolarwattTag CHANNEL_POWER_BUFFERED_FROM_PRODUCERS = new SolarwattTag(
            "PowerBufferedFromProducers");
    public static final SolarwattTag CHANNEL_POWER_CONSUMED = new SolarwattTag("PowerConsumed");
    public static final SolarwattTag CHANNEL_POWER_CONSUMED_UNMETERED = new SolarwattTag("PowerConsumedUnmetered");
    public static final SolarwattTag CHANNEL_POWER_CONSUMED_FROM_GRID = new SolarwattTag("PowerConsumedFromGrid");
    public static final SolarwattTag CHANNEL_POWER_CONSUMED_FROM_STORAGE = new SolarwattTag("PowerConsumedFromStorage");
    public static final SolarwattTag CHANNEL_POWER_CONSUMED_FROM_PRODUCERS = new SolarwattTag(
            "PowerConsumedFromProducers");
    public static final SolarwattTag CHANNEL_POWER_PRODUCED = new SolarwattTag("PowerProduced");
    public static final SolarwattTag CHANNEL_POWER_OUT_FROM_PRODUCERS = new SolarwattTag("PowerOutFromProducers");
    public static final SolarwattTag CHANNEL_POWER_OUT_FROM_STORAGE = new SolarwattTag("PowerOutFromStorage");
    public static final SolarwattTag CHANNEL_POWER_RELEASED = new SolarwattTag("PowerReleased");
    public static final SolarwattTag CHANNEL_POWER_SELF_CONSUMED = new SolarwattTag("PowerSelfConsumed");
    public static final SolarwattTag CHANNEL_POWER_DIRECT_CONSUMED = new SolarwattTag("PowerDirectConsumed");
    public static final SolarwattTag CHANNEL_POWER_SELF_SUPPLIED = new SolarwattTag("PowerSelfSupplied");
    public static final SolarwattTag CHANNEL_WORK_BUFFERED = new SolarwattTag("WorkBuffered");
    public static final SolarwattTag CHANNEL_WORK_BUFFERED_FROM_GRID = new SolarwattTag("WorkBufferedFromGrid");
    public static final SolarwattTag CHANNEL_WORK_BUFFERED_FROM_PRODUCERS = new SolarwattTag(
            "WorkBufferedFromProducers");
    public static final SolarwattTag CHANNEL_WORK_CONSUMED = new SolarwattTag("WorkConsumed");
    public static final SolarwattTag CHANNEL_WORK_CONSUMED_UNMETERED = new SolarwattTag("WorkConsumedUnmetered");
    public static final SolarwattTag CHANNEL_WORK_CONSUMED_FROM_GRID = new SolarwattTag("WorkConsumedFromGrid");
    public static final SolarwattTag CHANNEL_WORK_CONSUMED_FROM_STORAGE = new SolarwattTag("WorkConsumedFromStorage");
    public static final SolarwattTag CHANNEL_WORK_CONSUMED_FROM_PRODUCERS = new SolarwattTag(
            "WorkConsumedFromProducers");
    public static final SolarwattTag CHANNEL_WORK_PRODUCED = new SolarwattTag("WorkProduced");
    public static final SolarwattTag CHANNEL_WORK_OUT_FROM_PRODUCERS = new SolarwattTag("WorkOutFromProducers");
    public static final SolarwattTag CHANNEL_WORK_OUT_FROM_STORAGE = new SolarwattTag("WorkOutFromStorage");
    public static final SolarwattTag CHANNEL_WORK_RELEASED = new SolarwattTag("WorkReleased");
    public static final SolarwattTag CHANNEL_WORK_SELF_CONSUMED = new SolarwattTag("WorkSelfConsumed");
    public static final SolarwattTag CHANNEL_WORK_DIRECT_CONSUMED = new SolarwattTag("WorkDirectConsumed");
    public static final SolarwattTag CHANNEL_WORK_SELF_SUPPLIED = new SolarwattTag("WorkSelfSupplied");
    public static final SolarwattTag CHANNEL_CURRENT_LIMIT = new SolarwattTag("CurrentLimit");
    public static final SolarwattTag CHANNEL_FEED_IN_LIMIT = new SolarwattTag("FeedInLimit");
    public static final SolarwattTag CHANNEL_VOLTAGE_BATTERY_CELL_MAX = new SolarwattTag("VoltageBatteryCellMax");
    public static final SolarwattTag CHANNEL_VOLTAGE_BATTERY_CELL_MIN = new SolarwattTag("VoltageBatteryCellMin");
    public static final SolarwattTag CHANNEL_VOLTAGE_BATTERY_CELL_MEAN = new SolarwattTag("VoltageBatteryCellMean");
    public static final SolarwattTag CHANNEL_TEMPERATURE = new SolarwattTag("Temperature");
    public static final SolarwattTag CHANNEL_TEMPERATURE_SET_MAX = new SolarwattTag("TemperatureSetMax");
    public static final SolarwattTag CHANNEL_TEMPERATURE_BOILER = new SolarwattTag("TemperatureBoiler");
    public static final SolarwattTag CHANNEL_TEMPERATURE_SET_MIN = new SolarwattTag("TemperatureSetMin");
    public static final SolarwattTag CHANNEL_TEMPERATURE_SET = new SolarwattTag("TemperatureSet");

    // thing configuration and properties keys
    public static final String THING_PROPERTIES_GUID = "guid";
}
