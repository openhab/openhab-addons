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

import com.google.gson.annotations.SerializedName;

/**
 * Access Policy model for UniFi Access API (Section 5.1).
 *
 * @author Dan Cunningham - Initial contribution
 */
public class AccessPolicy {
    public String id;
    public String name;
    @SerializedName(value = "resources", alternate = { "resource" })
    public List<Resource> resources;
    public String scheduleId;

    public static class Resource {
        public String id;
        public ResourceType type;
    }

    public enum ResourceType {
        @SerializedName("door")
        DOOR,
        @SerializedName("door_group")
        DOOR_GROUP
    }
}
