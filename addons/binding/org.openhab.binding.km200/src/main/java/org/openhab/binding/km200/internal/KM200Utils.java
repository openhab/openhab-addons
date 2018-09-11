/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.km200.internal;

/**
 * The KM200Utils is a class with common utilities.
 *
 * @author Markus Eckhardt - Initial contribution
 *
 */
public class KM200Utils {
    /**
     * Translates a service name to a service path (Replaces # through /)
     *
     * @param name
     */
    public static String translatesNameToPath(String name) {
        return name.replace("#", "/");
    }

    /**
     * Translates a service path to a service name (Replaces / through #)
     *
     * @param name
     */
    public static String translatesPathToName(String path) {
        return path.replace("/", "#");
    }
}
