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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.event.Level;

/**
 * {@link Slf4jOutputStream} version using a {@link ThreadLocal} buffer.
 *
 * @author Florian Hotze - Initial contribution
 */
@NonNullByDefault
public class ThreadLocalSlf4jOutputStream extends Slf4jOutputStream {
    private final ThreadLocal<ByteArrayOutputStream> buffer = ThreadLocal.withInitial(ByteArrayOutputStream::new);

    public ThreadLocalSlf4jOutputStream(Logger logger, Level level) {
        super(logger, level);
    }

    @Override
    protected ByteArrayOutputStream getBuffer() {
        return buffer.get();
    }
}
