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
package org.openhab.binding.netatmo.internal.deserialization;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.api.data.EventType;
import org.openhab.binding.netatmo.internal.api.data.ModuleType;

/**
 * This class holds data of push_type field
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class NAPushType {
    public final static NAPushType UNKNOWN = new NAPushType(ModuleType.UNKNOWN, EventType.UNKNOWN);

    private final ModuleType moduleType;
    private final EventType event;

    NAPushType(ModuleType moduleType, EventType event) {
        this.moduleType = moduleType;
        this.event = event;
    }

    public ModuleType getModuleType() {
        return moduleType;
    }

    public EventType getEvent() {
        return event;
    }
}
