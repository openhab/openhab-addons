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
package org.openhab.binding.sensorpush.internal.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * API Response JSON object
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public abstract class Response {

    /** May contain message on error */
    public @Nullable String message;
    /** May contain error type on error */
    public @Nullable String type;
    /** May contain HTTP status code on error */
    public @Nullable String statusCode;

    public Response() {
    }
}
