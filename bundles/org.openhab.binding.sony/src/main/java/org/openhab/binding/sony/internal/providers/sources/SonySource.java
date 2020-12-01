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
package org.openhab.binding.sony.internal.providers.sources;

import java.util.Collection;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelGroupType;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.openhab.binding.sony.internal.providers.SonyModelProvider;
import org.openhab.binding.sony.internal.providers.models.SonyDeviceCapability;
import org.openhab.binding.sony.internal.providers.models.SonyThingDefinition;

/**
 * This interface defines the contract for a sony thing type source.
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public interface SonySource extends AutoCloseable, SonyModelProvider {
    /**
     * Returns a collection of thing types within the source
     *
     * @return a non-null but possibly empty collection of thing types
     */
    Collection<ThingType> getThingTypes();

    /**
     * Returns a thing type for the given thing type UID
     *
     * @param thingTypeUID a non-null thing type UID
     * @return the associated thing type or null if not found in this source
     */
    @Nullable
    ThingType getThingType(ThingTypeUID thingTypeUID);

    /**
     * Returns a thing type definition for the given thing type UID
     *
     * @param thingTypeUID a non-null thing type UID
     * @return the associated thing type definition or null if not found in this source
     */
    @Nullable
    SonyThingDefinition getSonyThingTypeDefinition(ThingTypeUID thingTypeUID);

    /**
     * Returns a channel group type for the given channel group type uid
     *
     * @param channelGroupTypeUID a non-null channel group type UID
     * @return the associated channel group type or null if not found in this source
     */
    @Nullable
    ChannelGroupType getChannelGroupType(ChannelGroupTypeUID channelGroupTypeUID);

    /**
     * Returns all channel group types
     *
     * @return a non-null, possibly empty collection of channel group types
     */
    @Nullable
    Collection<ChannelGroupType> getChannelGroupTypes();

    /**
     * This method will be called to write the thing definition to the underlying source
     *
     * @param thingTypeDefinition a non-null thing definition
     */
    void writeThingDefinition(SonyThingDefinition thingTypeDefinition);

    /**
     * Method to write out a device capability
     *
     * @param deviceCapability a non-null device capability to write
     */
    void writeDeviceCapabilities(SonyDeviceCapability deviceCapability);

    @Override
    public void close();
}
