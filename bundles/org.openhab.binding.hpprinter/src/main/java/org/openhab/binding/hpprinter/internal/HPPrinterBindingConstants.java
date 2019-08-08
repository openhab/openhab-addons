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
    public static final ThingTypeUID THING_MONOCHROME = new ThingTypeUID(BINDING_ID, "monochrome");
    public static final ThingTypeUID THING_SINGLECOLOR = new ThingTypeUID(BINDING_ID, "singlecolor");
    public static final ThingTypeUID THING_MULTICOLOR = new ThingTypeUID(BINDING_ID, "multicolor");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
    .of(THING_MONOCHROME, THING_SINGLECOLOR, THING_MULTICOLOR)
    .collect(Collectors.toSet());


    // ********** List of all Channel ids **********
    public static final String CHANNEL_STATUS = "status";

    // Colours
    public static final String CHANNEL_COLOUR_LEVEL = "colourLevel";
    public static final String CHANNEL_CYAN_LEVEL = "cyanLevel";
    public static final String CHANNEL_MAGENTA_LEVEL = "magentaLevel";
    public static final String CHANNEL_YELLOW_LEVEL = "yellowLevel";
    public static final String CHANNEL_BLACK_LEVEL = "blackLevel";

    // Page Counts
    public static final String CHANNEL_SUBSCRIPTION = "subsciptionCount";
    public static final String CHANNEL_TOTAL_PAGES = "totalCount";
    public static final String CHANNEL_TOTAL_COLOURPAGES = "totalColorCount";
    public static final String CHANNEL_TOTAL_MONOPAGES = "totalMonochromeCount";
    public static final String CHANNEL_JAM_EVENTS = "jamEvents";

    public static final String CGROUP_INK = "ink";
    public static final String CGROUP_STATUS = "status";
    public static final String CGROUP_CONTROL = "control";
    public static final String CGROUP_USAGE = "usage";
}
