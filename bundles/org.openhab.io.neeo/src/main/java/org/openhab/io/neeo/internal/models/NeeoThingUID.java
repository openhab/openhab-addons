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
package org.openhab.io.neeo.internal.models;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingUID;
import org.openhab.io.neeo.internal.NeeoConstants;

/**
 * Wrapper around a {@link ThingUID} to provide common initialization and to provide a non deprecated way to get the
 * thing type (which is the second element)
 *
 * @author Tim Roberts - Initial Contribution
 *
 */
@NonNullByDefault
public class NeeoThingUID extends ThingUID {
    /**
     * Constructs the {@link NeeoThingUID} from a {@link ThingUID}
     *
     * @param uid the thingUID
     */
    public NeeoThingUID(ThingUID uid) {
        super(uid.toString());
    }

    /**
     * Construct the {@link NeeoThingUID} from the string representation of a UID. If the string representation includes
     * {@link NeeoConstants#NEEO_ADAPTER_PREFIX}, that prefix will be removed.
     *
     * @param thingId the thing ID
     */
    public NeeoThingUID(String thingId) {
        super(thingId.startsWith(NeeoConstants.NEEO_ADAPTER_PREFIX)
                ? thingId.substring(NeeoConstants.NEEO_ADAPTER_PREFIX.length())
                : thingId);
    }

    /**
     * Constructs the {@link NeeoThingUID} from the given thing type and ID. This constructor uses
     * {@link NeeoConstants#NEEOIO_BINDING_ID} as the binding id.
     *
     * @param thingType the thing type
     * @param id the id
     */
    public NeeoThingUID(String thingType, String id) {
        super(NeeoConstants.NEEOIO_BINDING_ID, thingType, id);
    }

    /**
     * Returns the thing type for this UID
     *
     * @return a possibly null, possibly empty thing type
     */
    public String getThingType() {
        return getSegment(1);
    }

    /**
     * Converts the {@link NeeoThingUID} to a {@link ThingUID}
     *
     * @return a non-null {@link ThingUID}
     */
    public ThingUID asThingUID() {
        return new ThingUID(getAsString());
    }

    /**
     * Returns the UID required by the NEEO brain
     *
     * @return a non-null, non-empty UID
     */
    public String getNeeoUID() {
        return NeeoConstants.NEEO_ADAPTER_PREFIX + getAsString();
    }
}
