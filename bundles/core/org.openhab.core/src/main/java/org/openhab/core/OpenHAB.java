/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.core;

import org.apache.commons.lang.StringUtils;
import org.osgi.framework.FrameworkUtil;

/**
 * Some core static methods that provide information about the running openHAB instance.
 *
 * @author Kai Kreuzer
 *
 */
public class OpenHAB {
    /**
     * Returns the current openHAB version, retrieving the information from the core bundle version.
     *
     * @return the openHAB runtime version
     */
    static public String getVersion() {
        String versionString = FrameworkUtil.getBundle(OpenHAB.class).getVersion().toString();
        // if the version string contains a qualifier, remove it!
        if (StringUtils.countMatches(versionString, ".") == 3) {
            versionString = StringUtils.substringBeforeLast(versionString, ".");
        }
        return versionString;
    }

    /**
     * Returns the current openHAB build id, retrieving the information from the core bundle version.
     * If the id is "qualifier", i.e. openHAB is run from within the IDE, it returns an empty string.
     *
     * @return the openHAB build id or an empty string, if none is avaiable
     */
    static public String getBuildId() {
        String versionString = FrameworkUtil.getBundle(OpenHAB.class).getVersion().toString();
        String buildString = "";
        if (StringUtils.countMatches(versionString, ".") == 3) {
            buildString = StringUtils.substringAfterLast(versionString, ".");
            if (buildString.equals("qualifier")) {
                buildString = "";
            }
        }
        return buildString;
    }

}
