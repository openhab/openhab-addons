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
package org.openhab.binding.yamahareceiver.internal.protocol;

import java.io.IOException;

/**
 * To offer a method to retrieve a specific state, a protocol part would extend this interface.
 *
 * @author David Graeff - Initial contribution
 */
public interface IStateUpdatable {
    /**
     * Updates the corresponding state. This method is blocking.
     *
     * @throws IOException If the device is offline this exception will be thrown
     * @throws ReceivedMessageParseException If the response cannot be parsed correctly this exception is thrown
     */
    void update() throws IOException, ReceivedMessageParseException;
}
