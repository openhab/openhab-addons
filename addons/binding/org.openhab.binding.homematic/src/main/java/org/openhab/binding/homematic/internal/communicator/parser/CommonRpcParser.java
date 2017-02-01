/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.communicator.parser;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.openhab.binding.homematic.internal.model.HmDatapoint;

/**
 * Abstract base class for all parsers with common methods.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public abstract class CommonRpcParser<M, R> implements RpcParser<M, R> {

    /**
     * Converts the object to a string.
     */
    protected String toString(Object object) {
        return StringUtils.trimToNull(ObjectUtils.toString(object));
    }

    /**
     * Converts the object to a integer.
     */
    protected Integer toInteger(Object object) {
        if (object == null || object instanceof Integer) {
            return (Integer) object;
        }
        return NumberUtils.createInteger(ObjectUtils.toString(object));
    }

    /**
     * Converts the object to a number.
     */
    protected Number toNumber(Object object) {
        if (object == null || object instanceof Number) {
            return (Number) object;
        }
        return NumberUtils.createNumber(ObjectUtils.toString(object));
    }

    /**
     * Converts the object to a boolean.
     */
    protected Boolean toBoolean(Object object) {
        if (object == null || object instanceof Boolean) {
            return (Boolean) object;
        }
        return BooleanUtils.toBoolean(ObjectUtils.toString(object));
    }

    /**
     * Converts the object to a string array.
     */
    protected String[] toOptionList(Object optionList) {
        if (optionList != null && optionList instanceof Object[]) {
            Object[] vl = (Object[]) optionList;
            String[] stringArray = new String[vl.length];
            for (int i = 0; i < vl.length; i++) {
                stringArray[i] = vl[i].toString();
            }
            return stringArray;
        }
        return null;
    }

    /**
     * Returns the address of a device, replacing group address identifier.
     */
    protected String getAddress(Object object) {
        return StringUtils.replaceOnce(toString(object), "*", "T-");
    }

    /**
     * Adjust uninitialized rssi values to zero.
     */
    protected void adjustRssiValue(HmDatapoint dp) {
        if (dp.getValue() != null && dp.getName().startsWith("RSSI_") && dp.isIntegerType()) {
            int rssiValue = ((Number) dp.getValue()).intValue();
            if (rssiValue >= 255 || rssiValue <= -255) {
                dp.setValue(new Integer(0));
            }
        }
    }

    /**
     * Converts the type of the value if necessary and sets the value of the datapoint.
     */
    protected void setDatapointValue(HmDatapoint dp, Object value) {
        if (dp.isBooleanType()) {
            dp.setValue(toBoolean(value));
        } else if (dp.isIntegerType()) {
            dp.setValue(toInteger(value));
        } else if (dp.isFloatType()) {
            dp.setValue(toNumber(value));
        } else if (dp.isStringType()) {
            dp.setValue(toString(value));
        } else {
            dp.setValue(value);
        }
    }

}
