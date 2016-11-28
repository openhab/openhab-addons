package org.openhab.binding.regoheatpump.internal.protocol;

import static org.openhab.binding.regoheatpump.RegoHeatPumpBindingConstants.CHANNEL_GROUP_REGISTERS;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.types.State;;

public class RegoRegisterMapper {
    private final Map<String, Channel> mappings;

    private RegoRegisterMapper(Map<String, Channel> mappings) {
        this.mappings = mappings;
    }

    public Channel map(String channelIID) {
        return mappings.get(channelIID);
    }

    public Set<String> channels() {
        return mappings.keySet();
    }

    public static abstract class Channel {
        private final short address;

        private Channel(short address) {
            this.address = address;
        }

        public short address() {
            return address;
        }

        public abstract State convert(Short value);
    }

    public static class TemperatureChannel extends Channel {

        private TemperatureChannel(short address) {
            super(address);
        }

        @Override
        public State convert(Short value) {
            return new DecimalType(ValueConverter.toDouble(value));
        }
    }

    public static class Int16Channel extends Channel {

        private Int16Channel(short address) {
            super(address);
        }

        @Override
        public State convert(Short value) {
            return new DecimalType(value);
        }
    }

    public static RegoRegisterMapper rego600() {
        final Map<String, Channel> mappings = new HashMap<String, Channel>();
        {
            // Sensor values
            mappings.put(CHANNEL_GROUP_REGISTERS + "radiatorReturnGT1", new TemperatureChannel((short) 521));
            mappings.put(CHANNEL_GROUP_REGISTERS + "outdoorGT2", new TemperatureChannel((short) 522));
            mappings.put(CHANNEL_GROUP_REGISTERS + "hotWaterGT3", new TemperatureChannel((short) 523));
            mappings.put(CHANNEL_GROUP_REGISTERS + "shuntGT4", new TemperatureChannel((short) 524));
            mappings.put(CHANNEL_GROUP_REGISTERS + "roomGT5", new TemperatureChannel((short) 525));
            mappings.put(CHANNEL_GROUP_REGISTERS + "compressorGT6", new TemperatureChannel((short) 526));
            mappings.put(CHANNEL_GROUP_REGISTERS + "heatFluidOutGT8", new TemperatureChannel((short) 527));
            mappings.put(CHANNEL_GROUP_REGISTERS + "heatFluidInGT9", new TemperatureChannel((short) 528));
            mappings.put(CHANNEL_GROUP_REGISTERS + "coldFluidInGT10", new TemperatureChannel((short) 529));
            mappings.put(CHANNEL_GROUP_REGISTERS + "coldFluidOutGT11", new TemperatureChannel((short) 530));
            mappings.put(CHANNEL_GROUP_REGISTERS + "externalHotWaterGT3x", new TemperatureChannel((short) 531));

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
