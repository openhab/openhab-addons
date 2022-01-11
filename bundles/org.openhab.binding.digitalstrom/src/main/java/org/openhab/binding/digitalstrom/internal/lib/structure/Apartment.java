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
package org.openhab.binding.digitalstrom.internal.lib.structure;

import java.util.Map;

/**
 * The {@link Apartment} represents a digitalSTROM-Apartment.
 *
 * @author Alexander Betker - initial contributer
 * @author Michael Ochel - add java-doc
 * @author Matthias Siegele - add java-doc
 */
public interface Apartment {

    /**
     * Returns the {@link Map} of all digitalSTROM-Zones with the zone id as key and the {@link Zone} as value.
     *
     * @return map of all zones
     */
    Map<Integer, Zone> getZoneMap();
}
