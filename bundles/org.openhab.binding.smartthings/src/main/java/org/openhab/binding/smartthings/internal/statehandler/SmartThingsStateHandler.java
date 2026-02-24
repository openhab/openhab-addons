/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.smartthings.internal.statehandler;

import java.math.BigDecimal;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.smartthings.internal.handler.SmartThingsThingHandler;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Base converter class.
 * The converter classes are responsible for converting "state" messages from the SmartThings into openHAB States.
 * And, converting handler.handleCommand() into messages to be sent to SmartThings
 *
 * @author Bob Raker - Initial contribution
 */
@NonNullByDefault
public abstract class SmartThingsStateHandler {
    protected Map<String, State> stateCache = new Hashtable<String, State>();

    SmartThingsStateHandler() {
    }

    public void handleStateChange(ChannelUID channelUID, String deviceType, String componentId, State state,
            SmartThingsThingHandler thingHandler) {
    }

    protected PercentType convToPercentTypeIfNeed(State state) {
        if (state instanceof PercentType pc) {
            return pc;
        } else if (state instanceof DecimalType dec) {
            return new PercentType(new BigDecimal(dec.doubleValue()));
        } else {
            return new PercentType(0);
        }
    }

    public State getState(String key) {
        if (stateCache.containsKey(key)) {
            State result = stateCache.get(key);
            if (result != null) {
                return result;
            }
        }

        return UnDefType.UNDEF;
    }
}
