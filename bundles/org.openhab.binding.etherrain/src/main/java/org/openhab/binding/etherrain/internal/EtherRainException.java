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
package org.openhab.binding.etherrain.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link EtherRainException} class defines an exception for handling
 * EtherRainExceptions
 *
 * @author Joe Inkenbrandt - Initial contribution
 */

@NonNullByDefault
public class EtherRainException extends Exception {
    private static final long serialVersionUID = 1348095602193770716L;

    public EtherRainException(String message) {
        super(message);
    }
}
