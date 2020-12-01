/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sony.internal;

import java.util.Objects;

import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;

/**
 * This class represents a login response to a sony device if the login was unsuccessful
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class LoginUnsuccessfulResponse {
    /** The specific thing status detail for the response */
    private final ThingStatusDetail thingStatusDetail;

    /** The message related to the response */
    private final String message;

    /**
     * Constructs the unsuccessful response from the detail and message
     *
     * @param thingStatusDetail a non-null thing status detail
     * @param message the non-null, non-empty message
     */
    public LoginUnsuccessfulResponse(final ThingStatusDetail thingStatusDetail, final String message) {
        Objects.requireNonNull(thingStatusDetail, "thingStatusDetail cannot be null");
        Validate.notEmpty(message, "message cannot be empty");

        this.thingStatusDetail = thingStatusDetail;
        this.message = message;
    }

    /**
     * Returns the thing status detail for the response
     *
     * @return a non-null thing status detail
     */
    public ThingStatusDetail getThingStatusDetail() {
        return thingStatusDetail;
    }

    /**
     * Returns the message related to the response
     *
     * @return a non-null, non-empty message response
     */
    public String getMessage() {
        return message;
    }
}
