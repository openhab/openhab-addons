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
package org.openhab.binding.knx.internal.wizard;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.project.KnxDatapointType;
import org.openhab.binding.knx.internal.wizard.CheckedProposal.Channel;
import org.openhab.binding.knx.internal.wizard.CheckedProposal.Item;
import org.openhab.binding.knx.internal.wizard.CheckedProposal.LocationGroup;
import org.openhab.binding.knx.internal.wizard.CheckedProposal.Part;
import org.openhab.binding.knx.internal.wizard.CheckedProposal.Read;
import org.openhab.binding.knx.internal.wizard.CheckedProposal.Thing;

/**
 * Checked models built by hand for the wizard's tests, as the validator will build them. All data is made up.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
final class WizardFixtures {
    static final String BRIDGE = "knx:ip:example";

    private WizardFixtures() {
    }

    static KnxDatapointType dpt(String text) {
        return Objects.requireNonNull(KnxDatapointType.parse(text));
    }

    static Part part(String key, String dpt, String main, @Nullable String status, Read read) {
        return new Part(key, dpt(dpt), main, status, read);
    }

    static String thingUID(String thingId) {
        return "knx:device:example:" + thingId;
    }

    static Item item(String name, String type, String label, String channelUID, @Nullable String category,
            List<String> tags, List<String> groups) {
        return new Item(name, type, label, category, tags, groups, channelUID);
    }

    static Channel channel(String thingId, String id, String label, String channelType, String itemName,
            String itemType, Part... parts) {
        String uid = thingUID(thingId) + ":" + id;
        return new Channel(uid, id, label, channelType, Arrays.asList(parts),
                item(itemName, itemType, CheckedProposal.cleanItemLabel(label), uid, null, List.of(), List.of()));
    }

    static Channel withItem(Channel channel, Item item) {
        return new Channel(channel.uid(), channel.id(), channel.label(), channel.channelType(), channel.parts(), item);
    }

    static Thing thing(String id, String label, Channel... channels) {
        return new Thing(thingUID(id), id, label, Arrays.asList(channels));
    }

    static CheckedProposal proposal(boolean readAtStart, List<LocationGroup> groups, Thing... things) {
        return new CheckedProposal(BRIDGE, Arrays.asList(things), groups, readAtStart);
    }

    /** A switch and a dimmer with paired status addresses, a temperature in a room group, a rollershutter. */
    static CheckedProposal sample(boolean readAtStart) {
        Channel light = channel("Kitchen", "Ceiling_light", "Ceiling light", "switch", "Kitchen_Ceiling_light",
                "Switch", part("ga", "1.001", "31/7/1", "31/7/2", Read.STATUS));
        light = withItem(light, item("Kitchen_Ceiling_light", "Switch", "Ceiling light", light.uid(), null, List.of(),
                List.of("gKitchen")));
        Channel spots = channel("Kitchen", "Spots", "Spots", "dimmer", "Kitchen_Spots", "Dimmer",
                part("switch", "1.001", "31/7/3", "31/7/4", Read.STATUS),
                part("position", "5.001", "31/7/5", "31/7/6", Read.STATUS),
                part("increaseDecrease", "3.007", "31/7/7", null, Read.NONE));
        Channel temperature = channel("Kitchen", "Temperature", "Temperature", "number", "Kitchen_Temperature",
                "Number:Temperature", part("ga", "9.001", "31/7/8", null, Read.MAIN));
        temperature = withItem(temperature, item("Kitchen_Temperature", "Number:Temperature", "Temperature",
                temperature.uid(), "temperature", List.of("Measurement", "Temperature"), List.of("gKitchen")));
        Channel blind = channel("Hall", "Blind", "Blind", "rollershutter", "Blind", "Rollershutter",
                part("upDown", "1.008", "31/7/10", null, Read.NONE),
                part("stopMove", "1.010", "31/7/11", null, Read.NONE),
                part("position", "5.001", "31/7/12", "31/7/13", Read.STATUS));
        return proposal(readAtStart, List.of(new LocationGroup("gKitchen", "Kitchen", "Room", null, null, false)),
                thing("Kitchen", "Kitchen", light, spots, temperature), thing("Hall", "Hall", blind));
    }
}
