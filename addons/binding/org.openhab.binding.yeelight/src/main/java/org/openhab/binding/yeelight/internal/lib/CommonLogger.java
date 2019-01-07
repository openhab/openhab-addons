/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
