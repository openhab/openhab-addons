/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.converter.type;

import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.Type;
import org.openhab.binding.homematic.internal.converter.ConverterException;
import org.openhab.binding.homematic.internal.model.HmDatapoint;
import org.openhab.binding.homematic.internal.type.MetadataUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts between a Homematic datapoint value and a openHAB PercentType.
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
            int result = command.equals(UpDownType.UP) ? 100 : 0;
            if (MetadataUtils.isRollerShutter(dp)) {
                result = command.equals(UpDownType.UP) ? 0 : 100;
            }
            return convertToBinding(new PercentType(result), dp);
        } else {
            return super.commandToBinding(command, dp);
        }
    }

    @Override
    protected boolean toBindingValidation(HmDatapoint dp, Class<? extends Type> typeClass) {
        return dp.isNumberType() && dp.getMaxValue() != null && dp.getMinValue() != null
                && dp.getChannel().getType() != null && typeClass.isAssignableFrom(PercentType.class);
    }

    @Override
    protected Object toBinding(PercentType type, HmDatapoint dp) throws ConverterException {
        Double number = (type.doubleValue() / 100) * dp.getMaxValue().doubleValue();

        if (MetadataUtils.isRollerShutter(dp)) {
            number = dp.getMaxValue().doubleValue() - number;
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
        int percent = (int) ((100 / dp.getMaxValue().doubleValue()) * number);

        if (MetadataUtils.isRollerShutter(dp)) {
            percent = 100 - percent;
        }
        return new PercentType(percent);
    }
}
