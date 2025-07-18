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
package org.openhab.binding.evcc.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.type.ChannelTypeRegistry;

import com.google.gson.JsonObject;

/**
 * The {@link EvccHeatingHandler} is responsible for fetching the data from the API response for Heating things
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class EvccHeatingHandler extends EvccLoadpointHandler {

    public EvccHeatingHandler(Thing thing, ChannelTypeRegistry channelTypeRegistry) {
        super(thing, channelTypeRegistry);
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    @Override
    public void updateFromEvccState(JsonObject state) {
        super.updateFromEvccState(state);
    }
}
