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
package org.openhab.binding.tibberpulselocal.internal.handler;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Guards that the handlers only use the {@code BaseThingHandler.updateStatus} signatures that exist in core 5.2 and
 * 5.3. The live instance runs core 5.2, where {@code updateStatus(ThingStatusDetail, String)} and
 * {@code updateStatus(ThingStatusDetail)} (added in core 5.3) throw {@link NoSuchMethodError}.
 * <p>
 * Remove this test together with the switch back to the core 5.3 form.
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
class Core52CompatibilityTest {

    private static final String DETAIL_AND_TEXT = "updateStatus(Lorg/openhab/core/thing/ThingStatusDetail;Ljava/lang/String;)V";
    private static final String DETAIL_ONLY = "updateStatus(Lorg/openhab/core/thing/ThingStatusDetail;)V";

    @Test
    void whenTheHandlersAreCompiledThenNoneReferencesAnUpdateStatusSignatureThatIsNewInCore53() throws Exception {
        for (Class<?> handler : List.of(BridgeHandler.class, MeterHandler.class)) {
            // The inner classes (e.g. the stream callback of the bridge) call the outer updateStatus as well.
            List<String> names = new ArrayList<>();
            names.add(handler.getName());
            names.addAll(innerClassNames(handler));
            for (String name : names) {
                List<String> methods = methodReferences(handler, name);
                assertFalse(methods.contains(DETAIL_AND_TEXT), name);
                assertFalse(methods.contains(DETAIL_ONLY), name);
            }
        }
    }

    private static List<String> innerClassNames(Class<?> clazz) throws IOException, URISyntaxException {
        URL url = clazz.getResource(clazz.getSimpleName() + ".class");
        assertNotNull(url);
        Path file = Paths.get(url.toURI());
        List<String> names = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(file.getParent(),
                clazz.getSimpleName() + "$*.class")) {
            for (Path path : stream) {
                String fileName = path.getFileName().toString();
                names.add(clazz.getPackageName() + "." + fileName.substring(0, fileName.length() - ".class".length()));
            }
        }
        return names;
    }

    /** Reads the method references (name and descriptor) from the constant pool of a compiled class. */
    private static List<String> methodReferences(Class<?> anchor, String className) throws IOException {
        String resource = className.substring(className.lastIndexOf('.') + 1) + ".class";
        try (InputStream in = anchor.getResourceAsStream(resource)) {
            assertNotNull(in, resource);
            DataInputStream data = new DataInputStream(new ByteArrayInputStream(in.readAllBytes()));
            data.readInt();
            data.readUnsignedShort();
            data.readUnsignedShort();
            int count = data.readUnsignedShort();
            String[] utf8 = new String[count];
            int[] refNameAndType = new int[count];
            int[] natName = new int[count];
            int[] natDescriptor = new int[count];
            for (int i = 1; i < count; i++) {
                int tag = data.readUnsignedByte();
                switch (tag) {
                    case 1 -> utf8[i] = data.readUTF();
                    case 3, 4, 17, 18 -> data.readInt();
                    case 5, 6 -> {
                        data.readLong();
                        i++;
                    }
                    case 7, 8, 16, 19, 20 -> data.readUnsignedShort();
                    case 9, 10, 11 -> {
                        data.readUnsignedShort();
                        refNameAndType[i] = data.readUnsignedShort();
                    }
                    case 12 -> {
                        natName[i] = data.readUnsignedShort();
                        natDescriptor[i] = data.readUnsignedShort();
                    }
                    case 15 -> {
                        data.readUnsignedByte();
                        data.readUnsignedShort();
                    }
                    default -> throw new AssertionError("constant pool tag " + tag);
                }
            }
            List<String> methods = new ArrayList<>();
            for (int i = 1; i < count; i++) {
                if (refNameAndType[i] != 0) {
                    methods.add(utf8[natName[refNameAndType[i]]] + utf8[natDescriptor[refNameAndType[i]]]);
                }
            }
            return methods;
        }
    }
}
