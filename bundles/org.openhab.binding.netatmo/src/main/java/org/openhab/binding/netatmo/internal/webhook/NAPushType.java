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
package org.openhab.binding.netatmo.internal.webhook;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.api.doc.EventType;
import org.openhab.binding.netatmo.internal.api.doc.ModuleType;

/**
 * This class holds informations of push_type field
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class NAPushType {
    private final ModuleType moduleType;
    private final EventType event;

    public NAPushType(ModuleType moduleType, EventType event) {
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
