/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.myvaillant.internal.api;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.util.StringUtils;

/**
 * The myVAILLANT API answered with an HTTP error status.
 *
 * @author Marius Korte - Initial contribution
 */
@NonNullByDefault
public class MyVaillantApiException extends MyVaillantException {

    private static final long serialVersionUID = 1L;

    private final int status;

    public MyVaillantApiException(String method, String path, int status, String body) {
        super(method + " " + path + " failed with HTTP " + status + (body.isBlank() ? "" : ": " + abbreviate(body)));
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    private static String abbreviate(String body) {
        return Objects.requireNonNull(StringUtils.abbreviate(body.replaceAll("\\s+", " ").trim(), 300));
    }
}
