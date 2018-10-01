/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.domintell.internal.protocol.model.module;

import org.openhab.binding.domintell.internal.protocol.DomintellConnection;
import org.openhab.binding.domintell.internal.protocol.message.StatusMessage;
import org.openhab.binding.domintell.internal.protocol.model.SerialNumber;
import org.openhab.binding.domintell.internal.protocol.model.type.DataType;
import org.openhab.binding.domintell.internal.protocol.model.type.ModuleType;

/**
 * The {@link PushButtonModule} class is a base class of all push button modules
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class PushButtonModule extends ContactModule {
    /**
     * Constructor
     *
     * @param connection Connection
     * @param serialNumber Module serial number
     */
    PushButtonModule(DomintellConnection connection, ModuleType type, SerialNumber serialNumber, int ioNumber) {
        super(connection, type, serialNumber, ioNumber);
    }

    @Override
    public boolean isDiscoverable() {
        return true;
    }

    @Override
    protected void updateItems(StatusMessage message) {
        //dropping all messages for indicators
        Integer ioNumber = message.getIoNumber();
        if (message.getDataType() != DataType.O || (ioNumber != null && ioNumber >= getItems().size())) {
            super.updateItems(message);
        }
    }
}
