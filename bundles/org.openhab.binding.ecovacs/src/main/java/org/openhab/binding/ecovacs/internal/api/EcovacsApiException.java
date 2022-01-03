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
package org.openhab.binding.ecovacs.internal.api;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.api.Response;

/**
 * @author Danny Baumann - Initial contribution
 */
@NonNullByDefault
public class EcovacsApiException extends IOException {
    private static final long serialVersionUID = -5903398729974682356L;

    public EcovacsApiException(String reason) {
        super(reason);
    }

    public EcovacsApiException(Response response) {
        super("HTTP status " + response.getStatus());
    }

    public EcovacsApiException(Throwable cause) {
        super(cause);
    }
}
