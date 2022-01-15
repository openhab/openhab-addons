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
package org.openhab.binding.solarwatt.internal.exception;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Exception to be used whenever anything goes wrong talking to the energy manager.
 *
 * @author Sven Carstens - Initial contribution
 */
@NonNullByDefault
public class SolarwattConnectionException extends Exception {
    static final long serialVersionUID = 3387516924229948L;

    public SolarwattConnectionException(final @Nullable String message) {
        super(message);
    }

    public SolarwattConnectionException(final @Nullable String message, final Exception e) {
        super(message, e);
    }
}
