/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.linktap.protocol.frames;

import java.util.Collection;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link GatewayEndDevListReq} is a reusable frame used for multiple commands where a device endpoint list is
 * included.
 *
 * @provides: Endpoint Device ID List
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class GatewayEndDevListReq extends GatewayDeviceResponse {

    protected static final Pattern FULL_DEVICE_ID_PATTERN = Pattern.compile("[a-zA-Z0-9]{20}");

    public GatewayEndDevListReq() {
    }

    /**
     * Defines the endpoint devices added / registered to the Gateway.
     * Limited to the first 16 digits and letters of the Device ID
     */
    @SerializedName("end_dev")
    @Expose
    public String[] endDevices = EMPTY_STRING_ARRAY;

    public Collection<ValidationError> getValidationErrors() {
        final Collection<ValidationError> errors = super.getValidationErrors();

        for (String ed : endDevices) {
            if (command == CMD_ADD_END_DEVICE) {
                if (!FULL_DEVICE_ID_PATTERN.matcher(ed).matches()) {
                    errors.add(new ValidationError("end_dev", "endDevice " + ed + " invalid"));
                }
            } else {
                if (!DEVICE_ID_PATTERN.matcher(ed).matches()) {
                    errors.add(new ValidationError("end_dev", "endDevice " + ed + " invalid"));
                }
            }
        }
        return errors;
    }
}
