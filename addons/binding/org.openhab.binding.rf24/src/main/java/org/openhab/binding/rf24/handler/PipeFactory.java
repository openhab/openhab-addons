package org.openhab.binding.rf24.handler;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.rf24.rf24BindingConstants;

import com.google.common.base.Preconditions;

import pl.grzeslowski.smarthome.common.io.id.HardwareId;
import pl.grzeslowski.smarthome.common.io.id.IdUtils;
import pl.grzeslowski.smarthome.common.io.id.ReceiverId;
import pl.grzeslowski.smarthome.rf24.helpers.Pipe;

public class PipeFactory {

    private final IdUtils idUtils;

    public PipeFactory(IdUtils idUtils) {
        this.idUtils = Preconditions.checkNotNull(idUtils);
    }

    public final Pipe findPipe(Thing thing) {
        Configuration conf = thing.getConfiguration();
        if (conf.containsKey(rf24BindingConstants.RECIVER_PIPE_CONFIGURATION)) {
            String pipeId = (String) thing.getConfiguration().get(rf24BindingConstants.RECIVER_PIPE_CONFIGURATION);
            return new Pipe(parsePipe(pipeId).getId());
        } else {
            throw new RuntimeException("Thing does not have recive pipe in configuration!");
        }
    }

    public HardwareId parsePipe(String pipeId) {
        try {
            long pipe = Long.parseLong(pipeId);
            ReceiverId receiverId = new ReceiverId(pipe);
            return HardwareId.fromReceiverId(idUtils, receiverId);
        } catch (NumberFormatException ex) {

            // this should be parse from string
            return new HardwareId(new Pipe(pipeId).getPipe() + HardwareId.START_ID);
        }
    }
}
