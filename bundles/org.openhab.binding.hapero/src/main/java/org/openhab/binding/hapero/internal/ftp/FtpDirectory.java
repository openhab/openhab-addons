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
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.openhab.binding.hapero.internal.handler.HaperoBridgeHandler;

import com.github.drapostolos.rdp4j.spi.FileElement;
import com.github.drapostolos.rdp4j.spi.PolledDirectory;

/**
 * {@link HaperoBridgeHandler} is a helper class for FTOP File monitoring
 * and also implements reading the file contents from an FTP Server.
 *
 * @author Daniel Walter - Initial contribution
 */
public class FtpDirectory implements PolledDirectory {
    private final String host;
    private final String workingDirectory;
    private final String username;
    private final String password;

    /**
     * Constructor
     *
     * @param host The Hostname of the FTP Server
     * @param workingDirectory Directory to monitor
     * @param username Username for the Server
     * @param password Password for the server
     */
    public FtpDirectory(String host, String workingDirectory, String username, String password) {
        this.host = host;
        this.workingDirectory = workingDirectory;
        this.username = username;
        this.password = password;
    }

    /**
     * Opens an connection to the FTP Server and retrieves a file
     * out of the configured working Directory as an {@link InputStream}.
     * The connection is closed afterwards.
     *
     * @param name The name of the file
     * @return An {@link InputStream} for this file
     * @throws IOException If the file could not be read or an connection could not be opened.
     */
    public InputStream getFileStream(String name) throws IOException {
        FTPClient ftp = null;
        try {
            /* Try to connect to the server */
            ftp = new FTPClient();
            ftp.connect(host);
            if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
                ftp.disconnect();
                throw new IOException("Exception when connecting to FTP Server: " + ftp);
            }
            ftp.login(username, password);
            ftp.changeWorkingDirectory(workingDirectory);

            /* Return the file as InputStream */
            return ftp.retrieveFileStream(name);
        } catch (Exception e) {
            throw new IOException(e);
        } finally {
            try {
                if (ftp != null) {
                    if (ftp.isConnected()) {
                        ftp.disconnect();
                    }
                }
            } catch (IOException e) {
                // do nothing
            }
        }
    }

    @Override
    public Set<FileElement> listFiles() throws IOException {
        FTPClient ftp = null;
        try {
            /* Try to connect to the server */
            ftp = new FTPClient();
            ftp.connect(host);
            if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
                ftp.disconnect();
                throw new IOException("Exception when connecting to FTP Server: " + ftp);
            }
            ftp.login(username, password);
            ftp.changeWorkingDirectory(workingDirectory);

            /* Get a list of all files in the directory for monitoring */
            Set<FileElement> result = new LinkedHashSet<FileElement>();
            for (FTPFile file : ftp.listFiles(workingDirectory)) {
                result.add(new FtpFile(file));
            }
            return result;
        } catch (Exception e) {
            throw new IOException(e);
        } finally {
            try {
                if (ftp != null) {
                    if (ftp.isConnected()) {
                        ftp.disconnect();
                    }
                }
            } catch (IOException e) {
                // do nothing
            }
        }
    }
}
