/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.semsportal.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.semsportal.internal.dto.SEMSStatus;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Helper class to convert the POJOs of the SEMS portal response classes into openHAB State objects.
 *
 * @author Iwan Bron - Initial contribution
 */
@NonNullByDefault
public class StateHelper {

    private StateHelper() {
        // hide constructor, no initialisation possible
    }

    public static State getOnline(@Nullable SEMSStatus currentStatus) {
        return currentStatus == null || !currentStatus.isOnline() ? OnOffType.OFF : OnOffType.ON;
    }

    public static State getLastUpdate(@Nullable SEMSStatus currentStatus) {
        return currentStatus == null ? UnDefType.NULL : new DateTimeType(currentStatus.getLastUpdate());
    }

    public static State getCurrentOutput(@Nullable SEMSStatus status) {
        return new DecimalType(status == null || status.getCurrentOutput() == null ? 0d : status.getCurrentOutput());
    }

    public static State getDayTotal(@Nullable SEMSStatus status) {
        return new DecimalType(status == null || status.getDayTotal() == null ? 0d : status.getDayTotal());
    }

    public static State getMonthTotal(@Nullable SEMSStatus status) {
        return new DecimalType(status == null || status.getMonthTotal() == null ? 0d : status.getMonthTotal());
    }

    public static State getOverallTotal(@Nullable SEMSStatus status) {
        return new DecimalType(status == null || status.getOverallTotal() == null ? 0d : status.getOverallTotal());
    }

    public static State getDayIncome(@Nullable SEMSStatus status) {
        return new DecimalType(status == null || status.getDayIncome() == null ? 0d : status.getDayIncome());
    }

    public static State getTotalIncome(@Nullable SEMSStatus status) {
        return new DecimalType(status == null || status.getTotalIncome() == null ? 0d : status.getTotalIncome());
    }
}
