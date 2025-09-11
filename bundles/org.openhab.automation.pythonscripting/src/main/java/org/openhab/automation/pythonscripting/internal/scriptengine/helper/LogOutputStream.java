/*
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
package org.openhab.automation.pythonscripting.internal.scriptengine.helper;

import java.io.OutputStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.event.Level;

/**
 * LogOutputStream implementation
 *
 * @author Holger Hees - Initial contribution
 */
@NonNullByDefault
public class LogOutputStream extends OutputStream {
    private static final int DEFAULT_BUFFER_LENGTH = 2048;
    private static final String LINE_SEPARATOR = System.lineSeparator();
    private static final int LINE_SEPARATOR_SIZE = LINE_SEPARATOR.length();

    private Logger logger;
    private Level level;

    private int bufLength;
    private byte[] buf;
    private int count;

    public LogOutputStream(Logger logger, Level level) {
        this.logger = logger;
        this.level = level;

        bufLength = DEFAULT_BUFFER_LENGTH;
        buf = new byte[DEFAULT_BUFFER_LENGTH];
        count = 0;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public Logger getLogger() {
        return logger;
    }

    @Override
    public void write(int b) {
        // don't log nulls
        if (b == 0) {
            return;
        }

        if (count == bufLength) {
            growBuffer();
        }

        buf[count] = (byte) b;
        count++;
    }

    @Override
    public void flush() {
        if (count == 0) {
            return;
        }

        // don't print out blank lines;
        if (count == LINE_SEPARATOR_SIZE) {
            if (((char) buf[0]) == LINE_SEPARATOR.charAt(0)
                    && ((count == 1) || ((count == 2) && ((char) buf[1]) == LINE_SEPARATOR.charAt(1)))) {
                reset();
                return;
            }
        } else if (count > LINE_SEPARATOR_SIZE) {
            // remove linebreaks at the end
            if (((char) buf[count - 1]) == LINE_SEPARATOR.charAt(LINE_SEPARATOR_SIZE - 1)
                    && ((LINE_SEPARATOR_SIZE == 1) || ((LINE_SEPARATOR_SIZE == 2)
                            && ((char) buf[count - 1]) == LINE_SEPARATOR.charAt(LINE_SEPARATOR_SIZE - 2)))) {
                count -= LINE_SEPARATOR_SIZE;
            }
        }

        final byte[] line = new byte[count];
        System.arraycopy(buf, 0, line, 0, count);
        logger.atLevel(level).log(new String(line));
        reset();
    }

    private void growBuffer() {
        final int newBufLength = bufLength + DEFAULT_BUFFER_LENGTH;
        final byte[] newBuf = new byte[newBufLength];
        System.arraycopy(buf, 0, newBuf, 0, bufLength);
        buf = newBuf;
        bufLength = newBufLength;
    }

    private void reset() {
        // don't shrink buffer. assuming that if it grew that it will likely grow similarly again
        count = 0;
    }
}
