/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dsmr.internal.device;

import java.util.List;

import org.openhab.binding.dsmr.internal.device.DSMRDeviceConstants.DSMRPortEvent;
import org.openhab.binding.dsmr.internal.device.cosem.CosemObject;

/**
 * Interface for handling DSMRPortEvent events
 *
 * @author M. Volaart
 * @since 2.1.0
 */
public interface DSMRPortEventListener {
    /**
     * Callback for DSMRPortEvent events
     *
     * @param portEvent {@link DSMRPortEvent} that has occurred
     */
    public void handleDSMRPortEvent(DSMRPortEvent portEvent);

    /**
     * Callback for received P1 telegrams
     *
     * @param cosemObjects List containing the individual data elements of a received P1 telegram
     * @param telegramDetails the details about the received telegram
     */
    public void P1TelegramReceived(List<CosemObject> cosemObjects, String telegramDetails);
}
