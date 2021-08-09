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

import org.openhab.core.thing.ThingUID;

/**
 * Service for keeping track of things created by this binding.
 *
 * This is used to prevent repeated discovery of existing Nuki Bridge, which
 * causes overwriting of existing bridge's properties.
 *
 * @author Jan Vybíral - Initial contribution
 */
public interface NukiThingRegistryService {

    void thingCreated(ThingUID uuid);

    void thingDestroyed(ThingUID uuid);

    boolean thingExists(ThingUID uuid);
}
