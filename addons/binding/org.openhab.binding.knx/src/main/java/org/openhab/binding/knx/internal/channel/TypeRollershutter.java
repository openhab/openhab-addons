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
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.types.Type;

import com.google.common.collect.Sets;

import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.exception.KNXFormatException;

class TypeRollershutter extends KNXChannelType {

    TypeRollershutter() {
        super(CHANNEL_ROLLERSHUTTER);
    }

    @Override
    public String getDPT(GroupAddress groupAddress, Configuration configuration) throws KNXFormatException {
        if (isEquals(configuration, UP_DOWN_GA, groupAddress)) {
            return "1.008";
        }
        if (isEquals(configuration, UP_DOWN_STATUS_GA, groupAddress)) {
            return "1.008";
        }
        if (isEquals(configuration, STOP_MOVE_GA, groupAddress)) {
            return "1.010";
        }
        if (isEquals(configuration, STOP_MOVE_STATUS_GA, groupAddress)) {
            return "1.010";
        }
        if (isEquals(configuration, POSITION_GA, groupAddress)) {
            return "5.001";
        }
        if (isEquals(configuration, POSITION_STATUS_GA, groupAddress)) {
            return "5.001";
        }
        throw new IllegalArgumentException("Group address " + groupAddress + " is none of the configured addresses");
    }

    @Override
    protected Set<String> getReadAddressKeys() {
        return asSet(UP_DOWN_STATUS_GA, STOP_MOVE_STATUS_GA, POSITION_STATUS_GA);
    }

    @Override
    protected Set<String> getWriteAddressKeys(Type type) {
        if (type == null) {
            return asSet(UP_DOWN_GA, STOP_MOVE_GA, POSITION_GA);
        } else {
            if (type instanceof UpDownType) {
                return asSet(UP_DOWN_GA);
            }
            if (type instanceof PercentType) {
                return asSet(POSITION_GA);
            }
            if (type instanceof StopMoveType) {
                return asSet(STOP_MOVE_GA);
            }
        }
        return Sets.newHashSet();
    }

    @Override
    public Type convertType(Configuration configuration, Type type) {
        if (type instanceof UpDownType) {
            if (configuration.get(UP_DOWN_GA) != null) {
                return type;
            } else if (configuration.get(POSITION_GA) != null) {
                return ((UpDownType) type).as(PercentType.class);
            }
        }

        if (type instanceof PercentType) {
            if (configuration.get(POSITION_GA) != null) {
                return type;
            } else if (configuration.get(UP_DOWN_GA) != null) {
                return ((PercentType) type).as(UpDownType.class);
            }
        }

        return type;
    }
}
