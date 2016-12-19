/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.roku.configuration;

import org.eclipse.smarthome.core.thing.Thing;

/**
 * The {@link RokuBindingConfig} class defines a common configuration, which is used
 * to manually configure the binding.
 *
 * @author Jarod Peters - Initial contribution
 */
public class RokuDeviceConfig {
    private String ipAddress;
    private Number port;
    private Thing thing;

    public void setIPAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getIPAddress() {
        return ipAddress;
    }

    public void setPort(Number port) {
        this.port = port;
    }

    public Number getPort() {
        return port;
    }

    public void setThing(Thing thing) {
        this.thing = thing;
    }

    public Thing getThing() {
        return thing;
    }
}