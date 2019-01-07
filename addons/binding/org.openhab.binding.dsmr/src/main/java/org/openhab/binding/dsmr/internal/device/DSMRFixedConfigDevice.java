/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dsmr.internal.device;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;
import org.openhab.binding.dsmr.internal.device.connector.DSMRSerialConnector;
import org.openhab.binding.dsmr.internal.device.connector.DSMRSerialSettings;

/**
 * Implementation of a DSMRDevice with fixed serial port settings. With fixed port settings the code is much simpler
 * because no detecting of settings needs to be done and when things fail no redirecting is needed either.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public class DSMRFixedConfigDevice implements DSMRDevice {

    private final DSMRSerialConnector dsmrPort;
    private final DSMRSerialSettings fixedPortSettings;
    private final DSMRTelegramListener telegramListener;

    /**
     * Constructor
     *
     * @param serialPortManager the manager to get a new serial port connecting from
     * @param serialPortName the port name (e.g. /dev/ttyUSB0 or COM1)
     * @param fixedPortSettings The serial port connection settings
     * @param listener the parent {@link DSMREventListener}
     */
    public DSMRFixedConfigDevice(SerialPortManager serialPortManager, String serialPortName,
            DSMRSerialSettings fixedPortSettings, DSMREventListener listener) {
        this.fixedPortSettings = fixedPortSettings;
        telegramListener = new DSMRTelegramListener(listener);
        dsmrPort = new DSMRSerialConnector(serialPortManager, serialPortName, telegramListener);
    }

    @Override
    public void start() {
        dsmrPort.open(fixedPortSettings);
    }

    @Override
    public void restart() {
        dsmrPort.restart(fixedPortSettings);
    }

    @Override
    public void stop() {
        dsmrPort.close();
    }

    @Override
    public void setLenientMode(boolean lenientMode) {
        telegramListener.setLenientMode(lenientMode);
    }
}
