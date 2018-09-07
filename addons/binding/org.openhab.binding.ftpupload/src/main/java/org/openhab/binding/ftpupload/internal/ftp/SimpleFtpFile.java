/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ftpupload.internal.ftp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.apache.ftpserver.ftplet.FtpFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple FTP file implementation.
 *
 *
 * @author Pauli Anttila - Initial contribution
 */
public class SimpleFtpFile implements FtpFile {
    private Logger logger = LoggerFactory.getLogger(SimpleFtpFile.class);

    MyOutputStream file;

    public byte[] getData() {
        return file.getData();
    }

    @Override
    public InputStream createInputStream(long arg0) throws IOException {
        logger.trace("createInputStream: {}", arg0);
        return null;
    }

    @Override
    public OutputStream createOutputStream(long arg0) throws IOException {
        logger.trace("createOutputStream: {}", arg0);
        file = new MyOutputStream();
        return file;
    }

    @Override
    public boolean delete() {
        logger.trace("delete");
        return false;
    }

    @Override
    public boolean doesExist() {
        logger.trace("doesExist");
        return false;
    }

    @Override
    public String getAbsolutePath() {
        logger.trace("getAbsolutePath");
        return "/";
    }

    @Override
    public String getGroupName() {
        logger.trace("getGroupName");
        return null;
    }

    @Override
    public long getLastModified() {
        logger.trace("getLastModified");
        return 0;
    }

    @Override
    public int getLinkCount() {
        logger.trace("getLinkCount");
        return 0;
    }

    @Override
    public String getName() {
        logger.trace("getName");
        return "";
    }

    @Override
    public String getOwnerName() {
        logger.trace("getOwnerName");
        return null;
    }

    @Override
    public long getSize() {
        logger.trace("getSize");
        return 0;
    }

    @Override
    public boolean isDirectory() {
        logger.trace("isDirectory");
        return false;
    }

    @Override
    public boolean isFile() {
        logger.trace("isFile");
        return false;
    }

    @Override
    public boolean isHidden() {
        logger.trace("isHidden");
        return false;
    }

    @Override
    public boolean isReadable() {
        logger.trace("isReadable");
        return false;
    }

    @Override
    public boolean isRemovable() {
        logger.trace("isRemovable");
        return false;
    }

    @Override
    public boolean isWritable() {
        logger.trace("isWritable");
        return true;
    }

    @Override
    public List<FtpFile> listFiles() {
        logger.trace("listFiles");
        return null;
    }

    @Override
    public boolean mkdir() {
        logger.trace("mkdir");
        return false;
    }

    @Override
    public boolean move(FtpFile arg0) {
        logger.trace("move: {}", arg0);
        return false;
    }

    @Override
    public boolean setLastModified(long arg0) {
        logger.trace("setLastModified: {}", arg0);
        return false;
    }

    @Override
    public Object getPhysicalFile() {
        logger.trace("getPhysicalFile");
        return null;
    }

    private class MyOutputStream extends OutputStream {
        private StringBuilder data = new StringBuilder();

        @Override
        public void write(int b) throws IOException {
            data.append(String.format("%02X", (byte) b));
        }

        public byte[] getData() {
            try {
                byte[] d = DatatypeConverter.parseHexBinary(data.toString());
                logger.debug("File len: {}", d.length);
                return d;
            } catch (IllegalArgumentException e) {
                logger.debug("Exception occured during data conversion: {}", e.getMessage());
            }
            return null;
        }
    }
}
