/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.hue.internal.dto.clip2;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hue.internal.dto.clip2.enums.ResourceType;

/**
 * DTO that contains an API reference element.
 *
 * The V2 API is set up in such a way that all resources of the same type are grouped together under the
 * {@code /resource/<resourcetype>} endpoint, but all those resources commonly reference each other, which is done in a
 * standardized way by indicating the resource type (rtype) and resource id (rid).
 *
 * A typical usage is in a single physical device that hosts multiple services. An existing example is the Philips Hue
 * Motion sensor which has a motion, light_level, and temperature service, but theoretically any combination can be
 * supported such as an integrated device with two independently controllable light points and a motion sensor.
 *
 * This means that the information of the device itself can be found under the /device resource endpoint, but it then
 * contains a services array which references for example the light and motion resources, for which the details can be
 * found under the /light and /motion resource endpoints respectively. Other services the device might have, such as a
 * Zigbee radio (zigbee_connectivy) or battery (device_power) are modeled in the same way.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class ResourceReference {
    private @Nullable String rid;
    private @NonNullByDefault({}) String rtype;

    @Override
    public boolean equals(@Nullable Object obj) {
        String rid = this.rid;
        return (obj instanceof ResourceReference) && (rid != null) && rid.equals(((ResourceReference) obj).rid);
    }

    public @Nullable String getId() {
        return rid;
    }

    public ResourceType getType() {
        return ResourceType.of(rtype);
    }

    public ResourceReference setId(String id) {
        rid = id;
        return this;
    }

    public ResourceReference setType(ResourceType resourceType) {
        rtype = resourceType.name().toLowerCase();
        return this;
    }

    @Override
    public String toString() {
        String id = rid;
        return String.format("id:%s, type:%s", id != null ? id : "*" + " ".repeat(35), getType().name().toLowerCase());
    }
}
