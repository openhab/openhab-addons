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

package org.openhab.binding.openwebnet.internal.serial;

import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openwebnet4j.communication.serial.spi.SerialPort;
import org.openwebnet4j.communication.serial.spi.SerialPortProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aQute.bnd.annotation.spi.ServiceProvider;

/**
 * openwebnet4j SerialPortProvider implementation based on OH serial transport
 *
 * @author M. Valla - Initial contribution
 */
@ServiceProvider(value = SerialPortProvider.class)
@NonNullByDefault
public class SerialPortProviderAdapter implements SerialPortProvider {

    private Logger logger = LoggerFactory.getLogger(SerialPortProviderAdapter.class);
    @Nullable
    public static SerialPortManager serialPortManager = null;

    public static void setSerialPortManager(SerialPortManager serialPortManager) {
        SerialPortProviderAdapter.serialPortManager = serialPortManager;
    }

    @Override
    public @Nullable SerialPort getSerialPort(String portName) {
        final @Nullable SerialPortManager spm = serialPortManager;
        if (spm == null) {
            return null;
        }
        SerialPortIdentifier spid = spm.getIdentifier(portName);
        if (spid == null) {
            logger.debug("No SerialPort {} found", portName);
            return null;
        } else {
            return new SerialPortAdapter(spid);
        }
    }

    @Override
    public Stream<SerialPort> getSerialPorts() {
        final @Nullable SerialPortManager spm = serialPortManager;
        if (spm == null) {
            return Stream.empty();
        }
        return spm.getIdentifiers().map(sid -> new SerialPortAdapter(sid));
    }
}
