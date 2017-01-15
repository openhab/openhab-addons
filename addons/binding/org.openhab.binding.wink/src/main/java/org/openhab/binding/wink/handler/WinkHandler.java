/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wink.handler;

import static org.openhab.binding.wink.WinkBindingConstants.WINK_DEVICE_ID;

import java.io.IOException;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.openhab.binding.wink.config.WinkDeviceConfig;
import org.openhab.binding.wink.handler.WinkHub2Handler.RequestCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link WinkHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Sebastien Marchand - Initial contribution
 */
public abstract class WinkHandler extends BaseThingHandler {
    protected WinkDeviceConfig deviceConfig;
    protected Logger logger = LoggerFactory.getLogger(WinkHandler.class);

    public WinkHandler(Thing thing) {
        super(thing);
        String id = (String) getThing().getConfiguration().get(WINK_DEVICE_ID);
        this.deviceConfig = new WinkDeviceConfig(id);
    }

    protected WinkHub2Handler getHubHandler() {
        Bridge hub = getBridge();

        return hub == null ? null : (WinkHub2Handler) hub.getHandler();
    }

    protected abstract String getDeviceRequestPath();

    private class ReadConfigCallback implements RequestCallback {
        private WinkHandler handler;

        public ReadConfigCallback(WinkHandler handler) {
            this.handler = handler;
        }

        @Override
        public void parseRequestResult(String jsonResult) {
            JsonParser parser = new JsonParser();
            handler.parseConfig(parser.parse(jsonResult).getAsJsonObject());
        }
    }

    public void readConfig() {
        try {
            getHubHandler().getConfigFromServer(getDeviceRequestPath(), new ReadConfigCallback(this));
        } catch (IOException e) {
            logger.error("Error while querying the hub for " + getDeviceRequestPath(), e);
        }
    }

    public void parseConfig(JsonObject jsonConfig) {
        deviceConfig.readConfigFromJson(jsonConfig);
    }
}
