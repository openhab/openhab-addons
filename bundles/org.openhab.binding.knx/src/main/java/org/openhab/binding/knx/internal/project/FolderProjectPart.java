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
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.knx.internal.project.KnxProjectException.Reason;

/**
 * An unprotected project part: a folder {@code P-xxxx/} in the outer archive.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
final class FolderProjectPart implements ProjectPart {

    private static final Pattern INSTALLATION_FILE = Pattern.compile("(\\d{1,9})\\.xml", Pattern.CASE_INSENSITIVE);

    private final OuterContainer container;
    private final String id;
    private final ZipEntry projectFile;
    private final List<ZipEntry> installationFiles;

    FolderProjectPart(OuterContainer container, String folder, ZipEntry projectFile) {
        this.container = container;
        this.id = folder;
        this.projectFile = projectFile;
        String prefix = folder.toLowerCase(Locale.ROOT) + "/";
        record Numbered(int number, ZipEntry entry) {
        }
        List<Numbered> found = new ArrayList<>();
        for (String name : container.entryNames()) {
            if (!name.toLowerCase(Locale.ROOT).startsWith(prefix)) {
                continue;
            }
            Matcher matcher = INSTALLATION_FILE.matcher(name.substring(prefix.length()));
            ZipEntry entry = container.entry(name);
            if (matcher.matches() && entry != null) {
                found.add(new Numbered(Integer.parseInt(matcher.group(1)), entry));
            }
        }
        found.sort(Comparator.comparingInt(Numbered::number));
        this.installationFiles = found.stream().map(Numbered::entry).toList();
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public InputStream openProjectXml() throws IOException, KnxProjectException {
        return container.open(projectFile);
    }

    @Override
    public InputStream openInstallationXml() throws IOException, KnxProjectException {
        if (installationFiles.isEmpty()) {
            throw new KnxProjectException(Reason.MALFORMED, "the project part has no installation file");
        }
        return container.open(installationFiles.get(0));
    }

    @Override
    public int installationFileCount() {
        return installationFiles.size();
    }
}
