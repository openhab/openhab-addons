/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
public record NAPushType(ModuleType moduleType, EventType event) {
    public static final NAPushType UNKNOWN = new NAPushType(ModuleType.UNKNOWN, EventType.UNKNOWN);
}
