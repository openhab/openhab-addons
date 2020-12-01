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
package org.openhab.binding.sony.internal.providers;

import java.util.function.Predicate;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.ThingTypeProvider;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeProvider;
import org.openhab.binding.sony.internal.providers.models.SonyDeviceCapability;

/**
 * Defines the contract for a sony definition provider. A definition provider create thing types, channel group types
 * and is used to record device capabilities and things
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public interface SonyDefinitionProvider extends ThingTypeProvider, ChannelGroupTypeProvider, SonyModelProvider {
    /**
     * Method to write out a models device capabilities
     *
     * @param deviceCapability a non-null device capability
     */
    void writeDeviceCapabilities(SonyDeviceCapability deviceCapability);

    /**
     * Helper method to write a thing/thing type to a the source(s)
     *
     * @param service a non-null, non-empty service
     * @param configUri a non-null, non-empty configUri
     * @param modelName a non-null, non-empty model name
     * @param thing a non-null thing to use
     * @param channelFilter a non-null channel filter to use
     */
    void writeThing(String service, String configUri, String modelName, Thing thing, Predicate<Channel> channelFilter);
}
