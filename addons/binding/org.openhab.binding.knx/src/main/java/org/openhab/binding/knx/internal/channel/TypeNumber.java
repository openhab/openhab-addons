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
import org.eclipse.smarthome.core.types.Type;

import tuwien.auto.calimero.GroupAddress;

class TypeNumber extends KNXChannelType {

    TypeNumber() {
        super(CHANNEL_NUMBER);
    }

    @Override
    public String getDPT(GroupAddress groupAddress, Configuration configuration) {
        return (String) configuration.get(DPT);
    }

    @Override
    protected Set<String> getReadAddressKeys() {
        return asSet(GROUPADDRESS);
    }

    @Override
    protected Set<String> getWriteAddressKeys(Type type) {
        return asSet(GROUPADDRESS);
    }

}
