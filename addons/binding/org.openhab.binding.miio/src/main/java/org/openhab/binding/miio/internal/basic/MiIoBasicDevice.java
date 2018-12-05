/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.miio.internal.basic;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Mapping devices from json
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public class MiIoBasicDevice {

    @SerializedName("deviceMapping")
    @Expose
    private DeviceMapping deviceMapping;

    public DeviceMapping getDevice() {
        return deviceMapping;
    }

    public void setDevice(DeviceMapping deviceMapping) {
        this.deviceMapping = deviceMapping;
    }

}
