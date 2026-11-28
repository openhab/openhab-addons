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
package org.openhab.binding.tplinksmarthome.internal;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The device accepted the KLAP login but answers in TP-Link's newer SMART protocol, which newer hardware revisions of
 * some Kasa models use instead of the Kasa JSON this binding speaks. Retrying doesn't help.
 *
 * @author Christopher Smith - Initial contribution
 */
@NonNullByDefault
public class UnsupportedProtocolException extends IOException {

    private static final long serialVersionUID = 1L;

    public UnsupportedProtocolException(final String message) {
        super(message);
    }
}
