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
package org.openhab.binding.caddx.internal.message;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.caddx.internal.CaddxMessage;
import org.openhab.binding.caddx.internal.CaddxMessageReaderUtil;

/**
 * Test class for CaddxMessage.
 *
 * @author Georgios Moutsos - Initial contribution
 */
@NonNullByDefault
public class CaddxMessageParseTest {

    // @formatter:off
    public static final List<Object[]> data() {
        return Arrays.asList(new Object[][] {
            { "interface_configuration_message", "panel_firmware_version", "5.37", },
            { "interface_configuration_message", "panel_interface_configuration_message", "true", },

            { "zone_status_message", "zone_number", "4", },
            { "zone_status_message", "zone_partition1", "true", },
            { "zone_status_message", "zone_partition2", "false", },
            { "zone_status_message", "zone_partition3", "false", },
            { "zone_status_message", "zone_partition4", "false", },
            { "zone_status_message", "zone_partition5", "false", },
            { "zone_status_message", "zone_partition6", "false", },
            { "zone_status_message", "zone_partition7", "false", },
            { "zone_status_message", "zone_partition8", "false", },
            { "zone_status_message", "zone_fire", "true", },
            { "zone_status_message", "zone_24hour", "false", },
            { "zone_status_message", "zone_key_switch", "false", },
            { "zone_status_message", "zone_follower", "false", },
            { "zone_status_message", "zone_entry_exit_delay_1", "false", },
            { "zone_status_message", "zone_entry_exit_delay_2", "false", },
            { "zone_status_message", "zone_interior", "false", },
            { "zone_status_message", "zone_local_only", "false", },
            { "zone_status_message", "zone_keypad_sounder", "true", },
            { "zone_status_message", "zone_yelping_siren", "false", },
            { "zone_status_message", "zone_steady_siren", "true", },
            { "zone_status_message", "zone_chime", "false", },
            { "zone_status_message", "zone_bypassable", "false", },
            { "zone_status_message", "zone_group_bypassable", "false", },
            { "zone_status_message", "zone_force_armable", "false", },
            { "zone_status_message", "zone_entry_guard", "false", },
            { "zone_status_message", "zone_fast_loop_response", "false", },
            { "zone_status_message", "zone_double_eol_tamper", "false", },
            { "zone_status_message", "zone_type_trouble", "true", },
            { "zone_status_message", "zone_cross_zone", "false", },
            { "zone_status_message", "zone_dialer_delay", "false", },
            { "zone_status_message", "zone_swinger_shutdown", "false", },
            { "zone_status_message", "zone_restorable", "true", },
            { "zone_status_message", "zone_listen_in", "true", },
            { "zone_status_message", "zone_faulted", "false", },
            { "zone_status_message", "zone_tampered", "false", },
            { "zone_status_message", "zone_trouble", "false", },
            { "zone_status_message", "zone_bypassed", "false", },
            { "zone_status_message", "zone_inhibited", "false", },
            { "zone_status_message", "zone_low_battery", "false", },
            { "zone_status_message", "zone_loss_of_supervision", "false", },
            { "zone_status_message", "zone_alarm_memory", "false", },
            { "zone_status_message", "zone_bypass_memory", "false", },

            { "zones_snapshot_message", "zone_offset", "1", },
            { "zones_snapshot_message", "zone_1_faulted", "false", },
            { "zones_snapshot_message", "zone_1_bypassed", "false", },
            { "zones_snapshot_message", "zone_1_trouble", "false", },
            { "zones_snapshot_message", "zone_1_alarm_memory", "false", },
            { "zones_snapshot_message", "zone_2_faulted", "false", },
            { "zones_snapshot_message", "zone_2_bypassed", "false", },
            { "zones_snapshot_message", "zone_2_trouble", "false", },
            { "zones_snapshot_message", "zone_2_alarm_memory", "false", },
            { "zones_snapshot_message", "zone_3_faulted", "false", },
            { "zones_snapshot_message", "zone_3_bypassed", "false", },
            { "zones_snapshot_message", "zone_3_trouble", "false", },
            { "zones_snapshot_message", "zone_3_alarm_memory", "false", },
            { "zones_snapshot_message", "zone_4_faulted", "true", },
            { "zones_snapshot_message", "zone_4_bypassed", "false", },
            { "zones_snapshot_message", "zone_4_trouble", "false", },
            { "zones_snapshot_message", "zone_4_alarm_memory", "false", },
            { "zones_snapshot_message", "zone_5_faulted", "false", },
            { "zones_snapshot_message", "zone_6_faulted", "false", },
            { "zones_snapshot_message", "zone_7_faulted", "false", },
            { "zones_snapshot_message", "zone_8_faulted", "false", },
            { "zones_snapshot_message", "zone_9_faulted", "false", },
            { "zones_snapshot_message", "zone_10_faulted", "true", },
            { "zones_snapshot_message", "zone_11_faulted", "false", },
            { "zones_snapshot_message", "zone_12_faulted", "false", },
            { "zones_snapshot_message", "zone_13_faulted", "false", },
            { "zones_snapshot_message", "zone_14_faulted", "true", },

        });
    }
    // @formatter:on

    @ParameterizedTest
    @MethodSource("data")
    public void testParsing(String messageName, String property, String value) {
        CaddxMessage message = CaddxMessageReaderUtil.readCaddxMessage(messageName);

        assertNotNull(message, "Should not be null");
        assertEquals(value, message.getPropertyById(property), property + " should be: " + value);
    }
}
