package org.openhab.binding.rf24.handler;

import java.math.BigDecimal;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.rf24.rf24BindingConstants;

import com.google.common.base.Preconditions;

import pl.grzeslowski.smarthome.common.io.id.HardwareId;
import pl.grzeslowski.smarthome.common.io.id.IdUtils;
import pl.grzeslowski.smarthome.common.io.id.ReceiverId;

public class HardwareIdFactory {
    private final IdUtils idUtils;

    public HardwareIdFactory(IdUtils idUtils) {
        this.idUtils = Preconditions.checkNotNull(idUtils);
    }

    public final HardwareId findHardwareId(Thing thing) {
        Configuration conf = thing.getConfiguration();
        if (conf.containsKey(rf24BindingConstants.RECIVER_PIPE_CONFIGURATION)) {
            Object pipe = thing.getConfiguration().get(rf24BindingConstants.RECIVER_PIPE_CONFIGURATION);
            ReceiverId receiverId = new ReceiverId(parseId(pipe));
            return HardwareId.fromReceiverId(idUtils, receiverId);
        } else {
            throw new RuntimeException("Thing does not have recive pipe in configuration!");
        }
    }

    private long parseId(Object object) {
        if (object instanceof BigDecimal) {
            BigDecimal bigDecimal = (BigDecimal) object;
            return bigDecimal.longValue();
        }

        if (object instanceof String) {
            String string = (String) object;
            return Long.valueOf(string);
        }

        throw new RuntimeException(String.format("I dont' know how to parse this class: %s!", object.getClass()));
    }
}
