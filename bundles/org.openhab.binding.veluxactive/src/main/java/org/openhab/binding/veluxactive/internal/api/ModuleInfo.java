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
package org.openhab.binding.veluxactive.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Describes a supported module and its home, room and gateway associations for discovery and topology updates.
 *
 * Instances are assembled from the homesdata response by combining module data with the home's room and gateway
 * membership lists. The API supplies a default name for the module type when its reported name is absent or blank.
 *
 * @param homeId identifier of the home containing the module
 * @param moduleId identifier used to address the module in API requests
 * @param name reported module name, or the fallback name for its type
 * @param type API module type: NXG (gateway), NXS (indoor sensor), NXD (departure switch) or NXO (opening)
 * @param roomId reported or inferred room identifier; may be null or blank when unavailable
 * @param roomName name of the resolved room; may be null or blank when unavailable
 * @param bridgeId parent gateway's module identifier, or null when no gateway association can be resolved
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public record ModuleInfo(String homeId, String moduleId, String name, ModuleType type, @Nullable String roomId,
        @Nullable String roomName, @Nullable String bridgeId) {
}
