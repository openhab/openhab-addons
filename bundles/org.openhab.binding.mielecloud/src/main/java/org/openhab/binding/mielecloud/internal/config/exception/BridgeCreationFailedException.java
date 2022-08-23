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
package org.openhab.binding.mielecloud.internal.config.exception;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Exception thrown when a bridge cannot be created in the configuration flow.
 *
 * @author Björn Lange - Initial Contribution
 */
@NonNullByDefault
public final class BridgeCreationFailedException extends RuntimeException {
    private static final long serialVersionUID = -6150154333256723312L;

    public BridgeCreationFailedException(String message) {
        super(message);
    }

    public BridgeCreationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
