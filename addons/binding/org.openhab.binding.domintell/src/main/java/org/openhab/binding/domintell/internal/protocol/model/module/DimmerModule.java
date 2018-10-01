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
import org.openhab.binding.domintell.internal.protocol.model.type.DataType;
import org.openhab.binding.domintell.internal.protocol.model.type.ItemType;
import org.openhab.binding.domintell.internal.protocol.model.type.ModuleType;

/**
* The {@link DimmerModule} class represents dimmer type Domintell modules
*
* @author Gabor Bicskei - Initial contribution
*/
public abstract class DimmerModule extends Module {
    DimmerModule(DomintellConnection connection, ModuleType type, SerialNumber serialNumber, int itemNum) {
        super(connection, type, serialNumber);

        //add channels
        for (int i = 0; i < itemNum; i++){
            addItem(i + 1, ItemType.numericVar, Integer.class);
        }
    }

    protected void updateItems(StatusMessage message) {
        String data = message.getData();
        if (message.getDataType() == DataType.D) {
            // 064 0 0 0 0 0 0
            getItems().values().forEach(i->{
                int idx = i.getItemKey().getIoNumber();
                String valueStr = data.substring(idx*2, 2*(idx + 1));
                Integer value =Integer.parseInt(valueStr, 16);
                i.setValue(value);
            });
        }
    }

    @Override
    public boolean isDiscoverable() {
        return true;
    }

    /**
     * Turn ON
     *
     * @param idx Contact index
     */
    public void on(int idx) {
        percent(idx, 100);
    }

    /**
     * Turn OFF
     *
     * @param idx Contact index
     */
    public void off(int idx) {
        percent(idx, 0);
    }

    /**
     * Set percent
     *
     * @param idx Contact index
     */
    public void percent(int idx, int percent) {
        getConnection().sendCommand(ActionMessageBuilder.create()
                .withModuleKey(getModuleKey())
                .withIONumber(idx)
                .withAction(ActionType.SET_DOMMER_OR_VOLUME)
                .withValue((double) percent)
                .build());
    }

    /**
     * Increase
     *
     * @param idx Contact index
     */
    public void increase(int idx) {
        getConnection().sendCommand(ActionMessageBuilder.create()
                .withModuleKey(getModuleKey())
                .withIONumber(idx)
                .withAction(ActionType.INCREASE_BY)
                .withValue(10d)
                .build());
    }

    /**
     * Increase
     *
     * @param idx Contact index
     */
    public void decrease(int idx) {
        getConnection().sendCommand(ActionMessageBuilder.create()
                .withModuleKey(getModuleKey())
                .withIONumber(idx)
                .withAction(ActionType.DECREASE_BY)
                .withValue(10d)
                .build());
    }
}
