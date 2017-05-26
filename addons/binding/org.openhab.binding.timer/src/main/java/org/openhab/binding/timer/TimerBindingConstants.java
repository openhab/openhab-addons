/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.timer;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link TimerBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Neil Renaud - Initial contribution
 */
public class TimerBindingConstants {

    private static final String BINDING_ID = "timer";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_DAILY_TIMER = new ThingTypeUID(BINDING_ID, "dailyTimer");
    public static final ThingTypeUID THING_TYPE_PERIODIC_TIMER = new ThingTypeUID(BINDING_ID, "periodicTimer");
    public static final ThingTypeUID THING_TYPE_ONE_TIME_BY_DATE_TIMER = new ThingTypeUID(BINDING_ID,
            "oneTimeByDateTimer");
    public static final ThingTypeUID THING_TYPE_ONE_TIME_BY_DELAY_TIMER = new ThingTypeUID(BINDING_ID,
            "oneTimeByDelayTimer");
    public static final ThingTypeUID THING_TYPE_MONTHLY_TIMER = new ThingTypeUID(BINDING_ID, "monthlyTimer");

    // List of all Channel ids
    public static final String CHANNEL_ENABLED = "enabled";
    public static final String CHANNEL_DESCRIPTION = "description";
    public static final String CHANNEL_ON_TIME_HOURS = "on_time_hours";
    public static final String CHANNEL_ON_TIME_MINUTES = "on_time_minutes";
    public static final String CHANNEL_ON_TIME_SECONDS = "on_time_seconds";
    public static final String CHANNEL_OFF_TIME_HOURS = "off_time_hours";
    public static final String CHANNEL_OFF_TIME_MINUTES = "off_time_minutes";
    public static final String CHANNEL_OFF_TIME_SECONDS = "off_time_seconds";
    public static final String CHANNEL_RUN_ON_MON = "run_on_mon";
    public static final String CHANNEL_RUN_ON_TUE = "run_on_tue";
    public static final String CHANNEL_RUN_ON_WED = "run_on_wed";
    public static final String CHANNEL_RUN_ON_THU = "run_on_thu";
    public static final String CHANNEL_RUN_ON_FRI = "run_on_fri";
    public static final String CHANNEL_RUN_ON_SAT = "run_on_sat";
    public static final String CHANNEL_RUN_ON_SUN = "run_on_sun";
    public static final String CHANNEL_STATUS = "status";

}
