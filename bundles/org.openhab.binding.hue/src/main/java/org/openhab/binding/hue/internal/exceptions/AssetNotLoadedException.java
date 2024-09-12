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
package org.openhab.binding.hue.internal.exceptions;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Thrown when one of the connection or handler classes has not loaded all its assets.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class AssetNotLoadedException extends Exception {
    private static final long serialVersionUID = -1;

    public AssetNotLoadedException() {
    }

    public AssetNotLoadedException(String message) {
        super(message);
    }
}
