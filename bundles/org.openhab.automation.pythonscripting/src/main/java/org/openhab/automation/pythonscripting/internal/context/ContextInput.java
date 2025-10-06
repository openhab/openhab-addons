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
import java.io.InputStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * ContextInput wraps an @nullable InputStream, used as Standard Input for pythonscripting
 *
 * @author Holger Hees - Initial contribution
 */
@NonNullByDefault
public class ContextInput extends InputStream {
    private @Nullable InputStream stream;

    public ContextInput(@Nullable InputStream stream) {
        this.stream = stream;
    }

    public void setInputStream(@Nullable InputStream stream) {
        this.stream = stream;
    }

    @Override
    public void close() throws IOException {
        if (stream != null) {
            stream.close();
        }
    }

    @Override
    public int read() throws IOException {
        if (stream != null) {
            return stream.read();
        }
        return -1;
    }
}
