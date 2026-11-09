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
package org.openhab.binding.lutron.internal.hw;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lutron.internal.KeypadComponent;
import org.openhab.binding.lutron.internal.discovery.project.ComponentType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.State;

/**
 * Creates the channels of a keypad from its list of components and delivers button events and LED states to them.
 *
 * This class does not depend on a Lutron protocol. A handler provides the access to its thing through {@link Host}
 * and keeps the protocol specific parts, such as parsing messages and sending commands, to itself.
 *
 * @author Christopher Smith - Initial contribution
 */
@NonNullByDefault
final class KeypadChannelSupport {

    /**
     * The operations of the owning handler that this class needs.
     */
    interface Host {
        Thing getThing();

        ThingBuilder editThing();

        void updateThing(Thing thing);

        void triggerChannel(ChannelUID channelUID, String event);

        void updateState(ChannelUID channelUID, State state);
    }

    private final Host host;
    private final ChannelTypeUID buttonChannelType;
    private final ChannelTypeUID ledChannelType;
    private final String ledItemType;

    private final Map<Integer, ChannelUID> componentChannels = new HashMap<>();
    private final Map<ChannelUID, Integer> channelComponents = new HashMap<>();

    KeypadChannelSupport(Host host, ChannelTypeUID buttonChannelType, ChannelTypeUID ledChannelType,
            String ledItemType) {
        this.host = host;
        this.buttonChannelType = buttonChannelType;
        this.ledChannelType = ledChannelType;
        this.ledItemType = ledItemType;
    }

    /**
     * Makes the thing have a trigger channel for every button and a state channel for every LED in the components,
     * plus the extra channels. The thing is only updated if its channels differ.
     */
    void configure(List<KeypadComponent> components, List<Channel> extraChannels) {
        Thing thing = host.getThing();
        List<Channel> channels = new java.util.ArrayList<>();
        componentChannels.clear();
        channelComponents.clear();

        for (KeypadComponent component : components) {
            ChannelUID channelUID = new ChannelUID(thing.getUID(), component.channel());
            if (component.type() == ComponentType.BUTTON) {
                // Physical keypad buttons are momentary events, so they are trigger channels
                channels.add(ChannelBuilder.create(channelUID).withType(buttonChannelType).withKind(ChannelKind.TRIGGER)
                        .withLabel(component.description()).build());
            } else if (component.type() == ComponentType.LED) {
                channels.add(ChannelBuilder.create(channelUID, ledItemType).withType(ledChannelType)
                        .withLabel(component.description()).build());
            } else {
                continue;
            }
            componentChannels.put(component.id(), channelUID);
            channelComponents.put(channelUID, component.id());
        }
        channels.addAll(extraChannels);

        if (!sameChannels(thing.getChannels(), channels)) {
            host.updateThing(host.editThing().withChannels(channels).build());
        }
    }

    private static boolean sameChannels(List<Channel> existing, List<Channel> wanted) {
        if (existing.size() != wanted.size()) {
            return false;
        }
        Map<ChannelUID, @Nullable ChannelTypeUID> types = new HashMap<>();
        for (Channel channel : existing) {
            types.put(channel.getUID(), channel.getChannelTypeUID());
        }
        for (Channel channel : wanted) {
            if (!types.containsKey(channel.getUID())
                    || !java.util.Objects.equals(types.get(channel.getUID()), channel.getChannelTypeUID())) {
                return false;
            }
        }
        return true;
    }

    @Nullable
    ChannelUID channelFor(int componentId) {
        return componentChannels.get(componentId);
    }

    @Nullable
    Integer componentFor(ChannelUID channelUID) {
        return channelComponents.get(channelUID);
    }

    /**
     * Fires an event on the trigger channel of a button.
     *
     * @return whether the keypad has a channel for the component
     */
    boolean trigger(int componentId, String event) {
        ChannelUID channelUID = componentChannels.get(componentId);
        if (channelUID == null) {
            return false;
        }
        host.triggerChannel(channelUID, event);
        return true;
    }

    /**
     * Updates the state channel of an LED.
     *
     * @return whether the keypad has a channel for the component
     */
    boolean update(int componentId, State state) {
        ChannelUID channelUID = componentChannels.get(componentId);
        if (channelUID == null) {
            return false;
        }
        host.updateState(channelUID, state);
        return true;
    }
}
