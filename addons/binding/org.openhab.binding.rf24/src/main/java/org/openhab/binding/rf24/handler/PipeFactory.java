package org.openhab.binding.rf24.handler;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.rf24.rf24BindingConstants;

import pl.grzeslowski.smarthome.rf24.helpers.Pipe;

public class PipeFactory {

    public final Pipe findPipe(Thing thing) {
        Configuration conf = thing.getConfiguration();
        if (conf.containsKey(rf24BindingConstants.RECIVER_PIPE_CONFIGURATION)) {
            String pipeId = (String) thing.getConfiguration().get(rf24BindingConstants.RECIVER_PIPE_CONFIGURATION);
            return parsePipe(pipeId);
        } else {
            throw new RuntimeException("Thing does not have recive pipe in configuration!");
        }
    }

    public Pipe parsePipe(String pipeId) {
        try {
            long pipe = Long.parseLong(pipeId);
            return new Pipe(pipe);
        } catch (NumberFormatException ex) {

            // this should be parse from string
            return new Pipe(pipeId);
        }
    }
}
