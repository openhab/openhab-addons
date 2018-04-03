/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pioneeravr.internal.handler;

import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.pioneeravr.PioneerAvrBindingConstants;
import org.openhab.binding.pioneeravr.internal.protocol.serial.SerialAvrConnection;
import org.openhab.binding.pioneeravr.protocol.AvrConnection;

/**
 * An handler of an AVR connected through a serial port.
 * 
 * @author Antoine Besnard
 *
 */
public class SerialAvrHandler extends AbstractAvrHandler {

    public SerialAvrHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected AvrConnection createConnection() {
        String serialPort = (String) this.getConfig().get(PioneerAvrBindingConstants.SERIAL_PORT_PARAMETER);

        return new SerialAvrConnection(serialPort);
    }

}
