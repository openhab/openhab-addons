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

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.openhab.binding.solarman.internal.DefinitionParser;
import org.openhab.binding.solarman.internal.defmodel.Lookup;
import org.openhab.binding.solarman.internal.defmodel.ParameterItem;
import org.openhab.core.thing.type.AutoUpdatePolicy;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.StateDescription;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.w3c.dom.Element;

/**
 * @author Leo Siepel - Initial contribution
 * @author Ricardo Costa - Advanced profiles and shared compatibility corrections
 */
@NonNullByDefault
public class SolarmanChannelTypeProviderTest {
    private static final ChannelTypeUID CHANNEL_TYPE_UID = new ChannelTypeUID("solarman", "test");
    private final SolarmanChannelTypeProvider provider = createProvider();

    @ParameterizedTest
    @MethodSource("itemPatterns")
    void testStatePatternMatchesItemType(ParameterItem item, String expectedItemType,
            @Nullable String expectedPattern) {
        ChannelType channelType = provider.buildChannelType(CHANNEL_TYPE_UID, item);

        assertEquals(expectedItemType, channelType.getItemType());
        StateDescription stateDescription = channelType.getState();
        assertNotNull(stateDescription);
        assertEquals(expectedPattern, stateDescription.getPattern());
    }

    private static Stream<Arguments> itemPatterns() {
        return Stream.of(Arguments.of(createLookupItem(), "String", null),
                Arguments.of(createItem(5, "", BigDecimal.ONE), "String", null),
                Arguments.of(createItem(8, "", BigDecimal.ONE), "DateTime", null),
                Arguments.of(createItem(1, "", BigDecimal.ONE), "Number", "%d"),
                Arguments.of(createItem(1, "W", new BigDecimal("0.1")), "Number:Power", "%.1f %unit%"));
    }

    @ParameterizedTest
    @MethodSource("accessModes")
    void stateDescriptionMatchesDeclaredWriteAccess(@Nullable Boolean readOnly, boolean expectedReadOnly) {
        ParameterItem item = createItem(1, "%", BigDecimal.ONE);
        if (readOnly != null) {
            item.setIsReadOnly(readOnly);
        }
        StateDescription state = provider.buildChannelType(CHANNEL_TYPE_UID, item).getState();
        assertNotNull(state);
        assertEquals(expectedReadOnly, state.isReadOnly());
    }

    private static Stream<Arguments> accessModes() {
        return Stream.of(Arguments.of(Boolean.FALSE, false), Arguments.of(Boolean.TRUE, true),
                Arguments.of(null, true));
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 2, 3, 4, 5, 6 })
    void deyeHybridTimeOfUseSocIsWritableForSitemapControls(int period) {
        var definition = new DefinitionParser().parseDefinition("deye_hybrid");
        assertNotNull(definition);
        ParameterItem item = definition.getParameters().stream().flatMap(group -> group.getItems().stream())
                .filter(candidate -> candidate.getName().equals("Time of Use SOC " + period)).findFirst().orElseThrow();
        ChannelType type = provider.buildChannelType(CHANNEL_TYPE_UID, item);
        assertEquals("Number:Dimensionless", type.getItemType());
        assertEquals(AutoUpdatePolicy.VETO, type.getAutoUpdatePolicy());
        StateDescription state = type.getState();
        assertNotNull(state);
        assertEquals(false, state.isReadOnly());
    }

    private static ParameterItem createLookupItem() {
        ParameterItem item = createItem(1, "", BigDecimal.ONE);
        Lookup disabled = new Lookup();
        disabled.setKey(0);
        disabled.setValue("Disabled");
        Lookup enabled = new Lookup();
        enabled.setKey(1);
        enabled.setValue("Enabled");
        item.setLookup(List.of(disabled, enabled));
        return item;
    }

    @ParameterizedTest
    @MethodSource("writableTypes")
    void writableItemKindsRemainEditable(ParameterItem item, String itemType) {
        item.setIsReadOnly(false);
        ChannelType type = provider.buildChannelType(CHANNEL_TYPE_UID, item);
        assertEquals(itemType, type.getItemType());
        assertEquals(false, Objects.requireNonNull(type.getState()).isReadOnly());
    }

    private static Stream<Arguments> writableTypes() {
        return Stream.of(Arguments.of(createItem(1, "%", BigDecimal.ONE), "Number:Dimensionless"),
                Arguments.of(createItem(2, "W", BigDecimal.ONE), "Number:Power"),
                Arguments.of(createItem(3, "kWh", BigDecimal.ONE), "Number:Energy"),
                Arguments.of(createItem(4, "A", BigDecimal.ONE), "Number:ElectricCurrent"),
                Arguments.of(createItem(5, "", BigDecimal.ONE), "String"),
                Arguments.of(createItem(8, "", BigDecimal.ONE), "DateTime"),
                Arguments.of(createItem(9, "", BigDecimal.ONE), "String"), Arguments.of(createLookupItem(), "String"));
    }

    static Stream<String> bundledDefinitions() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        try (InputStream stream = Objects.requireNonNull(
                SolarmanChannelTypeProviderTest.class.getResourceAsStream("/OH-INF/thing/thing-types.xml"))) {
            var parameters = factory.newDocumentBuilder().parse(stream).getElementsByTagName("parameter");
            List<String> names = new ArrayList<>();
            for (int i = 0; i < parameters.getLength(); i++) {
                Element parameter = (Element) parameters.item(i);
                if ("inverterType".equals(parameter.getAttribute("name"))) {
                    var options = parameter.getElementsByTagName("option");
                    for (int j = 0; j < options.getLength(); j++) {
                        names.add(((Element) options.item(j)).getAttribute("value"));
                    }
                }
            }
            names.removeAll(
                    org.openhab.binding.solarman.internal.SolarmanBindingConstants.SUNSYNK_INVERTER_TYPES.keySet());
            assertEquals(18, names.size());
            return names.stream();
        }
    }

    @ParameterizedTest(name = "{0}: UI access and options")
    @MethodSource("bundledDefinitions")
    void allBundledChannelMetadataMatchesItsDefinition(String name) {
        var definition = Objects.requireNonNull(new DefinitionParser().parseDefinition(name));
        for (var group : definition.getParameters()) {
            for (ParameterItem item : group.getItems()) {
                ChannelType type = provider.buildChannelType(
                        ChannelUtils.computeChannelTypeId(name, group.getGroup(), item.getName()), item);
                StateDescription state = Objects.requireNonNull(type.getState());
                assertEquals(!Boolean.FALSE.equals(item.getIsReadOnly()), state.isReadOnly(),
                        name + "/" + item.getName());
                var expected = item.getLookup().stream().map(Lookup::getValue).distinct().toList();
                assertEquals(expected, state.getOptions().stream().map(option -> option.getValue()).toList(),
                        name + "/" + item.getName());
                state.getOptions().forEach(option -> assertEquals(option.getValue(), option.getLabel()));
            }
        }
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
