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
package org.openhab.io.homekit.internal.accessories;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.io.homekit.internal.HomekitCharacteristicType.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.core.items.GenericItem;
import org.openhab.core.items.GroupItem;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataKey;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.library.items.ColorItem;
import org.openhab.core.library.items.DimmerItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.io.homekit.internal.HomekitAccessoryType;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitCharacteristicType;
import org.openhab.io.homekit.internal.HomekitOHItemProxy;
import org.openhab.io.homekit.internal.HomekitSettings;
import org.openhab.io.homekit.internal.HomekitTaggedItem;

/**
 * Tests automatic optional characteristics for simple lighting and fan accessories.
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
class HomekitAccessoryFactoryTest {
    static Stream<Arguments> optionalCharacteristics() {
        return Stream.of(Arguments.of(new SwitchItem("lightSwitch"), "Lighting", List.of()),
                Arguments.of(new DimmerItem("lightDimmer"), "Lighting", List.of(BRIGHTNESS)),
                Arguments.of(new ColorItem("lightColor"), "Lighting", List.of(BRIGHTNESS, HUE, SATURATION)),
                Arguments.of(new SwitchItem("fanSwitch"), "Fan", List.of()),
                Arguments.of(new DimmerItem("fanDimmer"), "Fan", List.of(ROTATION_SPEED)),
                Arguments.of(new DimmerItem("basicFan"), "BasicFan", List.of()),
                Arguments.of(new GroupItem("lightGroup", new ColorItem("base")), "Lighting",
                        List.of(BRIGHTNESS, HUE, SATURATION)),
                Arguments.of(new GroupItem("fanGroup", new DimmerItem("base")), "Fan", List.of(ROTATION_SPEED)),
                Arguments.of(new ColorItem("explicitPower"), "Lighting,Lighting.OnState", List.of()),
                Arguments.of(new ColorItem("explicitBrightness"), "Lighting,Lighting.Brightness", List.of(BRIGHTNESS)),
                Arguments.of(new DimmerItem("explicitFan"), "Fan,Fan.ActiveStatus", List.of()),
                Arguments.of(new DimmerItem("multipleAccessories"), "Lighting,Fan", List.of()),
                Arguments.of(new ColorItem("trailingComma"), "Lighting,", List.of(BRIGHTNESS, HUE, SATURATION)),
                Arguments.of(new ColorItem("unknownCharacteristic"), "Lighting.Typo",
                        List.of(BRIGHTNESS, HUE, SATURATION)),
                Arguments.of(new ColorItem("whitespace"), " Lighting ", List.of(BRIGHTNESS, HUE, SATURATION)));
    }

    @ParameterizedTest
    @MethodSource("optionalCharacteristics")
    void linksOnlyExpectedOptionalCharacteristics(GenericItem item, String value,
            List<HomekitCharacteristicType> expected) throws Exception {
        assertOptionalCharacteristics(item, value, Map.of(), expected);
    }

    @ParameterizedTest
    @MethodSource("configuredCharacteristics")
    void metadataConfigurationPreservesAutomaticLinks(Map<String, Object> configuration,
            List<HomekitCharacteristicType> expected) throws Exception {
        assertOptionalCharacteristics(new ColorItem("configuredLight"), "Lighting", configuration, expected);
    }

    static Stream<Arguments> configuredCharacteristics() {
        return Stream.of(Arguments.of(Map.of("Name", "My light"), List.of(BRIGHTNESS, HUE, SATURATION)),
                Arguments.of(Map.of("Manufacturer", "IKEA"), List.of(BRIGHTNESS, HUE, SATURATION)),
                Arguments.of(Map.of("commandDelay", 100), List.of(BRIGHTNESS, HUE, SATURATION)));
    }

    @Test
    void untypedGroupUsesOnlyExplicitMemberCharacteristics() throws Exception {
        GroupItem group = new GroupItem("lights");
        ColorItem color = new ColorItem("colorMember");
        group.addMember(color);
        MetadataRegistry registry = mock(MetadataRegistry.class);
        when(registry.get(new MetadataKey("homekit", group.getName())))
                .thenReturn(new Metadata(new MetadataKey("homekit", group.getName()), "Lighting", Map.of()));
        when(registry.get(new MetadataKey("homekit", color.getName())))
                .thenReturn(new Metadata(new MetadataKey("homekit", color.getName()), "Lighting.OnState", Map.of()));
        HomekitTaggedItem tagged = new HomekitTaggedItem(new HomekitOHItemProxy(group), HomekitAccessoryType.LIGHTBULB,
                Map.of());
        AbstractHomekitAccessoryImpl accessory = HomekitAccessoryFactory.create(tagged, registry,
                mock(HomekitAccessoryUpdater.class), new HomekitSettings());
        assertSame(color, accessory.getCharacteristic(ON_STATE).orElseThrow().getItem());
        assertTrue(accessory.getCharacteristic(BRIGHTNESS).isEmpty());
        assertTrue(accessory.getCharacteristic(HUE).isEmpty());
        assertTrue(accessory.getCharacteristic(SATURATION).isEmpty());
    }

    private void assertOptionalCharacteristics(GenericItem item, String value, Map<String, Object> configuration,
            List<HomekitCharacteristicType> expected) throws Exception {
        MetadataRegistry registry = mock(MetadataRegistry.class);
        when(registry.get(any(MetadataKey.class)))
                .thenReturn(new Metadata(new MetadataKey("homekit", item.getName()), value, configuration));
        HomekitAccessoryType type = HomekitAccessoryType.valueOfTag(value.split(",")[0].split("\\.")[0].trim())
                .orElseThrow();
        HomekitTaggedItem tagged = new HomekitTaggedItem(new HomekitOHItemProxy(item), type, configuration);
        AbstractHomekitAccessoryImpl accessory = HomekitAccessoryFactory.create(tagged, registry,
                mock(HomekitAccessoryUpdater.class), new HomekitSettings());
        for (HomekitCharacteristicType characteristic : List.of(BRIGHTNESS, HUE, SATURATION, ROTATION_SPEED)) {
            assertEquals(expected.contains(characteristic), accessory.getCharacteristic(characteristic).isPresent(),
                    characteristic.getTag());
            accessory.getCharacteristic(characteristic).ifPresent(link -> {
                assertSame(item, link.getItem());
                assertSame(tagged.getProxyItem(), link.getProxyItem());
            });
        }
    }
}
