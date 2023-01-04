/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.regoheatpump.internal.rego6xx;

import static org.openhab.binding.regoheatpump.internal.RegoHeatPumpBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;

/**
 * The {@link RegoRegisterMapper} is responsible for mapping rego 6xx registers into channels.
 *
 * @author Boris Krivonog - Initial contribution
 */
@NonNullByDefault
public class RegoRegisterMapper {
    public static final RegoRegisterMapper REGO600;

    public static interface Channel {
        public short address();

        public double scaleFactor();

        public @Nullable Unit<?> unit();

        public int convertValue(short value);
    }

    private static class ChannelFactory {
        private static class ChannelImpl implements Channel {
            private final short address;
            private final double scaleFactor;
            private @Nullable final Unit<?> unit;

            private ChannelImpl(short address, double scaleFactor, @Nullable Unit<?> unit) {
                this.address = address;
                this.scaleFactor = scaleFactor;
                this.unit = unit;
            }

            @Override
            public short address() {
                return address;
            }

            @Override
            public double scaleFactor() {
                return scaleFactor;
            }

            @Override
            public @Nullable Unit<?> unit() {
                return unit;
            }

            @Override
            public int convertValue(short value) {
                return value;
            }
        }

        private ChannelFactory() {
        }

        static Channel temperature(short address) {
            return new ChannelImpl(address, 0.1, SIUnits.CELSIUS);
        }

        static Channel hours(short address) {
            return new ChannelImpl(address, 1, Units.HOUR) {
                @Override
                public int convertValue(short value) {
                    return Short.toUnsignedInt(value);
                }
            };
        }

        static Channel percent(short address) {
            return new ChannelImpl(address, 0.1, Units.PERCENT);
        }

        static Channel unitless(short address, double scaleFactor) {
            return new ChannelImpl(address, scaleFactor, null);
        }
    }

    private final Map<String, Channel> mappings;

    private RegoRegisterMapper(Map<String, Channel> mappings) {
        this.mappings = mappings;
    }

    public @Nullable Channel map(String channelIID) {
        return mappings.get(channelIID);
    }

    static {
        final Map<String, Channel> mappings = new HashMap<>();
        {
            // Sensor values
            mappings.put(CHANNEL_GROUP_SENSOR_VALUES + "radiatorReturn", ChannelFactory.temperature((short) 521));
            mappings.put(CHANNEL_GROUP_SENSOR_VALUES + "outdoor", ChannelFactory.temperature((short) 522));
            mappings.put(CHANNEL_GROUP_SENSOR_VALUES + "hotWater", ChannelFactory.temperature((short) 523));
            mappings.put(CHANNEL_GROUP_SENSOR_VALUES + "radiatorForward", ChannelFactory.temperature((short) 524));
            mappings.put(CHANNEL_GROUP_SENSOR_VALUES + "indoor", ChannelFactory.temperature((short) 525));
            mappings.put(CHANNEL_GROUP_SENSOR_VALUES + "compressor", ChannelFactory.temperature((short) 526));
            mappings.put(CHANNEL_GROUP_SENSOR_VALUES + "heatFluidOut", ChannelFactory.temperature((short) 527));
            mappings.put(CHANNEL_GROUP_SENSOR_VALUES + "heatFluidIn", ChannelFactory.temperature((short) 528));
            mappings.put(CHANNEL_GROUP_SENSOR_VALUES + "coldFluidIn", ChannelFactory.temperature((short) 529));
            mappings.put(CHANNEL_GROUP_SENSOR_VALUES + "coldFluidOut", ChannelFactory.temperature((short) 530));
            mappings.put(CHANNEL_GROUP_SENSOR_VALUES + "externalHotWater", ChannelFactory.temperature((short) 531));

            // Control data
            mappings.put(CHANNEL_GROUP_CONTROL_DATA + "radiatorReturnTarget", ChannelFactory.temperature((short) 110));
            mappings.put(CHANNEL_GROUP_CONTROL_DATA + "radiatorReturnOn", ChannelFactory.temperature((short) 111));
            mappings.put(CHANNEL_GROUP_CONTROL_DATA + "radiatorReturnOff", ChannelFactory.temperature((short) 112));
            mappings.put(CHANNEL_GROUP_CONTROL_DATA + "hotWaterOn", ChannelFactory.temperature((short) 115));
            mappings.put(CHANNEL_GROUP_CONTROL_DATA + "hotWaterOff", ChannelFactory.temperature((short) 116));
            mappings.put(CHANNEL_GROUP_CONTROL_DATA + "radiatorForwardTarget", ChannelFactory.temperature((short) 109));
            mappings.put(CHANNEL_GROUP_CONTROL_DATA + "addHeatPowerPercent", ChannelFactory.percent((short) 108));

            // Device values
            mappings.put(CHANNEL_GROUP_DEVICE_VALUES + "coldFluidPump", ChannelFactory.unitless((short) 509, 1));
            mappings.put(CHANNEL_GROUP_DEVICE_VALUES + "compressor", ChannelFactory.unitless((short) 510, 1));
            mappings.put(CHANNEL_GROUP_DEVICE_VALUES + "additionalHeat3kW", ChannelFactory.unitless((short) 511, 1));
            mappings.put(CHANNEL_GROUP_DEVICE_VALUES + "additionalHeat6kW", ChannelFactory.unitless((short) 512, 1));
            mappings.put(CHANNEL_GROUP_DEVICE_VALUES + "radiatorPump", ChannelFactory.unitless((short) 515, 1));
            mappings.put(CHANNEL_GROUP_DEVICE_VALUES + "heatFluidPump", ChannelFactory.unitless((short) 516, 1));
            mappings.put(CHANNEL_GROUP_DEVICE_VALUES + "switchValve", ChannelFactory.unitless((short) 517, 1));
            mappings.put(CHANNEL_GROUP_DEVICE_VALUES + "alarm", ChannelFactory.unitless((short) 518, 1));

            // Settings
            mappings.put(CHANNEL_GROUP_SETTINGS + "heatCurve", ChannelFactory.unitless((short) 0x0000, 0.1));
            mappings.put(CHANNEL_GROUP_SETTINGS + "heatCurveFineAdj", ChannelFactory.temperature((short) 0x0001));
            mappings.put(CHANNEL_GROUP_SETTINGS + "heatCurveCouplingDiff", ChannelFactory.temperature((short) 0x0002));
            mappings.put(CHANNEL_GROUP_SETTINGS + "heatCurve2", ChannelFactory.unitless((short) 0x0003, 0.1));
            mappings.put(CHANNEL_GROUP_SETTINGS + "heatCurve2FineAdj", ChannelFactory.temperature((short) 0x0004));
            mappings.put(CHANNEL_GROUP_SETTINGS + "adjCurveAt20", ChannelFactory.temperature((short) 0x001e));
            mappings.put(CHANNEL_GROUP_SETTINGS + "adjCurveAt15", ChannelFactory.temperature((short) 0x001c));
            mappings.put(CHANNEL_GROUP_SETTINGS + "adjCurveAt10", ChannelFactory.temperature((short) 0x001a));
            mappings.put(CHANNEL_GROUP_SETTINGS + "adjCurveAt5", ChannelFactory.temperature((short) 0x0018));
            mappings.put(CHANNEL_GROUP_SETTINGS + "adjCurveAt0", ChannelFactory.temperature((short) 0x0016));
            mappings.put(CHANNEL_GROUP_SETTINGS + "adjCurveAtMinus5", ChannelFactory.temperature((short) 0x0014));
            mappings.put(CHANNEL_GROUP_SETTINGS + "adjCurveAtMinus10", ChannelFactory.temperature((short) 0x0012));
            mappings.put(CHANNEL_GROUP_SETTINGS + "adjCurveAtMinus15", ChannelFactory.temperature((short) 0x0010));
            mappings.put(CHANNEL_GROUP_SETTINGS + "adjCurveAtMinus20", ChannelFactory.temperature((short) 0x000e));
            mappings.put(CHANNEL_GROUP_SETTINGS + "adjCurveAtMinus25", ChannelFactory.temperature((short) 0x000c));
            mappings.put(CHANNEL_GROUP_SETTINGS + "adjCurveAtMinus30", ChannelFactory.temperature((short) 0x000a));
            mappings.put(CHANNEL_GROUP_SETTINGS + "adjCurveAtMinus35", ChannelFactory.temperature((short) 0x0008));
            mappings.put(CHANNEL_GROUP_SETTINGS + "indoorTempSetting", ChannelFactory.temperature((short) 0x0021));
            mappings.put(CHANNEL_GROUP_SETTINGS + "curveInflByInTemp", ChannelFactory.unitless((short) 0x0022, 0.1));
            mappings.put(CHANNEL_GROUP_SETTINGS + "summerDisconnection", ChannelFactory.temperature((short) 0x0024));
            mappings.put(CHANNEL_GROUP_SETTINGS + "hotWaterTarget", ChannelFactory.temperature((short) 0x002b));
            mappings.put(CHANNEL_GROUP_SETTINGS + "hotWaterTargetHysteresis",
                    ChannelFactory.temperature((short) 0x002c));

            // Operating times
            mappings.put(CHANNEL_GROUP_OPERATING_TIMES + "heatPumpInOperationRAD", ChannelFactory.hours((short) 72));
            mappings.put(CHANNEL_GROUP_OPERATING_TIMES + "heatPumpInOperationDHW", ChannelFactory.hours((short) 74));
            mappings.put(CHANNEL_GROUP_OPERATING_TIMES + "addHeatInOperationRAD", ChannelFactory.hours((short) 76));
            mappings.put(CHANNEL_GROUP_OPERATING_TIMES + "addHeatInOperationDHW", ChannelFactory.hours((short) 78));
        }

        REGO600 = new RegoRegisterMapper(mappings);
    }
}
