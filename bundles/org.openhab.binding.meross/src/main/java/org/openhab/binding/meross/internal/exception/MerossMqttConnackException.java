/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.meross.internal.exception;

import java.io.Serial;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link MerossMqttConnackException} is responsible for handling Connack exception from
 * {@link org.openhab.binding.meross.internal.api.MerossMqttConnector}.
 *
 * @author Giovanni Fabiani - Initial contribution
 */
@NonNullByDefault
public class MerossMqttConnackException extends Exception {

    @Serial
    private static final long serialVersionUID = 2L;

    public MerossMqttConnackException(String message) {
        super(message);
    }

    public MerossMqttConnackException(Throwable cause) {
        super("Error occurred on Connack", cause);
    }

    public MerossMqttConnackException(String message, Throwable cause) {
        super(message, cause);
    }
}
