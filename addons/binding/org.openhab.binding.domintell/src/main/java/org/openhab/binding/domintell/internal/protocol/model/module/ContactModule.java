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
import org.openhab.binding.domintell.internal.protocol.message.ActionMessageBuilder;
import org.openhab.binding.domintell.internal.protocol.message.StatusMessage;
import org.openhab.binding.domintell.internal.protocol.model.SerialNumber;
import org.openhab.binding.domintell.internal.protocol.model.type.ActionType;
import org.openhab.binding.domintell.internal.protocol.model.type.ItemType;
import org.openhab.binding.domintell.internal.protocol.model.type.ModuleType;

/**
* The {@link ContactModule} class represents contact type Domintell modules
*
* @author Gabor Bicskei - Initial contribution
*/
public abstract class ContactModule extends Module {
    ContactModule(DomintellConnection connection, ModuleType type, SerialNumber serialNumber, int contactNum) {
        super(connection, type, serialNumber);

        //add channels
        for (int i = 0; i < contactNum; i++){
            addItem(i + 1, ItemType.contact, Boolean.class);
        }
    }

    protected void updateItems(StatusMessage message) {
        updateBooleanItems(message);
    }

    @Override
    public boolean isDiscoverable() {
        return true;
    }

    /**
     * Simulate short push on contact
     *
     * @param idx Contact index
     */
    public void shortPush(int idx) {
        getConnection().sendCommand(ActionMessageBuilder.create()
                .withModuleKey(getModuleKey())
                .withIONumber(idx)
                .withAction(ActionType.SHORT_PUSH)
                .build());
    }

    /**
     * Simulate long push on contact
     *
     * @param idx Contact index
     */
    public void longPush(int idx) {
        getConnection().sendCommand(ActionMessageBuilder.create()
                .withModuleKey(getModuleKey())
                .withIONumber(idx)
                .withAction(ActionType.LONG_PUSH)
                .build());
    }
}
