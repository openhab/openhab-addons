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
package org.openhab.binding.kvv.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.type.ChannelGroupType;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeProvider;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeProvider;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.osgi.service.component.annotations.Component;

/**
 * KVVChannelGroupTypeProvider provides a set of channel groups for a {@link Thing}.
 *
 * @author Maximilian Hess - Initial contribution
 */
@Component(service = { ChannelTypeProvider.class, ChannelGroupTypeProvider.class })
public class KVVChannelTypeProvider implements ChannelGroupTypeProvider, ChannelTypeProvider {

    private final List<ChannelGroupType> channelGroupTypes;

    private final List<ChannelType> channelTypes;

    public KVVChannelTypeProvider() {
        this.channelGroupTypes = new ArrayList<ChannelGroupType>();
        this.channelTypes = new ArrayList<ChannelType>();
    }

    @Override
    public Collection<ChannelGroupType> getChannelGroupTypes(final @Nullable Locale locale) {
        return this.channelGroupTypes;
    }

    @Override
    public @Nullable ChannelGroupType getChannelGroupType(final ChannelGroupTypeUID channelGroupTypeUID,
            final @Nullable Locale locale) {

        for (final ChannelGroupType cgt : this.channelGroupTypes) {
            if (cgt.getUID().equals(channelGroupTypeUID)) {
                return cgt;
            }
        }

        return null;
    }

    @Override
    public Collection<ChannelType> getChannelTypes(@Nullable Locale locale) {
        return this.channelTypes;
    }

    @Override
    public @Nullable ChannelType getChannelType(final ChannelTypeUID channelTypeUID, final @Nullable Locale locale) {

        for (final ChannelType ct : this.channelTypes) {
            if (ct.getUID().equals(channelTypeUID)) {
                return ct;
            }
        }

        return null;
    }

    public void addChannelType(final ChannelType type) {
        this.channelTypes.add(type);
    }

    public void removeChannelType(final ChannelType type) {
        this.channelTypes.remove(type);
    }
}
