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
package org.openhab.binding.netatmo.internal.deserialization;

import java.util.HashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.api.dto.NAThing;

/**
 * The {@link NADynamicObjectMap} defines an hashmap of NAObjects identified
 * by their id, dynamically created upon API response
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class NADynamicObjectMap extends HashMap<String, NAThing> {
    private static final long serialVersionUID = -7864636414965562293L;

    // TODO Remove unused code found by UCDetector
    // public List<NAThing> forModuleType(ModuleType searchedType) {
    // List<NAThing> result = new ArrayList<>();
    // result.addAll(values().stream().filter(thing -> thing.getType() == searchedType).collect(Collectors.toList()));
    // return result;
    // }
}
