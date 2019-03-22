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
