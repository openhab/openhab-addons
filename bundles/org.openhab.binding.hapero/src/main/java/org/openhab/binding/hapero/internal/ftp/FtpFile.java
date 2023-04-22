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
package org.openhab.binding.hapero.internal.ftp;

import java.io.IOException;

import org.apache.commons.net.ftp.FTPFile;
import org.eclipse.jdt.annotation.NonNullByDefault;

import com.github.drapostolos.rdp4j.spi.FileElement;

/**
 * {@link FtpFile} is a helper class for FTP file monitoring
 *
 * @author Daniel Walter - Initial contribution
 */
@NonNullByDefault
public class FtpFile implements FileElement {
    private final FTPFile file;
    private final String name;
    private final boolean isDirectory;

    /**
     * Constructor
     *
     * @param file
     */
    public FtpFile(FTPFile file) {
        this.file = file;
        this.name = file.getName();
        this.isDirectory = file.isDirectory();
    }

    @Override
    public long lastModified() throws IOException {
        return file.getTimestamp().getTimeInMillis();
    }

    @Override
    public boolean isDirectory() {
        return isDirectory;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return file.toString();
    }
}
