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

package org.openhab.binding.tedee.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * 
 * Exception thrown when communication with the Tedee Bridge API fails.
 *
 * @author Alex Goll - Initial contribution
 */
@NonNullByDefault
public class TedeeApiException extends Exception {
    private final int status;

    public TedeeApiException(String m, int s) {
        super(m);
        status = s;
    }

    public TedeeApiException(String m, int s, Throwable t) {
        super(m, t);
        status = s;
    }

    public int getStatus() {
        return status;
    }
}
