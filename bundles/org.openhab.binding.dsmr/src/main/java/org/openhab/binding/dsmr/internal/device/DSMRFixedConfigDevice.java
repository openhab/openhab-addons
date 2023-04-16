/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.dsmr.internal.device;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.dsmr.internal.device.connector.DSMRSerialConnector;
import org.openhab.binding.dsmr.internal.device.connector.DSMRSerialSettings;
import org.openhab.binding.dsmr.internal.device.p1telegram.P1TelegramListener;
import org.openhab.core.io.transport.serial.SerialPortManager;

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
     * Constructor.
     *
     * @param serialPortManager the manager to get a new serial port connecting from
     * @param serialPortName the port name (e.g. /dev/ttyUSB0 or COM1)
     * @param fixedPortSettings The serial port connection settings
     * @param listener the parent {@link P1TelegramListener}
     * @param telegramListener listener to report found telegrams or errors
     */
    public DSMRFixedConfigDevice(final SerialPortManager serialPortManager, final String serialPortName,
            final DSMRSerialSettings fixedPortSettings, final P1TelegramListener listener,
            final DSMRTelegramListener telegramListener) {
        this.fixedPortSettings = fixedPortSettings;
        this.telegramListener = telegramListener;
        telegramListener.setP1TelegramListener(listener);

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
    public void setLenientMode(final boolean lenientMode) {
        telegramListener.setLenientMode(lenientMode);
    }
}
