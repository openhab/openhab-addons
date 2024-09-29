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
package org.openhab.binding.nobohub.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Exception thrown when the data received from the hub has unexpected format.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class NoboDataException extends Exception {

    private static final long serialVersionUID = -620277949858983367L;

    public NoboDataException(String message) {
        super(message);
    }

    public NoboDataException(String message, Throwable parent) {
        super(message, parent);
    }
}
