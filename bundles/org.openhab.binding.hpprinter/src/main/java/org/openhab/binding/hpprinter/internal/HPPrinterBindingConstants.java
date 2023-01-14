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
package org.openhab.binding.hpprinter.internal;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;

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

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream.of(THING_PRINTER)
            .collect(Collectors.toSet());

    // ********** Channel Types **********
    public static ChannelTypeUID chanTypeStatus = new ChannelTypeUID("hpprinter:status");
    public static ChannelTypeUID chanTypeReadSwitch = new ChannelTypeUID("hpprinter:readonlyswitch");
    public static ChannelTypeUID chanTypeTotals = new ChannelTypeUID("hpprinter:totals");
    public static ChannelTypeUID chanTypeTotalsAdvanced = new ChannelTypeUID("hpprinter:totalsAdv");
    public static ChannelTypeUID chanTypeMarking = new ChannelTypeUID("hpprinter:cumlMarkingUsed");
    public static ChannelTypeUID chanTypeInkLevel = new ChannelTypeUID("hpprinter:inkLevel");

    // ********** Item Types **********
    public static final String ITEM_TYPE_CUMLMARK = "Number:Volume";
    public static final String ITEM_TYPE_INK = "Number:Dimensionless";

    // ********** List of all Channel ids **********
    // Status
    public static final String CHANNEL_STATUS = "status";
    public static final String CHANNEL_TRAYEMPTYOROPEN = "trayEmptyOrOpen";

    public static final String CHANNEL_SCANNER_STATUS = "scannerStatus";
    public static final String CHANNEL_SCANNER_ADFLOADED = "scannerAdfLoaded";

    // Colours
    public static final String CHANNEL_COLOR_LEVEL = "colorLevel";
    public static final String CHANNEL_CYAN_LEVEL = "cyanLevel";
    public static final String CHANNEL_MAGENTA_LEVEL = "magentaLevel";
    public static final String CHANNEL_YELLOW_LEVEL = "yellowLevel";
    public static final String CHANNEL_BLACK_LEVEL = "blackLevel";

    public static final String CHANNEL_COLOR_MARKING = "colorMarker";
    public static final String CHANNEL_CYAN_MARKING = "cyanMarker";
    public static final String CHANNEL_MAGENTA_MARKING = "magentaMarker";
    public static final String CHANNEL_YELLOW_MARKING = "yellowMarker";
    public static final String CHANNEL_BLACK_MARKING = "blackMarker";

    // Estimated Pages Remaining
    public static final String CHANNEL_COLOR_PAGES_REMAINING = "colorPagesRemaining";
    public static final String CHANNEL_CYAN_PAGES_REMAINING = "cyanPagesRemaining";
    public static final String CHANNEL_MAGENTA_PAGES_REMAINING = "magentaPagesRemaining";
    public static final String CHANNEL_YELLOW_PAGES_REMAINING = "yellowPagesRemaining";
    public static final String CHANNEL_BLACK_PAGES_REMAINING = "blackPagesRemaining";

    // Page Counts
    public static final String CHANNEL_TOTAL_PAGES = "totalCount";
    public static final String CHANNEL_SUBSCRIPTION = "subsciptionCount";
    public static final String CHANNEL_TOTAL_COLORPAGES = "totalColorCount";
    public static final String CHANNEL_TOTAL_MONOPAGES = "totalMonochromeCount";
    public static final String CHANNEL_JAM_EVENTS = "jamEvents";
    public static final String CHANNEL_MISPICK_EVENTS = "mispickEvents";
    public static final String CHANNEL_FRONT_PANEL_CANCEL = "fpCancelCount";
    public static final String CHANNEL_CLOUD_PRINT = "cloudPrint";

    // Scanner
    public static final String CHANNEL_TOTAL_ADF = "totalAdf";
    public static final String CHANNEL_TOTAL_FLATBED = "totalFlatbed";
    public static final String CHANNEL_TOTAL_TOEMAIL = "totalToEmail";
    public static final String CHANNEL_TOTAL_TOFOLDER = "totalToFolder";
    public static final String CHANNEL_TOTAL_TOHOST = "totalToHost";

    // App Usage
    public static final String CHANNEL_TOTAL_WIN = "totalWin";
    public static final String CHANNEL_TOTAL_ANDROID = "totalAndroid";
    public static final String CHANNEL_TOTAL_IOS = "totalIos";
    public static final String CHANNEL_TOTAL_OSX = "totalOsx";
    public static final String CHANNEL_TOTAL_SAMSUNG = "totalSamsung";
    public static final String CHANNEL_TOTAL_CHROME = "totalChrome";

    // ********** List of all Channel Groups **********
    public static final String CGROUP_INK = "ink";
    public static final String CGROUP_STATUS = "status";
    public static final String CGROUP_USAGE = "usage";
    public static final String CGROUP_SCANNER = "scanner"; // Scanner Engine
    public static final String CGROUP_SCAN = "scan"; // Scan Application
    public static final String CGROUP_COPY = "copy";
    public static final String CGROUP_APP = "app";
    public static final String CGROUP_OTHER = "other";
}
