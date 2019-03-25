/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.yamahareceiver.internal.protocol;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.smarthome.config.core.ConfigConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a connection to the AVR. Is implemented by the XMLConnection and JSONConnection.
 *
 * @author David Graeff - Initial contribution
 * @author Tomasz Maruszak - Refactoring
 */
public abstract class AbstractConnection {
    private Logger logger = LoggerFactory.getLogger(AbstractConnection.class);

    protected String host;
    protected boolean protocolSnifferEnabled;
    protected FileOutputStream debugOutStream;

    /**
     * Creates a connection with the given host.
     * Enables sniffing of the communication to the AVR if the logger level is set to trace
     * when the addon is being loaded.
     *
     * @param host
     */
    public AbstractConnection(String host) {
        this.host = host;
        setProtocolSnifferEnable(logger.isTraceEnabled());
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getHost() {
        return host;
    }

    public void setProtocolSnifferEnable(boolean enable) {

        if (enable) {
            File pathWithoutFilename = new File(ConfigConstants.getUserDataFolder());
            pathWithoutFilename.mkdirs();
            File file = new File(pathWithoutFilename, "yamaha_trace.log");
            if (file.exists()) {
                file.delete();
            }
            logger.warn("Protocol sniffing for Yamaha Receiver Addon is enabled. Performance may suffer! Writing to {}",
                    file.getAbsolutePath());
            try {
                debugOutStream = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                debugOutStream = null;
                logger.trace("Protocol log file not found!!", e);
            }
        } else if (protocolSnifferEnabled) {
            // Close stream if protocol sniffing will be disabled
            try {
                debugOutStream.close();
            } catch (Exception e) {
            }
            debugOutStream = null;
        }
        this.protocolSnifferEnabled = enable;
    }

    protected void writeTraceFile(String message) {
        if (protocolSnifferEnabled && debugOutStream != null) {
            try {
                debugOutStream.write(message.replace('\n', ' ').getBytes());
                debugOutStream.write('\n');
                debugOutStream.write('\n');
                debugOutStream.flush();
            } catch (IOException e) {
                logger.trace("Writing trace file failed", e);
            }
        }
    }

    /**
     * Implement this for a pure send.
     *
     * @param message The message to send. Must be xml or json already.
     * @throws IOException
     */
    public abstract void send(String message) throws IOException;

    /**
     * Implement this for a send/receive.
     *
     * @param message The message to send. Must be xml or json already.
     * @throws IOException
     */
    public abstract String sendReceive(String message) throws IOException;
}
