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
package org.openhab.binding.iotawatt.internal.client;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Thrown on communication errors with the IoTaWatt device.
 *
 * @author Peter Rosenberg - Initial contribution
 */
@NonNullByDefault
public class IoTaWattClientCommunicationException extends Exception {
    static final long serialVersionUID = 7960876940928850536L;

    IoTaWattClientCommunicationException() {
    }

    public IoTaWattClientCommunicationException(String message) {
        super(message);
    }
}
