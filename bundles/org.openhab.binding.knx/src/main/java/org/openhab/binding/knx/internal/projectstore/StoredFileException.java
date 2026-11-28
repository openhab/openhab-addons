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
package org.openhab.binding.knx.internal.projectstore;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A stored project file that cannot be used. Its text is fixed and never holds anything read from the file.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class StoredFileException extends IOException {

    private static final long serialVersionUID = 1L;

    private final boolean newerVersion;

    StoredFileException() {
        this(false);
    }

    StoredFileException(boolean newerVersion) {
        super(newerVersion ? "The stored project file was written by a newer version."
                : "The stored project file is not readable.");
        this.newerVersion = newerVersion;
    }

    /** {@return true if the file is of a format version this binding does not know} */
    boolean newerVersion() {
        return newerVersion;
    }
}
