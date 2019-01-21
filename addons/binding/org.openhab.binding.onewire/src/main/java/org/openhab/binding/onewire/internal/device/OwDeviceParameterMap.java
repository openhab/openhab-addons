/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.onewire.internal.device;

import java.util.HashMap;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link OwDeviceParameterMap} stores bridge specific implementation details of a device
 *
 * @author Jan N. Klug - Initial contribution
 */
public class OwDeviceParameterMap {

    private final HashMap<ThingTypeUID, OwDeviceParameter> map = new HashMap<>();

    /**
     * sets (or replaces) implementation details for a given bridge
     *
     * @param thingTypeUID the bridges thing type UID
     * @param owDeviceParameter the information for this bridge
     */
    public void set(ThingTypeUID thingTypeUID, OwDeviceParameter owDeviceParameter) {
        map.put(thingTypeUID, owDeviceParameter);
    }

    /**
     * gets implementation details for a given bridge
     *
     * @param thingTypeUID the bridges thing type UID
     * @return the information for this bridge
     */
    public OwDeviceParameter get(ThingTypeUID thingTypeUID) {
        return map.get(thingTypeUID);
    }
}
