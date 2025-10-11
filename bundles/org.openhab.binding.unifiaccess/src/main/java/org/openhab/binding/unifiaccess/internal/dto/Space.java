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
 * Space topology (Section 7 "Space"): buildings, floors, doors.
 *
 *
 * @author Dan Cunningham - Initial contribution
 */
public class Space {
    /** Top-level grouping, e.g., a “building”. */
    public String id;
    public String name;
    public String type; // e.g., "building"
    public List<ResourceTopology> resourceTopologies;

    /** A floor within a Space. */
    public static class ResourceTopology {
        public String id;
        public String name;
        public String type; // "floor"
        public List<Resource> resources;
    }

    /** A door resource on a floor. */
    public static class Resource {
        public String id;
        public String name;
        public String type; // "door"
        public Boolean isBindHub; // true if the door is bound to a hub
    }
}
