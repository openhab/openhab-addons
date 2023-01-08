/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.bluetooth.radoneye.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Exception for data parsing errors.
 *
 * @author Peter Obel - Initial contribution
 */
@NonNullByDefault
public class RadoneyeParserException extends Exception {

    private static final long serialVersionUID = 1;

    public RadoneyeParserException() {
    }

    public RadoneyeParserException(String message) {
        super(message);
    }

    public RadoneyeParserException(String message, Throwable cause) {
        super(message, cause);
    }

    public RadoneyeParserException(Throwable cause) {
        super(cause);
    }
}
