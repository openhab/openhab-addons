/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.nuki.internal.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;

/**
 * Implementation of NukiThingRegistryService which saves set of existing
 * things in memory.
 *
 * @author Jan Vybíral - Initial contribution
 */
@Component(service = NukiThingRegistryService.class)
public class InMemoryNukiThingRegistryService implements NukiThingRegistryService {

    private final Set<ThingUID> existingThings = Collections.synchronizedSet(new HashSet<>());

    @Override
    public void thingCreated(ThingUID uuid) {
        this.existingThings.add(uuid);
    }

    @Override
    public void thingDestroyed(ThingUID uuid) {
        this.existingThings.remove(uuid);
    }

    @Override
    public boolean thingExists(ThingUID uuid) {
        return this.existingThings.contains(uuid);
    }
}
