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
package org.openhab.binding.knx.internal.project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The entries of the outer archive that can hold manufacturer data, indexed by name: the entries that sit
 * directly in a top-level folder {@code M-…/}. Nothing is opened here, and no entry name is ever built from an
 * id: a program file is looked up by its file name.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
final class ManufacturerEntries {

    private static final String HARDWARE = "Hardware.xml";
    private static final Set<String> NOT_A_PROGRAM = createNotAProgram();

    private final Map<String, List<String>> hardwareByFolder = new TreeMap<>();
    private final Map<String, List<String>> programsByFileName = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    private static Set<String> createNotAProgram() {
        Set<String> names = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        names.addAll(List.of(HARDWARE, "Catalog.xml", "Baggages.xml", "knx_master.xml"));
        return Collections.unmodifiableSet(names);
    }

    private ManufacturerEntries() {
    }

    static ManufacturerEntries index(List<String> entryNames) {
        ManufacturerEntries entries = new ManufacturerEntries();
        for (String name : new LinkedHashSet<>(entryNames)) {
            int slash = name.indexOf('/');
            if (slash < 0 || name.indexOf('/', slash + 1) >= 0 || !name.startsWith("M-")) {
                continue;
            }
            String folder = name.substring(0, slash);
            String fileName = name.substring(slash + 1);
            if (fileName.equalsIgnoreCase(HARDWARE)) {
                add(entries.hardwareByFolder, folder, name);
            } else if (fileName.length() > 4 && fileName.regionMatches(true, fileName.length() - 4, ".xml", 0, 4)
                    && !NOT_A_PROGRAM.contains(fileName)) {
                add(entries.programsByFileName, fileName, name);
            }
        }
        return entries;
    }

    private static void add(Map<String, List<String>> index, String key, String name) {
        List<String> names = index.get(key);
        if (names == null) {
            names = new ArrayList<>();
            index.put(key, names);
        }
        names.add(name);
    }

    /** The {@code Hardware.xml} entries of this manufacturer folder, any case, in archive order. */
    List<String> hardware(String manufacturerFolder) {
        return hardwareByFolder.getOrDefault(manufacturerFolder, List.of());
    }

    /** The entries whose file name, without regard to case, is the program id followed by {@code .xml}. */
    List<String> programs(String programId) {
        return programsByFileName.getOrDefault(programId + ".xml", List.of());
    }
}
