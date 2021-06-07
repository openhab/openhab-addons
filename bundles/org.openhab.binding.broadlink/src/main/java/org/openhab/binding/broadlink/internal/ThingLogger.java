/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.broadlink.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.*;
import org.slf4j.Logger;

/**
 * Handles logging on behalf of a given Thing.
 *
 * @author John Marshall/Cato Sognen - Initial contribution
 */
@NonNullByDefault
public final class ThingLogger {

    private static final String STANDARD_LOG_PREFIX_WITH_MSG_FORMAT = "{}[{}]: {}";
    private static final String EXCEPTION_DUMP_PREFIX = "Broadlink Exception: ";

    private final Thing thing;
    private final Logger logger;

    public ThingLogger(Thing thing, Logger logger) {
        this.thing = thing;
        this.logger = logger;
    }

    String describeThing() {
        return thing.getUID().toString().replaceFirst("^broadlink:", "");
    }

    String describeStatus() {
        if (Utils.isOnline(thing)) {
            return "^";
        }
        if (Utils.isOffline(thing)) {
            return "v";
        }
        return "?";
    }

    Object[] prependDescription(Object... args) {
        Object[] allArgs = new Object[args.length + 2];
        allArgs[0] = describeThing();
        allArgs[1] = describeStatus();
        System.arraycopy(args, 0, allArgs, 2, args.length);
        return allArgs;
    }

    Object[] appendMessage(Object[] args, String msg) {
        Object[] allArgs = new Object[args.length + 1];
        System.arraycopy(args, 0, allArgs, 0, args.length);
        allArgs[args.length] = msg;
        return allArgs;
    }

    public void logDebug(String msg) {
        if (logger.isDebugEnabled()) {
            logger.debug(STANDARD_LOG_PREFIX_WITH_MSG_FORMAT, appendMessage(prependDescription(), msg));
        }
    }

    /**
     * @param msg - a message describing an error
     * @param t - zero-or-one Throwables to be logged
     */
    public void logError(String msg, Throwable... t) {
        // BETA 8 - errors logged with their throwable to assist diagnosis
        logger.error(STANDARD_LOG_PREFIX_WITH_MSG_FORMAT, appendMessage(prependDescription(), msg));

        if (t.length > 0) {
            logger.error(EXCEPTION_DUMP_PREFIX, t[0]);
        }
    }

    public void logWarn(String msg) {
        logger.warn(STANDARD_LOG_PREFIX_WITH_MSG_FORMAT, appendMessage(prependDescription(), msg));
    }

    public void logInfo(String msg) {
        logger.info(STANDARD_LOG_PREFIX_WITH_MSG_FORMAT, appendMessage(prependDescription(), msg));
    }

    public void logTrace(String msg) {
        if (logger.isTraceEnabled()) {
            logger.trace(STANDARD_LOG_PREFIX_WITH_MSG_FORMAT, appendMessage(prependDescription(), msg));
        }
    }
}
