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
package org.openhab.binding.dahuadoor.internal.dahuaeventhandler;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Thrown when the device responds with an HTTP 302 redirect, which indicates a protocol mismatch
 * (e.g. device requires HTTPS but the binding is configured to use plain HTTP).
 * This is a permanent misconfiguration and should take the Thing offline.
 *
 * @author Sven Schad - Initial contribution
 */
@NonNullByDefault
public class DahuaHttpRedirectException extends Exception {

    private static final long serialVersionUID = 1L;

    private final String redirectMessage;

    public DahuaHttpRedirectException(String message) {
        super(message);
        this.redirectMessage = message;
    }

    public String getRedirectMessage() {
        return redirectMessage;
    }
}
