/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.cm11a.handler;

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
