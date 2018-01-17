/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hp1000.handler;

import static org.openhab.binding.hp1000.HP1000BindingConstants.WEBHOOK_DATE_TIME_FORMAT;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;

/**
 * The {@link StateConverterUtils} is a helper to convert values to state
 *
 * @author Daniel Bauer - Initial contribution
 */
final class StateConverterUtils {

    @Nullable
    public static Double parseDouble(String value) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }

        return Double.valueOf(value);
    }

    public static State toDateTimeType(@Nullable String value) {
        DateTimeType dateTimeType;
        try {
            dateTimeType = new DateTimeType(value);
        } catch (IllegalArgumentException exception) {
            DateTimeFormatter parser = DateTimeFormatter.ofPattern(WEBHOOK_DATE_TIME_FORMAT)
                    .withZone(ZoneId.systemDefault());
            dateTimeType = new DateTimeType(ZonedDateTime.parse(value, parser));
        }

        return (value == null) ? UnDefType.NULL : dateTimeType;
    }

    public static State toDecimalType(@Nullable Double value) {
        return (value == null) ? UnDefType.NULL : new DecimalType(value);
    }

    public static State toStringType(@Nullable String value) {
        return (value == null) ? UnDefType.NULL : new StringType(value);
    }

    private StateConverterUtils() {
        // helper class
    }
}
