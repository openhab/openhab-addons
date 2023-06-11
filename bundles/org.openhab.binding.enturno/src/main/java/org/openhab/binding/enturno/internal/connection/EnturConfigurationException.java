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
package org.openhab.binding.enturno.internal.connection;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link EnturConfigurationException} is a configuration exception for the connections to Entur API.
 *
 * @author Michal Kloc - Initial contribution
 */
@NonNullByDefault
public class EnturConfigurationException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

    public EnturConfigurationException(String message) {
        super(message);
    }

    public EnturConfigurationException(Throwable cause) {
        super(cause);
    }

    public EnturConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
