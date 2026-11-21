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
package org.openhab.automation.java223.internal.codegeneration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openhab.core.automation.type.ModuleTypeRegistry;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingUID;
import org.osgi.framework.BundleContext;

/**
 * End to end test of the code generation of the {@code Items} and {@code Things} helper classes, with item names
 * and thing UIDs which are not directly usable as Java member names.
 * <p>
 * The generated source code must be valid Java: the identifiers have to be sanitized, while the values (the real
 * item names and the real thing UIDs) must be left untouched.
 *
 * @author Gwendal Roulleau - Initial contribution
 */
@NonNullByDefault
public class SourceGeneratorEndToEndTest {

    @TempDir
    @NonNullByDefault({})
    private Path tempDir;

    @Test
    public void itemsClassIsGeneratedWithSanitizedIdentifiers() throws Exception {
        // an item named "class" is a valid openHAB item name, but it is a Java reserved keyword
        SwitchItem keywordItem = new SwitchItem("class");
        keywordItem.setLabel("A switch named class");
        SwitchItem regularItem = new SwitchItem("my_switch");

        ItemRegistry itemRegistry = mock(ItemRegistry.class);
        when(itemRegistry.getItems()).thenReturn(List.of(keywordItem, regularItem));

        SourceGenerator generator = newGenerator(itemRegistry, mock(ThingRegistry.class));
        invokeGenerate(generator, "internalGenerateItems");

        String source = readGeneratedSource("Items");
        assertTrue(source.contains("public static final String class_ = \"class\";"),
                "The reserved keyword must be sanitized, but the value must be kept as is:\n" + source);
        assertTrue(source.contains("public static final String my_switch = \"my_switch\";"),
                "A valid name must not be modified:\n" + source);
        assertTrue(source.contains("public SwitchItem class_()"), "The accessor method must be sanitized:\n" + source);
        assertTrue(source.contains("getItem(class_)"), "The accessor must use the sanitized name:\n" + source);
        assertFalse(source.contains("public SwitchItem class()"), "The reserved keyword must not be used:\n" + source);
    }

    @Test
    public void thingsClassIsGeneratedWithSanitizedIdentifiers() throws Exception {
        Thing thing = mock(Thing.class);
        when(thing.getUID()).thenReturn(new ThingUID("mqtt", "topic", "my-topic"));
        when(thing.getLabel()).thenReturn("A thing");

        ThingRegistry thingRegistry = mock(ThingRegistry.class);
        when(thingRegistry.getAll()).thenReturn(List.of(thing));

        SourceGenerator generator = newGenerator(mock(ItemRegistry.class), thingRegistry);
        invokeGenerate(generator, "internalGenerateThings");

        String source = readGeneratedSource("Things");
        assertTrue(source.contains("public static final String mqtt_topic_my_topic = \"mqtt:topic:my-topic\";"),
                "The identifier must be sanitized, but the UID must be kept as is:\n" + source);
        assertTrue(source.contains("public Thing mqtt_topic_my_topic()"), "The accessor must be sanitized:\n" + source);
        assertTrue(source.contains("getThing(\"mqtt:topic:my-topic\")"), "The UID must be kept as is:\n" + source);
    }

    /**
     * A label containing a comment terminating sequence must not be able to close the comment it is inserted in,
     * otherwise the rest of the label is interpreted as Java code.
     */
    @Test
    public void itemsClassIsGeneratedWithSanitizedLabels() throws Exception {
        SwitchItem item = new SwitchItem("my_switch");
        item.setLabel("Broken */ public static String evil = \"injected\"; /* label");

        ItemRegistry itemRegistry = mock(ItemRegistry.class);
        when(itemRegistry.getItems()).thenReturn(List.of(item));

        SourceGenerator generator = newGenerator(itemRegistry, mock(ThingRegistry.class));
        invokeGenerate(generator, "internalGenerateItems");

        String source = readGeneratedSource("Items");
        assertTrue(source.contains("*\\/"), "The comment terminating sequence must be neutralized:\n" + source);
        assertTrue(source.contains("/\\*"), "The comment opening sequence must be neutralized:\n" + source);
        // the injected code must stay inside the comment: the label comments must be closed only at their end
        assertEquals(2, countOccurrences(source, "label */"),
                "The label must not be able to close the comment prematurely:\n" + source);
        // the label is inserted twice (once for the constant, once for the accessor method)
        assertEquals(2, countOccurrences(source, "public static String evil"),
                "The injected code must be neutralized in both occurrences:\n" + source);
    }

    /**
     * Same as above, for the thing labels.
     */
    @Test
    public void thingsClassIsGeneratedWithSanitizedLabels() throws Exception {
        Thing thing = mock(Thing.class);
        when(thing.getUID()).thenReturn(new ThingUID("mqtt", "topic", "my-topic"));
        when(thing.getLabel()).thenReturn("Broken */ public static String evil = \"injected\"; /* label");

        ThingRegistry thingRegistry = mock(ThingRegistry.class);
        when(thingRegistry.getAll()).thenReturn(List.of(thing));

        SourceGenerator generator = newGenerator(mock(ItemRegistry.class), thingRegistry);
        invokeGenerate(generator, "internalGenerateThings");

        String source = readGeneratedSource("Things");
        assertTrue(source.contains("*\\/"), "The comment terminating sequence must be neutralized:\n" + source);
        assertTrue(source.contains("/\\*"), "The comment opening sequence must be neutralized:\n" + source);
        assertEquals(2, countOccurrences(source, "label */"),
                "The label must not be able to close the comment prematurely:\n" + source);
    }

    /**
     * Counts the occurrences of the given text in the given source.
     */
    private int countOccurrences(String source, String text) {
        int count = 0;
        int index = source.indexOf(text);
        while (index >= 0) {
            count++;
            index = source.indexOf(text, index + text.length());
        }
        return count;
    }

    private SourceGenerator newGenerator(ItemRegistry itemRegistry, ThingRegistry thingRegistry) {
        return new SourceGenerator(new SourceWriter(tempDir), mock(DependencyGenerator.class),
                mock(InjectedCodeGenerator.class), itemRegistry, thingRegistry, mock(ModuleTypeRegistry.class),
                mock(BundleContext.class));
    }

    /**
     * Invokes the given private generation method of the {@link SourceGenerator}.
     */
    private void invokeGenerate(SourceGenerator generator, String methodName) throws Exception {
        Method method = SourceGenerator.class.getDeclaredMethod(methodName);
        method.setAccessible(true);
        method.invoke(generator);
    }

    /**
     * Reads the source code of a generated class.
     */
    private String readGeneratedSource(String className) throws IOException {
        String packageName = SourceWriter.getPackageName(SourceGenerator.GENERATED);
        Path path = tempDir.resolve(packageName.replace('.', File.separatorChar)).resolve(className + ".java");
        assertTrue(Files.exists(path), "The generated file " + path + " does not exist");
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
