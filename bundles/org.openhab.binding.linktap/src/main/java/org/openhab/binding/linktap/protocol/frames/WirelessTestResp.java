/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link WirelessTestResp} defines the wireless test result data response
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class WirelessTestResp extends EndpointDeviceResponse {

    public WirelessTestResp() {
    }

    /**
     * Defines true if the last packet has been transmitted and
     * therefore the test is complete
     */
    @SerializedName("final")
    @Expose
    public boolean testComplete = false;

    /**
     * Defines how many pings have been sent to the endpoint device
     */
    @SerializedName("ping")
    @Expose
    public int pingCount = DEFAULT_INT;

    /**
     * Defines how many pongs have been received from the endpoint device
     */
    @SerializedName("pong")
    @Expose
    public int pongCount = DEFAULT_INT;

    public Collection<ValidationError> getValidationErrors() {
        final Collection<ValidationError> errors = super.getValidationErrors();

        if (pingCount == DEFAULT_INT) {
            errors.add(new ValidationError("ping", "count is missing"));
        }

        if (pongCount == DEFAULT_INT) {
            errors.add(new ValidationError("pong", "count is missing"));
        }

        if (!DEVICE_ID_PATTERN.matcher(deviceId).matches()) {
            errors.add(new ValidationError("dev_id", "is not in the expected format"));
        }

        return errors;
    }
}
