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
package org.openhab.binding.publicholiday.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link PublicHolidayConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Martin Güthle - Initial contribution
 */
@NonNullByDefault
public class PublicHolidayConfiguration {

    public String updateValueCron = "";
    public boolean newYear = false;
    public boolean threeKingsDay = false;
    public boolean reformationDay = false;
    public boolean allSaintsDay = false;
    public boolean christmasEve = false;
    public boolean christmasDay = false;
    public boolean secondChristmasDay = false;
    public boolean newYearsEve = false;

    public boolean goodFriday = false;
    public boolean easterSunday = false;
    public boolean easterMonday = false;
    public boolean ascensionDay = false;
    public boolean whitSunday = false;
    public boolean whitMonday = false;
    public boolean corpusChristi = false;

    public boolean tagDerArbeit = false;
    public boolean tagDerDeutschenEinheit = false;
}
