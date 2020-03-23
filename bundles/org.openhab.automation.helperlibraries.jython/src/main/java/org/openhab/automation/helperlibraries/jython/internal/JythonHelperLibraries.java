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
package org.openhab.automation.helperlibraries.jython.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class adds the Jython core and community helper libraries to `python.path` in the system properties.
 *
 * @author Scott Rushworth - Initial contribution
 */
@Component
@NonNullByDefault
public class JythonHelperLibraries {

    public JythonHelperLibraries() {
    }

    @Activate
    protected void setPythonPath() {
        final Logger logger = LoggerFactory.getLogger(JythonHelperLibraries.class);

        logger.trace("Loading Jython helper libraries");

        final String HELPER_LIBRARY_PYTHON_PATH = JythonHelperLibraries.class.getProtectionDomain().getCodeSource()
                .getLocation().toString().replace("file:", "");

        String existingPythonPath = System.getProperty("python.path");
        if (existingPythonPath == null || existingPythonPath.isEmpty()) {
            System.setProperty("python.path", HELPER_LIBRARY_PYTHON_PATH);
        } else if (!existingPythonPath.contains(HELPER_LIBRARY_PYTHON_PATH)) {
            TreeSet<String> newPythonPathList = new TreeSet<>(
                    new ArrayList<String>(Arrays.asList(existingPythonPath.split(File.pathSeparator))));
            newPythonPathList.add(HELPER_LIBRARY_PYTHON_PATH);
            String newPythonPath = String.join(File.pathSeparator, newPythonPathList);
            System.setProperty("python.path", newPythonPath);
        }

        logger.trace("python.home [{}], python.path [{}]", System.getProperty("python.home"),
                System.getProperty("python.path"));
    }

}
