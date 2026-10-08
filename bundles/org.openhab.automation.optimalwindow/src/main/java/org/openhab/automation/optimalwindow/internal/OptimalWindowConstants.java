/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

package org.openhab.automation.optimalwindow.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Constants of the Optimal Window automation.
 *
 * @author Thomas Leber - Initial contribution
 */
@NonNullByDefault
public class OptimalWindowConstants {
    public static final String AUTOMATION_NAME = "optimalwindow";

    public static final String TRIGGER_TYPE_ID = AUTOMATION_NAME + ".WindowTrigger";
    public static final String CONDITION_TYPE_ID = AUTOMATION_NAME + ".InWindowCondition";

    // window configuration
    public static final String CONFIG_FORECAST_ITEM = "forecastItem";
    public static final String CONFIG_PERSISTENCE_SERVICE = "persistenceService";
    public static final String CONFIG_RANGE_START = "rangeStart";
    public static final String CONFIG_RANGE_DURATION = "rangeDuration";
    public static final String CONFIG_LENGTH = "length";
    public static final String CONFIG_CONSECUTIVE = "consecutive";
    public static final String CONFIG_GOAL = "goal";
    public static final String CONFIG_PREFER_START = "preferStart";

    // trigger status items
    public static final String CONFIG_ACTIVE_ITEM = "activeItem";
    public static final String CONFIG_START_ITEM = "startItem";
    public static final String CONFIG_END_ITEM = "endItem";
    public static final String CONFIG_REMAINING_ITEM = "remainingItem";
    public static final String CONFIG_COUNTDOWN_ITEM = "countdownItem";
    public static final String CONFIG_WINDOW_TEXT_ITEM = "windowTextItem";

    // trigger outputs
    public static final String OUTPUT_EVENT = "event";
    public static final String OUTPUT_COMMAND = "command";
    public static final String OUTPUT_START = "start";
    public static final String OUTPUT_END = "end";
    public static final String OUTPUT_AVERAGE = "average";

    public static final String EVENT_START = "START";
    public static final String EVENT_END = "END";

    public static final String GOAL_MINIMUM = "minimum";
    public static final String GOAL_MAXIMUM = "maximum";
}
