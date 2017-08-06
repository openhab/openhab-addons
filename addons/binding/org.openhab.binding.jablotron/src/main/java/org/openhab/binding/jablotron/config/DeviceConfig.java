/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jablotron.config;

/**
 * The {@link DeviceConfig} class defines the thing configuration
 * object.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class DeviceConfig {
    private String serviceId;
    private String url;
    private int refresh;

    public String getServiceId() {
        return serviceId;
    }

    public String getUrl() {
        return url;
    }

    public int getRefresh() {
        return refresh;
    }
}
