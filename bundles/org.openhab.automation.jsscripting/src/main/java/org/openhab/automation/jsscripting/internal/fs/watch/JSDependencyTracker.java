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
package org.openhab.automation.jsscripting.internal.fs.watch;

import java.io.File;

import org.openhab.core.OpenHAB;
import org.openhab.core.automation.module.script.rulesupport.loader.DependencyTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tracks JS module dependencies
 *
 * @author Jonathan Gilbert - Initial contribution
 */
public class JSDependencyTracker extends DependencyTracker {

    private final Logger logger = LoggerFactory.getLogger(JSDependencyTracker.class);

    public static final String LIB_PATH = String.join(File.separator, OpenHAB.getConfigFolder(), "automation", "js",
            "node_modules");

    public JSDependencyTracker() {
        super(LIB_PATH);
    }

    public void activate() {
        File directory = new File(LIB_PATH);
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                logger.warn("Failed to create watched directory: {}", LIB_PATH);
            }
        } else if (directory.isFile()) {
            logger.warn("Trying to watch directory {}, however it is a file", LIB_PATH);
        }

        super.activate();
    }
}
