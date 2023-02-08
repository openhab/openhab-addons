/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.tplinksmarthome.internal.model;

import com.google.gson.annotations.Expose;

/**
 * Base class for responses containing the common error response fields.
 * Only getter methods as the values are set by gson based on the retrieved json.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
public class ErrorResponse implements HasErrorResponse {

    @Expose(serialize = false)
    private int errCode;
    @Expose(serialize = false)
    private String errMsg;

    /**
     * @return the error code
     */
    public int getErrorCode() {
        return errCode;
    }

    /**
     * @return the error message
     */
    public String getErrorMessage() {
        return errMsg;
    }

    @Override
    public ErrorResponse getErrorResponse() {
        return this;
    }

    @Override
    public String toString() {
        return "{err_code:" + errCode + ", err_msg:'" + errMsg + "'}";
    }
}
