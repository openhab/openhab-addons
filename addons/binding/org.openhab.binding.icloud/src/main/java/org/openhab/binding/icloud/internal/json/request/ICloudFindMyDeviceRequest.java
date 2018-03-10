/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.icloud.internal.json.request;

import com.google.gson.annotations.SerializedName;

/**
 * Serializable class to create a "Find My Device" json request string.
 *
 * @author Patrik Gfeller
 *
 */
public class ICloudFindMyDeviceRequest {
    @SerializedName("device")
    String deviceId;
    final String subject = "Find My Device alert";

    public ICloudFindMyDeviceRequest(String id) {
        deviceId = id;
    }
}
