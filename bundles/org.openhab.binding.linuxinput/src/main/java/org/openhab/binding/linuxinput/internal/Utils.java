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
package org.openhab.binding.linuxinput.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Utilities
 *
 * @author Thomas Weißschuh - Initial contribution
 */
@NonNullByDefault
class Utils {
    private Utils() {
    }

    static Thread backgroundThread(Runnable r, Class<?> clazz, @Nullable String instance) {
        String name = LinuxInputBindingConstants.BINDING_ID + " :: " + clazz.getSimpleName();
        if (instance != null) {
            name += " :: " + instance;
        }
        Thread t = new Thread(r, name);
        t.setDaemon(true);
        return t;
    }
}
