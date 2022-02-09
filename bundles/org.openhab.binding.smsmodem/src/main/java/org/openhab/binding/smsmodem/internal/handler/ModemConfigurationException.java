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
package org.openhab.binding.smsmodem.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 *
 * Exception class for SMSLib configuration
 *
 * @author Gwendal ROULLEAU - Initial contribution
 */
@NonNullByDefault
public class ModemConfigurationException extends Exception {

    private static final long serialVersionUID = -3455806333751297448L;

    public ModemConfigurationException(String message) {
        super(message);
    }

    public ModemConfigurationException(String message, Exception cause) {
        super(message, cause);
    }
}
