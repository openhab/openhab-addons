/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dsmr.internal.discovery;

/**
 * This interface is notified of new meter discoveries
 *
 * @author M. Volaart
 * @since 2.1.0
 */
public interface DSMRBridgeDiscoveryListener {
    /**
     * A new bridge is discovered
     *
     * @param serialPort serial port identifier (e.g. /dev/ttyUSB0 or COM1)
     * @return true if the new bridge is accepted, false otherwise
     */
    public boolean bridgeDiscovered(String serialPort);
}
