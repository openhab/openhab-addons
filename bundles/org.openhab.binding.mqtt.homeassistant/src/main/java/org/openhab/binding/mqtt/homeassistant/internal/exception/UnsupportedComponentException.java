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
package org.openhab.binding.mqtt.homeassistant.internal.exception;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Exception class for unsupported components
 *
 * @author Anton Kharuzhy - Initial contribution
 */
@NonNullByDefault
public class UnsupportedComponentException extends ConfigurationException {
    private static final long serialVersionUID = 5134690914728956232L;

    public UnsupportedComponentException(String message) {
        super(message);
    }

    public UnsupportedComponentException(String message, Throwable cause) {
        super(message, cause);
    }
}
