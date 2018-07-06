/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.opendaikin.internal;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.openhab.binding.opendaikin.internal.api.ControlInfo;
import org.openhab.binding.opendaikin.internal.api.SensorInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles performing the actual HTTP requests for communicating with Daiking air conditioning units.
 *
 * @author Tim Waterhouse - Initial Contribution
 *
 */
public class OpenDaikinWebTargets {

    private WebTarget base;
    private WebTarget setControlInfo;
    private WebTarget getControlInfo;
    private WebTarget getSensorInfo;
    private Logger logger = LoggerFactory.getLogger(OpenDaikinWebTargets.class);

    public OpenDaikinWebTargets(Client client, String ipAddress) {
        base = client.target("http://" + ipAddress);
        setControlInfo = base.path("aircon/set_control_info");
        getControlInfo = base.path("aircon/get_control_info");
        getSensorInfo = base.path("aircon/get_sensor_info");
    }

    public ControlInfo getControlInfo() {
        String response = invoke(getControlInfo.request().buildGet(), getControlInfo);
        return ControlInfo.parse(response);
    }

    public void setControlInfo(ControlInfo info) {
        WebTarget target = info.getParamString(setControlInfo);
        logger.debug("Calling this url: {}", target.getUri().toString());
        invoke(target.request().buildGet(), target);
    }

    public SensorInfo getSensorInfo() {
        String response = invoke(getSensorInfo.request().buildGet(), getSensorInfo);
        return SensorInfo.parse(response);
    }

    private String invoke(Invocation invocation, WebTarget target) {
        Response response;
        synchronized (this) {
            response = invocation.invoke();
        }

        if (response.getStatus() != 200) {
            logger.error("Bridge returned {} while invoking {} : {}", response.getStatus(), target.getUri(),
                    response.readEntity(String.class));
            return null;
        } else if (!response.hasEntity()) {
            logger.error("Bridge returned null response while invoking {}", target.getUri());
            return null;
        }

        return response.readEntity(String.class);
    }
}
