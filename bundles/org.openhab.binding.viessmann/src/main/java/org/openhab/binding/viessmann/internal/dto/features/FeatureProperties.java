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
package org.openhab.binding.viessmann.internal.dto.features;

import java.util.ArrayList;

/**
 * The {@link FeatureProperties} provides properties of features
 *
 * @author Ronny Grun - Initial contribution
 */
public class FeatureProperties {
    public FeatureString value;
    public FeatureString status;
    public FeatureBoolean active;
    public FeatureString name;
    public FeatureInteger shift;
    public FeatureDouble slope;
    public FeatureEntriesWeekDays entries;
    public FeatureErrorEntries errorEntries;
    public FeatureBoolean overlapAllowed;
    public FeatureInteger temperature;
    public FeatureString start;
    public FeatureString end;
    public FeatureInteger top;
    public FeatureInteger middle;
    public FeatureInteger bottom;
    public FeatureListDouble day;
    public FeatureListDouble week;
    public FeatureListDouble month;
    public FeatureListDouble year;
    public FeatureString unit;
    public FeatureDouble hours;
    public FeatureInteger starts;
    public FeatureInteger hoursLoadClassOne;
    public FeatureInteger hoursLoadClassTwo;
    public FeatureInteger hoursLoadClassThree;
    public FeatureInteger hoursLoadClassFour;
    public FeatureInteger hoursLoadClassFive;
    public FeatureInteger min;
    public FeatureInteger max;
    public FeatureString phase;
    public FeatureString switchOnValue;
    public FeatureString switchOffValue;

    public ArrayList<String> getUsedEntries() {
        ArrayList<String> list = new ArrayList<>();

        if (value != null) {
            list.add("value");
        }
        if (status != null) {
            list.add("status");
        }
        if (active != null) {
            list.add("active");
        }
        if (name != null) {
            list.add("name");
        }
        if (shift != null) {
            list.add("shift");
        }
        if (slope != null) {
            list.add("slope");
        }
        if (entries != null) {
            list.add("entries");
        }
        if (overlapAllowed != null) {
            list.add("overlapAllowed");
        }
        if (temperature != null) {
            list.add("temperature");
        }
        if (start != null) {
            list.add("start");
        }
        if (end != null) {
            list.add("end");
        }
        if (top != null) {
            list.add("top");
        }
        if (middle != null) {
            list.add("middle");
        }
        if (bottom != null) {
            list.add("bottom");
        }
        if (day != null) {
            list.add("day");
        }
        if (week != null) {
            list.add("week");
        }
        if (month != null) {
            list.add("month");
        }
        if (year != null) {
            list.add("year");
        }
        if (unit != null) {
            list.add("unit");
        }
        if (hours != null) {
            list.add("hours");
        }
        if (starts != null) {
            list.add("starts");
        }
        if (hoursLoadClassOne != null) {
            list.add("hoursLoadClassOne");
        }
        if (hoursLoadClassOne != null) {
            list.add("hoursLoadClassTwo");
        }
        if (hoursLoadClassOne != null) {
            list.add("hoursLoadClassThree");
        }
        if (hoursLoadClassOne != null) {
            list.add("hoursLoadClassFour");
        }
        if (hoursLoadClassOne != null) {
            list.add("hoursLoadClassFive");
        }
        if (min != null) {
            list.add("min");
        }
        if (max != null) {
            list.add("max");
        }
        if (phase != null) {
            list.add("phase");
        }
        if (switchOnValue != null) {
            list.add("switchOnValue");
        }
        if (switchOffValue != null) {
            list.add("switchOffValue");
        }

        return list;
    }
}
