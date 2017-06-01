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

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.types.Type;

import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.exception.KNXFormatException;

class TypeDimmer extends KNXChannelType {

    TypeDimmer() {
        super(CHANNEL_DIMMER);
    }

    @Override
    public String getDPT(GroupAddress groupAddress, Configuration configuration) throws KNXFormatException {
        if (isEquals(configuration, SWITCH_GA, groupAddress)) {
            return "1.001";
        }
        if (isEquals(configuration, STATUS_GA, groupAddress)) {
            return "1.001";
        }
        if (isEquals(configuration, POSITION_GA, groupAddress)) {
            return "5.001";
        }
        if (isEquals(configuration, POSITION_STATUS_GA, groupAddress)) {
            return "5.001";
        }
        if (isEquals(configuration, INCREASE_DECREASE_GA, groupAddress)) {
            return "3.007";
        }
        return null;
    }

    @Override
    protected Set<String> getReadAddressKeys() {
        return asSet(STATUS_GA, POSITION_GA);
    }

    @Override
    protected Set<String> getWriteAddressKeys(Type type) {
        if (type == null) {
            return asSet(SWITCH_GA, INCREASE_DECREASE_GA, POSITION_GA);
        } else {
            if (type instanceof OnOffType) {
                return asSet(SWITCH_GA);
            }
            if (type instanceof PercentType) {
                return asSet(POSITION_GA);
            }
            if (type instanceof IncreaseDecreaseType) {
                return asSet(INCREASE_DECREASE_GA);
            }
        }
        return Collections.emptySet();
    }

    @Override
    public Type convertType(Configuration configuration, Type type) {
        if (type instanceof OnOffType) {
            if (configuration.get(SWITCH_GA) != null) {
                return type;
            } else if (configuration.get(POSITION_GA) != null) {
                return ((OnOffType) type).as(PercentType.class);
            }
        }

        if (type instanceof PercentType) {
            if (configuration.get(POSITION_GA) != null) {
                return type;
            } else if (configuration.get(SWITCH_GA) != null) {
                return ((PercentType) type).as(OnOffType.class);
            }
        }

        return type;
    }
}
