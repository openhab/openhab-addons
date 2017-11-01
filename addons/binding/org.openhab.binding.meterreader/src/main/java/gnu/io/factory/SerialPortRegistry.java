/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package gnu.io.factory;

import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;

import gnu.io.SerialPort;

/**
 *
 * @author MatthiasS
 *
 */
public class SerialPortRegistry {

    private Collection<SerialPortCreator<? extends SerialPort>> portCreators;

    public SerialPortRegistry() {
        // register the LOCAL PortCreator as last argument, so that is always taken into account when no other creator
        // is applicable.
        this.portCreators = new TreeSet<SerialPortCreator<? extends SerialPort>>(
                new Comparator<SerialPortCreator<? extends SerialPort>>() {

                    @Override
                    public int compare(SerialPortCreator<? extends SerialPort> o1,
                            SerialPortCreator<? extends SerialPort> o2) {
                        if (o1.getProtocol().equals(SerialPortCreator.LOCAL)) {
                            return 1;
                        }
                        if (o2.getProtocol().equals(SerialPortCreator.LOCAL)) {
                            return -1;
                        }
                        return o1.getProtocol().compareTo(o2.getProtocol());
                    }
                });

        registerDefaultSerialPortCreators();
    }

    /**
     * Registers the {@link RxTxPortCreator} and the {@link RFC2217PortCreator}.
     */
    protected void registerDefaultSerialPortCreators() {
        registerSerialPortCreator(new RxTxPortCreator());
        registerSerialPortCreator(new RFC2217PortCreator());
    }

    /**
     * Registers a {@link SerialPortCreator}.
     *
     * @param creator
     */
    public void registerSerialPortCreator(SerialPortCreator<? extends SerialPort> creator) {
        this.portCreators.add(creator);
    }

    /**
     * Gets the best applicable {@link SerialPortCreator} for the given <code>portName</code>
     *
     * @param portName The port's name.
     * @return A found {@link SerialPortCreator} or null if none could be found.
     */
    @SuppressWarnings("unchecked")
    public <T extends SerialPort> SerialPortCreator<T> getPortCreatorForPortName(String portName,
            Class<T> expectedClass) {
        for (@SuppressWarnings("rawtypes")
        SerialPortCreator creator : this.portCreators) {
            try {
                if (creator.isApplicable(portName, expectedClass)) {
                    return creator;
                }
            } catch (Exception e) {
                System.err.println("Error for SerialPortCreator#isApplicable: " + creator.getClass() + "; "
                        + creator.getProtocol() + " -> " + e.getMessage());
            }
        }
        return null;
    }
}
