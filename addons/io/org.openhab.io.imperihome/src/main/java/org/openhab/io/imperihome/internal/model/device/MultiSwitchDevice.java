/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.imperihome.internal.model.device;

import java.util.Map;

import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.io.imperihome.internal.model.param.DeviceParam;
import org.openhab.io.imperihome.internal.model.param.ParamType;

import com.google.common.base.Joiner;

/**
 * MultiSwitch device, mimics behavior of a OH Switch with a mapping.
 *
 * @author Pepijn de Geus - Initial contribution
 */
public class MultiSwitchDevice extends AbstractDevice {

    private String itemValue = "";

    public MultiSwitchDevice(Item item) {
        super(DeviceType.MULTI_SWITCH, item);
    }

    @Override
    public void updateParams() {
        super.updateParams();

        Map<String, String> mapping = getMapping();
        if (mapping == null || mapping.isEmpty()) {
            logger.error("MultiSwitch device {} contains no mapping", this);
            return;
        }

        DeviceParam choicesParam = new DeviceParam(ParamType.CHOICES, Joiner.on(',').join(mapping.values()));
        addParam(choicesParam);

        // Find current value text
        String currentValue = "";
        if (mapping.containsKey(itemValue)) {
            currentValue = mapping.get(itemValue);
        }

        DeviceParam valueParam = new DeviceParam(ParamType.MULTISWITCH_VALUE, currentValue);
        addParam(valueParam);
    }

    @Override
    public void stateUpdated(Item item, State newState) {
        super.stateUpdated(item, newState);

        State state = item.getStateAs(DecimalType.class);
        if (state instanceof DecimalType) {
            itemValue = String.valueOf(((DecimalType) state).intValue());
        }
    }

}
