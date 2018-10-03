/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.meterreader.internal.conformity.negate;

import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.meterreader.internal.MeterValue;

/**
 *
 * @author MatthiasS
 *
 */
@NonNullByDefault
public class NegateHandler {

    public static boolean shouldNegateState(String negateProperty,
            Function<String, @Nullable MeterValue<?>> getObisValueFunction) {
        NegateBitModel negateModel = NegateBitParser.parseNegateProperty(negateProperty);
        MeterValue<?> value = getObisValueFunction.apply(negateModel.getNegateChannelId());
        boolean isStatus = negateModel.isStatus();
        if (value != null) {
            String status = value.getStatus();
            String stringValue;
            ;
            if (isStatus && status != null) {
                stringValue = status;
            } else {
                stringValue = value.getValue();
            }
            boolean negateBit = isNegateSet(stringValue, negateModel.getNegatePosition());

            return negateBit == negateModel.isNegateBit();
        } else {
            return false;
        }
    }

    public static boolean isNegateSet(String value, int negatePosition) {
        long longValue = Long.parseLong(value);
        return (longValue & (1L << negatePosition)) != 0;
    }
}
