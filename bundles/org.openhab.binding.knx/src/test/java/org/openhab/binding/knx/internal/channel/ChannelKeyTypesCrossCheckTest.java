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
import static org.openhab.binding.knx.internal.KNXBindingConstants.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.knx.internal.dpt.DPTUtil;
import org.openhab.binding.knx.internal.project.ChannelFixtures;
import org.openhab.binding.knx.internal.project.ChannelKeyTypes;
import org.openhab.binding.knx.internal.project.KnxChannelMapper;
import org.openhab.binding.knx.internal.project.KnxChannelSuggestion;
import org.openhab.binding.knx.internal.project.KnxDatapointType;
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

import io.calimero.KNXException;
import io.calimero.dptxlator.DPT;
import io.calimero.dptxlator.TranslatorTypes;
import io.calimero.dptxlator.TranslatorTypes.MainType;

/**
 * Proves the per-key table of the wizard against the binding: a key works for a datapoint type exactly when
 * {@link DPTUtil#getAllowedTypes(String)} maps the type to the value class the key exists for.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class ChannelKeyTypesCrossCheckTest {

    // the accepted types of the Type* constructors; preferredType() of a created channel checks the first entry
    private static final Map<String, List<Class<? extends Type>>> ACCEPTED = Map.of( //
            CHANNEL_SWITCH, List.of(OnOffType.class), //
            CHANNEL_CONTACT, List.of(OpenClosedType.class), //
            CHANNEL_NUMBER, List.of(DecimalType.class, QuantityType.class), //
            CHANNEL_STRING, List.of(StringType.class), //
            CHANNEL_DATETIME, List.of(DateTimeType.class), //
            CHANNEL_DIMMER, List.of(PercentType.class, OnOffType.class, IncreaseDecreaseType.class), //
            CHANNEL_ROLLERSHUTTER, List.of(PercentType.class, UpDownType.class, StopMoveType.class), //
            CHANNEL_COLOR, List.of(HSBType.class, PercentType.class, OnOffType.class, IncreaseDecreaseType.class));

    private static final Map<String, Set<String>> KEYS = Map.of( //
            CHANNEL_SWITCH, Set.of(GA), CHANNEL_CONTACT, Set.of(GA), CHANNEL_NUMBER, Set.of(GA), //
            CHANNEL_STRING, Set.of(GA), CHANNEL_DATETIME, Set.of(GA), //
            CHANNEL_DIMMER, Set.of(SWITCH_GA, POSITION_GA, INCREASE_DECREASE_GA), //
            CHANNEL_ROLLERSHUTTER, Set.of(UP_DOWN_GA, STOP_MOVE_GA, POSITION_GA), //
            CHANNEL_COLOR, Set.of(HSB_GA, SWITCH_GA, POSITION_GA, INCREASE_DECREASE_GA));

    private static final List<String> ALL_KEYS = List.of(GA, SWITCH_GA, POSITION_GA, INCREASE_DECREASE_GA, UP_DOWN_GA,
            STOP_MOVE_GA, HSB_GA);

    private static KNXChannel channel(String channelType, String key) {
        Channel channel = Objects.requireNonNull(mock(Channel.class));
        when(channel.getChannelTypeUID()).thenReturn(new ChannelTypeUID("knx:" + channelType));
        when(channel.getUID()).thenReturn(new ChannelUID("knx:device:bridge:thing:channel"));
        when(channel.getConfiguration()).thenReturn(new Configuration(Map.of(key, "31/7/1")));
        when(channel.getAcceptedItemType()).thenReturn("none");
        return KNXChannelFactory.createKnxChannel(channel);
    }

    /** The value classes the key exists for: what the channel accepts and the key's default type gives. */
    private static Set<Class<? extends Type>> needed(String channelType, String key) {
        KNXChannel channel = channel(channelType, key);
        List<Class<? extends Type>> accepted = Objects.requireNonNull(ACCEPTED.get(channelType));
        assertEquals(accepted.get(0), channel.preferredType(), channelType);
        Set<Class<? extends Type>> needed = new HashSet<>(DPTUtil.getAllowedTypes(channel.getDefaultDPT(key)));
        needed.retainAll(accepted);
        assertFalse(needed.isEmpty(), channelType + " " + key);
        return needed;
    }

    /** Every sub number Calimero knows, one made-up sub number per main number and the binding's raw type. */
    private static Set<String> walkedTypes() {
        Set<String> ids = new TreeSet<>();
        for (int main = 1; main <= 999; main++) {
            ids.add(main + ".001");
            try {
                @Nullable
                MainType type = TranslatorTypes.getMainType(main);
                if (type != null) {
                    type.getSubTypes().values().stream().map(DPT::getID).forEach(ids::add);
                }
            } catch (KNXException e) {
                // no such main number
            }
        }
        ids.add("0.000");
        return ids;
    }

    @Test
    void channelTypesAndKeysAreThoseOfTheBinding() {
        assertEquals(KEYS.keySet(), Set.copyOf(ChannelKeyTypes.channelTypes()));
        for (String channelType : ChannelKeyTypes.channelTypes()) {
            assertEquals(KEYS.get(channelType), Set.copyOf(ChannelKeyTypes.keysOf(channelType)), channelType);
            assertEquals(ChannelKeyTypes.keysOf(channelType).size(),
                    Set.copyOf(ChannelKeyTypes.keysOf(channelType)).size(), channelType);
            if (!List.of(GA).equals(ChannelKeyTypes.keysOf(channelType))) {
                for (String key : ALL_KEYS) {
                    KNXChannel channel = channel(channelType, key);
                    if (ChannelKeyTypes.keysOf(channelType).contains(key)) {
                        assertNotNull(channel.getDefaultDPT(key));
                    } else {
                        assertThrows(IllegalArgumentException.class, () -> channel.getDefaultDPT(key),
                                channelType + " " + key);
                    }
                }
            }
        }
        assertEquals(List.of(), ChannelKeyTypes.keysOf("trigger"));
    }

    @Test
    void everyRowAgreesWithTheBindingsTypeMapping() {
        Set<String> walked = walkedTypes();
        assertTrue(walked.size() > 1000, "walked only " + walked.size());
        List<String> differences = new ArrayList<>();
        for (String channelType : ChannelKeyTypes.channelTypes()) {
            for (String key : ChannelKeyTypes.keysOf(channelType)) {
                Set<Class<? extends Type>> needed = needed(channelType, key);
                for (String id : walked) {
                    KnxDatapointType type = Objects.requireNonNull(KnxDatapointType.parse(id), id);
                    boolean expected = DPTUtil.getAllowedTypes(id).stream().anyMatch(needed::contains);
                    if (expected != ChannelKeyTypes.works(channelType, key, type)) {
                        differences.add(channelType + " " + key + " " + id + ": binding " + expected);
                    }
                }
            }
        }
        assertEquals(List.of(), differences);
    }

    @Test
    void singleKeyChannelTypesAreTheGaRowsInFixedOrder() {
        for (String id : walkedTypes()) {
            KnxDatapointType type = Objects.requireNonNull(KnxDatapointType.parse(id));
            List<String> expected = new ArrayList<>();
            for (String channelType : List.of(CHANNEL_SWITCH, CHANNEL_CONTACT, CHANNEL_NUMBER, CHANNEL_STRING,
                    CHANNEL_DATETIME)) {
                if (ChannelKeyTypes.works(channelType, GA, type)) {
                    expected.add(channelType);
                }
            }
            assertEquals(expected, ChannelKeyTypes.singleKeyChannelTypesFor(type), id);
        }
        assertEquals(List.of(CHANNEL_NUMBER, CHANNEL_STRING),
                ChannelKeyTypes.singleKeyChannelTypesFor(new KnxDatapointType(20, 102)));
    }

    @Test
    void aTypeWithoutSubNumberWorksNowhere() {
        for (String channelType : ChannelKeyTypes.channelTypes()) {
            for (String key : ChannelKeyTypes.keysOf(channelType)) {
                assertFalse(ChannelKeyTypes.works(channelType, key, new KnxDatapointType(1, null)));
                assertFalse(ChannelKeyTypes.works(channelType, key, new KnxDatapointType(9, null)));
            }
        }
        assertEquals(List.of(), ChannelKeyTypes.singleKeyChannelTypesFor(new KnxDatapointType(9, null)));
    }

    @Test
    void aKeyOfAnotherChannelTypeWorksNowhere() {
        KnxDatapointType onOff = new KnxDatapointType(1, 1);
        assertTrue(ChannelKeyTypes.works(CHANNEL_DIMMER, SWITCH_GA, onOff));
        assertFalse(ChannelKeyTypes.works(CHANNEL_SWITCH, SWITCH_GA, onOff));
        assertFalse(ChannelKeyTypes.works(CHANNEL_ROLLERSHUTTER, SWITCH_GA, onOff));
        assertFalse(ChannelKeyTypes.works("trigger", GA, onOff));
    }

    @Test
    void everySuggestionOfTheMapperWorksOnItsKey() {
        List<String> problems = new ArrayList<>();
        for (String id : walkedTypes()) {
            KnxProject project = ChannelFixtures.project(List.of(ChannelFixtures.address(1, 0xFF01, id)), List.of());
            KnxChannelSuggestion suggestion = new KnxChannelMapper(project)
                    .forGroupAddress(Objects.requireNonNull(project.groupAddress(0xFF01)));
            String channelType = suggestion.channelType();
            if (channelType != null && !ChannelKeyTypes.works(channelType, suggestion.key(),
                    Objects.requireNonNull(KnxDatapointType.parse(id)))) {
                problems.add(id + ": " + channelType + " " + suggestion.key());
            }
        }
        assertEquals(List.of(), problems);
    }
}
