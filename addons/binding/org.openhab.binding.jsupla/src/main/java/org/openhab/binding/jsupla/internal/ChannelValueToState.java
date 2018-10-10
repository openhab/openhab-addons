/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jsupla.internal;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.State;
import pl.grzeslowski.jsupla.protocoljava.api.channels.values.ChannelValueSwitch;
import pl.grzeslowski.jsupla.protocoljava.api.channels.values.DecimalValue;
import pl.grzeslowski.jsupla.protocoljava.api.channels.values.OnOff;
import pl.grzeslowski.jsupla.protocoljava.api.channels.values.OpenClose;
import pl.grzeslowski.jsupla.protocoljava.api.channels.values.PercentValue;
import pl.grzeslowski.jsupla.protocoljava.api.channels.values.RgbValue;
import pl.grzeslowski.jsupla.protocoljava.api.channels.values.StoppableOpenClose;
import pl.grzeslowski.jsupla.protocoljava.api.channels.values.TemperatureAndHumidityValue;
import pl.grzeslowski.jsupla.protocoljava.api.channels.values.TemperatureValue;
import pl.grzeslowski.jsupla.protocoljava.api.channels.values.UnknownValue;

/**
 * @author Grzeslowski - Initial contribution
 */
public class ChannelValueToState implements ChannelValueSwitch.Callback<State> {
    @Override
    public State onDecimalValue(final DecimalValue decimalValue) {
        return new DecimalType(decimalValue.value);
    }

    @Override
    public State onOnOff(final OnOff onOff) {
        if (onOff == OnOff.ON) {
            return OnOffType.ON;
        } else {
            return OnOffType.OFF;
        }
    }

    @Override
    public State onOpenClose(final OpenClose openClose) {
        if (openClose == OpenClose.OPEN) {
            return OpenClosedType.OPEN;
        } else {
            return OpenClosedType.CLOSED;
        }
    }

    @Override
    public State onPercentValue(final PercentValue percentValue) {
        return new PercentType(percentValue.getValue());
    }

    @Override
    public State onRgbValue(final RgbValue rgbValue) {
        return HSBType.fromRGB(rgbValue.red, rgbValue.green, rgbValue.blue);
    }

    @Override
    public State onStoppableOpenClose(final StoppableOpenClose stoppableOpenClose) {
        if (stoppableOpenClose == StoppableOpenClose.OPEN) {
            return OpenClosedType.OPEN;
        } else {
            return OpenClosedType.CLOSED;
        }
    }

    @Override
    public State onTemperatureValue(final TemperatureValue temperatureValue) {
        return new DecimalType(temperatureValue.temperature);
    }

    @Override
    public State onTemperatureAndHumidityValue(final TemperatureAndHumidityValue temperatureAndHumidityValue) {
        return new DecimalType(temperatureAndHumidityValue.temperature); // TODO support humidity also
    }

    @Override
    public State onUnknownValue(final UnknownValue unknownValue) {
        return StringType.valueOf(unknownValue.message);
    }
}
