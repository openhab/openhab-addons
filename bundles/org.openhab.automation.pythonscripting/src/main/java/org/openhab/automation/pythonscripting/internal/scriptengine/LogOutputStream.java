/**
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.automation.pythonscripting.internal.scriptengine;

import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.event.Level;

/**
 * LogOutputStream implementation
 *
 * @author Holger Hees - initial contribution
 */
public class LogOutputStream extends OutputStream {
    private Logger logger = null;

    private Level level;

    private String mem;

    /**
     * Creates a new log output stream which logs bytes to the specified logger with the specified
     * level.
     *
     * @param logger the logger where to log the written bytes
     * @param level the level
     */
    public LogOutputStream(Logger logger, Level level) {
        this.logger = logger;
        this.level = level;
        mem = "";
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    /**
     * Writes a byte to the output stream. This method flushes automatically at the end of a line.
     *
     * @param b
     */
    @Override
    public void write(int b) {
        byte[] bytes = new byte[1];
        bytes[0] = (byte) (b & 0xff);
        mem = mem + new String(bytes);

        if (mem.endsWith("\n")) {
            mem = mem.substring(0, mem.length() - 1);
            flush();
        }
    }

    /**
     * Flushes the output stream.
     */
    @Override
    public void flush() {
        logger.atLevel(level).log(mem);
        mem = "";
    }
}
