/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.yeelight.internal.lib;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The {@link CommonLogger} is responsible for logging.
 *
 * @author Coaster Li - Initial contribution
 */
public class CommonLogger {

    private static final boolean DEBUG = false;
    private static Logger sLogger = Logger.getLogger("YeelightLib");

    public static void debug(String tag, String msg) {
        if (DEBUG) {
            sLogger.log(Level.INFO, tag + ":" + msg);
        }
    }

    public static void debug(String msg) {
        if (DEBUG) {
            sLogger.log(Level.INFO, msg);
        }
    }

    public static void warning(String msg) {
        if (DEBUG) {
            sLogger.warning(msg);
        }
    }
}
