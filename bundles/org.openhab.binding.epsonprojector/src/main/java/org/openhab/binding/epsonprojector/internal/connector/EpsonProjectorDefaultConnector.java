/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.epsonprojector.internal.connector;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.epsonprojector.internal.EpsonProjectorException;

/**
 * Default Connector
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public class EpsonProjectorDefaultConnector implements EpsonProjectorConnector {

    public EpsonProjectorDefaultConnector() {
    }

    @Override
    public void connect() throws EpsonProjectorException {
        throw new EpsonProjectorException(
                "Epson Projector Binding is not configured correctly, please configure for Serial Port or TCP connection");
    }

    @Override
    public void disconnect() throws EpsonProjectorException {
        throw new EpsonProjectorException(
                "Epson Projector Binding is not configured correctly, please configure for Serial Port or TCP connection");
    }

    @Override
    public String sendMessage(String data, int timeout) throws EpsonProjectorException {
        throw new EpsonProjectorException(
                "Epson Projector Binding is not configured correctly, please configure for Serial Port or TCP connection");
    }
}
