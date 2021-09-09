/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.cloudrain.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.cloudrain.internal.CloudrainException;
import org.openhab.binding.cloudrain.internal.api.model.CloudrainAPIError;

/**
 * An Exception Class for errors when communicating with the Cloudrain API
 *
 * @author Till Koellmann - Initial contribution
 */
@NonNullByDefault
public class CloudrainAPIException extends CloudrainException {

    private static final long serialVersionUID = -3833584090572489599L;

    /**
     * The error object containing details about the API error
     */
    private CloudrainAPIError error = new CloudrainAPIError();

    /**
     * Creates a new CloudrainAPIException with a throwable
     *
     * @param cause the original throwable
     */
    public CloudrainAPIException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new CloudrainAPIException with a message
     *
     * @param message the summary error message
     */
    public CloudrainAPIException(String message) {
        super(message);
    }

    /**
     * Creates a new CloudrainAPIException with message and error information
     *
     * @param message the summary error message
     * @param error the detailed error information received from the API
     */
    public CloudrainAPIException(String message, CloudrainAPIError error) {
        super(message);
        this.error = error;
    }

    /**
     * Creates a new CloudrainAPIException with message and error information
     *
     * @param error the detailed error information received from the API
     */
    public CloudrainAPIException(CloudrainAPIError error) {
        super(error.getMessage());
        this.error = error;
    }

    /**
     * Returns the error information
     *
     * @return the error information
     */
    public CloudrainAPIError getError() {
        return error;
    }

    /**
     * Sets the error information
     *
     * @param error the error information object
     */
    public void setError(CloudrainAPIError error) {
        this.error = error;
    }

    @Override
    public @Nullable String getMessage() {
        String errorMsg = error.getMessage();
        if (!CloudrainAPIError.ERROR_MSG_UNAVAILABLE.equals(errorMsg) && !errorMsg.isEmpty()) {
            return errorMsg;
        } else {
            return super.getMessage();
        }
    }
}
