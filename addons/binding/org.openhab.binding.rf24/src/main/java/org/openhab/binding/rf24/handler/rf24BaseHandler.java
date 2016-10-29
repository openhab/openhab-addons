package org.openhab.binding.rf24.handler;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.openhab.binding.rf24.rf24BindingConstants;
import org.openhab.binding.rf24.wifi.StubWiFi;
import org.openhab.binding.rf24.wifi.WiFi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.grzeslowski.smarthome.rpi.wifi.help.Pipe;

public abstract class rf24BaseHandler extends BaseThingHandler {
    private Logger logger = LoggerFactory.getLogger(rf24BaseHandler.class);
    protected final WiFi wifi;
    protected final Pipe pipe;

    public rf24BaseHandler(Thing thing) {
        super(thing);
        Configuration conf = thing.getConfiguration();
        if (conf.containsKey(rf24BindingConstants.RECIVER_PIPE_CONFIGURATION)) {
            pipe = (Pipe) thing.getConfiguration().get(rf24BindingConstants.RECIVER_PIPE_CONFIGURATION);
        } else {
            throw new RuntimeException("Thing does not have recive pipe in configuration!");
        }
        wifi = new StubWiFi();
    }

    @Override
    public void initialize() {
        super.initialize();
        wifi.init();
    }

    @Override
    public void dispose() {
        wifi.close();
    }
}
