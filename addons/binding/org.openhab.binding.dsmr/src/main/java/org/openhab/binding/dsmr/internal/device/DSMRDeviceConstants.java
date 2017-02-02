/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dsmr.internal.device;

import java.util.concurrent.TimeUnit;

/**
 * This class holds all the constants that are applicable for the DSMR Device
 *
 * @author M. Volaart
 * @since 2.1.0
 */
public class DSMRDeviceConstants {
    // State definitions
    public enum DeviceState {
        INITIALIZING,
        STARTING,
        SWITCH_PORT_SPEED,
        ONLINE,
        OFFLINE,
        SHUTDOWN,
        CONFIGURATION_PROBLEM;
    }

    // DSMR Port events
    public enum DSMRPortEvent {
        CLOSED("Serial port closed"),
        OPENED("Serial port opened"),
        READ_OK("Read ok"),
        READ_ERROR("Read error"),
        LINE_BROKEN("Serial line is broken (cable problem?)"),
        CONFIGURATION_ERROR("Configuration error"),
        DONT_EXISTS("Serial port does not exist"),
        IN_USE("Serial port is already in use"),
        NOT_COMPATIBLE("Serial port is not compatible"),
        WRONG_BAUDRATE("Wrong baudrate"),
        ERROR("General error");

        // Public accessible details about the event
        public final String eventDetails;

        /**
         * Constructor for a DSMRPortEvent
         *
         * @param eventDetails String containing the details about the event
         */
        private DSMRPortEvent(String eventDetails) {
            this.eventDetails = eventDetails;
        }
    }

    // DSMR Device events
    public enum DSMRDeviceEvent {
        INITIALIZE("Initializing DSMR device"),
        DSMR_PORT_OPENED("DMSR port opened successfull"),
        DSMR_PORT_CLOSED("DMSR port closed"),
        SWITCH_BAUDRATE("DSMR port switch baudrate"),
        TELEGRAM_RECEIVED("DSMR device received P1 telegram successfull"),
        CONFIGURATION_ERROR("DSMR device has a configuration error"),
        ERROR("DSMR device experienced a general error"),
        READ_ERROR("DSMR port read error"),
        SHUTDOWN("DSMR device shutdown");

        // Public accessible details about the event
        public String eventDetails;

        /**
         * Constructor for a DSMRDeviceEvent
         *
         * @param eventDetails String containing the details about the event
         */
        private DSMRDeviceEvent(String eventDetails) {
            this.eventDetails = eventDetails;
        }
    }

    // Serial port read time out (15 seconds)
    public static final int SERIAL_PORT_READ_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(15);
    // Timeout for detecting the correct serial port settings
    public static final int SERIAL_PORT_AUTO_DETECT_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(30);
    // // Timeout for recovery from offline mode
    public static final int RECOVERY_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(30);
}
