package org.openhab.binding.ivtheatpump.internal.protocol;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RegoMapper {

    public Channel map(String channelIID) {
        return mappings.get(channelIID);
    }

    public Set<String> channels() {
        return mappings.keySet();
    }

    public static class Channel {
        private final short address;

        private Channel(short address) {
            this.address = address;
        }

        public short address() {
            return address;
        }
    }

    private final Map<String, Channel> mappings = new HashMap<String, Channel>();
    {
        mappings.put("radiatorReturnGT1", new Channel((short) 521));
        mappings.put("outdoorGT2", new Channel((short) 522));
        mappings.put("hotWaterGT3", new Channel((short) 523));
        mappings.put("shuntGT4", new Channel((short) 524));
        mappings.put("roomGT5", new Channel((short) 525));
        mappings.put("compressorGT6", new Channel((short) 526));
        mappings.put("heatFluidOutGT8", new Channel((short) 527));
        mappings.put("heatFluidInGT9", new Channel((short) 528));
        mappings.put("coldFluidInGT10", new Channel((short) 529));
        mappings.put("coldFluidOutGT11", new Channel((short) 530));

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
    };
}
