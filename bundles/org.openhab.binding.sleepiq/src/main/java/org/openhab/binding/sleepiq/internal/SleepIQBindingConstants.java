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
package org.openhab.binding.sleepiq.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SleepIQBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Gregory Moyer - Initial contribution
 */
@NonNullByDefault
public class SleepIQBindingConstants {
    public static final String BINDING_ID = "sleepiq";

    // List of all Thing Type UIDs

    // bridge
    public static final ThingTypeUID THING_TYPE_CLOUD = new ThingTypeUID(BINDING_ID, "cloud");

    // generic thing types
    // public static final ThingTypeUID THING_TYPE_SINGLE_BED = new ThingTypeUID(BINDING_ID, "singleBed");
    public static final ThingTypeUID THING_TYPE_DUAL_BED = new ThingTypeUID(BINDING_ID, "dualBed");

    // List of all Channel ids
    // public static final String CHANNEL_BED_IN_BED = "bed#inBed";
    public static final String CHANNEL_LEFT_IN_BED = "left#inBed";
    public static final String CHANNEL_RIGHT_IN_BED = "right#inBed";

    // public static final String CHANNEL_BED_SLEEP_NUMBER = "bed#sleepNumber";
    public static final String CHANNEL_LEFT_SLEEP_NUMBER = "left#sleepNumber";
    public static final String CHANNEL_RIGHT_SLEEP_NUMBER = "right#sleepNumber";

    // public static final String CHANNEL_BED_PRESSURE = "bed#pressure";
    public static final String CHANNEL_LEFT_PRESSURE = "left#pressure";
    public static final String CHANNEL_RIGHT_PRESSURE = "right#pressure";

    // public static final String CHANNEL_BED_LAST_LINK = "bed#lastLink";
    public static final String CHANNEL_LEFT_LAST_LINK = "left#lastLink";
    public static final String CHANNEL_RIGHT_LAST_LINK = "right#lastLink";

    // public static final String CHANNEL_BED_ALERT_ID = "bed#alertId";
    public static final String CHANNEL_LEFT_ALERT_ID = "left#alertId";
    public static final String CHANNEL_RIGHT_ALERT_ID = "right#alertId";

    // public static final String CHANNEL_BED_ALERT_DETAILED_MESSAGE = "bed#alertDetailedMessage";
    public static final String CHANNEL_LEFT_ALERT_DETAILED_MESSAGE = "left#alertDetailedMessage";
    public static final String CHANNEL_RIGHT_ALERT_DETAILED_MESSAGE = "right#alertDetailedMessage";

    // public static final String CHANNEL_BED_FIRST_NAME = "bed#firstName";
    public static final String CHANNEL_LEFT_FIRST_NAME = "left#firstName";
    public static final String CHANNEL_RIGHT_FIRST_NAME = "right#firstName";

    // public static final String CHANNEL_BED_SLEEP_GOAL_MINUTES = "bed#sleepGoalMinutes";
    public static final String CHANNEL_LEFT_SLEEP_GOAL_MINUTES = "left#sleepGoalMinutes";
    public static final String CHANNEL_RIGHT_SLEEP_GOAL_MINUTES = "right#sleepGoalMinutes";

    // public static final String CHANNEL_BED_SLEEP_GOAL_MINUTES = "bed#privateMode";
    public static final String CHANNEL_LEFT_PRIVACY_MODE = "left#privacyMode";
    public static final String CHANNEL_RIGHT_PRIVACY_MODE = "right#privacyMode";

    // public static final String CHANNEL_BED_TODAY_SLEEP_IQ = "bed#todaySleepIQ";
    public static final String CHANNEL_LEFT_TODAY_SLEEP_IQ = "left#todaySleepIQ";
    public static final String CHANNEL_RIGHT_TODAY_SLEEP_IQ = "right#todaySleepIQ";

    // public static final String CHANNEL_BED_TODAY_AVG_HEART_RATE = "bed#todayAverageHeartRate";
    public static final String CHANNEL_LEFT_TODAY_AVG_HEART_RATE = "left#todayAverageHeartRate";
    public static final String CHANNEL_RIGHT_TODAY_AVG_HEART_RATE = "right#todayAverageHeartRate";

    // public static final String CHANNEL_BED_TODAY_AVG_RESPIRATION_RATE = "bed#todayAverageRespirationRate";
    public static final String CHANNEL_LEFT_TODAY_AVG_RESPIRATION_RATE = "left#todayAverageRespirationRate";
    public static final String CHANNEL_RIGHT_TODAY_AVG_RESPIRATION_RATE = "right#todayAverageRespirationRate";

    // public static final String CHANNEL_BED_TODAY_MESSAGE = "bed#todayMessage";
    public static final String CHANNEL_LEFT_TODAY_MESSAGE = "left#todayMessage";
    public static final String CHANNEL_RIGHT_TODAY_MESSAGE = "right#todayMessage";

    // public static final String CHANNEL_BED_TODAY_SLEEP_DURATION_SECONDS = "bed#todaySleepDurationSeconds";
    public static final String CHANNEL_LEFT_TODAY_SLEEP_DURATION_SECONDS = "left#todaySleepDurationSeconds";
    public static final String CHANNEL_RIGHT_TODAY_SLEEP_DURATION_SECONDS = "right#todaySleepDurationSeconds";

    // public static final String CHANNEL_BED_MONTHLY_SLEEP_IQ = "bed#monthlySleepIQ";
    public static final String CHANNEL_LEFT_MONTHLY_SLEEP_IQ = "left#monthlySleepIQ";
    public static final String CHANNEL_RIGHT_MONTHLY_SLEEP_IQ = "right#monthlySleepIQ";

    // public static final String CHANNEL_BED_MONTHLY_AVG_HEART_RATE = "bed#monthlyAverageHeartRate";
    public static final String CHANNEL_LEFT_MONTHLY_AVG_HEART_RATE = "left#monthlyAverageHeartRate";
    public static final String CHANNEL_RIGHT_MONTHLY_AVG_HEART_RATE = "right#monthlyAverageHeartRate";

    // public static final String CHANNEL_BED_MONTHLY_AVG_RESPIRATION_RATE = "bed#monthlyAverageRespirationRate";
    public static final String CHANNEL_LEFT_MONTHLY_AVG_RESPIRATION_RATE = "left#monthlyAverageRespirationRate";
    public static final String CHANNEL_RIGHT_MONTHLY_AVG_RESPIRATION_RATE = "right#monthlyAverageRespirationRate";

    // List of non-standard Properties
    public static final String PROPERTY_BASE = "base";
    public static final String PROPERTY_KIDS_BED = "kidsBed";
    public static final String PROPERTY_MAC_ADDRESS = "macAddress";
    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_PURCHASE_DATE = "purchaseDate";
    public static final String PROPERTY_SIZE = "size";
    public static final String PROPERTY_SKU = "sku";
}
