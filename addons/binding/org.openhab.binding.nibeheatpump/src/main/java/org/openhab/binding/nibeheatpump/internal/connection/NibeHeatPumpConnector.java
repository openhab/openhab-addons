/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nibeheatpump.internal.connection;

import org.openhab.binding.nibeheatpump.internal.NibeHeatPumpException;
import org.openhab.binding.nibeheatpump.internal.config.NibeHeatPumpConfiguration;
import org.openhab.binding.nibeheatpump.internal.message.NibeHeatPumpMessage;

/**
 * Define interface to communicate Nibe heat pumps.
 *
 * @author Pauli Anttila - Initial contribution
 */
public interface NibeHeatPumpConnector {

    /**
     * Procedure for connect to heat pump.
     *
     * @param configuration
     *            Configuration parameters for connector.
     *
     * @throws NibeHeatPumpException
     */
    void connect(NibeHeatPumpConfiguration configuration) throws NibeHeatPumpException;

    /**
     * Procedure for disconnect from heat pump.
     */
    void disconnect();

    /**
     * Procedure for register event listener.
     *
     * @param listener
     *            Event listener instance to handle events.
     */
    void addEventListener(NibeHeatPumpEventListener listener);

    /**
     * Procedure for remove event listener.
     *
     * @param listener
     *            Event listener instance to remove.
     */
    void removeEventListener(NibeHeatPumpEventListener listener);

    /**
     * Procedure for sending datagram to heat pump.
     *
     * @throws NibeHeatPumpException
     */
    void sendDatagram(NibeHeatPumpMessage msg) throws NibeHeatPumpException;

    /**
     * Procedure to check if connector is currently connected to heat pump.
     *
     * @return true, if connected
     */
    boolean isConnected();

}
