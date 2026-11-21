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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adds what the manufacturer files define to a project: the products of the devices and a
 * {@link KnxGroupObject} for every object instance. The way is device, {@code Hardware.xml}, program file; each
 * file is read at most once per read, one program file at a time, and only what the devices need is kept.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
final class ManufacturerReader {

    private final Logger logger = LoggerFactory.getLogger(ManufacturerReader.class);
    private final OuterContainer container;
    private final ReadBudget budget;
    private final @Nullable String language;
    private final int maxPasses;
    private int hardwareFiles;
    private int programFiles;
    private int passes;
    private int ambiguousPrograms;
    private int unresolvedBases;
    private int passBoundPrograms;

    /** @param language the language tag of the texts, {@code null} for the programs' default texts */
    ManufacturerReader(OuterContainer container, ReadBudget budget, @Nullable String language) {
        this(container, budget, language, ProgramReader.MAX_PASSES);
    }

    /** @param maxPasses the bound of passes over one program file, lowered by tests */
    ManufacturerReader(OuterContainer container, ReadBudget budget, @Nullable String language, int maxPasses) {
        this.container = container;
        this.budget = budget;
        this.language = language;
        this.maxPasses = maxPasses;
    }

    /** Objects with a {@code BaseNumber} whose device number could not be worked out, in this reader's reads. */
    int unresolvedBases() {
        return unresolvedBases;
    }

    /** Program files that still missed wanted entries after {@link ProgramReader#MAX_PASSES} passes. */
    int passBoundPrograms() {
        return passBoundPrograms;
    }

    /** Passes over program files, in this reader's reads. */
    int passes() {
        return passes;
    }

    KnxProject read(KnxProject project) throws IOException, KnxProjectException {
        List<KnxDevice> devices = project.topology().devices();
        ManufacturerEntries entries = ManufacturerEntries.index(container.entryNames());
        HardwareReader hardware = readHardware(devices, entries);

        List<List<KnxGroupObject>> perDevice = new ArrayList<>(devices.size());
        Map<String, List<Integer>> devicesByProgram = new LinkedHashMap<>();
        for (int i = 0; i < devices.size(); i++) {
            KnxDevice device = devices.get(i);
            perDevice.add(List.of());
            String hardware2Program = device.hardware2ProgramRefId();
            if (hardware2Program == null || !hardware.hasHardware2Program(hardware2Program)) {
                perDevice.set(i, GroupObjects.withoutDefinition(device, KnxObjectDefinition.NO_HARDWARE));
                continue;
            }
            String program = hardware.programOf(hardware2Program);
            if (program == null) {
                perDevice.set(i, GroupObjects.withoutDefinition(device, KnxObjectDefinition.NO_PROGRAM_FILE));
            } else if (!device.objects().isEmpty()) {
                List<Integer> users = devicesByProgram.get(program);
                if (users == null) {
                    users = new ArrayList<>();
                    devicesByProgram.put(program, users);
                }
                users.add(i);
            }
        }
        for (Map.Entry<String, List<Integer>> program : devicesByProgram.entrySet()) {
            ProgramFile file = readProgram(program.getKey(), program.getValue(), devices, entries);
            for (int i : program.getValue()) {
                KnxDevice device = devices.get(i);
                perDevice.set(i,
                        file != null && file.programFound()
                                ? GroupObjects.resolve(device, file, () -> unresolvedBases++)
                                : GroupObjects.withoutDefinition(device, KnxObjectDefinition.NO_PROGRAM_FILE));
            }
        }

        List<KnxGroupObject> groupObjects = new ArrayList<>();
        perDevice.forEach(groupObjects::addAll);
        Map<String, KnxProduct> products = new LinkedHashMap<>();
        for (KnxDevice device : devices) {
            String id = device.productRefId();
            KnxProduct product = id == null ? null : hardware.product(id);
            if (product != null) {
                products.putIfAbsent(product.id(), product);
            }
        }
        budget.countRecords(groupObjects.size());
        logCounts(groupObjects);
        return project.withManufacturerData(List.copyOf(products.values()), groupObjects);
    }

    private HardwareReader readHardware(List<KnxDevice> devices, ManufacturerEntries entries)
            throws IOException, KnxProjectException {
        Set<String> folders = new LinkedHashSet<>();
        Set<String> hardware2Programs = new LinkedHashSet<>();
        Set<String> products = new LinkedHashSet<>();
        for (KnxDevice device : devices) {
            addIfPresent(folders, device.manufacturerRefId());
            addIfPresent(hardware2Programs, device.hardware2ProgramRefId());
            addIfPresent(products, device.productRefId());
        }
        HardwareReader hardware = new HardwareReader(hardware2Programs, products);
        for (String folder : folders) {
            for (String name : entries.hardware(folder)) {
                ZipEntry entry = container.entry(name);
                if (entry == null || entry.isDirectory()) {
                    continue;
                }
                try (InputStream in = container.open(entry)) {
                    hardware.read(in, name, budget);
                }
                hardwareFiles++;
            }
        }
        return hardware;
    }

    private static void addIfPresent(Set<String> values, @Nullable String value) {
        if (value != null) {
            values.add(value);
        }
    }

    /** {@code null} when the container has no file of that name. */
    private @Nullable ProgramFile readProgram(String programId, List<Integer> users, List<KnxDevice> devices,
            ManufacturerEntries entries) throws IOException, KnxProjectException {
        ZipEntry entry = programEntry(programId, entries);
        if (entry == null) {
            return null;
        }
        ProgramFile file = new ProgramFile(programId, entry.getName(), budget, language);
        for (int i : users) {
            KnxDevice device = devices.get(i);
            for (KnxObjectInstance instance : device.objects()) {
                for (String id : GroupObjects.referenceIds(instance.refId(), programId)) {
                    file.comObjectRefs.want(id);
                }
            }
            for (KnxModuleInstance module : device.modules()) {
                for (KnxModuleArgument argument : module.arguments()) {
                    file.wantArgument(argument.refId());
                    String value = argument.value();
                    if (value != null && ObjectNames.isAllocator(value)) {
                        file.allocators.want(programId + "_" + value);
                    }
                }
            }
        }
        passes += ProgramReader.read(container, entry, file, budget, maxPasses);
        programFiles++;
        if (file.passBoundReached()) {
            passBoundPrograms++;
        }
        return file;
    }

    /**
     * Of several entries with the wanted file name, the one in the folder of the program's own manufacturer,
     * else the first.
     */
    private @Nullable ZipEntry programEntry(String programId, ManufacturerEntries entries) {
        List<String> names = entries.programs(programId);
        if (names.isEmpty()) {
            return null;
        }
        String chosen = names.get(0);
        if (names.size() > 1) {
            ambiguousPrograms++;
            int end = programId.indexOf('_');
            String ownFolder = (end < 0 ? programId : programId.substring(0, end)) + "/";
            for (String name : names) {
                if (name.startsWith(ownFolder)) {
                    chosen = name;
                    break;
                }
            }
        }
        ZipEntry entry = container.entry(chosen);
        return entry == null || entry.isDirectory() ? null : entry;
    }

    private void logCounts(List<KnxGroupObject> groupObjects) {
        if (!logger.isDebugEnabled()) {
            return;
        }
        Map<KnxObjectDefinition, Integer> states = new EnumMap<>(KnxObjectDefinition.class);
        for (KnxGroupObject object : groupObjects) {
            states.merge(object.definition(), 1, Integer::sum);
        }
        logger.debug(
                "Read {} hardware files and {} program files in {} passes, {} program files found under more than"
                        + " one entry, {} program files still missing entries after the last pass, {} object numbers"
                        + " without a resolvable base; group objects by definition: {}",
                hardwareFiles, programFiles, passes, ambiguousPrograms, passBoundPrograms, unresolvedBases, states);
    }
}
