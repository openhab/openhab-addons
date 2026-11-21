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
package org.openhab.binding.knx.internal.channel;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.openhab.binding.knx.internal.project.ChannelFixtures.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.knx.internal.dpt.DPTUtil;
import org.openhab.binding.knx.internal.project.ChannelFixtures;
import org.openhab.binding.knx.internal.project.KnxChannelMapper;
import org.openhab.binding.knx.internal.project.KnxChannelSuggestion;
import org.openhab.binding.knx.internal.project.KnxGroupAddress;
import org.openhab.binding.knx.internal.project.KnxGroupAddressStyle;
import org.openhab.binding.knx.internal.project.KnxProject;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Type;

import io.calimero.GroupAddress;
import io.calimero.KNXException;
import io.calimero.dptxlator.DPT;
import io.calimero.dptxlator.TranslatorTypes;
import io.calimero.dptxlator.TranslatorTypes.MainType;

/**
 * Proves the channel text of the project reader against the binding's own channel code: every suggestion the mapper
 * makes must be one the binding accepts, and the text must read back as what was meant. This test sits outside the
 * reader's package and uses only its public API.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class KnxChannelTextCrossCheckTest {

    // Copied from the constructors of the Type* classes; preferredType() of the created channel is checked against
    // the first entry, so a change in a class shows up here.
    private static final Map<String, List<Class<? extends Type>>> ACCEPTED = Map.of( //
            "switch", List.of(OnOffType.class), //
            "contact", List.of(OpenClosedType.class), //
            "number", List.of(DecimalType.class, QuantityType.class), //
            "string", List.of(StringType.class), //
            "datetime", List.of(DateTimeType.class), //
            "dimmer", List.of(PercentType.class, OnOffType.class, IncreaseDecreaseType.class), //
            "rollershutter", List.of(PercentType.class, UpDownType.class, StopMoveType.class), //
            "color", List.of(HSBType.class, PercentType.class, OnOffType.class, IncreaseDecreaseType.class));

    private static final int G1 = raw(1, 2, 3);
    private static final int G2 = raw(1, 2, 4);
    private static final int G3 = raw(1, 2, 5);

    private static KNXChannel channel(KnxChannelSuggestion suggestion) {
        String channelType = Objects.requireNonNull(suggestion.channelType());
        Channel channel = Objects.requireNonNull(mock(Channel.class));
        when(channel.getChannelTypeUID()).thenReturn(new ChannelTypeUID("knx:" + channelType));
        when(channel.getUID()).thenReturn(new ChannelUID("knx:device:bridge:thing:channel"));
        when(channel.getConfiguration()).thenReturn(new Configuration(Map.of(suggestion.key(), suggestion.value())));
        when(channel.getAcceptedItemType()).thenReturn("none");
        return KNXChannelFactory.createKnxChannel(channel);
    }

    /** What is wrong with a suggestion for this type, empty when nothing. */
    private static List<String> problems(String dpt, KnxChannelSuggestion suggestion) {
        List<String> problems = new ArrayList<>();
        String channelType = suggestion.channelType();
        if (channelType == null) {
            return problems;
        }
        List<Class<? extends Type>> accepted = ACCEPTED.get(channelType);
        if (accepted == null) {
            return List.of(dpt + ": channel type " + channelType + " is not in the accepted table of this test");
        }
        KNXChannel channel;
        try {
            channel = channel(suggestion);
        } catch (RuntimeException e) {
            return List.of(dpt + ": " + channelType + " cannot be created: " + e);
        }
        if (!accepted.get(0).equals(channel.preferredType())) {
            problems.add(dpt + ": the accepted types of " + channelType + " changed, now " + channel.preferredType());
        }
        Set<Class<? extends Type>> allowed = DPTUtil.getAllowedTypes(dpt);
        if (accepted.stream().noneMatch(allowed::contains)) {
            problems.add(dpt + ": " + channelType + " accepts none of " + allowed);
        }
        String defaultDpt = channel.getDefaultDPT(suggestion.key());
        Set<Class<? extends Type>> ofDefault = DPTUtil.getAllowedTypes(defaultDpt);
        if (ofDefault.stream().noneMatch(allowed::contains)) {
            problems.add(dpt + ": key " + suggestion.key() + " of " + channelType + " has default " + defaultDpt
                    + " with " + ofDefault + ", sharing nothing with " + allowed);
        }
        return problems;
    }

    private static KnxChannelSuggestion suggestionFor(String dpt) {
        KnxProject project = project(List.of(address(1, G1, dpt)), List.of());
        KnxGroupAddress address = Objects.requireNonNull(project.groupAddress(G1));
        return new KnxChannelMapper(project).forGroupAddress(address);
    }

    /** Every sub number Calimero knows, by main number. */
    private static Map<Integer, List<String>> knownSubTypes() {
        Map<Integer, List<String>> known = new TreeMap<>();
        for (int main = 1; main <= 999; main++) {
            try {
                @Nullable
                MainType type = TranslatorTypes.getMainType(main);
                if (type != null) {
                    known.put(main, type.getSubTypes().values().stream().map(DPT::getID).toList());
                }
            } catch (KNXException e) {
                // no such main number
            }
        }
        return known;
    }

    @Test
    void everySubNumberOfCalimeroTheTableSuggestsForIsAcceptedByTheChannel() {
        List<String> problems = new ArrayList<>();
        for (List<String> subs : knownSubTypes().values()) {
            for (String id : subs) {
                problems.addAll(problems(id, suggestionFor(id)));
            }
        }
        assertEquals(List.of(), problems);
    }

    @Test
    void tableRowsKept() {
        Map<Integer, Set<String>> kept = new TreeMap<>();
        for (Map.Entry<Integer, List<String>> entry : knownSubTypes().entrySet()) {
            for (String id : entry.getValue()) {
                String channelType = suggestionFor(id).channelType();
                if (channelType != null) {
                    kept.computeIfAbsent(entry.getKey(), k -> new TreeSet<>());
                    Objects.requireNonNull(kept.get(entry.getKey())).add(channelType);
                }
            }
        }
        assertEquals(Set.of(1, 3, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 16, 17, 18, 19, 28, 29, 232), kept.keySet());
        assertEquals(Set.of("switch", "contact", "rollershutter"), kept.get(1));
        assertEquals(Set.of("dimmer"), kept.get(3));
        assertEquals(Set.of("color"), kept.get(232));
    }

    @Test
    void namedSubNumbersOfTheTableAreAccepted() {
        for (String dpt : List.of("1.001", "1.008", "1.009", "1.019", "3.007", "5.001", "9.001", "16.000", "232.600")) {
            KnxChannelSuggestion suggestion = suggestionFor(dpt);
            assertNotNull(suggestion.channelType(), dpt);
            assertEquals(List.of(), problems(dpt, suggestion));
        }
    }

    @Test
    void subNumbersTheBindingMapsElsewhereGetNoSuggestion() {
        for (String dpt : List.of("1.010", "1.022", "6.020")) {
            assertNull(suggestionFor(dpt).channelType(), dpt);
        }
    }

    private record Expected(@Nullable String dpt, int main, Set<Integer> listen, Set<Integer> read) {
    }

    private static void assertReadsBack(Expected expected, KnxChannelSuggestion suggestion) {
        GroupAddressConfiguration parsed = GroupAddressConfiguration.parse(suggestion.value());
        assertNotNull(parsed, suggestion.value());
        assertEquals(expected.dpt(), parsed.getDPT(), suggestion.value());
        assertEquals(expected.main(), parsed.getMainGA().getRawAddress(), suggestion.value());
        assertEquals(expected.listen(), raws(parsed.getListenGAs()), suggestion.value());
        assertEquals(expected.read(), raws(parsed.getReadGAs()), suggestion.value());
    }

    private static Set<Integer> raws(Set<GroupAddress> addresses) {
        return addresses.stream().map(GroupAddress::getRawAddress).collect(Collectors.toSet());
    }

    private static int ga(int sub) {
        return raw(31, 7, sub);
    }

    @Test
    void textReadsBackInAllThreeAddressStyles() {
        for (KnxGroupAddressStyle style : KnxGroupAddressStyle.values()) {
            KnxProject project = ChannelFixtures.projectOfDevices(style,
                    List.of(address(style, 1, ga(1), "9.001"), address(style, 2, ga(2), null),
                            address(style, 3, ga(3), "1.001"), address(style, 4, ga(4), "1.001"),
                            address(style, 5, ga(5), "1.001"), address(style, 6, ga(6), "1.001")),
                    List.of(List.of(object("O-1", 1, "Value", null, null, 1, 2).flags("CRW--"),
                            object("O-2", 2, "Switch", null, null, 3, 5).flags("C-W--"),
                            object("O-3", 3, "Status", null, null, 4).flags("CR-T-")),
                            List.of(object("O-4", 1, "Switch", null, null, 5).flags("C-W--")),
                            List.of(object("O-5", 1, "Dummy", null, null, 3, 1, 4).flags("CRWT-")),
                            List.of(object("O-6", 1, "Display", null, null, 4, 6).flags("C-W--"))));
            KnxChannelMapper mapper = new KnxChannelMapper(project);

            Map<Integer, Expected> expected = Map.of( //
                    1, new Expected("9.001", ga(1), Set.of(ga(1)), Set.of(ga(1))), //
                    2, new Expected(null, ga(2), Set.of(ga(2)), Set.of()), //
                    3, new Expected("1.001", ga(3), Set.of(ga(3)), Set.of(ga(3))), //
                    4, new Expected("1.001", ga(4), Set.of(ga(4)), Set.of(ga(4))), //
                    5, new Expected("1.001", ga(5), Set.of(ga(5)), Set.of()), //
                    6, new Expected("1.001", ga(6), Set.of(ga(6)), Set.of()));
            for (Map.Entry<Integer, Expected> entry : expected.entrySet()) {
                KnxGroupAddress address = Objects.requireNonNull(project.groupAddress(ga(entry.getKey())));
                assertReadsBack(entry.getValue(), mapper.forGroupAddress(address));
            }

            List<KnxChannelSuggestion> device = mapper.forDevice(project.topology().devices().get(0));
            assertEquals(3, device.size());
            assertReadsBack(Objects.requireNonNull(expected.get(1)), device.get(0));
        }
    }

    @Test
    void channelIsCreatedFromTheProducedConfiguration() {
        KnxProject project = project(
                List.of(address(1, G1, "1.001"), address(2, G2, "1.008"), address(3, G3, "232.600")),
                List.of(object("O-1_R-1", 1, "A", null, null, 1, 3).flags("CRW--"),
                        object("O-2_R-2", 2, "B", null, null, 2)));
        KnxChannelMapper mapper = new KnxChannelMapper(project);
        KnxChannelSuggestion sw = mapper.forGroupAddress(Objects.requireNonNull(project.groupAddress(G1)));
        KNXChannel switchChannel = channel(sw);
        assertEquals("switch", switchChannel.getChannelType());
        assertEquals(Set.of(G1), switchChannel.getAllGroupAddresses().stream().map(GroupAddress::getRawAddress)
                .collect(Collectors.toSet()));
        assertEquals(List.of(G1), switchChannel.getWriteAddresses().stream().map(GroupAddress::getRawAddress).toList());

        KnxChannelSuggestion shutter = mapper.forGroupAddress(Objects.requireNonNull(project.groupAddress(G2)));
        assertEquals("rollershutter", shutter.channelType());
        assertEquals("upDown", shutter.key());
        assertEquals("rollershutter", channel(shutter).getChannelType());

        KnxChannelSuggestion color = mapper.forGroupAddress(Objects.requireNonNull(project.groupAddress(G3)));
        assertEquals("color", color.channelType());
        assertEquals("hsb", color.key());
        assertEquals("color", channel(color).getChannelType());
    }
}
