/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.ecoflow.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.api.Response;

/**
 * @author Danny Baumann - Initial contribution
 */
@NonNullByDefault
public class EcoflowApiException extends Exception {
    public static final long serialVersionUID = -3954265654869396363L;

    public EcoflowApiException(String reason) {
        super(reason);
    }

    public EcoflowApiException(String reason, Throwable cause) {
        super(reason, cause);
    }

    public EcoflowApiException(Response response) {
        super("HTTP status " + response.getStatus());
    }

    public EcoflowApiException(Throwable cause) {
        super(cause);
    }
}
