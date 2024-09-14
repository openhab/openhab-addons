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
package org.openhab.binding.entsoe.internal.exception;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Jørgen Melhus - Initial contribution
 *
 */
@NonNullByDefault
public class entsoeResponseMapException extends Exception {

    private static final long serialVersionUID = -2792399185046072564L;

    public entsoeResponseMapException(String message) {
        super(message);
    }

    public entsoeResponseMapException(Throwable cause) {
        super(cause);
    }
}
