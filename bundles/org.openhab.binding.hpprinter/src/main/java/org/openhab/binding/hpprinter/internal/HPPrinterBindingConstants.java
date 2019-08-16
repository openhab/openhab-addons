/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.hpprinter.internal;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID;

/**
 * The {@link HPPrinterBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Stewart Cossey - Initial contribution
 */
@NonNullByDefault
public class HPPrinterBindingConstants {

    private static final String BINDING_ID = "hpprinter";

    // ********** List of all Thing Type UIDs **********
    public static final ThingTypeUID THING_PRINTER = new ThingTypeUID(BINDING_ID, "printer");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
    .of(THING_PRINTER)
    .collect(Collectors.toSet());


    // ********** List of all Channel ids **********
    public static final String CHANNEL_STATUS = "status";

    // Colours
    public static final String CHANNEL_COLOR_LEVEL = "colorLevel"; //Dynamic
    public static final String CHANNEL_CYAN_LEVEL = "cyanLevel"; //Dynamic
    public static final String CHANNEL_MAGENTA_LEVEL = "magentaLevel"; //Dynamic
    public static final String CHANNEL_YELLOW_LEVEL = "yellowLevel"; //Dynamic
    public static final String CHANNEL_BLACK_LEVEL = "blackLevel";

    public static final String CHANNEL_COLOR_MARKING = "colorMarker"; //Dynamic
    public static final String CHANNEL_CYAN_MARKING = "cyanMarker"; //Dynamic
    public static final String CHANNEL_MAGENTA_MARKING = "magentaMarker"; //Dynamic
    public static final String CHANNEL_YELLOW_MARKING = "yellowMarker"; //Dynamic
    public static final String CHANNEL_BLACK_MARKING = "blackMarker"; //Dynamic

    // Page Counts
    public static final String CHANNEL_TOTAL_PAGES = "totalCount";
    public static final String CHANNEL_SUBSCRIPTION = "subsciptionCount"; //Dynamic
    public static final String CHANNEL_TOTAL_COLORPAGES = "totalColorCount"; //Dynamic
    public static final String CHANNEL_TOTAL_MONOPAGES = "totalMonochromeCount"; //Dynamic
    public static final String CHANNEL_JAM_EVENTS = "jamEvents"; //Dynamic
    public static final String CHANNEL_MISPICK_EVENTS = "mispickEvents"; //Dynamic
    public static final String CHANNEL_FRONT_PANEL_CANCEL = "fpCancelCount"; //Dynamic

    public static final String CGROUP_INK = "ink";
    public static final String CGROUP_STATUS = "status";
    public static final String CGROUP_USAGE = "usage";

    public static final ChannelGroupTypeUID GROUP_INK = new ChannelGroupTypeUID(BINDING_ID, CGROUP_INK);
}
