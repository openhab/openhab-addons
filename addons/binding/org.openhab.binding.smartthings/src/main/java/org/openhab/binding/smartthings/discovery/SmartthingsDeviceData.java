/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smartthings.discovery;

/**
 * Mapping object for data returned from smartthings hub
 *
 * @author Bob Raker - Initial contribution
 *
 */

public class SmartthingsDeviceData {
    private String capability;
    private String attribute;
    private String name;
    private String id;

    SmartthingsDeviceData() {
    }

    public String getCapability() {
        return capability;
    }

    public String getAttribute() {
        return attribute;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

}
