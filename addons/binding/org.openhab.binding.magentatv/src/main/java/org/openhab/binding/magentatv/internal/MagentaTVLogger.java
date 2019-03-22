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
package org.openhab.binding.magentatv.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MagentaTVLogger} class implements a wrapper for slf4j.Logger and
 * inserts a prefix to every messages, which makes it easier to read between all
 * the other messages.
 *
 * @author Markus Michels - Initial contribution
 */
public class MagentaTVLogger {
    private Logger logger;
    private static final String binding = "MagentaTV";
    private final String prefix;

    public MagentaTVLogger(Class<?> clazz, String module) {
        logger = LoggerFactory.getLogger(clazz);
        prefix = "MagentaTV." + module + ": ";
    }

    public void fatal(String message, Object... a) {
        logger.info(prefix + " - FATAL: " + message, a);
    }

    public void exception(String message, Exception e) {
        logger.info(prefix + " - EXCEPTION: {} - {} ({})", message, e.getMessage(), e.getClass());
    }

    public void info(String message, Object... a) {
        logger.info(prefix + message, a);
    }

    public void debug(String message, Object... a) {
        logger.debug(prefix + message, a);
    }

    public void trace(String message, Object... a) {
        logger.trace(prefix + message, a);
    }
}
