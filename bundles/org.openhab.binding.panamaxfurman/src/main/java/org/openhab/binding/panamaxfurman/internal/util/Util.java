/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.panamaxfurman.internal.util;

import java.io.Closeable;
import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for miscellaneous utility methods
 *
 * @author Dave Badia - Initial contribution
 */
@NonNullByDefault
public final class Util {

    private Util() {
        // utility class
    }

    /**
     * Helper method to close streams without throwing any exceptions
     *
     * @param closeable the object to close
     */
    public static void closeQuietly(@Nullable Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException e) {
            LoggerFactory.getLogger(Util.class).debug("Got exception during close of {}", closeable.getClass(), e);
        }
    }

    /**
     * Helper method to only log stack traces when debugging is active
     *
     * @param logger the logger
     * @param exception the exception
     *
     * @return the Exception object to be passed to the logger as the last argument. Non-null only when debug logging is
     *         enabled
     */
    public static @Nullable Exception exceptionToLog(Logger logger, Exception e) {
        if (logger.isDebugEnabled()) {
            return e;
        } else {
            return null;
        }
    }
}
