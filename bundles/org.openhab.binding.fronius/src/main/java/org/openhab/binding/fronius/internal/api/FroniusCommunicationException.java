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
package org.openhab.binding.fronius.internal.api;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Exception for unexpected response from or communication failure with the Fronius controller.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class FroniusCommunicationException extends IOException {
    private static final long serialVersionUID = 619020705591964155L;

    public FroniusCommunicationException(String message) {
        super(message);
    }

    public FroniusCommunicationException(Throwable ex) {
        super(ex);
    }

    public FroniusCommunicationException(String message, @Nullable Throwable cause) {
        super(message, cause);
    }
}
