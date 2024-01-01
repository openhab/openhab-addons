/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.semsportal.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Exception indicating there was a problem communicating with the portal. It can indicate either no response at all, or
 * a response that was not expected.
 *
 * @author Iwan Bron - Initial contribution
 *
 */
@NonNullByDefault
public class CommunicationException extends Exception {
    private static final long serialVersionUID = 4175625868879971138L;

    public CommunicationException(String message) {
        super(message);
    }
}
