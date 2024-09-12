/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import org.openhab.binding.homematic.internal.converter.ConverterException;
import org.openhab.binding.homematic.internal.model.HmDatapoint;
import org.openhab.binding.homematic.internal.model.HmInterface;
import org.openhab.binding.homematic.internal.type.MetadataUtils;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.types.Command;
import org.openhab.core.types.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts between a Homematic datapoint value and an openHAB PercentType.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class PercentTypeConverter extends AbstractTypeConverter<PercentType> {
    private final Logger logger = LoggerFactory.getLogger(PercentTypeConverter.class);

    @Override
    protected Object commandToBinding(Command command, HmDatapoint dp) throws ConverterException {
        if (command.getClass() == IncreaseDecreaseType.class) {
            PercentType type = convertFromBinding(dp);

            int percent = type.intValue();
            percent += command.equals(IncreaseDecreaseType.INCREASE) ? 10 : -10;
            percent = (percent / 10) * 10;
            percent = Math.min(100, percent);
            percent = Math.max(0, percent);
            return convertToBinding(new PercentType(percent), dp);
        } else if (command.getClass() == OnOffType.class) {
            PercentType type = new PercentType(command.equals(OnOffType.ON) ? 100 : 0);
            return convertToBinding(type, dp);
        } else if (command.getClass() == UpDownType.class) {
            return convertToBinding(command.equals(UpDownType.UP) ? PercentType.ZERO : PercentType.HUNDRED, dp);
        } else {
            return super.commandToBinding(command, dp);
        }
    }

    private double getCorrectedMaxValue(HmDatapoint dp) {
        double max = dp.getMaxValue().doubleValue();
        return (max == 1.01 && dp.getChannel().getDevice().getHmInterface() == HmInterface.HMIP ? 1.0d : max);
    }

    @Override
    protected boolean toBindingValidation(HmDatapoint dp, Class<? extends Type> typeClass) {
        return dp.isNumberType() && dp.getMaxValue() != null && dp.getMinValue() != null
                && dp.getChannel().getType() != null && typeClass.isAssignableFrom(PercentType.class);
    }

    @Override
    protected Object toBinding(PercentType type, HmDatapoint dp) throws ConverterException {
        double maxValue = getCorrectedMaxValue(dp);
        Double number = (type.doubleValue() / 100) * maxValue;

        if (MetadataUtils.isRollerShutter(dp)) {
            if (PercentType.HUNDRED.equals(type)) { // means DOWN
                return dp.getMinValue().doubleValue();
            } else if (PercentType.ZERO.equals(type)) { // means UP
                return maxValue;
            }
            return maxValue - number;
        }
        if (number < 0.0 || number > 100.0) {
            logger.warn("Percent value '{}' out of range, truncating value for {}", number, dp);
            number = number < 0.0 ? 0.0 : 100.0;
        }
        if (dp.isIntegerType()) {
            return number.intValue();
        }
        return round(number).doubleValue();
    }

    @Override
    protected boolean fromBindingValidation(HmDatapoint dp) {
        return dp.isNumberType() && dp.getValue() instanceof Number && dp.getMaxValue() != null
                && dp.getChannel().getType() != null;
    }

    @Override
    protected PercentType fromBinding(HmDatapoint dp) throws ConverterException {
        Double number = ((Number) dp.getValue()).doubleValue();
        int percent = (int) (100 / getCorrectedMaxValue(dp) * number);
        if (percent > 100) {
            logger.warn("Percent value '{}' out of range, truncating value for {}", number, dp);
            percent = 100;
        }
        if (MetadataUtils.isRollerShutter(dp)) {
            percent = 100 - percent;
        }
        return new PercentType(percent);
    }
}
