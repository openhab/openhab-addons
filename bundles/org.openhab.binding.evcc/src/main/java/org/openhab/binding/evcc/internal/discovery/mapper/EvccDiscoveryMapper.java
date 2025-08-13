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
package org.openhab.binding.evcc.internal.discovery.mapper;

import java.util.Collection;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.evcc.internal.handler.EvccBridgeHandler;
import org.openhab.core.config.discovery.DiscoveryResult;

import com.google.gson.JsonObject;

/**
 * The {@link EvccDiscoveryMapper} is responsible mapping discovered things to discovery results
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public interface EvccDiscoveryMapper {
    /**
     * This method returns the discovered things from the JSON response
     * 
     * @param state the JSON response from the API
     * @param bridgeHandler the bridge handler
     * @return Collection of discovery results
     */
    Collection<DiscoveryResult> discover(JsonObject state, EvccBridgeHandler bridgeHandler);
}
