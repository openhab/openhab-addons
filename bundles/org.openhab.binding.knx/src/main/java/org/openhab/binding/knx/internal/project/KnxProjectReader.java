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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.project.KnxProjectException.Reason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads an ETS project file ({@code .knxproj}) into a {@link KnxProject}.
 * <p>
 * The file is read in place with streaming XML parsing and held to {@link ReadLimits}; nothing is unpacked
 * to disk. Keys, passwords and other secrets in the file are skipped and appear neither in the model nor in
 * a log or an exception. An instance holds no state of a read and can be used from several threads.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public final class KnxProjectReader {

    private static final Pattern PROJECT_FOLDER = Pattern.compile("(P-[^/]+)/project\\.xml", Pattern.CASE_INSENSITIVE);
    private static final Pattern PROTECTED_PART = Pattern.compile("(P-[^/]+)\\.zip", Pattern.CASE_INSENSITIVE);

    private final Logger logger = LoggerFactory.getLogger(KnxProjectReader.class);
    private final ReadLimits limits;
    private final Consumer<String> openedEntries;

    public KnxProjectReader() {
        this(ReadLimits.DEFAULT);
    }

    KnxProjectReader(ReadLimits limits) {
        this(limits, name -> {
        });
    }

    /**
     * @param openedEntries is given the name of every entry of the outer archive before it is read; must be
     *            safe to call from several threads when the reader is
     */
    KnxProjectReader(ReadLimits limits, Consumer<String> openedEntries) {
        this.limits = limits;
        this.openedEntries = openedEntries;
    }

    /**
     * Reads the project with the texts of group objects in each program's default language.
     *
     * @param file the {@code .knxproj} file
     * @param password the project password, needed only for a protected project; the array is not changed or
     *            kept
     * @throws KnxProjectException if the file is not a readable project; {@link KnxProjectException#getReason()}
     *             says why
     * @throws IOException if the file cannot be read at all
     */
    public KnxProject read(Path file, char @Nullable [] password) throws IOException, KnxProjectException {
        return read(file, password, null);
    }

    /**
     * Reads the project with the texts of group objects in a language.
     *
     * @param file the {@code .knxproj} file
     * @param password the project password, needed only for a protected project; the array is not changed or
     *            kept
     * @param language a language tag such as {@code de-DE}, in which an underscore counts as a hyphen
     *            ({@code de_DE} is {@code de-DE}); per application program a tag equal to the program's default
     *            language (ignoring case) gives the default texts; else the language equal to the tag
     *            (ignoring case) is taken, else the first with the same language part, else the default texts;
     *            {@code null} or blank for the default texts
     * @throws KnxProjectException if the file is not a readable project; {@link KnxProjectException#getReason()}
     *             says why
     * @throws IOException if the file cannot be read at all
     */
    public KnxProject read(Path file, char @Nullable [] password, @Nullable String language)
            throws IOException, KnxProjectException {
        ReadBudget budget = new ReadBudget(limits);
        try (OuterContainer container = OuterContainer.open(file, budget, openedEntries)) {
            ProjectPart part = openProjectPart(container, password);
            KnxProjectInfo info;
            try (InputStream in = part.openProjectXml()) {
                info = ProjectInfoReader.read(in, part.id(), budget);
            }
            KnxProject project;
            try (InputStream in = part.openInstallationXml()) {
                project = new InstallationReader(info, budget).read(in, part.installationFileCount() - 1);
            }
            if (project.ignoredInstallations() > 0) {
                logger.debug("Read the first installation of the project, {} more not read",
                        project.ignoredInstallations());
            }
            return new ManufacturerReader(container, budget, language).read(project);
        }
    }

    private ProjectPart openProjectPart(OuterContainer container, char @Nullable [] password)
            throws IOException, KnxProjectException {
        List<ProjectPart> folders = new ArrayList<>();
        List<ZipEntry> protectedParts = new ArrayList<>();
        for (String name : container.entryNames()) {
            ZipEntry entry = container.entry(name);
            if (entry == null || entry.isDirectory()) {
                continue;
            }
            Matcher folder = PROJECT_FOLDER.matcher(name);
            if (folder.matches()) {
                folders.add(new FolderProjectPart(container, folder.group(1), entry));
            } else if (PROTECTED_PART.matcher(name).matches()) {
                protectedParts.add(entry);
            }
        }
        if (folders.size() + protectedParts.size() > 1) {
            throw new KnxProjectException(Reason.MALFORMED, "the file holds more than one project");
        }
        if (!folders.isEmpty()) {
            return folders.get(0);
        }
        if (!protectedParts.isEmpty()) {
            return openProtectedPart(container, protectedParts.get(0), password);
        }
        throw new KnxProjectException(Reason.NOT_A_PROJECT, "the archive holds no KNX project");
    }

    private ProjectPart openProtectedPart(OuterContainer container, ZipEntry entry, char @Nullable [] password)
            throws IOException, KnxProjectException {
        Matcher name = PROTECTED_PART.matcher(entry.getName());
        if (!name.matches()) {
            throw new IllegalStateException("not a protected project part");
        }
        return ProtectedProjectPart.open(container, entry, name.group(1), password);
    }
}
