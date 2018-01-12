/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
