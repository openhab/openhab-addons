/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.regoheatpump.internal.handler;

import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.regoheatpump.internal.RegoHeatPumpBindingConstants;
import org.openhab.binding.regoheatpump.internal.protocol.RegoConnection;
import org.openhab.binding.regoheatpump.internal.protocol.SerialRegoConnection;

/**
 * The {@link SerialHusdataHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Boris Krivonog - Initial contribution
 */
public class SerialHusdataHandler extends HusdataHandler {
    public SerialHusdataHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected RegoConnection createConnection() {
        String portName = (String) getConfig().get(RegoHeatPumpBindingConstants.PORT_NAME);
        return new SerialRegoConnection(portName, 19200);
    }
}
