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

/**
 * This class holds various channel values conversion methods
 *
 * @author Gaël L'hopital
 */
public class ChannelTypeUtils {
    public static DateTimeType toDateTimeType(Integer netatmoTS) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(netatmoTS * 1000L);
        return new DateTimeType(calendar);
    }

    public static DecimalType toDecimalType(float value) {
        BigDecimal decimal = new BigDecimal(value).setScale(2, BigDecimal.ROUND_HALF_UP);
        return new DecimalType(decimal);
    }

    public static DecimalType toDecimalType(double value) {
        BigDecimal decimal = new BigDecimal(value).setScale(2, BigDecimal.ROUND_HALF_UP);
        return new DecimalType(decimal);
    }
}
