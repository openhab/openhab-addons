/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.automation.java223.internal.codegeneration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.automation.module.script.ScriptExtensionAccessor;
import org.openhab.core.automation.module.script.internal.defaultscope.ItemRegistryDelegate;
import org.openhab.core.internal.items.DefaultStateDescriptionFragmentProvider;
import org.openhab.core.internal.items.ItemRegistryImpl;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.library.types.OnOffType;

/**
 * Test for generating code for imports and default injected field member.
 *
 * @author Gwendal Roulleau - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
class InjectedCodeGeneratorTest {

    @Mock
    @NonNullByDefault({})
    ScriptExtensionAccessor scripExtensionAccessor;
    @Mock
    @NonNullByDefault({})
    MetadataRegistry medataRegistry;
    @Mock
    @NonNullByDefault({})
    DefaultStateDescriptionFragmentProvider defaultStateDescriptionFragmentProvider;

    @Test
    void nameParseBindingsTest() {

        // given
        Map<String, Object> defaultPresets = new HashMap<>();
        ItemRegistryImpl itemRegistry = new ItemRegistryImpl(medataRegistry, defaultStateDescriptionFragmentProvider);
        defaultPresets.put("ir", itemRegistry);
        defaultPresets.put("items", new ItemRegistryDelegate(itemRegistry));
        defaultPresets.put("ON", OnOffType.ON);
        defaultPresets.put("OFF", OnOffType.OFF);

        Mockito.when(scripExtensionAccessor.findDefaultPresets("")).thenReturn(defaultPresets);

        // when
        InjectedCodeGenerator injectedCodeGenerator = new InjectedCodeGenerator(scripExtensionAccessor);

        // then
        List<String> injectedFieldsDeclaration = Arrays
                .stream(injectedCodeGenerator.getInjectedFieldsDeclaration().split("\n")).map(String::strip).toList();
        assertThat(injectedFieldsDeclaration).contains("protected @InjectBinding ItemRegistry ir;");
        assertThat(injectedFieldsDeclaration).contains("protected @InjectBinding Map<String, State> items;");

        List<String> defaultPresetImportList = Arrays
                .stream(injectedCodeGenerator.getDefaultPresetImportList().split("\n")).map(String::strip).toList();
        assertThat(defaultPresetImportList).contains("import org.openhab.core.items.ItemRegistry;");

        assertThat(injectedCodeGenerator.getEnumByType()).containsEntry("OnOffType", Set.of("ON", "OFF"));
    }
}
