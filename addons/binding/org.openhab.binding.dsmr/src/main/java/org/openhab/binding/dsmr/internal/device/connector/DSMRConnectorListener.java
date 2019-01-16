/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dsmr.internal.device.connector;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Connector listener to handle connector events.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public interface DSMRConnectorListener {

    /**
     * Callback for {@link DSMRConnectorErrorEvent} events.
     *
     * @param portEvent {@link DSMRConnectorErrorEvent} that has occurred
     */
    public void handleErrorEvent(DSMRConnectorErrorEvent portEvent);

    /**
     * Handle data.
     *
     * @param buffer byte buffer with the data
     * @param length length of the data in the buffer. Buffer may be larger than data in buffer, therefore always use
     *            length
     */
    void handleData(byte[] buffer, int length);
}
