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
package org.openhab.binding.regoheatpump.internal.handler;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.regoheatpump.internal.RegoHeatPumpBindingConstants;
import org.openhab.binding.regoheatpump.internal.protocol.RegoConnection;
import org.openhab.binding.regoheatpump.internal.protocol.SerialRegoConnection;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;

/**
 * The {@link SerialRego6xxHeatPumpHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Boris Krivonog - Initial contribution
 */
@NonNullByDefault
public class SerialRego6xxHeatPumpHandler extends Rego6xxHeatPumpHandler {
    private final SerialPortManager serialPortManager;
    private @Nullable SerialPortIdentifier serialPortIdentifier;

    public SerialRego6xxHeatPumpHandler(Thing thing, SerialPortManager serialPortManager) {
        super(thing);
        this.serialPortManager = serialPortManager;
    }

    @Override
    public void initialize() {
        String portName = (String) getConfig().get(RegoHeatPumpBindingConstants.PORT_NAME);
        serialPortIdentifier = serialPortManager.getIdentifier(portName);
        if (serialPortIdentifier == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Serial port does not exist: " + portName);
        } else {
            super.initialize();
        }
    }

    @Override
    protected RegoConnection createConnection() throws IOException {
        SerialPortIdentifier serialPortIdentifier = this.serialPortIdentifier;
        if (serialPortIdentifier == null) {
            throw new IOException("Serial port does not exist");
        }
        return new SerialRegoConnection(serialPortIdentifier, 19200);
    }
}
