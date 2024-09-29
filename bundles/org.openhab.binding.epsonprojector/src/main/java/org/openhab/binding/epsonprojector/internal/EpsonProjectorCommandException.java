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
package org.openhab.binding.epsonprojector.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Exception for Epson projector command errors.
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public class EpsonProjectorCommandException extends Exception {

    private static final long serialVersionUID = -8048415193494625295L;

    public EpsonProjectorCommandException(String message) {
        super(message);
    }
}
