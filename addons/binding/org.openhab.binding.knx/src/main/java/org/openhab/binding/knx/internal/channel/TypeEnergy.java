/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.internal.channel;

import static org.openhab.binding.knx.KNXBindingConstants.*;

import java.util.Set;

import org.eclipse.smarthome.config.core.Configuration;

import tuwien.auto.calimero.GroupAddress;

class TypeEnergy extends KNXChannelType {

    TypeEnergy() {
        super(CHANNEL_ENERGY);
    }

    @Override
    public String getDPT(GroupAddress groupAddress, Configuration configuration) {
        String unit = (String) configuration.get(UNIT);
        switch (unit) {
            case "Wh":
                return "13.010";
            case "kWh":
                return "13.013";
        }
        return null;
    }

    @Override
    protected Set<String> getReadAddressKeys() {
        return asSet(ENERGY_GA);
    }

}
