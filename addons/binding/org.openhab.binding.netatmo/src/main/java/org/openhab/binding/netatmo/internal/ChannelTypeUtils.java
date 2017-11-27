/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.internal;

import java.math.BigDecimal;
import java.util.Calendar;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;

/**
 * This class holds various channel values conversion methods
 *
 * @author Gaël L'hopital
 */
public class ChannelTypeUtils {
    public static State toStringType(String value) {
        return (value == null) ? UnDefType.NULL : new StringType(value);
    }

    public static State toDateTimeType(Integer netatmoTS) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(netatmoTS * 1000L);
        return toDateTimeType(calendar);
    }

    public static State toDateTimeType(Calendar calendar) {
        return (calendar == null) ? UnDefType.NULL : new DateTimeType(calendar);
    }

    public static State toDecimalType(Float value) {
        return (value == null) ? UnDefType.NULL : toDecimalType(new BigDecimal(value));
    }

    public static State toDecimalType(Double value) {
        return (value == null) ? UnDefType.NULL : toDecimalType(new BigDecimal(value));
    }

    public static State toDecimalType(float value) {
        return toDecimalType(new BigDecimal(value));
    }

    public static State toDecimalType(double value) {
        return toDecimalType(new BigDecimal(value));
    }

    public static State toDecimalType(BigDecimal decimal) {
        if (decimal == null) {
            return UnDefType.NULL;
        } else {
            return new DecimalType(decimal.setScale(2, BigDecimal.ROUND_HALF_UP));
        }
    }
}
