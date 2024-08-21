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
package org.openhab.binding.growatt.internal.cloud;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link GrowattApiException} is thrown if a call to the Growatt cloud API server fails.
 *
 * @author Andrew Fiddian-Green - Initial contribution.
 */
@NonNullByDefault
public class GrowattApiException extends Exception {

    private static final long serialVersionUID = 218139823621683189L;

    public GrowattApiException(String message) {
        super(message);
    }

    public GrowattApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
