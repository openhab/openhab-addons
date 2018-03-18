/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.meterreader.internal;

import java.util.function.Function;

/**
 *
 * @author MatthiasS
 *
 */
public class NegateHandler {

    public static boolean shouldNegateState(String negateProperty, Function<String, MeterValue> getObisValueFunction) {
        NegateBitModel negateModel = NegateBitParser.parseNegateProperty(negateProperty);
        MeterValue value = getObisValueFunction.apply(negateModel.getNegateChannelId());
        if (value != null) {
            boolean negateBit = isNegateSet(negateModel.isStatus() ? value.getStatus() : value.getValue(),
                    negateModel.getNegatePosition());

            return negateBit == negateModel.isNegateBit();
        }
        return false;
    }

    private static boolean isNegateSet(String value, int negatePosition) {
        long longValue = Long.parseLong(value);
        return (longValue & (1L << negatePosition)) != 0;
    }
}
