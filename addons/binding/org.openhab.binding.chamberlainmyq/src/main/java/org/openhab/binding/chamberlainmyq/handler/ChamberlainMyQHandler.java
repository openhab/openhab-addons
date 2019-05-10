/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.chamberlainmyq.handler;

import static org.openhab.binding.chamberlainmyq.ChamberlainMyQBindingConstants.MYQ_ID;

import java.io.IOException;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.openhab.binding.chamberlainmyq.config.ChamberlainMyQDeviceConfig;
import org.openhab.binding.chamberlainmyq.handler.ChamberlainMyQGatewayHandler.RequestCallback;
import org.openhab.binding.chamberlainmyq.internal.json.Device;
import org.openhab.binding.chamberlainmyq.internal.json.MyqJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ChamberlainMyQHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Scott Hanson - Initial contribution
 */
public abstract class ChamberlainMyQHandler extends BaseThingHandler {
    /**
     * Base configuration of this device.
     */
    protected ChamberlainMyQDeviceConfig deviceConfig;

    private Logger logger = LoggerFactory.getLogger(ChamberlainMyQHandler.class);

    /**
     * Creates a new instance of this class for the {@link Thing}.
     *
     * @param thing the thing that should be handled, not null.
     */
    public ChamberlainMyQHandler(Thing thing) {
        super(thing);

        String id = getThing().getProperties().get(MYQ_ID);
        logger.debug("Thing ID: {}", id);
        this.deviceConfig = new ChamberlainMyQDeviceConfig(getThing().getProperties());
        logger.info("Initializing a MyQ device: \n{}", deviceConfig.asString());
    }

    /**
     * Used to retrieve the {@link ChamberlainMyQGatewayHandler} controlling this thing.
     *
     * @return this thing gateway handler, null if it hasn't been set yet.
     */
    protected ChamberlainMyQGatewayHandler getGatewayHandler() {
        Bridge gateway = getBridge();
        return gateway == null ? null : (ChamberlainMyQGatewayHandler) gateway.getHandler();
    }

    /**
     * Function called when a communication error between the gateway and the thing has been detected.
     */
    protected void handleCommunicationError(String errorMessage) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, errorMessage);
    }

    private abstract class ChamberlainMyQDeviceRequestCallback implements RequestCallback {
        /**
         * Handler of the thing that should for which the configuration should be set.
         */
        protected ChamberlainMyQHandler handler;

        /**
         * Creates a new instance of this class.
         *
         * @param handler The handler for which the configuration should be read.
         */
        public ChamberlainMyQDeviceRequestCallback(ChamberlainMyQHandler handler) {
            if (handler != null) {
                this.handler = handler;
            }
        }

        @Override
        public void onError(String error) {
            handler.handleCommunicationError(error);
        }
    }

    //////////// Read State functions ////////////

    /**
     * Specialization of a {link RequestCallback} to read a device configuration.
     *
     * @author Scott Hanson
     */
    private class ReadDeviceStateCallback extends ChamberlainMyQDeviceRequestCallback {
        /**
         * Creates a new instance of this class.
         *
         * @param handler The handler for which the state should be read.
         */
        public ReadDeviceStateCallback(ChamberlainMyQHandler handler) {
            super(handler);
        }

        @Override
        public void parseRequestResult(MyqJson jsonResult) {
            logger.trace("Parsing a ReadDeviceState request result: {}", jsonResult);
            // The response from the server is a JSON object containing the device information and state.
            handler.updateDeviceStateCallback(jsonResult);
        }
    }

    /**
     * Query the {@link MyQGatewayHandler} for this device's state.
     */
    protected void readDeviceState() {
        try {
            getGatewayHandler().sendRequestToServer(new ReadDeviceStateCallback(this));
        } catch (IOException e) {
            logger.warn("Error while querying the myQ Service for {}", e);
        }
    }

    /**
     * Callback called once the device state has been updated.
     *
     * @param jsonDataBlob the reply from the gateway.
     */
    protected void updateDeviceStateCallback(MyqJson myqDevices) {
        String deviceID = getThing().getProperties().get(MYQ_ID);

        for (Device myqdevice : myqDevices.getDevices()) {
            String findDeviceId = myqdevice.getMyQDeviceId().toString();

            if (deviceID.compareTo(findDeviceId) == 0) {
                ChamberlainMyQHandler test = (ChamberlainMyQHandler) thing.getHandler();
                if (test != null) {
                    test.updateState(myqdevice);
                }
            }
        }
    }

    protected abstract void updateState(Device myqDevice);

}
