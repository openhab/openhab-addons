/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom.internal;

import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.messages.RFXComDeviceMessage;

/**
 * The {@link DeviceMessageListener} is notified when a message is received.
 *
 *
 * @author Pauli Anttila - Initial contribution
 */
public interface DeviceMessageListener {

    /**
     * This method is called whenever the message is received from the bridge.
     *
     * @param bridge
     *            The RFXCom bridge where message is received.
     * @param message
     *            The message which received.
     */
    void onDeviceMessageReceived(ThingUID bridge, RFXComDeviceMessage message) throws RFXComException;
}
