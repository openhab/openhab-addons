/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dsmr.internal.device.connector;

import java.util.Enumeration;

import org.eclipse.jdt.annotation.NonNullByDefault;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;

/**
 * Interface for a serial port manager.
 *
 * This interface is modeled after the smart home serial transport interface. Because that interface is not yet
 * compatible with the requirements in this binding the transport module is not used. But the code is modeled to smooth
 * migration once it's possible.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public interface SerialPortManager {
    /**
     * Gets an serial port identifier for a given name.
     *
     * @param name the name
     * @return a serial port identifier or throws NoSuchPortException
     */
    default CommPortIdentifier getIdentifier(final String name) throws NoSuchPortException {
        return CommPortIdentifier.getPortIdentifier(name);
    }

    /**
     * Gets the serial port identifiers.
     *
     * @return stream of serial port identifiers
     */
    @SuppressWarnings("unchecked")
    default Enumeration<CommPortIdentifier> getPortIdentifiers() {
        return CommPortIdentifier.getPortIdentifiers();
    }

}
