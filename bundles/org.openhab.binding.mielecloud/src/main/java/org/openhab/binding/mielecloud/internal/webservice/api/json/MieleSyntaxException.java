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
package org.openhab.binding.mielecloud.internal.webservice.api.json;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * {@link RuntimeException} thrown when the syntax of a message received from the Miele REST API does not match and
 * cannot be interpreted as the expected syntax (e.g. by ignoring entries).
 *
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public class MieleSyntaxException extends RuntimeException {
    private static final long serialVersionUID = 8253804935427566729L;

    public MieleSyntaxException(String message) {
        super(message);
    }

    public MieleSyntaxException(String message, Throwable cause) {
        super(message, cause);
    }
}
