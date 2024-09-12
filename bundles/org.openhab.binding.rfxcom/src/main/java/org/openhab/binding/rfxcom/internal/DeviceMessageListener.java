/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.rfxcom.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.messages.RFXComDeviceMessage;
import org.openhab.core.thing.ThingUID;

/**
 * The {@link DeviceMessageListener} is notified when a message is received.
 *
 *
 * @author Pauli Anttila - Initial contribution
 */
@NonNullByDefault
public interface DeviceMessageListener {

    /**
     * This method is called whenever the message is received from the bridge.
     *
     * @param bridge The RFXCom bridge where message is received.
     * @param message The message which received.
     */
    void onDeviceMessageReceived(ThingUID bridge, RFXComDeviceMessage message) throws RFXComException;
}
