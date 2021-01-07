/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.cul;

/**
 * An interface representing a culfw based device. Is handled by {@link CulCunBaseBridgeHandler}.
 * Classes implementing this interface need to have a constructor with a CULConfiguration as a parameter.
 *
 * @author Till Klocke - Initial contribution
 * @author Johannes Goehr (johgoe) - Migration to OpenHab 3.0
 * @since 1.4.0
 */
public interface CULHandler {

    /**
     * Send a String representing a culfw command to the CULHandler. Note that
     * Strings changing the RF mode will be discarded silently.
     *
     * @param command
     * @throws CULCommunicationException
     */
    public void send(String command) throws CULCommunicationException;

    /**
     * Get the number of transmit credits remaining. This
     * value is updated every time data is RX'd or TX'd
     *
     * @return number of 10ms transmit credits remaining
     */
    public int getCredit10ms();
}
