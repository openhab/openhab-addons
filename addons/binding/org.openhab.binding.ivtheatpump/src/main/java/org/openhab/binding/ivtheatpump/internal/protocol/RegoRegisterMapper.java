package org.openhab.binding.ivtheatpump.internal.protocol;

import static org.openhab.binding.ivtheatpump.IVTHeatPumpBindingConstants.CHANNEL_GROUP_SENSORS;

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

    public static RegoRegisterMapper rego600() {
        final Map<String, Channel> mappings = new HashMap<String, Channel>();
        {
            mappings.put(CHANNEL_GROUP_SENSORS + "radiatorReturnGT1", new TemperatureChannel((short) 521));
            mappings.put(CHANNEL_GROUP_SENSORS + "outdoorGT2", new TemperatureChannel((short) 522));
            mappings.put(CHANNEL_GROUP_SENSORS + "hotWaterGT3", new TemperatureChannel((short) 523));
            mappings.put(CHANNEL_GROUP_SENSORS + "shuntGT4", new TemperatureChannel((short) 524));
            mappings.put(CHANNEL_GROUP_SENSORS + "roomGT5", new TemperatureChannel((short) 525));
            mappings.put(CHANNEL_GROUP_SENSORS + "compressorGT6", new TemperatureChannel((short) 526));
            mappings.put(CHANNEL_GROUP_SENSORS + "heatFluidOutGT8", new TemperatureChannel((short) 527));
            mappings.put(CHANNEL_GROUP_SENSORS + "heatFluidInGT9", new TemperatureChannel((short) 528));
            mappings.put(CHANNEL_GROUP_SENSORS + "coldFluidInGT10", new TemperatureChannel((short) 529));
            mappings.put(CHANNEL_GROUP_SENSORS + "coldFluidOutGT11", new TemperatureChannel((short) 530));
            // mappings.put("GT3x External hot water", new Channel((short) 531));

            /*
             * ['GT3x External hot water', 531, TYPE_TEMP10], \
             * ['GT1 Target value', 110, TYPE_TEMP10], \
             * ['GT1 On value', 111, TYPE_TEMP10], \
             * ['GT1 Off value', 112, TYPE_TEMP10], \
             * ['GT3 Target value', 43, TYPE_TEMP10], \
             * ['GT3 On value', 115, TYPE_TEMP10], \
             * ['GT3 Off value', 116, TYPE_TEMP10], \
             * ['P3 Cold fluid pump', 509, TYPE_INT16], \
             * ['Compressor', 510, TYPE_INT16], \
             * ['Additional heat 1', 511, TYPE_INT16], \
             * ['Additional heat 2', 512, TYPE_INT16], \
             * ['P1 Radiator pump', 515, TYPE_INT16], \
             * ['P2 Heat fluid pump', 516, TYPE_INT16], \
             * ['Three-way valve', 517, TYPE_INT16], \
             * ['Alarm', 518, TYPE_INT16] \
             */
        }

        return new RegoRegisterMapper(mappings);
    }
}
