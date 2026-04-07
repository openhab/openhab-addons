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
package org.openhab.automation.jsscripting.internal.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.event.Level;

/**
 * A {@link OutputStream} implementation that redirects to SLF4J logging.
 *
 * @author Florian Hotze - Initial contribution
 */
@NonNullByDefault
public class Slf4jOutputStream extends OutputStream {
    private final Logger logger;
    private final Level level;
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    public Slf4jOutputStream(Logger logger, Level level) {
        this.logger = logger;
        this.level = level;
    }

    protected ByteArrayOutputStream getBuffer() {
        return buffer;
    }

    @Override
    public void write(int b) {
        if (b == '\n') {
            flush();
        } else {
            getBuffer().write(b);
        }
    }

    @Override
    public void flush() {
        var buffer = getBuffer();
        if (buffer.size() > 0) {
            logger.atLevel(level).log(buffer.toString());
            buffer.reset();
        }
    }

    @Override
    public void close() throws IOException {
        flush();
        getBuffer().close();
        super.close();
    }
}
