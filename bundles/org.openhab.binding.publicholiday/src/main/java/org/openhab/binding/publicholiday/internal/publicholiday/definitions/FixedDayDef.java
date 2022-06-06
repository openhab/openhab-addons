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

import org.openhab.binding.publicholiday.internal.publicholiday.HolidayJob;

/**
 * Definition of a fixed holiday
 *
 * Based on the month and the day, the {@link HolidayJob} decides whether the current and the next day is a public
 * holiday or not.
 * 
 * @author Martin Güthle - Initial contribution
 */
public interface FixedDayDef {

    String getName();

    int getMonth();

    int getDay();
}
