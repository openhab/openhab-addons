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
package org.openhab.binding.mielecloud.internal.webservice;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Response;
import org.openhab.binding.mielecloud.internal.webservice.api.json.ErrorMessage;
import org.openhab.binding.mielecloud.internal.webservice.api.json.MieleSyntaxException;
import org.openhab.binding.mielecloud.internal.webservice.exception.AuthorizationFailedException;
import org.openhab.binding.mielecloud.internal.webservice.exception.MieleWebserviceException;
import org.openhab.binding.mielecloud.internal.webservice.exception.MieleWebserviceTransientException;
import org.openhab.binding.mielecloud.internal.webservice.exception.TooManyRequestsException;

/**
 * Holds utility functions for working with HTTP.
 *
 * @author Björn Lange - Initial Contribution
 */
@NonNullByDefault
public final class HttpUtil {
    private static final String RETRY_AFTER_HEADER_FIELD_NAME = "Retry-After";

    private HttpUtil() {
        throw new IllegalStateException("This class must not be instantiated");
    }

    /**
     * Checks whether the HTTP status given in {@code response} is a success state. In case an error state is obtained,
     * exceptions are thrown.
     *
     * @param response The response to check.
     * @throws MieleWebserviceTransientException if the status indicates a transient HTTP error.
     * @throws MieleWebserviceException if the status indicates another HTTP error.
     * @throws AuthorizationFailedException if the status indicates an authorization failure.
     * @throws TooManyRequestsException if the status indicates that too many requests have been made against the remote
     *             endpoint.
     */
    public static void checkHttpSuccess(Response response) {
        if (isHttpSuccessStatus(response.getStatus())) {
            return;
        }

        String exceptionMessage = getHttpErrorMessageFromCloudResponse(response);

        switch (response.getStatus()) {
            case 401:
                throw new AuthorizationFailedException(exceptionMessage);
            case 429:
                String retryAfter = null;
                if (response.getHeaders().containsKey(RETRY_AFTER_HEADER_FIELD_NAME)) {
                    retryAfter = response.getHeaders().get(RETRY_AFTER_HEADER_FIELD_NAME);
                }
                throw new TooManyRequestsException(exceptionMessage, retryAfter);
            case 500:
                throw new MieleWebserviceTransientException(exceptionMessage, ConnectionError.SERVER_ERROR);
            case 503:
                throw new MieleWebserviceTransientException(exceptionMessage, ConnectionError.SERVICE_UNAVAILABLE);
            default:
                throw new MieleWebserviceException(exceptionMessage, ConnectionError.OTHER_HTTP_ERROR);
        }
    }

    /**
     * Gets whether {@code httpStatus} is a HTTP error code from the 200 range (success).
     */
    private static boolean isHttpSuccessStatus(int httpStatus) {
        return httpStatus / 100 == 2;
    }

    private static String getHttpErrorMessageFromCloudResponse(Response response) {
        String exceptionMessage = "HTTP error " + response.getStatus() + ": " + response.getReason();

        if (response instanceof ContentResponse) {
            try {
                ErrorMessage errorMessage = ErrorMessage.fromJson(((ContentResponse) response).getContentAsString());
                exceptionMessage += "\nCloud returned message: " + errorMessage.getMessage();
            } catch (MieleSyntaxException e) {
                exceptionMessage += "\nCloud returned invalid message.";
            }
        }
        return exceptionMessage;
    }
}
