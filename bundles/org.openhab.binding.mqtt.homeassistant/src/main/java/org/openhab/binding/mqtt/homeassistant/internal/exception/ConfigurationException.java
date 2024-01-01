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
 * Exception class for errors in HomeAssistant components configurations
 *
 * @author Anton Kharuzhy - Initial contribution
 */
@NonNullByDefault
public class ConfigurationException extends RuntimeException {
    private static final long serialVersionUID = -4828651603869498942L;

    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
