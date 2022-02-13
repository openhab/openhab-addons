/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.danfossairunit.internal;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This interface defines a communication controller that can be used to send requests to the Danfoss Air Unit.
 * 
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public interface CommunicationController {
    void connect() throws IOException;

    void disconnect();

    byte[] sendRobustRequest(byte[] operation, byte[] register) throws IOException;

    byte[] sendRobustRequest(byte[] operation, byte[] register, byte[] value) throws IOException;
}
