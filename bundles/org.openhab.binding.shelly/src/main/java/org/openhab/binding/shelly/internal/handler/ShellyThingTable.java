/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

/***
 * The{@link ShellyThingTable} implements a simple table to allow dispatching incoming events to the proper thing
 * handler
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
@Component(service = ShellyThingTable.class, configurationPolicy = ConfigurationPolicy.OPTIONAL)
public class ShellyThingTable {
    private Map<String, ShellyThingInterface> thingTable = new ConcurrentHashMap<>();

    public void addThing(String key, ShellyThingInterface thing) {
        thingTable.put(key, thing);
    }

    public ShellyThingInterface getThing(String key) {
        ShellyThingInterface t = thingTable.get(key);
        if (t != null) {
            return t;
        }
        for (Map.Entry<String, ShellyThingInterface> entry : thingTable.entrySet()) {
            t = entry.getValue();
            if (t.checkRepresentation(key)) {
                return t;
            }
        }
        throw new IllegalArgumentException();
    }

    public void removeThing(String key) {
        if (thingTable.containsKey(key)) {
            thingTable.remove(key);
        }
    }

    public Map<String, ShellyThingInterface> getTable() {
        return thingTable;
    }

    public int size() {
        return thingTable.size();
    }
}
