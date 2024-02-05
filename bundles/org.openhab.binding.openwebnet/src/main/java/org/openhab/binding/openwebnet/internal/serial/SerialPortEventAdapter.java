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
package org.openhab.binding.openwebnet.internal.serial;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openwebnet4j.communication.serial.spi.SerialPortEvent;

/**
 * openwebnet4j SerialPortEvent implementation based on OH serial transport
 *
 * @author M. Valla - Initial contribution
 */

@NonNullByDefault
public class SerialPortEventAdapter implements SerialPortEvent {

    private final org.openhab.core.io.transport.serial.SerialPortEvent event;

    /**
     * Constructor.
     *
     * @param event the underlying event implementation
     */
    public SerialPortEventAdapter(org.openhab.core.io.transport.serial.SerialPortEvent event) {
        this.event = event;
    }

    @Override
    public int getEventType() {
        if (event.getEventType() == org.openhab.core.io.transport.serial.SerialPortEvent.PORT_DISCONNECTED) {
            return SerialPortEvent.EVENT_PORT_DISCONNECTED;
        } else if (event.getEventType() == org.openhab.core.io.transport.serial.SerialPortEvent.DATA_AVAILABLE) {
            return SerialPortEvent.EVENT_DATA_AVAILABLE;
        } else {
            return event.getEventType();
        }
    }
}
