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
package org.openhab.binding.magentatv.internal.utils;

import java.text.MessageFormat;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MagentaTVLogger} class implements a wrapper for slf4j.Logger and
 * inserts a prefix to every messages, which makes it easier to read between all
 * the other messages.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class MagentaTVLogger {
    private Logger logger;

    public MagentaTVLogger(Class<?> clazz, String module) {
        logger = LoggerFactory.getLogger(clazz);
    }

    public void fatal(String message, Object... a) {
        logger.warn("FATAL: {}", fmt(message, a));
    }

    public void info(String message, Object... a) {
        logger.info("{}", fmt(message, a));
    }

    public void debug(String message, Object... a) {
        logger.debug("{}", fmt(message, a));
    }

    public void trace(String message, Object... a) {
        logger.trace("{}", fmt(message, a));
    }

    public void exception(Exception e, String message, Object... a) {
        logger.warn("FATAL: {} - {} ({})", MessageFormat.format(message, a), e.getMessage(), e.getClass());
        logger.debug("Stack Trace:\n{}", MagentaTVException.stackTrace(e));
    }

    static String fmt(String message, Object... a) {
        return MessageFormat.format(message, a);
    }

}
