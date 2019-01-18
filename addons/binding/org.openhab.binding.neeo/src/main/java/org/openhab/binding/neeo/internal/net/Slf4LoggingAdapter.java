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
package org.openhab.binding.neeo.internal.net;

import java.util.Objects;
import java.util.logging.LogRecord;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;

/**
 * Logging adapter to use for Slf4j
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
class Slf4LoggingAdapter extends java.util.logging.Logger {

    /** The logger. */
    private final Logger logger;

    /**
     * Creates the logging adapter from the given logger
     *
     * @param logger a non-null logger to use
     */
    protected Slf4LoggingAdapter(Logger logger) {
        super("jersey", null);
        Objects.requireNonNull(logger, "logger cannot be null");
        this.logger = logger;
    }

    @Override
    public void log(@Nullable LogRecord record) {
        logger.debug("{}", record == null ? "" : record.getMessage());
    }
}
