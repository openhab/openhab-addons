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
package org.openhab.binding.homematic.internal.converter.type;

import static org.openhab.binding.homematic.internal.misc.HomematicConstants.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.openhab.binding.homematic.internal.converter.ConverterException;
import org.openhab.binding.homematic.internal.converter.ConverterTypeException;
import org.openhab.binding.homematic.internal.converter.StateInvertInfo;
import org.openhab.binding.homematic.internal.converter.TypeConverter;
import org.openhab.binding.homematic.internal.model.HmDatapoint;
import org.openhab.binding.homematic.internal.model.HmDatapointInfo;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.Type;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for all Converters with common methods.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public abstract class AbstractTypeConverter<T extends State> implements TypeConverter<T> {
    private final Logger logger = LoggerFactory.getLogger(AbstractTypeConverter.class);

    /**
     * Defines all devices where the state datapoint must be inverted.
     */
    private static final List<StateInvertInfo> STATE_INVERT_INFO_LIST = new ArrayList<>(3);

    static {
        STATE_INVERT_INFO_LIST.add(new StateInvertInfo(DEVICE_TYPE_SHUTTER_CONTACT));
        STATE_INVERT_INFO_LIST.add(new StateInvertInfo(DEVICE_TYPE_SHUTTER_CONTACT_2));
        STATE_INVERT_INFO_LIST.add(new StateInvertInfo(DEVICE_TYPE_INCLINATION_SENSOR));
        STATE_INVERT_INFO_LIST.add(new StateInvertInfo(DEVICE_TYPE_WIRED_IO_MODULE, 15, 26));
        STATE_INVERT_INFO_LIST.add(new StateInvertInfo(DEVICE_TYPE_MAX_WINDOW_SENSOR));
        STATE_INVERT_INFO_LIST.add(new StateInvertInfo(DEVICE_TYPE_SHUTTER_CONTACT_INTERFACE));
    }

    /**
     * Checks the datapoint if the state value must be inverted.
     */
    protected boolean isStateInvertDatapoint(HmDatapoint dp) {
        if (DATAPOINT_NAME_STATE.equals(dp.getName())) {
            for (StateInvertInfo stateInvertInfo : STATE_INVERT_INFO_LIST) {
                if (stateInvertInfo.isToInvert(dp)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Rounds a double value.
     */
    protected BigDecimal round(Double number) {
        BigDecimal bd = new BigDecimal(number == null ? "0" : number.toString());
        String stringBd = bd.toPlainString();
        int scale = stringBd.length() - (stringBd.lastIndexOf('.') + 1);
        return bd.setScale(scale > 2 ? 6 : 2, RoundingMode.HALF_UP);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object convertToBinding(Type type, HmDatapoint dp) throws ConverterException {
        if (isLoggingRequired()) {
            logAtDefaultLevel(
                    "Converting type {} with value '{}' using {} to datapoint '{}' (dpType='{}', dpUnit='{}')",
                    type.getClass().getSimpleName(), type.toString(), this.getClass().getSimpleName(),
                    new HmDatapointInfo(dp), dp.getType(), dp.getUnit());
        }

        if (type == UnDefType.NULL) {
            return null;
        } else if (type.getClass().isEnum() && !(this instanceof OnOffTypeConverter)
                && !(this instanceof OpenClosedTypeConverter)) {
            return commandToBinding((Command) type, dp);
        } else if (!toBindingValidation(dp, type.getClass())) {
            String errorMessage = String.format("Can't convert type %s with value '%s' to %s value with %s for '%s'",
                    type.getClass().getSimpleName(), type.toString(), dp.getType(), this.getClass().getSimpleName(),
                    new HmDatapointInfo(dp));
            throw new ConverterTypeException(errorMessage);
        }

        return toBinding((T) type, dp);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T convertFromBinding(HmDatapoint dp) throws ConverterException {
        if (isLoggingRequired()) {
            logAtDefaultLevel("Converting datapoint '{}' (dpType='{}', dpUnit='{}', dpValue='{}') with {}",
                    new HmDatapointInfo(dp), dp.getType(), dp.getUnit(), dp.getValue(),
                    this.getClass().getSimpleName());
        }

        if (dp.getValue() == null) {
            return (T) UnDefType.NULL;
        } else if (!fromBindingValidation(dp)) {
            String errorMessage = String.format("Can't convert %s value '%s' with %s for '%s'", dp.getType(),
                    dp.getValue(), this.getClass().getSimpleName(), new HmDatapointInfo(dp));
            throw new ConverterTypeException(errorMessage);
        }

        return fromBinding(dp);
    }

    /**
     * By default, instances of {@link AbstractTypeConverter} log in level TRACE.
     * May be overridden to increase logging verbosity of a converter.
     *
     * @return desired LogLevel
     */
    protected LogLevel getDefaultLogLevelForTypeConverter() {
        return LogLevel.TRACE;
    }

    private boolean isLoggingRequired() {
        if (getDefaultLogLevelForTypeConverter() == LogLevel.TRACE) {
            return logger.isTraceEnabled();
        }
        if (getDefaultLogLevelForTypeConverter() == LogLevel.DEBUG) {
            return logger.isDebugEnabled();
        }
        return true;
    }

    private void logAtDefaultLevel(String format, Object... arguments) {
        switch (getDefaultLogLevelForTypeConverter()) {
            case TRACE:
                logger.trace(format, arguments);
                break;
            case DEBUG:
                logger.debug(format, arguments);
                break;
            case INFO:
            default:
                logger.info(format, arguments);
                break;
        }
    }

    /**
     * Converts an openHAB command to a Homematic value.
     */
    protected Object commandToBinding(Command command, HmDatapoint dp) throws ConverterException {
        throw new ConverterException("Unsupported command " + command.getClass().getSimpleName() + " for "
                + this.getClass().getSimpleName());
    }

    /**
     * Returns true, if the conversion from openHAB to the binding is possible.
     */
    protected abstract boolean toBindingValidation(HmDatapoint dp, Class<? extends Type> typeClass);

    /**
     * Converts the type to a datapoint value.
     */
    protected abstract Object toBinding(T type, HmDatapoint dp) throws ConverterException;

    /**
     * Returns true, if the conversion from the binding to openHAB is possible.
     */
    protected abstract boolean fromBindingValidation(HmDatapoint dp);

    /**
     * Converts the datapoint value to an openHAB type.
     */
    protected abstract T fromBinding(HmDatapoint dp) throws ConverterException;

    protected enum LogLevel {
        TRACE,
        INFO,
        DEBUG
    }
}
