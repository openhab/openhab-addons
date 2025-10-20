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
package org.openhab.automation.pythonscripting.internal.context;

import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * ContextOutput wraps an @nullable OutputStream, used as Standard Output for pythonscripting
 *
 * @author Holger Hees - Initial contribution
 */
@NonNullByDefault
public class ContextOutput extends OutputStream {
    private OutputStream stream;

    public ContextOutput(OutputStream stream) {
        this.stream = stream;
    }

    public void setOutputStream(OutputStream stream) {
        this.stream = stream;
    }

    @Override
    public void close() throws IOException {
        stream.close();
    }

    @Override
    public void flush() throws IOException {
        stream.flush();
    }

    @Override
    public void write(int b) throws IOException {
        stream.write(b);
    }
}
