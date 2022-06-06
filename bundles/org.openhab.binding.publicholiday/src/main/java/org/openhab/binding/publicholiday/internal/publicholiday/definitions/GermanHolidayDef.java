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
 * German specific holiday definitions
 * 
 * @author Martin Güthle - Initial contribution
 */
public enum GermanHolidayDef implements FixedDayDef {
    TAG_DER_ARBEIT("Tag der Arbeit", 5, 1),
    TAG_DER_DEUTSCHEN_EINHEIT("Tag der deutschen Einheit", 10, 3);

    private final String name;
    private final int month;
    private final int day;

    private GermanHolidayDef(String name, int month, int day) {
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
