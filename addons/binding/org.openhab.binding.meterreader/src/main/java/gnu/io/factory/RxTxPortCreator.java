/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package gnu.io.factory;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.RXTXPort;
import gnu.io.UnsupportedCommOperationException;

/**
 *
 * @author MatthiasS
 *
 */
public class RxTxPortCreator implements SerialPortCreator<RXTXPort> {

    @Override
    public boolean isApplicable(String portName, Class<RXTXPort> expectedClass) {
        return expectedClass.isAssignableFrom(RXTXPort.class);
    }

    @Override
    public RXTXPort createPort(String port)
            throws NoSuchPortException, UnsupportedCommOperationException, PortInUseException {
        RXTXPort comm = null;
        CommPortIdentifier ident = null;
        if ((System.getProperty("os.name").toLowerCase().indexOf("linux") != -1)) {
            // if ( port.toLowerCase().contains("rfcomm".toLowerCase())||
            // port.toLowerCase().contains("ttyUSB".toLowerCase()) ||
            // port.toLowerCase().contains("ttyS".toLowerCase())||
            // port.toLowerCase().contains("ACM".toLowerCase()) ||
            // port.toLowerCase().contains("Neuron_Robotics".toLowerCase())||
            // port.toLowerCase().contains("DyIO".toLowerCase())||
            // port.toLowerCase().contains("NR".toLowerCase())||
            // port.toLowerCase().contains("FTDI".toLowerCase())||
            // port.toLowerCase().contains("ftdi".toLowerCase())
            // ){
            SerialPortUtil.appendSerialPortProperty(port);
            // }
        }
        ident = CommPortIdentifier.getPortIdentifier(port);

        comm = ident.open("NRSerialPort", 2000);

        if (!(comm instanceof RXTXPort)) {
            throw new UnsupportedCommOperationException("Non-serial connections are unsupported.");
        }
        comm.enableReceiveTimeout(100);
        return comm;

    }

    @Override
    public String getProtocol() {
        return LOCAL;
    }

}
