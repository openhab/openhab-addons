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
package org.openhab.binding.solarwatt.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
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
    public static final ThingTypeUID THING_TYPE_DEVICE = new ThingTypeUID(BINDING_ID, "device");
    public static final ThingTypeUID THING_TYPE_INVERTER = new ThingTypeUID(BINDING_ID, "inverter");
    public static final ThingTypeUID THING_TYPE_LOCATION = new ThingTypeUID(BINDING_ID, "location");
    public static final ThingTypeUID THING_TYPE_BATTERYCONVERTER = new ThingTypeUID(BINDING_ID, "batteryconverter");
    public static final ThingTypeUID THING_TYPE_POWERMETER = new ThingTypeUID(BINDING_ID, "powermeter");
    public static final ThingTypeUID THING_TYPE_EVSTATION = new ThingTypeUID(BINDING_ID, "evstation");
    public static final ThingTypeUID THING_TYPE_PVPLANT = new ThingTypeUID(BINDING_ID, "pvplant");
    public static final ThingTypeUID THING_TYPE_GRIDFLOW = new ThingTypeUID(BINDING_ID, "gridflow");

    public static final String PROPERTY_ID_NAME = "IdName";
    public static final String PROPERTY_ID_FIRMWARE = "IdFirmware";
    public static final String PROPERTY_ID_MANUFACTURER = "IdManufacturer";

    // List of all Channel ids, taken from the tagNames
    public static final String CHANNEL_WORK_AC_OUT = "WorkACOut";
    public static final String CHANNEL_WORK_AC_IN = "WorkACIn";
    public static final String CHANNEL_WORK_AC_IN_SESSION = "WorkACInSession";
    public static final String CHANNEL_POWER_INSTALLED_PEAK = "PowerInstalledPeak";
    public static final String CHANNEL_POWER_AC_OUT_MAX = "PowerACOutMax";
    public static final String CHANNEL_POWER_AC_OUT = "PowerACOut";
    public static final String CHANNEL_POWER_AC_IN = "PowerACIn";
    public static final String CHANNEL_POWER_AC_OUT_LIMIT = "PowerACOutLimit";
    public static final String CHANNEL_DIRECTION_METERING = "DirectionMetering";
    public static final String CHANNEL_POWER_IN = "PowerIn";
    public static final String CHANNEL_POWER_OUT = "PowerOut";
    public static final String CHANNEL_WORK_IN = "WorkIn";
    public static final String CHANNEL_WORK_OUT = "WorkOut";
    public static final String CHANNEL_CONSUMPTION_ENERGY_SUM = "ConsumptionEnergySum";
    public static final String CHANNEL_STATE_DEVICE = "StateDevice";
    public static final String CHANNEL_MODE_STATION = "ModeStation";
    public static final String CHANNEL_CONNECTIVITY_STATUS = "ConnectivityStatus";
    public static final String CHANNEL_TIMESTAMP = "Timestamp";
    public static final String CHANNEL_IDTIMEZONE = "IdTimezone";
    public static final String CHANNEL_FRACTION_CPU_LOAD_TOTAL = "FractionCPULoadTotal";
    public static final String CHANNEL_FRACTION_CPU_LOAD_USER = "FractionCPULoadUser";
    public static final String CHANNEL_FRACTION_CPU_LOAD_KERNEL = "FractionCPULoadKernel";
    public static final String CHANNEL_FRACTION_CPU_LOAD_AVERAGE_LAST_MINUTE = "FractionCPULoadAverageLastMinute";
    public static final String CHANNEL_FRACTION_CPU_LOAD_AVERAGE_LAST_FIVE_MINUTES = "FractionCPULoadAverageLastFiveMinutes";
    public static final String CHANNEL_FRACTION_CPU_LOAD_AVERAGE_LAST_FIFTEEN_MINUTES = "FractionCPULoadAverageLastFifteenMinutes";
    public static final String CHANNEL_MODE_CONVERTER = "ModeConverter";
    public static final String CHANNEL_STATE_OF_CHARGE = "StateOfCharge";
    public static final String CHANNEL_STATE_OF_HEALTH = "StateOfHealth";
    public static final String CHANNEL_TEMPERATURE_BATTERY = "TemperatureBattery";
    public static final String CHANNEL_POWER_BUFFERED = "PowerBuffered";
    public static final String CHANNEL_POWER_BUFFERED_FROM_GRID = "PowerBufferedFromGrid";
    public static final String CHANNEL_POWER_BUFFERED_FROM_PRODUCERS = "PowerBufferedFromProducers";
    public static final String CHANNEL_POWER_CONSUMED = "PowerConsumed";
    public static final String CHANNEL_POWER_CONSUMED_FROM_GRID = "PowerConsumedFromGrid";
    public static final String CHANNEL_POWER_CONSUMED_FROM_STORAGE = "PowerConsumedFromStorage";
    public static final String CHANNEL_POWER_CONSUMED_FROM_PRODUCERS = "PowerConsumedFromProducers";
    public static final String CHANNEL_POWER_PRODUCED = "PowerProduced";
    public static final String CHANNEL_POWER_OUT_FROM_PRODUCERS = "PowerOutFromProducers";
    public static final String CHANNEL_POWER_OUT_FROM_STORAGE = "PowerOutFromStorage";
    public static final String CHANNEL_POWER_RELEASED = "PowerReleased";
    public static final String CHANNEL_POWER_SELF_CONSUMED = "PowerSelfConsumed";
    public static final String CHANNEL_POWER_DIRECT_CONSUMED = "PowerDirectConsumed";
    public static final String CHANNEL_POWER_SELF_SUPPLIED = "PowerSelfSupplied";
    public static final String CHANNEL_WORK_BUFFERED = "WorkBuffered";
    public static final String CHANNEL_WORK_BUFFERED_FROM_GRID = "WorkBufferedFromGrid";
    public static final String CHANNEL_WORK_BUFFERED_FROM_PRODUCERS = "WorkBufferedFromProducers";
    public static final String CHANNEL_WORK_CONSUMED = "WorkConsumed";
    public static final String CHANNEL_WORK_CONSUMED_FROM_GRID = "WorkConsumedFromGrid";
    public static final String CHANNEL_WORK_CONSUMED_FROM_STORAGE = "WorkConsumedFromStorage";
    public static final String CHANNEL_WORK_CONSUMED_FROM_PRODUCERS = "WorkConsumedFromProducers";
    public static final String CHANNEL_WORK_PRODUCED = "WorkProduced";
    public static final String CHANNEL_WORK_OUT_FROM_PRODUCERS = "WorkOutFromProducers";
    public static final String CHANNEL_WORK_OUT_FROM_STORAGE = "WorkOutFromStorage";
    public static final String CHANNEL_WORK_RELEASED = "WorkReleased";
    public static final String CHANNEL_WORK_SELF_CONSUMED = "WorkSelfConsumed";
    public static final String CHANNEL_WORK_DIRECT_CONSUMED = "WorkDirectConsumed";
    public static final String CHANNEL_WORK_SELF_SUPPLIED = "WorkSelfSupplied";
    public static final String CHANNEL_CURRENT_LIMIT = "CurrentLimit";
    public static final String CHANNEL_FEED_IN_LIMIT = "FeedInLimit";

    // thing configuration and properties keys
    public static final String THING_PROPERTIES_GUID = "guid";
}
