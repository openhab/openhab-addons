/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.unifiaccess.internal.dto;

import java.util.List;

/**
 * Door Group (a.k.a. Space Group) composed of door resources.
 *
 * <p>
 * Use for create/fetch/update of door groups. Complements the topology view.
 * </p>
 *
 * @author Dan Cunningham - Initial contribution
 */
public class DoorGroup {
    public String id;
    public String name;
    /**
     * Group type: examples include "access" and "building".
     * Kept as String to remain forward-compatible.
     */
    public String type;

    public List<Resource> resources;

    public static class Resource {
        public String id;
        public String name;
        /** Expected "door" but kept as String for forward-compatibility. */
        public String type;
    }

    public boolean hasDoors() {
        return resources != null && !resources.isEmpty();
    }
}
