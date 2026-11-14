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
package org.openhab.binding.lametrictime.internal.api.impl;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation class for file icons.
 *
 * @author Gregory Moyer - Initial contribution
 */
@NonNullByDefault
public class FileIcon extends AbstractDataIcon {
    private final Logger logger = LoggerFactory.getLogger(FileIcon.class);

    private final Path path;

    public FileIcon(File file) {
        this(file.toPath());
    }

    public FileIcon(Path path) {
        this.path = path;
    }

    @Override
    protected void populateFields() {
        // the JDK file name map, as Jakarta Activation 2 needs a separate provider for MIME type lookups
        String type = URLConnection.guessContentTypeFromName(path.getFileName().toString().toLowerCase(Locale.ROOT));
        setType(type != null ? type : "application/octet-stream");
        try {
            setData(Files.readAllBytes(path));
        } catch (IOException e) {
            logger.warn("Failed reading icon content.", e);
        }
    }
}
