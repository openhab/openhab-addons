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
package org.openhab.binding.proteusecometer.internal.serialport;

import java.io.InputStream;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Abstract over serial port implementations
 *
 * @author Matthias Herrmann - Initial contribution
 *
 */
@NonNullByDefault
public interface SerialPortService {
    public InputStream getInputStream(String portId, int baudRate, int numDataBits, int numStopBits, int parity);
}
