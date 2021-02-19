/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.bmwconnecteddrive.internal.utils;

import java.time.DayOfWeek;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ChargeProfileUtils} utility functions for charging profiles
 *
 * @author Norbert Truchsess - initial contribution
 */
@NonNullByDefault
public class ChargeProfileUtils {

    public static String formatDays(final Set<DayOfWeek> weekdays) {
        return weekdays.stream().map(day -> Constants.DAYS.get(day)).collect(Collectors.joining(Constants.COMMA));
    }
}
