/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package gnu.io.factory;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author MatthiasS
 *
 */
public class SerialPortUtil {

    private static final String GNU_IO_RXTX_SERIAL_PORTS = "gnu.io.rxtx.SerialPorts";

    /**
     * Registers the given port as system property {@value #GNU_IO_RXTX_SERIAL_PORTS}. The method is capable of
     * extending the system property, if any other ports are already registered.
     *
     * @param port the port to be registered
     */
    public synchronized static void appendSerialPortProperty(String port) {
        String serialPortsProperty = System.getProperty(GNU_IO_RXTX_SERIAL_PORTS);
        String newValue = initSerialPort(port, serialPortsProperty);
        System.setProperty(GNU_IO_RXTX_SERIAL_PORTS, newValue);
    }

    private static String initSerialPort(String port, String serialPortsProperty) {

        Set<String> serialPorts = null;
        if (serialPortsProperty != null) {
            serialPorts = Stream.of(serialPortsProperty.split(":")).collect(Collectors.toSet());
        } else {
            serialPorts = new HashSet<String>();
        }
        if (serialPorts.add(port)) {
            return serialPorts.stream().collect(Collectors.joining(System.getProperty("path.separator", ":"))); // see
                                                                                                                // RXTXCommDriver#addSpecifiedPorts
        }
        return null;
    }
}
