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
package org.openhab.binding.arcam.internal.exceptions;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Thrown when an item cannot be found in a lookup table.
 *
 * @author Joep Admiraal - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("serial")
public class NotFoundException extends RuntimeException {
    public NotFoundException() {
    }

    public NotFoundException(String message) {
        super(message);
    }
}
