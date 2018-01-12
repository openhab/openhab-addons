/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.regoheatpump.internal.rego6xx;

import static org.openhab.binding.regoheatpump.RegoHeatPumpBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.types.State;;

/**
 * The {@link RegoRegisterMapper} is responsible for mapping rego 6xx registers into channels.
 *
 * @author Boris Krivonog - Initial contribution
 */
public class RegoRegisterMapper {
    public static final RegoRegisterMapper rego600;

    public static interface Channel {
        public short address();

        public State convert(Short value);
    }

    private static class Int16Channel implements Channel {
        private final short address;

        private Int16Channel(short address) {
            this.address = address;
        }

        @Override
        public short address() {
            return address;
        }

        @Override
        public State convert(Short value) {
            return new DecimalType(value);
        }
    }

    private static class TemperatureChannel extends Int16Channel {
        private TemperatureChannel(short address) {
            super(address);
        }

        @Override
        public State convert(Short value) {
            return new DecimalType(ValueConverter.toDouble(value));
        }
    }

    private final Map<String, Channel> mappings;

    private RegoRegisterMapper(Map<String, Channel> mappings) {
        this.mappings = mappings;
    }

    public Channel map(String channelIID) {
        return mappings.get(channelIID);
    }

    static {
        final Map<String, Channel> mappings = new HashMap<>();
        {
            // Sensor values
            mappings.put(CHANNEL_GROUP_SENSOR_VALUES + "radiatorReturn", new TemperatureChannel((short) 521));
            mappings.put(CHANNEL_GROUP_SENSOR_VALUES + "outdoor", new TemperatureChannel((short) 522));
            mappings.put(CHANNEL_GROUP_SENSOR_VALUES + "hotWater", new TemperatureChannel((short) 523));
            mappings.put(CHANNEL_GROUP_SENSOR_VALUES + "radiatorForward", new TemperatureChannel((short) 524));
            mappings.put(CHANNEL_GROUP_SENSOR_VALUES + "indoor", new TemperatureChannel((short) 525));
            mappings.put(CHANNEL_GROUP_SENSOR_VALUES + "compressor", new TemperatureChannel((short) 526));
            mappings.put(CHANNEL_GROUP_SENSOR_VALUES + "heatFluidOut", new TemperatureChannel((short) 527));
            mappings.put(CHANNEL_GROUP_SENSOR_VALUES + "heatFluidIn", new TemperatureChannel((short) 528));
            mappings.put(CHANNEL_GROUP_SENSOR_VALUES + "coldFluidIn", new TemperatureChannel((short) 529));
            mappings.put(CHANNEL_GROUP_SENSOR_VALUES + "coldFluidOut", new TemperatureChannel((short) 530));
            mappings.put(CHANNEL_GROUP_SENSOR_VALUES + "externalHotWater", new TemperatureChannel((short) 531));

            // Control data
            mappings.put(CHANNEL_GROUP_CONTROL_DATA + "radiatorReturnTarget", new TemperatureChannel((short) 110));
            mappings.put(CHANNEL_GROUP_CONTROL_DATA + "radiatorReturnOn", new TemperatureChannel((short) 111));
            mappings.put(CHANNEL_GROUP_CONTROL_DATA + "radiatorReturnOff", new TemperatureChannel((short) 112));
            mappings.put(CHANNEL_GROUP_CONTROL_DATA + "hotWaterTarget", new TemperatureChannel((short) 43));
            mappings.put(CHANNEL_GROUP_CONTROL_DATA + "hotWaterOn", new TemperatureChannel((short) 115));
            mappings.put(CHANNEL_GROUP_CONTROL_DATA + "hotWaterOff", new TemperatureChannel((short) 116));
            mappings.put(CHANNEL_GROUP_CONTROL_DATA + "radiatorForwardTarget", new TemperatureChannel((short) 109));
            mappings.put(CHANNEL_GROUP_CONTROL_DATA + "addHeatPowerPercent", new TemperatureChannel((short) 108));

            // Device values
            mappings.put(CHANNEL_GROUP_DEVICE_VALUES + "coldFluidPump", new Int16Channel((short) 509));
            mappings.put(CHANNEL_GROUP_DEVICE_VALUES + "compressor", new Int16Channel((short) 510));
            mappings.put(CHANNEL_GROUP_DEVICE_VALUES + "additionalHeat3kW", new Int16Channel((short) 511));
            mappings.put(CHANNEL_GROUP_DEVICE_VALUES + "additionalHeat6kW", new Int16Channel((short) 512));
            mappings.put(CHANNEL_GROUP_DEVICE_VALUES + "radiatorPump", new Int16Channel((short) 515));
            mappings.put(CHANNEL_GROUP_DEVICE_VALUES + "heatFluidPump", new Int16Channel((short) 516));
            mappings.put(CHANNEL_GROUP_DEVICE_VALUES + "switchValve", new Int16Channel((short) 517));
            mappings.put(CHANNEL_GROUP_DEVICE_VALUES + "alarm", new Int16Channel((short) 518));

            // Settings
            mappings.put(CHANNEL_GROUP_SETTINGS + "heatCurve", new TemperatureChannel((short) 0));
            mappings.put(CHANNEL_GROUP_SETTINGS + "heatCurveFineAdj", new TemperatureChannel((short) 1));
            mappings.put(CHANNEL_GROUP_SETTINGS + "indoorTempSetting", new TemperatureChannel((short) 0x0021));
            mappings.put(CHANNEL_GROUP_SETTINGS + "curveInflByInTemp", new TemperatureChannel((short) 0x0022));
            mappings.put(CHANNEL_GROUP_SETTINGS + "adjCurveAt20", new TemperatureChannel((short) 0x001e));
            mappings.put(CHANNEL_GROUP_SETTINGS + "adjCurveAt15", new TemperatureChannel((short) 0x001c));
            mappings.put(CHANNEL_GROUP_SETTINGS + "adjCurveAt10", new TemperatureChannel((short) 0x001a));
            mappings.put(CHANNEL_GROUP_SETTINGS + "adjCurveAt5", new TemperatureChannel((short) 0x0018));
            mappings.put(CHANNEL_GROUP_SETTINGS + "adjCurveAt0", new TemperatureChannel((short) 0x0016));
            mappings.put(CHANNEL_GROUP_SETTINGS + "adjCurveAtMinus5", new TemperatureChannel((short) 0x0014));
            mappings.put(CHANNEL_GROUP_SETTINGS + "adjCurveAtMinus10", new TemperatureChannel((short) 0x0012));
            mappings.put(CHANNEL_GROUP_SETTINGS + "adjCurveAtMinus15", new TemperatureChannel((short) 0x0010));
            mappings.put(CHANNEL_GROUP_SETTINGS + "adjCurveAtMinus20", new TemperatureChannel((short) 0x000e));
            mappings.put(CHANNEL_GROUP_SETTINGS + "adjCurveAtMinus25", new TemperatureChannel((short) 0x000c));
            mappings.put(CHANNEL_GROUP_SETTINGS + "adjCurveAtMinus30", new TemperatureChannel((short) 0x000a));
            mappings.put(CHANNEL_GROUP_SETTINGS + "adjCurveAtMinus35", new TemperatureChannel((short) 0x0008));
            mappings.put(CHANNEL_GROUP_SETTINGS + "heatCurveCouplingDiff", new TemperatureChannel((short) 0x0002));
        }

        rego600 = new RegoRegisterMapper(mappings);
    }
}
