/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
import org.openhab.binding.smartthings.internal.handler.SmartthingsThingHandler;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base converter class.
 * The converter classes are responsible for converting "state" messages from the smartthings hub into openHAB States.
 * And, converting handler.handleCommand() into messages to be sent to smartthings
 *
 * @author Bob Raker - Initial contribution
 */
@NonNullByDefault
public abstract class SmartthingsStateHandler {
    private final Logger logger = LoggerFactory.getLogger(SmartthingsStateHandler.class);
    protected Map<String, State> stateCache = new Hashtable<String, State>();

    SmartthingsStateHandler() {
    }

    public void handleStateChange(ChannelUID channelUID, String deviceType, String componentId, State state,
            SmartthingsThingHandler thingHandler) {
    }

    protected PercentType convToPercentTypeIfNeed(State state) {
        if (state instanceof PercentType pc) {
            return pc;
        } else if (state instanceof DecimalType dec) {
            return new PercentType(new BigDecimal(dec.doubleValue()));
        } else {
            logger.info("");
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
