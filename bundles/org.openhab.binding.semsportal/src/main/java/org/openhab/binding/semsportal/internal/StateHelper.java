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
package org.openhab.binding.semsportal.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.semsportal.internal.dto.StationStatus;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
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

    public static State getOperational(@Nullable StationStatus currentStatus) {
        if (currentStatus == null) {
            return UnDefType.UNDEF;
        }
        return OnOffType.from(currentStatus.isOperational());
    }

    public static State getLastUpdate(@Nullable StationStatus currentStatus) {
        return currentStatus == null ? UnDefType.UNDEF : new DateTimeType(currentStatus.getLastUpdate());
    }

    public static State getCurrentOutput(@Nullable StationStatus status) {
        if (status == null) {
            return UnDefType.UNDEF;
        }
        if (status.getCurrentOutput() == null) {
            return UnDefType.NULL;
        }
        return new QuantityType<>(status.getCurrentOutput(), Units.WATT);
    }

    public static State getDayTotal(@Nullable StationStatus status) {
        if (status == null) {
            return UnDefType.UNDEF;
        }
        if (status.getDayTotal() == null) {
            return UnDefType.NULL;
        }
        return new QuantityType<>(status.getDayTotal(), Units.KILOWATT_HOUR);
    }

    public static State getMonthTotal(@Nullable StationStatus status) {
        if (status == null) {
            return UnDefType.UNDEF;
        }
        if (status.getMonthTotal() == null) {
            return UnDefType.NULL;
        }
        return new QuantityType<>(status.getMonthTotal(), Units.KILOWATT_HOUR);
    }

    public static State getOverallTotal(@Nullable StationStatus status) {
        if (status == null) {
            return UnDefType.UNDEF;
        }
        if (status.getOverallTotal() == null) {
            return UnDefType.NULL;
        }
        return new QuantityType<>(status.getOverallTotal(), Units.KILOWATT_HOUR);
    }

    public static State getDayIncome(@Nullable StationStatus status) {
        if (status == null) {
            return UnDefType.UNDEF;
        }
        if (status.getDayIncome() == null) {
            return UnDefType.NULL;
        }
        return new DecimalType(status.getDayIncome());
    }

    public static State getTotalIncome(@Nullable StationStatus status) {
        if (status == null) {
            return UnDefType.UNDEF;
        }
        if (status.getTotalIncome() == null) {
            return UnDefType.NULL;
        }
        return new DecimalType(status.getTotalIncome());
    }
}
