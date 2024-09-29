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
package org.openhab.binding.nibeheatpump.internal.connection;

import org.openhab.binding.nibeheatpump.internal.message.NibeHeatPumpMessage;

/**
 * This interface defines interface to receive data from heat pump.
 *
 * @author Pauli Anttila - Initial contribution
 */
public interface NibeHeatPumpEventListener {

    /**
     * Procedure for receive raw data from heat pump.
     *
     * @param msg
     *            Received raw data.
     */
    void msgReceived(NibeHeatPumpMessage msg);

    /**
     * Procedure for receiving error information.
     *
     * @param error
     *            Error occurred.
     */
    void errorOccurred(String error);
}
