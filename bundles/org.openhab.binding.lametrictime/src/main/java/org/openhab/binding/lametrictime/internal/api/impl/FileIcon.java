/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import java.nio.file.Files;
import java.nio.file.Path;

import javax.activation.MimetypesFileTypeMap;

/**
 * Implementation class for file icons.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class FileIcon extends AbstractDataIcon {
    private final MimetypesFileTypeMap mimeTypeMap = new MimetypesFileTypeMap();

    private final Path path;

    public FileIcon(File file) {
        this(file.toPath());
    }

    public FileIcon(Path path) {
        this.path = path;
        mimeTypeMap.addMimeTypes("image/png png PNG");
    }

    @Override
    protected void populateFields() {
        setType(mimeTypeMap.getContentType(path.toFile()));
        try {
            setData(Files.readAllBytes(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
