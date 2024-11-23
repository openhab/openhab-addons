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
package org.openhab.binding.rfxcom.internal.exceptions;

/**
 * Exception to indicate that a request was received for an unsupported channel
 *
 * @author Martin van Wingerden - Initial contribution
 */
public class RFXComUnsupportedChannelException extends RFXComException {
    public RFXComUnsupportedChannelException(String message) {
        super(message);
    }
}
