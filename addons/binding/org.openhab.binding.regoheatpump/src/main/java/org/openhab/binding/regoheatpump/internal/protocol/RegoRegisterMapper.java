package org.openhab.binding.regoheatpump.internal.protocol;

import static org.openhab.binding.regoheatpump.RegoHeatPumpBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.types.State;;

public class RegoRegisterMapper {
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

    public static RegoRegisterMapper rego600() {
        final Map<String, Channel> mappings = new HashMap<String, Channel>();
        {
            // Sensor values
            mappings.put(CHANNEL_GROUP_SENSORS + "radiatorReturn", new TemperatureChannel((short) 521));
            mappings.put(CHANNEL_GROUP_SENSORS + "outdoor", new TemperatureChannel((short) 522));
            mappings.put(CHANNEL_GROUP_SENSORS + "hotWater", new TemperatureChannel((short) 523));
            mappings.put(CHANNEL_GROUP_SENSORS + "radiatorForward", new TemperatureChannel((short) 524));
            mappings.put(CHANNEL_GROUP_SENSORS + "indoor", new TemperatureChannel((short) 525));
            mappings.put(CHANNEL_GROUP_SENSORS + "compressor", new TemperatureChannel((short) 526));
            mappings.put(CHANNEL_GROUP_SENSORS + "heatFluidOut", new TemperatureChannel((short) 527));
            mappings.put(CHANNEL_GROUP_SENSORS + "heatFluidIn", new TemperatureChannel((short) 528));
            mappings.put(CHANNEL_GROUP_SENSORS + "coldFluidIn", new TemperatureChannel((short) 529));
            mappings.put(CHANNEL_GROUP_SENSORS + "coldFluidOut", new TemperatureChannel((short) 530));
            mappings.put(CHANNEL_GROUP_SENSORS + "externalHotWater", new TemperatureChannel((short) 531));

            // Control data
            mappings.put(CHANNEL_GROUP_REGISTERS + "targetValueGT1", new TemperatureChannel((short) 110));
            mappings.put(CHANNEL_GROUP_REGISTERS + "onValueGT1", new TemperatureChannel((short) 111));
            mappings.put(CHANNEL_GROUP_REGISTERS + "offValueGT1", new TemperatureChannel((short) 112));
            mappings.put(CHANNEL_GROUP_REGISTERS + "targetValueGT3", new TemperatureChannel((short) 43));
            mappings.put(CHANNEL_GROUP_REGISTERS + "onValueGT3", new TemperatureChannel((short) 115));
            mappings.put(CHANNEL_GROUP_REGISTERS + "offValueGT3", new TemperatureChannel((short) 116));
            mappings.put(CHANNEL_GROUP_REGISTERS + "targetValueGT4", new TemperatureChannel((short) 109));
            mappings.put(CHANNEL_GROUP_REGISTERS + "addHeatPower", new TemperatureChannel((short) 108));

            // Device values
            mappings.put(CHANNEL_GROUP_REGISTERS + "coldFluidPumpP3", new Int16Channel((short) 509));
            mappings.put(CHANNEL_GROUP_REGISTERS + "compressor", new Int16Channel((short) 510));
            mappings.put(CHANNEL_GROUP_REGISTERS + "additionalHeat3kW", new Int16Channel((short) 511));
            mappings.put(CHANNEL_GROUP_REGISTERS + "additionalHeat6kW", new Int16Channel((short) 512));
            mappings.put(CHANNEL_GROUP_REGISTERS + "radiatorPumpP1", new Int16Channel((short) 515));
            mappings.put(CHANNEL_GROUP_REGISTERS + "heatFluidPumpP2", new Int16Channel((short) 516));
            mappings.put(CHANNEL_GROUP_REGISTERS + "threeWayValve", new Int16Channel((short) 517));
            mappings.put(CHANNEL_GROUP_REGISTERS + "alarm", new Int16Channel((short) 518));

            // Settings
            mappings.put(CHANNEL_GROUP_REGISTERS + "heatCurve", new TemperatureChannel((short) 0));
            mappings.put(CHANNEL_GROUP_REGISTERS + "heatCurveFineAdj", new TemperatureChannel((short) 1));
            mappings.put(CHANNEL_GROUP_REGISTERS + "indoorTempSetting", new TemperatureChannel((short) 0x0021));
            mappings.put(CHANNEL_GROUP_REGISTERS + "curveInflByInTemp", new TemperatureChannel((short) 0x0022));
            mappings.put(CHANNEL_GROUP_REGISTERS + "adjCurveAt20", new TemperatureChannel((short) 0x001e));
            mappings.put(CHANNEL_GROUP_REGISTERS + "adjCurveAt15", new TemperatureChannel((short) 0x001c));
            mappings.put(CHANNEL_GROUP_REGISTERS + "adjCurveAt10", new TemperatureChannel((short) 0x001a));
            mappings.put(CHANNEL_GROUP_REGISTERS + "adjCurveAt5", new TemperatureChannel((short) 0x0018));
            mappings.put(CHANNEL_GROUP_REGISTERS + "adjCurveAt0", new TemperatureChannel((short) 0x0016));
            mappings.put(CHANNEL_GROUP_REGISTERS + "adjCurveAtMinus5", new TemperatureChannel((short) 0x0014));
            mappings.put(CHANNEL_GROUP_REGISTERS + "adjCurveAtMinus10", new TemperatureChannel((short) 0x0012));
            mappings.put(CHANNEL_GROUP_REGISTERS + "adjCurveAtMinus15", new TemperatureChannel((short) 0x0010));
            mappings.put(CHANNEL_GROUP_REGISTERS + "adjCurveAtMinus20", new TemperatureChannel((short) 0x000e));
            mappings.put(CHANNEL_GROUP_REGISTERS + "adjCurveAtMinus25", new TemperatureChannel((short) 0x000c));
            mappings.put(CHANNEL_GROUP_REGISTERS + "adjCurveAtMinus30", new TemperatureChannel((short) 0x000a));
            mappings.put(CHANNEL_GROUP_REGISTERS + "adjCurveAtMinus35", new TemperatureChannel((short) 0x0008));
            mappings.put(CHANNEL_GROUP_REGISTERS + "heatCurveCouplingDiff", new TemperatureChannel((short) 0x0002));
        }

        return new RegoRegisterMapper(mappings);
    }
}
