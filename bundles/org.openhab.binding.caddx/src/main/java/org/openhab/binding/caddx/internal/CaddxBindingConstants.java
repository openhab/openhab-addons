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
package org.openhab.binding.caddx.internal;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link CaddxBindingConstants} class is responsible for creating things and thing
 * handlers.
 *
 * @author Georgios Moutsos - Initial contribution
 */
@NonNullByDefault
public class CaddxBindingConstants {
    // Binding ID
    private static final String BINDING_ID = "caddx";

    // List of bridge device types
    public static final String CADDX_BRIDGE = "bridge";

    // List of device types
    public static final String PANEL = "panel";
    public static final String PARTITION = "partition";
    public static final String ZONE = "zone";
    public static final String KEYPAD = "keypad";

    // List of all Bridge Thing Type UIDs
    public static final ThingTypeUID CADDXBRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, CADDX_BRIDGE);

    // List of all Thing Type UIDs
    public static final ThingTypeUID PANEL_THING_TYPE = new ThingTypeUID(BINDING_ID, PANEL);
    public static final ThingTypeUID PARTITION_THING_TYPE = new ThingTypeUID(BINDING_ID, PARTITION);
    public static final ThingTypeUID ZONE_THING_TYPE = new ThingTypeUID(BINDING_ID, ZONE);
    public static final ThingTypeUID KEYPAD_THING_TYPE = new ThingTypeUID(BINDING_ID, KEYPAD);

    // Bridge
    // Commands
    // Channels
    public static final String SEND_COMMAND = "send_command";

    // Panel
    // Commands
    public static final String PANEL_INTERFACE_CONFIGURATION_REQUEST = "panel_interface_configuration_request";
    public static final String PANEL_SYSTEM_STATUS_REQUEST = "panel_system_status_request";
    public static final String PANEL_LOG_EVENT_REQUEST = "panel_log_event_request";
    // Channels
    public static final String PANEL_FIRMWARE_VERSION = "panel_firmware_version";
    public static final String PANEL_LOG_MESSAGE_N_0 = "panel_log_message_n_0";

    // Partition
    // Commands
    public static final String PARTITION_STATUS_REQUEST = "partition_status_request";
    public static final String PARTITION_PRIMARY_COMMAND_WITH_PIN = "partition_primary_command_with_pin";
    public static final String PARTITION_SECONDARY_COMMAND = "partition_secondary_command";
    // Channels
    public static final String PARTITION_ARMED = "partition_armed";
    public static final String PARTITION_PRIMARY = "partition_primary";
    public static final String PARTITION_SECONDARY = "partition_secondary";

    // Zone
    // Commands
    public static final String ZONE_STATUS_REQUEST = "zone_status_request";
    public static final String ZONE_NAME_REQUEST = "zone_name_request";
    // Channels
    public static final String ZONE_NAME = "zone_name";
    public static final String ZONE_FAULTED = "zone_faulted";
    public static final String ZONE_BYPASSED = "zone_bypassed";

    // Keypad
    // Commands
    public static final String KEYPAD_TERMINAL_MODE_REQUEST = "keypad_terminal_mode_request";
    public static final String KEYPAD_SEND_KEYPAD_TEXT_MESSAGE = "keypad_send_keypad_text_message";
    // Channels
    public static final String KEYPAD_KEY_PRESSED = "keypad_key_pressed";

    // Set of all supported Thing Type UIDs
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.unmodifiableSet(Stream
            .of(CADDXBRIDGE_THING_TYPE, PANEL_THING_TYPE, PARTITION_THING_TYPE, ZONE_THING_TYPE, KEYPAD_THING_TYPE)
            .collect(Collectors.toSet()));

    // Set of all supported Bridge Type UIDs
    public static final Set<ThingTypeUID> SUPPORTED_BRIDGE_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(CADDXBRIDGE_THING_TYPE).collect(Collectors.toSet()));
}
