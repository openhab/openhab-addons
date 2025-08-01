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

import com.google.gson.JsonObject;

/**
 * The {@link EvccThingLifecycleAware} is responsible for sharing the evcc api response
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public interface EvccThingLifecycleAware {
    /**
     * This method shall update the channels from the JSON received from the evcc API
     * 
     * @param state the responded JSON
     */
    void updateFromEvccState(JsonObject state);

    /**
     * This method shall return the to the thing corresponding JSON object
     * 
     * @param state the cached API JSON response
     * @return to the thing corresponding JSON object
     */
    JsonObject getStateFromCachedState(JsonObject state);
}
