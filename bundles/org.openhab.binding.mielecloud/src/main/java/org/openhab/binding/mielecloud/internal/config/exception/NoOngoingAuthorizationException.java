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
package org.openhab.binding.mielecloud.internal.config.exception;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Exception thrown when no authorization is ongoing.
 *
 * @author Björn Lange - Initial Contribution
 */
@NonNullByDefault
public class NoOngoingAuthorizationException extends RuntimeException {
    private static final long serialVersionUID = 3074275827393542416L;

    public NoOngoingAuthorizationException(String message) {
        super(message);
    }
}
