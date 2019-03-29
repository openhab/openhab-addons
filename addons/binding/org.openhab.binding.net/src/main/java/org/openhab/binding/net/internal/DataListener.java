/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.net.internal;

import org.eclipse.smarthome.core.thing.ThingUID;

/**
 * The {@link DataListener} is notified when a message is received.
 *
 *
 * @author Pauli Anttila - Initial contribution
 */
public interface DataListener {

    /**
     * This method is called whenever the message is received from the bridge.
     *
     * @param bridge
     *                   The bridge where message is received.
     * @param data
     *                   Received data.
     */
    void dataReceived(ThingUID bridge, Object data);
}
