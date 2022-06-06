/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.publicholiday.internal.publicholiday.definitions;

/**
 * Some general fixed holiday definitions
 * 
 * @author Martin Güthle - Initial contribution
 */
public enum GeneralFixedHolidayDef implements FixedDayDef {
    NEW_YEAR("New year", 1, 1),
    THREE_KINGS_DAY("Three Kings day", 1, 6),
    REFORMATION_DAY("Reformation Day", 10, 31),
    ALL_SAINTS_DAY("All Saints Day", 11, 1),
    CHRISTMAS_EVE("Christmas Eve", 12, 24),
    CHRISTMAS_DAY("Christmas Day", 12, 25),
    SECOND_CHRISTMAS_DAY("2nd Christmas Day", 12, 26),
    NEW_YEARS_EVE("New Years Eve", 12, 31);

    private final String name;
    private final int month;
    private final int day;

    private GeneralFixedHolidayDef(String name, int month, int day) {
        this.name = name;
        this.month = month;
        this.day = day;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getMonth() {
        return month;
    }

    @Override
    public int getDay() {
        return day;
    }
}
