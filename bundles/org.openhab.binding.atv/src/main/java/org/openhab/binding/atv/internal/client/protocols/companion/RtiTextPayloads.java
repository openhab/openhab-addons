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
package org.openhab.binding.atv.internal.client.protocols.companion;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.support.BinaryPlist;
import org.openhab.binding.atv.internal.client.support.Uid;

/**
 * Builders for the NSKeyedArchiver payloads sent to the RTI (text input) service.
 *
 * <p>
 * The archives are hand-assembled, since there's no general NSKeyedArchiver encoder
 * available, and serialized as binary plists with insertion order preserved.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class RtiTextPayloads {

    private RtiTextPayloads() {
    }

    /**
     * Prepares an NSKeyedArchiver encoded payload for clearing the RTI text.
     *
     * @param sessionUuid the RTI session UUID bytes
     * @return binary plist bytes
     */
    public static byte[] clearTextPayload(byte[] sessionUuid) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("$version", 100000L);
        root.put("$archiver", "RTIKeyedArchiver");
        root.put("$top", orderedMap("textOperations", new Uid(1)));

        Map<String, Object> textOperations = new LinkedHashMap<>();
        textOperations.put("$class", new Uid(7));
        textOperations.put("targetSessionUUID", new Uid(5));
        textOperations.put("keyboardOutput", new Uid(2));
        textOperations.put("textToAssert", new Uid(4));

        root.put("$objects",
                List.of("$null", textOperations, orderedMap("$class", new Uid(3)), classDescriptor("TIKeyboardOutput"),
                        "", orderedMap("NS.uuidbytes", sessionUuid, "$class", new Uid(6)), classDescriptor("NSUUID"),
                        classDescriptor("RTITextOperations")));

        return BinaryPlist.dump(root);
    }

    /**
     * Prepares an NSKeyedArchiver encoded payload for RTI text input.
     *
     * @param sessionUuid the RTI session UUID bytes
     * @param text text to insert
     * @return binary plist bytes
     */
    public static byte[] inputTextPayload(byte[] sessionUuid, String text) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("$version", 100000L);
        root.put("$archiver", "RTIKeyedArchiver");
        root.put("$top", orderedMap("textOperations", new Uid(1)));

        Map<String, Object> textOperations = new LinkedHashMap<>();
        textOperations.put("keyboardOutput", new Uid(2));
        textOperations.put("$class", new Uid(7));
        textOperations.put("targetSessionUUID", new Uid(5));

        root.put("$objects",
                List.of("$null", textOperations, orderedMap("insertionText", new Uid(3), "$class", new Uid(4)), text,
                        classDescriptor("TIKeyboardOutput"),
                        orderedMap("NS.uuidbytes", sessionUuid, "$class", new Uid(6)), classDescriptor("NSUUID"),
                        classDescriptor("RTITextOperations")));

        return BinaryPlist.dump(root);
    }

    private static Map<String, Object> classDescriptor(String className) {
        return orderedMap("$classname", className, "$classes", List.of(className, "NSObject"));
    }

    private static Map<String, Object> orderedMap(Object... keyValues) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put((String) keyValues[i], keyValues[i + 1]);
        }
        return map;
    }
}
