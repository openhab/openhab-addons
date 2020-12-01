/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sony.internal;

import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.jupnp.model.types.UDN;

/**
 * Utility class for various UID type of operations
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class UidUtils {
    /**
     * Gets the device id from the UUID string ("[uuid:]{deviceid}::etc") from the specified {@link UDN}
     *
     * @param udn a non-null {@link UDN}
     * @return the device id or null (possibly empty) if not found
     */
    @Nullable
    public static String getDeviceId(final UDN udn) {
        Objects.requireNonNull(udn, "udn cannot be null");

        final String uuid = udn.getIdentifierString();

        final String[] uuidParts = StringUtils.split(uuid, ':');
        if (uuidParts == null || uuidParts.length == 0) {
            return null;
        } else if (uuidParts.length == 1) {
            return uuidParts[0]; // probably was just "{deviceid}" or "{deviceid}:etc"
        } else if (uuidParts.length == 2) {
            return uuidParts[1]; // probably was "uuid:{deviceid}.."
        }
        return uuid;
    }

    /**
     * Create a {@link ThingUID} for the specified {@link ThingTypeUID} and {@link UDN}
     *
     * @param udn a non-null {@link UDN}
     * @return a possibly null {@link ThingUID}
     */
    @Nullable
    public static String getThingId(final UDN udn) {
        Objects.requireNonNull(udn, "udn cannot be null");

        final String uuid = getDeviceId(udn);
        if (StringUtils.isEmpty(uuid)) {
            return null;
        }

        // Not entirely correct however most UUIDs are version 1
        // which makes the last node the mac address
        // Close enough to unique for our purposes - we just
        // verify the mac address is 12 characters in length
        // if not, we fall back to using the full uuid
        final String[] uuidParts = StringUtils.split(uuid, '-');
        final String macAddress = uuidParts[uuidParts.length - 1];
        return macAddress.length() == 12 ? macAddress : uuid;
    }

    /**
     * Create a {@link ThingUID} for the specified {@link ThingTypeUID} and {@link UDN}
     *
     * @param thingTypeId a non-null {@link ThingTypeUID}
     * @param udn a non-null {@link UDN}
     * @return a possibly null {@link ThingUID}
     */
    public static ThingUID createThingUID(final ThingTypeUID thingTypeId, final UDN udn) {
        Objects.requireNonNull(thingTypeId, "thingTypeId cannot be null");
        Objects.requireNonNull(udn, "udn cannot be null");
        return new ThingUID(thingTypeId, getThingId(udn));
    }
}
