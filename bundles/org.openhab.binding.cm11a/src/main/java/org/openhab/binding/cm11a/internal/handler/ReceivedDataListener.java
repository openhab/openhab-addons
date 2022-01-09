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
package org.openhab.binding.cm11a.internal.handler;

import org.openhab.binding.cm11a.internal.X10ReceivedData;

/**
 * Interface to support listening for data received by the cm11a. This needs to be sent to OpenHAB so it has the current
 * status of the modules
 *
 * @author Bob Raker - Initial contribution
 *
 */
public interface ReceivedDataListener {

    /**
     * This is the method called when data is received from the cm11a
     *
     * @param rd
     */
    void receivedX10Data(X10ReceivedData rd);
}
