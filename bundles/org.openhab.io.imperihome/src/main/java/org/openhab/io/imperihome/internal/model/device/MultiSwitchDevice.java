/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.io.imperihome.internal.model.device;

import java.util.Map;
import java.util.stream.Collectors;

import org.openhab.core.items.Item;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.types.State;
import org.openhab.io.imperihome.internal.model.param.DeviceParam;
import org.openhab.io.imperihome.internal.model.param.ParamType;

/**
 * MultiSwitch device, mimics behavior of an OH Switch with a mapping.
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

        DeviceParam choicesParam = new DeviceParam(ParamType.CHOICES,
                mapping.values().stream().collect(Collectors.joining(",")));
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
