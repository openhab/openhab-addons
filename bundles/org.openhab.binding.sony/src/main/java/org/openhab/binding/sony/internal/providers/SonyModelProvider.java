/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sony.internal.providers;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * Defines the contract for any provider that wished to manage model to thing type relations (and provide callback to a
 * listener when that relationship changes)
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public interface SonyModelProvider {
    /**
     * Adds a listener for the given model name (that uses the current thingtypeuid)
     * 
     * @param modelName a non-null, non-empty model name
     * @param currentThingTypeUID a non-null current thing type uid
     * @param listener a non-null listener
     */
    void addListener(String modelName, ThingTypeUID currentThingTypeUID, SonyModelListener listener);

    /**
     * Removes a listener from this source
     * 
     * @param listener a non-null listener to remove
     * @return true if removed, false otherwise
     */
    boolean removeListener(SonyModelListener listener);
}
