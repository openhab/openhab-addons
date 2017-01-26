/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wink.handler;

import static org.openhab.binding.wink.WinkBindingConstants.*;

import java.io.IOException;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.openhab.binding.wink.config.WinkDeviceConfig;
import org.openhab.binding.wink.handler.WinkHub2Handler.RequestCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link WinkHandler} provides a base implementation for a Wink device, it
 * contains all the common logic and declares some abstract method that should
 * be implemented by every Wink device.
 *
 * @author Sebastien Marchand - Initial contribution
 */
public abstract class WinkHandler extends BaseThingHandler {
    /**
     * Base configuration of this device.
     */
    protected WinkDeviceConfig deviceConfig;

    /**
     * Logger for this class. Can be used by the derived classes.
     */
    protected Logger logger = LoggerFactory.getLogger(WinkHandler.class);

    /**
     * Creates a new instance of this class for the {@link Thing}.
     *
     * @param thing the thing that should be handled, not null.
     */
    public WinkHandler(Thing thing) {
        super(thing);
        String config = (String) getThing().getConfiguration().get(WINK_DEVICE_CONFIG);
        String id = (String) getThing().getConfiguration().get(WINK_DEVICE_ID);
        this.deviceConfig = new WinkDeviceConfig(id);
        parseConfig(config);
    }

    public void parseConfig(String jsonConfigString) {
        JsonParser parser = new JsonParser();
        deviceConfig.readConfigFromJson(parser.parse(jsonConfigString).getAsJsonObject());
    }

    /**
     * Used to retrieve the {@link WinkHub2Handler} controlling this thing.
     *
     * @return this thing hub handler, null if it hasn't been set yet.
     */
    protected WinkHub2Handler getHubHandler() {
        Bridge hub = getBridge();
        return hub == null ? null : (WinkHub2Handler) hub.getHandler();
    }

    public void HandleCommunicationError(String errorMessage) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, errorMessage);
    }

    /**
     * Abstract method that should return the device path that get appended to the
     * Wink server URL when making a request.
     *
     * It usually has the form "{device_type}/{device_id}.
     *
     * @return this thing REST request path.
     */
    protected abstract String getDeviceRequestPath();

    private abstract class WinkDeviceRequestCallback implements RequestCallback {
        /**
         * Handler of the thing that should for which the configuration should be set.
         */
        protected WinkHandler handler;

        /**
         * Creates a new instance of this class.
         *
         * @param handler The handler for which the configuration should be read.
         */
        public WinkDeviceRequestCallback(WinkHandler handler) {
            Preconditions.checkArgument(handler != null, "The argument 'handler' must not be null.");
            this.handler = handler;
        }

        @Override
        public void OnError(String error) {
            handler.HandleCommunicationError(error);
        }
    }

    //////////// Read State functions ////////////

    /**
     * Specialization of a {link RequestCallback} to read a device configuration.
     *
     * @author Sebastien Marchand
     */
    private class ReadDeviceStateCallback extends WinkDeviceRequestCallback {
        public ReadDeviceStateCallback(WinkHandler handler) {
            super(handler);
        }

        @Override
        public void parseRequestResult(JsonObject jsonResult) {
            // The response from the server is a JSON object containing the device information and state.
            handler.updateDeviceStateCallback(jsonResult.get("data").getAsJsonObject());
        }
    }

    protected void ReadDeviceState() {
        try {
            getHubHandler().sendRequestToServer(getDeviceRequestPath(), new ReadDeviceStateCallback(this));
        } catch (IOException e) {
            logger.error("Error while querying the hub for " + getDeviceRequestPath(), e);
        }
    }

    public void updateDeviceState(String jsonDataBlob) {
        JsonParser parser = new JsonParser();
        updateDeviceStateCallback(parser.parse(jsonDataBlob).getAsJsonObject());
    }

    abstract public void updateDeviceStateCallback(JsonObject jsonDataBlob);

    /////////////////////////////////////////////////

    //////////// Send commands functions ////////////

    private class SendCommandCallback extends WinkDeviceRequestCallback {
        public SendCommandCallback(WinkHandler handler) {
            super(handler);
        }

        @Override
        public void parseRequestResult(JsonObject jsonResult) {
            handler.sendCommandCallback(jsonResult);
        }
    }

    public void sendCommand(String payLoad) {
        try {
            getHubHandler().sendRequestToServer(getDeviceRequestPath() + "/desired_state",
                    new SendCommandCallback(this), payLoad);
        } catch (IOException e) {
            logger.error("Error while querying the hub for " + getDeviceRequestPath(), e);
        }
    }

    abstract public void sendCommandCallback(JsonObject jsonResult);

    /////////////////////////////////////////////////
}
