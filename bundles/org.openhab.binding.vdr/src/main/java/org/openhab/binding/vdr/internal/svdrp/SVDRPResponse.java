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
package org.openhab.binding.vdr.internal.svdrp;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SVDRPResponse} represents a general Object returned by an SVDRP Client Call
 *
 * @author Matthias Klocke - Initial contribution
 */
@NonNullByDefault
public class SVDRPResponse {
    private int code;
    private String message;

    public SVDRPResponse(int code, String response) {
        this.code = code;
        this.message = response;
    }

    /**
     * Get Status Code of SVDRP Response
     *
     * @return Status Code of SVDRP Response
     */
    public int getCode() {
        return code;
    }

    /**
     * Get Message of SVDRP Response
     *
     * @return Message of SVDRP Response
     */
    public String getMessage() {
        return message;
    }
}
