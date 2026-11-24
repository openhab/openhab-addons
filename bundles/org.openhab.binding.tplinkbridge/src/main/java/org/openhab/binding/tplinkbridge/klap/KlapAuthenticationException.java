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
package org.openhab.binding.tplinkbridge.klap;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The device rejected every candidate credential, or none was available. Retrying with the same credentials won't
 * help, so callers should report a configuration error instead of a communication error.
 *
 * @author Christopher Smith - Initial contribution
 */
@NonNullByDefault
public class KlapAuthenticationException extends IOException {

    private static final long serialVersionUID = 1L;

    public KlapAuthenticationException(String message) {
        super(message);
    }
}
