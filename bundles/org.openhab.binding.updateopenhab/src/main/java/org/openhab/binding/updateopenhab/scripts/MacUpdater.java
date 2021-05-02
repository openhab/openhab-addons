/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.updateopenhab.scripts;

import java.io.File;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.updateopenhab.internal.TargetVersion;
import org.openhab.core.OpenHAB;

/**
 * The {@link MacUpdater} is the shell script for updating OpenHab on this OS
 *
 * @author AndrewFG - initial contribution
 */
@NonNullByDefault
public class MacUpdater extends BaseUpdater {

    private static final String EXT = ".sh";
    private static final String DIRECTORY = OpenHAB.getConfigFolder().replace("conf", "runtime/bin/");
    private static final String FILENAME = DIRECTORY + FILE_ID + EXT;

    private final String[] commands = new String[] { "sh", FILENAME };

    private final String contents =
    // @formatter:off
            "#!/bin/sh\n"+
            "cd " + DIRECTORY + "\n" +
            "./stop\n" +
            "sleep 30\n" +
//            "./backup\n" +
//            "./update " + getLatestVersion() +"\n" +
            "./karaf\n";
    // @formatter:on

    public MacUpdater(TargetVersion targetVersion, String password) {
        super(targetVersion, password);
    }

    @Override
    protected boolean prerequisitesOk() {
        if (!super.prerequisitesOk()) {
            return false;
        }
        if (!writeFile(FILENAME, contents)) {
            logger.warn("Cannot run OpenHAB update script: error writing {}", FILENAME);
            return false;
        }
        return true;
    }

    @Override
    public void run() {
        if (prerequisitesOk()) {
            ProcessBuilder builder = new ProcessBuilder();
            for (String arg : commands) {
                builder.command().add(arg);
            }
            builder.directory(new File(DIRECTORY));
            executeProcess(builder);
        }
    }
}
