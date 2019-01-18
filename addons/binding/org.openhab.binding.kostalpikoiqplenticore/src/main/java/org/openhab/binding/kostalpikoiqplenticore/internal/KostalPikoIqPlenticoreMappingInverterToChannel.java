/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.kostalpikoiqplenticore.internal;

import static org.openhab.binding.kostalpikoiqplenticore.internal.KostalPikoIqPlenticoreBindingConstants.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The {@link KostalPikoIqPlenticoreMappingInverterToChannel} is responsible for the management of the channels
 * available to inverters
 *
 * @author René Stakemeier - Initial contribution
 */
class KostalPikoIqPlenticoreMappingInverterToChannel {

    private static Map<KostalPikoIqPlenticoreInverterTypes, List<KostalPikoIqPlenticoreChannelMappingToWebApi>> channelMapping = new HashMap<KostalPikoIqPlenticoreInverterTypes, List<KostalPikoIqPlenticoreChannelMappingToWebApi>>();

    /*
     * Assign the channels to the devices.
     */
    static {
        /*
         * Channels available on all devices
         */
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq42, KostalPikoIqPlenticoreInverterTypes.PikoIq55,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq70, KostalPikoIqPlenticoreInverterTypes.PikoIq85,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq100).collect(Collectors.toList()),
                CHANNEL_DEVICE_LOCAL_DC_POWER, "devices:local", "Dc_P", KostalPikoIqPlenticoreChannelDatatypes.Watt);

        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq42, KostalPikoIqPlenticoreInverterTypes.PikoIq55,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq70, KostalPikoIqPlenticoreInverterTypes.PikoIq85,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq100).collect(Collectors.toList()),
                CHANNEL_DEVICE_LOCAL_HOMECONSUMPTION_FROM_GRID, "devices:local", "HomeGrid_P",
                KostalPikoIqPlenticoreChannelDatatypes.Watt);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq42, KostalPikoIqPlenticoreInverterTypes.PikoIq55,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq70, KostalPikoIqPlenticoreInverterTypes.PikoIq85,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq100).collect(Collectors.toList()),
                CHANNEL_DEVICE_LOCAL_OWNCONSUMPTION, "devices:local", "HomeOwn_P",
                KostalPikoIqPlenticoreChannelDatatypes.Watt);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq42, KostalPikoIqPlenticoreInverterTypes.PikoIq55,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq70, KostalPikoIqPlenticoreInverterTypes.PikoIq85,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq100).collect(Collectors.toList()),
                CHANNEL_DEVICE_LOCAL_HOMECONSUMPTION_FROM_PV, "devices:local", "HomePv_P",
                KostalPikoIqPlenticoreChannelDatatypes.Watt);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq42, KostalPikoIqPlenticoreInverterTypes.PikoIq55,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq70, KostalPikoIqPlenticoreInverterTypes.PikoIq85,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq100).collect(Collectors.toList()),
                CHANNEL_DEVICE_LOCAL_HOMECONSUMPTION_TOTAL, "devices:local", "Home_P",
                KostalPikoIqPlenticoreChannelDatatypes.Watt);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq42, KostalPikoIqPlenticoreInverterTypes.PikoIq55,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq70, KostalPikoIqPlenticoreInverterTypes.PikoIq85,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq100).collect(Collectors.toList()),
                CHANNEL_DEVICE_LOCAL_LIMIT_EVU_ABSOLUTE, "devices:local", "LimitEvuAbs",
                KostalPikoIqPlenticoreChannelDatatypes.Watt);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq42, KostalPikoIqPlenticoreInverterTypes.PikoIq55,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq70, KostalPikoIqPlenticoreInverterTypes.PikoIq85,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq100).collect(Collectors.toList()),
                CHANNEL_DEVICE_LOCAL_LIMIT_EVU_RELATIV, "devices:local", "LimitEvuRel",
                KostalPikoIqPlenticoreChannelDatatypes.Percent);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq42, KostalPikoIqPlenticoreInverterTypes.PikoIq55,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq70, KostalPikoIqPlenticoreInverterTypes.PikoIq85,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq100).collect(Collectors.toList()),
                CHANNEL_DEVICE_LOCAL_WORKTIME, "devices:local", "WorkTime",
                KostalPikoIqPlenticoreChannelDatatypes.Seconds);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq42, KostalPikoIqPlenticoreInverterTypes.PikoIq55,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq70, KostalPikoIqPlenticoreInverterTypes.PikoIq85,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq100).collect(Collectors.toList()),
                CHANNEL_DEVICE_LOCAL_AC_PHASE_1_CURRENT_AMPERAGE, "devices:local:ac", "L1_I",
                KostalPikoIqPlenticoreChannelDatatypes.Ampere);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq42, KostalPikoIqPlenticoreInverterTypes.PikoIq55,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq70, KostalPikoIqPlenticoreInverterTypes.PikoIq85,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq100).collect(Collectors.toList()),
                CHANNEL_DEVICE_LOCAL_AC_PHASE_1_CURRENT_POWER, "devices:local:ac", "L1_P",
                KostalPikoIqPlenticoreChannelDatatypes.Watt);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq42, KostalPikoIqPlenticoreInverterTypes.PikoIq55,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq70, KostalPikoIqPlenticoreInverterTypes.PikoIq85,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq100).collect(Collectors.toList()),
                CHANNEL_DEVICE_LOCAL_AC_PHASE_1_CURRENT_VOLTAGE, "devices:local:ac", "L1_U",
                KostalPikoIqPlenticoreChannelDatatypes.Volt);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq42, KostalPikoIqPlenticoreInverterTypes.PikoIq55,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq70, KostalPikoIqPlenticoreInverterTypes.PikoIq85,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq100).collect(Collectors.toList()),
                CHANNEL_DEVICE_LOCAL_AC_PHASE_2_CURRENT_AMPERAGE, "devices:local:ac", "L2_I",
                KostalPikoIqPlenticoreChannelDatatypes.Ampere);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq42, KostalPikoIqPlenticoreInverterTypes.PikoIq55,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq70, KostalPikoIqPlenticoreInverterTypes.PikoIq85,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq100).collect(Collectors.toList()),
                CHANNEL_DEVICE_LOCAL_AC_PHASE_2_CURRENT_POWER, "devices:local:ac", "L2_P",
                KostalPikoIqPlenticoreChannelDatatypes.Watt);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq42, KostalPikoIqPlenticoreInverterTypes.PikoIq55,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq70, KostalPikoIqPlenticoreInverterTypes.PikoIq85,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq100).collect(Collectors.toList()),
                CHANNEL_DEVICE_LOCAL_AC_PHASE_2_CURRENT_VOLTAGE, "devices:local:ac", "L2_U",
                KostalPikoIqPlenticoreChannelDatatypes.Volt);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq42, KostalPikoIqPlenticoreInverterTypes.PikoIq55,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq70, KostalPikoIqPlenticoreInverterTypes.PikoIq85,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq100).collect(Collectors.toList()),
                CHANNEL_DEVICE_LOCAL_AC_PHASE_3_CURRENT_AMPERAGE, "devices:local:ac", "L3_I",
                KostalPikoIqPlenticoreChannelDatatypes.Ampere);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq42, KostalPikoIqPlenticoreInverterTypes.PikoIq55,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq70, KostalPikoIqPlenticoreInverterTypes.PikoIq85,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq100).collect(Collectors.toList()),
                CHANNEL_DEVICE_LOCAL_AC_PHASE_3_CURRENT_POWER, "devices:local:ac", "L3_P",
                KostalPikoIqPlenticoreChannelDatatypes.Watt);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq42, KostalPikoIqPlenticoreInverterTypes.PikoIq55,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq70, KostalPikoIqPlenticoreInverterTypes.PikoIq85,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq100).collect(Collectors.toList()),
                CHANNEL_DEVICE_LOCAL_AC_PHASE_3_CURRENT_VOLTAGE, "devices:local:ac", "L3_U",
                KostalPikoIqPlenticoreChannelDatatypes.Volt);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq42, KostalPikoIqPlenticoreInverterTypes.PikoIq55,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq70, KostalPikoIqPlenticoreInverterTypes.PikoIq85,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq100).collect(Collectors.toList()),
                CHANNEL_DEVICE_LOCAL_AC_CURRENT_POWER, "devices:local:ac", "P",
                KostalPikoIqPlenticoreChannelDatatypes.Watt);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq42, KostalPikoIqPlenticoreInverterTypes.PikoIq55,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq70, KostalPikoIqPlenticoreInverterTypes.PikoIq85,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq100).collect(Collectors.toList()),
                CHANNEL_DEVICE_LOCAL_PVSTRING_1_AMPERAGE, "devices:local:pv1", "I",
                KostalPikoIqPlenticoreChannelDatatypes.Ampere);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq42, KostalPikoIqPlenticoreInverterTypes.PikoIq55,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq70, KostalPikoIqPlenticoreInverterTypes.PikoIq85,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq100).collect(Collectors.toList()),
                CHANNEL_DEVICE_LOCAL_PVSTRING_1_POWER, "devices:local:pv1", "P",
                KostalPikoIqPlenticoreChannelDatatypes.Watt);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq42, KostalPikoIqPlenticoreInverterTypes.PikoIq55,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq70, KostalPikoIqPlenticoreInverterTypes.PikoIq85,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq100).collect(Collectors.toList()),
                CHANNEL_DEVICE_LOCAL_PVSTRING_1_VOLTAGE, "devices:local:pv1", "U",
                KostalPikoIqPlenticoreChannelDatatypes.Volt);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq42, KostalPikoIqPlenticoreInverterTypes.PikoIq55,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq70, KostalPikoIqPlenticoreInverterTypes.PikoIq85,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq100).collect(Collectors.toList()),
                CHANNEL_DEVICE_LOCAL_PVSTRING_2_AMPERAGE, "devices:local:pv2", "I",
                KostalPikoIqPlenticoreChannelDatatypes.Ampere);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq42, KostalPikoIqPlenticoreInverterTypes.PikoIq55,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq70, KostalPikoIqPlenticoreInverterTypes.PikoIq85,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq100).collect(Collectors.toList()),
                CHANNEL_DEVICE_LOCAL_PVSTRING_2_POWER, "devices:local:pv2", "P",
                KostalPikoIqPlenticoreChannelDatatypes.Watt);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq42, KostalPikoIqPlenticoreInverterTypes.PikoIq55,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq70, KostalPikoIqPlenticoreInverterTypes.PikoIq85,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq100).collect(Collectors.toList()),
                CHANNEL_DEVICE_LOCAL_PVSTRING_2_VOLTAGE, "devices:local:pv2", "U",
                KostalPikoIqPlenticoreChannelDatatypes.Volt);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq42, KostalPikoIqPlenticoreInverterTypes.PikoIq55,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq70, KostalPikoIqPlenticoreInverterTypes.PikoIq85,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq100).collect(Collectors.toList()),
                CHANNEL_SCB_EVENT_ERROR_COUNT_MC, "scb:event", "ErrMc", KostalPikoIqPlenticoreChannelDatatypes.Integer);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq42, KostalPikoIqPlenticoreInverterTypes.PikoIq55,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq70, KostalPikoIqPlenticoreInverterTypes.PikoIq85,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq100).collect(Collectors.toList()),
                CHANNEL_SCB_EVENT_ERROR_COUNT_SFH, "scb:event", "ErrSFH",
                KostalPikoIqPlenticoreChannelDatatypes.Integer);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq42, KostalPikoIqPlenticoreInverterTypes.PikoIq55,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq70, KostalPikoIqPlenticoreInverterTypes.PikoIq85,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq100).collect(Collectors.toList()),
                CHANNEL_SCB_EVENT_ERROR_COUNT_SCB, "scb:event", "Event:ActiveErrorCnt",
                KostalPikoIqPlenticoreChannelDatatypes.Integer);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq42, KostalPikoIqPlenticoreInverterTypes.PikoIq55,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq70, KostalPikoIqPlenticoreInverterTypes.PikoIq85,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq100).collect(Collectors.toList()),
                CHANNEL_SCB_EVENT_WARNING_COUNT_SCB, "scb:event", "Event:ActiveWarningCnt",
                KostalPikoIqPlenticoreChannelDatatypes.Integer);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq42, KostalPikoIqPlenticoreInverterTypes.PikoIq55,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq70, KostalPikoIqPlenticoreInverterTypes.PikoIq85,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq100).collect(Collectors.toList()),
                CHANNEL_STATISTIC_AUTARKY_DAY, "scb:statistic:EnergyFlow", "Statistic:Autarky:Day",
                KostalPikoIqPlenticoreChannelDatatypes.Percent);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq42, KostalPikoIqPlenticoreInverterTypes.PikoIq55,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq70, KostalPikoIqPlenticoreInverterTypes.PikoIq85,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq100).collect(Collectors.toList()),
                CHANNEL_STATISTIC_AUTARKY_MONTH, "scb:statistic:EnergyFlow", "Statistic:Autarky:Month",
                KostalPikoIqPlenticoreChannelDatatypes.Percent);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq42, KostalPikoIqPlenticoreInverterTypes.PikoIq55,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq70, KostalPikoIqPlenticoreInverterTypes.PikoIq85,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq100).collect(Collectors.toList()),
                CHANNEL_STATISTIC_AUTARKY_TOTAL, "scb:statistic:EnergyFlow", "Statistic:Autarky:Total",
                KostalPikoIqPlenticoreChannelDatatypes.Percent);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq42, KostalPikoIqPlenticoreInverterTypes.PikoIq55,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq70, KostalPikoIqPlenticoreInverterTypes.PikoIq85,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq100).collect(Collectors.toList()),
                CHANNEL_STATISTIC_AUTARKY_YEAR, "scb:statistic:EnergyFlow", "Statistic:Autarky:Year",
                KostalPikoIqPlenticoreChannelDatatypes.Percent);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq42, KostalPikoIqPlenticoreInverterTypes.PikoIq55,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq70, KostalPikoIqPlenticoreInverterTypes.PikoIq85,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq100).collect(Collectors.toList()),
                CHANNEL_STATISTIC_CO2SAVING_DAY, "scb:statistic:EnergyFlow", "Statistic:CO2Saving:Day",
                KostalPikoIqPlenticoreChannelDatatypes.KiloGram);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq42, KostalPikoIqPlenticoreInverterTypes.PikoIq55,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq70, KostalPikoIqPlenticoreInverterTypes.PikoIq85,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq100).collect(Collectors.toList()),
                CHANNEL_STATISTIC_CO2SAVING_MONTH, "scb:statistic:EnergyFlow", "Statistic:CO2Saving:Month",
                KostalPikoIqPlenticoreChannelDatatypes.KiloGram);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq42, KostalPikoIqPlenticoreInverterTypes.PikoIq55,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq70, KostalPikoIqPlenticoreInverterTypes.PikoIq85,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq100).collect(Collectors.toList()),
                CHANNEL_STATISTIC_CO2SAVING_TOTAL, "scb:statistic:EnergyFlow", "Statistic:CO2Saving:Total",
                KostalPikoIqPlenticoreChannelDatatypes.KiloGram);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq42, KostalPikoIqPlenticoreInverterTypes.PikoIq55,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq70, KostalPikoIqPlenticoreInverterTypes.PikoIq85,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq100).collect(Collectors.toList()),
                CHANNEL_STATISTIC_CO2SAVING_YEAR, "scb:statistic:EnergyFlow", "Statistic:CO2Saving:Year",
                KostalPikoIqPlenticoreChannelDatatypes.KiloGram);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq42, KostalPikoIqPlenticoreInverterTypes.PikoIq55,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq70, KostalPikoIqPlenticoreInverterTypes.PikoIq85,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq100).collect(Collectors.toList()),
                CHANNEL_STATISTIC_HOMECONSUMPTION_DAY, "scb:statistic:EnergyFlow", "Statistic:EnergyHome:Day",
                KostalPikoIqPlenticoreChannelDatatypes.KiloWattHour);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq42, KostalPikoIqPlenticoreInverterTypes.PikoIq55,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq70, KostalPikoIqPlenticoreInverterTypes.PikoIq85,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq100).collect(Collectors.toList()),
                CHANNEL_STATISTIC_HOMECONSUMPTION_MONTH, "scb:statistic:EnergyFlow", "Statistic:EnergyHome:Month",
                KostalPikoIqPlenticoreChannelDatatypes.KiloWattHour);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq42, KostalPikoIqPlenticoreInverterTypes.PikoIq55,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq70, KostalPikoIqPlenticoreInverterTypes.PikoIq85,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq100).collect(Collectors.toList()),
                CHANNEL_STATISTIC_HOMECONSUMPTION_TOTAL, "scb:statistic:EnergyFlow", "Statistic:EnergyHome:Total",
                KostalPikoIqPlenticoreChannelDatatypes.KiloWattHour);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq42, KostalPikoIqPlenticoreInverterTypes.PikoIq55,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq70, KostalPikoIqPlenticoreInverterTypes.PikoIq85,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq100).collect(Collectors.toList()),
                CHANNEL_STATISTIC_HOMECONSUMPTION_YEAR, "scb:statistic:EnergyFlow", "Statistic:EnergyHome:Year",
                KostalPikoIqPlenticoreChannelDatatypes.KiloWattHour);

        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq42, KostalPikoIqPlenticoreInverterTypes.PikoIq55,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq70, KostalPikoIqPlenticoreInverterTypes.PikoIq85,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq100).collect(Collectors.toList()),
                CHANNEL_STATISTIC_HOMECONSUMPTION_FROM_GRID_DAY, "scb:statistic:EnergyFlow",
                "Statistic:EnergyHomeGrid:Day", KostalPikoIqPlenticoreChannelDatatypes.KiloWattHour);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq42, KostalPikoIqPlenticoreInverterTypes.PikoIq55,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq70, KostalPikoIqPlenticoreInverterTypes.PikoIq85,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq100).collect(Collectors.toList()),
                CHANNEL_STATISTIC_HOMECONSUMPTION_FROM_GRID_MONTH, "scb:statistic:EnergyFlow",
                "Statistic:EnergyHomeGrid:Month", KostalPikoIqPlenticoreChannelDatatypes.KiloWattHour);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq42, KostalPikoIqPlenticoreInverterTypes.PikoIq55,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq70, KostalPikoIqPlenticoreInverterTypes.PikoIq85,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq100).collect(Collectors.toList()),
                CHANNEL_STATISTIC_HOMECONSUMPTION_FROM_GRID_TOTAL, "scb:statistic:EnergyFlow",
                "Statistic:EnergyHomeGrid:Total", KostalPikoIqPlenticoreChannelDatatypes.KiloWattHour);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq42, KostalPikoIqPlenticoreInverterTypes.PikoIq55,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq70, KostalPikoIqPlenticoreInverterTypes.PikoIq85,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq100).collect(Collectors.toList()),
                CHANNEL_STATISTIC_HOMECONSUMPTION_FROM_GRID_YEAR, "scb:statistic:EnergyFlow",
                "Statistic:EnergyHomeGrid:Year", KostalPikoIqPlenticoreChannelDatatypes.KiloWattHour);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq42, KostalPikoIqPlenticoreInverterTypes.PikoIq55,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq70, KostalPikoIqPlenticoreInverterTypes.PikoIq85,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq100).collect(Collectors.toList()),
                CHANNEL_STATISTIC_HOMECONSUMPTION_FROM_PV_DAY, "scb:statistic:EnergyFlow", "Statistic:EnergyHomePv:Day",
                KostalPikoIqPlenticoreChannelDatatypes.KiloWattHour);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq42, KostalPikoIqPlenticoreInverterTypes.PikoIq55,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq70, KostalPikoIqPlenticoreInverterTypes.PikoIq85,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq100).collect(Collectors.toList()),
                CHANNEL_STATISTIC_HOMECONSUMPTION_FROM_PV_MONTH, "scb:statistic:EnergyFlow",
                "Statistic:EnergyHomePv:Month", KostalPikoIqPlenticoreChannelDatatypes.KiloWattHour);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq42, KostalPikoIqPlenticoreInverterTypes.PikoIq55,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq70, KostalPikoIqPlenticoreInverterTypes.PikoIq85,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq100).collect(Collectors.toList()),
                CHANNEL_STATISTIC_HOMECONSUMPTION_FROM_PV_TOTAL, "scb:statistic:EnergyFlow",
                "Statistic:EnergyHomePv:Total", KostalPikoIqPlenticoreChannelDatatypes.KiloWattHour);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq42, KostalPikoIqPlenticoreInverterTypes.PikoIq55,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq70, KostalPikoIqPlenticoreInverterTypes.PikoIq85,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq100).collect(Collectors.toList()),
                CHANNEL_STATISTIC_HOMECONSUMPTION_FROM_PV_YEAR, "scb:statistic:EnergyFlow",
                "Statistic:EnergyHomePv:Year", KostalPikoIqPlenticoreChannelDatatypes.KiloWattHour);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq42, KostalPikoIqPlenticoreInverterTypes.PikoIq55,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq70, KostalPikoIqPlenticoreInverterTypes.PikoIq85,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq100).collect(Collectors.toList()),
                CHANNEL_STATISTIC_OWNCONSUMPTION_RATE_DAY, "scb:statistic:EnergyFlow",
                "Statistic:OwnConsumptionRate:Day", KostalPikoIqPlenticoreChannelDatatypes.Percent);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq42, KostalPikoIqPlenticoreInverterTypes.PikoIq55,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq70, KostalPikoIqPlenticoreInverterTypes.PikoIq85,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq100).collect(Collectors.toList()),
                CHANNEL_STATISTIC_OWNCONSUMPTION_RATE_MONTH, "scb:statistic:EnergyFlow",
                "Statistic:OwnConsumptionRate:Month", KostalPikoIqPlenticoreChannelDatatypes.Percent);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq42, KostalPikoIqPlenticoreInverterTypes.PikoIq55,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq70, KostalPikoIqPlenticoreInverterTypes.PikoIq85,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq100).collect(Collectors.toList()),
                CHANNEL_STATISTIC_OWNCONSUMPTION_RATE_TOTAL, "scb:statistic:EnergyFlow",
                "Statistic:OwnConsumptionRate:Total", KostalPikoIqPlenticoreChannelDatatypes.Percent);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq42, KostalPikoIqPlenticoreInverterTypes.PikoIq55,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq70, KostalPikoIqPlenticoreInverterTypes.PikoIq85,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq100).collect(Collectors.toList()),
                CHANNEL_STATISTIC_OWNCONSUMPTION_RATE_YEAR, "scb:statistic:EnergyFlow",
                "Statistic:OwnConsumptionRate:Year", KostalPikoIqPlenticoreChannelDatatypes.Percent);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq42, KostalPikoIqPlenticoreInverterTypes.PikoIq55,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq70, KostalPikoIqPlenticoreInverterTypes.PikoIq85,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq100).collect(Collectors.toList()),
                CHANNEL_STATISTIC_YIELD_DAY, "scb:statistic:EnergyFlow", "Statistic:Yield:Day",
                KostalPikoIqPlenticoreChannelDatatypes.KiloWattHour);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq42, KostalPikoIqPlenticoreInverterTypes.PikoIq55,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq70, KostalPikoIqPlenticoreInverterTypes.PikoIq85,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq100).collect(Collectors.toList()),
                CHANNEL_STATISTIC_YIELD_MONTH, "scb:statistic:EnergyFlow", "Statistic:Yield:Month",
                KostalPikoIqPlenticoreChannelDatatypes.KiloWattHour);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq42, KostalPikoIqPlenticoreInverterTypes.PikoIq55,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq70, KostalPikoIqPlenticoreInverterTypes.PikoIq85,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq100).collect(Collectors.toList()),
                CHANNEL_STATISTIC_YIELD_TOTAL, "scb:statistic:EnergyFlow", "Statistic:Yield:Total",
                KostalPikoIqPlenticoreChannelDatatypes.KiloWattHour);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq42, KostalPikoIqPlenticoreInverterTypes.PikoIq55,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq70, KostalPikoIqPlenticoreInverterTypes.PikoIq85,
                        KostalPikoIqPlenticoreInverterTypes.PikoIq100).collect(Collectors.toList()),
                CHANNEL_STATISTIC_YIELD_YEAR, "scb:statistic:EnergyFlow", "Statistic:Yield:Year",
                KostalPikoIqPlenticoreChannelDatatypes.KiloWattHour);
        /*
         * Plenticore Plus devices can be expanded with a battery.
         * Additional channels become available, but the pv3 information are hidden (since the battery is attached
         * there)
         */
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery).collect(Collectors.toList()),
                CHANNEL_DEVICE_LOCAL_BATTERY_LOADING_CYCLES, "devices:local:battery", "Cycles",
                KostalPikoIqPlenticoreChannelDatatypes.Integer);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery).collect(Collectors.toList()),
                CHANNEL_DEVICE_LOCAL_BATTERY_FULL_CHARGE_CAPACITY, "devices:local:battery", "FullChargeCap_E",
                KostalPikoIqPlenticoreChannelDatatypes.AmpereHour);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery).collect(Collectors.toList()),
                CHANNEL_DEVICE_LOCAL_BATTERY_AMPERAGE, "devices:local:battery", "I",
                KostalPikoIqPlenticoreChannelDatatypes.Ampere);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery).collect(Collectors.toList()),
                CHANNEL_DEVICE_LOCAL_BATTERY_POWER, "devices:local:battery", "P",
                KostalPikoIqPlenticoreChannelDatatypes.Watt);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery).collect(Collectors.toList()),
                CHANNEL_DEVICE_LOCAL_BATTERY_STATE_OF_CHARGE, "devices:local:battery", "SoC",
                KostalPikoIqPlenticoreChannelDatatypes.Percent);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery).collect(Collectors.toList()),
                CHANNEL_DEVICE_LOCAL_BATTERY_VOLTAGE, "devices:local:battery", "U",
                KostalPikoIqPlenticoreChannelDatatypes.Volt);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery).collect(Collectors.toList()),
                CHANNEL_STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_DAY, "scb:statistic:EnergyFlow",
                "Statistic:EnergyHomeBat:Day", KostalPikoIqPlenticoreChannelDatatypes.KiloWattHour);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery).collect(Collectors.toList()),
                CHANNEL_STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_MONTH, "scb:statistic:EnergyFlow",
                "Statistic:EnergyHomeBat:Month", KostalPikoIqPlenticoreChannelDatatypes.KiloWattHour);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery).collect(Collectors.toList()),
                CHANNEL_STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_TOTAL, "scb:statistic:EnergyFlow",
                "Statistic:EnergyHomeBat:Total", KostalPikoIqPlenticoreChannelDatatypes.KiloWattHour);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery).collect(Collectors.toList()),
                CHANNEL_STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_YEAR, "scb:statistic:EnergyFlow",
                "Statistic:EnergyHomeBat:Year", KostalPikoIqPlenticoreChannelDatatypes.KiloWattHour);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithBattery).collect(Collectors.toList()),
                CHANNEL_DEVICE_LOCAL_HOMECONSUMPTION_FROM_BATTERY, "devices:local", "HomeBat_P",
                KostalPikoIqPlenticoreChannelDatatypes.Watt);
        /*
         * Plenticore devices without battery have the pv3 settings available
         */
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithoutBattery)
                        .collect(Collectors.toList()),
                CHANNEL_DEVICE_LOCAL_PVSTRING_3_AMPERAGE, "devices:local:pv3", "I",
                KostalPikoIqPlenticoreChannelDatatypes.Ampere);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithoutBattery)
                        .collect(Collectors.toList()),
                CHANNEL_DEVICE_LOCAL_PVSTRING_3_POWER, "devices:local:pv3", "P",
                KostalPikoIqPlenticoreChannelDatatypes.Watt);
        addInverterChannel(
                Stream.of(KostalPikoIqPlenticoreInverterTypes.PlenticorePlus42WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus55WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus70WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus85WithoutBattery,
                        KostalPikoIqPlenticoreInverterTypes.PlenticorePlus100WithoutBattery)
                        .collect(Collectors.toList()),
                CHANNEL_DEVICE_LOCAL_PVSTRING_3_VOLTAGE, "devices:local:pv3", "U",
                KostalPikoIqPlenticoreChannelDatatypes.Volt);
    }

    static Map<String, List<KostalPikoIqPlenticoreChannelMappingToWebApi>> getModuleToChannelsMappingForInverter(
            KostalPikoIqPlenticoreInverterTypes inverter) {
        Map<String, List<KostalPikoIqPlenticoreChannelMappingToWebApi>> results = new HashMap<String, List<KostalPikoIqPlenticoreChannelMappingToWebApi>>();
        for (KostalPikoIqPlenticoreChannelMappingToWebApi mapping : channelMapping.get(inverter)) {
            List<KostalPikoIqPlenticoreChannelMappingToWebApi> channelList = null;
            if (results.containsKey(mapping.moduleId)) {
                channelList = results.get(mapping.moduleId);
            } else {
                channelList = new ArrayList<KostalPikoIqPlenticoreChannelMappingToWebApi>();
                results.put(mapping.moduleId, channelList);
            }
            channelList.add(mapping);
        }
        return results;
    }

    private static void addInverterChannel(KostalPikoIqPlenticoreInverterTypes inverter,
            KostalPikoIqPlenticoreChannelMappingToWebApi mapping) {
        if (!channelMapping.containsKey(inverter)) {
            channelMapping.put(inverter, new ArrayList<KostalPikoIqPlenticoreChannelMappingToWebApi>());
        }
        channelMapping.get(inverter).add(mapping);
    }

    private static void addInverterChannel(KostalPikoIqPlenticoreInverterTypes inverter, String channelUID,
            String moduleId, String processdataId, KostalPikoIqPlenticoreChannelDatatypes dataType) {
        addInverterChannel(inverter,
                new KostalPikoIqPlenticoreChannelMappingToWebApi(channelUID, moduleId, processdataId, dataType));
    }

    private static void addInverterChannel(List<KostalPikoIqPlenticoreInverterTypes> inverterList, String channelUID,
            String moduleId, String processdataId, KostalPikoIqPlenticoreChannelDatatypes dataType) {
        for (KostalPikoIqPlenticoreInverterTypes inverter : inverterList) {
            addInverterChannel(inverter, channelUID, moduleId, processdataId, dataType);
        }
    }
}
