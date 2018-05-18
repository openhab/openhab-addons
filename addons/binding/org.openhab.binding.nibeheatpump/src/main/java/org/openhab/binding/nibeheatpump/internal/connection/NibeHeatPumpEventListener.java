/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
