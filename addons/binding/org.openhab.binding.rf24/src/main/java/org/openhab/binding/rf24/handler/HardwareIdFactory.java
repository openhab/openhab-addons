package org.openhab.binding.rf24.handler;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.rf24.rf24BindingConstants;

import pl.grzeslowski.smarthome.common.io.id.HardwareId;

public class HardwareIdFactory {
    public final HardwareId findHardwareId(Thing thing) {
        Configuration conf = thing.getConfiguration();
        if (conf.containsKey(rf24BindingConstants.RECIVER_PIPE_CONFIGURATION)) {
            String pipeId = (String) thing.getConfiguration().get(rf24BindingConstants.RECIVER_PIPE_CONFIGURATION);
            long id = Long.parseLong(pipeId);
            return new HardwareId(id);
        } else {
            throw new RuntimeException("Thing does not have recive pipe in configuration!");
        }
    }
}
