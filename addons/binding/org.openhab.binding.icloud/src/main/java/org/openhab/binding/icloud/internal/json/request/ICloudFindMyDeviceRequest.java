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
package org.openhab.binding.icloud.internal.json.request;

import static org.openhab.binding.icloud.internal.ICloudBindingConstants.FIND_MY_DEVICE_REQUEST_SUBJECT;

import com.google.gson.annotations.SerializedName;

/**
 * Serializable class to create a "Find My Device" json request string.
 *
 * @author Patrik Gfeller - Initial Contribution
 */
public class ICloudFindMyDeviceRequest {
    @SerializedName("device")
    String deviceId;
    final String subject = FIND_MY_DEVICE_REQUEST_SUBJECT;

    public ICloudFindMyDeviceRequest(String id) {
        deviceId = id;
    }
}
