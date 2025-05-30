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
package org.openhab.binding.lgthinq.internal.handler;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqException;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.BaseThingHandler;

/**
 * The {@link BaseThingWithExtraInfoHandler} contains method definitions to the Handle be able to work
 * with extra info data.
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public abstract class BaseThingWithExtraInfoHandler extends BaseThingHandler {
    /**
     * Creates a new instance of this class for the {@link Thing}.
     *
     * @param thing the thing that should be handled, not null
     */
    public BaseThingWithExtraInfoHandler(Thing thing) {
        super(thing);
    }

    /**
     * Handle must implement this method to update device's extra information collected to the respective channels.
     *
     * @param energyStateAttributes map containing the key and values collected
     */
    protected void updateExtraInfoStateChannels(Map<String, Object> energyStateAttributes) {
        throw new UnsupportedOperationException(
                "Method must be implemented in the Handle that supports energy collector. It is most likely a bug");
    }

    /**
     * Must be implemented with the code to get energy state if the thing supports it.
     *
     * @return map containing energy state attributes
     */
    protected Map<String, Object> collectExtraInfoState() throws LGThinqException {
        throw new UnsupportedOperationException(
                "Method must be implemented in the Handle that supports energy collector. It is most likely a bug");
    }

    /**
     * Reset (put in UNDEF) the channels related to extra information. Normally called when the collector stops.
     */
    protected void resetExtraInfoChannels() {
    }
}
