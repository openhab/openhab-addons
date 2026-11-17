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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.project.KnxProjectException.Reason;

/**
 * The outer zip archive of a {@code .knxproj} file. Entries are read in place, never unpacked to disk, and
 * every read goes through the {@link ReadBudget}.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
final class OuterContainer implements Closeable {

    private final ZipFile zip;
    private final ReadBudget budget;
    private final List<String> names;
    private final Path path;
    private final Consumer<String> openedEntries;
    private @Nullable EntryRanges ranges;

    private OuterContainer(ZipFile zip, Path path, ReadBudget budget, Consumer<String> openedEntries) {
        this.zip = zip;
        this.path = path;
        this.budget = budget;
        this.openedEntries = openedEntries;
        this.names = zip.stream().map(ZipEntry::getName).toList();
    }

    static OuterContainer open(Path file, ReadBudget budget) throws IOException, KnxProjectException {
        return open(file, budget, name -> {
        });
    }

    /**
     * @param openedEntries is given the name of every entry before it is read; for tests
     */
    static OuterContainer open(Path file, ReadBudget budget, Consumer<String> openedEntries)
            throws IOException, KnxProjectException {
        ZipFile zip;
        try {
            // Names without the UTF-8 flag are decoded as ISO 8859-1, which accepts every byte; the names this
            // reader looks for are ASCII, so a container with other names still opens.
            zip = new ZipFile(file.toFile(), ZipFile.OPEN_READ, StandardCharsets.ISO_8859_1);
        } catch (ZipException e) {
            throw new KnxProjectException(Reason.NOT_A_PROJECT, "the file is not a zip archive");
        }
        try {
            budget.checkEntryCount(zip.size());
            return new OuterContainer(zip, file, budget, openedEntries);
        } catch (KnxProjectException | RuntimeException e) {
            zip.close();
            throw e;
        }
    }

    List<String> entryNames() {
        return names;
    }

    @Nullable
    ZipEntry entry(String name) {
        return zip.getEntry(name);
    }

    InputStream open(ZipEntry entry) throws IOException, KnxProjectException {
        EntryRanges known = ranges;
        if (known == null) {
            known = EntryRanges.read(path, zip.size());
            ranges = known;
        }
        known.check(entry);
        openedEntries.accept(entry.getName());
        try {
            return budget.limit(zip.getInputStream(entry), entry.getName(), entry.getCompressedSize());
        } catch (ZipException e) {
            throw new KnxProjectException(Reason.MALFORMED, "entry " + entry.getName() + " cannot be read");
        }
    }

    ReadBudget budget() {
        return budget;
    }

    @Override
    public void close() throws IOException {
        zip.close();
    }
}
