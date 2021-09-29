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
package org.openhab.binding.freeboxos.internal.api.login;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.Response;

/**
 * The {@link Authorize} is the Java class used to map the
 * structure used by the response of the request authorization API
 * https://dev.freebox.fr/sdk/os/login/#
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
class Authorize {
    public static class AuthorizeResponse extends Response<Authorize> {
    }

    @NotEmpty(message = "No app token provided")
    private @NonNullByDefault({}) String appToken;

    @Min(value = 1, message = "Missing or invalid trackId")
    private int trackId;

    public String getAppToken() {
        return appToken;
    }

    public int getTrackId() {
        return trackId;
    }
}
