/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.keba.internal.handler;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.State;

/**
 * Routes protocol results either directly to openHAB or to the combined Thing's coordinator.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
public abstract class KeContactProtocolHandler extends BaseThingHandler {

    public interface Listener {
        void stateUpdated(String channel, State state);

        void statusUpdated(ThingStatus status, ThingStatusDetail detail, @Nullable String description);

        void propertiesUpdated(Map<String, String> properties);

        boolean isLinked(String channel);
    }

    private final @Nullable Listener listener;
    private final @Nullable Configuration configuration;

    protected KeContactProtocolHandler(Thing thing, @Nullable Configuration configuration,
            @Nullable Listener listener) {
        super(thing);
        this.configuration = configuration;
        this.listener = listener;
    }

    protected final boolean isCombined() {
        return listener != null;
    }

    @Override
    protected Configuration getConfig() {
        Configuration localConfiguration = configuration;
        return localConfiguration == null ? super.getConfig() : new Configuration(localConfiguration);
    }

    @Override
    protected boolean isLinked(ChannelUID channelUID) {
        Listener localListener = listener;
        return localListener == null ? super.isLinked(channelUID) : localListener.isLinked(channelUID.getId());
    }

    @Override
    protected void updateState(ChannelUID channelUID, State state) {
        Listener localListener = listener;
        if (localListener == null) {
            super.updateState(channelUID, state);
        } else {
            localListener.stateUpdated(channelUID.getId(), state);
        }
    }

    @Override
    public void updateStatus(ThingStatus status, ThingStatusDetail detail, @Nullable String description) {
        Listener localListener = listener;
        if (localListener == null) {
            super.updateStatus(status, detail, description);
        } else {
            localListener.statusUpdated(status, detail, description);
        }
    }

    @Override
    protected void updateProperties(@Nullable Map<String, String> properties) {
        Listener localListener = listener;
        if (localListener == null) {
            super.updateProperties(properties);
        } else if (properties != null) {
            properties.forEach((name, value) -> getThing().setProperty(name, value));
            localListener.propertiesUpdated(properties);
        }
    }

    @Override
    protected void updateThing(Thing updatedThing) {
        Listener localListener = listener;
        if (localListener == null) {
            super.updateThing(updatedThing);
        } else {
            thing = updatedThing;
            localListener.propertiesUpdated(updatedThing.getProperties());
        }
    }
}
