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
package org.openhab.binding.dsmr.internal.device.connector;

import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Error events from a connector.
 *
 * @author M. Volaart - Initial contribution
 * @author Hilbrand Bouwkamp - Refactored all error into one enum
 */
@NonNullByDefault
public enum DSMRErrorStatus {
    /**
     * The smarty telegram was successfully received but could not be decoded because of an invalid decryption key.
     */
    INVALID_DECRYPTION_KEY(false),
    /**
     * Serial port could not be found.
     */
    PORT_DONT_EXISTS(true),
    /**
     * Serial port is already in use by another application.
     */
    PORT_IN_USE(true),
    /**
     * Internal error in the serial port communication.
     */
    PORT_INTERNAL_ERROR(true),
    /**
     * Serial port doesn't support the configured settings.
     */
    PORT_NOT_COMPATIBLE(true),
    /**
     * Serial port time out or illegal state.
     */
    PORT_PORT_TIMEOUT(false),
    /**
     * Reading data from the serial port failed.
     */
    SERIAL_DATA_READ_ERROR(false),
    /**
     * The telegram CRC16 checksum failed (only DSMR V4 and up).
     */
    TELEGRAM_CRC_ERROR(false),
    /**
     * The P1 telegram has syntax errors.
     */
    TELEGRAM_DATA_CORRUPTION(false),
    /**
     * Received telegram data, but after parsing no data is present. Possibly all data corrupted.
     */
    TELEGRAM_NO_DATA(false);

    private final boolean fatal;

    private DSMRErrorStatus(final boolean fatal) {
        this.fatal = fatal;
    }

    /**
     * @return Returns true if this error is not something possible temporary, but something that can't be recovered
     *         from.
     */
    public boolean isFatal() {
        return fatal;
    }

    /**
     * @return the event details
     */
    public String getEventDetails() {
        return "@text/addon.dsmr.error.status." + name().toLowerCase(Locale.ROOT);
    }
}
