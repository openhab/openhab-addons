/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nibeheatpump.internal.connection;

import static org.openhab.binding.nibeheatpump.NibeHeatPumpBindingConstants.*;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.nibeheatpump.internal.NibeHeatPumpException;

/**
 * The {@link ConnectorFactory} implements factory class to create Nibe connectors.
 *
 *
 * @author Pauli Anttila - Initial contribution
 */
public class ConnectorFactory {

    public static NibeHeatPumpConnector getConnector(ThingTypeUID type) throws NibeHeatPumpException {
        if (type != null) {

            if (THING_TYPE_F1X45_UDP.equals(type)) {
                return new UDPConnector();
            } else if (THING_TYPE_F1X45_SERIAL.equals(type)) {
                return new SerialConnector();
            } else if (THING_TYPE_F1X45_SIMULATOR.equals(type)) {
                return new SimulatorConnector();
            }
        }

        String description = String.format("Unknown connector type %s", type);
        throw new NibeHeatPumpException(description);
    }
}
