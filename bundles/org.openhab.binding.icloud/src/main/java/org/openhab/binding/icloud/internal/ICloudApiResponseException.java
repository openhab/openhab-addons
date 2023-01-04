/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.icloud.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 *
 * Exception for errors during calls of the iCloud API.
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
public class ICloudApiResponseException extends Exception {

    private static final long serialVersionUID = 1L;
    private int statusCode;

    /**
     * The constructor.
     *
     * @param url URL for which the exception occurred
     * @param statusCode HTTP status code which was reported
     */
    public ICloudApiResponseException(String url, int statusCode) {
        super(String.format("Request %s failed with %s.", url, statusCode));
        this.statusCode = statusCode;
    }

    /**
     * @return statusCode HTTP status code of failed request.
     */
    public int getStatusCode() {
        return this.statusCode;
    }
}
