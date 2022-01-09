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
package org.openhab.binding.mielecloud.internal.webservice;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ConnectionError} enumeration represents the error state of a connection to the Miele cloud.
 *
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public enum ConnectionError {
    SERVER_ERROR,
    SERVICE_UNAVAILABLE,
    OTHER_HTTP_ERROR,
    REQUEST_INTERRUPTED,
    TIMEOUT,
    REQUEST_EXECUTION_FAILED,
    RESPONSE_MALFORMED,
    AUTHORIZATION_FAILED,
    TOO_MANY_RERQUESTS,
    SSE_STREAM_ENDED,
    UNKNOWN,
}
