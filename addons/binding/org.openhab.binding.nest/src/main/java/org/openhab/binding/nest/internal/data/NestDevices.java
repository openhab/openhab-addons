/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.internal.data;

import java.util.Map;

/**
 * All the Nest devices broken up by type.
 *
 * @author David Bennett - Initial Contribution
 */
public class NestDevices {

    private Map<String, Thermostat> thermostats;
    private Map<String, SmokeDetector> smokeCoAlarms;
    private Map<String, Camera> cameras;

    /** Id to thermostat mapping */
    public Map<String, Thermostat> getThermostats() {
        return thermostats;
    }

    /** Id to camera mapping */
    public Map<String, Camera> getCameras() {
        return cameras;
    }

    /** Id to smoke detector */
    public Map<String, SmokeDetector> getSmokeCoAlarms() {
        return smokeCoAlarms;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("NestDevices [thermostats=").append(thermostats).append(", smokeCoAlarms=").append(smokeCoAlarms)
                .append(", cameras=").append(cameras).append("]");
        return builder.toString();
    }

}
