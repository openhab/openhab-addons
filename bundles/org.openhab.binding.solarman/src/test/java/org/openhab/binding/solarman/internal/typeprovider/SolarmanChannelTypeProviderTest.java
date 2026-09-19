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
package org.openhab.binding.solarman.internal.typeprovider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.solarman.internal.defmodel.Lookup;
import org.openhab.binding.solarman.internal.defmodel.ParameterItem;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.StateDescription;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class SolarmanChannelTypeProviderTest {
    private static final ChannelTypeUID CHANNEL_TYPE_UID = new ChannelTypeUID("solarman", "test");
    private final SolarmanChannelTypeProvider provider = createProvider();

    @ParameterizedTest
    @MethodSource("itemPatterns")
    void testStatePatternMatchesItemType(ParameterItem item, String expectedItemType, String expectedPattern) {
        ChannelType channelType = provider.buildChannelType(CHANNEL_TYPE_UID, item);

        assertEquals(expectedItemType, channelType.getItemType());
        @Nullable
        StateDescription stateDescription = channelType.getState();
        assertNotNull(stateDescription);
        assertEquals(expectedPattern, stateDescription.getPattern());
    }

    private static Stream<Arguments> itemPatterns() {
        return Stream.of(Arguments.of(createLookupItem(), "String", "%s"),
                Arguments.of(createItem(5, "", BigDecimal.ONE), "String", "%s"),
                Arguments.of(createItem(8, "", BigDecimal.ONE), "DateTime", "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS"),
                Arguments.of(createItem(1, "", BigDecimal.ONE), "Number", "%d"),
                Arguments.of(createItem(1, "W", new BigDecimal("0.1")), "Number:Power", "%.1f %unit%"));
    }

    private static ParameterItem createLookupItem() {
        ParameterItem item = createItem(1, "", BigDecimal.ONE);
        item.setLookup(List.of(new Lookup()));
        return item;
    }

    private static ParameterItem createItem(int rule, String uom, BigDecimal scale) {
        ParameterItem item = new ParameterItem();
        item.setName("Test");
        item.setRule(rule);
        item.setUom(uom);
        item.setScale(scale);
        item.setRegisters(List.of(1));
        return item;
    }

    private static SolarmanChannelTypeProvider createProvider() {
        Bundle bundle = (@NonNull Bundle) mock(Bundle.class);
        when(bundle.findEntries("/definitions", "*", false)).thenReturn(Collections.emptyEnumeration());
        BundleContext bundleContext = (@NonNull BundleContext) mock(BundleContext.class);
        when(bundleContext.getBundle()).thenReturn(bundle);
        return new SolarmanChannelTypeProvider(bundleContext);
    }
}
