/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.paradoxalarm.internal.handlers;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link ParadoxAlarmBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public class ParadoxAlarmBindingConstants {

    public static final String BINDING_ID = "paradoxalarm";

    public static final String PARADOX_COMMUNICATOR_THING_TYPE_ID = "ip150";

    public static final String PARADOX_PANEL_THING_TYPE_ID = "panel";

    public static final String PARTITION_THING_TYPE_ID = "partition";

    public static final String ZONE_THING_TYPE_ID = "zone";

    // List of all Thing Type UIDs
    public static final ThingTypeUID COMMUNICATOR_THING_TYPE_UID = new ThingTypeUID(BINDING_ID,
            PARADOX_COMMUNICATOR_THING_TYPE_ID);
    public static final ThingTypeUID PANEL_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, PARADOX_PANEL_THING_TYPE_ID);
    public static final ThingTypeUID PARTITION_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, PARTITION_THING_TYPE_ID);
    public static final ThingTypeUID ZONE_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, ZONE_THING_TYPE_ID);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.unmodifiableSet(
            Stream.of(COMMUNICATOR_THING_TYPE_UID, PANEL_THING_TYPE_UID, PARTITION_THING_TYPE_UID, ZONE_THING_TYPE_UID)
                    .collect(Collectors.toSet()));

    // List of all Channel UIDs
    public static final String IP150_COMMUNICATION_COMMAND_CHANNEL_UID = "communicationCommand";
    public static final String IP150_COMMUNICATION_STATE_CHANNEL_UID = "communicationState";

    public static final String PANEL_STATE_CHANNEL_UID = "state";
    public static final String PANEL_SERIAL_NUMBER_PROPERTY_NAME = "serialNumber";
    public static final String PANEL_TYPE_PROPERTY_NAME = "panelType";
    public static final String PANEL_HARDWARE_VERSION_PROPERTY_NAME = "hardwareVersion";
    public static final String PANEL_APPLICATION_VERSION_PROPERTY_NAME = "applicationVersion";
    public static final String PANEL_BOOTLOADER_VERSION_PROPERTY_NAME = "bootloaderVersion";

    public static final String PANEL_TIME = "panelTime";
    public static final String PANEL_INPUT_VOLTAGE = "inputVoltage";
    public static final String PANEL_BOARD_VOLTAGE = "boardVoltage";
    public static final String PANEL_BATTERY_VOLTAGE = "batteryVoltage";

    public static final String PARTITION_LABEL_CHANNEL_UID = "label";
    public static final String PARTITION_STATE_CHANNEL_UID = "state";
    public static final String PARTITION_DETAILED_STATE_CHANNEL_UID = "detailedState";
    @Deprecated // After implementation of channels for every possible state, the summarized additional states is no
                // longer needed. We'll keep it for backward compatibility
    public static final String PARTITION_ADDITIONAL_STATES_CHANNEL_UID = "additionalStates";
    public static final String PARTITION_READY_TO_ARM_CHANNEL_UID = "readyToArm";
    public static final String PARTITION_IN_EXIT_DELAY_CHANNEL_UID = "inExitDelay";
    public static final String PARTITION_IN_ENTRY_DELAY_CHANNEL_UID = "inEntryDelay";
    public static final String PARTITION_IN_TROUBLE_CHANNEL_UID = "inTrouble";
    public static final String PARTITION_ALARM_IN_MEMORY_CHANNEL_UID = "alarmInMemory";
    public static final String PARTITION_ZONE_BYPASS_CHANNEL_UID = "zoneBypass";
    public static final String PARTITION_ZONE_IN_TAMPER_CHANNEL_UID = "zoneInTamperTrouble";
    public static final String PARTITION_ZONE_IN_LOW_BATTERY_CHANNEL_UID = "zoneInLowBatteryTrouble";
    public static final String PARTITION_ZONE_IN_FIRE_LOOP_CHANNEL_UID = "zoneInFireLoopTrouble";
    public static final String PARTITION_ZONE_IN_SUPERVISION_TROUBLE_CHANNEL_UID = "zoneInSupervisionTrouble";
    public static final String PARTITION_STAY_INSTANT_READY_CHANNEL_UID = "stayInstantReady";
    public static final String PARTITION_FORCE_READY_CHANNEL_UID = "forceReady";
    public static final String PARTITION_BYPASS_READY_CHANNEL_UID = "bypassReady";
    public static final String PARTITION_INHIBIT_READY_CHANNEL_UID = "inhibitReady";
    public static final String PARTITION_ALL_ZONES_CLOSED_CHANNEL_UID = "allZonesClosed";

    public static final String ZONE_LABEL_CHANNEL_UID = "label";
    public static final String ZONE_OPENED_CHANNEL_UID = "opened";
    public static final String ZONE_TAMPERED_CHANNEL_UID = "tampered";
    public static final String ZONE_LOW_BATTERY_CHANNEL_UID = "lowBattery";

    public static final String ZONE_SUPERVISION_TROUBLE_UID = "supervisionTrouble";
    public static final String ZONE_IN_TX_DELAY_UID = "inTxDelay";
    public static final String ZONE_SHUTDOWN_UID = "shutdown";
    public static final String ZONE_BYPASSED_UID = "bypassed";
    public static final String ZONE_HAS_ACTIVATED_INTELLIZONE_DELAY_UID = "hasActivatedIntellizoneDelay";
    public static final String ZONE_HAS_ACTIVATED_ENTRY_DELAY_UID = "hasActivatedEntryDelay";
    public static final String ZONE_PRESENTLY_IN_ALARM_UID = "presentlyInAlarm";
    public static final String ZONE_GENERATED_ALARM_UID = "generatedAlarm";

    // Misc constants
    public static final StringType STATE_OFFLINE = new StringType("Offline");
    public static final StringType STATE_ONLINE = new StringType("Online");
}
