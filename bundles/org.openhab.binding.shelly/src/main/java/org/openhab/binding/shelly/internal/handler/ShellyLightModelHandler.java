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
package org.openhab.binding.shelly.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link ShellyLightModelHandler} interface is supported by Shelly thing handlers that own
 * a set of one or more {@link ShellyLightModel}'s.
 * 
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public interface ShellyLightModelHandler {

    /**
     * Get the light model for the given modelId
     * 
     * @param componentIndex the component index of the light within the device.
     * @return the light model, or null if not found.
     */
    @Nullable
    ShellyLightModel getLightModel(int componentIndex);

    /**
     * Acquire the lock for the light models. This means that other callers cannot modify the light
     * models until the lock is released.
     */
    void acquireLock();

    /**
     * Release the lock, and check if any of the models are dirty.
     * 
     * @return true if any of the models are dirty, false otherwise.
     */
    boolean releaseLock();
}
