/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.fronius.handler;

import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.fronius.FroniusBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The Handler for the Active Device Information.
 *
 * @author Gerrit Beine - Initial contribution
 */
public class FroniusActiveDeviceInfoHandler extends FroniusBaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(FroniusActiveDeviceInfoHandler.class);
    private final JsonParser parser = new JsonParser();

    private String url;

    public FroniusActiveDeviceInfoHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected String getServiceDescription() {
        return FroniusBindingConstants.ACTIVE_DEVICE_INFO_DESCRIPTION;
    }

    @Override
    protected String getServiceUrl() {
        if (null == url) {
            StringBuilder sb = new StringBuilder();
            sb.append("http://");
            sb.append(getHostname());
            sb.append(FroniusBindingConstants.ACTIVE_DEVICE_INFO_URL);
            sb.append("?DeviceClass=System");
            url = sb.toString();
        }

        logger.debug("{} URL: {}", getServiceDescription(), url);

        return url;
    }

    @Override
    protected void updateData(String data) {
        logger.debug("Refresh data {}", data);
        JsonObject json = parser.parse(data).getAsJsonObject();

    }

}
