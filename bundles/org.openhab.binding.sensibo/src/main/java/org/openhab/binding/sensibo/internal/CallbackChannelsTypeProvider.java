/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.sensibo.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sensibo.internal.handler.SensiboSkyHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.type.ChannelGroupType;
import org.openhab.core.thing.type.ChannelGroupTypeProvider;
import org.openhab.core.thing.type.ChannelGroupTypeUID;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeProvider;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * Channel Type Provider that does a callback the SensiboSkyHandler that initiated it.
 *
 * @author Arne Seime - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = { CallbackChannelsTypeProvider.class, ChannelTypeProvider.class,
        ChannelGroupTypeProvider.class })
@NonNullByDefault
public class CallbackChannelsTypeProvider
        implements ChannelTypeProvider, ChannelGroupTypeProvider, ThingHandlerService {
    private @NonNullByDefault({}) SensiboSkyHandler handler;

    @Override
    public Collection<ChannelType> getChannelTypes(@Nullable final Locale locale) {
        return handler != null ? handler.getChannelTypes(locale) : List.of();
    }

    @Override
    public @Nullable ChannelType getChannelType(final ChannelTypeUID channelTypeUID, @Nullable final Locale locale) {
        return handler != null ? handler.getChannelType(channelTypeUID, locale) : null;
    }

    @Override
    public @Nullable ChannelGroupType getChannelGroupType(final ChannelGroupTypeUID channelGroupTypeUID,
            @Nullable final Locale locale) {
        return null;
    }

    @Override
    public Collection<ChannelGroupType> getChannelGroupTypes(@Nullable final Locale locale) {
        return Collections.emptyList();
    }

    @Override
    @Nullable
    public ThingHandler getThingHandler() {
        return handler;
    }

    @NonNullByDefault({})
    @Override
    public void setThingHandler(final ThingHandler handler) {
        this.handler = (SensiboSkyHandler) handler;
    }
}
