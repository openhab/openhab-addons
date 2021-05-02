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
 * The {@link WindowsUpdater} is the shell script for updating OpenHab on this OS
 *
 * @author AndrewFG - Initial contribution
 */
@NonNullByDefault
public class WindowsUpdater extends BaseUpdater {

    private static final String EXT = ".cmd";
    private static final String DIRECTORY = OpenHAB.getConfigFolder().replace("conf", "runtime\\bin\\");
    private static final String FILENAME = DIRECTORY + FILE_ID + EXT;

    private final String[] commands = new String[] { "cmd.exe", "/C", FILENAME };

    private final String contents =
    // @formatter:off
            "@echo off\n" +
            "cd " + DIRECTORY + "\n" +
            "call stop.bat\n" +
            "timeout /t 30\n" +
//            "call backup.bat\n" +
//            "call update.bat " + getLatestVersion() +"\n" +
            "call karaf.bat\n";
    // @formatter:on

    public WindowsUpdater(TargetVersion targetVersion, String password) {
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
